package cpusim.model.util.units;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Defines an architectural type.
 * 
 * @author Kevin Brightwell
 * @since 2016-09-20
 */
public enum ArchType {
	
	/**
	 * Represent a bit
	 */
	Bit(1.0),
	
	/**
	 * Represent a nibble, which is 4 bits.
	 */
	Nibble(4.0),
	
	/**
	 * Represent a Byte which is 8 bits
	 */
	Byte(8.0),
	
	Kilobyte(1024 * Byte.getBitFactor()),
	
	Megabyte(1024 * Kilobyte.getBitFactor());
	
	/**
	 * @see #getBitFactor()
	 */
	private final double bitFactor;
	
	/**
	 * Initialises types.
	 * @param bitFactor
	 */
	private ArchType(final double bitFactor) {
		this.bitFactor = bitFactor;
	}

	/**
	 * Get the factor between this {@link ArchType} width and {@link ArchType#Bit}.
	 * 
	 * @return the bitFactor
	 */
	public double getBitFactor() {
		return bitFactor;
	}
	
	/**
	 * Convert a value to its {@link ArchType#Bit} width.
	 * @param value int value to convert
	 * @return Value as bits rounded after multiplying by {@link #getBitFactor()}.
	 */
	int asBits(final int value) {
		return (int)(Math.round(value * bitFactor));
	}
	
	/**
	 * Converts a value to another {@link ArchType}. It will return the <i>minimum</i> required units of `otherType` to
	 * store the value. 
	 * 
	 * 
	 * 
	 * @param otherType {@link ArchType} to convert to
	 * @param value 
	 * @return Converted value represented by `otherType`
	 */
	int convertTo(final ArchType otherType, final int value) {
		final double conv = (value * (checkNotNull(otherType).bitFactor / bitFactor));
		return (int)Math.ceil(conv);
	}
	
	/**
	 * Creates a mask of `width` `Type`, e.g. for {@link ArchType#Bit} `#getMask(4)` returns `0xf`, but for 
	 * {@link ArchType#Nibble} `#getMask(2)` returns `0xff`. 
	 * 
	 * @param width Width of the mask 
	 * @return long mask
	 */
	public long getMask(final int width) {
		return (1l << asBits(width)) - 1l;
	}
	
	/**
	 * Creates a new {@link ArchValue} instance. 
	 * @param value value in the {@link ArchType} units.
	 * @return new {@link ArchValue}.
	 */
	public ArchValue value(final int value) {
		return new ArchValue(this, value);
	}
}