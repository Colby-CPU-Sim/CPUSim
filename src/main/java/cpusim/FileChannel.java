/**
 * File: FileChannel
 * Author: Dale Skrien
 * Last Update: August 2013
 */

/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 12/5/13
 * with the following changes:
 *
 * 1). Added unReadLong to unread a integer.
 * 2). Added unReadOneChar to unread a character.
 * 3). Added unWriteLong to unwrite a integer.
 * 4). Added unwriteOneChar to unwrite a character.
 * 5). Added writeToFile to write the buffer to the file.
 */
package cpusim;

import cpusim.util.*;

import java.io.*;


/**
 * This file contains the class that manages an IO channel to/from a file.
 * It maintains PushBackReaders and FileWriters for each file
 * and maintains the data to and from the user.
 */
public class FileChannel implements IOChannel  {
	// Where to get or send the data
	private File file;
	// Reader for the file for input
	private PushBackReader reader;
	// Writer for the file for output
	private PushBackWriter writer;
	
	/**
	 * Creates a new File Channel. Note that this file channel
	 * constructor should probably not be used. The file channel
	 * that is used is in the CPUSimConstants file, it is the only
	 * one that is used.
	 * 
	 * @param file - The file for the channel to read from or write to.
	 */
    public FileChannel(File file) {
        this.file = file;
        this.reader = null;
        this.writer = null;
    }

    /**
     * returns the next integer from input as a long that fits in the given
     * number of bits.  If it doesn't fit, a NumberFormatException is thrown.
     *
     * @param numBits the number of bits into which the long must fit
     * @return the long value that was input
     * @throws ExecutionException if it cannot read a long.
     */
    public long readLong(int numBits) {
        try {
            if (reader == null) {
                reader = new PushBackReader(new FileReader(file));
            }
            // Read past any white space and
            // read the first non-white space-- if not a digit or + or -,
            // throw error
            int c = reader.read();
            while (c != -1 && Character.isWhitespace((char) c)) {
                c = reader.read();
            }
            String s = "";
            if (c == '+' || c == '-') {
                s += (char) c;
                c = reader.read();
            }
            if (c == -1 || !Character.isDigit((char) c)) {
                throw new ExecutionException("Attempted to read an integer" +
                        " from file " + file.getName() + " but found " +
                        (c == -1 ? "the end of file" : "" + (char) c) + ".");
            }
            // Loop while reading digits, appending them to the string
            while (c != -1 && Character.isDigit((char) c)) {
                s += (char) c;
                c = reader.read();
            }
            // Push back the last character read
            reader.unread();
            // Make sure input is valid
            long value = Convert.fromAnyBaseStringToLong(s);
            Validate.fitsInBits(value, numBits);
            return value;
        } catch (ValidationException ve) {
            throw new ExecutionException(ve.getMessage());
        } catch (FileNotFoundException fne) {
            throw new ExecutionException("Attempted to read from file " +
                    file.getName() + " but it could not be found.");
        } catch (IOException ioe) {
            throw new ExecutionException("CPUSim was unable to read " +
                    "from file " + file.getName() + ".");
        }
    }

    /**
     * returns the next ASCII char from input.
     *
     * @return the ASCII character read
     * @throws ExecutionException if it cannot read an ASCII char.
     */
    public char readAscii() {
        try {
            if (reader == null) {
                reader = new PushBackReader(new FileReader(file));
            }
            int c = reader.read();
            if (c > 255 || c < 0) {
                throw new ExecutionException("Attempted to read an ASCII" +
                        "character from file " + file.getName() + "\n but the next " +
                        "character was " +
                        (c == -1 ? "the end of file" : "" + (char) c) + ".");
            }
            return (char) c;
        } catch (FileNotFoundException fne) {
            throw new ExecutionException("Attempted to read from file " +
                    file.getName() + " but it could not be found.");
        } catch (IOException ioe) {
            throw new ExecutionException("CPUSim was unable to read " +
                    "from file " + file.getName() + ".");
        }
    }
    
    public void flush(boolean saveInputBuffers) {}

