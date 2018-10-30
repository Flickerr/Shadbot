package me.shadorc.shadbot.ratelimiter;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.commons.lang3.time.DurationFormatUtils;

import me.shadorc.shadbot.utils.BotUtils;
import me.shadorc.shadbot.utils.StringUtils;
import me.shadorc.shadbot.utils.TextUtils;
import me.shadorc.shadbot.utils.executor.ShadbotScheduledExecutor;
import me.shadorc.shadbot.utils.object.Emoji;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public class RateLimiter {

	public static final int DEFAULT_COOLDOWN = 5;

	private final ScheduledThreadPoolExecutor scheduledExecutor;
	private final ConcurrentHashMap<Long, LimitedGuild> guildsLimitedMap;
	private final int max;
	private final int cooldown;

	public RateLimiter(int max, int cooldown, ChronoUnit unit) {
		this.scheduledExecutor = new ShadbotScheduledExecutor("RateLimiter-%d");
		this.guildsLimitedMap = new ConcurrentHashMap<>();
		this.max = max;
		this.cooldown = (int) Duration.of(cooldown, unit).toMillis();
	}

	public boolean isLimited(IChannel channel, IUser user) {
		this.guildsLimitedMap.putIfAbsent(channel.getGuild().getLongID(), new LimitedGuild());

		final LimitedGuild limitedGuild = this.guildsLimitedMap.get(channel.getGuild().getLongID());
		limitedGuild.addUserIfAbsent(user);

		final LimitedUser limitedUser = limitedGuild.getUser(user);
		limitedUser.increment();

		// The user has not exceeded the limit yet, he is not limited
		if(limitedUser.getCount() <= this.max) {
			limitedGuild.scheduledDeletion(this.scheduledExecutor, user, this.cooldown);
			return false;
		}

		// The user has exceeded the limit, he's warned and limited
		if(limitedUser.getCount() == this.max + 1) {
			limitedGuild.scheduledDeletion(this.scheduledExecutor, user, this.cooldown);
			this.warn(channel, user);
			return true;
		}

		// The user has already exceeded the limit, he will be unlimited when the deletion task will be done
		return true;
	}

	private void warn(IChannel channel, IUser user) {
		BotUtils.sendMessage(String.format(Emoji.STOPWATCH + " (**%s**) %s You can use this command %s every *%s*.",
				user.getName(),
				TextUtils.getSpamMessage(),
				StringUtils.pluralOf(this.max, "time"),
				DurationFormatUtils.formatDurationWords(this.cooldown, true, true)), channel);
	}

}
