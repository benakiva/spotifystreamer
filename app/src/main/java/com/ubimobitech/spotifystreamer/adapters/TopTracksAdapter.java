/**
 * FILE: TopTracksAdapter.java
 * AUTHOR: Dr. Isaac Ben-Akiva <isaac.ben-akiva@ubimobitech.com>
 * <p/>
 * CREATED ON: 09/06/15
 */

package com.ubimobitech.spotifystreamer.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.ubimobitech.spotifystreamer.R;
import com.ubimobitech.spotifystreamer.model.TrackInfo;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by benakiva on 09/06/15.
 */
public class TopTracksAdapter extends BaseAdapter {
    private ArrayList<TrackInfo> mTracks;
    private Context mContext;

    public TopTracksAdapter(Context context, ArrayList<TrackInfo> tracks) {
        mContext = context;
        mTracks = tracks;
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return mTracks.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return mTracks.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.top_tracks_list_item,
                    parent, false);

            holder = new ViewHolder(convertView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TrackInfo track = (TrackInfo) getItem(position);

        if (!TextUtils.isEmpty(track.getImgUrl())) {
            Picasso.with(mContext).load(track.getImgUrl()).into(holder.icon);
            holder.icon.setVisibility(View.VISIBLE);
        } else {
            holder.icon.setVisibility(View.INVISIBLE);
        }

        holder.album.setText(track.getAlbumName());
        holder.track.setText(track.getTrackName());

        return convertView;
    }

    static class ViewHolder {
        @InjectView(R.id.album_icon) ImageView icon;
        @InjectView(R.id.track_title) TextView track;
        @InjectView(R.id.album_title) TextView album;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
