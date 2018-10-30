package me.shadorc.shadbot.utils.embed;

public class Argument {

	private final String name;
	private final String desc;
	private final boolean isFacultative;

	public Argument(String name, String desc, boolean isFacultative) {
		this.name = name;
		this.desc = desc;
		this.isFacultative = isFacultative;
	}

	public String getName() {
		return this.name;
	}

	public String getDesc() {
		return this.desc;
	}

	public boolean isFacultative() {
		return this.isFacultative;
	}

}
