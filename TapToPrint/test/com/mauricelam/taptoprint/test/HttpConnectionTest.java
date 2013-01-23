package com.mauricelam.taptoprint.test;

import com.mauricelam.taptoprint.Utils;

import android.test.AndroidTestCase;

public class HttpConnectionTest extends AndroidTestCase {

	public void testGet() {
		String response = Utils.httpGet("http://web.engr.illinois.edu/~lam25/utils/echo.php")
				.param("ping", "pingping")
				.connect("DEFAULT");
		assertEquals("pingping", response);
	}
	
	public void testPost() {
		String response = Utils.httpPost("http://web.engr.illinois.edu/~lam25/utils/echo.php")
				.param("ping", "pingping")
				.connect("DEFAULT");
		assertEquals("pingping", response);
	}
	
	public void testInvalidGet() {
		String response = Utils.httpGet("http://web.engr.illinois.edu/~lam25/utils/echo_nonexistent.php")
				.param("ping", "pingping")
				.connect("DEFAULT");
		assertEquals("DEFAULT", response);
	}
	
	public void testInvalidPost() {
		String response = Utils.httpPost("http://web.engr.illinois.edu/~lam25/utils/echo_nonexistent.php")
				.param("ping", "pingping")
				.connect("DEFAULT");
		assertEquals("DEFAULT", response);
	}

}
