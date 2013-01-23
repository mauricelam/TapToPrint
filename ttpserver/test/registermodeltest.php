<?php

require_once 'simpletest/autorun.php';

set_include_path('..');
require_once 'SQL.php';
require_once 'registermodel.php';

class RegisterModelTest extends UnitTestCase {

    function setup () {
        SQL::initWithDB('lam25_ttp_test');
    }

    function tearDown () {
        $stmt = SQL::prepare('TRUNCATE TABLE `register_keys`');
        $stmt->execute();
    }

    public function testAnimal () {
        $rm = new RegisterModel(); 
        $animal = $rm->getAnimal('some string');
        $this->assertEqual('blue rabbit', $animal);
    }

    public function testGenerateKey () {
        $rm = new RegisterModel();
        $key = $rm->generateKey('some string');
        $this->assertFalse(is_null($key));
        $nfcid = $rm->getNfcIdFromKey($key);
        $this->assertEqual('some string', $nfcid);
    }

    public function testSameKey () {
        $rm = new RegisterModel();

        $key = $rm->generateKey('some string');
        $this->assertFalse(is_null($key));
        $key2 = $rm->generateKey('some string');
        $this->assertFalse(is_null($key2));
        $this->assertEqual($key, $key2);

        $nfcid = $rm->getNfcIdFromKey($key);
        $this->assertEqual('some string', $nfcid);
    }

    public function testKeyCollision () {
        $rm = new RegisterModel();

        $key = $rm->generateKey('d');
        $this->assertFalse(is_null($key));
        $key2 = $rm->generateKey('u');
        $this->assertFalse(is_null($key2));
        $this->assertNotEqual($key, $key2);

        $this->assertEqual('d', $rm->getNfcIdFromKey($key));
        $this->assertEqual('u', $rm->getNfcIdFromKey($key2));
    }

}

?>