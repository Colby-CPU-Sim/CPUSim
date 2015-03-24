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

package cpusim.util;

import cpusim.Field;
import cpusim.Machine;
import cpusim.assembler.AssemblyException;
import cpusim.assembler.Token;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import javafx.scene.input.KeyCode;

/**
 * This class contains a collection of static conversion methods, mostly for converting
 * integers to and from strings in different bases
 */

public class Convert
{

    public static String tokenContentsToStringWithEscapes(Token t) throws AssemblyException.InvalidOperandError {
        String result = "";
        boolean escaping = false;
        for (char c : t.contents.toCharArray()){
            if (c == '\\' && !escaping){
                escaping = true;
            }
            else if (c == '\\' && escaping){
                result += '\\';
                escaping = false;
            }
            else if (c == 'n' && escaping){
                result += '\n';
                escaping = false;
            }
            else if (c == 't' && escaping){
                result += '\t';
                escaping = false;
            }
            else if (c == 'r' && escaping){
                result += '\r';
                escaping = false;
            }
            else if (c == '"' && escaping){
                result += '"';
                escaping = false;
            }
            else if (escaping){
                throw new AssemblyException.InvalidOperandError("The ascii string "+t.contents+" has the "
                        + "escape character (\"\\\") before an the invalid character \'"+c+
                        "\'.  Valid characters to escape from are \'\\\', \'n\', \'r\', \'t\', and \'\"\'"
                        + "for backslash, new line, carraige return, tab, and quotation respectively.", t);
            }
            else{
                result += c;
            }
        }
        return result;
    }
    /**
     *     private constructor since we don't want any objects created in this class
     */
    private Convert() {}

    /**
     * converts a list of integers into a string of the integers separated by spaces
     * @param fieldLengths the array of integers to be converted
     * @return the string with the integers separated by spaces
     */
    public static String lengths2Format(int[] fieldLengths)
    {
        String format = "";
        for (int length : fieldLengths)
            format += length + " ";
        return format.trim();
    }


    //~~~~~~~~~~~~ long <==> byte array ~~~~~~~~~~~~~
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //------------------------
    //byteArrayToLong
    //pre: bytes.length <= 8 and each value is -128 to 127.
    //Converts bits into a long, assuming that bytes[0] is the most significant
    // byte and assuming that the value stored in bytes is in 2's complement.
    //This implementation uses BigIntegers to do the conversion for it.
    public static long fromByteArrayToLong(byte[] bytes)
    {
        assert bytes.length <= 8 : "Tried to convert an array of more than 8 " +
                "bytes into a long using Convert.byteArrayToLong.";
        BigInteger bigLong = new BigInteger(bytes);
        return bigLong.longValue();
    }

    //------------------------
    //byteArrayToLong2
    //pre: bytes.length <= 8 and each value is -128 to 127.
    //Converts bits into a long, assuming that bytes[0] is the most significant
    // byte and assuming that the value stored in bytes is in 2's complement.
    public static long fromByteArrayToLong2(byte[] bytes)
    {
        assert bytes.length <= 8 : "Tried to convert an array of more than 8 " +
                "bytes into a long using Convert.byteArrayToLong.";

        //Temporarily change the first bit to 0 to avoid long overflow.
        //Is it necessary to temporarily change the first bit to 0?
        boolean neg = (bytes[0] < 0);
        long value = bytes[0] & 127;
        for (int i = 1; i < bytes.length; i++)
            value = (value << 8) | (bytes[i] & 255);
        if (neg)
            value |= Long.MIN_VALUE; //put the 1 back into the leftmost bit
        return value;
    }

    //------------------------
    //longToByteArray
    //Converts a long to an array of the given number of bytes, where index 0
    //is the most significant bit and where the value is in 2's complement.
    public static byte[] fromLongToByteArray(long value, int numBytes)
    {
        //Alternate implementation:
        //	Use BigInteger class methods

        byte[] byteArray = new byte[numBytes];
        for (int i = numBytes - 1; i >= 0; i--) {
            byteArray[i] = (byte) (value & 255);  // use only the rightmost 8 bits
            value = value >> 8;  // remove the rightmost 8 bits
        }
        return byteArray;
    }

