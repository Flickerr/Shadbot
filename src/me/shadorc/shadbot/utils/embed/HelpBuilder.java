package me.shadorc.shadbot.utils.embed;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import me.shadorc.shadbot.core.command.AbstractCommand;
import me.shadorc.shadbot.utils.FormatUtils;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.obj.Embed.EmbedField;
import sx.blah.discord.util.EmbedBuilder;

public class HelpBuilder {

	private final String prefix;
	private final AbstractCommand cmd;
	private final List<Argument> args;
	private final List<EmbedField> fields;

	private String description;
	private String usage;
	private String example;

	public HelpBuilder(AbstractCommand cmd, String prefix) {
		this.prefix = prefix;
		this.cmd = cmd;
		this.args = new ArrayList<>();
		this.fields = new ArrayList<>();
	}

	public HelpBuilder setDescription(String description) {
		this.description = String.format("**%s**", description);
		return this;
	}

	public HelpBuilder setFullUsage(String usage) {
		this.usage = usage;
		return this;
	}

	public HelpBuilder setUsage(String usage) {
		return this.setFullUsage(String.format("%s%s %s", this.prefix, this.cmd.getName(), usage));
	}

	public HelpBuilder setExample(String example) {
		this.example = example;
		return this;
	}

	public HelpBuilder addArg(String name, String desc, boolean isFacultative) {
		this.args.add(new Argument(name, desc, isFacultative));
		return this;
	}

	public EmbedObject build() {
		final EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
				.setLenient(true)
				.withAuthorName(String.format("Help for %s command", this.cmd.getName()))
				.withDescription(this.description)
				.appendField("Usage", this.getUsage(), false)
				.appendField("Arguments", this.getArguments(), false)
				.appendField("Example", this.example, false);

		for(final EmbedField field : this.fields) {
			embed.appendField(field);
		}

		if(!this.cmd.getAlias().isEmpty()) {
			embed.withFooterText(String.format("Alias: %s", this.cmd.getAlias()));
		}

		return embed.build();
	}

	private String getUsage() {
		if(this.usage != null) {
			return String.format("`%s`", this.usage);
		}

		return String.format("`%s%s %s`",
				this.prefix, this.cmd.getName(),
				FormatUtils.format(this.args, arg -> String.format(arg.isFacultative() ? "[<%s>]" : "<%s>", arg.getName()), " "));
	}

	private String getArguments() {
		return this.args.stream()
				.filter(arg -> arg.getDesc() != null)
				.map(arg -> String.format("%n**%s** %s - %s", arg.getName(), arg.isFacultative() ? "[optional] " : "", arg.getDesc()))
				.collect(Collectors.joining());
	}
}
