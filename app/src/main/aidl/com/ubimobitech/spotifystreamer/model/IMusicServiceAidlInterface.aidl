// IMusicServiceAidlInterface.aidl
package com.ubimobitech.spotifystreamer.model;

import com.ubimobitech.spotifystreamer.model.TrackInfo;

interface IMusicServiceAidlInterface {
	boolean isPlaying();
	void stop();
	void pause();
	void play(in TrackInfo track, int position);
	void prev();
	void next();
	long duration();
	int getCurrentposition();
	int getState();
	void seekTo(int position);
	void setQueue(in List<TrackInfo> queue);
	void setTrackName(String track);
	void setAlbumName(String album);
	void setArtistName(String name);
	String getTrackName();
	String getAlbumName();
	String getArtistName();
}
