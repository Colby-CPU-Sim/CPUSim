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

import cpusim.model.util.Copyable;
import cpusim.model.util.LegacyXMLSupported;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Holds a Register and a RAM for the purpose of highlighting.
 * The RAM cell whose address is in the register is highlighted when in Debug mode.
 * 
 * @since 2001-06-01
 */
public class RegisterRAMPair implements LegacyXMLSupported, Copyable<RegisterRAMPair> {
	
	private SimpleObjectProperty<Register> register;
	private SimpleObjectProperty<RAM> ram;
	
	/**
     * if true, always highlight with the current value, not just at the start of a cycle
     *         otherwise, update highlighting only at the start of a cycle
     */
	private SimpleBooleanProperty dynamic;
    
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
    
    /**
     * Copy constructor
     * @param other instance to copy
     */
    public RegisterRAMPair(RegisterRAMPair other) {
        this(other.getRegister(), other.getRam(), other.isDynamic());
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
    public ObjectProperty<Register> registerProperty() {
    	return register;
    }

    public ObjectProperty<RAM> ramProperty() {
    	return ram;
    }

    public BooleanProperty dynamicProperty() {
    	return dynamic;
    }
    
    @Override
    public <U extends RegisterRAMPair> void copyTo(final U other) {
        checkNotNull(other);
        
        other.setRegister(getRegister());
        other.setDynamic(isDynamic());
        other.setRam(getRam());
    }
    
    @Override
    public String getXMLDescription(String indent) {
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
