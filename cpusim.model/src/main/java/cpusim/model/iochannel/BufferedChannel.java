/**
 * File: ConcreteChannel
 * Last update: November 2013
 * Authors: Stephen Morse, Ian Tibbits, Terrence Tan
 * Class: CS 361
 * Project 6
 * 
 * update: changed for CS361 Project 6 Stephen Terrence Ian
 * Changed to help enable multiple input and output capability.
 * 
 * Added the flush method.
 */
package cpusim.model.iochannel;

import static com.google.common.base.Preconditions.checkNotNull;

import cpusim.model.ExecutionException;
import cpusim.model.util.MoreStrings;
import cpusim.model.util.Validate;
import cpusim.model.util.ValidationException;
import cpusim.model.util.conversion.ConvertStrings;

/**
 * This class represents an IOChannel that uses the State pattern to
 * delegate the actual behavior to another IOChannel. It just provides
 * an extra level of indirection to allow for separation of the GUI from
 * the model. This class is part of the model. The state channels
 * involve the GUI as needed.  It is up to the GUI to add these state
 * channels.
 * 
 * @author Dale Skrien
 * @author Stephen Morse
 * @author Ian Tibbits
 * @author Terrence Tan
 * 
 * @since 2013-11-01
 */
public class BufferedChannel implements IOChannel {
	
	private String name;
	private IOChannel state;

	// String buffer field for outputs
	private StringBuilder outputBuffer;

	// String buffer field for inputs
	private StringBuilder inputBuffer;

	// The line separator, used in String Channels
	private String LINE_SEPARATOR = System.getProperty("line.separator");

	/**
	 * The constructor to fromRootController a ConcreteChannel.
	 * 
	 * @param n - The name of the channel.
	 * @param s - The actual channel this concrete channel
	 * uses to execute any instructions.
	 */
	public BufferedChannel(String n, IOChannel s) {
		this.state = s;
		this.name = n;
		this.inputBuffer = new StringBuilder();
		this.outputBuffer = new StringBuilder();
	}

	/**
	 * Creates a concrete channel with null name.
	 * 
	 * @param strc - The actual channel this concrete channel
	 * uses to execute any instructions.
	 */
	public BufferedChannel(IOChannel strc) {
		this(null, strc);
	}

	/**
	 * Creates a concrete channel with null IOChannel.
	 * 
	 * @param n - The name of the channel.
	 */
	public BufferedChannel(String n) {
		this(n, null);
	}

	/**
	 * Gives the current state (IOChannel).
	 * 
	 * @return - the current state (IOChannel).
	 */
	public IOChannel getChannel() {
		return state;
	}

	/**
	 * Sets the IOChannel.
	 * 
	 * @param c - The actual channel this concrete channel
	 * uses to execute any instructions.
	 */
	public void setState(IOChannel c) { 
		this.state = checkNotNull(c); 
	}

	/**
	 * Resets the state.  Does nothing unless the state is a console channel, 
     * in which case it clears the console.
	 */
    @Override
	public void reset() {
        //reset the console channel if that is the state
//        if (this.state.getClass().equals(ConsoleChannel.class)){
//            ((ConsoleChannel)this.state).getIOConsole().clear();
//        }
    }

    @Override
	public void flush(boolean saveInputBuffers) {
		if(!saveInputBuffers) {
			this.inputBuffer = new StringBuilder();
		}
		if (this.outputBuffer.length() != 0) {
			state.writeString("Write: " + this.outputBuffer.toString()
					+ LINE_SEPARATOR);
			this.outputBuffer = new StringBuilder();
		}
	}

