    ///////////////////////////////////////////////////////////////////////////////
// File:    	Register.java
// Type:    	java application file
// Author:		Dale Skrien
// Project: 	CPU Sim
// Date:    	June, 1999
//
// Description:
//   This file contains the code for the Register module.  Currently, registers
//   can have width of at most 64 bits.
//
// Things to do:
//   1.  Implement the rest of this class storing the value in a long, as started below.
//		 The advantage of this method is that you can easily extract bits via
//       Java's shift and bit operators.
//   2.  Implement the rest of this class storing the value in an array of 0's and 1's
//	 	 The advantage is that you can use registers with more than 64 bits.
//   3.  Implement the rest of this class storing the value in an array of booleans.
//		 The advantage is that you needn't worry about an array accidentally containing
//		 ints other than 0's and 1's.
//   4.  Decide how to proceed if in constructor or in setValue or in setWidth
//       the value becomes too big for the register.
//
///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

/*1.) Added a field read-only which stores if the register is read-only.
 * 2.) Modified the constructor so that it initialize the read-only field.
 * 3.) Modified getHTMLDescription() and getXMLDescription() so it writes the values to machine.
 * 4.) Modified clone() and copyDateTo() method to copy the read-only property to new register.
*/  
package cpusim.model.module;

import cpusim.model.Module;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.util.ValidationException;
import cpusim.model.util.units.ArchType;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.math.BigInteger;
import java.util.EnumSet;

import static com.google.common.base.Preconditions.*;

/**
 * Edit the parameters associated with any register or fromRootController new or delete old registers.
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
	
    //------------------------
    //instance variables
    private SimpleLongProperty value;  //the current value stored in the register
    private SimpleIntegerProperty width;	 //the number of bits in the register
    private SimpleLongProperty initialValue; // the initial value stored in the register
    private SimpleObjectProperty<EnumSet<Access>> access;
   
    private boolean nameDirty;
    
    // FIXME Why was this here?
//    private boolean programCounter; // if true, program breaks when this register's value
//                                    // matches the address of an instruction where a break
//                                    // point has been set

    /**
     * Constructor
     * @param name name of the register
     * @param width a positive integer that specifies the number of bits in the register.
     */
    public Register(String name, int width)
    {
        this(name, width, 0, Access.readWrite(), false);
    }

    /**
     * Constructor
     * @param name name of the register
     * @param width a positive integer that specifies the number of bits in the register.
     * @param initialValue the initial value stored in the register
     * @param readOnly the read only status of the register (if true, the value in
     *                 the register cannot be changed)
     */
    public Register(String name, int width, long initialValue, boolean readOnly)
    {
        this(name, width, initialValue, Access.isReadOnly(readOnly), false);
    }

    /**
     * Constructor
     * @param name name of the register
     * @param width a positive integer that specifies the number of bits in the register.
     * @param initialValue the initial value stored in the register
     * @param readOnly the read only status of the register (if true, the value in
     *                 the register cannot be changed)
     * @param dirty  whether the Register's name has changed since last displayed
     * 
     */
    public Register(String name, int width, long initialValue, boolean readOnly, boolean dirty)
    {
        this(name, width, initialValue, Access.isReadOnly(readOnly), dirty);
    }
    
    /**
     * Constructor
     * @param name name of the register
     * @param width a positive integer that specifies the number of bits in the register.
     * @param initialValue the initial value stored in the register
     * @param readOnly the read only status of the register (if true, the value in
     *                 the register cannot be changed)
     * @param dirty  whether the Register's name has changed since last displayed
     * 
     * @since 2016-10-12
     */
    public Register(String name, int width, long initialValue, EnumSet<Access> readOnly, boolean dirty)
    {
        super(name);
        checkNotNull(readOnly);
        
        this.value = new SimpleLongProperty(this, "register value", 0);
        this.width = new SimpleIntegerProperty(width);
        this.initialValue = new SimpleLongProperty(initialValue);
        this.access = new SimpleObjectProperty<>(readOnly);
        nameDirty = dirty;
        setValue(initialValue);
    }
    
    /**
     * Copy constructor!
     * @param other
     */
    public Register(Register other) {
        super(other.getName());
        
        this.value = new SimpleLongProperty();
        this.width = new SimpleIntegerProperty(other.getWidth());
        this.initialValue = new SimpleLongProperty();
        this.access = new SimpleObjectProperty<>(other.getAccess());
        
        this.nameDirty = other.nameDirty;
        
        this.setValue(other.getValue());
        this.setInitialValue(other.getInitialValue());
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
     * getter of the value
     * @return the value as long object
     */
    public long getValue()
    {
        return value.get();
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
     * getter of the read only value
     * @return the read only value as a boolean
     * 
     * @deprecated Use {@link #getAccess()} and check if {@link Access#Read} is the only entry. 
     */
    public boolean getReadOnly()
    {
        return access.get() == Access.readOnly();
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
     * set the width value
     * @param w new width value
     */
    public void setWidth(int w)
    {
        checkArgument(w > 0, "Register.setWidth() called with a parameter <= 0");
        
        if (width != null && w < width.get()) {
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
     * return the property object of value
     * @return property object
     */
    public SimpleLongProperty valueProperty() {
        return value;
    }

    /**
     * return the property object of readonly
     * @return property object of readonly
     */
    public SimpleObjectProperty<EnumSet<Access>> accessProperty(){
        return access;
    }

    /**
     * clear the value in the register to its initial value
     */
    public void clear()
    {
        setValue(initialValue.get());
    }
    

    /**
     * sets the name dirty value
     * @param dirty new boolean value
     */
    public void setNameDirty(boolean dirty){
        this.nameDirty = dirty;
    }

    /**
     * get the name dirty value
     * @return the name dirty value
     */
    public boolean getNameDirty(){
        return nameDirty;
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
	public <U extends Register> void copyTo(U newRegister) {
		checkNotNull(newRegister);
		
		newRegister.setName(getName());
        newRegister.setWidth(width.get());  //if a narrower width, the value is cleared
        newRegister.setInitialValue(initialValue.get());
        newRegister.setAccess(access.get());
        newRegister.setNameDirty(nameDirty);
	}
    
    @Override
    protected void validateState() {
        // Validate the width
        final int width = getWidth();
        if (width <= 0) {
            throw new ValidationException("You must specify a positive value for the " +
                    "bitwise width\nof the Register " + getName() + ".");
        }
        if (width > 64) {
            throw new ValidationException("Register " + getName() +
                    " can have a width of at most 64 bits.");
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
