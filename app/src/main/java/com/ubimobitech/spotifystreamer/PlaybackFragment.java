/*
 * Copyright 2015, Isaac Ben-Akiva
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
/**
 * FILE: PlaybackFragment.java
 * AUTHOR: Dr. Isaac Ben-Akiva <isaac.ben-akiva@ubimobitech.com>
 * <p/>
 * CREATED ON: 10/07/15
 */

package com.ubimobitech.spotifystreamer;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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

/**
 * Created by benakiva on 10/07/15.
 */
public class PlaybackFragment extends DialogFragment implements View.OnClickListener {
    private static final String TAG = PlaybackFragment.class.getSimpleName();
    public static final String TRACKS_ARGS = "tracks_args";
    public static final String POSITION_ARGS = "position_args";

    @InjectView(R.id.artist)
    TextView mArtistName;
    @InjectView(R.id.album_title) TextView mAlbumTitle;
    @InjectView(R.id.album_icon)
    ImageView mAlbumIcon;
    @InjectView(R.id.song_title) TextView mSongName;
    @InjectView(R.id.music_progress_bar)
    SeekBar mProgressBar;
    @InjectView(R.id.time_progress) TextView mTimeProgress;
    @InjectView(R.id.song_duration) TextView mDuration;
    @InjectView(R.id.previous) ImageButton mPrevious;
    @InjectView(R.id.play_pause) ImageButton mPlayPause;
    @InjectView(R.id.next) ImageButton mNext;

    private ArrayList<TrackInfo> mTrackInfo;
    private int mCurrentPosition;
    private IMusicServiceAidlInterface mService;
    private Handler updateHandler = new Handler();

    private ShareActionProvider mShareActionProvider;

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

    public static PlaybackFragment newInstance(ArrayList<TrackInfo> tracks, int position) {
        PlaybackFragment fragment = new PlaybackFragment();


        Bundle args = new Bundle();
        args.putParcelableArrayList(TRACKS_ARGS, tracks);
        args.putInt(POSITION_ARGS, position);

        fragment.setArguments(args);

        return fragment;
    }

    public PlaybackFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.media_playback_screen, container, false);

        ButterKnife.inject(this, view);

        Bundle args = getArguments();

        if (args != null) {
            mTrackInfo = args.getParcelableArrayList(TRACKS_ARGS);
            mCurrentPosition = args.getInt(POSITION_ARGS);

            updatePlaybackScreen(mCurrentPosition);
        }

        mPlayPause.setOnClickListener(this);
        mNext.setOnClickListener(this);
        mPrevious.setOnClickListener(this);

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mConnection != null)
            getActivity().unbindService(mConnection);
    }

    /**
     * Dispatch onStart() to all fragments.  Ensure any created loaders are
     * now started.
     */
    @Override
    public void onStart() {
        super.onStart();

        Intent intent = new Intent(getActivity(), MusicService.class);

        getActivity().startService(intent);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    /**
     * @param menu     The options menu in which you place your items.
     * @param inflater
     * @see #setHasOptionsMenu
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_playback, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        setShareIntent(mTrackInfo.get(mCurrentPosition));
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
            Picasso.with(getActivity()).load(track.getImgUrl()).into(mAlbumIcon);
            mAlbumTitle.setText(track.getAlbumName());
            mSongName.setText(track.getTrackName());
            mDuration.setText(track.getFormattedDuration());
            mTimeProgress.setText("");
            mProgressBar.setMax(0);
            mProgressBar.setProgress(0);

            setShareIntent(track);
        }
    }

    private void setShareIntent(TrackInfo track) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");

        StringBuilder builder = new StringBuilder(getString(R.string.share_music));
        builder.append(track.getArtistName())
                .append(": " + track.getTrackName())
                .append(" " + track.getPreviewUrl());

        shareIntent.putExtra(Intent.EXTRA_TEXT, builder.toString());

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }
}
