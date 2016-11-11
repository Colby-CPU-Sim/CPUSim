///////////////////////////////////////////////////////////////////////////////
//  File:    	Convert.java
//  Type:    	java application file
//  Author:		Dale Skrien
//  Project: 	CPU Sim
//  Date:    	June, 2000 (created)
//              December, 2005 (modified)
//
//  Description:
//   This file contains a collection of static methods for converting:
//   1. numbers between longs, bit arrays, and strings displaying the value in
//		decimal, hex, and binary using a specified number of bits,
//	 2.	arrays to vectors.
//
//  Note:  The Long class provides static methods that do some of this work for
//  us, as mentioned below.
//
//	Things to do:
//	1.  !!!! Use java.util.BitSet to implement these?
//  2.	!!!! Use java.math.BigInteger?  Then can use more than 64 bits per register.
//	3.  Use a byte array instead of int array so that it can be easily converted
//		to a string?
//	4.  Also check out the NumberFormat class for converting to Strings
//
///////////////////////////////////////////////////////////////////////////////

/*
 * Michael Goldenberg, Ben Borchard, and Jinghui Yu made the following changes in 11/6/13
 * 
 * 1.) Modified listOfRandomLightColors() method so that it generates two seperate lists
 * of the same random light colors.  We then renamed it to generateTwoListsOfRandomLightColors()
 * 2.) We also noticed there were no javadoc comments on the last few methods so we added those
 * in
 * 
 * 
 */

/**
 * File: Convert.java
 * Last update: December 2013
 * Authors: Stephen Morse, Ian Tibbits, Terrence Tan
 * Class: CS 361
 * Project 7
 * 
 * add removeLeadingWhitespace(String s):String for use in BufferedChannel�s getLong
 * add capitalizeFirstLetter method
 * remove fitsInBits and checkFitsInBits
 */

package cpusim.model.util;

import java.math.BigInteger;
import java.util.List;

import cpusim.model.Field;
import cpusim.model.Machine;
import cpusim.model.assembler.Assembler;
import cpusim.model.assembler.AssemblyException;
import cpusim.model.assembler.Token;
import cpusim.model.util.conversion.ConvertStrings;
import cpusim.model.util.units.ArchType;
import cpusim.model.util.units.ArchValue;

/**
 * This class contains a collection of static conversion methods, mostly for converting
 * integers to and from strings in different bases
 * 
 * @since 2013-12-01
 */
public abstract class Convert
{
	/**
     * private constructor since we don't want any objects created in this class
     */
    private Convert() {
    	// noop
    }

    //~~~~~~~~~~~~ String ==> long ~~~~~~~~~~~~~~~~~
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * converts a string into a long value.  The String must consist of
     * an optional + or - followed by one or more digits.  Furthermore, the
     * value must fit in the given number of bits in 2's complement notation.
     * @param s the String to be converted
     * @param numBits the number of bits in which the value must fit
     * @return the long value
     * @throws NumberFormatException if the String does not parse
     *         as a positive or negative integer, or doesn't fit in numbits.
     */
    public static long fromDecimalStringToLong(String s, ArchValue numBits) {
        BigInteger bigNum;

        try {
            bigNum = new BigInteger(s);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("The input \"" + s + "\" is " +
                    "not a valid integer.");
        }
        
        try {
			Validate.fitsInBits(bigNum.longValue(), numBits);
		} catch (ValidationException e) {
			throw new NumberFormatException("The number " + bigNum.longValue() +
                    " won't fit in " + numBits + " bits.");
		}
        return bigNum.longValue();
    }
    
    /**
     * Delegate to {@link #fromDecimalStringToLong(String, ArchValue)}.
     *
     * @param s
     * @param numBits
     * @return
     *
     * @deprecated Use {@link #fromDecimalStringToLong(String, ArchValue)}
     */
    public static long fromDecimalStringToLong(String s, int numBits) {
        return fromDecimalStringToLong(s, ArchValue.bits(numBits));
    }
    
    //------------------------
    //fromBinaryStringToLong
    //returns the long number expressed by s, where s is a 2's complement number.
    //If s has fewer than numBits digits,
    //then it is considered to be positive and have leading 0's.
    //It assumes that numBits <= 64.
    public static long fromBinaryStringToLong(String s, int numBits)
            throws NumberFormatException
    {
        if (s.charAt(0) == '-')
            throw new NumberFormatException
                    ("Negative signs are not allowed in binary");
        BigInteger bigNum;
        try {
            bigNum = new BigInteger(s, 2);
        } catch (Exception e) {
            throw new NumberFormatException("Not a valid binary number");
        }
        if (bigNum.compareTo((new BigInteger("2")).pow(numBits - 1)) < 0)
            return bigNum.longValue();
        else if (bigNum.compareTo((new BigInteger("2")).pow(numBits)) < 0)
            return bigNum.subtract((new BigInteger("2")).pow(numBits)).longValue();
        else //number is out of range
            throw new NumberFormatException("value won't fit in the given bits");
    }

