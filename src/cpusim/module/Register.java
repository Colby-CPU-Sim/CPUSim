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
package cpusim.module;

import cpusim.Module;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;

import java.math.BigInteger;


/**
 * Edit the parameters associated with any register or create new or delete old registers.
 */
public class Register extends Module
{
    //------------------------
    //instance variables
    private SimpleLongProperty value;  //the current value stored in the register
    private SimpleIntegerProperty width;	 //the number of bits in the register
    private SimpleLongProperty initialValue; // the initial value stored in the register
    private SimpleBooleanProperty readOnly;
    private boolean nameDirty;

    /**
     * Constructor
     * @param name name of the register
     * @param width a positive integer that specifies the number of bits in the register.
     */
    public Register(String name, int width)
    {
        this(name, width, 0, false);
    }

    /**
     * Constructor
     * @param name name of the register
     * @param width a positive integer that specifies the number of bits in the register.
     */
    public Register(String name, int width, long initialValue, boolean readOnly)
    {
        this(name, width, initialValue, readOnly, false);
    }

    /**
     * Constructor
     * @param name name of the register
     * @param width a positive integer that specifies the number of bits in the register.
     */
    public Register(String name, int width, long initialValue, boolean readOnly, boolean dirty)
    {
        super(name);
        this.value = new SimpleLongProperty(this, "register value", 0);
        setWidth(width);
        this.initialValue = new SimpleLongProperty(initialValue);
        this.readOnly = new SimpleBooleanProperty(readOnly);
        nameDirty = dirty;
        setValue(initialValue);
    }

    /**
     * getter of the width
     * @return the width as integer
     */
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
     */
    public boolean getReadOnly()
    {
        return readOnly.get();
    }

    /**
     * set the width value
     * @param w new width value
     */
    public void setWidth(int w)
    {
        assert w > 0 : "Register.setWidth() called with a parameter <= 0";
        if (width != null && w < width.get())
            setValue(0); //narrowing of width causes the value to be cleared
        this.width = new SimpleIntegerProperty(w);
    }

    /**
     * set the value stored
     * @param newValue new value
     */
    public void setValue(long newValue)
    {
        //check that newValue is between -(2^(width-1)) and (2^width)-1
        BigInteger max = BigInteger.valueOf(2).pow(width.get() - 1);
        BigInteger newBigValue = BigInteger.valueOf(newValue);
        assert max.negate().compareTo(newBigValue) <= 0 &&
                newBigValue.compareTo(max.shiftLeft(1).subtract(BigInteger.ONE)) <= 0 :
                "Attempt to set value of register " + getName() +
                " to value " + newValue + " which is out of range.";
        final long oldValue = value.get();
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
     */
    public void setReadOnly(boolean newReadOnly)
    {
        readOnly.set(newReadOnly);
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
    public SimpleBooleanProperty readOnlyProperty(){
        return readOnly;
    }
    //------------------------
    // module methods

    /**
     * returns the HTML description
     * @return the HTML description
     */
    public String getHTMLDescription()
    {
        return "<TR><TD>" + getHTMLName() + "</TD><TD>" + getWidth() + "</TD><TD>"
                + getInitialValue() + "</TD><TD>" + getReadOnly() + "</TD><TD></TD></TR>";
    }

    /**
     * returns the XML description
     * @return the XML description
     */
    public String getXMLDescription()
    {
        return "<Register name=\"" + getHTMLName() +
                "\" width=\"" + getWidth() +
                "\" initialValue=\"" + getInitialValue() +
                "\" readOnly=\"" + getReadOnly()+ "\" id=\"" +
                getID() + "\" />";
    }

    /**
     * clone the whole object
     * @return a clone of this object
     */
    public Object clone()
    {
        return new Register(getName(), width.get(),initialValue.get(),readOnly.get(), nameDirty);
    }

    /**
     * clear the value in the register to its initial value
     */
    public void clear()
    {
        setValue(initialValue.get());
    }

    /**
     * copies the data from the current module to a specific module
     * @param comp the micro instruction that will be updated
     */
    public void copyDataTo(Module comp)
    {
        assert comp instanceof Register :
                "Passed non-Register to Register.copyDataTo()";
        Register newRegister = (Register) comp;
        newRegister.setName(getName());
        newRegister.setWidth(width.get());  //if a narrower width, the value is cleared
        newRegister.setInitialValue(initialValue.get());
        newRegister.setReadOnly(readOnly.get());
        newRegister.setNameDirty(nameDirty);
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


} //end class Register
