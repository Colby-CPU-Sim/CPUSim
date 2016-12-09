package cpusim.model.module;

import cpusim.model.Machine;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.util.MachineComponent;
import cpusim.model.util.ValidationException;
import cpusim.model.util.units.ArchType;
import javafx.beans.property.*;

import java.math.BigInteger;
import java.util.EnumSet;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This file contains the code for the Register module. Currently, registers can have width of at most 64 bits.
 *
 * @since 1999-06-01
 */
public class Register extends Module<Register> implements Sized<Register>
{
	/**
	 * Memory Access specifier, usually used with an {@link EnumSet}.
	 *
	 * @since 2016-10-12
	 */
	public enum Access {
		/**
		 * Designates something may be read.
		 */
		Read,
		
		/**
		 * Designates something may be written to. 
		 */
		Write;
		
		/**
		 * Helper for getting a set of Access data if it is read-only or not.
		 * 
		 * @param isReadOnly True if read-only
		 * @return <code>EnumSet.of(Read)</code> if true, otherwise all.
		 * 
		 * @deprecated Use {@link #readOnly()} or create an {@link EnumSet} manually. 
		 */
		static EnumSet<Access> isReadOnly(boolean isReadOnly) {
			return isReadOnly ? EnumSet.of(Read) : EnumSet.allOf(Access.class);
		}
		
		/**
		 * Helper to get an {@link EnumSet} of only {@link #Read}. 
		 * 
		 * @return
		 */
		public static EnumSet<Access> readOnly() {
			return EnumSet.of(Read);
		}
		
		/**
		 * Helper to get an {@link EnumSet} of only {@link #Write}. 
		 * 
		 * @return
		 */
		public static EnumSet<Access> writeOnly() {
			return EnumSet.of(Write);
		}
		
		/**
		 * Helper to get a {@link #Read}-{@link #Write} {@link EnumSet}. 
		 * 
		 * @return
		 */
		public static EnumSet<Access> readWrite() {
			return EnumSet.of(Read, Write);
		}
	}

    /**
     * Maximum width for a {@code Register}
     */
	private static final int REGISTER_MAX_WIDTH = 64;
	
    //------------------------
    //instance variables
    private LongProperty value;  //the current value stored in the register
    private IntegerProperty width;	 //the number of bits in the register
    private LongProperty initialValue; // the initial value stored in the register
    private ObjectProperty<EnumSet<Access>> access;
    
    /**
     * Constructor
     * @param name name of the register
     * @param width a positive integer that specifies the number of bits in the register.
     * @param initialValue the initial value stored in the register
     * @param access the read only status of the register (if true, the value in
     *                 the register cannot be changed)
     * 
     * @since 2016-10-12
     */
    public Register(String name, UUID id, Machine machine, int width, long initialValue, EnumSet<Access> access) {
        super(name, id, machine);
        checkNotNull(access);
        
        this.value = new SimpleLongProperty(this, "value", 0);
        this.width = new SimpleIntegerProperty(this, "width", width);
        this.initialValue = new SimpleLongProperty(this, "initialValue", initialValue);
        this.access = new SimpleObjectProperty<>(this, "access", access);

        setValue(initialValue);
    }
    
    /**
     * Copy constructor!
     * @param other
     */
    public Register(Register other) {
        this(other.getName(), UUID.randomUUID(),
                other.getMachine(), other.getWidth(),
                other.getInitialValue(), other.getAccess());
    }

    /**
     * getter of the width
     * @return the width as integer
     */
    @Override
    public int getWidth()
    {
        return width.get();
    }

    /**
     * Gets a Read-only property for the width. To set the width, use {@link #setWidth(int)}.
     *
     * @return Read-only, non-{@code null} property.
     */
    public IntegerProperty widthProperty() { return width; }

    /**
     * getter of the value
     * @return the value as long object
     */
    public long getValue()
    {
        return value.get();
    }

    /**
     * return the property object of value
     * @return property object
     */
    public LongProperty valueProperty() {
        return value;
    }

    /**
     * getter of the initial value
     * @return the initial value as long object
     */
    public long getInitialValue()
    {
        return initialValue.get();
    }

    /**
     * Property for initial value.
     * @return Read-only, non-{@code null} property for initial value.
     */
    public LongProperty initialValueProperty() {
        return initialValue;
    }

    /**
     * getter of the read only value
     * @return the read only value as a boolean
     * 
     * @deprecated Use {@link #getAccess()} and check if {@link Access#Read} is the only entry. 
     */
    public boolean getReadOnly()
    {
        return access.get().equals(Access.readOnly());
    }
    
