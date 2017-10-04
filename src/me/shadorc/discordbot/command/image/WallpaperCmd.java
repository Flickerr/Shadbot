package me.shadorc.discordbot.command.image;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ivkos.wallhaven4j.Wallhaven;
import com.ivkos.wallhaven4j.models.misc.Ratio;
import com.ivkos.wallhaven4j.models.misc.Resolution;
import com.ivkos.wallhaven4j.models.misc.enums.Category;
import com.ivkos.wallhaven4j.models.misc.enums.Purity;
import com.ivkos.wallhaven4j.models.wallpaper.Wallpaper;
import com.ivkos.wallhaven4j.util.searchquery.SearchQueryBuilder;

import me.shadorc.discordbot.command.AbstractCommand;
import me.shadorc.discordbot.command.CommandCategory;
import me.shadorc.discordbot.command.Context;
import me.shadorc.discordbot.command.Role;
import me.shadorc.discordbot.data.Config;
import me.shadorc.discordbot.data.Config.APIKey;
import me.shadorc.discordbot.data.Setting;
import me.shadorc.discordbot.utils.BotUtils;
import me.shadorc.discordbot.utils.MathUtils;
import me.shadorc.discordbot.utils.StringUtils;
import me.shadorc.discordbot.utils.Utils;
import me.shadorc.discordbot.utils.command.Emoji;
import me.shadorc.discordbot.utils.command.MissingArgumentException;
import me.shadorc.discordbot.utils.command.RateLimiter;
import sx.blah.discord.util.EmbedBuilder;

public class WallpaperCmd extends AbstractCommand {

	private final static Wallhaven WALLHAVEN = new Wallhaven(Config.get(APIKey.WALLHAVEN_LOGIN), Config.get(APIKey.WALLHAVEN_PASSWORD));

	private final RateLimiter rateLimiter;

	public WallpaperCmd() {
		super(CommandCategory.IMAGE, Role.USER, "wallpaper", "wp");
		this.rateLimiter = new RateLimiter(RateLimiter.COMMON_COOLDOWN, ChronoUnit.SECONDS);
	}

	@Override
	public void execute(Context context) throws MissingArgumentException {
		if(rateLimiter.isSpamming(context)) {
			return;
		}

		SearchQueryBuilder queryBuilder = new SearchQueryBuilder();

		List<String> invalidArgs = new ArrayList<>();
		for(String pair : StringUtils.getSplittedArg(context.getArg())) {
			String[] splitPair = pair.split("=");

			if(splitPair.length != 2) {
				invalidArgs.add(pair);
				continue;
			}

			String name = pair.split("=")[0];
			String value = pair.split("=")[1];
			switch (name) {
				case "purity":
					if(!Arrays.stream(Purity.values()).anyMatch(purityValue -> purityValue.toString().equalsIgnoreCase(value))) {
						invalidArgs.add(pair);
						continue;
					}

					if(value.matches("nsfw|sketchy") && !context.getChannel().isNSFW()) {
						BotUtils.sendMessage(Emoji.GREY_EXCLAMATION + " This must be a NSFW-channel. If you're an admin, you can use "
								+ "`" + context.getPrefix() + "settings " + Setting.NSFW + " toggle`", context.getChannel());
						return;
					}

					queryBuilder.purity(Purity.valueOf(value.toUpperCase()));
					break;

				case "category":
					if(!Arrays.stream(Category.values()).anyMatch(category -> category.toString().equalsIgnoreCase(value))) {
						invalidArgs.add(pair);
						continue;
					}
					queryBuilder.categories(Category.valueOf(value.toUpperCase()));
					break;

				case "ratio":
					String[] ratioArray = value.split(value.contains("x") ? "x" : "\\*");
					if(ratioArray.length != 2 || !StringUtils.isPositiveInt(ratioArray[0]) || !StringUtils.isPositiveInt(ratioArray[1])) {
						invalidArgs.add(pair);
						continue;
					}
					queryBuilder.ratios(new Ratio(Integer.parseInt(ratioArray[0]), Integer.parseInt(ratioArray[1])));
					break;

				case "resolution":
					String[] resArray = value.split(value.contains("x") ? "x" : "\\*");
					if(resArray.length != 2 || !StringUtils.isPositiveInt(resArray[0]) || !StringUtils.isPositiveInt(resArray[1])) {
						invalidArgs.add(pair);
						continue;
					}
					queryBuilder.resolutions(new Resolution(Integer.parseInt(resArray[0]), Integer.parseInt(resArray[1])));
					break;

				case "keyword":
					queryBuilder.keywords(value);
					break;

				default:
					invalidArgs.add(pair);
					break;
			}
		}

		if(!invalidArgs.isEmpty()) {
			BotUtils.sendMessage(Emoji.GREY_EXCLAMATION + " " + StringUtils.formatList(invalidArgs, str -> "`" + str + "`", ", ")
					+ (invalidArgs.size() == 1 ? " is an invalid argument" : " are invalid arguments") + ". "
					+ "Use `" + context.getPrefix() + "help " + this.getNames()[0] + "` for more information.", context.getChannel());
			return;
		}

		// Set default purity to SFW
		if(!context.getArg().contains("purity=")) {
			queryBuilder.purity(Purity.SFW);
		}

		List<Wallpaper> wallpapers = WALLHAVEN.search(queryBuilder.pages(1).build());
		if(wallpapers.isEmpty()) {
			BotUtils.sendMessage(Emoji.MAGNIFYING_GLASS + " No result found.", context.getChannel());
			return;
		}

		Wallpaper wallpaper = wallpapers.get(MathUtils.rand(wallpapers.size()));

		EmbedBuilder embed = Utils.getDefaultEmbed()
				.withAuthorName("Wallpaper")
				.withUrl(wallpaper.getUrl())
				.withImage(wallpaper.getImageUrl())
				.appendField("Resolution", wallpaper.getResolution().toString(), false)
				.appendField("Tags", StringUtils.formatList(wallpaper.getTags(), tag -> "`" + tag.toString().replace("#", "") + "`", " "), false);

		BotUtils.sendMessage(embed.build(), context.getChannel());
	}

	@Override
	public void showHelp(Context context) {
		EmbedBuilder builder = Utils.getDefaultEmbed(this)
				.withDescription("**Search for a wallpaper.**")
				.appendField("Usage", "`" + context.getPrefix() + this.getNames()[0] + " [arguments]`", false)
				.appendField("Arguments", "**[OPTIONAL]** You can add them to refine your search, just put `<name>=<value>` after the command.", false)
				.appendField("Name - value", "**purity** - " + StringUtils.formatArray(Purity.values(), purity -> purity.toString().toLowerCase(), ", ")
						+ "\n**category** - " + StringUtils.formatArray(Category.values(), cat -> cat.toString().toLowerCase(), ", ")
						+ "\n**ratio** - Image ratio (Ex: 16x9)"
						+ "\n**resolution** - Image resolution (Ex: 1920x1080)"
						+ "\n**keyword** - Keyword (Ex: doom)", true)
				.appendField("Example", "Search a *SFW* wallpaper in category *Anime*, with a *16x9* ratio :"
						+ "\n`" + context.getPrefix() + this.getNames()[0] + " purity=sfw category=anime ratio=16x9`", false);
		BotUtils.sendMessage(builder.build(), context.getChannel());
	}

}