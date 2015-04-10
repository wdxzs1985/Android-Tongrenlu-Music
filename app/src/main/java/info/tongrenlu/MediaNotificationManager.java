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

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.session.PlaybackState;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import info.tongrenlu.domain.TrackBean;

/**
 * Keeps track of a notification and updates it automatically for a given
 * MediaSession. Maintaining a visible notification (usually) guarantees that the music service
 * won't be killed during playback.
 */
public class MediaNotificationManager {

    private static final String TAG = MediaNotificationManager.class.getName();
    private static final int NOTIFICATION_ID = 1;
    private static final int REQUEST_CODE = 101;
    private final MusicService mService;

    private NotificationManager mNotificationManager;

    private PendingIntent mTogglePlaybackPendingIntent;
    private PendingIntent mPreviousPendingIntent;
    private PendingIntent mNextPendingIntent;
    private PendingIntent mDeletePendingIntent;

    private int mNotificationColor;
    private boolean mStarted = false;

    private PlaybackStateCompat mPlaybackState;

    public MediaNotificationManager(MusicService service) {
        mService = service;

        mNotificationColor = Color.DKGRAY;
        mNotificationManager = (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent togglePlaybackIntent = new Intent(mService, MusicService.class);
        togglePlaybackIntent.setAction(MusicService.CMD_TOGGLE_PLAYBACK);
        mTogglePlaybackPendingIntent = PendingIntent.getService(mService,
                                                                REQUEST_CODE,
                                                                togglePlaybackIntent,
                                                                PendingIntent.FLAG_CANCEL_CURRENT);


        Intent previousIntent = new Intent(mService, MusicService.class);
        previousIntent.setAction(MusicService.CMD_PREV);
        mPreviousPendingIntent = PendingIntent.getService(mService,
                                                          REQUEST_CODE,
                                                          previousIntent,
                                                          PendingIntent.FLAG_CANCEL_CURRENT);


        Intent nextIntent = new Intent(mService, MusicService.class);
        nextIntent.setAction(MusicService.CMD_NEXT);
        mNextPendingIntent = PendingIntent.getService(mService,
                                                      REQUEST_CODE,
                                                      nextIntent,
                                                      PendingIntent.FLAG_CANCEL_CURRENT);


        Intent deleteIntent = new Intent(mService, MusicService.class);
        nextIntent.setAction(MusicService.CMD_STOP);
        mDeletePendingIntent = PendingIntent.getBroadcast(mService,
                                                          REQUEST_CODE,
                                                          deleteIntent,
                                                          PendingIntent.FLAG_CANCEL_CURRENT);

        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        mNotificationManager.cancelAll();


    }

    /**
     * Posts the notification and starts tracking the session to keep it
     * updated. The notification will automatically be removed if the session is
     * destroyed before {@link #stopNotification} is called.
     */
    public void startNotification(PlaybackStateCompat playbackState) {
        Log.d(TAG, "startNotification, state = " + playbackState.getState());
        if (!mStarted) {
            mPlaybackState = playbackState;
            Notification notification = createNotification();
            if (notification != null) {
                mService.startForeground(NOTIFICATION_ID, notification);
                mStarted = true;
            }
        }
    }

    public void onPlaybackStateChanged(PlaybackStateCompat state) {
        mPlaybackState = state;
        Log.d(TAG, "Received new playback state"+ state);
        if (state != null &&
            (state.getState() == PlaybackState.STATE_STOPPED ||
             state.getState() == PlaybackState.STATE_NONE)) {
            stopNotification();
        } else {
            Notification notification = createNotification();
            if (notification != null) {
                mNotificationManager.notify(NOTIFICATION_ID, notification);
            }
        }
    }

    /**
     * Removes the notification and stops tracking the session. If the session
     * was destroyed this has no effect.
     */
    public void stopNotification() {
        Log.d(TAG, "stopNotification");
        if(mStarted) {
            try {
                mNotificationManager.cancel(NOTIFICATION_ID);
            } catch (IllegalArgumentException ex) {
                // ignore if the receiver is not registered.
            }
            mService.stopForeground(true);
            mStarted = false;
        }
    }


    private PendingIntent createContentIntent() {
        Intent openUI = new Intent(mService, FullScreenPlayerActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(mService,
                                         REQUEST_CODE,
                                         openUI,
                                         PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private Notification createNotification() {

        TrackBean playingTrack = mService.getPlaying();

        long action = mPlaybackState.getActions();
        boolean canPrev = (action & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0;
        boolean canNext = (action & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0;
        boolean isPlaying = mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING;

        Bitmap art = BitmapFactory.decodeResource(mService.getResources(),
                                                  R.drawable.ic_default_art);

        final String artUrl = "http://files.tongrenlu.info/m" +
                              playingTrack.getArticleId() +
                              "/cover_400.jpg";
        RequestCreator picasso = Picasso.with(mService.getApplicationContext())
                                        .load(artUrl)
                                        .placeholder(R.drawable.ic_default_art)
                                        .resizeDimen(R.dimen.notification_large_icon_size,
                                                     R.dimen.notification_large_icon_size)
                                        .centerCrop();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Notification.Builder builder = new Notification.Builder(mService);
            int playPauseButtonPosition = 0;

            if (canPrev) {
                builder.addAction(R.drawable.ic_skip_previous_white_24dp,
                                  mService.getString(R.string.label_previous),
                                  mPreviousPendingIntent);
                playPauseButtonPosition = 1;
            }

            if (isPlaying) {
                builder.addAction(R.drawable.ic_pause_white_24dp,
                                  mService.getString(R.string.label_pause),
                                  mTogglePlaybackPendingIntent);
            } else {
                builder.addAction(R.drawable.ic_play_arrow_white_24dp,
                                  mService.getString(R.string.label_play),
                                  mTogglePlaybackPendingIntent);
            }

            if (canNext) {
                builder.addAction(R.drawable.ic_skip_next_white_24dp,
                                  mService.getString(R.string.label_next),
                                  mNextPendingIntent);
            }

            builder.setDeleteIntent(mDeletePendingIntent);


            Notification.MediaStyle style = new Notification.MediaStyle();
            style.setShowActionsInCompactView(playPauseButtonPosition);

            builder.setStyle(style)
                   .setColor(mNotificationColor)
                   .setSmallIcon(R.drawable.ic_notification)
                   .setVisibility(Notification.VISIBILITY_PUBLIC)
                   .setContentIntent(createContentIntent())
                   .setContentTitle(playingTrack.getName())
                   .setContentText(playingTrack.getAlbum())
                   .setLargeIcon(art)
                   .setOngoing(isPlaying)
                   .setAutoCancel(false);

            if (mPlaybackState == null || !mStarted) {
                Log.d(TAG, "updateNotificationPlaybackState. cancelling notification!");
                mService.stopForeground(true);
            } else {
                if (isPlaying && mPlaybackState.getPosition() >= 0) {
                    builder.setWhen(System.currentTimeMillis() - mPlaybackState.getPosition());
                    builder.setShowWhen(true);
                    builder.setUsesChronometer(true);
                } else {
                    builder.setWhen(0);
                    builder.setShowWhen(false);
                    builder.setUsesChronometer(false);
                }
            }

            picasso.into(new Target() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
                    Log.d(TAG, "fetchBitmapFromURLAsync: set bitmap to " + artUrl);
                    builder.setLargeIcon(bitmap);
                    mNotificationManager.notify(NOTIFICATION_ID, builder.build());
                }

                @Override
                public void onBitmapFailed(final Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(final Drawable placeHolderDrawable) {

                }
            });
            return builder.build();
        } else {
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(mService);
            if (canPrev) {
                builder.addAction(R.drawable.ic_skip_previous_white_24dp,
                                  mService.getString(R.string.label_previous),
                                  mPreviousPendingIntent);
            }

            if (isPlaying) {
                builder.addAction(R.drawable.ic_pause_white_24dp,
                                  mService.getString(R.string.label_pause),
                                  mTogglePlaybackPendingIntent);
            } else {
                builder.addAction(R.drawable.ic_play_arrow_white_24dp,
                                  mService.getString(R.string.label_play),
                                  mTogglePlaybackPendingIntent);
            }

            if (canNext) {
                builder.addAction(R.drawable.ic_skip_next_white_24dp,
                                  mService.getString(R.string.label_next),
                                  mNextPendingIntent);
            }
            builder.setDeleteIntent(mDeletePendingIntent);

            builder.setColor(mNotificationColor)
                   .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                   .setSmallIcon(R.drawable.ic_notification)
                   .setContentIntent(createContentIntent())
                   .setContentTitle(playingTrack.getName())
                   .setContentText(playingTrack.getAlbum())
                   .setLargeIcon(art)
                   .setOngoing(isPlaying)
                   .setAutoCancel(false);

            if (mPlaybackState == null || !mStarted) {
                mService.stopForeground(true);
            } else {
                if (isPlaying && mPlaybackState.getPosition() >= 0) {
                    builder.setWhen(System.currentTimeMillis() - mPlaybackState.getPosition());
                    builder.setShowWhen(true);
                    builder.setUsesChronometer(true);
                } else {
                    builder.setWhen(0);
                    builder.setShowWhen(false);
                    builder.setUsesChronometer(false);
                }

            }

            picasso.into(new Target() {
                @Override
                public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
                    Log.d(TAG, "fetchBitmapFromURLAsync: set bitmap to " + artUrl);
                    builder.setLargeIcon(bitmap);
                    mNotificationManager.notify(NOTIFICATION_ID, builder.build());
                }

                @Override
                public void onBitmapFailed(final Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(final Drawable placeHolderDrawable) {

                }
            });
            return builder.build();
        }
    }

}
