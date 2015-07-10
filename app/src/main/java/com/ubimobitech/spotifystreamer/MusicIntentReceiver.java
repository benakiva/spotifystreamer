package com.ubimobitech.spotifystreamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.ubimobitech.spotifystreamer.service.MusicService;

public class MusicIntentReceiver extends BroadcastReceiver {
    public MusicIntentReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            Intent i = new Intent(context, MusicService.class);
            i.setAction(MusicService.ACTION_CMD);
            i.putExtra(MusicService.CMD_NAME, MusicService.CMD_PAUSE);
            context.sendBroadcast(intent);
        }
    }
}
