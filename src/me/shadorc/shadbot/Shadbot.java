package me.shadorc.shadbot;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import me.shadorc.shadbot.core.command.CommandManager;
import me.shadorc.shadbot.data.APIKeys;
import me.shadorc.shadbot.data.APIKeys.APIKey;
import me.shadorc.shadbot.listener.ReadyListener;
import me.shadorc.shadbot.listener.ShardListener;
import me.shadorc.shadbot.utils.LogUtils;
import me.shadorc.shadbot.utils.StringUtils;
import me.shadorc.shadbot.utils.executor.ShadbotCachedExecutor;
import me.shadorc.shadbot.utils.executor.ShadbotScheduledExecutor;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

public class Shadbot {

	public static final String VERSION;

	private static final ThreadPoolExecutor EVENT_THREAD_POOL = new ShadbotCachedExecutor("EventThreadPool-%d");
	private static final ScheduledThreadPoolExecutor DEFAULT_SCHEDULER = new ShadbotScheduledExecutor(3, "DefaultScheduler-%d");

	private static IDiscordClient client;

	static {
		final Properties properties = new Properties();
		try (InputStream inStream = Shadbot.class.getClassLoader().getResourceAsStream("project.properties")) {
			properties.load(inStream);
		} catch (final IOException err) {
			LogUtils.error(err, "An error occurred while getting version.");
		}
		VERSION = properties.getProperty("version");
	}

	public static void main(String[] args) {
		Locale.setDefault(new Locale("en", "US"));

		try {
			APIKeys.init();
		} catch (final IOException e) {
			LogUtils.error(e, "An error occurred while initializing API keys, exiting.");
			System.exit(1);
		}

		// Initialization
		if(!CommandManager.init()) {
			System.exit(1);
		}

		client = new ClientBuilder()
				.withToken(APIKeys.get(APIKey.DISCORD_TOKEN))
				.withRecommendedShardCount()
				.build();

		LogUtils.infof("Connecting to %s...", StringUtils.pluralOf(client.getShardCount(), "shard"));

		client.getDispatcher().registerListeners(Shadbot.getEventThreadPool(), new ReadyListener(), new ShardListener());
		client.login();
	}

	public static IDiscordClient getClient() {
		return client;
	}

	public static ThreadPoolExecutor getEventThreadPool() {
		return EVENT_THREAD_POOL;
	}

	public static ScheduledThreadPoolExecutor getScheduler() {
		return DEFAULT_SCHEDULER;
	}

}
