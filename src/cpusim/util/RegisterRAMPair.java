///////////////////////////////////////////////////////////////////////////////
// File:    	RegisterRAMPair.java
// Author:		Dale Skrien
// Project: 	CPU Sim
// Date:    	June, 2001
//
// Description:
//   This file contains the object that contains the info
//   for the highlighting
//   of the rows of RAM during stepping.


///////////////////////////////////////////////////////////////////////////////
// the package in which our file resides

package cpusim.util;


///////////////////////////////////////////////////////////////////////////////
// the libraries we need to import

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import cpusim.module.RAM;
import cpusim.module.Register;


///////////////////////////////////////////////////////////////////////////////
// the RegisterRAMPair class

public class RegisterRAMPair implements Cloneable {
	
	private SimpleObjectProperty<Register> register;
	private SimpleObjectProperty<RAM> ram;
	
	//if true, always use the current value
	public SimpleBooleanProperty dynamic;    
	//otherwise use the value at start of cycle
    private int addressAtStart; 

    //constructor
    public RegisterRAMPair(Register theRegister, RAM theRAM, boolean dyn) {
        register = new SimpleObjectProperty<Register>(theRegister);
        ram = new SimpleObjectProperty<RAM>(theRAM);
        dynamic = new SimpleBooleanProperty(dyn);
        addressAtStart = 0;
    }

    //getters and setters
    public Register getRegister() {
        return register.get();
    }
    
    public RAM getRam() {
        return ram.get();
    }

    public boolean isDynamic() {
        return dynamic.get();
    }

    public int getAddressAtStart() {
        return addressAtStart;
    }

    public void setRegister(Register r) {
        register.set(r);
    }

    public void setRam(RAM r) {
        ram.set(r);
    }

    public void setDynamic(boolean d) {
        dynamic.set(d);
    }

    public void setAddressAtStart(int a) {
    	addressAtStart = a;
    }

    ////////////////Getters ////////////////
    public SimpleObjectProperty<Register> registerProperty() {
    	return register;
    }

    public SimpleObjectProperty<RAM> ramProperty() {
    	return ram;
    }

    public SimpleBooleanProperty dynamicProperty() {
    	return dynamic;
    }

    /**
     * Gives a clone of the current
     * RegisterRAM pair. Must be casted from 
     * an Object to RegisterRAMPair.
     * 
     * @return a clone of the current
     * RegisterRAM pair
     */
    public RegisterRAMPair clone() {
        return new RegisterRAMPair(register.get(), ram.get(), dynamic.get());
    }

    public String getXMLDescription() {
        return "<RegisterRAMPair register=\"" + register.get().getID() +
                "\" ram=\"" + ram.get().getID() + "\" dynamic=\"" +
                (dynamic.get() ? "true" : "false") + "\" />";
    }

}  //end of class RegisterRAMPair

