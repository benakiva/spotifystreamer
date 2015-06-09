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
    private static Tracks mTracks;
    private ProgressBar mProgressBar;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mSpotifyApi = new SpotifyApi();
        mSpotify = mSpotifyApi.getService();

        getArtistTopTrack(getIntent().getStringExtra(ARTIST_ID_INTENT_EXTRA));
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

    public Tracks getTopTracks() {
        return mTracks;
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
                mTracks = tracks;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mProgressBar.setVisibility(View.GONE);

                                String artistName = "";

                                if (mTracks != null && mTracks.tracks.size() > 0) {
                                    if (mTracks.tracks.get(0).artists.size() > 0)
                                        artistName = mTracks.tracks.get(0).artists.get(0).name;
                                }

                                getSupportActionBar().setSubtitle(artistName);

                                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                                if (mTracks != null && mTracks.tracks.size() > 1) {
                                    TopTracksFragment fragment = TopTracksFragment.newInstance();
                                    ft.replace(R.id.top_track_container, fragment);
                                    ft.commit();
                                } else if (mTracks != null && mTracks.tracks.size() == 1) {
                                    Track track = mTracks.tracks.get(0);

                                    List<Image> imgs = track.album.images;
                                    String imgUrl = "";

                                    if (imgs.size() > 0)
                                        imgUrl = imgs.get(0).url;

                                    SingleTopTrackFragment f = SingleTopTrackFragment.newInstance(track.name, imgUrl,
                                            track.album.name);
                                    ft.replace(R.id.top_track_container, f);
                                    ft.commit();
                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.no_artist_found,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).start();
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

                new Thread(new Runnable() {
                    public void run() {
                        mHandler.post(new Runnable() {
                            public void run() {
                                mProgressBar.setVisibility(View.GONE);

                                Toast.makeText(getApplicationContext(), msg,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
            }
        });
    }
}
