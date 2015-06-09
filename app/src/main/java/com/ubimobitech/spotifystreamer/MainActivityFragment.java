package com.ubimobitech.spotifystreamer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ubimobitech.spotifystreamer.adapters.ArtistsAdapter;
import com.ubimobitech.spotifystreamer.interfaces.OnArtistClickListener;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private static final String TAG = MainActivityFragment.class.getSimpleName();
    public static final String QUERY_ARGS = "com.ubimobitech.spotifystreamer.QUERY_ARGS";
    private SpotifyApi mSpotifyApi;
    private SpotifyService mSpotify;
    private ListView mListView;
    private ArtistsAdapter mArtistAdapter;
    private String mQuery = "";

    private static final String LIST_STATE_KEY = "listState";
    private static final String LIST_POSITION_KEY = "listPosition";
    private static final String ITEM_POSITION_KEY = "itemPosition";
    private static final String QUERY = "query";

    private Parcelable mListState = null;
    private int mListPosition = 0;
    private int mItemPosition = 0;

    private OnArtistClickListener mListener;

    public static MainActivityFragment newInstance(String query) {
        MainActivityFragment f = new MainActivityFragment();
        Bundle args = new Bundle();
        args.putString(QUERY_ARGS, query);
        f.setArguments(args);

        return f;
    }

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        Bundle args = getArguments();

        if (args != null) {
            mQuery = args.getString(QUERY_ARGS);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnArtistClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnArtistClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mListView = (ListView) view.findViewById(R.id.search_listview);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = (Artist)mArtistAdapter.getItem(position);

                mListener.onArtistClick(artist.id);
            }
        });

        return view;
    }

    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(LIST_STATE_KEY);
            mListPosition = savedInstanceState.getInt(LIST_POSITION_KEY);
            mItemPosition = savedInstanceState.getInt(ITEM_POSITION_KEY);
            mQuery = savedInstanceState.getString(QUERY);

        }

        mArtistAdapter = new ArtistsAdapter(getActivity(),
                ((MainActivity)getActivity()).getArtistsPager().artists);
        mListView.setAdapter(mArtistAdapter);

        if (mListState != null)
            mListView.onRestoreInstanceState(mListState);

        mListView.setSelectionFromTop(mListPosition, mItemPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mListState = mListView.onSaveInstanceState();
        outState.putParcelable(LIST_STATE_KEY, mListState);

        // Save position of first visible item
        mListPosition = mListView.getFirstVisiblePosition();
        outState.putInt(LIST_POSITION_KEY, mListPosition);

        // Save scroll position of item
        View itemView = mListView.getChildAt(0);
        mItemPosition = itemView == null ? 0 : itemView.getTop();
        outState.putInt(ITEM_POSITION_KEY, mItemPosition);

        outState.putString(QUERY, mQuery);
    }
}
