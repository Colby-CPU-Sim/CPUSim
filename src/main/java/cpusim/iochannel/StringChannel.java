/**
 * File: StringChannel
 * LastUpdate: November 2013
 */

package cpusim.iochannel;

import cpusim.util.CPUSimConstants;

/**
 * Interface that any channels we want to use for input or output
 * must implement.
 */
public interface StringChannel extends CPUSimConstants {
	
	/**
	 * displays an output to the user
	 * @param s - the output displayed to the user
	 */
	public void writeString(String s);

	/**
	 * reads and returns the String input of the user
	 * @param prompt - the prompt to the user for input
	 * @return the String input by the user
	 */
	public String readString(String prompt);
}
