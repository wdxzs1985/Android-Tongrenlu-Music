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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Keeps track of a notification and updates it automatically for a given
 * MediaSession. Maintaining a visible notification (usually) guarantees that the music service
 * won't be killed during playback.
 */
public class MediaNotificationManager extends BroadcastReceiver {

    private static final String TAG = MediaNotificationManager.class.getName();
    private static final int NOTIFICATION_ID = 1;
    private static final int REQUEST_CODE = 100;
    private final MusicService mService;

    private NotificationManager mNotificationManager;

    private PendingIntent mTogglePlaybackIntent;
    private PendingIntent mPreviousIntent;
    private PendingIntent mNextIntent;

    private int mNotificationColor;

    private PlaybackStateCompat mPlaybackState;

    public MediaNotificationManager(MusicService service) {
        mService = service;

        Context context = service.getApplicationContext();

        mNotificationColor = Color.DKGRAY;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent togglePlaybackIntent = new Intent(context, MusicService.class);
        togglePlaybackIntent.setAction(MusicService.ACTION_CMD);
        togglePlaybackIntent.putExtra(MusicService.CMD_NAME, MusicService.CMD_TOGGLE_PLAYBACK);
        mTogglePlaybackIntent = PendingIntent.getService(context,
                                                         0,
                                                         togglePlaybackIntent,
                                                         PendingIntent.FLAG_CANCEL_CURRENT);

        Intent previousIntent = new Intent(context, MusicService.class);
        previousIntent.setAction(MusicService.ACTION_CMD);
        previousIntent.putExtra(MusicService.CMD_NAME, MusicService.CMD_PREV);
        mPreviousIntent = PendingIntent.getService(mService,
                                                   0,
                                                   previousIntent,
                                                   PendingIntent.FLAG_CANCEL_CURRENT);

        Intent nextIntent = new Intent(context, MusicService.class);
        nextIntent.setAction(MusicService.ACTION_CMD);
        nextIntent.putExtra(MusicService.CMD_NAME, MusicService.CMD_NEXT);
        mNextIntent = PendingIntent.getService(mService,
                                               0,
                                               nextIntent,
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
        mPlaybackState = playbackState;

        // The notification must be updated after setting started to true
        Notification notification = createNotification();
        if (notification != null) {
            mService.startForeground(NOTIFICATION_ID, notification);
        }
    }

    /**
     * Removes the notification and stops tracking the session. If the session
     * was destroyed this has no effect.
     */
    public void stopNotification() {
        try {
            mNotificationManager.cancel(NOTIFICATION_ID);
            mService.unregisterReceiver(this);
        } catch (IllegalArgumentException ex) {
            // ignore if the receiver is not registered.
        }
        mService.stopForeground(true);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "Received intent with action " + action);
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
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mService);

        if ((mPlaybackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
            notificationBuilder.addAction(R.drawable.ic_skip_previous_white_24dp,
                                          mService.getString(R.string.label_previous),
                                          mPreviousIntent);
        }

        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            notificationBuilder.addAction(R.drawable.ic_pause_white_24dp,
                                          mService.getString(R.string.label_pause),
                                          mTogglePlaybackIntent);
        } else {
            notificationBuilder.addAction(R.drawable.ic_play_arrow_white_24dp,
                                          mService.getString(R.string.label_play),
                                          mTogglePlaybackIntent);
        }

        if ((mPlaybackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) {
            notificationBuilder.addAction(R.drawable.ic_skip_next_white_24dp,
                                          mService.getString(R.string.label_next),
                                          mNextIntent);
        }


        String fetchArtUrl = "http://files.tongrenlu.info/m1002/cover_400.jpg";
        Bitmap art = BitmapFactory.decodeResource(mService.getResources(),
                                                  R.drawable.ic_default_art);


        notificationBuilder.setColor(mNotificationColor)
                           .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);


        notificationBuilder.setUsesChronometer(true);


        notificationBuilder.setSmallIcon(R.drawable.ic_notification)
                           .setContentIntent(createContentIntent())
                           .setContentTitle("title")
                           .setContentText("text")
                           .setLargeIcon(art);

        setNotificationPlaybackState(notificationBuilder);
        fetchBitmapFromURLAsync(fetchArtUrl, notificationBuilder);

        return notificationBuilder.build();

    }

    private void setNotificationPlaybackState(NotificationCompat.Builder builder) {
        Log.d(TAG, "updateNotificationPlaybackState. mPlaybackState=" + mPlaybackState);
        if (mPlaybackState == null) {
            Log.d(TAG, "updateNotificationPlaybackState. cancelling notification!");
            mService.stopForeground(true);
            return;
        }
        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING &&
            mPlaybackState.getPosition() >= 0) {
            Log.d(TAG, "updateNotificationPlaybackState. updating playback position to " +
                       (System.currentTimeMillis() - mPlaybackState.getPosition()) / 1000 +
                       " seconds");
            builder.setWhen(System.currentTimeMillis() - mPlaybackState.getPosition());

            builder.setShowWhen(true);
            builder.setUsesChronometer(true);

        } else {
            Log.d(TAG, "updateNotificationPlaybackState. hiding playback position");
            builder.setWhen(0);
            builder.setShowWhen(false);
            builder.setUsesChronometer(false);

        }

        // Make sure that the notification can be dismissed by the user when we are not playing:
        builder.setOngoing(mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING);
    }

    private void fetchBitmapFromURLAsync(final String bitmapUrl,
                                         final NotificationCompat.Builder builder) {
        Picasso.with(mService.getApplicationContext())
               .load(bitmapUrl)
               .placeholder(R.drawable.ic_default_art)
               .resizeDimen(R.dimen.notification_large_icon_size,
                            R.dimen.notification_large_icon_size)
               .centerCrop()
               .into(new Target() {
                   @Override
                   public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
                       Log.d(TAG, "fetchBitmapFromURLAsync: set bitmap to " + bitmapUrl);


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
    }
}
