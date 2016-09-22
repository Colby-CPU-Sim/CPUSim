/**
 * 
 */
package cpusim.model.util.conversion;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import cpusim.model.Field;
import cpusim.model.Machine;
import cpusim.model.util.NamedObject;
import cpusim.model.util.units.ArchType;
import cpusim.model.util.units.ArchValue;

import com.google.common.collect.ImmutableList;

/**
 * 
 * @author Kevin Brightwell
 * @since 2016-10-11
 */
public abstract class ConvertStrings {

	private ConvertStrings() {
		// noop
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
    public static long from16WToLong(String s, ArchValue numBits)
            throws NumberFormatException
    {
        if (s.length() * 16 > numBits.as(ArchType.Bit))
            throw new NumberFormatException("Value won't fit in the given bits");

        long result = 0;
        for (int i = 0; i < s.length(); i++)
            result = (result << 16) | s.codePointAt(i);
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
    public static long toLong(String string)
            throws NumberFormatException
    {
        String trimmedString = string.trim();
        if (trimmedString.startsWith("'")) { //single Unicode character
            int c = trimmedString.codePointAt(1);
            return (long) c;
        }
        if (trimmedString.startsWith("+")) {
        	trimmedString = trimmedString.substring(1); //start just after the + sign
        }
        
        int radix = 10;
        boolean negative = false;
        if (trimmedString.codePointAt(0) == '-') {
        	negative = true;
        	trimmedString = trimmedString.substring(1);
        }
        
        if (trimmedString.codePointAt(0) == '0') {
        	// it's either binary, hex, or octal
        	int substrIdx = 2;
        	
        	if (trimmedString.codePointAt(1) == 'b') {
                radix = 2;
            } else if (trimmedString.codePointAt(1) == 'x') {
                radix = 16;
            } else {
            	// octal
            	radix = 8;
            	substrIdx = 1;
            }
        	
            trimmedString = trimmedString.substring(substrIdx);
        }
        
        if (negative) {
        	trimmedString = '-' + trimmedString;
        }
        
        return Long.parseLong(trimmedString, radix);
    }

	/**
	 * takes a format string and converts it into an array list of the proper fields
	 * @param format the format string to convert
	 * @param machine a pointer to the machine so it can access all the fields
	 * @return an array list of fields determined by the format string
	 * 
	 * @author Kevin Brightwell (Nava2)
	 */
	public static ImmutableList<Field> formatStringToFields(String format, Machine machine){
	    checkNotNull(machine);
		final String[] stringFields = checkNotNull(format).split("\\s"); // split on whitespace
	    
	    final Map<String, Field> fieldMap = NamedObject.toNamedMap(machine.getFields());
	    
	    final ImmutableList.Builder<Field> bld = ImmutableList.builder();
	    
	    Arrays.stream(stringFields).map(name -> {
	    	if (!fieldMap.containsKey(name)) {
	    		throw new IllegalStateException("Unknown field specified: " + name);
	    	}
	    	
	    	return fieldMap.get(name);
	    }).forEach(bld::add);
	    
	    return bld.build();
	}

	/**
	 * takes a format string and converts it into an array list of the proper fields
	 * @param instructionFields the format string to convert
	 * @return an array list of fields determined by the format string
	 * 
	 * @author Kevin Brightwell (Nava2)
	 */
	public static String fieldsToFormatString(Iterable<? extends Field> instructionFields){
	    return StreamSupport.stream(checkNotNull(instructionFields).spliterator(), false)
	    	.map(NamedObject::getName)
	    	.collect(Collectors.joining(" ")).trim();
	}
}
