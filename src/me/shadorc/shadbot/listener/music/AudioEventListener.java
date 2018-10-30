package me.shadorc.shadbot.listener.music;

import org.jsoup.Jsoup;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import me.shadorc.shadbot.music.GuildMusic;
import me.shadorc.shadbot.utils.BotUtils;
import me.shadorc.shadbot.utils.FormatUtils;
import me.shadorc.shadbot.utils.LogUtils;
import me.shadorc.shadbot.utils.StringUtils;
import me.shadorc.shadbot.utils.object.Emoji;

public class AudioEventListener extends AudioEventAdapter {

	private final GuildMusic guildMusic;

	private int errorCount;

	public AudioEventListener(GuildMusic guildMusic) {
		super();
		this.guildMusic = guildMusic;
		this.errorCount = 0;
	}

	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		BotUtils.sendMessage(String.format(Emoji.MUSICAL_NOTE + " Currently playing: **%s**", FormatUtils.formatTrackName(track.getInfo())),
				this.guildMusic.getChannel());
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if(endReason.mayStartNext) {
			this.errorCount = 0; // Everything seems to be fine, reset error count.
			if(!this.guildMusic.getScheduler().nextTrack()) {
				this.guildMusic.end();
			}
		}
	}

	@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException err) {
		this.errorCount++;

		final String errMessage = Jsoup.parse(StringUtils.remove(err.getMessage(), "Watch on YouTube")).text().trim();

		if(this.errorCount <= 3) {
			BotUtils.sendMessage(String.format(Emoji.RED_CROSS + " Sorry, %s. I'll try to play the next available song.",
					errMessage.toLowerCase()), this.guildMusic.getChannel());
		}

		if(this.errorCount == 3) {
			BotUtils.sendMessage(Emoji.RED_FLAG + " Too many errors in a row, I will ignore them until finding a music that can be played.",
					this.guildMusic.getChannel());
			LogUtils.infof("{Guild ID: %d} Too many errors in a row. They will be ignored until music can be played.",
					this.guildMusic.getChannel().getGuild().getLongID());
		}

		LogUtils.infof("{Guild ID: %d} %sTrack exception: %s",
				this.guildMusic.getChannel().getGuild().getLongID(), this.errorCount > 3 ? "(Ignored) " : "", errMessage);

		if(!this.guildMusic.getScheduler().nextTrack()) {
			this.guildMusic.end();
		}
	}

	@Override
	public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
		BotUtils.sendMessage(Emoji.RED_EXCLAMATION + " Music seems stuck, I'll try to play the next available song.", this.guildMusic.getChannel());
		LogUtils.warnf("{Guild ID: %d} Music stuck, skipping it.", this.guildMusic.getChannel().getGuild().getLongID());

		if(!this.guildMusic.getScheduler().nextTrack()) {
			this.guildMusic.end();
		}
	}
}
