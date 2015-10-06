///////////////////////////////////////////////////////////////////////////////
// File:    	NamedObject.java
// Type:    	java application file
// Author:		Josh Ladieu
// Project: 	CPU Sim
// Date:    	November, 2000
//
// Description:
//   This file contains the interface for a NamedObject in CPUSim

///////////////////////////////////////////////////////////////////////////////
// the package in which our file resides
package cpusim.util;

public interface NamedObject extends Cloneable
{
    public String getName();

    public void setName(String name);
}