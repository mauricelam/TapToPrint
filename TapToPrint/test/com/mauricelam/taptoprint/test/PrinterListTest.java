package com.mauricelam.taptoprint.test;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.widget.ListView;

import com.mauricelam.taptoprint.PrinterListActivity;
import com.mauricelam.taptoprint.R;

public class PrinterListTest extends ActivityUnitTestCase<PrinterListActivity> {

	public PrinterListTest() {
		super(PrinterListActivity.class);
	}

	public void testConstruction() throws Exception {
		Intent intent = new Intent();
		startActivity(intent, null, null);

		getInstrumentation().callActivityOnResume(getActivity());

		ListView list = (ListView) getActivity().findViewById(R.id.printerlist);
		assertNotNull(list);
		assertNotNull(list.getAdapter());
	}

}
