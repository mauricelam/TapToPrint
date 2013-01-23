TapToPrint is an Android application for use on a printer. It lets the user print to a virtual printer on Google Cloud Print, and then select the physical printer by tapping a NFC tag onto the printer. Think of a printer vending machine that can identify you, fetch the print job and charge for your printing in a single tap. 

This is also the winning entry to Google GSA development competition Jan 2013! :)

This project consist of two parts: the first part the server, in the directory `ttpserver`, which is a PHP server supporting the communication with Google Cloud Print. The other part is an Android application in directory `TapToPrint`, which is designed to run on, or together with, the printer that fetches the print job from the cloud. 
