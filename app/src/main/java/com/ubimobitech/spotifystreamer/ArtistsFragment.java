package com.ubimobitech.spotifystreamer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.ubimobitech.spotifystreamer.adapters.ArtistsAdapter;
import com.ubimobitech.spotifystreamer.interfaces.OnArtistClickListener;
import com.ubimobitech.spotifystreamer.model.ArtistInfo;
import com.ubimobitech.spotifystreamer.sync.SpotifyDataFetcher;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistsFragment extends Fragment implements Callback<ArtistsPager> {
    private static final String TAG = ArtistsFragment.class.getSimpleName();
    public static final String QUERY_ARGS = "com.ubimobitech.spotifystreamer.QUERY_ARGS";
    private ListView mListView;
    private ArtistsAdapter mArtistAdapter;
    private String mQuery = "";

    private static final String LIST_STATE_KEY = "listState";
    private static final String LIST_POSITION_KEY = "listPosition";
    private static final String ITEM_POSITION_KEY = "itemPosition";
    private static final String QUERY = "query";
    private static final String LIST_STATE = "list_state";

    private Parcelable mListState = null;
    private int mListPosition = 0;
    private int mItemPosition = 0;

    private OnArtistClickListener mListener;
    private ArrayList<ArtistInfo> mArtistsList = new ArrayList<>();
    private SpotifyDataFetcher mDataFetcher;

    public static ArtistsFragment newInstance(String query) {
        ArtistsFragment f = new ArtistsFragment();
        Bundle args = new Bundle();
        args.putString(QUERY_ARGS, query);
        f.setArguments(args);

        return f;
    }

    public ArtistsFragment() {
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

        mDataFetcher = SpotifyDataFetcher.getInstance(getActivity());
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
                ArtistInfo artist = (ArtistInfo)mArtistAdapter.getItem(position);

                mListener.onArtistClick(artist.getId(), artist.getName());
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
            mArtistsList = savedInstanceState.getParcelableArrayList(LIST_STATE);

            mArtistAdapter = new ArtistsAdapter(getActivity(),
                    mArtistsList);
            mListView.setAdapter(mArtistAdapter);

            if (mListState != null)
                mListView.onRestoreInstanceState(mListState);

            mListView.setSelectionFromTop(mListPosition, mItemPosition);
        }
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

        outState.putString(QUERY, mQuery);
        outState.putParcelableArrayList(LIST_STATE, mArtistsList);

        super.onSaveInstanceState(outState);
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.  For this method
     * to be called, you must have first called {@link #setHasOptionsMenu}.  See
     * {@link Activity#onCreateOptionsMenu(Menu) Activity.onCreateOptionsMenu}
     * for more information.
     *
     * @param menu     The options menu in which you place your items.
     * @param inflater
     * @see #setHasOptionsMenu
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setIconifiedByDefault(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                MenuItemCompat.collapseActionView(searchItem);
                searchView.setQuery("", false);
                mQuery = query;

                mDataFetcher.getArtist(urlEncode(mQuery), ArtistsFragment.this);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     * <p/>
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(getActivity(), UserPreferencesActivity.class);
            getActivity().startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
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
    public void success(ArtistsPager artistsPager, Response response) {
        mArtistsList = new ArrayList<ArtistInfo>();

        if (artistsPager != null) {
            for (Artist artist : artistsPager.artists.items) {
                ArtistInfo artistInfo = new ArtistInfo();
                artistInfo.setName(artist.name);
                artistInfo.setId(artist.id);

                if (artist.images != null && artist.images.size() > 0) {
                    artistInfo.setIconUrl(artist.images.get(0).url);
                }

                mArtistsList.add(artistInfo);
            }
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //   mProgressBar.setVisibility(View.GONE);

                if (mArtistsList.size() > 1) {
                    mArtistAdapter = new ArtistsAdapter(getActivity(),
                            mArtistsList);
                    mListView.setAdapter(mArtistAdapter);

                    if (mListState != null)
                        mListView.onRestoreInstanceState(mListState);

                    mListView.setSelectionFromTop(mListPosition, mItemPosition);
                } else if (mArtistsList.size() == 1) {
                    ArtistInfo artist = mArtistsList.get(0);

                    SingleArtistFragment fragment = SingleArtistFragment.newInstance(artist.getName(),
                            artist.getIconUrl(), artist.getId());
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.container, fragment);
                    ft.commit();
                } else {
                    Toast.makeText(getActivity(), R.string.no_artist_found,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void failure(RetrofitError error) {
        final String msg = error.getMessage();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //   mProgressBar.setVisibility(View.GONE);

                Toast.makeText(getActivity(), msg,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