	/**
	 * Uses the state to read a long from the channel.
	 * 
	 * @param numBits - the number of bits the long should be able to fit into.
	 */
    @Override
	public long readLong(int numBits) {
		if(this.inputBuffer.length() > 0) {
            return this.getLongFromInputBuffer(numBits);
		} else {
			String inputFromChannel = 
					state.readString("Enter Inputs, the first of which must be an Integer: ");
			// Write directions if the user asks for "help"
			if(inputFromChannel.toLowerCase().equals("help")) {
				state.writeString("Type in a decimal, binary, or hexadecimal " +
						"integer and then press Enter. " +
						"For binary, use a prefix of \"0b\" or \"-0b\"." +
						" For hexadecimal, use " +
						"\"0x\" or \"-0x\"." + this.LINE_SEPARATOR +
						"To halt execution, use the Stop menu item from the Execute menu." +
						this.LINE_SEPARATOR);
			} else { //not help
				this.inputBuffer.append(inputFromChannel);
			}
			//recursive call with updated buffer
			return this.readLong(numBits);
		}
	}

	/**
	 * Uses the state to read an ASCII character from the channel.
	 */
	public char readAscii() {
		if(this.inputBuffer.length() > 0) {
            return this.getAsciiFromInputBuffer();
		} else {
			String readState = 
					state.readString("Enter Inputs, the first of which must be an Ascii character: ");
			// Write directions if the user asks for "help"
			if(readState.toLowerCase().equals("help")) {
				state.writeString("Type in a character with no surrounding " +
						"quotes and then press Enter." + this.LINE_SEPARATOR +
						"To halt execution, use the Stop menu item from the Execute menu." +
						this.LINE_SEPARATOR);
			} else { //not help
				this.inputBuffer.append(readState);
			}
			//recursive call with updated buffer
			return this.readAscii();
		}
	}

	/**
	 * 
	 * Uses the state to read a Unicode character from the channel.
	 */
	public int readUnicode() {
		//If buffer not empty, get the long from the front of the buffer 
		//and throws an exception if the initial characters do not make a long
		if(this.inputBuffer.length() > 0) {
            return this.getUnicodeFromInputBuffer();
		} else {
			String readState = 
					state.readString("Enter Inputs, the first of which must be Unicode: ");
			// Write directions if the user asks for "help"
			if(readState.toLowerCase().equals("help")) {
				state.writeString("Type in a character with no surrounding " +
						"quotes and then press Enter." + this.LINE_SEPARATOR +
						"To halt execution, use the Stop menu item from the Execute menu." +
						this.LINE_SEPARATOR);
			} else { //not help
				this.inputBuffer.append(readState);
			}
			//recursive call with updated buffer
			return this.readUnicode();
		}
	}

	/**
	 * Uses the state to output a Long value to the user.
	 * 
	 * @param value - the value to output to the user.
	 */
	public void writeLong(long value) {
		this.outputBuffer.append(" ");
		this.outputBuffer.append(value);
	}

	/**
	 * Uses the state to output an ASCII value to the user.
	 *
     * @param longValue - the long value of the character to
     * output to the user.
     */
	public void writeAscii(char longValue) {
		char charValue = (char) longValue;
		if (charValue != '\n') {
			this.outputBuffer.append(charValue);
		} else { //new line character
			if(this.outputBuffer.length() > 0) {
				state.writeString("Write: "+ this.outputBuffer.toString() + this.LINE_SEPARATOR);
				this.outputBuffer = new StringBuilder();
			}
		}
	}

	/**
	 * Uses the state to output a Unicode value to the user.
	 * 
	 * Do not include the checking for newline character so 
	 * that user can still output a newline character with
	 * this.
	 * 
	 * @param unicodeChar - the long value of the Unicode character
	 * to output to the user.
	 */
	public void writeUnicode(int unicodeChar) {
		this.outputBuffer.append(String.valueOf(unicodeChar));
	}

	/**
	 * Gives a string representation of the Concrete channel.
	 */
	public String toString() { 
		if (state == null) {
			return name; 
		} else {
			return state.toString();
		}
	}

