package com.ubimobitech.spotifystreamer;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.ubimobitech.spotifystreamer.adapters.TopTracksAdapter;
import com.ubimobitech.spotifystreamer.model.TrackInfo;
import com.ubimobitech.spotifystreamer.sync.SpotifyDataFetcher;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A fragment representing a list of tracks.
 * <p/>
 * <p/>
 *
 */
public class TopTracksFragment extends ListFragment implements Callback<Tracks> {
    private TopTracksAdapter mTracksAdapter;
    private SpotifyDataFetcher mDataFetcher;
    private ArrayList<TrackInfo> mTrackInfo;

    public interface OnTrackCliclListener {
        void onTrackClick(ArrayList<TrackInfo> track, int position);
    }

    private OnTrackCliclListener mListener;

    public static TopTracksFragment newInstance(final String artistId) {
        TopTracksFragment fragment = new TopTracksFragment();
        Bundle args = new Bundle();
        args.putString(TopTracksActivity.ARTIST_ID_INTENT_EXTRA, artistId);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TopTracksFragment() {
    }

    /**
     * Called when a fragment is first attached to its activity.
     * {@link #onCreate(Bundle)} will be called after this.
     *
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (OnTrackCliclListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnTrackCliclListener");
        }
    }

    /**
     * Called when the fragment is no longer attached to its activity.  This
     * is called after {@link #onDestroy()}.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /**
     * Provide default implementation to return a simple list view.  Subclasses
     * can override to replace with their own layout.  If doing so, the
     * returned view hierarchy <em>must</em> have a ListView whose id
     * is {@link android.R.id#list android.R.id.list} and can optionally
     * have a sibling view id {@link android.R.id#empty android.R.id.empty}
     * that is to be shown when the list is empty.
     * <p/>
     * <p>If you are overriding this method with your own custom content,
     * consider including the standard layout {@link android.R.layout#list_content}
     * in your layout file, so that you continue to retain all of the standard
     * behavior of ListFragment.  In particular, this is currently the only
     * way to have the built-in indeterminant progress state be shown.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_top_track, container, false);

        mDataFetcher = SpotifyDataFetcher.getInstance(getActivity());
        Bundle args = getArguments();

        if (args != null) {
            mDataFetcher.getTopTracks(args.getString(TopTracksActivity.ARTIST_ID_INTENT_EXTRA), this);
        }

        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mListener != null) {
            mListener.onTrackClick(mTrackInfo, position);
        }
    }

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
                info.setTrackName(track.name);
                info.setDuration(track.duration_ms);
                info.setPreviewUrl(track.preview_url);

                mTrackInfo.add(info);
            }

            mTracksAdapter = new TopTracksAdapter(getActivity(), mTrackInfo);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setListAdapter(mTracksAdapter);
                }
            });
        }
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

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), msg,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
