package me.shadorc.shadbot.utils;

import java.net.HttpURLConnection;
import java.net.URL;

public class NetUtils {

	public static boolean isValidURL(String url) {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.connect();
			return true;

		} catch (final Exception err) {
			return false;

		} finally {
			if(conn != null) {
				conn.disconnect();
			}
		}
	}

}
