package me.shadorc.shadbot.listener.music;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import me.shadorc.shadbot.Config;
import me.shadorc.shadbot.Shadbot;
import me.shadorc.shadbot.core.command.CommandManager;
import me.shadorc.shadbot.message.MessageListener;
import me.shadorc.shadbot.message.MessageManager;
import me.shadorc.shadbot.music.GuildMusic;
import me.shadorc.shadbot.music.GuildMusicManager;
import me.shadorc.shadbot.utils.BotUtils;
import me.shadorc.shadbot.utils.CastUtils;
import me.shadorc.shadbot.utils.FormatUtils;
import me.shadorc.shadbot.utils.LogUtils;
import me.shadorc.shadbot.utils.StringUtils;
import me.shadorc.shadbot.utils.TextUtils;
import me.shadorc.shadbot.utils.embed.EmbedUtils;
import me.shadorc.shadbot.utils.object.Emoji;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.EmbedBuilder;

public class AudioLoadResultListener implements AudioLoadResultHandler, MessageListener {

	public static final String YT_SEARCH = "ytsearch: ";
	public static final String SC_SEARCH = "scsearch: ";

	private static final int CHOICE_DURATION = 30;

	private final GuildMusic guildMusic;
	private final IUser userDj;
	private final IVoiceChannel userVoiceChannel;
	private final String identifier;
	private final boolean putFirst;

	private List<AudioTrack> resultsTracks;
	private ScheduledFuture<?> stopWaitingTask;

	public AudioLoadResultListener(GuildMusic guildMusic, IUser userDj, IVoiceChannel userVoiceChannel, String identifier, boolean putFirst) {
		this.guildMusic = guildMusic;
		this.userDj = userDj;
		this.userVoiceChannel = userVoiceChannel;
		this.identifier = identifier;
		this.putFirst = putFirst;
	}

	@Override
	public void trackLoaded(AudioTrack track) {
		this.guildMusic.joinVoiceChannel(this.userVoiceChannel);
		if(!this.guildMusic.getScheduler().startOrQueue(track, this.putFirst)) {
			BotUtils.sendMessage(String.format(Emoji.MUSICAL_NOTE + " **%s** has been added to the playlist.",
					FormatUtils.formatTrackName(track.getInfo())), this.guildMusic.getChannel());
		}
	}

	@Override
	public void playlistLoaded(AudioPlaylist playlist) {
		final List<AudioTrack> tracks = playlist.getTracks();

		// SoundCloud returns an empty playlist when it has not found any results
		if(tracks.isEmpty()) {
			this.onNoMatches();
			return;
		}

		if(this.identifier.startsWith(YT_SEARCH) || this.identifier.startsWith(SC_SEARCH)) {
			this.guildMusic.setDj(this.userDj);
			this.guildMusic.setWaiting(true);

			final String choices = FormatUtils.numberedList(5, tracks.size(), count -> String.format("\t**%d.** %s",
					count, FormatUtils.formatTrackName(tracks.get(count - 1).getInfo())));

			final EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
					.withAuthorName("Music results")
					.withAuthorIcon(this.guildMusic.getDj().getAvatarURL())
					.withThumbnail("http://icons.iconarchive.com/icons/dtafalonso/yosemite-flat/512/Music-icon.png")
					.appendDescription("**Select a music by typing the corresponding number.**"
							+ "\nYou can choose several musics by separating them with a comma."
							+ "\nExample: 1,3,4"
							+ "\n\n" + choices)
					.withFooterText(String.format("Use %scancel to cancel the selection (Automatically canceled in %ds).",
							Config.DEFAULT_PREFIX, CHOICE_DURATION));
			BotUtils.sendMessage(embed.build(), this.guildMusic.getChannel());

			this.stopWaitingTask = Shadbot.getScheduler().schedule(() -> this.stopWaiting(), CHOICE_DURATION, TimeUnit.SECONDS);

			this.resultsTracks = new ArrayList<>(tracks);
			MessageManager.addListener(this.guildMusic.getChannel(), this);
			return;
		}

		this.guildMusic.joinVoiceChannel(this.userVoiceChannel);

		int musicsAdded = 0;
		for(final AudioTrack track : tracks) {
			this.guildMusic.getScheduler().startOrQueue(track, this.putFirst);
			musicsAdded++;
			if(this.guildMusic.getScheduler().getPlaylist().size() >= Config.MAX_PLAYLIST_SIZE - 1) {
				BotUtils.sendMessage(TextUtils.PLAYLIST_LIMIT_REACHED, this.guildMusic.getChannel());
				break;
			}
		}

		BotUtils.sendMessage(String.format(Emoji.MUSICAL_NOTE + " %d musics have been added to the playlist.", musicsAdded), this.guildMusic.getChannel());
	}

