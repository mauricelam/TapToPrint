<?php
require_once 'utils.php';

\Utils\assert(isset($_GET['jobid']) && isset($_GET['nfcid']) && isset($_GET['status']), 'Required field(s) missing');

require_once 'gcp.php';
$gcp = GCP::fromNfcId($_GET['nfcid']);
\Utils\assert($gcp, 'Not registered. Please register on /register.php');

// delete the downloaded file
if ($_GET['status'] == 'DONE' || $_GET['status'] == 'ERROR') {
    $tempPath = __DIR__ . '/printfiles/' . $_GET['jobid'] . '.pdf';
    unlink($tempPath);
}

$result = $gcp->control($_GET['jobid'], $_GET['status']);
echo json_encode($result);

?>