<?php 

/**
 * This is a model for registration, responsible for saving mnemonic keys with NFC IDs (so that the user 
 * does not have to remember their NFC keys). 
 */
class RegisterModel {

    /**
     * Get a string of the format "pink panther" by hashing the string and accessing the arrays with the hash. 
     * @param  String $string The String to be hashed to generate the mnemonic animal. 
     * @return String         A String formed by hashing the input into a mnemonic. 
     */
    static function getAnimal($string) {
        $numrow_stmt = SQL::prepare('SELECT COUNT(*) AS total FROM key_color, key_animal');
        $numrow_stmt->execute();
        $numrow_result = $numrow_stmt->fetch(PDO::FETCH_ASSOC);
        $numrows = $numrow_result['total'];

        $inthash = intval(base_convert(crc32($string), 16, 10));

        $stmt = SQL::prepare('SELECT key_color.value, key_animal.value FROM key_color, key_animal LIMIT 1 OFFSET ?');
        $stmt->execute(array($inthash % $numrows));
        $entry = $stmt->fetch(PDO::FETCH_NUM);

        $color = $entry[0];
        $animal = $entry[1];

        return $color . ' ' . $animal;
    }

    /**
     * Generate a mnemonic key and store it in the database. 
     */
    static function generateKey($nfcid) {
        $select = SQL::prepare('SELECT register_key FROM register_keys WHERE nfcid=?');
        $select->execute(array($nfcid));
        $rowCount = $select->rowCount();

        if ($rowCount > 0) {
            // update expiration
            $update = SQL::prepare('UPDATE register_keys SET expiration=(NOW() + INTERVAL 6 HOUR) WHERE nfcid=?');
            $update->execute(array($nfcid));

            // return the key
            $result = $select->fetch(PDO::FETCH_ASSOC);
            return $result['register_key'];
        } 

        $salt = $nfcid;

        for ($i = 0; $i < 10; $i++) {
            $key = self::getAnimal($salt);
            try {
                $stmt = SQL::prepare('INSERT INTO `register_keys` SET `nfcid`=?, `register_key`=?, `expiration`=(NOW() + INTERVAL 6 HOUR)');
                $success = $stmt->execute(array($nfcid, $key));
                return $key;
            } catch (PDOException $e) {
                // append something so the hashed value changes
                $salt .= '-';
            }
            // keep trying...
        }

        // I give up :(
        return null;
    }

    /**
     * Gets the NFC id associated with the key. 
     */
    static function getNfcIdFromKey($key) {
        $key = strtolower($key);
        $stmt = SQL::prepare('SELECT nfcid FROM register_keys WHERE register_key=? AND NOW() <= expiration');
        $stmt->execute(array($key));
        if ($stmt->rowCount() != 1) {
            // register key should be unique
            return null;
        }

        $entry = $stmt->fetch(PDO::FETCH_NUM);
        return $entry[0];
    }

}

?>