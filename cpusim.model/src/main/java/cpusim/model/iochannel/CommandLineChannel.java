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
package cpusim.model.iochannel;

import java.util.Scanner;

import cpusim.model.util.Convert;
import cpusim.model.util.units.ArchType;
import cpusim.model.util.units.ArchValue;

/**
 * This class implements IOChannel using the terminal/command line.  It is
 * used when CPU Sim is run in non-GUI mode.
 */
public class CommandLineChannel implements IOChannel {

	Scanner scanner = new Scanner(System.in);

	/**
	 * Constructor for CommandLineChannel. There is only
	 * one CommandLineChannel channel that is ever used.
	 */
	public CommandLineChannel() {
	}

	/**
	 * Reads the next line from stdin.
	 * @return
	 */
	private String getLine() {
		return scanner.nextLine();
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
		return getLine();
	}
	
	@Override
	public long readLong(ArchValue numBits) {
		final String line = getLine();
		final long init = Long.parseLong(line);
		return init & numBits.mask();
	}
	
	@Override
	public char readAscii() {
		final String line = getLine();
		return (char)Convert.fromAsciiStringToLong(line, 8, 0);
	}
	
	@Override
	public char readUnicode() {
		final String line = getLine();
		return (char)Convert.fromUnicodeStringToLong(line, 8);
	}
	
	@Override
	public void writeLong(long value) {
		System.out.print(value);
	}
	
	@Override
	public void writeAscii(long longValue) {
		System.out.print(ArchType.Byte.of(1).mask() & longValue);
	}
	
	@Override
	public void writeUnicode(long longValue) {
		System.out.print(ArchType.Byte.of(2).mask() & longValue);
		
	}
	
	@Override
	public void flush(boolean saveInputBuffers) {
		// Does nothing
		
	}
	
	@Override
	public void reset() {
		// nothing to reset
	}

}
