package com.ubimobitech.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ubimobitech.spotifystreamer.model.TrackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TopTracksActivity extends AppCompatActivity {
    public static final String ARTIST_ID_INTENT_EXTRA =
            "com.ubimobitech.spotifystreamer.ARTIST_NAME_INTENT_EXTRA";

    private SpotifyApi mSpotifyApi;
    private SpotifyService mSpotify;
    private ProgressBar mProgressBar;
    private ArrayList<TrackInfo> mTrackInfo;

    private static final String STATE_TRACK_INFO = "state_track_info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mSpotifyApi = new SpotifyApi();
        mSpotify = mSpotifyApi.getService();

        if (savedInstanceState != null) {
            mTrackInfo = savedInstanceState.getParcelableArrayList(STATE_TRACK_INFO);
            getSupportActionBar().setSubtitle(mTrackInfo.get(0).getArtistName());
            startFragment();
        } else {
            getArtistTopTrack(getIntent().getStringExtra(ARTIST_ID_INTENT_EXTRA));
        }
    }

    /**
     * Save all appropriate fragment state.
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(STATE_TRACK_INFO, mTrackInfo);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_tracks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    public ArrayList<TrackInfo> getTopTracks() {
        return mTrackInfo;
    }

    private void getArtistTopTrack(String artistId) {
        mProgressBar.setVisibility(View.VISIBLE);

        Map countryCode = new HashMap<String, String>();
        Locale current = getResources().getConfiguration().locale;

        countryCode.put("country", current.getCountry());

        mSpotify.getArtistTopTrack(artistId, countryCode, new Callback<Tracks>() {

            /**
             * Successful HTTP response.
             *
             * @param tracks
             * @param response
             */
            @Override
            public void success(Tracks tracks, Response response) {
                mTrackInfo = new ArrayList<TrackInfo>();

                if (tracks.tracks != null && tracks.tracks.size() > 0) {
                    for (Track track : tracks.tracks) {
                        TrackInfo info = new TrackInfo();

                        if (track.artists.size() > 0)
                            info.setArtistName(track.artists.get(0).name);
                        else
                            info.setArtistName("");

                        List<Image> imgs = track.album.images;

                        if (imgs.size() > 0)
                            info.setImgUrl(imgs.get(0).url);
                        else
                            info.setImgUrl("");

                        info.setAlbumName(track.album.name);
                        info.setmTrackName(track.name);

                        mTrackInfo.add(info);
                    }
                }

                TopTracksActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);

                        getSupportActionBar().setSubtitle(mTrackInfo.get(0).getArtistName());

                        startFragment();
                    }
                });
            }

            /**
             * Unsuccessful HTTP response due to network failure, non-2XX status code, or unexpected
             * exception.
             *
             * @param error
             */
            @Override
            public void failure(RetrofitError error) {
                final String msg = error.getMessage();

                TopTracksActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);

                        Toast.makeText(getApplicationContext(), msg,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void startFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (mTrackInfo != null && mTrackInfo.size() > 1) {
            TopTracksFragment fragment = TopTracksFragment.newInstance();
            ft.replace(R.id.top_track_container, fragment);
            ft.commit();
        } else if (mTrackInfo != null && mTrackInfo.size() == 1) {
            TrackInfo trackInfo = mTrackInfo.get(0);

            SingleTopTrackFragment f = SingleTopTrackFragment.newInstance(
                    trackInfo.getmTrackName(), trackInfo.getImgUrl(),
                    trackInfo.getAlbumName());
            ft.replace(R.id.top_track_container, f);
            ft.commit();
        } else {
            Toast.makeText(getApplicationContext(), R.string.no_artist_found,
                    Toast.LENGTH_SHORT).show();
        }
    }
}
