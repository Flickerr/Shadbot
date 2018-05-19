package me.shadorc.shadbot.listener;

import java.util.Optional;

import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.object.entity.Guild;
import me.shadorc.shadbot.utils.LogUtils;

public class GuildListener {

	public static void onGuildCreate(GuildCreateEvent event) {
		LogUtils.infof("Shadbot connected to a guild. (ID: %d | Users: %d)",
				event.getGuild().getId().asLong(),
				event.getGuild().getMemberCount().orElse(0));
	}

	public static void onGuildDelete(GuildDeleteEvent event) {
		Optional<Guild> guild = event.getGuild();
		LogUtils.infof("Shadbot disconnected from a guild. (ID: %d | Users: %d)",
				event.getGuildId().asLong(),
				guild.isPresent() ? guild.get().getMemberCount().orElse(0) : -1);
	}
}
