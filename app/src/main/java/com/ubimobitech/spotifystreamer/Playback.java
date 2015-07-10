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
 * FILE: Playback
 * AUTHOR: Dr. Isaac Ben-Akiva <isaac.ben-akiva@ubimobitech.com>
 * <p/>
 * CREATED ON: 08/07/15
 */

package com.ubimobitech.spotifystreamer;

import com.ubimobitech.spotifystreamer.model.TrackInfo;

/**
 * Created by benakiva on 08/07/15.
 */
public interface Playback {
    boolean isPlaying();
    void stop();
    void pause();
    void play(TrackInfo track);
    void prev();
    void next();
    long duration();
    int getCurrentPosition();
    void seekTo(int position);
    void setState(int state);
    int getState();
    void setTrackName(String track);
    void setAlbumName(String album);
    void setArtistName(String name);
    String getTrackName();
    String getAlbumName();
    String getArtistName();

    interface Callback {
        /**
         * On current music completed.
         */
        void onCompletion();
        /**
         * on Playback status changed
         * Implementations can use this callback to update
         * playback state on the media sessions.
         */
        void onPlaybackStatusChanged(int state);

        /**
         * @param error to be added to the PlaybackState
         */
        void onError(String error);

        /**
         * @param mediaId being currently played
         */
        void onMetadataChanged(String mediaId);
    }

    /**
     * @param callback to be called
     */
    void setCallback(Callback callback);
}
