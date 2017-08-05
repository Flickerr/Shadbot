package me.shadorc.discordbot.command.utils;

import java.awt.Color;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import me.shadorc.discordbot.Emoji;
import me.shadorc.discordbot.command.Command;
import me.shadorc.discordbot.command.Context;
import me.shadorc.discordbot.utils.BotUtils;
import sx.blah.discord.util.EmbedBuilder;

public class CalcCmd extends Command {

	public CalcCmd() {
		super(false, "calc");
	}

	@Override
	public void execute(Context context) {
		if(context.getArg() == null) {
			throw new IllegalArgumentException();
		}

		try {
			ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
			BotUtils.sendMessage(context.getArg() + " = " + engine.eval(context.getArg()), context.getChannel());
		} catch (ScriptException e) {
			BotUtils.sendMessage(Emoji.WARNING + " Invalid expression.", context.getChannel());
		}
	}

	@Override
	public void showHelp(Context context) {
		EmbedBuilder builder = new EmbedBuilder()
				.withAuthorName("Help for /" + context.getArg())
				.withAuthorIcon(context.getClient().getOurUser().getAvatarURL())
				.withColor(new Color(170, 196, 222))
				.appendDescription("**Calculate an expression.**")
				.appendField("Usage", "/calc <expression>", false);
		BotUtils.sendEmbed(builder.build(), context.getChannel());
	}

}