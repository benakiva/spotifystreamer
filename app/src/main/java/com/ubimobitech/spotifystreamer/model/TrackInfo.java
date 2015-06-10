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

    public void setAlbumName(String albumName) {
        this.mAlbumName = albumName;
    }

    public void setArtistName(String artistName) {
        this.mArtistName = artistName;
    }

    public void setImgUrl(String imgUrl) {
        this.mImgUrl = imgUrl;
    }

    public String getmTrackName() {
        return mTrackName;
    }

    public void setmTrackName(String mTrackName) {
        this.mTrackName = mTrackName;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mAlbumName);
        out.writeString(mArtistName);
        out.writeString(mTrackName);
        out.writeString(mImgUrl);
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
    }
}
