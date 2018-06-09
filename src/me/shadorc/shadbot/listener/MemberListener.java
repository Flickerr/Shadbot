package me.shadorc.shadbot.listener;

import java.util.Optional;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.object.util.Snowflake;
import me.shadorc.shadbot.data.db.DBGuild;
import me.shadorc.shadbot.data.db.Database;
import me.shadorc.shadbot.utils.BotUtils;
import me.shadorc.shadbot.utils.ExceptionUtils;
import me.shadorc.shadbot.utils.embed.log.LogUtils;

public class MemberListener {

	public static void onMemberJoin(MemberJoinEvent event) {
		final Snowflake guildId = event.getGuildId();
		DBGuild dbGuild = Database.getDBGuild(guildId);

		MemberListener.sendAutoMsg(event.getClient(), dbGuild.getMessageChannelId(), dbGuild.getJoinMessage());

		dbGuild.getAutoRoles()
				.forEach(roleId -> event.getMember().addRole(roleId)
						.doOnError(ExceptionUtils::isForbidden,
								err -> LogUtils.infof("{Guild ID: %s} Shadbot was not allowed to edit role.", guildId))
						.doOnError(
								err -> LogUtils.error(event.getClient(), err, String.format("{Guild ID: %s} An error occured while editing a role.", guildId)))
						.subscribe());
	}

	public static void onMemberLeave(MemberLeaveEvent event) {
		DBGuild dbGuild = Database.getDBGuild(event.getGuildId());
		MemberListener.sendAutoMsg(event.getClient(), dbGuild.getMessageChannelId(), dbGuild.getLeaveMessage());
	}

	private static void sendAutoMsg(DiscordClient client, Optional<Snowflake> channelId, Optional<String> msg) {
		if(!channelId.isPresent() || !msg.isPresent()) {
			return;
		}

		BotUtils.sendMessage(msg.get(), client.getMessageChannelById(channelId.get()));
	}
}