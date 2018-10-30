package me.shadorc.shadbot.shard;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import me.shadorc.shadbot.utils.executor.ShadbotCachedExecutor;
import sx.blah.discord.api.IShard;
import sx.blah.discord.handle.obj.IGuild;

public class ShardManager {

	private static final Map<IShard, ShadbotShard> SHARDS_MAP = new HashMap<>();
	private static final ThreadPoolExecutor DEFAUT_THREAD_POOL = new ShadbotCachedExecutor("DefaultThreadPool-%d");

	public static ThreadPoolExecutor createThreadPool(ShadbotShard shard) {
		return new ShadbotCachedExecutor("ShadbotShard-" + shard.getID() + "-%d");
	}

	public static ShadbotShard getShadbotShard(IShard shard) {
		return SHARDS_MAP.get(shard);
	}

	/**
	 * @param guild - the guild in which the event happened (can be null)
	 * @param runnable - the runnable to execute
	 * @return true if the runnable could have been executed, false otherwise
	 */
	public static boolean execute(IGuild guild, Runnable runnable) {
		ThreadPoolExecutor threadPool;

		// Private message
		if(guild == null) {
			threadPool = DEFAUT_THREAD_POOL;
		} else {
			threadPool = SHARDS_MAP.get(guild.getShard()).getThreadPool();
		}

		if(threadPool.isShutdown()) {
			return false;
		}
		threadPool.execute(runnable);
		return true;
	}

	public static void addShardIfAbsent(IShard shard) {
		SHARDS_MAP.putIfAbsent(shard, new ShadbotShard(shard));
	}

}
