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
 * added writeString(String s):void method that writes string to out
 * added readString(String prompt): String method that writes prompt 
 * to out and waits for whole line user input from System.in,
 * then returns input

 */
package cpusim.model.iochannel;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import cpusim.model.util.Convert;
import cpusim.model.util.conversion.ConvertStrings;
import cpusim.model.util.units.ArchType;

/**
 * This class implements IOChannel using the terminal/command line.  It is
 * used when CPU Sim is run in non-GUI mode.
 */
public class StreamChannel implements IOChannel, AutoCloseable {

	private final InputStream in;
	private final PrintStream out;
	
	private final Scanner scanner;
	
	private static final StreamChannel CONSOLE_CHANNEL_HELPER = new StreamChannel();
	
	/**
	 * Helper to replace {@code StreamChannel.console()} helper from {@code CpuSimConstants}.
	 * 
	 * @return {@link #CONSOLE_CHANNEL_HELPER}
	 * 
	 * @deprecated Counting on a single instance is bad, should replace later
	 */
	public static final StreamChannel console() {
		return CONSOLE_CHANNEL_HELPER;
	}

	/**
	 * Constructor for CommandLineChannel. There is only
	 * one CommandLineChannel channel that is ever used.
	 * 
	 * Delegates to {@link #CommandLineChannel(InputStream, PrintStream)} with {@link System#out} and {@link System#in}. 
	 * 
	 * @see #CommandLineChannel(InputStream, PrintStream)
	 */
	public StreamChannel() {
		this(System.in, System.out);
	}
	
	/**
	 * Creates a channel from an {@link InputStream} and a {@link PrintStream}. 
	 * 
	 * @param in
	 * @param out
	 */
	public StreamChannel(final InputStream in, final PrintStream out) {
		this.in = checkNotNull(in);
		this.out = checkNotNull(out);
		
		this.scanner = new Scanner(in);
	}
	
	
	@Override
	public void close() throws IOException {
		if (in != null && in != System.in) {
			// don't close System.in for obvious reasons, but we compare with == since reference it correct
			scanner.close();
		}
		
		if (out != null && out != System.out) {
			// same justification for out
			out.close();
		}
	}

	/**
	 * Get the input stream.
	 * @return the in
	 */
	final InputStream getInputStream() {
		return in;
	}

	/**
	 * Get the current output stream.
	 * 
	 * @return the out
	 */
	final PrintStream getOutputStream() {
		return out;
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
		out.print(s);
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
		out.print(prompt);
		return getLine();
	}
	
	@Override
	public long readLong(int numBits) {
		final String line = getLine();
		final long init = Long.parseLong(line);
		return init & ArchType.Bit.getMask(numBits);
	}
	
	@Override
	public char readAscii() {
		final String line = getLine();
		return (char)Convert.fromAsciiStringToLong(line, 8, 0);
	}
	
	@Override
	public int readUnicode() {
		final String line = getLine();
		return (int)ConvertStrings.from16WToLong(line, ArchType.Byte.of(2));
	}
	
	@Override
	public void writeLong(long value) {
		out.print(value);
	}
	
	@Override
	public void writeAscii(char longValue) {
		out.print(ArchType.Byte.of(1).mask() & longValue);
	}
	
	@Override
	public void writeUnicode(int unicodeChar) {
		out.print(ArchType.Byte.of(2).mask() & unicodeChar);
	}
	
	@Override
	public void flush(boolean saveInputBuffers) {
		out.flush();
	}
	
	@Override
	public void reset() {
		try {
			if (in != null) {
				in.reset();
			}
		} catch (IOException ioe) {
			throw new IllegalStateException(ioe);
		}
	}

}
