package me.shadorc.shadbot.data;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;

public class APIKeys {

	private static final Properties KEYS_PROPERTIES = new Properties();
	private static final File API_KEYS_FILE = new File("api_keys.properties");

	public enum APIKey {
		DISCORD_TOKEN,
	}

	public static void init() throws MalformedURLException, IOException {
		try (FileReader reader = new FileReader(API_KEYS_FILE)) {
			KEYS_PROPERTIES.load(reader);
		}

		for(final APIKey key : APIKey.values()) {
			if(APIKeys.get(key) == null) {
				throw new ExceptionInInitializerError(String.format("%s not found.", key.toString()));
			}
		}
	}

	public static String get(APIKey key) {
		return KEYS_PROPERTIES.getProperty(key.toString());
	}

}