    //~~~~~~~~~~~~ long ==> String ~~~~~~~~~~~~~~~~~
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //----------------------------
    //returns a string representing the long value
    // in two's complement using the given number of bits
    //It assumes that the value can be expressed in the given
    //number of bits.

    public static String fromLongToTwosComplementString(long value, int numBits)
    {
        String stringValue;
        if (value < 0)
            stringValue =
                    Long.toBinaryString(value).substring(64 - numBits);
        else {
            stringValue = Long.toBinaryString(value);
            int length = stringValue.length();
            for (int i = 0; i < numBits - length; i++)
                stringValue = "0" + stringValue;
        }
        return stringValue;
    }

    //----------------------------
    //returns a string representing the long value
    // in hexadecimal using the given number of bits

    public static String fromLongToHexadecimalString(long value, int numBits)
    {
        if (value < 0 && numBits == 64) {
            //return Long.toString(value, 16).toUpperCase();
            String left = Convert.fromLongToHexadecimalString(value >>> 32, 32);
            String right = Convert.fromLongToHexadecimalString(
                                     value & Integer.MAX_VALUE, 32);
            return left + right;
        }
        if (value < 0)
            value += powerOfTwo(numBits);
        String stringValue = Long.toString(value, 16);
        int length = stringValue.length();
        for (int i = 0; i < numBits - 4 * length; i += 4)
            stringValue = "0" + stringValue;
        return stringValue.toUpperCase();
    }

    /**
     * Converts a long to its unsigned int string representation
     *
     * @param l       the long to convert
     * @param numBits the number of bits in the long
     * @return the unsigned int string
     */
    public static String fromLongToUnsignedDecString(long l, int numBits)
    {
        if (l < 0) {
            l += powerOfTwo(numBits);
        }
        return Long.toString(l, 10);
    }

    /**
     * Converts a long to its unicode string representation
     *
     * @param l       the long to convert
     * @param numBits the number of bits in the long
     * @return the Unicode String representation of the long
     */
    public static String fromLongToUnicodeString(long l, int numBits)
    {
        String result = "";
        for (int i = 0; i < numBits / 16; i++) {
            char c = (char) (l & 65535);
            l = l >>> 16;
            result = c + result;
        }
        return result;
    }

    //------------------------------
    //
    /**
     * treats the long l as numBits bits and groups the bits into groups of 8
     * and converts each group to the corresponding ascii character.  If the character
     * is unprintable (has an ascii value between 0 and 31), a box or Â˜ is printed out
     * instead of blank space.
     * numBits should be either 0, 8, 16, 24, 32, 40, 48, 54, or 64.
     * @param l the long to be converted to an ascii string
     * @param numBits number of bits in the long
     * @return 
     */
    public static String fromLongToAsciiString(long l, int numBits)
    {
        String result = "";
        for( int i = 0; i < numBits/8; i++) {
            //ASCII chars 0-31 are control characters,
            //these are unprintable and should show a â˜� instead
            //The box is the way the DELETE char is displayed
            if( (l&255)<32){
                result = "Â˜" + result; //there is a DELETE char inside the quotes
            }
            
            else /* ascii character is printable */ {
                char c = (char) (l & 255);
                result = c + result;
            }
            
            l = l >>> 8;
        }
        return result;
    }

    //----------------------------
    //returns the value of 2 raised to the given power
    //precondition:  power >= 0

