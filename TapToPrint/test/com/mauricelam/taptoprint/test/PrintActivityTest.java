package com.mauricelam.taptoprint.test;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.widget.ListView;
import android.widget.TextView;

import com.mauricelam.taptoprint.PrintActivity;
import com.mauricelam.taptoprint.R;

public class PrintActivityTest extends ActivityUnitTestCase<PrintActivity> {

	public PrintActivityTest() {
		super(PrintActivity.class);
	}
	
	public void testConstruction() {
		Intent intent = new Intent();
		intent.putExtra("printername", "someprinter");
		intent.putExtra("printerid", "some printer id");
		startActivity(intent, null, null);
		
		TextView printerBox = (TextView) getActivity().findViewById(R.id.printer);
		assertEquals("someprinter", printerBox.getText());
		
		ListView list = (ListView) getActivity().findViewById(android.R.id.list);
		assertNotNull(list);
		assertNotNull(list.getAdapter());
	}
	
	public void testNoPrinterName() {
		Intent intent = new Intent();
		intent.putExtra("printerid", "some printer id");
		startActivity(intent, null, null);
		
		TextView printerBox = (TextView) getActivity().findViewById(R.id.printer);
		assertEquals("Unknown Printer", printerBox.getText());
	}

}
