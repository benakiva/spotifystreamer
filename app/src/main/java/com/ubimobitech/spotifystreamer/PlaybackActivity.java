package com.ubimobitech.spotifystreamer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.ubimobitech.spotifystreamer.model.IMusicServiceAidlInterface;
import com.ubimobitech.spotifystreamer.model.TrackInfo;
import com.ubimobitech.spotifystreamer.service.MusicService;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PlaybackActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TRACK_INFO_INTENT_EXTRA =
            "com.ubimobitech.spotifystreamer.TRACK_INFO_INTENT_EXTRA";
    public static final String TRACK_POSITION_INTENT_EXTRA =
            "com.ubimobitech.spotifystreamer.TRACK_POSITION_INTENT_EXTRA";

    private static final String TAG = PlaybackActivity.class.getSimpleName();

    @InjectView(R.id.artist) TextView mArtistName;
    @InjectView(R.id.album_title) TextView mAlbumTitle;
    @InjectView(R.id.album_icon) ImageView mAlbumIcon;
    @InjectView(R.id.song_title) TextView mSongName;
    @InjectView(R.id.music_progress_bar) SeekBar mProgressBar;
    @InjectView(R.id.time_progress) TextView mTimeProgress;
    @InjectView(R.id.song_duration) TextView mDuration;
    @InjectView(R.id.previous) ImageButton mPrevious;
    @InjectView(R.id.play_pause) ImageButton mPlayPause;
    @InjectView(R.id.next) ImageButton mNext;

    private ArrayList<TrackInfo> mTrackInfo = null;
    private IMusicServiceAidlInterface mService;
    private Handler updateHandler = new Handler();
    private int mCurrentPosition;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IMusicServiceAidlInterface.Stub.asInterface(service);

            try {
                mService.setQueue(mTrackInfo);
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage());
            }

            updatePlayInfo();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_playback_screen);
        ButterKnife.inject(this);

        Intent intent = getIntent();

        if (intent != null) {
            mTrackInfo = intent.getParcelableArrayListExtra(TRACK_INFO_INTENT_EXTRA);
            mCurrentPosition = intent.getIntExtra(TRACK_POSITION_INTENT_EXTRA, 0);
        }

        updatePlaybackScreen(mCurrentPosition);

        mPlayPause.setOnClickListener(this);
        mNext.setOnClickListener(this);
        mPrevious.setOnClickListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mConnection != null)
            unbindService(mConnection);
    }

    /**
     * Dispatch onStart() to all fragments.  Ensure any created loaders are
     * now started.
     */
    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, MusicService.class);

        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_playback, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_pause:
                try {
                    if (mService.getState() != LocalPlayback.STATE_PLAYING) {
                        mService.play(mTrackInfo.get(mCurrentPosition), mCurrentPosition);
                        updateHandler.postDelayed(new ProgressUpdater(), 100);
                        mPlayPause.setImageResource(R.drawable.ic_pause_circle_filled_black_48dp);
                    } else {
                        mService.pause();
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, e.getMessage());
                }

                break;
            case R.id.next:
                try {
                    if ((mCurrentPosition + 1) < mTrackInfo.size()) {
                        mCurrentPosition += 1;
                        mService.next();
                        updateHandler.postDelayed(new ProgressUpdater(), 100);
                        mPlayPause.setImageResource(R.drawable.ic_pause_circle_filled_black_48dp);
                        updatePlaybackScreen(mCurrentPosition);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, e.getMessage());
                }

                break;
            case R.id.previous:
                try {
                    if ((mCurrentPosition - 1) >= 0) {
                        mCurrentPosition -= 1;
                        mService.prev();
                        updateHandler.postDelayed(new ProgressUpdater(), 100);
                        mPlayPause.setImageResource(R.drawable.ic_pause_circle_filled_black_48dp);
                        updatePlaybackScreen(mCurrentPosition);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, e.getMessage());
                }

                break;
        }
    }

    private void updatePlayInfo() {
        if (mService != null) {
            updateHandler.postDelayed(new ProgressUpdater(), 100);
        }
    }

    private class ProgressUpdater implements Runnable {
        @Override
        public void run() {
            try {
                if (mService.getState() == LocalPlayback.STATE_PLAYING) {
                    mProgressBar.setMax((int)mService.duration()); // Very bad
                    mProgressBar.setProgress(mService.getCurrentposition());
                    mDuration.setText(getFormattedDuration(mService.duration()));
                    mTimeProgress.setText(getFormattedDuration(mService.getCurrentposition()));
                    mPlayPause.setImageResource(R.drawable.ic_pause_circle_filled_black_48dp);
                } else {
                  //  mProgressBar.setMax(0);
                   // mProgressBar.setProgress(0);
                    mPlayPause.setImageResource(R.drawable.ic_play_circle_filled_black_48dp);
                }
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage());
            }

            updateHandler.postDelayed(this, 100);
        }
    }

    private String getFormattedDuration(long millis) {
        long seconds = (millis / 1000) + 1;
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }

    private void updatePlaybackScreen(int position) {
        if (mTrackInfo != null) {
            TrackInfo track = mTrackInfo.get(position);

            mArtistName.setText(track.getArtistName());
            Picasso.with(this).load(track.getImgUrl()).into(mAlbumIcon);
            mAlbumTitle.setText(track.getAlbumName());
            mSongName.setText(track.getTrackName());
            mDuration.setText(track.getFormattedDuration());
            mTimeProgress.setText("");
            mProgressBar.setMax(0);
            mProgressBar.setProgress(0);
        }
    }
}
