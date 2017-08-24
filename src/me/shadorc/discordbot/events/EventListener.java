package me.shadorc.discordbot.events;

import me.shadorc.discordbot.Config;
import me.shadorc.discordbot.Emoji;
import me.shadorc.discordbot.Shadbot;
import me.shadorc.discordbot.Storage;
import me.shadorc.discordbot.Storage.Setting;
import me.shadorc.discordbot.command.CommandManager;
import me.shadorc.discordbot.message.MessageManager;
import me.shadorc.discordbot.music.GuildMusicManager;
import me.shadorc.discordbot.utils.BotUtils;
import me.shadorc.discordbot.utils.LogUtils;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.GuildLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent.Reason;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IVoiceChannel;

@SuppressWarnings("ucd")
public class EventListener {

	@EventSubscriber
	public void onDisconnectedEvent(DisconnectedEvent event) {
		if(event.getReason().equals(Reason.LOGGED_OUT)) {
			LogUtils.info("------------------- Shadbot logged out [Version:" + Config.VERSION.toString() + "] -------------------");
		}
	}

	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event) {
		if(event.getAuthor().isBot()) {
			return;
		}

		if(event.getChannel().isPrivate()) {
			BotUtils.sendMessage(Emoji.INFO + " Sorry, I don't respond (yet ?) to private messages.", event.getChannel());
			LogUtils.info("Shadbot has received a private message. (Message: " + event.getMessage().getContent() + ")");
			return;
		}

		if(Config.VERSION.isBeta() && event.getChannel().getLongID() != Config.DEBUG_CHANNEL_ID
				|| !Config.VERSION.isBeta() && event.getChannel().getLongID() == Config.DEBUG_CHANNEL_ID) {
			return;
		}

		IMessage message = event.getMessage();
		if(MessageManager.isWaitingForMessage(event.getChannel())) {
			MessageManager.notify(message);
			return;
		}

		if(message.getContent().startsWith(Storage.getSetting(event.getGuild(), Setting.PREFIX).toString())) {
			CommandManager.getInstance().manage(event);
		}
	}

	@EventSubscriber
	public void onGuildCreateEvent(GuildCreateEvent event) {
		LogUtils.info("Shadbot connected to guild: " + event.getGuild().getName()
				+ " (ID: " + event.getGuild().getStringID()
				+ " | Users: " + event.getGuild().getUsers().size() + ")");
	}

	@EventSubscriber
	public void onGuildLeaveEvent(GuildLeaveEvent event) {
		LogUtils.info("Shadbot disconnected from guild: " + event.getGuild().getName()
				+ " (ID: " + event.getGuild().getStringID()
				+ " | Users: " + event.getGuild().getUsers().size() + ")");
	}

	@EventSubscriber
	public void onUserVoiceChannelJoinEvent(UserVoiceChannelJoinEvent event) {
		this.check(event.getGuild());
	}

	@EventSubscriber
	public void onUserVoiceChannelLeaveEvent(UserVoiceChannelLeaveEvent event) {
		this.check(event.getGuild());
	}

	@EventSubscriber
	public void onUserVoiceChannelMoveEvent(UserVoiceChannelMoveEvent event) {
		this.check(event.getGuild());
	}

	private void check(IGuild guild) {
		IVoiceChannel botVoiceChannel = Shadbot.getClient().getOurUser().getVoiceStateForGuild(guild).getChannel();
		if(botVoiceChannel != null) {

			GuildMusicManager gmm = GuildMusicManager.getGuildMusicManager(guild);

			if(gmm == null) {
				return;
			}

			if(this.isAlone(botVoiceChannel) && !gmm.isLeavingScheduled()) {
				BotUtils.sendMessage(Emoji.INFO + " Nobody is listening anymore, music paused. I will leave the voice channel in 1 minute.", gmm.getChannel());
				gmm.getScheduler().setPaused(true);
				gmm.scheduleLeave();

			} else if(!this.isAlone(botVoiceChannel) && gmm.isLeavingScheduled()) {
				BotUtils.sendMessage(Emoji.INFO + " Somebody joined me, music resumed.", gmm.getChannel());
				gmm.getScheduler().setPaused(false);
				gmm.cancelLeave();
			}
		}
	}

	private boolean isAlone(IVoiceChannel voiceChannel) {
		return voiceChannel.getConnectedUsers().stream().filter(user -> !user.isBot()).count() == 0;
	}
}