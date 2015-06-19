package info.tongrenlu;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import info.tongrenlu.domain.TrackBean;
import info.tongrenlu.music.LocalPlayback;
import info.tongrenlu.music.Playback;

public class MusicService extends Service implements Playback.Callback {

    // A value of a CMD_NAME key in the extras of the incoming Intent that
    // indicates that the music playback should be paused (see {@link #onStartCommand})
    public static final String CMD_PLAY = "CMD_PLAY";
    public static final String CMD_STOP = "CMD_STOP";
    public static final String CMD_TOGGLE_PLAYBACK = "CMD_TOGGLE_PLAYBACK";
    public static final String CMD_PREV = "CMD_PREV";
    public static final String CMD_NEXT = "CMD_NEXT";
    public static final String CMD_PLAYBACK_STATE = "CMD_PLAYBACK_STATE";
    public static final String CMD_SEEK_TO = "CMD_SEEK_TO";
    public static final String CMD_RELEASE = "CMD_RELEASE";
    // A value of a CMD_NAME key that indicates that the music playback should switch
    // to local playback from cast playback.
    public static final String PARAM_TRACK_LIST = "PARAM_TRACK_LIST";
    public static final String PARAM_POSITION = "PARAM_POSITION";
    public static final String PARAM_RECEIVER = "PARAM_RECEIVER";
    public static final String PARAM_STATE = "PARAM_STATE";
    public static final String PARAM_PROGRESS = "PARAM_PROGRESS";

    public static final String PARAM_TITLE = "PARAM_TITLE";
    public static final String PARAM_COVER = "PARAM_COVER";
    public static final String PARAM_DURATION = "PARAM_DURATION";

    public static final String TAG = MusicService.class.getName();
    // Delay stopSelf by using a handler.
    private static final int STOP_DELAY = 30000;

    // "Now playing" queue:
    private ArrayList<TrackBean> mTrackList;
    private ArrayList<TrackBean> mPlayingQueue;
    private int mCurrentIndexOnQueue;
    private MediaNotificationManager mMediaNotificationManager;
    // Indicates whether the service was started.
    private LocalBroadcastManager mLocalBroadcastManager = null;

    private DelayedStopHandler mDelayedStopHandler = new DelayedStopHandler(this);
    private Playback mPlayback;

    public TrackBean getPlaying() {
        return mPlayingQueue.isEmpty() ? null : mPlayingQueue.get(mCurrentIndexOnQueue);

    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    /*
         * (non-Javadoc)
         * @see android.app.Service#onCreate()
         */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        mPlayingQueue = new ArrayList<>();

        mPlayback = new LocalPlayback(this);
        mPlayback.setState(PlaybackStateCompat.STATE_NONE);
        mPlayback.setCallback(this);
        mPlayback.start();

        mMediaNotificationManager = new MediaNotificationManager(this);
        this.mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        updatePlaybackState(null);
    }

    /**
     * (non-Javadoc)
     *
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        if (startIntent != null) {
            String action = startIntent.getAction();


            Log.d(TAG, "onStartCommand. action = " + action);

            switch (action) {
                case CMD_TOGGLE_PLAYBACK:
                    if (mPlayback.isPlaying()) {
                        handlePauseRequest();
                    } else {
                        handlePlayRequest();
                    }
                    break;
                case CMD_PLAY:
                    this.mTrackList = startIntent.getParcelableArrayListExtra(MusicService.PARAM_TRACK_LIST);
                    this.mPlayingQueue = new ArrayList<>(mTrackList);
                    this.mCurrentIndexOnQueue = startIntent.getIntExtra(PARAM_POSITION, 0);
                    handlePlayRequest();
                    break;
                case CMD_PLAYBACK_STATE:
                    sendUpdatePlaybackStateBroadcast(startIntent.getStringExtra(PARAM_RECEIVER));
                    break;
                case CMD_SEEK_TO:
                    mPlayback.seekTo(startIntent.getIntExtra(PARAM_PROGRESS, 0));
                    break;
                case CMD_NEXT:
                    handleNextRequest();
                    break;
                case CMD_PREV:
                    handlePrevRequest();
                    break;
                case CMD_RELEASE:
                    // Reset the delay handler to enqueue a message to stop the service if
                    // nothing is playing.
                    mDelayedStopHandler.removeCallbacksAndMessages(null);
                    mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
                    break;
                case CMD_STOP:
                    handleStopRequest(null);
                    break;
                default:
                    break;
            }
        }
        return START_STICKY;
    }

    /**
     * (non-Javadoc)
     *
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        // Service is being killed, so make sure we release our resources
        handleStopRequest(null);

        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler = null;
    }

    /**
     * Handle a request to play music
     */
    private void handlePlayRequest() {
        Log.d(TAG, "handlePlayRequest: mState=" + mPlayback.getState());
        mDelayedStopHandler.removeCallbacksAndMessages(null);

        TrackBean trackBean = mPlayingQueue.get(mCurrentIndexOnQueue);
        Uri mediaUri = Uri.parse("http://files.tongrenlu.info/m" +
                                 1002 +
                                 "/" +
                                 trackBean.getChecksum() +
                                 ".mp3");
        mPlayback.play(mediaUri);
    }

