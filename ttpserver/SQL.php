<?php 

/**
 * This class wraps the PDO class in a singleton instance. 
 * Reference: http://php.net/manual/en/book.pdo.php
 */
class SQL { 
    
    private static $objInstance; 
    
    /* 
     * Class Constructor - Create a new database connection if one doesn't exist 
     * Set to private so no-one can create a new instance via ' = new SQL();' 
     */ 
    private function __construct() {} 
    
    /* 
     * Like the constructor, we make __clone private so nobody can clone the instance 
     */ 
    private function __clone() {} 


    const DB_DSN  = 'mysql:host=engr-cpanel-mysql.engr.illinois.edu;dbname=lam25_ttp;charset=UTF-8';

    public static function initWithDB($dbname) {

        $dsn = 'mysql:host=engr-cpanel-mysql.engr.illinois.edu;dbname='.$dbname.';charset=UTF-8';

        if(!self::$objInstance){ 
            self::$objInstance = new PDO($dsn, $_SERVER['dbuser'], $_SERVER['dbpass']); 
            self::$objInstance->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION); 
            self::$objInstance->setAttribute(PDO::ATTR_EMULATE_PREPARES, false);
        } 
        
        return self::$objInstance; 

    }
    
    /* 
     * Returns DB instance or create initial connection 
     * @param 
     * @return $objInstance; 
     */ 
    public static function getInstance() { 
            
        if(!self::$objInstance){ 
            self::$objInstance = new PDO(self::DB_DSN, $_SERVER['dbuser'], $_SERVER['dbpass']); 
            self::$objInstance->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION); 
			self::$objInstance->setAttribute(PDO::ATTR_EMULATE_PREPARES, false);
        } 
        
        return self::$objInstance; 
    
    } # end method 
    
    /* 
     * Passes on any static calls to this class onto the singleton PDO instance 
     * @param $chrMethod, $arrArguments 
     * @return $mix 
     */ 
    final public static function __callStatic( $chrMethod, $arrArguments ) { 
            
        $objInstance = self::getInstance(); 
        
        return call_user_func_array(array($objInstance, $chrMethod), $arrArguments); 
        
    } # end method 
    
}
?>