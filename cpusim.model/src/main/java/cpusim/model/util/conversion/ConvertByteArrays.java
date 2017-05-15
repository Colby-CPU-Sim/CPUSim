/**
 * 
 */
package cpusim.model.util.conversion;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigInteger;

import cpusim.model.util.units.ArchType;
import cpusim.model.util.units.ArchValue;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Provides conversion utilities for <code>byte[]</code> values.
 * 
 * @author Kevin Brightwell
 * @since 2016-09-20
 */
public abstract class ConvertByteArrays {

	private ConvertByteArrays() {
		// No construction
	}

	/**
	 * @deprecated Use {@link #fromLong(long,ArchValue)} instead
	 */
	public static byte[] fromLong(long inValue, int numBytes)
	{
		return fromLong(inValue, ArchType.Byte.of(numBytes));
	}

	/**
	 * Converts a long to an array of the given number of bytes, where index 0 is the most significant bit and where the 
	 * value is in 2's complement.
	 * 
	 * @param inValue
	 * @param numBytes
	 * @return Byte array representation of <code>inValue</code> of width <code>numBytes</code>
	 */
	public static byte[] fromLong(long inValue, ArchValue numBytes)
	{	
		final byte[] outArr = numBytes.newByteArray();
		final byte[] biArr = BigInteger.valueOf(inValue).toByteArray();
		
		checkArgument(outArr.length >= biArr.length, 
	            "Invalid number of bytes specified for input, %s, required at least %s", 
	            numBytes, Integer.valueOf(biArr.length));
	        
		
		System.arraycopy(biArr, 0, outArr, 0, biArr.length);
        ArrayUtils.reverse(outArr);
        
        return outArr;
	}

	/** 
	 * Converts bits into a long, assuming that bytes[0] is the most significant
	 * byte and assuming that the value stored in bytes is in 2's complement.
	 * 
	 * @throws IllegalArgumentException if bytes.length > 8.
	 */
	public static long toLong2(byte[] bytes)
	{
		checkArgument(bytes.length <= 8, "Tried to convert an array of more than 8 bytes into a long");
	    
	    // Temporarily change the first bit to 0 to avoid long overflow.
	    // Is it necessary to temporarily change the first bit to 0?
	    final boolean neg = bytes[0] < 0;
	    long value = bytes[0] & 127;
	    
	    for (int i = 1; i < bytes.length; i++) {
	        value = (value << 8) | ((long)bytes[i] & 255);    
	    }
	    
	    if (neg) {
	        // put the 1 back into the leftmost bit
	        return value | Long.MIN_VALUE;  
	    } else {
	        return value;
	    }
	}

	/** 
	 * Converts bits into a long, assuming that bytes[0] is the most significant
	 * byte and assuming that the value stored in bytes is in 2's complement.
	 * This implementation uses BigIntegers to do the conversion for it.
	 * 
	 * @throws IllegalArgumentException if bytes.length > 8.
	 */
	public static long toLong(byte[] bytes)
	{
		checkArgument(bytes.length <= 8, "Tried to convert an array of more than 8 bytes into a long");
	    
	    return (new BigInteger(bytes)).longValue();
	}
	

}