	@Override
	public void loadFailed(FriendlyException err) {
		final String errMessage = Jsoup.parse(StringUtils.remove(err.getMessage(), "Watch on YouTube")).text().trim();
		BotUtils.sendMessage(Emoji.RED_CROSS + " Sorry, " + errMessage.toLowerCase(), this.guildMusic.getChannel());
		LogUtils.infof("{Guild ID: %d} Load failed: %s", this.guildMusic.getChannel().getGuild().getLongID(), errMessage);

		if(this.guildMusic.getScheduler().isStopped()) {
			this.guildMusic.leaveVoiceChannel();
		}
	}

	@Override
	public void noMatches() {
		this.onNoMatches();
	}

	private void onNoMatches() {
		BotUtils.sendMessage(TextUtils.noResult(StringUtils.remove(this.identifier, YT_SEARCH, SC_SEARCH)), this.guildMusic.getChannel());
		LogUtils.infof("{Guild ID: %d} No matches: %s", this.guildMusic.getChannel().getGuild().getLongID(), this.identifier);

		if(this.guildMusic.getScheduler().isStopped()) {
			this.guildMusic.leaveVoiceChannel();
		}
	}

	@Override
	public boolean intercept(IMessage message) {
		if(!message.getAuthor().equals(this.guildMusic.getDj())) {
			return false;
		}

		if(message.getContent().equalsIgnoreCase(Config.DEFAULT_PREFIX + "cancel")) {
			BotUtils.sendMessage(Emoji.CHECK_MARK + " Choice cancelled.", this.guildMusic.getChannel());
			this.stopWaiting();
			return true;
		}

		// Remove prefix and command names from message content
		String content = message.getContent().toLowerCase();
		for(final String cmdName : CommandManager.getCommand("play").getNames()) {
			content = StringUtils.remove(content, Config.DEFAULT_PREFIX, cmdName);
		}
		content = content.trim();

		final List<Integer> choices = new ArrayList<>();
		for(final String str : content.split(",")) {
			final Integer num = CastUtils.asIntBetween(str, 1, Math.min(5, this.resultsTracks.size()));
			if(num == null) {
				return false;
			}

			if(!choices.contains(num)) {
				choices.add(num);
			}
		}

		// If the manager was removed from the list while an user chose a music, we re-add it and join voice channel
		GuildMusicManager.GUILD_MUSIC_MAP.putIfAbsent(message.getGuild().getLongID(), this.guildMusic);
		this.guildMusic.joinVoiceChannel(this.userVoiceChannel);

		// Joining a voice channel can take several seconds to be completed, if in the mean time someone chosen a music, resultsTracks will be empty.
		if(this.resultsTracks.isEmpty()) {
			LogUtils.warnf("{Guild ID: %d} Results tracks were empty.", message.getGuild().getLongID());
			return true;
		}

		for(final int choice : choices) {
			final AudioTrack track = this.resultsTracks.get(choice - 1);
			if(this.guildMusic.getScheduler().isPlaying()) {
				BotUtils.sendMessage(Emoji.MUSICAL_NOTE + " **" + FormatUtils.formatTrackName(track.getInfo())
						+ "** has been added to the playlist.", this.guildMusic.getChannel());
			}
			this.guildMusic.getScheduler().startOrQueue(track, this.putFirst);
			if(this.guildMusic.getScheduler().getPlaylist().size() >= Config.MAX_PLAYLIST_SIZE - 1) {
				BotUtils.sendMessage(TextUtils.PLAYLIST_LIMIT_REACHED, this.guildMusic.getChannel());
				break;
			}
		}

		this.stopWaiting();
		return true;
	}

	private void stopWaiting() {
		this.stopWaitingTask.cancel(false);
		MessageManager.removeListener(this.guildMusic.getChannel(), this);
		this.guildMusic.setWaiting(false);
		this.resultsTracks.clear();

		if(this.guildMusic.getScheduler().isStopped()) {
			this.guildMusic.leaveVoiceChannel();
		}
	}
}
