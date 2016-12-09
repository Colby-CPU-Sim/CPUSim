///////////////////////////////////////////////////////////////////////////////
// File:    	RegisterArray.java
// Type:    	java application file
// Author:		Josh Ladieu
// Project: 	CPU Sim
// Date:    	April, 2000
//
// Last Modified: 6/3/13
//
// Description:
//   This file contains the code for the RegisterArray module.
//
///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

/*
 * Michael Goldenberg, Ben Borchard, and Jinghui Yu made the following changes in 11/11/13
 * 
 * 1.) Modified the getHTMLDescription method so that it put a register table in the fifth
 * column of the register array table
 * 
 */

package cpusim.model.module;

import cpusim.model.Machine;
import cpusim.model.util.MachineComponent;
import cpusim.model.util.Validatable;
import cpusim.model.util.ValidationException;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.fxmisc.easybind.EasyBind;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A register array is an indexed list of any number of registers.
 */
public class RegisterArray extends Module<RegisterArray>
        implements Sized<RegisterArray>, Iterable<Register> {


    //------------------------
    //instance variables
    @ChildComponent
    private final ListProperty<Register> registers;

    private final IntegerProperty width;
    private final ReadOnlyIntegerProperty numIndexDigits;  //== floor(log10(length-1))+1

    private final int initialValue;
    private final EnumSet<Register.Access> initialAccess;

    private final ReadOnlySetProperty<MachineComponent> children;
    
    /**
     * Constructor
     * @param name name of the register array
     * @param width a positive base-10 integer that specifies the number of bits in each register in the array.
     */
    public RegisterArray(String name, UUID id, Machine machine,
                         int width, int length,
                         int initialValue, EnumSet<Register.Access> initialAccess) {
        super(name, id, machine);

        this.width = new SimpleIntegerProperty(this, "width", width);  //used in setLength
        this.initialAccess = checkNotNull(initialAccess);
        this.initialValue = Math.max(0, initialValue);

        this.registers = new SimpleListProperty<>(this, "registers", FXCollections.observableArrayList());

        // Auto compute the number of digits when the size changes
        IntegerProperty numIndexDigits = new SimpleIntegerProperty(this, "numIndexDigits", 0);
        numIndexDigits.bind(EasyBind.map(this.registers.sizeProperty(), size -> {
            if (size.intValue() <= 1)
                return 1;

            return (int)Math.floor(Math.log10(size.intValue() - 1)) + 1;
        }));
        this.numIndexDigits = numIndexDigits;

        for (int i = 0; i < length; ++i) {
            Register register = new Register("", UUID.randomUUID(),
                    machine, width,
                    initialValue,
                    initialAccess);

            bindRegister(register);
        }

        children = MachineComponent.collectChildren(this)
                .buildSet(this, "children");
    }

    /**
     * Binds the name "arrayName[i]" ({@link Register#nameProperty()} &lt;- {@link #nameProperty()}) and the
     * {@link Register#widthProperty()} &lt;-&gt; {@link #widthProperty()}.
     *
     * @param toBind The register that will be bound to the properties of the RegisterArray
     */
    private void bindRegister(Register toBind) {
        checkNotNull(toBind);

        toBind.nameProperty().bind(Bindings.format("%s[%s]",
                nameProperty(),
                Bindings.createStringBinding(() -> getIndexString(registers.indexOf(toBind)),
                        registers)));

        toBind.widthProperty().bindBidirectional(width);

        this.registers.add(toBind);
    }

    @Override
    public ReadOnlySetProperty<MachineComponent> getChildrenComponents() {
        return children;
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
     * Read-only property for the width.
     * @return Read-only, non-{@code null} width property.
     */
    public IntegerProperty widthProperty() {
        return width;
    }

    /**
     * getter of the length
     * @return the length as integer
     */
    public int getLength() {
        return registers.size();
    }

    /**
     * Gets the length of the register array, it is bound to {@link ObservableList#size()} of the
     * {@link #registersProperty()}.
     *
     * @return Length of register array.
     */
    public ReadOnlyIntegerProperty lengthProperty() {
        return registers.sizeProperty();
    }

    public ListProperty<Register> registersProperty() {
        return registers;
    }

    public ObservableList<Register> getRegisters() {
        return FXCollections.unmodifiableObservableList(registers.getValue());
    }

    @Override
    public Iterator<Register> iterator() {
        return registers.iterator();
    }

    /**
     * set the width value
     * @param width new width value
     */
    public void setWidth(int width) {
        this.width.set(width);
    }

    /**
     * This method does not worry about throwing away registers that were
     * used in a microinstruction or by a ConditionBit
     * @param newLength new length of the register array
     */
    public void setLength(int newLength) {
        if (newLength <= 0) {
            throw new IllegalArgumentException("RegisterArray.setLength() called with length <= 0");
        }

        //now delete or add registers to the array, saving the deleted ones
        //in deletedRegisters

        // remove registers if needed
        registers.remove(newLength, registers.size());

        for (int i = registers.size(); i < newLength; i++) {
            Register r = new Register("",
                    UUID.randomUUID(),
                    getMachine(),
                    width.get(),
                    initialValue,
                    initialAccess);

            bindRegister(r);
        }
    }

    public void setRegisters(ObservableList<Register> newRegisters) {
        registers.clear();
        newRegisters.forEach(this::bindRegister);
    }



    //------------------------
    // other utility methods

    /**
     * returns the HTML description
     * @return the HTML description
     */
    @Override
    public String getHTMLDescription(String indent)
    {
        StringBuilder registerTableString = new StringBuilder("<TABLE bgcolor=\"#FFC0A0\" BORDER=\"1\"" +
                    "CELLPADDING=\"0\" CELLSPACING=\"3\" WIDTH=\"100%\">" + 
                "<TR><TD><B>Name</B></TD><TD><B>" +
                "Width</B></TD><TD><B>Initial Value</B></TD>"+
                "<TD><B>Read Only</B></TD><B>");
        for (Register register : registers) {
            registerTableString.append(register.getHTMLDescription(indent + "\t"));
        }
        registerTableString.append("</TABLE><P></P>");
        
        return indent + "<TR><TD>" + getHTMLName() + "</TD><TD>" + getLength() +
                "</TD><TD>" + getWidth() + "</TD><TD>" + registerTableString.toString() +
                "</TD></TR>";
    }

    /**
     * clear the value in the register in this array to 0
     */
    public void clear() {
        getRegisters().forEach(Register::clear);
    }

    /**
     * returns a string rep of i preceded by enough spaces to make the total
     * length of the string equal to the given length.
     * @param i a integer value
     * @return a string representation of value i
     */
    private String getIndexString(int i)
    {
        StringBuilder bld = new StringBuilder();
        String intStr = Integer.toString(i);

        int numBits = this.numIndexDigits.get();
        while (bld.length() < (numBits - intStr.length())) {
            bld.append(' ');
        }

        return bld.append(intStr).toString();
    }

	@Override
	public String getXMLDescription(String indent) {
		String nl = System.getProperty("line.separator");
        String result = "<RegisterArray name=\"" + getHTMLName() + "\" length=\""
                + getLength() + "\" width=\"" + getWidth() + "\" id=\""
                + getID() + "\" >" + nl;
        //write the descriptions of all the registers in the array
        for(Register r: registers)
            result += "\t\t" + r.getXMLDescription(indent + "\t") + nl;
        result += "\t</RegisterArray>";
        return indent + result;
	}

    @Override
    public RegisterArray cloneFor(MachineComponent.IdentifierMap oldToNew) {
        checkNotNull(oldToNew);

        RegisterArray newArray = new RegisterArray(getName(), UUID.randomUUID(), oldToNew.getNewMachine(),
                getWidth(), getWidth(), getLength(), Register.Access.readWrite());

        registers.stream().map(oldToNew::get).forEach(newArray.registers::add);

        return newArray;
    }

    @Override
    public <U extends RegisterArray> void copyTo(U other) {
        checkNotNull(other);

        other.setName(getName());
        other.setWidth(getWidth());  //causes all register widths to change
        other.setRegisters(registers);
    }

    @Override
    public void validate() {
        super.validate();

        final int width = getWidth();
        if (width <= 0) {
            throw new ValidationException("You must specify a positive value for the " +
                    "bitwise width\nof the registers in the RegisterArray " +
                    getName() + ".");
        }
        else if (width > 64) {
            throw new ValidationException("The registers in RegisterArray " + getName() +
                    " can be at most 64 bits wide.");
        }

        registers.forEach(Validatable::validate);
    }
} //end class RegisterArray
