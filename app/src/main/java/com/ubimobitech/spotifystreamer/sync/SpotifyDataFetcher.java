/**
 * FILE: SpotifyDataFetcher.java
 * AUTHOR: Dr. Isaac Ben-Akiva <isaac.ben-akiva@ubimobitech.com>
 * <p/>
 * CREATED ON: 25/06/15
 */

package com.ubimobitech.spotifystreamer.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ubimobitech.spotifystreamer.R;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;

/**
 * Created by benakiva on 25/06/15.
 */
public class SpotifyDataFetcher {
    private static SpotifyDataFetcher mInstance;
    private SpotifyApi mSpotifyApi;
    private SpotifyService mSpotify;
    private Context mContext;

    public static SpotifyDataFetcher getInstance(Context context) {
        if (mInstance == null)
            mInstance = new SpotifyDataFetcher(context);

        return mInstance;
    }

    private SpotifyDataFetcher(Context context) {
        mSpotifyApi = new SpotifyApi();
        mSpotify = mSpotifyApi.getService();
        mContext = context;
    }

    public void getArtist(final String name, Callback<ArtistsPager> callback) {
        mSpotify.searchArtists(name, callback);
    }

    public void getTopTracks(final String artistId, Callback<Tracks> callback) {
        Map countryCode = new HashMap<String, String>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        countryCode.put("country", prefs.getString("pref_countryCode",
                mContext.getString(R.string.pref_default_country)));

        mSpotify.getArtistTopTrack(artistId, countryCode, callback);
    }
}
