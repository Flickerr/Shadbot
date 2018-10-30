package me.shadorc.shadbot.core.command;

public enum CommandCategory {

	MUSIC("Music");

	private final String name;

	CommandCategory(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}
}
