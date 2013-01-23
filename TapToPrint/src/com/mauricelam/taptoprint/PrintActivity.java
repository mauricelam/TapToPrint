package com.mauricelam.taptoprint;

import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.mauricelam.taptoprint.CloudPrint.AuthCompleteListener;
import com.mauricelam.taptoprint.CloudPrint.Job;
import com.mauricelam.taptoprint.HttpConnection.HttpResultListener;

/**
 * The main activity in the workflow. (But not the one launched by launcher). Displays the current
 * state of user's print queue: the list of jobs in the queue if non-empty, or an error message if
 * empty.
 * 
 * @author Maurice Lam
 * 
 */
public class PrintActivity extends ListActivity implements HttpResultListener, AuthCompleteListener {

	private NfcAdapter nfcAdapter;
	private CloudPrint cloudPrint;
	private JobsAdapter adapter;
	private Handler handler = new Handler();
	private String lastNfcId = null;

	private boolean nfcEnabled = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_print);

		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

		nfcAdapter = NfcAdapter.getDefaultAdapter(this);

		if (!getIntent().hasExtra("printerid")) {
			// Show dialog when printer not specified. This is the unlikely case when the intent
			// somehow does not have printer id.
			new AlertDialog.Builder(this).setMessage("Printer not specified")
					.setNeutralButton("Close", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							PrintActivity.this.finish();
						}
					}).create().show();
		}

		// set title to be printer
		String printer = getIntent().getStringExtra("printername");
		if (printer == null)
			printer = "Unknown Printer";
		this.setTitle(printer);
		TextView printerBox = (TextView) this.findViewById(R.id.printer);
		printerBox.setText(printer);

		// create the cloud print instance
		cloudPrint = CloudPrint.instance();
		// singleton should make cloudprint already logged in, do it again just for safety
		cloudPrint.login(this, this);

		adapter = new JobsAdapter(this);
		getListView().setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (lastNfcId == null)
			setNfcEnabled(true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		handler.removeCallbacks(reset);
		setNfcEnabled(false);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		findNFC(intent);
	}

	@Override
	public void onAuthComplete(boolean success) {
		// try to find a tag after login is successful.
		if (success)
			findNFC(getIntent());
		else
			Toast.makeText(PrintActivity.this, "Unable to login", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onHttpResult(HttpConnection connection, String result) {
		if (getEndPointUrl("fetch.php").equals(connection.getUrl())) {
			handleFetchResult(result);
		}
	}

	/**
	 * Trys to find NFC from the given intent. Calls nfcDidConnect if a tag is found.
	 * 
	 * @param intent
	 */
	private void findNFC(Intent intent) {
		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if (tag != null) {
			nfcDidConnect(tag);
		}
	}

	/**
	 * Callback method to be called when the NFC successfully connected.
	 * 
	 * @param tag
	 */
	private void nfcDidConnect(Tag tag) {
		String id = getNfcId(tag);
		TextView tv = (TextView) this.findViewById(R.id.text);
		tv.setText(id);

		if (lastNfcId == null) {
			// proceed to fetching the print jobs if no current processing job
			showListMessage("Fetching print jobs...", R.drawable.big_progress);
			Utils.httpGet(getEndPointUrl("fetch.php")).param("nfcid", id)
					.connect(this, "{\"success\":false, \"message\": \"Connection error\"}");
			lastNfcId = id;
			setNfcEnabled(false);
		}
	}

	/**
	 * Called when the fetch request is completed (it is asynchronous). Gets the result, parses it
	 * into JSON and then use it to populate the print queue (listview on screen). Then prints the
	 * most recent job.
	 * 
	 * @param result
	 */
	private void handleFetchResult(String result) {
		try {

			JSONObject fetchResponse = new JSONObject(result);
			if (!fetchResponse.getBoolean("success")) {
				// prompt to register if there is a key supplied
				if (fetchResponse.has("key")) {
					registerWithKey(fetchResponse.getString("key"));
					return;
				}
				throw new RuntimeException(fetchResponse.getString("message"));
			}

			List<Job> jobs = Utils.toList(fetchResponse.getJSONArray("jobs"), Job.class);
			adapter.clear();
			adapter.addAll(jobs);

			if (jobs.isEmpty()) {
				throw new RuntimeException("No file to print");
			}

			jobs.get(0).status = Job.Status.IN_PROGRESS;
			new PrintTask().execute(jobs.get(0));

		} catch (Exception e) {
			e.printStackTrace();
			done();
			showListMessage(e.getMessage(), R.drawable.warning);
		}
	}

	/**
	 * Jumps to the register activity.
	 * 
	 * @param key
	 */
	private void registerWithKey(String key) {
		Intent intent = new Intent(this, RegisterActivity.class);
		intent.putExtra("registerKey", key);
		this.startActivity(intent);
	}

	/**
	 * Prints the documents in background.
	 * 
	 * @author Maurice Lam
	 * 
	 */
	private class PrintTask extends AsyncTask<Job, Void, String> {

		private Job incomingJob;

		@Override
		protected String doInBackground(Job... params) {
			incomingJob = params[0];
			String printerid = getIntent().getStringExtra("printerid");
			String outgoingJobId = cloudPrint.submitURL(incomingJob.title, incomingJob.fileUrl,
					printerid);

			if (outgoingJobId == null)
				return cloudPrint.getLastMessage();

			Job outgoingJob;
			for (int i = 0; i < 20; i++) {
				outgoingJob = cloudPrint.job(outgoingJobId);
				switch (outgoingJob.status) {
				case DONE:
					return null; // no error
				case ERROR:
					return outgoingJob.message;
				case IN_PROGRESS:
				case QUEUED:
					break; // continue polling
				}
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// Just let it use up one of the retries
				}
			}

			return "Connection timeout: The print job took too long";
		}

		@Override
		protected void onPostExecute(String errorMessage) {
			if (errorMessage != null)
				showListMessage(errorMessage, R.drawable.warning);
			else
				printSuccessful(incomingJob, lastNfcId);
		}

	}

	/**
	 * Called when we are sure that the print completed successfully. Calls back to the server
	 * telling it that we are done. Also changes the status of the job.
	 * 
	 * @param incomingJob
	 * @param nfcid
	 */
	private void printSuccessful(final Job incomingJob, String nfcid) {
		// Tell GCP that we are done printing this job
		Utils.httpGet(getEndPointUrl("control.php"))
				.param("jobid", incomingJob.id)
				.param("nfcid", nfcid)
				.param("status", "DONE")
				.connect(this, "Connection error");
		incomingJob.status = Job.Status.DONE;
		adapter.notifyDataSetChanged();
		done();
	}

	/**
	 * Done with this NFC card. Timeout and then wait for another card.
	 */
	private void done() {
		handler.postDelayed(reset, 5000);
	}

	/**
	 * Reset all states and wait for another card.
	 */
	private Runnable reset = new Runnable() {
		@Override
		public void run() {
			TextView nfcid = (TextView) findViewById(R.id.text);
			nfcid.setText("");
			showListMessage("Tap your NFC tag on the back", R.drawable.nfc);
			lastNfcId = null;
			try {
				setNfcEnabled(true);
			} catch (IllegalStateException e) {
				// Illegal state exception will be thrown when the we try to set foreground dispatch
				// while we are not foreground.
				e.printStackTrace();
			}
		}
	};

	/**
	 * Enable / Disable foreground dispatch for NFC.
	 * 
	 * @param enable
	 */
	private void setNfcEnabled(boolean enable) {
		if (nfcAdapter == null || enable == nfcEnabled) {
			return;
		}
		if (enable) {
			// Respond to all ACTION_TAG_DISCOVERED
			Intent nfcIntent = new Intent(this, getClass());
			nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent pendingNfcIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);
			nfcAdapter.enableForegroundDispatch(this, pendingNfcIntent, null, null);
			nfcEnabled = true;
			Log.d("NFC", "ENABLE");
		} else {
			nfcAdapter.disableForegroundDispatch(this);
			nfcEnabled = false;
			Log.d("NFC", "DISABLE");
		}
	}

	/**
	 * Shows a message to the list. Note that this forces the list to clear (since we need to show
	 * the empty message).
	 * 
	 * @param message
	 */
	private void showListMessage(String message, int icon) {
		TextView messageText = (TextView) findViewById(R.id.emptytext);
		messageText.setText(message);

		ImageView emptyImage = (ImageView) findViewById(R.id.emptyimage);
		emptyImage.setImageResource(icon);

		Drawable drawable = emptyImage.getDrawable();
		if (drawable instanceof AnimationDrawable) {
			AnimationDrawable animation = (AnimationDrawable) emptyImage.getDrawable();
			animation.start();
		}

		// remove everything from the list to show the message
		adapter.clear();
	}

	/**
	 * Generate a unique ID for the NFC tag. This hashes the technology together with the real ID of
	 * the tag to make sure it's unique.
	 * 
	 * @param tag
	 * @return
	 */
	private String getNfcId(Tag tag) {
		if (tag != null) {
			// Get the hex representation of the hash code of all the tech
			// To make sure IDs are unique even across different techs.
			String tech = Integer.toHexString(Arrays.toString(tag.getTechList()).hashCode());
			try {
				String id = Utils.toHexString(tag.getId());
				return "nfc-" + tech + "-" + id;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private String getEndPointUrl(String endpoint) {
		ToggleButton btn = (ToggleButton) this.findViewById(R.id.useGDrive);
		if (btn.isChecked()) {
			return "http://web.engr.illinois.edu/~junliu3/TTPServer/" + endpoint;
		} else {
			return "http://web.engr.illinois.edu/~lam25/ttp/" + endpoint;
		}
	}
}