    //------------------------
    //fromHexadecimalStringToLong
    //returns the long number expressed by s, where s is treated as a hex number
    //in 2's complement form using the given number of bits.
    //It assumes that numBits <= 64.
    public static long fromHexadecimalStringToLong(String s, int numBits)
            throws NumberFormatException
    {
        if (s.charAt(0) == '-')
            throw new NumberFormatException(
                    "Negative signs are not allowed in hex in CPU Sim.");
        BigInteger bigNum = null;
        try {
            bigNum = new BigInteger(s, 16);
        } catch (Exception e) {
            throw new NumberFormatException("Not a valid hex value");
        }
        if (bigNum.compareTo((new BigInteger("2")).pow(numBits - 1)) < 0)
            return bigNum.longValue();
        else if (bigNum.compareTo((new BigInteger("2")).pow(numBits)) < 0)
            return bigNum.subtract(
                                (new BigInteger("2")).pow(numBits)).longValue();
        else //number is out of range
            throw new NumberFormatException("value won't fit in the given bits");
    }

    /**
     * Converts a String representing an unsigned int to a long.
     *
     * @param s       the string to convert to a long
     * @param numBits numBits the number of bits available for the long
     * @return The long value of the given String
     * @throws NumberFormatException The given string is not an unsigned
     *         int that fits in the given number of bits.
     */
    public static long fromUnsignedDecStringToLong(String s, int numBits)
            throws NumberFormatException
    {
        BigInteger bigNum = null;

        try {
            bigNum = new BigInteger(s);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("The value \"" + s + "\" is " +
                    "not a valid integer.");
        }
        if (bigNum.compareTo(new BigInteger("0")) < 0)
            throw new NumberFormatException("The number cannot be negative");
        else if (bigNum.compareTo((new BigInteger("2")).pow(numBits)) < 0)
                return bigNum.longValue();
        else { //number is out of range
            throw new NumberFormatException("The number " + s +
                    " won't fit in " + numBits + " bits.");
        }
    }

    /**
     * Converts an ASCII String to a long using the given number of bits.
     * Each character in the String is converted to its 8-bit integer value and
     * the 8-bit values are appended together.
     * If numBits is not a multiple of 8, then the first char in s must fit
     * in numBits % 8 bits.
     * After all the characters have been converted, zeros are added to the
     * left of the result, as necessary, to make a 64-bit long value.
     * If any of the chars are "☐", then the corresponding 8 bits in the defaultLong
     * are used instead of the value of "☐".
     *
     * @param s       the string to convert to a long
     * @param numBits the number of bits in which the string must fit (a value <= 64)
     * @param defaultLong the default bits to use if the char is "☐"
     * @return The long value of the given String
     * @throws NumberFormatException The given string exceeded the available bits
     *                               for the long or any of the chars are not ASCII
     */
    public static long fromAsciiStringToLong(String s, int numBits, long defaultLong)
            throws NumberFormatException
    {
        if( numBits % 8 == 0 && numBits/8 < s.length() ||
                numBits % 8 > 0 && numBits/8 < s.length()-1)
            throw new NumberFormatException("The string is too long for the given bits");
        s = removeBoxesFrom(s, defaultLong);
        if( numBits % 8 != 0 && Math.pow(2, numBits % 8) <= (int) s.charAt(0))
            throw new NumberFormatException("The first char won't fit in " + (numBits % 8) +
                                            " bits");

        long result = 0;
        for( int i = 0; i < s.length(); i++ ) {
            if ((int) s.charAt(i) > 255)
                throw new NumberFormatException("There are non-ASCII characters in the string");
            result = (result << 8) + (int) s.charAt(i);
        }
        return result;
    }

    /**
     * returns a string that is the same as s except every box character "☐" is replaced
     * by the integer formed from the corresponding 8 bits from the default long.
     * It assumes the string s has at most 8 characters.
     * @param s the string from which the boxes are to be removed
     * @param defaultLong the 64-bit value whose bits are to be substituted for the boxes
     * @return the new string
     */
    private static String removeBoxesFrom(String s, long defaultLong) {
        String result = "";
        for(int i = s.length()-1; i >= 0; i--) {
            if(s.charAt(i) == '☐')
                result = (char) (defaultLong&255) + result;
            else
                result = s.charAt(i) + result;
            defaultLong <<= 8;
        }
        return result;
    }

