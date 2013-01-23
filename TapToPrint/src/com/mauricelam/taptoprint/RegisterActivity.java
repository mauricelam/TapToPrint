package com.mauricelam.taptoprint;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

/**
 * Activity to prompt the user to register with a key. It is assumed that "registerKey" is passed in
 * as an extra in the intent, containing the key that is displayed.
 * 
 * @author Maurice Lam
 * 
 */
public class RegisterActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		// get the register code
		String registerCode = getIntent().getStringExtra("registerKey");
		if (registerCode != null) {
			// display the register key
			TextView regKeyBox = (TextView) findViewById(R.id.register_key);
			regKeyBox.setText(registerCode);
			findViewById(R.id.register_key_hint).setVisibility(View.VISIBLE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_register, menu);
		return true;
	}

}
