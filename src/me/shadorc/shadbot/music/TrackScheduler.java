package me.shadorc.shadbot.music;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class TrackScheduler {

	private final AudioPlayer audioPlayer;
	private final BlockingDeque<AudioTrack> queue;

	private RepeatMode repeatMode;
	private AudioTrack currentTrack;

	public TrackScheduler(AudioPlayer audioPlayer, int defaultVolume) {
		this.audioPlayer = audioPlayer;
		this.queue = new LinkedBlockingDeque<>();
		this.repeatMode = RepeatMode.NONE;
		this.setVolume(defaultVolume);
	}

	/**
	 * @return true if the music has been started, false if it was added to the queue
	 */
	public boolean startOrQueue(AudioTrack track, boolean first) {
		// The track has been started
		if(this.audioPlayer.startTrack(track.makeClone(), true)) {
			this.currentTrack = track;
			return true;
		} else if(first) {
			this.queue.offerFirst(track);
		} else {
			this.queue.offerLast(track);
		}
		return false;
	}

	public boolean nextTrack() {
		switch (this.repeatMode) {
			case PLAYLIST:
				this.queue.offer(this.currentTrack.makeClone());
			case NONE:
				this.currentTrack = this.queue.poll();
				return this.audioPlayer.startTrack(this.currentTrack, false);
			case SONG:
				this.audioPlayer.playTrack(this.currentTrack.makeClone());
				break;
		}
		return true;
	}

	public void skipTo(int num) {
		AudioTrack track = null;
		for(int i = 0; i < num; i++) {
			track = this.queue.poll();
		}
		this.audioPlayer.playTrack(track.makeClone());
		this.currentTrack = track;
	}

	public long changePosition(long time) {
		long newPosition = this.audioPlayer.getPlayingTrack().getPosition() + time;
		newPosition = Math.max(0, Math.min(this.audioPlayer.getPlayingTrack().getDuration(), newPosition));
		this.audioPlayer.getPlayingTrack().setPosition(newPosition);
		return newPosition;
	}

	public void shufflePlaylist() {
		final List<AudioTrack> tempList = new ArrayList<>(this.queue);
		Collections.shuffle(tempList);
		this.queue.clear();
		this.queue.addAll(tempList);
	}

	public void clearPlaylist() {
		this.queue.clear();
	}

	public BlockingQueue<AudioTrack> getPlaylist() {
		return this.queue;
	}

	public AudioPlayer getAudioPlayer() {
		return this.audioPlayer;
	}

	public RepeatMode getRepeatMode() {
		return this.repeatMode;
	}

	public boolean isPlaying() {
		return this.audioPlayer.getPlayingTrack() != null;
	}

	public boolean isStopped() {
		return this.queue.isEmpty() && !this.isPlaying();
	}

	public void setVolume(int volume) {
		this.audioPlayer.setVolume(Math.max(0, Math.min(100, volume)));
	}

	public void setRepeatMode(RepeatMode repeatMode) {
		this.repeatMode = repeatMode;
	}
}