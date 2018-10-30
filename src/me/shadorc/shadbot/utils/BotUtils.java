package me.shadorc.shadbot.utils;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.PermissionUtils;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.RequestBuffer.RequestFuture;

public class BotUtils {

	public static RequestFuture<IMessage> sendMessage(String content, IChannel channel) {
		if(!BotUtils.hasPermissions(channel, Permissions.SEND_MESSAGES)) {
			LogUtils.infof("{Guild ID: %d} Shadbot wasn't allowed to send a message.", channel.getGuild().getLongID());
			return null;
		}

		return BotUtils.sendMessage(new MessageBuilder(channel.getClient()).withChannel(channel).withContent(content));
	}

	public static RequestFuture<IMessage> sendMessage(EmbedObject embed, IChannel channel) {
		if(!BotUtils.hasPermissions(channel, Permissions.SEND_MESSAGES, Permissions.EMBED_LINKS)) {
			BotUtils.sendMessage(TextUtils.missingPerm(Permissions.EMBED_LINKS), channel);
			LogUtils.infof("{Guild ID: %d} Shadbot wasn't allowed to send embed link.", channel.getGuild().getLongID());
			return null;
		}

		return BotUtils.sendMessage(new MessageBuilder(channel.getClient()).withChannel(channel).withEmbed(embed));
	}

	public static RequestFuture<IMessage> sendMessage(MessageBuilder message) {
		return BotUtils.sendMessage(message, 3);
	}

	public static RequestFuture<IMessage> sendMessage(MessageBuilder message, int retry) {
		final IGuild guild = message.getChannel().isPrivate() ? null : message.getChannel().getGuild();
		final long guildID = guild == null ? -1 : guild.getLongID();

		if(retry == 0) {
			LogUtils.infof("{Guild ID: %d} Abort attempt to send message (3 failed requests).", guildID);
			return null;
		}

		if(!message.getChannel().getShard().isReady()) {
			if(guild != null) {
				LogUtils.infof("{Guild ID: %d} A message couldn't be sent because shard isn't ready, adding it to queue.", guildID);
			}
			return null;
		}

		return RequestBuffer.request(() -> {
			try {
				return message.send();
			} catch (final MissingPermissionsException err) {
				BotUtils.sendMessage(TextUtils.missingPerm(err.getMissingPermissions()), message.getChannel());
				LogUtils.infof("{Guild ID: %d} %s", guildID, err.getMessage());
			} catch (final DiscordException err) {
				if(err.getMessage().contains("Message was unable to be sent (Discord didn't return a response)")) {
					LogUtils.infof("{Guild ID: %d} A message could not be send because Discord didn't return a response, retrying.", guildID);
					final RequestFuture<IMessage> msgRequest = BotUtils.sendMessage(message, retry - 1);
					if(msgRequest != null) {
						return msgRequest.get();
					}
				} else if(err.getMessage().contains("Failed to make a 400 failed request after 5 tries!")) {
					LogUtils.infof("{Guild ID: %d} %s", guildID, err.getMessage());
				} else {
					LogUtils.error(err, "An error occurred while sending message.");
				}
			}
			return null;
		});
	}

	public static boolean hasPermissions(IChannel channel, Permissions... permissions) {
		return RequestBuffer.request(() -> {
			return PermissionUtils.hasPermissions(channel, channel.getClient().getOurUser(), permissions);
		}).get();
	}
}
