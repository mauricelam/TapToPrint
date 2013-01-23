package com.mauricelam.taptoprint.test;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.widget.TextView;

import com.mauricelam.taptoprint.R;
import com.mauricelam.taptoprint.RegisterActivity;

public class RegisterTest extends ActivityUnitTestCase<RegisterActivity> {

	public RegisterTest() {
		super(RegisterActivity.class);
	}

	public void testStartActivity() {
		Intent intent = new Intent();
		intent.putExtra("registerKey", "This is a key");
		startActivity(intent, null, null);
		
		TextView keybox = (TextView) getActivity().findViewById(R.id.register_key);
		assertEquals("This is a key", keybox.getText());
	}
	
	public void testNoKey() {
		Intent intent = new Intent();
		startActivity(intent, null, null);
		
		TextView keybox = (TextView) getActivity().findViewById(R.id.register_key);
		assertEquals("--", keybox.getText());
	}

}
