package com.ubimobitech.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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

public class TopTracksActivity extends AppCompatActivity {
    public static final String ARTIST_ID_INTENT_EXTRA =
            "com.ubimobitech.spotifystreamer.ARTIST_NAME_INTENT_EXTRA";

    private SpotifyApi mSpotifyApi;
    private SpotifyService mSpotify;
    private TopTracksTask mTopTracksTask = null;
    private static Tracks mTracks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSpotifyApi = new SpotifyApi();
        mSpotify = mSpotifyApi.getService();

        fetchTopTracks(getIntent().getStringExtra(ARTIST_ID_INTENT_EXTRA));
    }

    private void fetchTopTracks(String id) {
        if (mTopTracksTask != null) {
            mTopTracksTask.cancel(true);
            mTopTracksTask = null;
        }

        mTopTracksTask = new TopTracksTask();
        mTopTracksTask.execute(id);
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

    private class TopTracksTask extends AsyncTask<String, Void, Tracks> {

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected Tracks doInBackground(String... params) {
            Map countryCode = new HashMap<String, String>();
            Locale current = getResources().getConfiguration().locale;

            countryCode.put("country", current.getCountry());

            return mSpotify.getArtistTopTrack(params[0], countryCode);
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p/>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param tracks The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(Tracks tracks) {
            mTracks = tracks;

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
    }
}