	/**
	 * Parses the input buffer for a long at the beginning.
	 * 
	 * @return the next long in the output buffer
	 * @throws ExecutionException if the next element is not an integer
	 */
	private long getLongFromInputBuffer(int numBits) throws ExecutionException {

		CharSequence inputString = this.inputBuffer.toString().trim();
		long nextLong = 0;
		// Loops through input buffer, first checking if the entire 
		// String can be converted to a long, then if not check all 
		// but the last character until the string is empty n squared 
		// algorithm, but only used for user input so that is OK
		while (inputString.length() > 0) {
			try {
				nextLong = ConvertStrings.toLong(inputString);
				break;
			} catch (NumberFormatException e) {
				inputString = inputString.subSequence(0, inputString.length() - 1);
			}
		} 
		//if the string is empty after the above loop 
		//then the input cannot be converted to a long
		if(inputString.length() > 0) {
			throw new ExecutionException("There are currently no predefined " +
					"inputs from the user of type long.");
		}
		
		try {
			Validate.fitsInBits(nextLong, numBits);
		} catch(ValidationException ve) {
			throw new ExecutionException("Not enough bits to store: " +
					inputString+". Number of bits = "+numBits+".");
		}

		final CharSequence newInput = MoreStrings.removeLeadingWhitespace(inputString);
		this.inputBuffer = new StringBuilder(newInput.subSequence(inputString.length(), newInput.length()));
		
		return nextLong;
	}

	/**
	 * Gets the next character from input buffer.
	 * 
	 * @return - The first character from input buffer (if it is
	 * a valid ASCII character). 
	 * @throws ExecutionException - If next element in buffer
	 * is not a valid ASCII character.
	 */
	private char getAsciiFromInputBuffer() throws ExecutionException {
		char nextAscii = this.inputBuffer.charAt(0);
		try {
			Validate.isAsciiChar(((long)nextAscii));
		} catch (ValidationException e) {
			throw new ExecutionException(e.getMessage());
		}		
		this.inputBuffer.deleteCharAt(0);
		return nextAscii;
	}

	/**
	 * Gets the next character from input buffer.
	 * 
	 * @return - The first character from input buffer (if it is
	 * a valid UNICODE character). 
	 * @throws ExecutionException - If next element in buffer
	 * is not a valid UNICODE character.
	 */
	private char getUnicodeFromInputBuffer() throws ExecutionException {
		char nextUnicode = this.inputBuffer.charAt(0);
		try {
			Validate.isUnicodeChar(((long)nextUnicode));
		} catch (ValidationException e) {
			throw new ExecutionException(e.getMessage());
		}		
		this.inputBuffer.deleteCharAt(0);
		return nextUnicode;
	}
	
	/**
	 * Gets a String from the input buffer.
	 * 
	 * @return - The first character from input buffer (if it is
	 * a valid UNICODE character). 
	 * @throws ExecutionException - If next element in buffer
	 * is not a valid UNICODE character.
	 */
	private String getStringFromInputBuffer() {
		int pos = this.inputBuffer.indexOf("\n");
		if (pos == -1) {
			final String out = this.inputBuffer.toString();
			this.inputBuffer = new StringBuilder();
			
			return out;
		} else {
			final String out = this.inputBuffer.subSequence(0, pos).toString();
			this.inputBuffer.delete(0, pos + 1);
			
			return out;
		}
	}

	@Override
	public void writeString(String s) {
		state.writeString(s);
	}

	@Override
	public String readString(String prompt) {
		if(this.inputBuffer.length() > 0) {
            return this.getStringFromInputBuffer();
		} else {
			String inputFromChannel = 
					state.readString("Enter String input, end with a newline: ");
			// Write directions if the user asks for "help"
			if(inputFromChannel.toLowerCase().equals("help")) {
				state.writeString("Type in a string of characters and then press Enter. " +
						this.LINE_SEPARATOR +
						"To halt execution, use the Stop menu item from the Execute menu." +
						this.LINE_SEPARATOR);
			} else { //not help
				this.inputBuffer.append(inputFromChannel);
			}
			
			//recursive call with updated buffer
			return this.readString(prompt);
		}
	}
}
