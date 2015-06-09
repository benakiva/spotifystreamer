package com.ubimobitech.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ubimobitech.spotifystreamer.interfaces.OnArtistClickListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

public class MainActivity extends AppCompatActivity implements OnArtistClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private SpotifyApi mSpotifyApi;
    private SpotifyService mSpotify;
    private ArtistsTask mArtistTask = null;
    private String mQuery = "";
    private static ArtistsPager mArtistsPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSpotifyApi = new SpotifyApi();
        mSpotify = mSpotifyApi.getService();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setIconifiedByDefault(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                MenuItemCompat.collapseActionView(searchItem);
                searchView.setQuery("", false);
                mQuery = query;

                fetchArtist(mQuery);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public ArtistsPager getArtistsPager() {
        return mArtistsPager;
    }

    private void fetchArtist(String artist) {
        if (mArtistTask != null) {
            mArtistTask.cancel(true);
            mArtistTask = null;
        }

        mArtistTask = new ArtistsTask();
        mArtistTask.execute(urlEncode(artist));
    }

    public static String urlEncode(String data) {
        try {
            return URLEncoder.encode(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
        }

        return null;
    }

    @Override
    public void onArtistClick(String id) {
        Intent intent = new Intent(this, TopTracksActivity.class);
        intent.putExtra(TopTracksActivity.ARTIST_ID_INTENT_EXTRA, id);
        startActivity(intent);
    }

    private class ArtistsTask extends AsyncTask<String, Void, ArtistsPager> {

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
        protected ArtistsPager doInBackground(String... params) {
            return mSpotify.searchArtists(params[0]);
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p/>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param artistsPager The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(ArtistsPager artistsPager) {
            mArtistsPager = artistsPager;

            if (artistsPager != null && artistsPager.artists.total > 1) {
                MainActivityFragment artistList = MainActivityFragment.newInstance(mQuery);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.container, artistList);
                ft.commit();
            } else if (artistsPager != null && artistsPager.artists.total == 1) {
                Artist artist = mArtistsPager.artists.items.get(0);
                String imgUrl = "";

                if (artist.images.size() > 0)
                    imgUrl = artist.images.get(0).url;

                SingleArtistFragment fragment = SingleArtistFragment.newInstance(artist.name,
                        imgUrl, artist.id);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.container, fragment);
                ft.commit();
            } else {
                Toast.makeText(getApplicationContext(), R.string.no_artist_found,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


}
