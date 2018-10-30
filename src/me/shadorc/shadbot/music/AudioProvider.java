package me.shadorc.shadbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import sx.blah.discord.handle.audio.AudioEncodingType;
import sx.blah.discord.handle.audio.IAudioProvider;

public class AudioProvider implements IAudioProvider {

	private final AudioPlayer audioPlayer;
	private AudioFrame lastFrame;

	public AudioProvider(AudioPlayer audioPlayer) {
		this.audioPlayer = audioPlayer;
	}

	@Override
	public boolean isReady() {
		if(this.lastFrame == null) {
			this.lastFrame = this.audioPlayer.provide();
		}

		return this.lastFrame != null;
	}

	@Override
	public byte[] provide() {
		if(this.lastFrame == null) {
			this.lastFrame = this.audioPlayer.provide();
		}

		final byte[] data = this.lastFrame == null ? null : this.lastFrame.data;
		this.lastFrame = null;

		return data;
	}

	@Override
	public AudioEncodingType getAudioEncodingType() {
		return AudioEncodingType.OPUS;
	}
}
