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

import cpusim.model.Module;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.*;

/**
 * A register array is an indexed list of any number of registers.
 */
public class RegisterArray extends Module<RegisterArray> implements Iterable<Register>
{

    //------------------------
    //instance variables
    private SimpleIntegerProperty length;
    private SimpleIntegerProperty width;
    private ObservableList<Register> registers;
    private int numIndexDigits;  //== floor(log10(length-1))+1

    //------------------------
    
    

    /**
     * Constructor
     * @param name name of the register array
     * @param width a positive base-10 integer that specifies the number of bits in each register in the array.
     */
    public RegisterArray(String name, int width)
    {
        this(name, 1, width);
    }

    /**
     * Constructor
     * @param name name of the register array
     * @param length a positive base-10 integer specifying the number of registers in the register array.
     * @param width a positive base-10 integer that specifies the number of bits in each register in the array.
     */
    public RegisterArray(String name, int length, int width)
    {
        super(name);
        this.width = new SimpleIntegerProperty(width);  //used in setLength
        this.length = new SimpleIntegerProperty(0);  //initial length value to be used in setLength
        setLength(length);  //also sets the width
    }

    /**
     * Constructor
     * @param name name of the register array
     * @param length a positive base-10 integer specifying the number of registers in the register array.
     * @param width a positive base-10 integer that specifies the number of bits in each register in the array.
     */
    public RegisterArray(String name, int length, int width, ObservableList<Register> registers)
    {
        super(name);
        this.width = new SimpleIntegerProperty(width);  //used in setLength
        this.length = new SimpleIntegerProperty(0);  //initial length value to be used in setLength
        setLength(length);  //also sets the width
        setRegisters(registers);
    }

    //------------------------

    /**
     * getter of the width
     * @return the width as integer
     */
    public int getWidth()
    {
        return width.get();
    }

    /**
     * getter of the length
     * @return the length as integer
     */
    public int getLength()
    {
        return length.get();
    }

    /**
     * getter of the list of registers
     * @return a list of registers
     */
    public ObservableList<Register> registers()
    {
        return registers;
    }
    
    @Override
    public Iterator<Register> iterator() {
        return registers.iterator();
    }
    
    @Override
    public void forEach(final Consumer<? super Register> action) {
        registers.forEach(action);
    }
    
    @Override
    public Spliterator<Register> spliterator() {
        return registers.spliterator();
    }
    
    /**
     * overrides the Module.setName() method it inherits
     */
    public void setName(String name)
    {
        super.setName(name);
        
        for (int i = 0; i < registers.size(); i++){
            final Register r = registers.get(i);
            if (!r.getNameDirty())
                r.setName(name + "[" + toStringOfLength(i, numIndexDigits) + "]");
        }
    }

    /**
     * set the width value
     * @param width new width value
     */
    public void setWidth(int width)
    {
        this.width.set(width);
        for (Register register : registers)
            register.setWidth(width);
    }

    /**
     * This method does not worry about throwing away registers that were
     * used in a microinstruction or by a ConditionBit
     * @param newLength new length of the register array
     */
    public void setLength(int newLength)
    {
        assert newLength > 0 :
                "RegisterArray.setLength() called with length <= 0";

        //compute numIndexDigits instance variable for the new length
        updateNumIndexDigits(newLength);

        //now delete or add registers to the array, saving the deleted ones
        //in deletedRegisters
        if (newLength < length.get()) {
            //copy the old registers in a new shorter array
            ObservableList<Register> newRegisters = FXCollections.observableArrayList();
            for (int i = 0; i < newLength; i++)
                newRegisters.add(registers.get(i));

            registers = newRegisters;
        }
        else if (newLength >= length.get()) {
            //save the old registers and add new ones on the end
            ObservableList<Register> newRegisters = FXCollections.observableArrayList();
            for (int i = 0; i < length.get(); i++) {
                newRegisters.add(registers.get(i));
//                newRegisters.get(i).setName(getName() + "[" +
//                        toStringOfLength(i, numIndexDigits) + "]");
            }
            for (int i = length.get(); i < newLength; i++) {
                newRegisters.add(new Register(getName()+
                        toStringOfLength(i, numIndexDigits), width.get()));
            }
            registers = newRegisters;
//            deletedRegisters = null;
        }

        //adjust the length instance variable
        this.length.set(newLength);
    }

    public void setRegisters(ObservableList<Register> newRegisters){
        for (int i = 0; i < newRegisters.size(); i++){
            if (newRegisters.size() <= registers.size()){
                newRegisters.get(i).copyTo(registers.get(i));
            }
            else {
                registers.add(newRegisters.get(i));
            }
        }
    }

    /**
     * updates the number digits
     * @param newLength  new length of the digits
     */
    private void updateNumIndexDigits(int newLength) {
        numIndexDigits = 1;
        int temp = newLength;
        while (temp > 10) {
            numIndexDigits++;
            temp /= 10;
        }
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
        for (Register register : registers){
            registerTableString.append(register.getHTMLDescription(indent + "\t"));
        }
        registerTableString.append("</TABLE><P></P>");
        
        return indent + "<TR><TD>" + getHTMLName() + "</TD><TD>" + getLength() +
                "</TD><TD>" + getWidth() + "</TD><TD>" + registerTableString.toString() +
                "</TD></TR>";
    }

    /**
     * clone the whole object
     * @return a clone of this object
     */
    public Object clone()
    {
        //does not reuse the existing array of registers, but
        //instead creates a new array of registers.
        return new RegisterArray(getName(), length.get(), width.get(),registers());
    }

    /**
     * clear the value in the register in this array to 0
     */
    public void clear()
    {
        ObservableList<Register> registersInArray = registers();
        for (int j = 0; j < registersInArray.size(); j++) {
            registersInArray.get(j).clear();
        }

    } // end clear()

    /**
     * copies the data from the current module to a specific module
     * @param module the micro instruction that will be updated
     * 
     * @deprecated Use {@link #copyTo(RegisterArray)} instead.
     */
    @Deprecated
    public void copyDataTo(Module<?> module)
    {
    	checkArgument(module instanceof RegisterArray, "Passed non-RegisterArray to RegisterArray.copyDataTo()");
    	
        RegisterArray oldRegisterArray = (RegisterArray) module;
        this.copyTo(oldRegisterArray);
    }

    /**
     * returns a string rep of i preceded by enough spaces to make the total
     * length of the string equal to the given length.
     * @param i a integer value
     * @param length length of the data
     * @return a string representation of value i
     */
    private String toStringOfLength(int i, int length)
    {
        String result = "" + i;
        while (result.length() < length)
            result = " " + result;
        return result;
    }

	@Override
	public String getXMLDescription(String indent) {
		String nl = System.getProperty("line.separator");
        String result = "<RegisterArray name=\"" + getHTMLName() + "\" length=\""
                + getLength() + "\" width=\"" + getWidth() + "\" id=\""
                + getID() + "\" >" + nl;
        //write the descriptions of all the registers in the array
        for(int i = 0; i < length.get(); i++)
            result += "\t\t" + registers.get(i).getXMLDescription(indent + "\t") + nl;
        result += "\t</RegisterArray>";
        return indent + result;
	}

	@Override
	public <U extends RegisterArray> void copyTo(U oldRegisterArray) {
		checkNotNull(oldRegisterArray);
		
		oldRegisterArray.setLength(length.get()); //causes the array length to change
        oldRegisterArray.setName(getName());
        oldRegisterArray.setWidth(width.get());  //causes all register widths to change
        oldRegisterArray.setRegisters(registers);
		
	}


} //end class RegisterArray
