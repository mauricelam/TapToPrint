<?php 

/**
 * A collection of common utilities that other code requires. Either use the namespace
 * or call with the syntax \Utils\somefunction(). 
 */
namespace Utils;

/**
 * Download a file from $url to the specified $path. Optionally with a stream context. 
 */
function downloadFile($url, $path, $stream = null) {
	$bufferSize = 1024 * 8;
	if (is_null($stream)) {
		$file = fopen ($url, "rb");
	} else {
		$file = fopen ($url, "rb", false, $stream);
	}
	if ($file) {
		$newf = fopen ($path, "wb");
		if ($newf) {
			while(!feof($file)) {
				fwrite($newf, fread($file, $bufferSize), $bufferSize);
			}
		}
	}
	if ($file) fclose($file);
	if ($newf) fclose($newf);
}

/**
 * Exits the program and prints a JSON encoded error message. 
 */
function error($msg) {
	exit(json_encode(array('success' => false, 'message' => $msg)));
}

function assert($condition, $msg) {
	if (!$condition) error($msg);
}

?>