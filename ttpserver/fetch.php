<?php

/**
 * AJAX endpoint for fetching the most recent print job from the queue. 
 */
require_once 'utils.php';
require_once 'gcp.php';
require_once 'registermodel.php';

$DIR_HTTP_URL = dirname('https://' . $_SERVER['HTTP_HOST'] . $_SERVER['PHP_SELF']);

\Utils\assert(isset($_GET['nfcid']), 'Field nfcid is missing');
$gcp = GCP::fromNfcId($_GET['nfcid']);

if (!$gcp) failWithRegistrationKey($_GET['nfcid']);
$result = $gcp->fetch($gcp->user['printerid']);

\Utils\assert($result['jobs'] && count($result['jobs'] > 0), 'No file to print');

$jobList = array_reverse($result['jobs']); // sort the print jobs from most recent to least recent
refreshDatabasePrintJobs($_GET['nfcid'], $jobList);

$jobs = array();
foreach ($jobList as $job) {
	$jobs[] = array(
		'title' => $job['title'], 
		'id' => $job['id'], 
		'fileUrl' => $DIR_HTTP_URL . '/download.php?jobid=' . $job['id']
	);
}

$response = array('success' => true, 'jobs' => $jobs);
echo json_encode($response);


/**
 * Exit, print failure message and a registration key that can be used to register in /register.php. 
 */
function failWithRegistrationKey($nfcid) {
	// Not registered. Return with registration key. 
	$response = new stdClass();
	$response->success = false;
	$response->message = 'Not registered. Please register on /register.php';
	$response->key = RegisterModel::generateKey($_GET['nfcid']);
	exit(json_encode($response));
}

/**
 * Refresh the list of print jobs in database to the newly fetched $jobList. 
 */
function refreshDatabasePrintJobs($nfcid, $jobList) {
	// delete existing records about the print queue
	$deleteStmt = SQL::prepare('DELETE FROM printjobs WHERE nfcid=?');
	$deleteStmt->execute(array($_GET['nfcid']));

	$insertStmt = SQL::prepare('INSERT INTO printjobs SET nfcid=?, jobid=?, fileUrl=?');
	foreach ($jobList as $job) {
		// insert the print jobs into the database
		$insertStmt->execute(array($nfcid, $job['id'], $job['fileUrl']));
	}
}

?>