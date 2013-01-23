package com.mauricelam.taptoprint;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Library for Google Cloud Print API. In order to successfully use any method, you must first call
 * login to get an access token.
 * 
 * @author Maurice Lam
 * 
 */
public class CloudPrint {

	private static final String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/cloudprint";
	private static final String CLOUD_PRINT_URL = "https://www.google.com/cloudprint/";

	private static CloudPrint instance;

	private String token;
	private JSONObject lastResponse;

	public static CloudPrint instance() {
		if (instance == null)
			instance = new CloudPrint();
		return instance;
	}

	public interface AuthCompleteListener {
		public void onAuthComplete(boolean success);
	}

	/**
	 * Logins using the first available Google Account on this phone.
	 * 
	 * @param activity
	 *            The activity associated with the login.
	 * @param authComplete
	 *            Callback that is run when the we successfully get a token.
	 */
	public void login(Activity activity, final AuthCompleteListener authComplete) {
		if (this.ready()) {
			authComplete.onAuthComplete(true);
			return;
		}
		AccountManager accountManager = AccountManager.get(activity);
		Account[] accounts = accountManager.getAccountsByType("com.google");
		if (accounts.length <= 0) {
			if (authComplete != null)
				authComplete.onAuthComplete(false);
			return;
		}
		Account account = accounts[0];

		accountManager.getAuthToken(account, AUTH_TOKEN_TYPE, null, activity,
				new AccountManagerCallback<Bundle>() {
					public void run(AccountManagerFuture<Bundle> future) {
						boolean success;
						try {
							token = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
							success = true;
						} catch (Exception e) {
							success = false;
						}
						if (authComplete != null)
							authComplete.onAuthComplete(success);
					}
				}, null);
	}

	/**
	 * Whether we have a token and is ready to connect to the APIs.
	 * 
	 * @return
	 */
	public boolean ready() {
		return (token != null);
	}

	/**
	 * Returns a list of all available printers.
	 * 
	 * @return
	 */
	public List<Printer> search() {
		return search("");
	}

