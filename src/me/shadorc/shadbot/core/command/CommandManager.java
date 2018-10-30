package me.shadorc.shadbot.core.command;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import me.shadorc.shadbot.Shadbot;
import me.shadorc.shadbot.core.command.annotation.Command;
import me.shadorc.shadbot.exception.IllegalCmdArgumentException;
import me.shadorc.shadbot.exception.MissingArgumentException;
import me.shadorc.shadbot.utils.BotUtils;
import me.shadorc.shadbot.utils.LogUtils;
import me.shadorc.shadbot.utils.StringUtils;
import me.shadorc.shadbot.utils.TextUtils;
import me.shadorc.shadbot.utils.object.Emoji;
import sx.blah.discord.util.MessageBuilder;

public class CommandManager {

	private static final Map<String, AbstractCommand> COMMANDS_MAP = new HashMap<>();

	public static boolean init() {
		LogUtils.infof("Initializing commands...");

		final Reflections reflections = new Reflections(Shadbot.class.getPackage().getName(), new SubTypesScanner(), new TypeAnnotationsScanner());
		for(final Class<?> cmdClass : reflections.getTypesAnnotatedWith(Command.class)) {
			if(!AbstractCommand.class.isAssignableFrom(cmdClass)) {
				LogUtils.error(String.format("An error occurred while generating command, %s cannot be cast to AbstractCommand.",
						cmdClass.getSimpleName()));
				continue;
			}

			try {
				final AbstractCommand cmd = (AbstractCommand) cmdClass.getConstructor().newInstance();

				final List<String> names = cmd.getNames();
				if(!cmd.getAlias().isEmpty()) {
					names.add(cmd.getAlias());
				}

				for(final String name : names) {
					if(COMMANDS_MAP.putIfAbsent(name, cmd) != null) {
						LogUtils.error(String.format("Command name collision between %s and %s",
								cmdClass.getSimpleName(), COMMANDS_MAP.get(name).getClass().getSimpleName()));
						continue;
					}
				}
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException err) {
				LogUtils.error(err, String.format("An error occurred while initializing command %s.",
						cmdClass.getDeclaringClass().getSimpleName()));
				return false;
			}
		}

		LogUtils.infof("%s initialized.", StringUtils.pluralOf((int) COMMANDS_MAP.values().stream().distinct().count(), "command"));
		return true;
	}

	public static void execute(Context context) {
		final AbstractCommand cmd = COMMANDS_MAP.get(context.getCommandName());
		if(cmd == null) {
			return;
		}

		final CommandPermission authorPermission = context.getAuthorPermission();
		if(cmd.getPermission().isSuperior(authorPermission)) {
			BotUtils.sendMessage(Emoji.ACCESS_DENIED + " You do not have the permission to execute this command.", context.getChannel());
			return;
		}

		if(cmd.getRateLimiter() != null && cmd.getRateLimiter().isLimited(context.getChannel(), context.getAuthor())) {
			return;
		}

		try {
			cmd.execute(context);
		} catch (final IllegalCmdArgumentException err) {
			BotUtils.sendMessage(Emoji.GREY_EXCLAMATION + err.getMessage(), context.getChannel());
		} catch (final MissingArgumentException err) {
			BotUtils.sendMessage(new MessageBuilder(context.getClient())
					.withChannel(context.getChannel())
					.withContent(TextUtils.MISSING_ARG)
					.withEmbed(cmd.getHelp(context.getPrefix())));
		}
	}

	public static AbstractCommand getCommand(String name) {
		return COMMANDS_MAP.get(name);
	}
}
