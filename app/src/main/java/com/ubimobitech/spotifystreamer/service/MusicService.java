package com.ubimobitech.spotifystreamer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.session.PlaybackState;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.ubimobitech.spotifystreamer.model.IMusicServiceAidlInterface;
import com.ubimobitech.spotifystreamer.LocalPlayback;
import com.ubimobitech.spotifystreamer.Playback;
import com.ubimobitech.spotifystreamer.PlaybackActivity;
import com.ubimobitech.spotifystreamer.R;
import com.ubimobitech.spotifystreamer.model.TrackInfo;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service implements Playback.Callback {

    public static final String ACTION_CMD = "com.ubimobitech.spotifystreamer.service.ACTION_CMD";
    public static final String CMD_NAME = "CMD_NAME";
    public static final String CMD_PAUSE = "CMD_PAUSE";

    public static final String ACTION_PAUSE = "com.ubimobitech.spotifystreamer.service.pause";
    public static final String ACTION_PLAY = "com.ubimobitech.spotifystreamer.service.play";
    public static final String ACTION_PREV = "com.ubimobitech.spotifystreamer.service.prev";
    public static final String ACTION_NEXT = "com.ubimobitech.spotifystreamer.service.next";

    private PendingIntent mPauseIntent;
    private PendingIntent mPlayIntent;
    private PendingIntent mPreviousIntent;
    private PendingIntent mNextIntent;

    private ArrayList<TrackInfo> mQueue = new ArrayList<>();
    private static int mCurrentPosition = 0;

    private static final String TAG = MusicService.class.getSimpleName();
    private LocalPlayback mPlayback = null;
    private NotificationManager mNotificationManager;
    private Notification mNotification = null;
    private NotificationCompat.Builder mBuilder;

    private final int NOTIFICATION_ID = 251225;
    private static final int REQUEST_CODE = 89;

    private final IBinder mBinder = new ServiceStub(this);
    private SharedPreferences mPrefs;

    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mPlayback = new LocalPlayback(this);
        mPlayback.setState(LocalPlayback.STATE_NONE);
        mPlayback.setCallback(this);

        String pkg = getPackageName();

        mPauseIntent = PendingIntent.getBroadcast(getApplicationContext(), REQUEST_CODE,
                new Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPlayIntent = PendingIntent.getBroadcast(getApplicationContext(), REQUEST_CODE,
                new Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPreviousIntent = PendingIntent.getBroadcast(getApplicationContext(), REQUEST_CODE,
                new Intent(ACTION_PREV).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mNextIntent = PendingIntent.getBroadcast(getApplicationContext(), REQUEST_CODE,
                new Intent(ACTION_NEXT).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NEXT);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_PLAY);
        filter.addAction(ACTION_PREV);

        registerReceiver(mActionReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return Service.START_NOT_STICKY;
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        if (mPlayback != null)
            mPlayback.stop();

        unregisterReceiver(mActionReceiver);

        super.onDestroy();
    }

    /**
     * Public interface methods implementation
     */

    public boolean isPlaying() {
        return mPlayback.isPlaying();
    }

    public void stop() {
        handleStopRequest();
    }

    public void pause() {
        mPlayback.pause();
    }

    public void play(TrackInfo track, int position) {
        mCurrentPosition = position;
        mPlayback.play(track);

        if (mPrefs.getBoolean(getString(R.string.pref_notification_key), true))
            setupForeground(track);
    }

    public void prev() {
        if ((mCurrentPosition - 1) >= 0) {
            mCurrentPosition -= 1;

            if (mPlayback.getState() == LocalPlayback.STATE_PAUSED) {
                mPlayback.stop();
            }

            mPlayback.play(mQueue.get(mCurrentPosition));

            if (mPrefs.getBoolean(getString(R.string.pref_notification_key), true))
                setupForeground(mQueue.get(mCurrentPosition));
        }
    }

    public void next() {
        if ((mCurrentPosition + 1) < mQueue.size()) {
            mCurrentPosition += 1;

            if (mPlayback.getState() == LocalPlayback.STATE_PAUSED) {
                mPlayback.stop();
            }

            mPlayback.play(mQueue.get(mCurrentPosition));

            if (mPrefs.getBoolean(getString(R.string.pref_notification_key), true))
                setupForeground(mQueue.get(mCurrentPosition));
        }
    }

    public void setQueue(List<TrackInfo> tracks) {
        mQueue.clear();
        mQueue.addAll(tracks);
    }

    public long duration() {
        return mPlayback.duration();
    }

    public int position() {
        return mPlayback.getCurrentPosition();
    }

    public int getState() {
        return mPlayback.getState();
    }

    public void seekTo(int position){
        mPlayback.seekTo(position);
    }

    public void setTrackName(String track) {
        mPlayback.setTrackName(track);
    }

    public String getTrackName() {
        return mPlayback.getTrackName();
    }

    public void setAlbumName(String album) {
        mPlayback.setAlbumName(album);
    }

    public String getAlbumName() {
        return mPlayback.getAlbumName();
    }

    public void setArtistName(String artist) {
        mPlayback.setArtistName(artist);
    }

    public String getArtistName() {
        return mPlayback.getArtistName();
    }

    /**
     * On current music completed.
     */
    @Override
    public void onCompletion() {
        handleStopRequest();
    }

    /**
     * on Playback status changed
     * Implementations can use this callback to update
     * playback state on the media sessions.
     *
     * @param state
     */
    @Override
    public void onPlaybackStatusChanged(int state) {

    }

    /**
     * @param error to be added to the PlaybackState
     */
    @Override
    public void onError(String error) {

    }

    /**
     * @param mediaId being currently played
     */
    @Override
    public void onMetadataChanged(String mediaId) {

    }

    private void handleStopRequest() {
        mPlayback.stop();
        stopSelf();
    }

    private void setupForeground(TrackInfo track) {
        Intent intent = new Intent(getApplicationContext(), PlaybackActivity.class);
        intent.putParcelableArrayListExtra(PlaybackActivity.TRACK_INFO_INTENT_EXTRA, mQueue);
        intent.putExtra(PlaybackActivity.TRACK_POSITION_INTENT_EXTRA, mCurrentPosition);

        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_notification)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentTitle(track.getTrackName())
                .setContentText(track.getArtistName())
                .setSubText(track.getAlbumName())
                .setContentIntent(pi);

        mBuilder.addAction(R.drawable.ic_skip_previous_white_24dp,
                null, mPreviousIntent);

        addPlayPauseAction(mBuilder);

        mBuilder.addAction(R.drawable.ic_skip_next_white_24dp,
                null, mNextIntent);

        mNotification = mBuilder.build();

        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mBuilder.setLargeIcon(bitmap);
                mNotification = mBuilder.build();

                mNotificationManager.notify(NOTIFICATION_ID, mNotification);
                startForeground(NOTIFICATION_ID, mNotification);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        Picasso.with(getApplicationContext()).load(track.getImgUrl()).into(target);

    }

    private void addPlayPauseAction(NotificationCompat.Builder builder) {
        int icon;
        PendingIntent intent;
        int state = mPlayback.getState();

        if ((state == LocalPlayback.STATE_PLAYING) || state == LocalPlayback.STATE_BUFFERING) {
            icon = R.drawable.uamp_ic_pause_white_24dp;
            intent = mPauseIntent;
        } else {
            icon = R.drawable.uamp_ic_play_arrow_white_24dp;
            intent = mPlayIntent;
        }

        builder.addAction(icon, null, intent);
    }

    /**
     * Service Stub
     */
   static class ServiceStub extends IMusicServiceAidlInterface.Stub {
        WeakReference<MusicService> mService;

        ServiceStub(MusicService service) {
            mService = new WeakReference<MusicService>(service);
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return mService.get().isPlaying();
        }

        @Override
        public void stop() throws RemoteException {
            mService.get().stop();
        }

        @Override
        public void pause() throws RemoteException {
            mService.get().pause();
        }

        @Override
        public void play(TrackInfo track, int position) throws RemoteException {
            mService.get().play(track, position);
        }

        @Override
        public void prev() throws RemoteException {
            mService.get().prev();
        }

        @Override
        public void next() throws RemoteException {
            mService.get().next();
        }

        @Override
        public long duration() throws RemoteException {
            return mService.get().duration();
        }

        @Override
        public int getCurrentposition() throws RemoteException {
            return mService.get().position();
        }

        @Override
        public int getState() throws RemoteException {
            return mService.get().getState();
        }

        @Override
        public void seekTo(int position) throws RemoteException {
            mService.get().seekTo(position);
        }

        @Override
        public void setQueue(List<TrackInfo> queue) throws RemoteException {
            mService.get().setQueue(queue);
        }

        @Override
        public void setTrackName(String track) throws RemoteException {
            mService.get().setTrackName(track);
        }

        @Override
        public void setAlbumName(String album) throws RemoteException {
            mService.get().setAlbumName(album);
        }

        @Override
        public void setArtistName(String name) throws RemoteException {
            mService.get().setArtistName(name);
        }

        @Override
        public String getTrackName() throws RemoteException {
            return mService.get().getTrackName();
        }

        @Override
        public String getAlbumName() throws RemoteException {
            return mService.get().getAlbumName();
        }

        @Override
        public String getArtistName() throws RemoteException {
            return mService.get().getArtistName();
        }
    }

    private BroadcastReceiver mActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_PAUSE:
                    pause();
                    break;
                case ACTION_PLAY:
                    play(mQueue.get(mCurrentPosition), mCurrentPosition);
                    break;
                case ACTION_NEXT:
                    next();
                    break;
                case ACTION_PREV:
                    prev();
                    break;
                default:
                    Log.w(TAG, "Unknown intent ignored. Action= " + action);
            }
        }
    };
}
