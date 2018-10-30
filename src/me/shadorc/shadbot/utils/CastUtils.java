package me.shadorc.shadbot.utils;

public class CastUtils {

	public static Integer asIntBetween(String str, int min, int max) {
		try {
			final Integer nbr = Integer.parseInt(str);
			if(!Utils.isInRange(nbr, min, max)) {
				return null;
			}
			return nbr;
		} catch (final NumberFormatException err) {
			return null;
		}
	}

	public static Integer asPositiveInt(String str) {
		try {
			final Integer nbr = Integer.parseInt(str);
			return nbr > 0 ? nbr : null;
		} catch (final NumberFormatException err) {
			return null;
		}
	}

	public static Long asPositiveLong(String str) {
		try {
			final Long nbr = Long.parseLong(str);
			return nbr > 0 ? nbr : null;
		} catch (final NumberFormatException err) {
			return null;
		}
	}

}
