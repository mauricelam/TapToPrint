package com.mauricelam.taptoprint.test;

import java.util.List;

import org.json.JSONException;

import android.test.ActivityInstrumentationTestCase2;

import com.mauricelam.taptoprint.CloudPrint;
import com.mauricelam.taptoprint.CloudPrint.AuthCompleteListener;
import com.mauricelam.taptoprint.CloudPrint.Job;
import com.mauricelam.taptoprint.CloudPrint.Printer;
import com.mauricelam.taptoprint.PrintActivity;

public class CloudPrintTest extends ActivityInstrumentationTestCase2<PrintActivity> {

	private static final String TEST_FILE_TITLE = "gcptest";
	private int loginState = 0;
	private CloudPrint cloudPrint;

	public CloudPrintTest() throws InterruptedException {
		super(PrintActivity.class);
	}

	public void test() throws JSONException {
		login();

		assertTrue("Authorized", cloudPrint.ready());
		assertEquals("Login callback called", 1, loginState);

		search();
		submit();
		submit2();
		jobs();
	}

	private void login() {
		if (cloudPrint == null) {
			cloudPrint = CloudPrint.instance();
			cloudPrint.login(getActivity(), new AuthCompleteListener() {
				@Override
				public void onAuthComplete(boolean success) {
					loginState = success ? 1 : -1;
					synchronized(cloudPrint) {
						cloudPrint.notifyAll();
					}
				}
			});
			if (loginState == 0) {
				synchronized (cloudPrint) {
					try {
						cloudPrint.wait(5000);
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	private void search() {
		assertTrue("Not authorized", cloudPrint.ready());
		// Look for Google Docs here simply because it should be availabe to all GCP users.
		boolean hasGoogleDocs = false;
		List<Printer> printers = cloudPrint.search();
		for (Printer printer : printers) {
			hasGoogleDocs = hasGoogleDocs || printer.name.contains("Google Docs");
		}
		assertTrue("Print to Google Docs option not found", hasGoogleDocs);
	}

	private void submit() {
		assertTrue("Not authorized", cloudPrint.ready());
		String url = "https://docs.google.com/open?id=14Pth6Fbom2DSMKZ5Gci2DglwykjBHXM06AA0bhDhRF8ZRRDZbl7hWkLhENCpY-9lWHd48xf6jJBc7KuV";
		boolean success = cloudPrint.submitURL(TEST_FILE_TITLE, url, "__google__docs") != null;
		assertTrue("Failure message returned from GCP", success);
		// Warning: This is going to clone itself on Google Drive
	}
	
	private void submit2() {
		assertTrue("Not authorized", cloudPrint.ready());
		String url = "https://web.engr.illinois.edu/~lam25/ttp/printfiles/temp.pdf";
		boolean success = cloudPrint.submitURL(TEST_FILE_TITLE, url, "__google__docs") != null;
		assertTrue("Failure message returned from GCP", success);
		// Warning: This is going to clone itself on Google Drive
	}
	
	private void jobs() {
		assertTrue("Not authorized", cloudPrint.ready());
		boolean hasTestJob = false;
		Job[] jobs = cloudPrint.jobs();
		for (Job job : jobs) {
			hasTestJob = hasTestJob || job.title.equals(TEST_FILE_TITLE);
		}
		assertTrue("Test job not found", hasTestJob);
	}

}
