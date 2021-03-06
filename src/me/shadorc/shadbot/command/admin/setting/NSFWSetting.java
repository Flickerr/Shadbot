package me.shadorc.shadbot.command.admin.setting;

import me.shadorc.shadbot.command.admin.setting.core.AbstractSetting;
import me.shadorc.shadbot.command.admin.setting.core.Setting;
import me.shadorc.shadbot.command.admin.setting.core.SettingEnum;
import me.shadorc.shadbot.core.command.Context;
import me.shadorc.shadbot.exception.IllegalCmdArgumentException;
import me.shadorc.shadbot.exception.MissingArgumentException;
import me.shadorc.shadbot.utils.BotUtils;
import me.shadorc.shadbot.utils.FormatUtils;
import me.shadorc.shadbot.utils.LogUtils;
import me.shadorc.shadbot.utils.TextUtils;
import me.shadorc.shadbot.utils.Utils;
import me.shadorc.shadbot.utils.embed.EmbedUtils;
import me.shadorc.shadbot.utils.object.Emoji;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;

@Setting(description = "Manage current channel's NSFW state.", setting = SettingEnum.NSFW)
public class NSFWSetting extends AbstractSetting {

	private enum Action {
		TOGGLE, ENABLE, DISABLE;
	}

	@Override
	public void execute(Context context, String arg) throws MissingArgumentException, IllegalCmdArgumentException {
		if(!BotUtils.hasPermissions(context.getChannel(), Permissions.MANAGE_CHANNELS)) {
			BotUtils.sendMessage(TextUtils.missingPerm(Permissions.MANAGE_CHANNEL), context.getChannel());
			LogUtils.infof("{Guild ID: %d} Shadbot wasn't allowed to manage channel.", context.getGuild().getLongID());
			return;
		}

		if(arg == null) {
			throw new MissingArgumentException();
		}

		Action action = Utils.getValueOrNull(Action.class, arg);
		if(action == null) {
			throw new IllegalCmdArgumentException(String.format("`%s` is not a valid action. %s",
					arg, FormatUtils.formatOptions(Action.class)));
		}

		boolean isNSFW = false;
		switch (action) {
			case TOGGLE:
				isNSFW = !context.getChannel().isNSFW();
				break;
			case ENABLE:
				isNSFW = true;
				break;
			case DISABLE:
				isNSFW = false;
				break;
		}

		context.getChannel().changeNSFW(isNSFW);
		BotUtils.sendMessage(String.format(Emoji.CHECK_MARK + " This channel is now %sSFW.", isNSFW ? "N" : ""), context.getChannel());
	}

	@Override
	public EmbedBuilder getHelp(String prefix) {
		return EmbedUtils.getDefaultEmbed()
				.appendField("Usage", String.format("`%s%s <action>`", prefix, this.getCmdName()), false)
				.appendField("Argument", String.format("**action** - %s",
						FormatUtils.format(Action.values(), action -> action.toString().toLowerCase(), "/")), false)
				.appendField("Example", String.format("`%s%s toggle`", prefix, this.getCmdName()), false);
	}

}
