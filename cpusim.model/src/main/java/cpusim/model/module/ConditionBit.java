///////////////////////////////////////////////////////////////////////////////
// File:    	ConditionBit.java
// Type:    	java application file
// Author:		Josh Ladieu
// Project: 	CPU Sim
// Date:    	March, 1999
//
// Description:
//   This file contains the code for the ConditionBit module.


///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

package cpusim.model.module;

import java.util.Optional;

import cpusim.model.Machine;
import cpusim.model.Module;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;


/**
 * A condition bit is a specific bit of a register.
 * The bit can be set to 0 or 1 by a SetCondBit microinstruction
 * or it can be set to 1 if an overflow or carry out occurs in an
 * Arithmetic or Increment microinstruction.
 */
public class ConditionBit extends Module<ConditionBit>
{
	
    private SimpleObjectProperty<Register> register;  //the register containing the bit
    private SimpleIntegerProperty bit;    //the index of the bit in the register, bit = 0
    //means the left-most or rightmost bit depending on the indexFromRight field in the machine.
    private SimpleBooleanProperty halt;  //should machine halt when this bit is set to 1?

    private static final ConditionBit NONE_SET = new ConditionBit("(none)", new Machine("None"), new Register("", 1), 0, false);
    
    /**
     * Get the {@link ConditionBit} referring to having no condition bit set. This replaces the old <code>CpuSimConstants.NO_CONDITIONBIT</code>
     * field.
     * 
     * @return {@link #NONE_SET}
     * 
     * @since 2016-10-12
     * @deprecated Use an {@link Optional} instead to store if there maybe none set. 
     */
    @Deprecated
    public static ConditionBit none() {
    	return NONE_SET;
    }
    
    /**
     * Constructor
     * @param name name of the condition bit
     * @param machine the machine that contains this condition bit
     * @param register the register that contains the condition bit
     * @param bit the bit of the register that is the condition bit.
     * @param halt  a boolean value.
     *              If this value is true, then the machine will halt
     *              and display a message after the execution of any
     *              microinstruction that causes the bit's value to
     *              be set to 1.
     */
    public ConditionBit(String name, Machine machine, Register register, int bit, boolean halt)
    {
        super(name, machine);
        this.register = new SimpleObjectProperty<>(register);
        this.bit = new SimpleIntegerProperty(bit);
        this.halt = new SimpleBooleanProperty(halt);
    }

    /**
     * getter for the register
     * @return the register object
     */
    public Register getRegister()
    {
        return register.get();
    }

    /**
     * setter for the register
     * @param r new one that will replace the old register
     */
    public void setRegister(Register r)
    {
        register.set(r);
    }

    /**
     * getter for the bit
     * @return the bit value
     */
    public int getBit()
    {
        return bit.get();
    }

    /**
     * setter for the bit
     * @param newBit the new value that replaces the old one
     */
    public void setBit(int newBit)
    {
        bit.set(newBit);
    }

    /**
     * getter for the Halt
     * @return a boolean value if the machine will halt
     */
    public boolean getHalt()
    {
        return halt.get();
    }

    /**
     * setter for the Halt
     * @param newHalt the new value that replaces the old one
     */
    public void setHalt(boolean newHalt)
    {
        halt.set(newHalt);
    }

    /**
     * get the simple property object
     * @return the simple property object of halt
     */
    public SimpleBooleanProperty haltProperty(){
        return halt;
    }

    /**
     * clone the whole object
     * @return a clone of this object
     */
    public Object clone()
    {
        return new ConditionBit(getName(), machine, register.get(), bit.get(), halt.get());
    }

    /**
     * copies the data from the current module to a specific module
     * @param newConditionBit the micro instruction that will be updated
     */
    @Override
    public void copyTo(ConditionBit newBit)
    {
        newBit.setName(getName());
        newBit.setRegister(getRegister());
        newBit.setBit(getBit());
        newBit.setHalt(getHalt());
    }

    /**
     * sets the bit to the given bit value (0 or 1)
     * @param bitValue a given bit value
     */
    public void set(int bitValue)
    {
        assert bitValue == 0 || bitValue == 1 : "Illegal argument: " + bitValue +
                " to ConditionBit.set().";
        int leftShift;
        if (machine.getIndexFromRight()) {
            leftShift = bit.get();
        }
        else {
            leftShift = register.get().getWidth() - bit.get() - 1;
        }
        long mask = 1L << (leftShift);
        long value = register.get().getValue();
        if (bitValue == 1)
            value = value | mask;
        else
            value = value & ~mask;
        //value may be too big for register, so convert it to a negative by shifting
        value = (value << (64 - register.get().getWidth())) >> (64 - register.get().getWidth());
        register.get().setValue(value);
    }

    /**
     * returns true if the bit has value 1
     * @return true if the bit has value 1
     */
    public boolean isSet()
    {
        long registerValue = register.get().getValue();
        
        int leftShift;
        if (machine.getIndexFromRight()) {
            leftShift = 64 - bit.get() - 1;
        }
        else {
            leftShift = 64 - register.get().getWidth() + bit.get();
        }

        registerValue = registerValue << leftShift;
        return registerValue < 0;
    }

    /**
     * returns the XML description
     * @return the XML description
     */
    @Override
    public String getXMLDescription(String indent)
    {
        return indent + "<ConditionBit name=\"" + getHTMLName() +
                "\" bit=\"" + getBit() +
                "\" register=\"" + getRegister().getID() +
                "\" halt=\"" + getHalt() +
                "\" id=\"" + getID() + "\" />";
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
    @Override
    public String getHTMLDescription(String indent)
    {
        return indent + "<TR><TD>" + getHTMLName() + "</TD><TD>" + getRegister().getHTMLName() +
                "</TD><TD>" + getBit() + "</TD><TD>" + getHalt() + "</TD></TR>";
    }

} // end class ConditionBit
