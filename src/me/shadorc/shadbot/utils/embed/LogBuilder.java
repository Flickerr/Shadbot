package me.shadorc.shadbot.utils.embed;

import java.awt.Color;

import me.shadorc.shadbot.Shadbot;
import me.shadorc.shadbot.utils.StringUtils;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

public class LogBuilder {

	private final LogType type;
	private final String message;
	private final Throwable err;
	private final String input;

	public LogBuilder(LogType type, String message, Throwable err, String input) {
		this.type = type;
		this.message = message;
		this.err = err;
		this.input = input;
	}

	public LogBuilder(LogType type, String message, Throwable err) {
		this(type, message, err, null);
	}

	public LogBuilder(LogType type, String message) {
		this(type, message, null, null);
	}

	public EmbedObject build() {
		final EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
				.setLenient(true)
				.withAuthorName(String.format("%s (Version: %s)", StringUtils.capitalize(this.type.toString()), Shadbot.VERSION))
				.withDescription(this.message);

		switch (this.type) {
			case ERROR:
				embed.withColor(Color.RED);
				break;
			case WARN:
				embed.withColor(Color.ORANGE);
				break;
			case INFO:
				embed.withColor(Color.GREEN);
				break;
		}

		if(this.err != null) {
			embed.appendField("Error type", this.err.getClass().getSimpleName(), false);
			embed.appendField("Error message", this.err.getMessage(), false);
		}

		if(this.input != null) {
			embed.appendField("Input", this.input, false);
		}

		return embed.build();
	}
}
