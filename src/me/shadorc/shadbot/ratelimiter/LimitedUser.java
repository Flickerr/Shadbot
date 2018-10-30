package me.shadorc.shadbot.ratelimiter;

import java.util.concurrent.ScheduledFuture;

public class LimitedUser {

	private int count;
	private ScheduledFuture<LimitedUser> deletionTask;

	public LimitedUser() {
		this.count = 0;
	}

	public int getCount() {
		return this.count;
	}

	public ScheduledFuture<LimitedUser> getDeletionTask() {
		return this.deletionTask;
	}

	public void increment() {
		this.count++;
	}

	public void setDeletionTask(ScheduledFuture<LimitedUser> deletionTask) {
		this.deletionTask = deletionTask;
	}

}
