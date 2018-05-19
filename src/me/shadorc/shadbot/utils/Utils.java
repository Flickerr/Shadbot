package me.shadorc.shadbot.utils;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.json.JSONArray;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import me.shadorc.shadbot.data.db.Database;
import me.shadorc.shadbot.exception.IllegalCmdArgumentException;
import me.shadorc.shadbot.utils.object.Emoji;

public class Utils {

	public static double getProcessCpuLoad() {
		double cpuLoad;
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
			AttributeList list = mbs.getAttributes(name, new String[] { "ProcessCpuLoad" });

			if(list.isEmpty()) {
				return Double.NaN;
			}

			Attribute att = (Attribute) list.get(0);
			Double value = (Double) att.getValue();

			if(value == -1.0) {
				return Double.NaN;
			}

			cpuLoad = value * 100d;
		} catch (InstanceNotFoundException | ReflectionException | MalformedObjectNameException err) {
			cpuLoad = Double.NaN;
		}

		return cpuLoad;
	}

	public static <T extends Enum<T>> T getValueOrNull(Class<T> enumClass, String value) {
		for(T enumeration : enumClass.getEnumConstants()) {
			if(enumeration.toString().equalsIgnoreCase(value)) {
				return enumeration;
			}
		}
		return null;
	}

	public static <T> List<T> toList(JSONArray array, Class<T> listClass) {
		if(array == null) {
			return null;
		}

		List<T> list = new ArrayList<>();
		for(int i = 0; i < array.length(); i++) {
			if(!listClass.isInstance(array.get(i))) {
				throw new IllegalArgumentException(String.format("Array's elements cannot be casted to %s.", listClass.getSimpleName()));
			}
			list.add(listClass.cast(array.get(i)));
		}
		return list;
	}

	public static <K, V> Map<K, V> sortByValue(Map<K, V> map, Comparator<? super Entry<K, V>> comparator) {
		return map.entrySet()
				.stream()
				.sorted(comparator)
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue,
						(value1, value2) -> value1,
						LinkedHashMap::new));
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		return Utils.sortByValue(map, Map.Entry.comparingByValue(Collections.reverseOrder()));
	}

	public static Integer checkAndGetBet(TextChannel channel, User user, String betStr, int maxValue) throws IllegalCmdArgumentException {
		Integer bet = CastUtils.asPositiveInt(betStr);
		if(bet == null) {
			throw new IllegalCmdArgumentException(String.format("`%s` is not a valid amount for coins.", betStr));
		}

		if(Database.getDBMember(channel.getGuildId(), user.getId()).getCoins() < bet) {
			BotUtils.sendMessage(TextUtils.notEnoughCoins(user), channel);
			return null;
		}

		if(bet > maxValue) {
			BotUtils.sendMessage(String.format(Emoji.BANK + " Sorry, you can't bet more than **%s**.",
					FormatUtils.formatCoins(maxValue)), channel);
			return null;
		}

		return bet;
	}

	public static boolean isInRange(float nbr, float min, float max) {
		return nbr >= min && nbr <= max;
	}

	public static ThreadFactory createDaemonThreadFactory(String threadName) {
		return new ThreadFactoryBuilder().setNameFormat(threadName).setDaemon(true).build();
	}

}
