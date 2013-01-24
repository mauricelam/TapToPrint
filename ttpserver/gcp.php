<?php 

require_once 'googleapi/Google_Client.php';
require_once 'googleapi/contrib/Google_Oauth2Service.php';
require_once 'cloudprint.php';

/**
 * A wrapper class around the Google API client for convenient use of the GCP API. 
 */
class GCP {

	// The actual Google API client (responsible for OAuth)
	var $client;
	// The user information as stored in database
	var $user;
	// JSON string of both the access token and the refresh token
	var $tokens; 
	// The client for CloudPrint API (a.k.a. Google_CloudPrint)
	var $api;
	// The return value of the last response
	var $lastResponse;

	var $email;

	public static function fromNfcId($nfcid) {
		$user = self::getUser($nfcid);
		if ($user == null) return null;
		$gcp = new GCP();
		$gcp->user = $user;
		$gcp->authenticate();
		return $gcp;
	}

	public static function fromPrinterId($printerid) {
		$user = self::getUser(null, $printerid);
		if ($user == null) return null;
		$gcp = new GCP();
		$gcp->user = $user;
		$gcp->authenticate();
		return $gcp;
	}

	/**
	 * Sets up GCP wrapper by authenticating. This will redirect to a login page if the 
	 * user is not logged in. Or it may redirect to an authorization page. Either the NFC
	 * ID or the Printer ID should be provided if the user is already registered. 
	 * 
	 * @param String $nfcid     (optional) The NFC ID. 
	 * @param String $printerid (optional) The Printer ID. 
	 */
	public function __construct() {
		$this->client = new Google_Client();
		$this->client->setClientId($_SERVER['gcpoauthid']);
		$this->client->setClientSecret($_SERVER['gcpoauthsecret']);

		$this->api = new Google_CloudPrint($this->client);
		$this->userinfo = new Google_Oauth2Service($this->client);
	}

	/**
	 * Returns the user's email address. 
	 * @return String The user's email address. 
	 */
	public function getUserEmail() {
		// Change the base path because UserInfo uses googleapis.com instead of google.com
		global $apiConfig;
		$basePath = $apiConfig['basePath'];
		$apiConfig['basePath'] = 'https://www.googleapis.com';

		// This line is the actual work. 
		$userinfo = $this->userinfo->userinfo->get();

		// revert to original basepath
		$apiConfig['basePath'] = $basePath;

		return $userinfo['email'];
	}

	/**
	 * Authenticates the user for access permission. This may redirect to page to OAuth
	 * end points. 
	 */
	public function authenticate($token=null) {
		if ($token == null) $token = $this->user['token'];
		if ($token) {
			$this->client->setAccessToken($token);
		} else {
			$this->client->setRedirectUri('https://' . $_SERVER['HTTP_HOST'] . $_SERVER['PHP_SELF']);
			$this->client->setScopes(array('https://www.googleapis.com/auth/cloudprint', 'https://www.googleapis.com/auth/userinfo.email'));
			$this->client->setAccessType('offline');
			$this->client->authenticate();
		}
		$this->tokens = $this->client->getAccessToken();
	}

	/**
	 * Get the current access token. 
	 * 
	 * @return String The OAuth access token for accessing Google APIs. 
	 */
	public function getToken() {
		$tokens = json_decode($this->tokens, true);
		$this->client->refreshToken($tokens['refresh_token']);
		$tokens = json_decode($this->client->getAccessToken(), true);
		return $tokens['access_token'];
	}

	/**
	 * Gets the user from the database according to NFC ID or printer ID. 
	 * @param  String $nfcid     The NFC ID. 
	 * @param  String $printerid The Printer ID. 
	 * @return array An associative array
	 */
	private static function getUser($nfcid=null, $printerid=null) {
		$stmt = SQL::prepare('SELECT * FROM users WHERE nfcid=? or printerid=?');
		$stmt->execute(array($nfcid, $printerid));
		return $stmt->fetch();
	}

	public static function getUserByEmail($email) {
		$stmt = SQL::prepare('SELECT * FROM users WHERE email=?');
		$stmt->execute(array($email));
		return $stmt->fetch();
	}

	/**
	 * Override register so that it provides additional functionality of adding to database and 
	 * check for duplicates. 
	 *
	 * @param boolean $allowDuplicate Whether to allow registration of the same proxy. 
	 */
	public function register($nfcid, $printer, $proxy, $capabilities, $optParams=array(), $allowDuplicate=false) {
		$register = true;
		if (!$allowDuplicate) {
			$printers = $this->listPrinters('TapToPrint');
			$register = (count($printers['printers']) == 0);
		}
		
		if ($register) {
			$newprinter = $this->__call('register', array($printer, $proxy, $capabilities, $optParams, $allowDuplicate));

			$stmt = SQL::prepare('INSERT INTO users SET nfcid=?, token=?, printerid=?, email=?');
			$success = $stmt->execute(array($nfcid, $_POST['token'], $newprinter['printers'][0]['id'], $this->getUserEmail()));

			if ($success) {
				return $newprinter;
			} else {
				return null;
			}
		} else {
			trigger_error('The printer is already registered', E_USER_WARNING);
			return null;
		}
	}

	/**
	 * Delete a printer from GCP and the database. 
	 */
	public function delete($printerid, $optParams=array()) {
		$response = $this->__call('delete', array($printerid, $optParams));
		$stmt = SQL::prepare('DELETE FROM users WHERE printerid=?');
		if ($stmt->execute(array($printerid))) {
			return $response;
		} else {
			return null;
		}
	}

	/**
	 * Relay all other methods to the API client. 
	 */
	public function __call($name, $arguments) {
		return call_user_func_array(array($this->api->resource, $name), $arguments);
	}

}

?>