    /**
     * Handle a request to pause music
     */
    private void handlePauseRequest() {
        Log.d(TAG, "handlePauseRequest: mState=" + mPlayback.getState());
        mPlayback.pause();
        // reset the delayed stop handler.
        mDelayedStopHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Handle a request to stop music
     */
    private void handleStopRequest(String withError) {
        Log.d(TAG, "handleStopRequest: mState=" + mPlayback.getState() + " error=" + withError);
        mPlayback.stop(true);
        // reset the delayed stop handler.
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);

        updatePlaybackState(withError);

        mMediaNotificationManager.stopNotification();
        // service is no longer necessary. Will be started again if needed.
        stopSelf();
    }

    /**
     * Update the current media player state, optionally showing an error message.
     *
     * @param error if not null, error message to present to the user.
     */
    private PlaybackStateCompat buildPlaybackState(String error) {
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (mPlayback != null) {
            position = mPlayback.getCurrentStreamPosition();
        }

        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder().setActions(
                getAvailableActions());

        int state = mPlayback.getState();

        // If there is an error message, send it to the playback state:
        if (error != null) {
            // Error states are really only supposed to be used for errors that cause playback to
            // stop unexpectedly and persist until the user takes action to fix it.
            stateBuilder.setErrorMessage(error);
            state = PlaybackStateCompat.STATE_ERROR;
        }
        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());
        return stateBuilder.build();
    }

    private void updatePlaybackState(String error) {
        Log.d(TAG, "updatePlaybackState, playback state=" + mPlayback.getState());
        PlaybackStateCompat state = buildPlaybackState(error);

        mMediaNotificationManager.onPlaybackStateChanged(state);

    }

    private long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY |
                       PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                       PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH;
        if (mPlayingQueue == null || mPlayingQueue.isEmpty()) {
            return actions;
        }
        if (mPlayback.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        }
        if (mCurrentIndexOnQueue > 0) {
            actions |= PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
        }
        if (mCurrentIndexOnQueue < mPlayingQueue.size() - 1) {
            actions |= PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        }
        return actions;
    }

    /**
     * Implementation of the Playback.Callback interface
     */
    @Override
    public void onCompletion() {
        // The media player finished playing the current song, so we go ahead
        // and start the next.
        handleNextRequest();
    }

    private void handleNextRequest() {
        if (mPlayingQueue != null && !mPlayingQueue.isEmpty()) {
            // In this sample, we restart the playing queue when it gets to the end:
            mCurrentIndexOnQueue++;
            if (mCurrentIndexOnQueue >= mPlayingQueue.size()) {
                mCurrentIndexOnQueue = 0;
            }
            handlePlayRequest();
        } else {
            // If there is nothing to play, we stop and release the resources:
            handleStopRequest(null);
        }
    }

    private void handlePrevRequest() {
        if (mPlayingQueue != null && !mPlayingQueue.isEmpty()) {
            // In this sample, we restart the playing queue when it gets to the end:
            mCurrentIndexOnQueue--;
            if (mCurrentIndexOnQueue < 0) {
                mCurrentIndexOnQueue = mPlayingQueue.size() - 1;
            }
            handlePlayRequest();
        } else {
            // If there is nothing to play, we stop and release the resources:
            handleStopRequest(null);
        }
    }

    private void sendUpdatePlaybackStateBroadcast(String receiver) {
        Intent intent = new Intent(MusicService.CMD_PLAYBACK_STATE);
        intent.putExtra(PARAM_RECEIVER, receiver);
        intent.putExtra(PARAM_STATE, this.buildPlaybackState(null));
        intent.putParcelableArrayListExtra(PARAM_TRACK_LIST, mPlayingQueue);
        intent.putExtra(PARAM_POSITION, mCurrentIndexOnQueue);
        intent.putExtra(PARAM_DURATION, mPlayback.getDuration());
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onPlaybackStatusChanged(int state) {
        updatePlaybackState(null);
        sendUpdatePlaybackStateBroadcast(null);
    }

    @Override
    public void onError(String error) {
        updatePlaybackState(error);
        sendUpdatePlaybackStateBroadcast(null);
    }

    /**
     * A simple handler that stops the service if playback is not active (playing)
     */
    private static class DelayedStopHandler extends Handler {
        private final WeakReference<MusicService> mWeakReference;

        private DelayedStopHandler(MusicService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicService service = mWeakReference.get();
            if (service != null && service.mPlayback != null) {
                if (service.mPlayback.isPlaying()) {
                    return;
                }
                Log.d(TAG, "Stopping service with delay handler.");
                service.stopSelf();
            }
        }
    }

}
