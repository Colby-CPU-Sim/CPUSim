///////////////////////////////////////////////////////////////////////////////
// File:    	MachineReaderException.java
// Author:		Dale Skrien
// Project: 	CPU Sim 3.0
// Date:    	June, 2001
//
// Description:
// An extension of RuntimeException to handle any errors while reading
//    a new machine from an XML file.


///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

package cpusim.xml;


/**
 * This class represents exceptions thrown when attempting to read a machine from an XML
 * file.
 */
public class MachineReaderException extends RuntimeException {

    MachineReaderException(String message) {
        super(message);
    }


    MachineReaderException(String message, Throwable cause) {
        super(message, cause);
    }

}
