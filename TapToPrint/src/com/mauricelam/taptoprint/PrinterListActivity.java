package com.mauricelam.taptoprint;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mauricelam.taptoprint.CloudPrint.AuthCompleteListener;
import com.mauricelam.taptoprint.CloudPrint.Printer;

/**
 * Displays a list of printer for the user to select. This is a step that should be done by a
 * printer administrator. Real users of the printer should only see the PrintActivity (or
 * RegisterActivity). When a printer in the list is selected, PrintActivity will start with the
 * selected printer, ready for use.
 * 
 * @author Maurice Lam
 * 
 */
public class PrinterListActivity extends Activity implements OnItemClickListener {

	private ListView list;
	private CloudPrint cloudPrint;
	private PrinterListAdapter arrayAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_printer_list);

		// Allow networking on main thread, as this is not an interaction-critical app
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (list == null) {
			list = (ListView) findViewById(R.id.printerlist);
		}

		if (arrayAdapter == null) {
			arrayAdapter = new PrinterListAdapter(this);
			list.setAdapter(arrayAdapter);
			list.setOnItemClickListener(this);
		}

		if (cloudPrint == null) {
			// create the cloud print instance
			cloudPrint = CloudPrint.instance();
			cloudPrint.login(this, new AuthCompleteListener() {
				@Override
				public void onAuthComplete(boolean success) {
					// try to find a tag after login is successful.
					if (success) {
						arrayAdapter.clear();
						arrayAdapter.addAll(fetchPrinterList());
					} else {
						Toast.makeText(PrinterListActivity.this, "Unable to login",
								Toast.LENGTH_SHORT)
								.show();
					}
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_printer_list, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Printer selectedPrinter = (Printer) list.getItemAtPosition(position);
		Intent printActivityIntent = new Intent(PrinterListActivity.this,
				PrintActivity.class);
		printActivityIntent.putExtra("printerid", selectedPrinter.id);
		printActivityIntent.putExtra("printername", selectedPrinter.displayName);
		startActivity(printActivityIntent);
	}

	/**
	 * Returns the list of printers from GCP except for Tap to Print printer.
	 */
	private List<Printer> fetchPrinterList() {
		Printer ttp = new Printer();
		ttp.name = "TapToPrint";
		return Utils.filter(cloudPrint.search(), ttp);
	}

	private class PrinterListAdapter extends ArrayAdapter<Printer> {

		public PrinterListAdapter(Context context) {
			super(context, R.layout.listitem_printer, R.id.printername);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);

			Printer printer = this.getItem(position);

			TextView tag = (TextView) view.findViewById(R.id.printertag);
			tag.setText(getTagline(printer));

			ImageView icon = (ImageView) view.findViewById(R.id.icon);
			icon.setImageResource(getIcon(printer.type));

			return view;
		}

		/**
		 * Returns the icon resource for the type.
		 * 
		 * @param type
		 * @return
		 */
		private int getIcon(String type) {
			if ("DRIVE".equals(type)) {
				return R.drawable.gdrive;
			} else if ("ANDROID_CHROME_SNAPSHOT".equals(type)) {
				return R.drawable.phone;
			} else if ("FEDEX".equals(type)) {
				return R.drawable.fedex;
			}
			return R.drawable.printer;
		}

		/**
		 * Returns the second line describing the printer. Mimics the cloud print dialog. 
		 * 
		 * @param printer
		 * @return
		 */
		private String getTagline(Printer printer) {
			if ("DRIVE".equals(printer.type)) {
				return "Save your document as a PDF in Google Drive";
			} else if ("FEDEX".equals(printer.type)) {
				return "Submit your document and retrieve it from any FedEx Office";
			}
			if (printer.tags.contains("^own")) {
				return "Owned by me";
			} else {
				return "Shared printer";
			}
		}

	}

}
