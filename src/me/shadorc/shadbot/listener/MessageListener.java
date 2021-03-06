package me.shadorc.shadbot.listener;

import me.shadorc.shadbot.Config;
import me.shadorc.shadbot.core.command.CommandManager;
import me.shadorc.shadbot.core.command.Context;
import me.shadorc.shadbot.data.db.Database;
import me.shadorc.shadbot.data.stats.VariousStatsManager;
import me.shadorc.shadbot.data.stats.VariousStatsManager.VariousEnum;
import me.shadorc.shadbot.exception.IllegalCmdArgumentException;
import me.shadorc.shadbot.exception.MissingArgumentException;
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
		VariousStatsManager.log(VariousEnum.MESSAGES_RECEIVED);

		IMessage message = event.getMessage();
		try {
			if(message.getAuthor().isBot()) {
				return;
			}

			if(message.getChannel().isPrivate()) {
				this.privateMessageReceived(message);
				return;
			}

			ShardManager.getShadbotShard(message.getShard()).messageReceived();

			if(!BotUtils.hasAllowedRole(message.getGuild(), message.getGuild().getRolesForUser(message.getAuthor()))) {
				return;
			}

			if(!BotUtils.isChannelAllowed(message.getGuild(), message.getChannel())) {
				return;
			}

			if(MessageManager.intercept(message)) {
				return;
			}

			String prefix = Database.getDBGuild(message.getGuild()).getPrefix();
			if(message.getContent().startsWith(prefix)) {
				CommandManager.execute(new Context(prefix, message));
			}
		} catch (MissingPermissionsException err) {
			BotUtils.sendMessage(TextUtils.missingPerm(err.getMissingPermissions()), message.getChannel());
			LogUtils.infof("{Guild ID: %d} %s", message.getGuild().getLongID(), err.getMessage());
		} catch (Exception err) {
			BotUtils.sendMessage(Emoji.RED_FLAG + " Sorry, an unknown error occurred. My developer has been warned.", message.getChannel());
			LogUtils.error(message.getContent(), err,
					String.format("{Guild ID: %d} An unknown error occurred while receiving a message.", message.getGuild().getLongID()));
		}
	}

	private void privateMessageReceived(IMessage message) throws MissingArgumentException, IllegalCmdArgumentException {
		VariousStatsManager.log(VariousEnum.PRIVATE_MESSAGES_RECEIVED);

		if(message.getContent().startsWith(Config.DEFAULT_PREFIX + "help")) {
			CommandManager.getCommand("help").execute(new Context(Config.DEFAULT_PREFIX, message));
			return;
		}

		// If Shadbot didn't already send a message
		if(!message.getChannel().getMessageHistory().stream().anyMatch(msg -> msg.getAuthor().equals(message.getClient().getOurUser()))) {
			BotUtils.sendMessage(String.format("Hello !"
					+ "%nCommands only work in a server but you can see help using `%shelp`."
					+ "%nIf you have a question, a suggestion or if you just want to talk, don't hesitate to "
					+ "join my support server : %s", Config.DEFAULT_PREFIX, Config.SUPPORT_SERVER), message.getChannel());
		}
	}
}