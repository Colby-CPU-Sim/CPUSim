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

import com.google.common.primitives.Chars;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import cpusim.model.util.units.ArchType;
import cpusim.model.util.units.ArchValue;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class implements IOChannel using the terminal/command line.  It is
 * used when CPU Sim is run in non-GUI mode.
 */
public class StreamChannel implements IOChannel, AutoCloseable {

	private final ReadOnlyObjectProperty<UUID> uuidProperty;
	private final InputStream in;
	private final PrintStream out;
	
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
	 * Delegates to {@link #StreamChannel(InputStream, PrintStream)} with {@link System#out} and {@link System#in}.
	 * 
	 * @see #StreamChannel(InputStream, PrintStream)
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
	public StreamChannel(final UUID id, final InputStream in, final PrintStream out) {
		this.in = checkNotNull(in);
		this.out = checkNotNull(out);

		this.uuidProperty = new SimpleObjectProperty<>(this, "id", id);
	}

	/**
	 * Creates a channel from an {@link InputStream} and a {@link PrintStream}. 
	 * 
	 * @param in
	 * @param out
	 */
	public StreamChannel(final InputStream in, final PrintStream out) {
		this(UUID.randomUUID(), in, out);
	}

	@Override
	public ReadOnlyProperty<UUID> idProperty() {
		return uuidProperty;
	}

	@Override
	public void close() throws IOException {
		if (in != null && in != System.in) {
			// don't close System.in for obvious reasons, but we compare with == since reference it correct
			in.close();
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

		StringBuilder buff = new StringBuilder();
		try {
			int c;
			while ((c = in.read()) != '\n' && c != '\r') { // god damn carriage return.
				buff.append((char) c);
			}
		} catch (IOException ioe) {
			throw new IllegalArgumentException(ioe);
		}

		return buff.toString();
	}

	private void readBytes(byte[] buffer, int amount) {
		checkNotNull(buffer);
		checkArgument(buffer.length >= amount,
				"Must specify buffer[%s] larger than amount, %s", buffer.length, amount);

		try {
			int c, i = 0;
			while ((c = in.read()) != '\n' && i < buffer.length) {
				buffer[i++] = (byte)c;
			}

			Arrays.fill(buffer, i, buffer.length, (byte)0);
		} catch (IOException ioe) {
			throw new IllegalArgumentException(ioe);
		}
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
		final ArchValue value = ArchType.Bit.of(numBits);
		final byte[] buffer = new byte[Longs.BYTES];
		readBytes(buffer, (int)value.as(ArchType.Byte));

		return Longs.fromByteArray(buffer) & value.mask();
	}
	
	@Override
	public char readAscii() {
		final ArchValue value = ArchType.Byte.of(1);
		final byte[] buffer = new byte[Chars.BYTES];
		readBytes(buffer, (int)value.as(ArchType.Byte));

		return Chars.fromByteArray(buffer);
	}
	
	@Override
	public int readUnicode() {
		final ArchValue value = ArchType.Byte.of(2);
		final byte[] buffer = new byte[Ints.BYTES];
		readBytes(buffer, (int)value.as(ArchType.Byte));

		return Ints.fromByteArray(buffer) & value.imask();
	}
	
	@Override
	public void writeLong(long value) {
		out.print(value);
	}
	
	@Override
	public void writeAscii(char character) {
		out.print(character);
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
