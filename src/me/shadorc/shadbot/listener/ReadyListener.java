package me.shadorc.shadbot.listener;

import me.shadorc.shadbot.Shadbot;
import me.shadorc.shadbot.utils.LogUtils;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;

public class ReadyListener {

	@EventSubscriber
	public void onReadyEvent(ReadyEvent event) {
		LogUtils.infof("Shadbot (Version: %s) is ready.", Shadbot.VERSION);

		event.getClient().getDispatcher().registerListeners(Shadbot.getEventThreadPool(),
				new MessageListener(),
				new UserVoiceChannelListener(),
				new VoiceChannelListener());
	}

}
