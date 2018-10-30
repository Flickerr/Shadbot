package me.shadorc.shadbot.utils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringUtils {

	public static List<String> split(String str, int limit, String delimiter) {
		return Arrays.stream(str.split(delimiter, limit))
				.map(word -> word.trim())
				.filter(word -> !word.isEmpty())
				.collect(Collectors.toList());
	}

	public static List<String> split(String str, int limit) {
		return StringUtils.split(str, limit, " ");
	}

	public static String capitalize(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
	}

	public static String pluralOf(long count, String str) {
		if(count > 1) {
			return String.format("%d %ss", count, str);
		}
		return String.format("%d %s", count, str);
	}

	public static String remove(String text, String... toRemove) {
		return text.replaceAll(Arrays.stream(toRemove)
				.filter(str -> !str.isEmpty())
				.map(Pattern::quote)
				.collect(Collectors.joining("|")), "");
	}

}
