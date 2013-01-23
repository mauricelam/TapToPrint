package com.mauricelam.taptoprint;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.message.BasicNameValuePair;

/**
 * Handles HTTP POST connections. Inherits the ability to chain method calls from HttpConnection.
 * 
 * @author Maurice Lam
 * 
 */
public class HttpPost extends HttpConnection {
	public HttpPost(String url) {
		super(url);
	}

	@Override
	public HttpURLConnection openConnection() throws MalformedURLException, IOException {
		this.connection = (HttpURLConnection) new URL(url).openConnection();
		for (BasicNameValuePair header : headers) {
			connection.setRequestProperty(header.getName(), header.getValue());
		}
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.connect();
		OutputStreamWriter sender = new OutputStreamWriter(connection.getOutputStream());
		for (BasicNameValuePair param : this.params) {
			sender.write(param.getName() + "=" + param.getValue() + "&");
		}
		sender.flush();
		return connection;
	}
}