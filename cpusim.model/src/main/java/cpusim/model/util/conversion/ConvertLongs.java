/**
 * 
 */
package cpusim.model.util.conversion;

import static com.google.common.base.Preconditions.checkNotNull;

import cpusim.model.util.units.ArchType;
import cpusim.model.util.units.ArchValue;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author Kevin Brightwell
 * @since 2016-09-20
 */
public abstract class ConvertLongs {

	private ConvertLongs() {
		// ignore, for no construction
	}
	
    public static char BOX_CHAR = '☐';

	//----------------------------
	//returns a string representing the long value
	// in two's complement using the given number of bits
	//It assumes that the value can be expressed in the given
	//number of bits.
	/**
	 * @deprecated Use {@link #to2sComplementString(long,ArchValue)} instead
	 */
	public static String fromLongToTwosComplementString(long value, int numBits)
	{
		return to2sComplementString(value, ArchValue.bits(numBits));
	}

	/**
     * Returns a string representing the long value
     * in two's complement using the given number of bits
     * It assumes that the value can be expressed in the given
     * number of bits.
     * 
     * @throws NullPointerException if <code>numBits</code> is <code>null</code>
     */
	public static String to2sComplementString(long value, ArchValue numBits)
	{
		checkNotNull(numBits);
		
		final String binStr = Long.toBinaryString(value);
		if (value < 0)
            return binStr.substring((int)(64 - numBits.as()));
        else {
            return StringUtils.leftPad(binStr, (int)numBits.as(), '0');
        }
	}

	/**
	 * Returns a string representing the long value
	 * in hexadecimal using the given number of bits
	 * @deprecated Use {@link #toHexString(long,ArchValue)} instead
	 */
	@Deprecated
	public static String fromLongToHexadecimalString(long value, int numBits) {
		return toHexString(value, ArchValue.bits(numBits));
	}

	/**
	 * Returns a string representing the long value
	 * in hexadecimal using the given number of bits
	 */
	public static String toHexString(long value, ArchValue numBits) {
		return Long.toHexString(value & ((1 << numBits.as()) - 1));
	}
	
	/**
	 * Returns a string representing the long value
	 * in hexadecimal using the given number of bits
	 * 
	 * @deprecated Use {@link #toHexString(long, ArchValue)}
	 */
	public static String toHexString(long value, int numBits) {
		return toHexString(value, ArchValue.bits(numBits));
	}

	/**
	 * Converts a long to its unsigned int string representation
	 *
	 * @param l       the long to convert
	 * @param numBits the number of bits in the long
	 * @return the unsigned int string
	 * @deprecated Use {@link #toUString(long,ArchValue)} instead
	 */
	public static String fromLongToUnsignedDecString(long l, int numBits) {
		return toUString(l, ArchValue.bits(numBits));
	}

	/**
	 * Converts a long to its unsigned int string representation
	 *
	 * @param l       the long to convert
	 * @param numBits the number of bits in the long
	 * @return the unsigned int string
	 * 
	 * @throws NullPointerException if <code>numBits</code> is <code>null</code>
	 * 
	 * @see Long#toUnsignedString(long)
	 */
	public static String toUString(long l, ArchValue numBits)
	{
	    return Long.toUnsignedString(l & checkNotNull(numBits).mask(), 10);
	}

	/**
	 * Converts a long to its unicode string representation
	 *
	 * @param l       the long to convert
	 * @param numBits the number of bits in the long
	 * @return the Unicode String representation of the long
	 * @deprecated Use {@link #to16WString(long,ArchValue)} instead
	 */
	public static String fromLongToUnicodeString(long l, int numBits)
	{
		return to16WString(l, ArchValue.bits(numBits));
	}

	/**
	 * Converts a long to a 16-bit wide character string where each code point is the 16 but number. 
	 *
	 * @param value       the long to convert
	 * @param numBits the number of bits in the long
	 * @return the 16-bit wide character String representation of the long
	 */
	public static String to16WString(long value, ArchValue numBits)
	{
		final StringBuffer sb = new StringBuffer();
        final long mask16 = ArchType.Bit.getMask(16);
        
        long vvalue = value;
        
        for (int i = 0; i < Math.ceil(numBits.as() / 16.0); i++) {
            sb.append(Character.toChars((int)(vvalue & mask16)));
            vvalue = vvalue >> 16;
        }
        
        return sb.reverse().toString().toUpperCase();
	}

	//------------------------------
	//
	/**
	 * treats the long l as numBits bits and groups the bits into groups of 8
	 * and converts each group to the corresponding ascii character.  If the character
	 * is unprintable (has an ascii value between 0 and 31), a box is printed out.
	 *
	 * @param l the long to be converted to an ascii string
	 * @param numBits number of bits of the long to be converted, starting at the right
	 * @return the String with the ascii characters
	 * @deprecated Use {@link #toAsciiString(long,ArchValue)} instead
	 */
	public static String fromLongToAsciiString(long value, int numBits)
	{
		return toAsciiString(value, ArchValue.bits(numBits));
	}

	//------------------------------
	//
	/**
	 * treats the long l as numBits bits and groups the bits into groups of 8
	 * and converts each group to the corresponding ascii character.  If the character
	 * is unprintable (has an ascii value between 0 and 31), a box is printed out.
	 *
	 * @param l the long to be converted to an ascii string
	 * @param numBits number of bits of the long to be converted, starting at the right
	 * @return the String with the ascii characters
	 */
	public static String toAsciiString(long value, ArchValue numBits)
	{
		final ArchValue bytesVal = numBits.convert(ArchType.Byte);
        final StringBuilder resBld = new StringBuilder((int)bytesVal.getValue());
        
        long vvalue = value;
        long vmask = bytesVal.mask();
        
        for(int i = 0; i < bytesVal.getValue(); i++) {
            //ASCII chars 0-31 are control characters,
            //these are unprintable and should show a box instead
            //The box is the way the DELETE char is displayed
            final long v_and_255 = (vvalue & vmask) & 255;
            if(v_and_255 < 32) {
                resBld.append(BOX_CHAR);
            } else {
                /* ascii character is printable */
                resBld.append((char) v_and_255);
            }
            
            vvalue = vvalue >>> 8;
            vmask = vmask >>> 8;
        }
        
        return resBld.reverse().toString();
	}
	
	
}