    /**
     * Get the memory access for the {@link Register}.
     * 
     * @return 
     */
    public EnumSet<Access> getAccess() {
    	return access.get();
    }


    /**
     * return the property object of readonly
     * @return property object of readonly
     */
    public ObjectProperty<EnumSet<Access>> accessProperty(){
        return access;
    }

    /**
     * set the width value
     * @param w new width value
     */
    public void setWidth(int w)
    {
        checkArgument(w > 0 && w < REGISTER_MAX_WIDTH,
                "Register.setWidth() called with width, %d, must be between 0..%d exclusive",
                w, REGISTER_MAX_WIDTH);

        if (w < width.get()) {
            setValue(0); //narrowing of width causes the value to be cleared
        }
        
        this.width.set(w);
    }

    /**
     * set the value stored
     * @param newValue new value
     */
    public void setValue(long newValue)
    {
        //check that newValue is between -(2^(width-1)) and (2^width)-1
    	final int w = width.get();
    	
    	if (!ArchType.Bit.of(w).fitsWithin(newValue)) {
    		throw new IllegalArgumentException("Attempt to set value of register " + getName() +
                    " to value " + newValue + " which is out of range.");
    	}
        
        value.set(newValue);
    }

    /**
     * set the initial value stored
     * @param newInitialValue the new initial value
     */
    public void setInitialValue(long newInitialValue){
        initialValue.set(newInitialValue);
    }

    /**
     * set the read only value
     * @param  newReadOnly the new readonly value
     * 
     * @deprecated Use {@link #setAccess(EnumSet)} 
     */
    public void setReadOnly(boolean newReadOnly)
    {
        access.set(Access.isReadOnly(newReadOnly));
    }
    
    /**
     * Set the access for the machine. 
     * 
     * @param access New access values
     */
    public void setAccess(EnumSet<Access> access) {
    	this.access.set(checkNotNull(access));
    }


    /**
     * clear the value in the register to its initial value
     */
    public void clear()
    {
        setValue(initialValue.get());
    }

	@Override
	public String getXMLDescription(String indent) {
		return indent + "<Register name=\"" + getHTMLName() +
                "\" width=\"" + getWidth() +
                "\" initialValue=\"" + getInitialValue() +
                "\" readOnly=\"" + getReadOnly()+ "\" id=\"" +
                getID() + "\" />";
	}

	@Override
	public String getHTMLDescription(String indent) {
		return indent + "<TR><TD>" + getHTMLName() + "</TD><TD>" + getWidth() + "</TD><TD>"
                + getInitialValue() + "</TD><TD>" + getReadOnly() + "</TD></TR>";
	}

    @Override
    public Register cloneFor(MachineComponent.IdentifierMap oldToNew) {
        checkNotNull(oldToNew);

        return new Register(getName(), UUID.randomUUID(), oldToNew.getNewMachine(),
                getWidth(), getInitialValue(), getAccess());
    }

    @Override
    public void copyTo(Register other) {
        checkNotNull(other);

        other.setName(getName());
        other.setWidth(getWidth());
        other.setInitialValue(getInitialValue());
        other.setAccess(getAccess());
        other.setValue(getValue());
    }

    @Override
    public void validate() {
        // Validate the width
        final int width = getWidth();
        if (width <= 0) {
            throw new ValidationException("You must specify a positive value for the " +
                    "bitwise width\nof the Register " + getName() + ".");
        }
        if (width > REGISTER_MAX_WIDTH) {
            throw new ValidationException("Register " + getName() + " can have a width of at most " +
                    REGISTER_MAX_WIDTH + " bits.");
        }
        
        // The intial value is within the set bounds:
        BigInteger max = BigInteger.valueOf(2).pow(width - 1);
        BigInteger initial = BigInteger.valueOf(getInitialValue());
        BigInteger unsignedMax = max.shiftLeft(1).subtract(BigInteger.ONE);
        if (!(max.negate().compareTo(initial) <= 0 &&
                initial.compareTo(unsignedMax) <= 0)) {
            throw new ValidationException("The initial value of register " + getName() +
                    " is out of range. It must be set to a value greater than or equal to " + max.negate() +
                    " and smaller than or equal to " + unsignedMax + ".");
        }
    }
    
    /**
     * checks if the dest register of the given {@link Microinstruction} is read-only
     * @param r destination register to check
     * @param microName name of the given {@link Microinstruction}
     */
	public static void validateIsNotReadOnly(final Register r, final String microName) {
        if (r.getAccess().equals(Access.readOnly())) {
            throw new ValidationException("The destination register " +
                    r.getName() + " of the microinstruction " +
                    microName + " is read-only.");
        }
    }
} //end class Register
