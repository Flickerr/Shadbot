package me.shadorc.shadbot.command.music;

import me.shadorc.shadbot.core.command.AbstractCommand;
import me.shadorc.shadbot.core.command.CommandCategory;
import me.shadorc.shadbot.core.command.Context;
import me.shadorc.shadbot.core.command.annotation.Command;
import me.shadorc.shadbot.core.command.annotation.RateLimited;
import me.shadorc.shadbot.exception.MissingArgumentException;
import me.shadorc.shadbot.music.GuildMusic;
import me.shadorc.shadbot.music.GuildMusicManager;
import me.shadorc.shadbot.utils.BotUtils;
import me.shadorc.shadbot.utils.FormatUtils;
import me.shadorc.shadbot.utils.TextUtils;
import me.shadorc.shadbot.utils.embed.HelpBuilder;
import me.shadorc.shadbot.utils.object.Emoji;
import sx.blah.discord.api.internal.json.objects.EmbedObject;

@RateLimited
@Command(category = CommandCategory.MUSIC, names = { "name", "current", "np" })
public class NameCmd extends AbstractCommand {

	@Override
	public void execute(Context context) throws MissingArgumentException {
		final GuildMusic guildMusic = GuildMusicManager.GUILD_MUSIC_MAP.get(context.getGuild().getLongID());

		if(guildMusic == null || guildMusic.getScheduler().isStopped()) {
			BotUtils.sendMessage(TextUtils.NO_PLAYING_MUSIC, context.getChannel());
			return;
		}

		BotUtils.sendMessage(String.format(Emoji.MUSICAL_NOTE + " Currently playing: **%s**",
				FormatUtils.formatTrackName(guildMusic.getScheduler().getAudioPlayer().getPlayingTrack().getInfo())), context.getChannel());
	}

	@Override
	public EmbedObject getHelp(String prefix) {
		return new HelpBuilder(this, prefix)
				.setDescription("Show current music name.")
				.build();
	}
}