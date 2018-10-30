package me.shadorc.shadbot.music;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import me.shadorc.shadbot.Config;
import me.shadorc.shadbot.Shadbot;
import me.shadorc.shadbot.listener.music.AudioEventListener;
import me.shadorc.shadbot.shard.ShardManager;
import me.shadorc.shadbot.utils.BotUtils;
import me.shadorc.shadbot.utils.LogUtils;
import me.shadorc.shadbot.utils.object.Emoji;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.RequestBuffer;

public class GuildMusic {

	private final IGuild guild;
	private final AudioPlayer audioPlayer;
	private final AudioProvider audioProvider;
	private final TrackScheduler trackScheduler;

	private ScheduledFuture<?> leaveTask;
	private IChannel channel;
	private IUser userDj;
	private boolean isWaiting;

	public GuildMusic(IGuild guild, AudioPlayerManager audioPlayerManager) {
		this.guild = guild;
		this.audioPlayer = audioPlayerManager.createPlayer();
		this.audioProvider = new AudioProvider(this.audioPlayer);
		this.trackScheduler = new TrackScheduler(this.audioPlayer, Config.DEFAULT_VOLUME);
		this.audioPlayer.addListener(new AudioEventListener(this));
	}

	public void scheduleLeave() {
		this.leaveTask = Shadbot.getScheduler().schedule(() -> this.leaveVoiceChannel(), 1, TimeUnit.MINUTES);
	}

	public void cancelLeave() {
		if(this.leaveTask != null) {
			this.leaveTask.cancel(false);
		}
	}

	public void joinVoiceChannel(IVoiceChannel voiceChannel) {
		if(voiceChannel.getClient().getOurUser().getVoiceStateForGuild(this.guild).getChannel() == null) {
			voiceChannel.join();
			LogUtils.infof("{Guild ID: %d} Voice channel joined.", voiceChannel.getGuild().getLongID());
		}
	}

	public void end() {
		final StringBuilder strBuilder = new StringBuilder(Emoji.INFO + " End of the playlist.");
		strBuilder.append(String.format(" If you like me, you can make a donation on **%s**, it will help my creator keeping me alive :heart:",
				Config.PATREON_URL));
		BotUtils.sendMessage(strBuilder.toString(), this.channel);
		this.leaveVoiceChannel();
	}

	public void leaveVoiceChannel() {
		// Leaving a voice channel can take up to 30 seconds to be executed
		// We execute it in a separate thread pool to avoid thread blocking
		ShardManager.execute(this.channel.getGuild(), () -> {
			final IVoiceChannel voiceChannel = Shadbot.getClient().getOurUser().getVoiceStateForGuild(this.guild).getChannel();
			if(voiceChannel != null && voiceChannel.getShard().isReady()) {
				RequestBuffer.request(() -> {
					voiceChannel.leave();
				});
			}
		});
	}

	public void delete() {
		this.cancelLeave();
		GuildMusicManager.GUILD_MUSIC_MAP.remove(this.guild.getLongID());
		this.audioPlayer.destroy();
		this.trackScheduler.clearPlaylist();
	}

	public IChannel getChannel() {
		return this.channel;
	}

	public IUser getDj() {
		return this.userDj;
	}

	public AudioProvider getAudioProvider() {
		return this.audioProvider;
	}

	public TrackScheduler getScheduler() {
		return this.trackScheduler;
	}

	public boolean isLeavingScheduled() {
		return this.leaveTask != null && !this.leaveTask.isDone();
	}

	public boolean isWaiting() {
		return this.isWaiting;
	}

	public void setChannel(IChannel channel) {
		this.channel = channel;
	}

	public void setDj(IUser userDj) {
		this.userDj = userDj;
	}

	public void setWaiting(boolean isWaiting) {
		this.isWaiting = isWaiting;
	}

}
