/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.tongrenlu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import info.tongrenlu.domain.TrackBean;

/**
 * A full screen player that shows the current playing music with a background image
 * depicting the album art. The activity also has controls to seek/pause/play the audio.
 */
public class FullScreenPlayerActivity extends ActionBarActivity {

    private static final String TAG = FullScreenPlayerActivity.class.getName();
    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;

    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };
    private final ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ImageView mSkipPrev;
    private ImageView mSkipNext;
    private ImageView mPlayPause;
    private TextView mStart;
    private TextView mEnd;
    private SeekBar mSeekbar;
    private TextView mLine1;
    private TextView mLine2;
    private TextView mLine3;
    private ProgressBar mLoading;
    private View mControllers;
    private Drawable mPauseDrawable;
    private Drawable mPlayDrawable;
    private ImageView mBackgroundImage;
    private Uri mCurrentArtUri;
    private Handler mHandler = new Handler();
    private ScheduledFuture<?> mScheduleFuture;
    private PlaybackStateCompat mLastPlaybackState;

    private LocalBroadcastManager mLocalBroadcastManager = null;
    private BroadcastReceiver mPlaybackStateReceiver = null;

    public static String toTime(final int time) {
        final int time2 = time / 1000;
        final int minute = time2 / 60;
        final int second = time2 % 60;
        return String.format("%02d:%02d", minute, second);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_player);

        mBackgroundImage = (ImageView) findViewById(R.id.background_image);

        mPauseDrawable = this.getResources().getDrawable(R.drawable.ic_pause_white_48dp);
        mPlayDrawable = this.getResources().getDrawable(R.drawable.ic_play_arrow_white_48dp);
        mPlayPause = (ImageView) findViewById(R.id.imageView1);
        mSkipNext = (ImageView) findViewById(R.id.next);
        mSkipPrev = (ImageView) findViewById(R.id.prev);
        mStart = (TextView) findViewById(R.id.startText);
        mEnd = (TextView) findViewById(R.id.endText);
        mSeekbar = (SeekBar) findViewById(R.id.seekBar1);
        mLine1 = (TextView) findViewById(R.id.line1);
        mLine2 = (TextView) findViewById(R.id.line2);
        mLine3 = (TextView) findViewById(R.id.line3);
        mLoading = (ProgressBar) findViewById(R.id.progressBar1);
        mControllers = findViewById(R.id.controllers);

        mSkipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MusicService.class);
                intent.setAction(MusicService.CMD_NEXT);
                startService(intent);
            }
        });

        mSkipPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MusicService.class);
                intent.setAction(MusicService.CMD_PREV);
                startService(intent);
            }
        });

        mPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MusicService.class);
                intent.setAction(MusicService.CMD_TOGGLE_PLAYBACK);
                startService(intent);
            }
        });

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // mStart.setText(Utils.formatMillis(progress));
                mStart.setText(toTime(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopProgressUpdate();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Intent intent = new Intent(getApplicationContext(), MusicService.class);
                intent.setAction(MusicService.CMD_SEEK_TO);
                intent.putExtra(MusicService.PARAM_PROGRESS, seekBar.getProgress());
                startService(intent);

                scheduleProgressUpdate();
            }
        });

        // Only update from the intent if we are not recreating from a config change:
        if (savedInstanceState == null) {
            updateFromParams(getIntent());
        }

        this.mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        mPlaybackStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                switch (intent.getAction()) {
                    case MusicService.CMD_PLAYBACK_STATE:
                        String receiver = intent.getStringExtra(MusicService.PARAM_RECEIVER);
                        if (receiver == null || TAG.equals(receiver)) {
                            updateFromService(intent);
                            PlaybackStateCompat state = intent.getParcelableExtra(MusicService.PARAM_STATE);
                            updatePlaybackState(state);
                            updateProgress();
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void updateFromParams(Intent intent) {
        String title = intent.getStringExtra(MusicService.PARAM_TITLE);
        if (!TextUtils.isEmpty(title)) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(title);
        }

        Uri artUri = intent.getParcelableExtra(MusicService.PARAM_COVER);
        if (artUri != null) {
            mCurrentArtUri = artUri;
            Picasso.with(this.getApplicationContext()).load(mCurrentArtUri).into(mBackgroundImage);
        }
    }

    private void scheduleProgressUpdate() {
        stopProgressUpdate();
        if (!mExecutorService.isShutdown()) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(new Runnable() {
                                                                       @Override
                                                                       public void run() {
                                                                           mHandler.post(
                                                                                   mUpdateProgressTask);
                                                                       }
                                                                   },
                                                                   PROGRESS_UPDATE_INITIAL_INTERVAL,
                                                                   PROGRESS_UPDATE_INTERNAL,
                                                                   TimeUnit.MILLISECONDS);
        }
    }

    private void stopProgressUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        scheduleProgressUpdate();

        mLocalBroadcastManager.registerReceiver(mPlaybackStateReceiver,
                                                new IntentFilter(MusicService.CMD_PLAYBACK_STATE));

        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(MusicService.CMD_PLAYBACK_STATE);
        intent.putExtra(MusicService.PARAM_RECEIVER, TAG);
        this.startService(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        stopProgressUpdate();
        mLocalBroadcastManager.unregisterReceiver(mPlaybackStateReceiver);

        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(MusicService.CMD_RELEASE);
        this.startService(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopProgressUpdate();
        mExecutorService.shutdown();
    }

    private void updateFromService(Intent intent) {
        Log.d(TAG, "updateMedia called ");

        List<TrackBean> trackList = intent.getParcelableArrayListExtra(MusicService.PARAM_TRACK_LIST);
        int position = intent.getIntExtra(MusicService.PARAM_POSITION, 0);
        TrackBean trackBean = trackList.get(position);


        String title = trackBean.getName();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title);
        mLine1.setText(title);

        mCurrentArtUri = Uri.parse("http://files.tongrenlu.info/m" +
                                   trackBean.getArticleId() +
                                   "/cover_400.jpg");
        Picasso.with(getApplicationContext())
               .load(mCurrentArtUri)
               .into(mBackgroundImage);


        int duration = intent.getIntExtra(MusicService.PARAM_DURATION, 0);
        mSeekbar.setMax(duration);
        mEnd.setText(toTime(duration));
    }

    private void updatePlaybackState(PlaybackStateCompat state) {
        if (state == null) {
            return;
        }
        Log.d(TAG, "updatePlaybackState, playback state=" + state.getState());
        mLastPlaybackState = state;

        mLine3.setText("");

        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                mLoading.setVisibility(View.INVISIBLE);
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageDrawable(mPauseDrawable);
                mControllers.setVisibility(View.VISIBLE);
                scheduleProgressUpdate();
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                mControllers.setVisibility(View.VISIBLE);
                mLoading.setVisibility(View.INVISIBLE);
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageDrawable(mPlayDrawable);
                stopProgressUpdate();
                break;
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                mLoading.setVisibility(View.INVISIBLE);
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageDrawable(mPlayDrawable);
                stopProgressUpdate();
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                mPlayPause.setVisibility(View.INVISIBLE);
                mLoading.setVisibility(View.VISIBLE);
                mLine3.setText(R.string.loading);
                stopProgressUpdate();
                break;
            default:
                Log.d(TAG, "Unhandled state " + state.getState());
        }

        mSkipNext.setVisibility((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) == 0
                                ? View.INVISIBLE
                                : View.VISIBLE);
        mSkipPrev.setVisibility((state.getActions() &
                                 PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) == 0
                                ? View.INVISIBLE
                                : View.VISIBLE);
    }

    private void updateProgress() {
        if (mLastPlaybackState == null) {
            return;
        }
        long currentPosition = mLastPlaybackState.getPosition();
        if (mLastPlaybackState.getState() != PlaybackState.STATE_PAUSED) {
            // Calculate the elapsed time between the last position update and now and unless
            // paused, we can assume (delta * speed) + current position is approximately the
            // latest position. This ensure that we do not repeatedly call the getPlaybackState()
            // on MediaController.
            long timeDelta = SystemClock.elapsedRealtime() -
                             mLastPlaybackState.getLastPositionUpdateTime();
            currentPosition += (int) timeDelta * mLastPlaybackState.getPlaybackSpeed();
        }
        mSeekbar.setProgress((int) currentPosition);
    }
}
