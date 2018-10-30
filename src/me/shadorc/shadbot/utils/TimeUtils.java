package me.shadorc.shadbot.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {

	public static long parseTime(String text) {
		final Pattern pattern = Pattern.compile("[^a-z]*[a-z]{1}");
		final Matcher matcher = pattern.matcher(text.toLowerCase());

		final List<String> list = new ArrayList<>();
		while(matcher.find()) {
			list.add(matcher.group());
		}

		if(list.isEmpty() || list.stream().mapToInt(String::length).sum() != text.length()) {
			throw new IllegalArgumentException("Unit is missing");
		}

		long totalMs = 0;

		for(final String str : list) {
			final String unit = str.replaceAll("[0-9]", "");
			try {
				final int time = Integer.parseInt(str.replaceAll("[a-zA-Z]", ""));
				switch (unit) {
					case "s":
						totalMs += TimeUnit.SECONDS.toSeconds(time);
						break;
					case "m":
						totalMs += TimeUnit.MINUTES.toSeconds(time);
						break;
					case "h":
						totalMs += TimeUnit.HOURS.toSeconds(time);
						break;
					default:
						throw new IllegalArgumentException("Unknown unit: " + unit);
				}
			} catch (final NumberFormatException err) {
				throw new IllegalArgumentException(String.format("Missing number for unit \"%s\"", unit));
			}
		}

		return totalMs;
	}

}
