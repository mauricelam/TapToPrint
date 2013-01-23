<?php
require_once 'utils.php';
require_once 'gcp.php';
require_once 'registermodel.php';

$message = '';

$gcp = new GCP();
while (true) {
	try {
		$gcp->authenticate($_POST['token']);
		break;
	} catch (Google_AuthException $e) {
		// The code is expired. Unset it and get a new one. 
		unset($_GET['code']);
	}
}

if (isset($_POST['registerkey'])) {
	// We now have everything we need
	// Register on CloudPrint and save the necessary keys on the database
	$nfcid = RegisterModel::getNfcIdFromKey($_REQUEST['registerkey']);
	if (!is_null($nfcid)) {
		$ppd = file_get_contents('ttp.ppd');
		$newprinter = $gcp->register($nfcid, 'TapToPrint', 'TapToPrint', $ppd);
		$message = (is_null($newprinter)) ? 'Error adding entry to database' : 'Registration successful';
	} else {
		$message = 'Your registration key is invalid or expired. ';
	}
	
}


/* ---------- Below are HTML for showing the registration box ---------- */
?>

<!DOCTYPE HTML>
<html lang="en-US">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
	<link rel="stylesheet" type="text/css" href="style.css" />
	<link rel="stylesheet" type="text/css" href="bootstrap/css/bootstrap.min.css" />
	<title>Register printer</title>
</head>
<body>
	<div class="container">
		<form action="register.php" method="POST" class="form-register">
			<h2>Registration</h2>
			<input type="text" name="registerkey" placeholder="e.g. pink panther" />
			<input type="hidden" name="token" value="<?= htmlspecialchars($gcp->tokens) ?>" />
			<input class="btn btn-large btn-primary" type="submit" value="Register" />
			<div class="message"><?= $message ?></div>
		</form>
	</div>
</body>
</html>