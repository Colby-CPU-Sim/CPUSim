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

package cpusim.util;


///////////////////////////////////////////////////////////////////////////////
// the MachineReaderException class

public class MIFReaderException extends RuntimeException
{

    public MIFReaderException(String message)
    {
        super(message);
    }

}
