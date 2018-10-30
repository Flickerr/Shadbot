package me.shadorc.shadbot.core.command;

import java.util.List;

import me.shadorc.shadbot.shard.ShadbotShard;
import me.shadorc.shadbot.shard.ShardManager;
import me.shadorc.shadbot.utils.StringUtils;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;

public class Context {

	private final IMessage message;
	private final String prefix;
	private final String cmdName;
	private final String arg;

	public Context(String prefix, IMessage message) {
		this.message = message;
		this.prefix = prefix;

		final List<String> splittedMsg = StringUtils.split(message.getContent(), 2);
		this.cmdName = splittedMsg.get(0).substring(prefix.length()).toLowerCase();
		this.arg = splittedMsg.size() > 1 ? splittedMsg.get(1) : "";
	}

	public IMessage getMessage() {
		return this.message;
	}

	public String getContent() {
		return this.message.getContent();
	}

	public String getPrefix() {
		return this.prefix;
	}

	public String getCommandName() {
		return this.cmdName;
	}

	public String getArg() {
		return this.arg;
	}

	public IDiscordClient getClient() {
		return this.message.getClient();
	}

	public IUser getOurUser() {
		return this.message.getClient().getOurUser();
	}

	public ShadbotShard getShadbotShard() {
		return ShardManager.getShadbotShard(this.getShard());
	}

	public IShard getShard() {
		return this.message.getShard();
	}

	public IGuild getGuild() {
		return this.message.getGuild();
	}

	public IChannel getChannel() {
		return this.message.getChannel();
	}

	public IUser getAuthor() {
		return this.message.getAuthor();
	}

	public String getAuthorName() {
		return this.getAuthor().getName();
	}

	public CommandPermission getAuthorPermission() {
		if(this.getAuthor().equals(this.getClient().getApplicationOwner())) {
			return CommandPermission.OWNER;
		} else if(this.getGuild() == null) {
			return CommandPermission.ADMIN;
		} else if(this.getAuthor().getPermissionsForGuild(this.getGuild()).contains(Permissions.ADMINISTRATOR)) {
			return CommandPermission.ADMIN;
		} else {
			return CommandPermission.USER;
		}
	}

	public boolean hasArg() {
		return !this.arg.isEmpty();
	}
}
