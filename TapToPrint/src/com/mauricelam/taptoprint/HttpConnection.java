package com.mauricelam.taptoprint;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;

/**
 * An abstract class for HTTP Connections. This is a wrapper around HttpUrlConnection to allow for
 * method chaining and reduce other boilerplates.
 * 
 * @author Maurice Lam
 * 
 */
public abstract class HttpConnection {
	/** The host to connect to. */
	protected String url;
	/** A list of headers for this connection */
	protected List<BasicNameValuePair> headers = new ArrayList<BasicNameValuePair>();
	/** A list of parameters to submit to the host */
	protected List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
	/** The HttpURLConnection that is the underlying implementation of this class */
	protected HttpURLConnection connection;
	/** Key for identifying the connection */
	private String key;

	public HttpConnection(String url) {
		this.url = url;
	}

	/**
	 * Adds a header to the request. Subclasses must make use of the available values in the headers
	 * field.
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public HttpConnection header(String key, String value) {
		headers.add(new BasicNameValuePair(key, value));
		return this;
	}

	/**
	 * Add a parameter to this connection. Subclasses must make use of the available values in the
	 * params field.
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public HttpConnection param(String key, String value) {
		params.add(new BasicNameValuePair(key, value));
		return this;
	}

	/**
	 * @return The key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            The key to set
	 */
	public HttpConnection key(String key) {
		this.key = key;
		return this;
	}

	public String getUrl() {
		return this.url;
	}

	/**
	 * Opens a HttpURLConnection and returns it. You can perform any actions on the connection
	 * before calling connect. Note that you should not open a connection manually and instead you
	 * should use HttpConnection.connect().
	 * 
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public abstract HttpURLConnection openConnection() throws MalformedURLException,
			IOException;

	/**
	 * Connects to the specified host in the connection. Automatically open the connection if it is
	 * not already opened. This method suppresses all error messages and instead prints it to the
	 * log.
	 * 
	 * @param defValue
	 *            The default value to be returned if there are errors.
	 * @return
	 */
	public String connect(String defValue) {
		try {
			return connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return defValue;
	}

	/**
	 * Connects to the specified host in the connection. Automatically open the connection if it is
	 * not already opened.
	 * 
	 * @return
	 * @throws IOException
	 */
	public String connect() throws IOException {
		try {
			if (connection == null)
				connection = openConnection();
			if (connection.getResponseCode() != 200) {
				throw new IOException(connection.getResponseMessage());
			}
			String response = Utils.toString(connection.getInputStream());
			return response;
		} catch (IOException e) {
			throw e;
		} finally {
			connection.disconnect();
		}
	}

	public void connect(HttpResultListener listener, String defValue) {
		new AsyncConnection(listener, defValue).execute();
	}

	private class AsyncConnection extends AsyncTask<Void, Void, String> {

		private HttpResultListener listener;
		private String defValue;

		public AsyncConnection(HttpResultListener listener, String defValue) {
			this.listener = listener;
			this.defValue = defValue;
		}

		@Override
		protected String doInBackground(Void... params) {
			return connect(defValue);
		}

		@Override
		protected void onPostExecute(String result) {
			if (listener != null) {
				listener.onHttpResult(HttpConnection.this, result);
			}
		}

	}

	public interface HttpResultListener {
		public void onHttpResult(HttpConnection connection, String result);
	}

}