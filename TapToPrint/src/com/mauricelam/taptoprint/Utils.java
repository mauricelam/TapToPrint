package com.mauricelam.taptoprint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class Utils {

	/**
	 * A boilerplate click listener that simply dismisses the dialog when clicked.
	 */
	public static final OnClickListener dismissDialog = new OnClickListener() {
		@Override
		public void onClick(
				DialogInterface dialog,
				int which) {
			dialog.dismiss();
		}
	};

	/**
	 * Turns a JSONArray into a list of a specified class. Assumes that the class specified have
	 * static method fromJSON() implemented. Otherwise NoSuchMethodException will be thrown.
	 * 
	 * @param array
	 * @param clss
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> toList(JSONArray array, Class<T> clss) throws Exception {
		List<T> list = new ArrayList<T>(array.length());
		Method fromJSON = clss.getDeclaredMethod("fromJSON", JSONObject.class);
		for (int i = 0; i < array.length(); i++) {
			list.add((T) fromJSON.invoke(clss, new Object[] { array.getJSONObject(i) }));
		}
		return list;
	}

	/**
	 * Turns a JSONArray into a string list.
	 * 
	 * @param array
	 * @return
	 * @throws JSONException
	 */
	public static List<String> toStringList(JSONArray array) throws JSONException {
		List<String> list = new ArrayList<String>(array.length());
		for (int i = 0; i < array.length(); i++) {
			list.add(array.getString(i));
		}
		return list;
	}

	/**
	 * Filters an array, removing all the entries that is equal to "filter".
	 * 
	 * @param list
	 * @param filter
	 * @return
	 */
	public static <T> List<T> filter(List<T> list, T filter) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals(filter)) {
				list.remove(i);
				break;
			}
		}
		return list;
	}

	/**
	 * Turns an input stream into a string.
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public static String toString(InputStream input) throws IOException {
		// read it with BufferedReader
		BufferedReader br = new BufferedReader(new InputStreamReader(input));
		StringBuilder sb = new StringBuilder();

		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();
		return sb.toString();
	}

	/**
	 * Initiates an HTTP GET request. See {@link HttpGet}
	 * 
	 * @param url
	 * @return
	 */
	public static HttpConnection httpGet(String url) {
		return new HttpGet(url);
	}

	/**
	 * Initiates an HTTP POST request. See {@link HttpPost}
	 * 
	 * @param url
	 * @return
	 */
	public static HttpConnection httpPost(String url) {
		return new HttpPost(url);
	}

	/**
	 * Turns a byte array into a string of its hexadecimal value.
	 * http://www.rgagnon.com/javadetails/java-0596.html
	 * 
	 * @param b
	 *            The byte array.
	 * @return
	 * @throws Exception
	 */
	public static String toHexString(byte[] b) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < b.length; i++) {
			String hexByte = Integer.toHexString((b[i] & 0xff) + 0x100).substring(1);
			result.append(hexByte);
		}
		return result.toString();
	}

}
