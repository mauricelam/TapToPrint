<?php

require_once 'simpletest/autorun.php';

set_include_path('..');

require_once 'utils.php';

class UtilsTest extends UnitTestCase {

    public function testDownloadFile () {
        $url = 'http://web.engr.illinois.edu/~lam25/utils/echo.php?ping=something';
        $testpath = 'temp_test_download_file';

        // make sure the file does not exist
        $this->assertFalse(file_exists($testpath));

        // download the file
        \Utils\downloadFile($url, $testpath);
        $this->assertEqual('something', file_get_contents($testpath));

        // delete the test file
        unlink($testpath);
        $this->assertFalse(file_exists($testpath));
    }

}

?>