/**
 * File: CommandLineChannel
 * Last update: December 2013
 * Authors: Stephen Morse, Ian Tibbits, Terrence Tan
 * Class: CS 361
 * Project 7
 * 
 * changed implemented interface from IOChannel to StringChannel.
 * Removed writeLong, writeAscii, writeUnicode, readLong, readAscii, 
 * readUnicode, and reset methods.
 * added writeString(String s):void method that writes string to System.out
 * added readString(String prompt): String method that writes prompt 
 * to System.out and waits for whole line user input from System.in,
 * then returns input

 */
package cpusim.iochannel;

import java.util.Scanner;

/**
 * This class implements IOChannel using the terminal/command line.  It is
 * used when CPU Sim is run in non-GUI mode.
 */
public class CommandLineChannel implements StringChannel {

	Scanner scanner = new Scanner(System.in);

	/**
	 * Constructor for CommandLineChannel. There is only
	 * one CommandLineChannel channel that is ever used.
	 */
	public CommandLineChannel() {
	}
	/**
	 * Gives a string representation of the object.
	 * In this case, its name field.
	 */
	public String toString() {
		return "Command Line Channel";
	}

	/**
	 * displays an output to the user
	 * @param s - the output displayed to the user
	 */
	@Override
	public void writeString(String s) {
		System.out.print(s);
	}

	/**
	 * reads and returns the String input of the user
	 * @param prompt - the prompt to the user for input
	 * @return the String input by the user
	 */
	@Override
	public String readString(String prompt) {
		// User cannot abort, so no need to throw execution exceptions,
		// as in other channels.
		System.out.print(prompt);
		return scanner.nextLine();
	}

}