    public static long powerOfTwo(int power)
    {
        long result = 1;
        for (int i = 1; i <= power; i++)
            result *= 2;
        return result;
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
    public static long fromDecimalStringToLong(String s, int numBits) {
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
        BigInteger bigNum = new BigInteger(s, 2);
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
        BigInteger bigNum = new BigInteger(s, 16);
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
     * @param numBits numBits the number of bits availible for the long
     * @return The long value of the given String
     * @throws NumberFormatException The given string exceeded the availible bits
     *                               for the long.
     */
    public static long fromUnsignedDecStringToLong(String s, int numBits)
            throws NumberFormatException
    {
        BigInteger bigNum = null;

        try {
            bigNum = new BigInteger(s);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("The input \"" + s + "\" is " +
                    "not a valid integer.");
        }
        if (bigNum.compareTo((new BigInteger("2")).pow(numBits)) < 0) {
            return bigNum.longValue();
        } else { //number is out of range
            throw new NumberFormatException("The number " + s +
                    " won't fit in " + numBits + " bits.");
        }
    }

    //------------------------
    //fromAsciiStringToLong
    //returns the long number whose 64 bits are chosen from the chars in s.
    //The 8 ASCII bits of each char are concatenated to form the long number,
    //which is padded with 0's on the left, if necessary, to get 64 bits.
    //s should have at most numBits/8 characters in it.
    public static long fromAsciiStringToLong(String s, int numBits)
            throws NumberFormatException
    {
        if( s.length() * 8 > numBits)
            throw new NumberFormatException("value won't fit in the given bits");

        long result = 0;
        for( int i = 0; i < s.length(); i++ )
            result = (result << 8) + (int) s.charAt(i);
        return result;
    }

    /**
     * Converts a Unicode String to a long.
     *
     * @param s       the string to convert to a long
     * @param numBits the number of bits availible for the long
     * @return The long value of the given String
     * @throws NumberFormatException The given string exceeded the availible bits
     *                               for the long.
     */
    public static long fromUnicodeStringToLong(String s, int numBits)
            throws NumberFormatException
    {
        if (s.length() * 16 > numBits)
            throw new NumberFormatException("value won't fit in the given bits");

        long result = 0;
        for (int i = 0; i < s.length(); i++)
            result = (result << 16) + s.codePointAt(i);
        return result;
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
     */
    public static long fromAnyBaseStringToLong(String string)
            throws NumberFormatException
    {
        string = string.trim();
        if (string.startsWith("'")) { //single Unicode character
            char c = string.charAt(1);
            return (long) c;
        }
        if (string.startsWith("+")) {
            string = string.substring(1); //start just after the + sign
        }
        int radix = 10;
        if (string.startsWith("0b")) {
            radix = 2;
            string = string.substring(2);
        } else if (string.startsWith("-0b")) {
            radix = 2;
            string = "-" + string.substring(3);
        } else if (string.startsWith("0x")) {
            radix = 16;
            string = string.substring(2);
        } else if (string.startsWith("-0x")) {
            radix = 16;
            string = "-" + string.substring(3);
        }
        return Long.parseLong(string, radix);
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
     */
    public static String insertSpacesInString(String origString,
                                              int charGroupSize)
    {
        String builder = "";
        for (int i = origString.length(); i > 0; i -= charGroupSize) {
            //handles most of String
            if (i > charGroupSize) {
                builder = "  " + origString.substring(i-charGroupSize, i) +
                        builder;
            }
            //handles end of String
            else
                builder = origString.substring(0, i) + builder;
        }
        return builder;
    }

    /**
     * removes all whitespace from a String
     *  author: Taylor Snook (12.03.06)(tasnook)
     *
     * @param origString the String from which whitespace is to be removed
     * @return the original string but without whitespaces
     */
    public static String removeAllWhiteSpace(String origString)
    {
        //Create a newString and a char array from the original string
        String newString = "";
        char[] arrayOrigString = origString.toCharArray();

        //step through the array & only add characters that aren't whitespace
        int i;
        for (i = 0; i < origString.length(); i++) {
            if (!Character.isWhitespace(arrayOrigString[i]))
                newString += arrayOrigString[i];
        }

        //return new String without whitespace
        return newString;
    }
    
    /**
     * takes a format string and converts it into an array list of the proper fields
     * @param format the format string to convert
     * @param machine a pointer to the machine so it can access all the fields
     * @return an array list of fields determined by the format string
     */
    public static ArrayList<Field> formatStringToFields(String format, Machine machine){
        String[] stringFields = format.split(" ");
        ArrayList<Field> assemblyFormat = new ArrayList<>();
        for (String field1 : stringFields){
            for (Field field2 : machine.getFields()){
                if(field1.equals(field2.getName())){
                    assemblyFormat.add(field2);
                }
            }
        }
        return assemblyFormat;
    }
    
    /**
     * takes a format string and converts it into an array list of the proper fields
     * @param fields the format string to convert
     * @return an array list of fields determined by the format string
     */
    public static String fieldsToFormatString(ArrayList<Field> fields){
        String format = "";
        for (Field field: fields){
            format += field.getName()+" ";
        }
        return format.trim();
    }
    
    /**
     * generates two separate lists of random colors that have the same contents
     * @param length the desired length of the two lists
     * @return an ArrayList with that contains two separate ArrayLists of random light
     * colors
     */
    public static ArrayList<ArrayList<String>> generateTwoListsOfRandomLightColors(int length){
        ArrayList<String> colors1 = new ArrayList<String>();
        ArrayList<String> colors2 = new ArrayList<String>();
        for (int i=0; i<length; i++){
            String randomColor = generateRandomLightColor();
            while (colors1.contains(randomColor)){
                randomColor = generateRandomLightColor();
            }
            colors1.add(randomColor);
            colors2.add(randomColor);
        }
        ArrayList<ArrayList<String>> twoColorLists = new ArrayList<ArrayList<String>>();
        twoColorLists.add(colors1);
        twoColorLists.add(colors2);
        return twoColorLists;
    }
    
    /**
     * Generates a hexadecimal string representing an rgb value that is a random light color
     * @return hexadecimal string representation of a random light color
     */
    public static String generateRandomLightColor(){
        Random rand = new Random();
        String colorString = "#";
        for (int i=0; i<3; i++){
            colorString += Integer.toHexString(rand.nextInt(127)+128);
        }
        return colorString;
    }
    
    /**
     * turns an array of Strings that represent colors into one string with each color
     * separated by a space
     * @param colors an ArrayList of string representations of colors
     * @return a single string that is all the strings in the array list separated by a space
     */
    public static String colorsListToXML(ArrayList<String> colors){
        String returnString = "";
        for (String string : colors){
            returnString += string+" ";
        }
        return returnString.trim();
    }
    
    /**
     * takes an XML representation of a bunch of strings and converts them into an 
     * ArrayList of strings
     * @param xmlString one string of colors in xml form (one space between each string)
     * @return and ArrayList of Strings that were separated by a space in the xml string
     */
    public static ArrayList<String> xmlToColorsList(String xmlString){
        String[] colorArray = xmlString.split(" ");
        ArrayList<String> colors = new ArrayList<>();
        for(String string : colorArray){
            colors.add(string);
        }
        return colors;
    }
    
    /**
     * Converts a textual representation of a KeyCode into an actual KeyCode object
     * @param text textual representation of a KeyCode 
     * @return the KeyCode that the text represents
     */
    public static KeyCode charToKeyCode(String text) {
        if (text.equals("Close_Bracket")){
            return KeyCode.CLOSE_BRACKET;
        }
        else if (text.equals("Open_Bracket")){
            return KeyCode.OPEN_BRACKET;
        }
        else if (text.equals("Caps")){
            return KeyCode.CAPS;
        }
        else if (text.equals("Page_Up")){
            return KeyCode.PAGE_UP;
        }
        else if (text.equals("Page_Down")){
            return KeyCode.PAGE_DOWN;
        }
        else if (text.equals("Back_Slash")){
            return KeyCode.BACK_SLASH;
        }
        else if (text.equals("Context_Menu")){
            return KeyCode.CONTEXT_MENU;    
        }
        return null;
    }
    
    /**
     * Removes only the leading whitespace from the inputted string
     * and returns the result.
     *  
     * @param s - The string to remove the leading whitespace from.
     * @return - s with the leading whitespace removed.
     */
	public static String removeLeadingWhitespace(String s) {
		if (s == null || s.equals("")) {
			return s;
		}
		
		while(Character.isWhitespace(s.charAt(0))){
			s = s.substring(1);
		}
		return s;
	}
	
	/**
	 * Capitalizes the first letter of the inputted string. 
	 * Leaves the rest of the string as it was.
	 * 
	 * @param s - The string to capitalize.
	 * @return - the first letter of the inputted string. 
	 * Leaves the rest of the string as it was.
	 */
	public static String capitalizeFirstLetter(String s) {
		if (s == null || s.equals("")) {
			return s;
		} else {
			 String retString = s.substring(0, 1).toUpperCase() +
					s.substring(1);
			return retString;
		}
	}

}
