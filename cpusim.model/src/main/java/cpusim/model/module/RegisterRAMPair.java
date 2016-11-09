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

package cpusim.model.module;

import com.google.common.base.MoreObjects;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Objects;


/**
 * Holds a Register and a RAM for the purpose of highlighting.
 * The RAM cell whose address is in the register is highlighted when in Debug mode.
 * 
 * @since 2001-06-01
 */
public class RegisterRAMPair implements Cloneable {
	
	private SimpleObjectProperty<Register> register;
	private SimpleObjectProperty<RAM> ram;
	
	/**
     * if true, always highlight with the current value, not just at the start of a cycle
     *         otherwise, update highlighting only at the start of a cycle
     */
	public SimpleBooleanProperty dynamic;
    /**
     * the value in the register at the start of the current cycle.  It is preserved
     * here as the cycle is executed.
     */
    private int addressAtStart;

    //constructor
    public RegisterRAMPair(Register theRegister, RAM theRAM, boolean dyn) {
        register = new SimpleObjectProperty<>(theRegister);
        ram = new SimpleObjectProperty<>(theRAM);
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
    
    @Override
    public int hashCode() {
        return Objects.hash(dynamic.get(), addressAtStart, register.get(), ram.get());
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RegisterRAMPair that = (RegisterRAMPair) o;
        return addressAtStart == that.addressAtStart &&
                Objects.equals(register, that.register) &&
                Objects.equals(ram, that.ram) &&
                Objects.equals(dynamic, that.dynamic);
    }
}

