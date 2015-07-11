package com.ubimobitech.spotifystreamer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ubimobitech.spotifystreamer.adapters.ArtistsAdapter;
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

    private static final String LIST_STATE_KEY = "listState";
    private static final String LIST_POSITION_KEY = "listPosition";
    private static final String ITEM_POSITION_KEY = "itemPosition";
    private static final String LIST_STATE = "list_state";
    private ListView mListView;

    private Parcelable mListState = null;
    private int mListPosition = 0;
    private int mItemPosition = 0;

    public interface OnTrackClickListener {
        void onTrackClick(ArrayList<TrackInfo> track, int position);
    }

    private OnTrackClickListener mListener;

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
            mListener = (OnTrackClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnTrackClickListener");
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mListState = mListView.onSaveInstanceState();
        outState.putParcelable(LIST_STATE_KEY, mListState);

        // Save position of first visible item
        mListPosition = mListView.getFirstVisiblePosition();
        outState.putInt(LIST_POSITION_KEY, mListPosition);

        // Save scroll position of item
        View itemView = mListView.getChildAt(0);
        mItemPosition = itemView == null ? 0 : itemView.getTop();
        outState.putInt(ITEM_POSITION_KEY, mItemPosition);

        outState.putParcelableArrayList(LIST_STATE, mTrackInfo);

        super.onSaveInstanceState(outState);
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

        mListView = (ListView)view.findViewById(android.R.id.list);

        return view;
    }

    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * {@link #setRetainInstance(boolean)} to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after {@link #onCreateView}
     * and before {@link #onViewStateRestored(Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(LIST_STATE_KEY);
            mListPosition = savedInstanceState.getInt(LIST_POSITION_KEY);
            mItemPosition = savedInstanceState.getInt(ITEM_POSITION_KEY);

            mTrackInfo = savedInstanceState.getParcelableArrayList(LIST_STATE);

            mTracksAdapter = new TopTracksAdapter(getActivity(),
                    mTrackInfo);
            mListView.setAdapter(mTracksAdapter);

            if (mListState != null)
                mListView.onRestoreInstanceState(mListState);

            mListView.setSelectionFromTop(mListPosition, mItemPosition);
        } else {
            mDataFetcher = SpotifyDataFetcher.getInstance(getActivity());
            Bundle args = getArguments();

            if (args != null) {
                mDataFetcher.getTopTracks(args.getString(TopTracksActivity.ARTIST_ID_INTENT_EXTRA), this);
            }
        }
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

                    if (mListState != null)
                        mListView.onRestoreInstanceState(mListState);

                    mListView.setSelectionFromTop(mListPosition, mItemPosition);
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
