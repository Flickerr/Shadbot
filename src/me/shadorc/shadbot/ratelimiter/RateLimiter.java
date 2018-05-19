package me.shadorc.shadbot.ratelimiter;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import me.shadorc.shadbot.utils.BotUtils;
import me.shadorc.shadbot.utils.StringUtils;
import me.shadorc.shadbot.utils.TextUtils;
import me.shadorc.shadbot.utils.executor.ScheduledWrappedExecutor;
import me.shadorc.shadbot.utils.object.Emoji;

public class RateLimiter {

	public static final int DEFAULT_COOLDOWN = 5;
	public static final int GAME_COOLDOWN = 5;

	private final ScheduledThreadPoolExecutor scheduledExecutor;
	private final ConcurrentHashMap<Snowflake, LimitedGuild> guildsLimitedMap;
	private final int max;
	private final int cooldown;

	public RateLimiter(int max, int cooldown, ChronoUnit unit) {
		this.scheduledExecutor = new ScheduledWrappedExecutor("RateLimiter-%d");
		this.guildsLimitedMap = new ConcurrentHashMap<>();
		this.max = max;
		this.cooldown = (int) Duration.of(cooldown, unit).toMillis();
	}

	public boolean isLimited(TextChannel channel, User user) {
		guildsLimitedMap.putIfAbsent(channel.getGuildId(), new LimitedGuild());

		LimitedGuild limitedGuild = guildsLimitedMap.get(channel.getGuildId());
		limitedGuild.addUserIfAbsent(user);

		LimitedUser limitedUser = limitedGuild.getUser(user);
		limitedUser.increment();

		// The user has not exceeded the limit yet, he is not limited
		if(limitedUser.getCount() <= max) {
			limitedGuild.scheduledDeletion(scheduledExecutor, user, cooldown);
			return false;
		}

		// The user has exceeded the limit, he's warned and limited
		if(limitedUser.getCount() == max + 1) {
			limitedGuild.scheduledDeletion(scheduledExecutor, user, cooldown);
			this.warn(channel, user);
			return true;
		}

		// The user has already exceeded the limit, he will be unlimited when the deletion task will be done
		return true;
	}

	private void warn(TextChannel channel, User user) {
		BotUtils.sendMessage(String.format(Emoji.STOPWATCH + " (**%s**) %s You can use this command %s every *%s*.",
				user.getUsername(),
				TextUtils.getSpamMessage(),
				StringUtils.pluralOf(max, "time"),
				DurationFormatUtils.formatDurationWords(cooldown, true, true)), channel);
	}

}
