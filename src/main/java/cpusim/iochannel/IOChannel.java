/**
 * File: IOChannel
 * LastUpdate: August 2013
 */

/**
 * File: IOChannel
 * Last update: December 2013
 * Authors: Stephen Morse, Ian Tibbits, Terrence Tan
 * Class: CS 361
 * Project 7
 * 
 * added flushOutput(boolean saveInputBuffers):void method signature
 */

package cpusim.iochannel;

import cpusim.ExecutionException;
import cpusim.util.CPUSimConstants;

/**
 * Interface that any channels we want to use for input or output
 * must implement.
 */
public interface IOChannel extends CPUSimConstants {
	
    /**
     * returns the next integer from input as a long that fits in the given
     * number of bits.  If it doesn't fit, a NumberFormatException is thrown.
     *
     * @param numBits the number of bits into which the long must fit
     * @return the long value that was input
     * @throws ExecutionException if it cannot read a long.
     */
    long readLong(int numBits);

    /**
     * returns the next ASCII char from input.
     *
     * @return the ASCII character read
     * @throws ExecutionException if it cannot read an ASCII char.
     */
    char readAscii();

    /**
     * returns the next Unicode char from input.
     *
     * @return the Unicode character read
     * @throws ExecutionException if it cannot read an Unicode char.
     */
    char readUnicode();

    /**
     * writes the given long value to the output
     *
     * @param value the long value to be output
     */
    void writeLong(long value);

    /**
     * writes the given long value to the output as an ASCII value
     *
     * @param longValue the long value to be output
     * @throws ExecutionException if the long is not an ASCII char
     */
    void writeAscii(long longValue);

    /**
     * writes the given long value to the output as a Unicode value
     *
     * @param longValue the long value to be output
     * @throws ExecutionException if the long is not an Unicode char
     */
    void writeUnicode(long longValue);

    
    /**
     * Output any output values that hasn't already been
     * outputted.
     */
    void flushOutput();

    /** reset the input and output */
    void reset();

}
