<?php

require_once 'utils.php';
require_once 'gcp.php';

$message = '';
$enabled = true;

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
$email = $gcp->getUserEmail();
$user = GCP::getUserByEmail($email);
if (!$user) {
    $message = 'You are not registered with us. ';
    $enabled = false;
} else if ($_POST['unregister'] == 'Unregister') {
    $success = $gcp->delete($user['printerid']);

    if (!$success)
        $message = 'Could not remove entry from database';
    else
        $message = 'Unregistered. Wish you all the best. ';
}


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
        <form method="POST" class="form-register form-delete">
            <h4>But we want you to stay...</h4>
            <div class="message"><?= $email ?>, you really want to quit TapToPrint? </div>
            <input type="hidden" name="token" value="<?= htmlspecialchars($gcp->tokens) ?>" />
            <input class="btn btn-large btn-primary" <? if (!$enabled) echo 'disabled="disabled"'; ?> type="submit" name='unregister' value="Unregister" />
            <div class="message"><?= $message ?></div>
        </form>
    </div>
</body>
</html>