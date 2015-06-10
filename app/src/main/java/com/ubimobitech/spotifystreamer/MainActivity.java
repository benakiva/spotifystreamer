package com.ubimobitech.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ubimobitech.spotifystreamer.interfaces.OnArtistClickListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity implements OnArtistClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private SpotifyApi mSpotifyApi;
    private SpotifyService mSpotify;
    private String mQuery = "";
    private static ArtistsPager mArtistsPager;
    @InjectView(R.id.progress_bar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

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

                searchArtist(urlEncode(mQuery));

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

    private void searchArtist(String artist) {
        mProgressBar.setVisibility(View.VISIBLE);

        mSpotify.searchArtists(artist, new Callback<ArtistsPager>() {
            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                mArtistsPager = artistsPager;

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);

                        if (mArtistsPager != null && mArtistsPager.artists.total > 1) {
                            MainActivityFragment artistList = MainActivityFragment.newInstance(mQuery);
                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            ft.replace(R.id.container, artistList);
                            ft.commit();
                        } else if (mArtistsPager != null && mArtistsPager.artists.total == 1) {
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
                });
            }

            @Override
            public void failure(RetrofitError error) {
                final String msg = error.getMessage();

               MainActivity.this.runOnUiThread(new Runnable() {
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
}
