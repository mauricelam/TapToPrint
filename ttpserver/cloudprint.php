<?php
/**
 * An extension of google-api-php-client that allows for API interactions
 * with Google Cloud Print. 
 */
class Google_CloudPrint extends Google_Service {
  /**
   * Constructs the internal representation of the Cloud Print service.
   *
   * @param Google_Client $client
   */
  public function __construct(Google_Client $client) {
    $this->servicePath = 'cloudprint/';
    $this->version = 'v1';
    $this->serviceName = 'cloudprint';

    $client->addService($this->serviceName, $this->version);
    $this->resource = new Google_CloudPrintResource($this, $this->serviceName, 'register', json_decode('{"methods":{"register":{"scopes":["https://www.googleapis.com/auth/cloudprint"],"parameters":{"printer":{"required":true,"type":"string","location":"query"},"proxy":{"required":true,"type":"string","location":"query"},"capabilities":{"type":"string","location":"body"},"defaults":{"type":"string","location":"body"},"capsHash":{"type":"string","location":"query"}},"request":{"$ref":"File"},"response":{"$ref":"Output"},"httpMethod":"POST","path":"register","contentType":"multipart/form-data;boundary=TTPROCKS","id":"cloudprint.register"},"fetch":{"scopes":["https://www.googleapis.com/auth/cloudprint"],"parameters":{"printerid":{"required":true,"type":"string","location":"query"}},"request":{"$ref":"Input"},"response":{"$ref":"Output"},"httpMethod":"GET","path":"fetch","id":"cloudprint.fetch"},"list":{"scopes":["https://www.googleapis.com/auth/cloudprint"],"parameters":{"proxy":{"required":true,"type":"string","location":"query"}},"request":{"$ref":"Input"},"response":{"$ref":"Output"},"httpMethod":"GET","path":"list","id":"cloudprint.list"},"control":{"scopes":["https://www.googleapis.com/auth/cloudprint"],"parameters":{"jobid":{"required":true,"type":"string","location":"query"},"status":{"required":true,"type":"string","location":"query"},"code":{"type":"string","location":"query"},"message":{"type":"string","location":"query"}},"request":{"$ref":"Input"},"response":{"$ref":"Output"},"httpMethod":"GET","path":"control","id":"cloudprint.control"},"delete":{"scopes":["https://www.googleapis.com/auth/cloudprint"],"parameters":{"printerid":{"required":true,"type":"string","location":"query"}},"request":{"$ref":"Input"},"response":{"$ref":"Output"},"httpMethod":"GET","path":"delete","id":"cloudprint.delete"}}}', true));
  }
}

/**
 * API reference : https://developers.google.com/cloud-print/docs/proxyinterfaces. 
 *
 * All these methods asks for required fields, and other optional fields should be 
 * put inside the $optParams array. 
 */
class Google_CloudPrintResource extends Google_ServiceResource {

  /**
   * Interface for registering printers. 
   * 
   * @param  String $printername  Name of the printer. Used for identification. 
   * @param  String $proxyid      The unique ID of the proxy. 
   * @param  String $capabilities Text of a PPD file that describes the printer 
   *                              capabilities. Also used as defaults. 
   */
  public function register($printername, $proxyid, $capabilities, $optParams = array()) {
    // The body contains these two files, which are usually big
    // For PPD files, capabilities and defaults are the same file
    $postBody = array(
      'capabilities' => array('name' => 'capabilities', 'content' => $capabilities),
      'defaults' => array('name' => 'defaults', 'content' => $capabilities)
      );
    // Post using multipart format
    $postBody = self::encodeMultipart(array(), $postBody);
    $params = array('printer'  => $printername, 'proxy'    => $proxyid, 'postBody' => $postBody);
    $params = array_merge($params, $optParams);
    $data = $this->__call('register', array($params));
    if ($this->useObjects()) {
      return new Google_Output($data);
    } else {
      return $data;
    }
  }

  /**
   * Fetches the list of jobs pending for the printer. 
   * 
   * @param  String $printerid The unique ID of the printer (returned when 
   *                           registering / calling list).
   */
  public function fetch($printerid, $optParams = array()) {
    $params = array('printerid' => $printerid);
    $params = array_merge($params, $optParams);
    $data = $this->__call('fetch', array($params));
    if ($this->useObjects()) {
      return new Google_Output($data);
    } else {
      return $data;
    }
  }

  /**
   * Lists out the printers. As as /list in the API (list is a reserved keyword
   * in PHP). 
   * 
   * @param  String $proxy     The Proxy ID. 
   */
  public function listPrinters($proxy, $optParams = array()) {
    $params = array('proxy' => $proxy);
    $params = array_merge($params, $optParams);
    $data = $this->__call('list', array($params));
    if ($this->useObjects()) {
      return new Google_Output($data);
    } else {
      return $data;
    }
  }

  /**
   * Updates the status of a print job. 
   * 
   * @param  String $jobid     [description]
   * @param  String $status    Possible values are 
   *            QUEUED: Job just added and has not yet been downloaded.
   *            IN_PROGRESS:  Job downloaded and has been added to the 
   *                          client-side native printer queue.
   *            DONE: Job printed successfully.
   *            ERROR: Job cannot be printed due to an error.
   */
  public function control($jobid, $status, $optParams = array()) {
    $params = array('jobid' => $jobid, 'status' => $status);
    $params = array_merge($params, $optParams);
    $data = $this->__call('control', array($params));
    if ($this->useObjects()) {
      return new Google_Output($data);
    } else {
      return $data;
    }
  }

  /**
   * Deletes a printer from the account. 
   * 
   * @param  String $printerid The printer ID. 
   */
  public function delete($printerid, $optParams = array()) {
    $params = array('printerid' => $printerid);
    $params = array_merge($params, $optParams);
    $data = $this->__call('delete', array($params));
    if ($this->useObjects()) {
      return new Google_Output($data);
    } else {
      return $data;
    }
  }

  /**
   * Encode the file using the multipart format. 
   * @param $fields The fields that are not part of a file. Usually shorter params. 
   * @param $files The files to submit in the body. Should be associative arrays. 
   * @param $file_type String of the file Content-Type header
   */
  static $BOUNDARY = 'TTPROCKS';
  private static function encodeMultipart($fields, $files, $file_type='application/xml') {
    $lines = array();
    foreach ($fields as $key => $value) {
      $lines[] = '--' . self::$BOUNDARY;
      $lines[] = 'Content-Disposition: form-data; name="' . $key . '"';
      $lines[] = '';
      $lines[] = $value;
    }
    foreach ($files as $key => $file) {
      $lines[] = '--' . self::$BOUNDARY;
      $lines[] = 'Content-Disposition: form-data; name="' . $key . '"; filename="' . $file['name'] . '"';
      $lines[] = 'Content-Type: ' . $file_type;
      $lines[] = '';
      $lines[] = $file['content'];
    }
    $lines[] = '--' . self::$BOUNDARY . '--';
    $lines[] = '';
    return join("\r\n", $lines);
  }

}

?>