    /**
     * returns the next Unicode char from input.
     *
     * @return the Unicode character read
     * @throws ExecutionException if it cannot read an Unicode char.
     */
    public char readUnicode() {
        try {
            if (reader == null) {
                reader = new PushBackReader(new FileReader(file));
            }
            int c = reader.read();
            if (c < 0) {
                throw new ExecutionException("Attempted to read a Unicode " +
                        "character from file " + file.getName() + "\n but the " +
                        " end of the file was reached.");
            }
            return (char) c;
        } catch (FileNotFoundException fne) {
            throw new ExecutionException("Attempted to read from file " +
                    file.getName() + " but it could not be found.");
        } catch (IOException ioe) {
            throw new ExecutionException("CPUSim was unable to read " +
                    "from file " + file.getName() + ".");
        }
    }

    /**
     * unreads one integer from the current output file.
     */
    public void unReadLong(){
        int c = reader.unread();
        while ( c !=-1 && !Character.isWhitespace(c)){
            c = reader.unread();
        }
    }

    /**
     * unreads one character from the current output file.
     */
    public void unReadOneChar(){
        reader.unread();
    }

    /**
     * writes the given long value to the output, preceded by a space
     *
     * @param value the long value to be output
     */
    public void writeLong(long value) {
        try {
            if (writer == null) {
            	writer = new PushBackWriter(file);
            }
            // Start it with a space char
            String longString = " " + value;  
            writer.write(longString);
            writer.flush();
        } catch (IOException ioe) {
            String message = "CPUSim was unable to write " +
                    "to file: " + file.getName();
            if (ioe.getMessage() != null) {
                message += " because: " + ioe.getMessage();
            }
            throw new ExecutionException(message);
        }
    }

    /**
     * writes the given long value to the output as an ASCII value
     *
     * @param longValue the long value to be output
     * @throws ExecutionException if the long is not an ASCII char
     */
    public void writeAscii(long longValue) {
        if (longValue > 255 || longValue < 0)
            throw new ExecutionException("Attempt to output the value " +
                    longValue + " as an ASCII value.");
        try {
            if (writer == null) {
                writer = new PushBackWriter(file);
            }
            writer.write((int) longValue);
            writer.flush();
        } catch (IOException ioe) {
            String message = "CPUSim was unable to write " +
                    "to file: " + file.getName();
            if (ioe.getMessage() != null) {
                message += " because: " + ioe.getMessage();
            }
            throw new ExecutionException(message);
        }
    }

    /**
     * writes the given long value to the output as a Unicode value
     *
     * @param longValue the long value to be output
     * @throws ExecutionException if the long is not an Unicode char
     */
    public void writeUnicode(long longValue) {
        if (longValue > 65535 || longValue < 0)
            throw new ExecutionException("Attempt to output the value " +
                    longValue + " as a Unicode value.");
        try {
            if (writer == null) {
                writer = new PushBackWriter(file);
            }
            writer.write((int) longValue);
            writer.flush();
        } catch (IOException ioe) {
            String message = "CPUSim was unable to write " +
                    "to file: " + file.getName();
            if (ioe.getMessage() != null) {
                message += " because: " + ioe.getMessage();
            }
            throw new ExecutionException(message);
        }
    }

    /**
     * unwrite one integer value from the output file
     */
    public void unWriteLong(){
        int c = writer.unwrite();
        while ( c !=-1 && !Character.isWhitespace(c)){
            c = writer.unwrite();
        }
    }

    /**
     * unwrite one character from the output file.
     */
    public void unWriteOneChar(){
        writer.unwrite();

    }

    /**
     * write the whole buffer to the destination file.
     */
    public void writeToFile(){
        try{
            writer.writeBufToFile();
            writer.flush();
        } catch (IOException ioe) {
            String message = "CPUSim was unable to write " +
                    "to file: " + file.getName();
            if (ioe.getMessage() != null) {
                message += " because: " + ioe.getMessage();
            }
            throw new ExecutionException(message);
        }
    }

    /**
     * Reset the file channel.
     */
        @Override
    public void reset() {
        // Close the file if it is open.
        try {
            if (reader != null) {
                reader.close();
                reader = null;
            }
            if (writer != null) {
                writer.close();  // This flushes it first
                writer = null;
            }
        } catch (IOException ioe) {
            System.out.println("IOException occurred when attempting to " +
                "reset the file: " + file + ".");
        }
    }

    /**
     * Gives the file of the fileChannel.
     * @return - the file of the fileChannel.
     */
    public File getFile() {
        return file;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return file.toString();
    }
    
    /**
     * getID is the same as toString(). It is included
     * only for backwards compatibility.
     * @return a String representation of the object.
     */
    public String getID() {
        return file.toString();
    }

}
