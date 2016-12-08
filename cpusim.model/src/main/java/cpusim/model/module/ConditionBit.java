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

import cpusim.model.Machine;
import cpusim.model.util.MachineComponent;
import cpusim.model.util.ValidationException;
import javafx.beans.property.*;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * A condition bit is a specific bit of a register.
 * The bit can be set to 0 or 1 by a SetCondBit microinstruction
 * or it can be set to 1 if an overflow or carry out occurs in an
 * Arithmetic or Increment microinstruction.
 */
public class ConditionBit extends Module<ConditionBit> {

    @DependantComponent
    private final ObjectProperty<Register> register;  //the register containing the bit
    private final IntegerProperty bit;    //the index of the bit in the register, bit = 0
    //means the left-most or rightmost bit depending on the indexFromRight field in the machine.
    private final BooleanProperty halt;  //should machine halt when this bit is set to 1?

    private final ReadOnlySetProperty<MachineComponent> dependants;

    /**
     * Constructor
     * @param name name of the condition bit
     * @param bit the bit of the register that is the condition bit.
     * @param halt  a boolean value.
*              If this value is true, then the machine will halt
*              and display a message after the execution of any
*              microinstruction that causes the bit's value to
     */
    public ConditionBit(String name, UUID id, Machine machine, Register register, int bit, boolean halt) {
        super(name, id, machine);
        this.register = new SimpleObjectProperty<>(this, "register", register);
        this.bit = new SimpleIntegerProperty(bit);
        this.halt = new SimpleBooleanProperty(halt);

        this.dependants = MachineComponent.collectDependancies(this);
    }

    @Override
    public ReadOnlySetProperty<MachineComponent> getDependantComponents() {
        return this.dependants;
    }

    /**
     * getter for the register
     * @return the register object
     */
    public Register getRegister()
    {
        return register.get();
    }

    public ObjectProperty<Register> registerProperty() {
        return register;
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

    public IntegerProperty bitProperty() { return bit; }

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
    public BooleanProperty haltProperty(){
        return halt;
    }
    
    /**
     * Checks if the register stored is read-only, throws if it is.
     *
     * @throws ValidationException if the internal state is invalid.
     */
    private void validateNotReadOnlyRegister() {
        
        if(getRegister().getAccess().equals(Register.Access.readOnly())) {
            throw new ValidationException("The register \""+ getRegister().getName() +
                    "\" contains the halt bit \""+ getName() + "\".  This is not allowed. "
                    + "Any register containing a condition bit must be able to be written"
                    + " to.");
        }
    }
    
    /**
     * Checks if the bit set is valid within the {@link Register} that is set.
     *
     * @throws ValidationException if the bit is outside the width of the {@link Register}
     */
    private void validateBitWithinRegisterWidth() {
        final int width = getRegister().getWidth();
        final int bit = getBit();
        if (bit < 0) {
            throw new ValidationException("You cannot specify a negative value for the " +
                    "bit index of the ConditionBit " + getName() + ".");
        }
        else if (bit >= width) {
            throw new ValidationException("ConditionBit " + getName() +
                    " must have an index less than the length of the register.");
        }
    }
    
    @Override
    public void validate() {
        super.validate();

        if (getRegister() == null) {
            throw new ValidationException("ConditionBit " + getName() + " does not have a register set.");
        }
    
        validateBitWithinRegisterWidth();
        validateNotReadOnlyRegister();
    }

    @Override
    public ConditionBit cloneFor(MachineComponent.IdentifierMap oldToNew) {
        checkNotNull(oldToNew);

        return new ConditionBit(getName(), UUID.randomUUID(), oldToNew.getNewMachine(),
                oldToNew.get(getRegister()), getBit(), getHalt());
    }

    @Override
    public <U extends ConditionBit> void copyTo(U other) {
        other.setName(getName());
        other.setBit(getBit());
        other.setHalt(getHalt());

        other.registerProperty().setValue(getRegister());
    }

    /**
     * sets the bit to the given bit value (0 or 1)
     * @param bitValue a given bit value
     */
    public void set(int bitValue)
    {
        if (bitValue != 1 && bitValue != 0) {
            throw new IllegalArgumentException("Illegal value: " + bitValue);
        }

        int leftShift;
        if (machineProperty().getValue().getIndexFromRight()) {
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
     * Set the value
     * @param bitValue {@code 1} if {@code true}, {@code 0} otherwise.
     */
    public void set(boolean bitValue) {
        this.set(bitValue ? 1 : 0);
    }

    /**
     * returns true if the bit has value 1
     * @return true if the bit has value 1
     */
    public boolean isSet()
    {
        long registerValue = register.get().getValue();
        
        int leftShift;
        if (machineProperty().getValue().getIndexFromRight()) {
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
    public String getHTMLDescription(String indent) {
        return indent + "<TR><TD>" + getHTMLName() + "</TD><TD>" + getRegister().getHTMLName() +
                "</TD><TD>" + getBit() + "</TD><TD>" + getHalt() + "</TD></TR>";
    }

} // end class ConditionBit
