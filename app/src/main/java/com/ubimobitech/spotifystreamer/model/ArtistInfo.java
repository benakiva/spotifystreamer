/**
 * FILE: ArtistInfo.java
 * AUTHOR: Dr. Isaac Ben-Akiva <isaac.ben-akiva@ubimobitech.com>
 * <p/>
 * CREATED ON: 25/06/15
 */

package com.ubimobitech.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by benakiva on 25/06/15.
 */
public class ArtistInfo implements Parcelable {
    private String mName;
    private String mId;
    private String mIconUrl;

    public ArtistInfo() {}

    public ArtistInfo(Parcel in) {
        mName = in.readString();
        mId = in.readString();
        mIconUrl = in.readString();
    }

    public String getName() {
        return mName;
    }

    public String getId() {
        return mId;
    }

    public String getIconUrl() {
        return mIconUrl;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public void setIconUrl(String iconUrl) {
        this.mIconUrl = iconUrl;
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     * by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mId);
        dest.writeString(mIconUrl);
    }

    public static final Parcelable.Creator<ArtistInfo> CREATOR
            = new Parcelable.Creator<ArtistInfo>() {
        public ArtistInfo createFromParcel(Parcel in) {
            return new ArtistInfo(in);
        }

        public ArtistInfo[] newArray(int size) {
            return new ArtistInfo[size];
        }
    };
}
