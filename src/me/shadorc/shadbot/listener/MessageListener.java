package me.shadorc.shadbot.listener;

import me.shadorc.shadbot.Config;
import me.shadorc.shadbot.core.command.CommandManager;
import me.shadorc.shadbot.core.command.Context;
import me.shadorc.shadbot.message.MessageManager;
import me.shadorc.shadbot.shard.ShardManager;
import me.shadorc.shadbot.utils.BotUtils;
import me.shadorc.shadbot.utils.LogUtils;
import me.shadorc.shadbot.utils.TextUtils;
import me.shadorc.shadbot.utils.object.Emoji;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MissingPermissionsException;

public class MessageListener {

	@EventSubscriber
	public void onMessageEvent(MessageEvent event) {
		ShardManager.execute(event.getGuild(), () -> {
			if(event instanceof MessageReceivedEvent) {
				this.onMessageReceivedEvent((MessageReceivedEvent) event);
			}
		});
	}

	private void onMessageReceivedEvent(MessageReceivedEvent event) {
		final IMessage message = event.getMessage();
		try {
			if(message.getAuthor().isBot()) {
				return;
			}

			if(message.getChannel().isPrivate()) {
				return;
			}

			if(MessageManager.intercept(message)) {
				return;
			}

			if(message.getContent().startsWith(Config.DEFAULT_PREFIX)) {
				CommandManager.execute(new Context(Config.DEFAULT_PREFIX, message));
			}
		} catch (final MissingPermissionsException err) {
			BotUtils.sendMessage(TextUtils.missingPerm(err.getMissingPermissions()), message.getChannel());
			LogUtils.infof("{Guild ID: %d} %s", message.getGuild().getLongID(), err.getMessage());
		} catch (final Exception err) {
			BotUtils.sendMessage(Emoji.RED_FLAG + " Sorry, an unknown error occurred. My developer has been warned.", message.getChannel());
			LogUtils.error(message.getContent(), err,
					String.format("{Guild ID: %d} An unknown error occurred while receiving a message.", message.getGuild().getLongID()));
		}
	}

}