package com.mauricelam.taptoprint;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

/**
 * Handles HTTP GET connections. Inherits the ability to chain methods from HttpConnection.
 * 
 * @see HttpConnection
 * @author Maurice Lam
 * 
 */
public class HttpGet extends HttpConnection {
	public HttpGet(String url) {
		super(url);
	}

	@Override
	public HttpURLConnection openConnection() throws MalformedURLException, IOException {
		String fullUrl = url.endsWith("?") ? url : url + "?";
		fullUrl += URLEncodedUtils.format(params, "utf-8");
		this.connection = (HttpURLConnection) new URL(fullUrl).openConnection();
		for (BasicNameValuePair header : headers) {
			connection.setRequestProperty(header.getName(), header.getValue());
		}
		return connection;
	}

}