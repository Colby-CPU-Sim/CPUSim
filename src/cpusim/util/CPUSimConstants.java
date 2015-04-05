///////////////////////////////////////////////////////////////////////////////
// File:    	CPUSimConstants.java
// Type:    	java application file
// Author:		Dale Skrien
// Project: 	CPU Sim
// Date:    	June, 2000
//
// Description:
//   This file contains various constants for CPU Sim.
//
// To be done:


/**
 * File: CPUSimConstants.java
 * Last update: December 2013
 * Authors: Stephen Morse, Ian Tibbits, Terrence Tan
 * Class: CS 361
 * Project 7
 * 
 * changed concrete to buffered
 */

///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

package cpusim.util;

import cpusim.BufferedChannel;
import cpusim.ConsoleChannel;
import cpusim.DialogChannel;
import cpusim.IOChannel;
import cpusim.module.ConditionBit;
import cpusim.module.Register;

///////////////////////////////////////////////////////////////////////////////
// the CPUSimConstants class

public interface CPUSimConstants {
    // constant for the current version number of CPU Sim
    public static final String VERSION_NUMBER = "4.0.0";

    //is this a Macintosh?  This constant is used to set up the menus
    //differently for Macs and for other OS's.
    public static final boolean MAC_OS_X =
            (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));

    // constants for micros Arithmetic and Increment which specify optional
    // ConditionBits.  Setting or clearing this bit does nothing since the
    // Register it uses is hidden from the rest of the machine.
    public static final ConditionBit NO_CONDITIONBIT =
            new ConditionBit("(none)", null, new Register("", 1), 0, false);

    // the three standard options available for io channels
    public static final IOChannel CONSOLE_CHANNEL =
            new BufferedChannel(new ConsoleChannel("[Console]"));
    public static final IOChannel DIALOG_CHANNEL =
    		new BufferedChannel(new DialogChannel("[Dialog]"));
    public static final IOChannel FILE_CHANNEL =
            new BufferedChannel("File...");

}


