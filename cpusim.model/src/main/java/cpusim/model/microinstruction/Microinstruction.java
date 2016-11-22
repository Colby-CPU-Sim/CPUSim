/**
 * modified by Jinghui Yu
 * last editing date: 6/5/2013
 */

///////////////////////////////////////////////////////////////////////////////
// File:    	Microinstruction.java
// Type:    	java application file
// Author:		Dale Skrien
// Project: 	CPU Sim 3.0
// Date:    	June, 1999
//
// Description:
//   This file contains the code for the Microinstruction class.
//   The MicroInstruction class was created because it is very useful to be able to
//   classify the different MicroInstructions just as MicroInstructions rather than
//   Arithmetics, Shifts, and so on.  It saves time, space, and code.
//
//   It is abstract so no one will ever fromRootController an object of this class.


///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

package cpusim.model.microinstruction;


import com.google.common.base.Strings;
import cpusim.model.Machine;
import cpusim.model.Module;
import cpusim.model.util.LegacyXMLSupported;
import cpusim.model.util.NamedObject;
import cpusim.model.util.Validatable;
import cpusim.xml.HTMLEncodable;
import cpusim.xml.HtmlEncoder;
import javafx.beans.property.SimpleStringProperty;

import static com.google.common.base.Preconditions.*;



///////////////////////////////////////////////////////////////////////////////
// the Microinstruction class

public abstract class Microinstruction
        implements NamedObject, LegacyXMLSupported, HTMLEncodable, Validatable
{
	
    // name of the microinstruction
    private SimpleStringProperty name;
    
    protected Machine machine;

    //------------------------------
    // constructor

    public Microinstruction(String name, Machine machine)
    {
    	checkNotNull(name);
    	checkArgument(!Strings.isNullOrEmpty(name));
    	
    	this.name = new SimpleStringProperty(name);
        this.machine = machine;
    }   
    
	/**
	 * Copy constructor: copies data in <code>other</code>.
	 * @param other Instance to copy from
	 * 
	 * @throws NullPointerException if <code>other</code> is <code>null</code>.
	 */
	public Microinstruction(final Microinstruction other) {
		this(checkNotNull(other).getName(), other.machine);
	}

    /**
     * returns the name of the set microinstruction as a string.
     *
     * @return the name of the set microinstruction.
     */
    @Override
    public String getName() {
        return name.get();
    }
    
    /**
     * returns the class of the microinstruction
     * @return the class of the microinstruction
     */
    public abstract String getMicroClass();

    /**
     * updates the name of the set microinstruction.
     *
     * @param newName the new name for the set microinstruction.
     */
    @Override
    public void setName(String newName){
        name.set(newName);
    }

    public String getHTMLName()
    {
        return HtmlEncoder.sEncode(getName());
    }

    public String toString()
    {
        return name.get();
    }

    //------------------------------
    // abstract methods
    // These methods should be overridden by all subclasses

    public abstract void execute();

    /**
     * returns true if this microinstruction uses m (so if m is modified, this micro may need to be modified.
     * 
     * @param m
     * @return
     * 
     * @throws NullPointerException if <code>m</code> is <code>null</code>.
     */
    public abstract boolean uses(Module<?> m);
    
    /**
     * Should perform the actions described by {@link Validatable#validate()}. This allows common
     * {@link Microinstruction} validation to live inside {@link #validate()}.
     */
    protected abstract void validateState();
    
    @Override
    public final void validate() {
        NamedObject.super.validate();
        
        this.validateState();
    }
    
}  // end of class Microinstruction
