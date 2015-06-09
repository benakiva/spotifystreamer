package com.ubimobitech.spotifystreamer;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SingleTopTrackFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SingleTopTrackFragment extends Fragment {
    private static final String ARG_TRACK_NAME = "com.ubimobitech.spotifystreamer.ARG_TRACK_NAME";
    private static final String ARG_ALBUM_IMG = "com.ubimobitech.spotifystreamer.ARG_ALBUM_IMG";
    private static final String ARG_ALBUM_NAME = "com.ubimobitech.spotifystreamer.ARG_ALBUM_NAME";

    private String mTrackName;
    private String mAlbumImg;
    private String mAlbumName;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param trackName track name.
     * @param albumImg artist name.
     * @param albumName album name
     * @return A new instance of fragment SingleTopTrackFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SingleTopTrackFragment newInstance(String trackName, String albumImg,
                                                     String albumName) {
        SingleTopTrackFragment fragment = new SingleTopTrackFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TRACK_NAME, trackName);
        args.putString(ARG_ALBUM_IMG, albumImg);
        args.putString(ARG_ALBUM_NAME, albumName);
        fragment.setArguments(args);

        return fragment;
    }

    public SingleTopTrackFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTrackName = getArguments().getString(ARG_TRACK_NAME);
            mAlbumImg = getArguments().getString(ARG_ALBUM_IMG);
            mAlbumName = getArguments().getString(ARG_ALBUM_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.single_top_track_item, container, false);

        ImageView icon = (ImageView) view.findViewById(R.id.album_icon);
        TextView track = (TextView) view.findViewById(R.id.track_title);
        TextView album = (TextView) view.findViewById(R.id.album_title);

        Picasso.with(getActivity()).load(mAlbumImg).into(icon);
        track.setText(mTrackName);
        album.setText(mAlbumName);

        return view;
    }
}
