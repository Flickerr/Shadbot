package me.shadorc.shadbot.shard;

import java.util.concurrent.ThreadPoolExecutor;

import sx.blah.discord.api.IShard;

public class ShadbotShard {

	private final IShard shard;
	private final int shardID;

	private ThreadPoolExecutor threadPool;

	public ShadbotShard(IShard shard) {
		this.shard = shard;
		this.shardID = shard.getInfo()[0];
		this.threadPool = ShardManager.createThreadPool(this);
	}

	public IShard getShard() {
		return this.shard;
	}

	public int getID() {
		return this.shardID;
	}

	public ThreadPoolExecutor getThreadPool() {
		return this.threadPool;
	}

}