    /**
     * Converts a Unicode String to a long.
     *
     * @param s       the string to convert to a long
     * @param numBits the number of bits available for the long
     * @return The long value of the given String
     * @throws NumberFormatException The given string exceeded the available bits
     *                               for the long.
     */
    @Deprecated
    public static long fromUnicodeStringToLong(String s, int numBits)
            throws NumberFormatException
    {
        return ConvertStrings.from16WToLong(s, ArchType.Bit.of(numBits));
    }

    /**
     * converts the given string into a long value.  It can be a character
     * inside single quotes, in which case the Unicode value of the character
     * is returned.  Otherwise, it is parsed as an integer literal.
     * It is allowed to have spaces and one optional "+" or "-" in front.
     * If the string starts with "0b" it is parsed in base 2.
     * If the string starts with "0x" it is parsed in base 16.
     * Otherwise it is parse in base 10.
     * @param string the String to be parsed as a long
     * @return the long value of the string
     * @throws NumberFormatException if the string cannot be parsed as a long
     * 
     * @see ConvertStrings#toLong(String)
     */
    @Deprecated
    public static long fromAnyBaseStringToLong(String string)
            throws NumberFormatException
    {
        return ConvertStrings.toLong(string);
    }

    /**
     * inserts 2 spaces in a string at every so many characters, working from
     * right to left.  For example, if the string is "123456789" and
     * characters are to be put in groups of 4,
     * then the output is "1  2345  6789".
     * author: Taylor Snook (12.03.06) (tasnook)
     *
     * @param origString    the original String
     * @param charGroupSize the number of consecutive characters in a group to be
     *                      separated from neighboring groups
     * @return the original string with spaces added
     * 
     * @deprecated Used {@link MoreStrings#insertSpacesInString(CharSequence, int)} instead
     */
    @Deprecated
    public static String insertSpacesInString(String origString,
                                              int charGroupSize)
    {
    	return MoreStrings.insertSpacesInString(origString, charGroupSize).toString();
    }

    /**
	 * removes all whitespace from a String
	 *  author: Taylor Snook (12.03.06)(tasnook)
	 *
	 * @param origString the String from which whitespace is to be removed
	 * @return the original string but without whitespaces
	 * 
	 * @deprecated Use {@link MoreStrings#removeAllWhiteSpace(String)} instead
	 */
    @Deprecated
	public static String removeAllWhiteSpace(String origString)
	{
		return MoreStrings.removeAllWhiteSpace(origString);
	}
    
	/**
	 * takes a format string and converts it into an array list of the proper fields
	 * @param format the format string to convert
	 * @param machine a pointer to the machine so it can access all the fields
	 * @return an array list of fields determined by the format string
	 * @deprecated Use {@link ConvertStrings#formatStringToFields(String,Machine)} instead
	 */
	public static List<Field> formatStringToFields(String format, Machine machine){
		return ConvertStrings.formatStringToFields(format, machine);
	}
    
    /**
	 * takes a format string and converts it into an array list of the proper fields
	 * @param instructionFields the format string to convert
	 * @return an array list of fields determined by the format string
	 * 
	 * @deprecated Use {@link ConvertString#fieldsToFormatString(Iterable<? extends Field>)} instead
	 */
	public static String fieldsToFormatString(Iterable<? extends Field> instructionFields){
		return ConvertStrings.fieldsToFormatString(instructionFields);
	}
    
    /**
	 * Removes only the leading whitespace from the inputted string
	 * and returns the result.
	 *  
	 * @param s - The string to remove the leading whitespace from.
	 * @return - s with the leading whitespace removed.
	 * @deprecated Use {@link MoreStrings#removeLeadingWhitespace(CharSequence)} instead
	 */
	public static String removeLeadingWhitespace(String s) {
		return MoreStrings.removeLeadingWhitespace(s).toString();
	}
	
	/**
	 * Capitalizes the first letter of the inputted string. 
	 * Leaves the rest of the string as it was.
	 * 
	 * @param s - The string to capitalize.
	 * @return - the first letter of the inputted string. 
	 * Leaves the rest of the string as it was.
	 * @deprecated Use {@link MoreStrings#capitalizeFirstLetter(CharSequence)} instead
	 */
	public static String capitalizeFirstLetter(String s) {
		return MoreStrings.capitalizeFirstLetter(s).toString();
	}

}
