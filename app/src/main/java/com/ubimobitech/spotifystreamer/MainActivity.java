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

    private boolean mTwoPane;

  //  @InjectView(R.id.progress_bar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        if (findViewById(R.id.top_track_container) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.top_track_container, new TopTracksFragment())
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    public void onArtistClick(String id, String name) {
        if (mTwoPane) {
            getSupportActionBar().setSubtitle(name);
            TopTracksFragment fragment = TopTracksFragment.newInstance(id);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top_track_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, TopTracksActivity.class);
            intent.putExtra(TopTracksActivity.ARTIST_ID_INTENT_EXTRA, id);
            intent.putExtra(TopTracksActivity.ARTIST_NAME_INTENT_EXTRA, name);
            startActivity(intent);
        }
    }
}
