/**
 * 
 */
package cpusim.model.util.units;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.primitives.Ints;

import javax.annotation.Generated;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.math.BigInteger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Consists of a wrapped integer that has width information that allows it to store and perform math related to 
 * bit twiddling math. This removes ambiguous integer types in exchange for safety and consistency. 
 * 
 * This type is safe to use in sorted containers and in hash environments. It is thread-safe and has no modifiable 
 * state.
 * 
 * @author Kevin Brightwell
 * @since 2016-09-20
 */
@Immutable @ThreadSafe
public class ArchValue implements Comparable<ArchValue> {

	/**
	 * Type information
	 */
	private final ArchType type;
	
	/**
	 * Value in {@link #type} units. 
	 */
	@JsonProperty("value") // manual spec for serialization, do not expose as getter
	private final long value;
	
	/**
	 * Stores an {@link ArchValue} of <code>0</code> {@link ArchType#Bit}s.
	 * 
	 * @see ArchType#Bit
	 * @see ArchType#of(long)
	 */
	public static final ArchValue ZERO = ArchType.Bit.of(0);
	
	/**
	 * Creates a new ArchValue with `type` and `value`. This is meant to only be used by {@link ArchType#of(long)}.
	 * 
	 * @param type
	 * @param value
	 */
	@JsonCreator
	ArchValue(@JsonProperty("type") final ArchType type, 
			  @JsonProperty("value") final long value) {
		checkArgument(value >= 0, "value must be a value >= 0");
		this.type = checkNotNull(type);
		this.value = value;
	}
	
	/**
	 * Constructs a new {@link ArchValue} converting `other` to the new `type`.
	 * @param convertType Type to convert to 
	 * @param fromValue Value converting from 
	 */
	private ArchValue(final ArchType convertType, final ArchValue fromValue) {
		this.type = checkNotNull(convertType);
		this.value = checkNotNull(fromValue).type.convertTo(convertType, fromValue.value);
	}
	
	/**
	 * Short-cut method for bit values. Delegates to {@link ArchType#Bit}'s {@link ArchType#of(long)}.
	 * 
	 * @param bitValue
	 * @return Non-<code>null</code> {@link ArchValue}
	 * 
	 * @see ArchType#of(long)
	 */
	public static ArchValue bits(final int bitValue) {
		return ArchType.Bit.of(bitValue);
	}
		
	/**
	 * Converts the stored type into the new `other` type.
	 * 
	 * @param other {@link ArchType} to convert to.
	 * @return `new` {@link ArchValue} of type `other`. 
	 */
	public ArchValue convert(final ArchType other) {
		return new ArchValue(checkNotNull(other), this);
	}
	
	
	/**
	 * Gets the value as <code>other</code> units. <code>other</code> defaults to {@link ArchType#Bit}.
	 * 
	 * @param other {@link ArchType} to convert to.
	 * @return `new` {@link ArchValue} of type `other`. 
	 */
	public long as(final ArchType other) {
		return convert(other).value;
	}
	

	/**
	 * The stored value as a bits value.
	 * @return the stored value converted to bits.
	 * 
	 * @see #as(ArchType)
	 */
	public long as() {
		return type.asBits(value);
	}
	
	/**
	 * Get the stored type. 
	 * @return Stored type information
	 */
	public ArchType getType() {
		return type;
	}
	
	/**
	 * Get the stored value in unit {@link #getType()}.
	 * @return value in the base specified.
	 */
	public long getValue() {
		return value;
	}
	
	/**
	 * Allocates a new array of bytes that is the same width as the definition. 
	 * @return newly allocated array of bytes
	 */
	public byte[] newByteArray() {
		final long length = convert(ArchType.Byte).value;
		if (length > Integer.MAX_VALUE) {
			throw new IllegalStateException("Can not create a byte[] with length: " + length);
		}
		
		return new byte[(int)length];
	}
	
	/**
	 * Create a mask of <code>this</code> units wide. 
	 *
	 * @return bit mask.
     * @throws IllegalStateException if the width is too large
	 *
     * @see ArchType#getMask(long)
	 */
	public long mask() {
		try {
			return type.getMask(value);
		} catch (IllegalArgumentException iae) {
			throw new IllegalStateException(iae);
		}
	}

	/**
	 * Create a mask of <code>this</code> units wide.
	 *
	 * @return bit mask.
	 */
	public int imask() {
		checkState(Ints.BYTES >= as(ArchType.Byte),
				"Can not get int mask with %s", this);
		return (int)mask();
	}
	
	/**
	 * Adds `other` to `this`
	 * 
	 * @param other
	 * @return new {@link ArchValue} with type `this.type`
	 */
	public ArchValue add(ArchValue other) {
		return new ArchValue(type, checkNotNull(other).convert(type).value + value);
	}
	
	/**
	 * Subtracts `other` from `this`
	 * 
	 * @param other
	 * @return new {@link ArchValue} with type `this.type`
	 */
	public ArchValue sub(ArchValue other) {
		return new ArchValue(type, value - checkNotNull(other).convert(type).value);
	}
	
