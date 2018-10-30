package me.shadorc.shadbot.utils;

import java.util.concurrent.ThreadFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class Utils {

	public static <T extends Enum<T>> T getValueOrNull(Class<T> enumClass, String value) {
		for(final T enumeration : enumClass.getEnumConstants()) {
			if(enumeration.toString().equalsIgnoreCase(value)) {
				return enumeration;
			}
		}
		return null;
	}

	public static boolean isInRange(float nbr, float min, float max) {
		return nbr >= min && nbr <= max;
	}

	public static ThreadFactory createDaemonThreadFactory(String threadName) {
		return new ThreadFactoryBuilder().setNameFormat(threadName).setDaemon(true).build();
	}

}
