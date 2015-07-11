package com.ubimobitech.spotifystreamer;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.ubimobitech.spotifystreamer.model.TrackInfo;

import java.util.ArrayList;

public class TopTracksActivity extends AppCompatActivity implements TopTracksFragment.OnTrackClickListener {
    public static final String ARTIST_ID_INTENT_EXTRA =
            "com.ubimobitech.spotifystreamer.ARTIST_ID_INTENT_EXTRA";

    public static final String ARTIST_NAME_INTENT_EXTRA =
            "com.ubimobitech.spotifystreamer.ARTIST_NAME_INTENT_EXTRA";

    private static final String STATE_TRACK_INFO = "state_track_info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            String artistId = "";
            String name = "";

            if (intent != null) {
                artistId = intent.getStringExtra(ARTIST_ID_INTENT_EXTRA);
                name = intent.getStringExtra(ARTIST_NAME_INTENT_EXTRA);
            }

            getSupportActionBar().setSubtitle(name);

            TopTracksFragment fragment = TopTracksFragment.newInstance(artistId);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.top_track_container, fragment)
                    .commit();
        }
    }

    /**
     * Save all appropriate fragment state.
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
       // outState.putParcelableArrayList(STATE_TRACK_INFO, mTrackInfo);

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
                Intent intent = new Intent(this, UserPreferencesActivity.class);
                startActivity(intent);
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTrackClick(ArrayList<TrackInfo> track, int position) {
        Intent intent = new Intent(this, PlaybackActivity.class);
        intent.putParcelableArrayListExtra(PlaybackActivity.TRACK_INFO_INTENT_EXTRA, track);
        intent.putExtra(PlaybackActivity.TRACK_POSITION_INTENT_EXTRA, position);
        startActivity(intent);
    }
}
