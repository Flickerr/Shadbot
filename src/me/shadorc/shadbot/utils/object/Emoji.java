package me.shadorc.shadbot.utils.object;

public enum Emoji {

	CHECK_MARK("white_check_mark"),
	WARNING("warning"),
	ACCESS_DENIED("no_entry_sign"),
	RED_CROSS("x"),

	GREY_EXCLAMATION("grey_exclamation"),
	RED_EXCLAMATION("exclamation"),
	RED_FLAG("triangular_flag_on_post"),
	WHITE_FLAG("flag_white"),

	INFO("information_source"),
	MAGNIFYING_GLASS("mag"),
	STOPWATCH("stopwatch"),

	HOURGLASS("hourglass_flowing_sand"),

	MUSICAL_NOTE("musical_note"),
	PLAY("arrow_forward"),
	PAUSE("pause_button"),
	REPEAT("repeat"),
	SOUND("sound"),
	MUTE("mute");

	private final String discordNotation;

	Emoji(String discordNotation) {
		this.discordNotation = discordNotation;
	}

	@Override
	public String toString() {
		return String.format(":%s:", this.discordNotation);
	}
}
