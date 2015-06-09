package com.ubimobitech.spotifystreamer;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.ubimobitech.spotifystreamer.interfaces.OnArtistClickListener;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SingleArtistFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SingleArtistFragment extends Fragment implements View.OnClickListener {
    private static final String ARG_ARTIST_NAME = "com.ubimobitech.spotifystreamer.ARG_ARTIST_NAME";
    private static final String ARG_IMG_URL = "com.ubimobitech.spotifystreamer.ARG_IMG_URL";
    private static final String ARG_ARTIST_ID = "com.ubimobitech.spotifystreamer.ARG_ARTIST_ID";
    private String mName;
    private String mImgUrl;
    private String mArtistId;
    private OnArtistClickListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param name Artist name.
     * @param imgUrl artist's icon url.
     * @return A new instance of fragment SingleArtistFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SingleArtistFragment newInstance(String name, String imgUrl, String id) {
        SingleArtistFragment fragment = new SingleArtistFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ARTIST_NAME, name);
        args.putString(ARG_IMG_URL, imgUrl);
        args.putString(ARG_ARTIST_ID, id);
        fragment.setArguments(args);

        return fragment;
    }

    public SingleArtistFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mName = getArguments().getString(ARG_ARTIST_NAME);
            mImgUrl = getArguments().getString(ARG_IMG_URL);
            mArtistId = getArguments().getString(ARG_ARTIST_ID);
        }

        setRetainInstance(true);
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.single_artist, container, false);
        view.setOnClickListener(this);

        ImageView icon = (ImageView) view.findViewById(R.id.artist_icon);
        TextView name = (TextView) view.findViewById(R.id.artist_name);
        name.setText(mName);

        if (!TextUtils.isEmpty(mImgUrl))
            Picasso.with(getActivity()).load(mImgUrl).into(icon);

        return view;
    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        mListener.onArtistClick(mArtistId);
    }
}