	/**
	 * Returns a list of printers that matches the query. <blockquote>The API looks for an
	 * approximate match between q and the name and tag fields (ie. [field] == %q%). Thus, setting q
	 * = "^recent" will return the list of recently used printers. Setting q = ^own or q = ^shared"
	 * will return the list of printers either owned by or shared with this user.</blockquote>
	 * 
	 * @param query
	 * @return
	 */
	public List<Printer> search(String query) {
		String response = Utils.httpGet(CLOUD_PRINT_URL + "search")
				.header("Authorization", "OAuth " + token)
				.param("q", query)
				.connect("{}");
		try {
			this.lastResponse = new JSONObject(response);
			JSONArray jsonPrinters = this.lastResponse.getJSONArray("printers");
			int count = jsonPrinters.length();
			List<Printer> printers = new ArrayList<Printer>(count);
			for (int i = 0; i < count; i++) {
				printers.add(Printer.fromJSON(jsonPrinters.getJSONObject(i)));
			}
			return printers;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new ArrayList<CloudPrint.Printer>(0);
	}

	/**
	 * Submits a print job on the specified URL to the printer.
	 * 
	 * @param url
	 * @param printer
	 * @return
	 */
	public String submitURL(String title, String url, Printer printer) {
		return submitURL(title, url, printer.id);
	}

	/**
	 * Submits a print job on the specified URL to the printer with <i>printerId</i>.
	 * 
	 * @param title
	 *            The title of the job, used by the GCP system.
	 * @param url
	 *            The URL of the job to print. Should be a URL to a PDF file, accessible from
	 *            anywhere (no special authorization or headers needed).
	 * @param printerID
	 *            The ID of the printer to print to.
	 * @return The job id of the newly created job, or null if the submission failed
	 */
	public String submitURL(String title, String url, String printerID) {
		if (title == null) {
			// if title is null, use the filename instead
			title = url.substring(url.lastIndexOf('/') + 1);
			int dotpos = title.lastIndexOf('.');
			title = (dotpos > -1) ? title.substring(0, dotpos) : title;
			// if filename is too long, use "nfcprinted"
			title = (title.length() <= 100) ? title : "nfcprinted";
		}
		String response = Utils.httpPost(CLOUD_PRINT_URL + "submit")
				.header("Authorization", "OAuth " + token)
				.param("printerid", printerID)
				.param("title", title)
				.param("capabilities", "")
				.param("content", url)
				.param("contentType", "url")
				.param("tag", "")
				.connect(null);
		Log.d("NFC", String.valueOf(response));
		String jobid = null;
		try {
			this.lastResponse = new JSONObject(response);
			if (!this.lastResponse.getBoolean("success")) {
				return null;
			}
			jobid = this.lastResponse.getJSONObject("job").getString("id");
		} catch (Exception e) {
			// we are returning false for success anyway.
		}
		return jobid;
	}

	/**
	 * Returns a list of all print jobs for the current user.
	 * 
	 * @return
	 */
	public Job[] jobs() {
		String response = Utils.httpGet(CLOUD_PRINT_URL + "jobs")
				.header("Authorization", "OAuth " + token)
				.connect(null);

		try {
			JSONArray jsonJobs = new JSONObject(response).getJSONArray("jobs");
			int jobsCount = jsonJobs.length();
			Job[] jobs = new Job[jobsCount];
			for (int i = 0; i < jobsCount; i++) {
				jobs[i] = Job.fromJSON(jsonJobs.getJSONObject(i));
			}
			return jobs;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new Job[0];
	}

	public Job job(String id) {
		Job[] jobs = this.jobs();
		for (Job job : jobs) {
			if (job.id.equals(id)) {
				return job;
			}
		}
		return null;
	}

	/**
	 * A print job containing its status and data.
	 * 
	 * @author Maurice Lam
	 * 
	 */
	public static class Job {
		public enum Status {
			QUEUED, IN_PROGRESS, DONE, ERROR
		}

		public String id;
		public String printerid;
		public String title;
		public String contentType;
		public String fileUrl;
		public String ticketUrl;
		public Date createTime;
		public Date updateTime;
		public Status status;
		public String errorCode;
		public String message;

		public static Job fromJSON(JSONObject jObj) {
			Job job = new Job();

			for (Field field : Job.class.getFields()) {
				try {
					if (String.class.equals(field.getType())) {
						field.set(job, jObj.getString(field.getName()));
					} else if (Date.class.equals(field.getType())) {
						field.set(job, new Date(jObj.getLong(field.getName())));
					} else if (Status.class.equals(field.getType())) {
						field.set(job, Status.valueOf(jObj.getString(field.getName())));
					}
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				} catch (JSONException e) {
					// Ignore all the exceptions. Not all fields are required
				}
			}
			return job;
		}

		public Job() {
			this.status = Status.QUEUED;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof Job) {
				return this.id.equals(((Job) o).id);
			} else {
				return false;
			}
		}

		@Override
		public String toString() {
			return this.title;
		}
	}

	/**
	 * Return the message included in the last JSON response.
	 * 
	 * @return
	 */
	public String getLastMessage() {
		try {
			return this.lastResponse.getString("message");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "Unknown JSON error";
	}

	/**
	 * Stores the data of a Google Cloud Print printer.
	 * 
	 * @author Maurice Lam
	 * 
	 */
	public static class Printer {
		public String id;
		public String name;
		public String type;
		public String displayName;
		public List<String> tags;

		public static Printer fromJSON(JSONObject jObj) throws JSONException {
			Printer printer = new Printer();
			printer.id = jObj.getString("id");
			printer.name = jObj.getString("name");
			printer.type = jObj.getString("type");
			printer.displayName = jObj.getString("displayName");
			try {
				printer.tags = Utils.toStringList(jObj.getJSONArray("tags"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return printer;
		}

		@Override
		public String toString() {
			return this.displayName;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof Printer) {
				return this.name.equals(((Printer) o).name);
			} else {
				return false;
			}
		}
	}

}
