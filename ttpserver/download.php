<?php

require_once 'gcp.php';

header('Content-type: application/pdf'); 

$selectStmt = SQL::prepare('SELECT * FROM printjobs WHERE jobid=?');
$selectStmt->execute(array($_GET['jobid']));

$job = $selectStmt->fetch();

if (!$job) exit();

$gcp = GCP::fromNfcId($job['nfcid']);
// Download the file to local filesystem
// We need this because OAuth token and special headers are required to download the file
// And the phone won't want to handle that
$opts = array (
 'http' => array (
     'method' => 'GET',
     'header' => "X-CloudPrint-Proxy: true\r\n".
     'Authorization: OAuth ' . $gcp->getToken() . "\r\n".
     'Content-Type: application/pdf'
     )
 );
$context = stream_context_create($opts);
echo file_get_contents($job['fileUrl'], false, $context);

?>