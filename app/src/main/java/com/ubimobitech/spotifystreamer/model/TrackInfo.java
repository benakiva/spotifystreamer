/**
 * FILE: TrackInfo.java
 * AUTHOR: Dr. Isaac Ben-Akiva <isaac.ben-akiva@ubimobitech.com>
 * <p/>
 * CREATED ON: 10/06/15
 */

package com.ubimobitech.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by benakiva on 10/06/15.
 */
public class TrackInfo implements Parcelable {
    private String mAlbumName;
    private String mArtistName;
    private String mTrackName;
    private String mImgUrl;
    private String mPreviewUrl;
    private long mDuration_ms;

    public TrackInfo() {

    }

    public String getAlbumName() {
        return mAlbumName;
    }

    public String getArtistName() {
        return mArtistName;
    }

    public String getImgUrl() {
        return mImgUrl;
    }

    public String getPreviewUrl() {
        return mPreviewUrl;
    }

    public long getDuration() {
        return mDuration_ms;
    }

    public void setAlbumName(String albumName) {
        this.mAlbumName = albumName;
    }

    public void setArtistName(String artistName) {
        this.mArtistName = artistName;
    }

    public void setImgUrl(String imgUrl) {
        this.mImgUrl = imgUrl;
    }

    public String getTrackName() {
        return mTrackName;
    }

    public void setTrackName(String trackName) {
        this.mTrackName = trackName;
    }

    public void setPreviewUrl(String previewUrl) {
        mPreviewUrl = previewUrl;
    }

    public void setDuration(long duration) {
        mDuration_ms = duration;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mAlbumName);
        out.writeString(mArtistName);
        out.writeString(mTrackName);
        out.writeString(mImgUrl);
        out.writeString(mPreviewUrl);
        out.writeLong(mDuration_ms);
    }

    public static final Parcelable.Creator<TrackInfo> CREATOR
            = new Parcelable.Creator<TrackInfo>() {
        public TrackInfo createFromParcel(Parcel in) {
            return new TrackInfo(in);
        }

        public TrackInfo[] newArray(int size) {
            return new TrackInfo[size];
        }
    };

    private TrackInfo(Parcel in) {
        mAlbumName = in.readString();
        mArtistName = in.readString();
        mTrackName = in.readString();
        mImgUrl = in.readString();
        mPreviewUrl = in.readString();
        mDuration_ms = in.readLong();
    }

    public String getFormattedDuration() {
        long seconds = (mDuration_ms / 1000) + 1;
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }
}
