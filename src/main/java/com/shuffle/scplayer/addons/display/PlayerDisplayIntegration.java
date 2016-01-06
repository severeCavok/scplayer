package com.shuffle.scplayer.addons.display;

import com.shuffle.scplayer.core.PlayerListener;
import com.shuffle.scplayer.core.Track;

public class PlayerDisplayIntegration implements PlayerListener {

	private Display display;

	public PlayerDisplayIntegration() {
    	this.display = new Display();
    }

    @Override
    public void onActive() {
    	display.setActive(true);
    }

    @Override
    public void onInactive() {
    	display.setActive(false);
    }

    @Override
    public void onTokenLost() {
    }

    @Override
    public void onPlay() {
    	display.setPlaying(true);
    }

    @Override
    public void onPause() {
    	display.setPlaying(false);
    }

    @Override
    public void onSeek(int millis) {
    }

    @Override
    public void onTrackChanged(Track track) {
    	display.setTrack(track);
    }

    @Override
    public void onNextTrack(Track track) {
    }

    @Override
    public void onPreviousTrack(Track track) {
    }

    @Override
    public void onShuffle(boolean enabled) {
    }

    @Override
    public void onRepeat(boolean enabled) {
    }

    @Override
    public void onVolumeChanged(short volume) {
    	display.showVolume(volume);
    }

    @Override
    public void onPlayerNameChanged(String playerName) {
    }
}
