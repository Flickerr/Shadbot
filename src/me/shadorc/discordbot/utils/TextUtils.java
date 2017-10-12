package me.shadorc.discordbot.utils;

import me.shadorc.discordbot.data.Config;
import me.shadorc.discordbot.utils.command.Emoji;

public class TextUtils {

	public static final String PLAYLIST_LIMIT_REACHED =
			Emoji.WARNING + " You've reached the maximum number (" + Config.MAX_PLAYLIST_SIZE + ") of tracks in a playlist.";

	public static final String NO_PLAYING_MUSIC =
			Emoji.MUTE + " No currently playing music.";

}
