/**
 * File: CPUSimConstants.java
 * Last update: December 2013
 * Authors: Stephen Morse, Ian Tibbits, Terrence Tan
 * Class: CS 361
 * Project 7
 * 
 * changed concrete to buffered
 */

package cpusim.util;

import cpusim.BufferedChannel;
import cpusim.ConsoleChannel;
import cpusim.DialogChannel;
import cpusim.IOChannel;
import cpusim.module.ConditionBit;
import cpusim.module.Register;

public interface CPUSimConstants {
    //is this a Macintosh?  This constant might be used to set up the menus
    //differently for Macs and for other OS's.
    boolean MAC_OS_X =
            (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));

    // constants for micros Arithmetic and Increment which specify optional
    // ConditionBits.  This ConditionBit is uses when the user chooses (none)
    // as the ConditionBit.  Setting or clearing this bit does nothing since the
    // Register it uses is hidden from the rest of the machine.
    ConditionBit NO_CONDITIONBIT =
            new ConditionBit("(none)", null, new Register("", 1), 0, false);

    // the three standard options available for io channels
    IOChannel CONSOLE_CHANNEL =
            new BufferedChannel(new ConsoleChannel("[Console]"));
    IOChannel DIALOG_CHANNEL =
    		new BufferedChannel(new DialogChannel("[Dialog]"));
    IOChannel FILE_CHANNEL =
            new BufferedChannel("File...");

}


