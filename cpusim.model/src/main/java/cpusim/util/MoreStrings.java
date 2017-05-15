/**
 * 
 */
package cpusim.util;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 
 * @author Kevin Brightwell (Nava2)
 * @since 2016-10-11
 */
public abstract class MoreStrings {

	private MoreStrings() {
		// noop
	}
	
	/**
     * inserts 2 spaces in a string at every so many characters, working from
     * right to left.  For example, if the string is "123456789" and
     * characters are to be put in groups of 4,
     * then the output is "1 2345 6789".
     * 
     * @param origString    the original String
     * @param charGroupSize the number of consecutive characters in a group to be
     *                      separated from neighboring groups
     * @return the original string with spaces added
     *
     * @author Taylor Snook (12.03.06) (tasnook)
     * @author Kevin Brightwell (Nava2)
     *
     */
    public static CharSequence insertSpacesInString(CharSequence origString, int charGroupSize)
    {    	
    	final int EXPECTED_SIZE = (int)Math.ceil(1.0 * origString.length() / charGroupSize);
    	final List<CharSequence> buffer = Lists.newArrayListWithCapacity(EXPECTED_SIZE);
    	
    	// get the remainder of the division, this how many "extra" characters we have
    	final int rem = origString.length() % charGroupSize;
    	
    	if (rem > 0) {
	    	// append the first ones, then do the rest after
	    	buffer.add(origString.subSequence(0, rem));
    	}
    	    	
        for (int i = rem; i < origString.length(); i += charGroupSize) {
            //handles most of String
            buffer.add(origString.subSequence(i, i + charGroupSize));
        }
    
        // Use the joiner to join with a space, if there are extras this will still respect them while
        // not putting extras at the end
        return Joiner.on(' ').join(buffer);
    }

	/**
	 * Removes all whitespace ({@link CharMatcher#WHITESPACE}) from a {@link CharSequence}.
	 * 
	 * @author Taylor Snook (12.03.06) (tasnook)
	 * @author Kevin Brightwell (Nava2)
	 *
	 * @param origString the String from which whitespace is to be removed
	 * @return the original string but without whitespaces
	 * 
	 * @see CharMatcher#WHITESPACE
	 * @see CharMatcher#removeFrom(CharSequence)
	 * 
	 */
	public static String removeAllWhiteSpace(CharSequence origString)
	{
		return CharMatcher.WHITESPACE.removeFrom(origString);
	}

	/**
	 * Removes only the leading whitespace from the inputted string
	 * and returns the result.
	 *  
	 * @param s - The string to remove the leading whitespace from.
	 * @return - s with the leading whitespace removed.
	 * 
	 * @author Kevin Brightwell (Nava2)
	 */
	public static CharSequence removeLeadingWhitespace(CharSequence s) {
		checkNotNull(s);
		
		if (s.length() == 0) {
			return s;
		}
		
		int idx = CharMatcher.WHITESPACE.negate().indexIn(s);
		
		return s.subSequence(idx, s.length());
	}

	/**
	 * Capitalizes the first letter of the inputted string. 
	 * Leaves the rest of the string as it was.
	 * 
	 * @param s - The string to capitalize.
	 * @return - the first letter of the inputted string. 
	 * Leaves the rest of the string as it was.
	 * 
	 * @author Kevin Brightwell (Nava2)
	 */
	public static CharSequence capitalizeFirstLetter(CharSequence s) {
		checkNotNull(s);
		
		if (s.length() == 0) {
			return s;
		}
		
		return Character.toUpperCase(s.charAt(0)) + s.subSequence(1, s.length()).toString();
	}
}