	/**
	 * Multiplies `other` and `this`
	 * 
	 * @param other
	 * @return new {@link ArchValue} with type `this.type`
	 */
	public ArchValue mul(ArchValue other) {
		return new ArchValue(type, value * checkNotNull(other).convert(type).value);
	}
	
	/**
	 * Divides `this` by `other` 
	 * 
	 * @param other
	 * @return new {@link ArchValue} with type `this.type`
	 */
	public ArchValue div(ArchValue other) {
		return new ArchValue(type, value / checkNotNull(other).convert(type).value);
	}
	
	/**
	 * Performs `this` mod `other` 
	 * 
	 * @param other
	 * @return new {@link ArchValue} with type `this.type`
	 */
	public ArchValue mod(ArchValue other) {
		return new ArchValue(type, value % checkNotNull(other).convert(type).value);
	}
	
	/**
	 * Performs `this` << `other` 
	 * 
	 * @param other
	 * @return new {@link ArchValue} with type `this.type`
	 */
	public ArchValue shl(ArchValue other) {
		return new ArchValue(type, value << checkNotNull(other).as());
	}
	
	/**
	 * Performs `this` >> `other` 
	 * 
	 * @param other
	 * @return new {@link ArchValue} with type `this.type`
	 */
	public ArchValue shr(ArchValue other) {
		return new ArchValue(type, value >> checkNotNull(other).as());
	}
	
	/**
	 * Performs `this` >>> `other` (arithmetic right shift).
	 * 
	 * @param other
	 * @return new {@link ArchValue} with type `this.type`
	 */
	public ArchValue ashr(ArchValue other) {
		return new ArchValue(type, value >>> checkNotNull(other).as());
	}
	
	/**
	 * Check if a value is less than <code>other</code>.
	 * 
	 * @param other
	 * @return True if <code>this &lt; other</code>
	 * 
	 * @throws NullPointerException if <code>other</code> is <code>null</code>
	 */
	public boolean lt(ArchValue other) {
		return as() < checkNotNull(other).as();
	}
	
	/**
	 * Check if a value is less than or equal to <code>other</code>.
	 * 
	 * @param other
	 * @return True if <code>this &lt;= other</code>
	 * 
	 * @throws NullPointerException if <code>other</code> is <code>null</code>
	 */
	public boolean lte(ArchValue other) {
		return as() <= checkNotNull(other).as();
	}
	
	/**
	 * Check if a value is greater than <code>other</code>.
	 * 
	 * @param other
	 * @return True if <code>this &gt; other</code>
	 * 
	 * @throws NullPointerException if <code>other</code> is <code>null</code>
	 */
	public boolean gt(ArchValue other) {
		return as() > checkNotNull(other).as();
	}
	
	/**
	 * Convenience function roughly equivalent to {@link #gt(ArchValue)} with {@link #ZERO} as the argument. 
	 * 
	 * @return <code>true</code> if the value is greater than zero.
	 * 
	 * @see #gt(ArchValue)
	 * @see #ZERO
	 */
	public boolean gtZero() {
		return as() > 0;
	}
	
	/**
	 * Check if a value is greater than or equal to <code>other</code>.
	 * 
	 * @param other
	 * @return True if <code>this &gt;= other</code>
	 * 
	 * @throws NullPointerException if <code>other</code> is <code>null</code>
	 */
	public boolean gte(ArchValue other) {
		return as() >= checkNotNull(other).as();
	}
	
	/**
	 * Checks if the value fits within the {@link #getType()} specified. 
	 * 
	 * For example:
	 * <code>
	 * 	assert(Bits.value(8).fitsWithin(0x100) == false);
	 *  assert(Byte.value(2).fitsWithin(0xffff) == true);
	 * </code>
	 * 
	 * @param checkValue
	 * @return new {@link ArchValue} with type `this.type`
	 */
	public boolean fitsWithin(final long checkValue) {
		if (value <= 63) {
	       final double max = Math.pow(2, value);
	       return checkValue <= max;
	    } else if (value <= Integer.MAX_VALUE) {
	    	final BigInteger max = BigInteger.valueOf(2).pow((int)value);
	        return BigInteger.valueOf(checkValue).compareTo(max) <= 0;
	    } else {
	    	throw new IllegalStateException("Stored value is too large to be represented as an Integer: " + value);
	    }
	}

	@Override
	public int compareTo(ArchValue other) {
		return Long.compare(this.value, checkNotNull(other).convert(type).value);
	}
	
	/**
	 * Delegates to {@link #compareTo(ArchValue)}.
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	public static int compare(ArchValue left, ArchValue right) {
		return checkNotNull(left).compareTo(checkNotNull(right));
	}
	
	@Override
	public int hashCode() {
		return Long.hashCode(as());
	}
	
	@Override
	public String toString() {
        return MoreObjects.toStringHelper(this)
        		.addValue(type)
        		.addValue(value)
        		.toString();
    }

	@Override @Generated(value="Eclipse")
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArchValue other = (ArchValue) obj;
		if (type != other.type)
			return false;
		if (value != other.value)
			return false;
		return true;
	}
	
	
}
