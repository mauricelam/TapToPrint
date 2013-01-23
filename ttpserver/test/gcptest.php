<?php

require_once 'simpletest/autorun.php';

set_include_path('..');

require_once 'gcp.php';
require_once 'SQL.php';

class GCPTest extends UnitTestCase {

    public function testInvalidCreators () {
        $this->assertNull(GCP::fromPrinterId('invalid'));
        $this->assertNull(GCP::fromNfcId('invalid'));
    }

    public function testNfcCreator () {
        $fromNfc = GCP::fromNfcId('nfc-639c85e7-01010501cd040d05');
        $fromPrinter = GCP::fromPrinterId('f5b9c075-87e8-c558-d14d-be4fb3d945db');

        $this->assertNotNull($fromNfc);
        $this->assertNotNull($fromPrinter);

        // assertEqual could not handle circular dependency, so json_encode it first
        $this->assertEqual(json_encode($fromNfc), json_encode($fromPrinter));
    }

    public function testGetUserByEmail() {
        $email = 'mauricelam@gmail.com';

        $this->assertFalse(GCP::getUserByEmail('invalid'));

        $user = GCP::getUserByEmail($email);
        $this->assertNotNull($user);
        $this->assertEqual('nfc-639c85e7-01010501cd040d05', $user['nfcid']);
    }

}

?>