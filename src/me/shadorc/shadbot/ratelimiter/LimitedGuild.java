package me.shadorc.shadbot.ratelimiter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import sx.blah.discord.handle.obj.IUser;

public class LimitedGuild {

	private final ConcurrentHashMap<Long, LimitedUser> limitedUsersMap;

	public LimitedGuild() {
		this.limitedUsersMap = new ConcurrentHashMap<>();
	}

	public LimitedUser getUser(IUser user) {
		return this.limitedUsersMap.get(user.getLongID());
	}

	public void addUserIfAbsent(IUser user) {
		this.limitedUsersMap.putIfAbsent(user.getLongID(), new LimitedUser());
	}

	public void scheduledDeletion(ScheduledThreadPoolExecutor scheduledExecutor, IUser user, int cooldown) {
		ScheduledFuture<LimitedUser> deletionTask = this.limitedUsersMap.get(user.getLongID()).getDeletionTask();
		if(deletionTask != null) {
			deletionTask.cancel(false);
		}

		deletionTask = scheduledExecutor.schedule(() -> this.limitedUsersMap.remove(user.getLongID()), cooldown, TimeUnit.MILLISECONDS);
		this.limitedUsersMap.get(user.getLongID()).setDeletionTask(deletionTask);
	}

}
