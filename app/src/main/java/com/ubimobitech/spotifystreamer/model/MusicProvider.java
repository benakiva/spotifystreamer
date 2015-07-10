/*
 * Copyright 2015, Isaac Ben-Akiva
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * FILE: MusicProvider.java
 * AUTHOR: Dr. Isaac Ben-Akiva <isaac.ben-akiva@ubimobitech.com>
 * <p/>
 * CREATED ON: 03/07/15
 */

package com.ubimobitech.spotifystreamer.model;

import android.support.v4.media.MediaMetadataCompat;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by benakiva on 03/07/15.
 */
public class MusicProvider {
    private List<MediaMetadataCompat> mTrackList;

    public static final String CUSTOM_METADATA_TRACK_PREVIEW_URL = "__PREVIEW_URL__";
    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    public MusicProvider() {
        mTrackList = new ArrayList<>();
    }

    public static MediaMetadataCompat buildFromTrack(Track track) {
        if (track != null) {
            return new MediaMetadataCompat.Builder()
                    .putString(CUSTOM_METADATA_TRACK_PREVIEW_URL, track.preview_url)
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, track.id)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track.album.name)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artists.get(0).name)
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, track.duration_ms)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, track.album.images.get(0).url)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.name)
                    .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, track.track_number)
                    .build();
        }

        return null;
    }
}
