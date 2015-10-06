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
//   It is abstract so no one will ever create an object of this class.


///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

package cpusim;


///////////////////////////////////////////////////////////////////////////////
// the libraries we need to import

import cpusim.util.*;  //for Assert
import cpusim.xml.*;   //for HtmlEncoder
import javafx.beans.property.SimpleStringProperty;

import java.io.*;

//for Serializable



///////////////////////////////////////////////////////////////////////////////
// the Microinstruction class

public abstract class Microinstruction
        implements Serializable, Cloneable, NamedObject
{
    // name of the microinstruction
    private SimpleStringProperty name = new SimpleStringProperty("");
    private String ID;
    protected Machine machine;

    //------------------------------
    // constructor

    public Microinstruction(String name, Machine machine)
    {
        setName(name);
        
        this.machine = machine;

        String s = super.toString();
        int index = s.indexOf('@');
        if (index == -1)
            ID = s;
        else
            ID = s.substring(7, index) + s.substring(index + 1);

    }

    /**
     * returns the name of the set microinstruction as a string.
     *
     * @return the name of the set microinstruction.
     */
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

    // the ID is a unique identifier for each microinstruction.  It is
    // used in the XML machine file.
    // if the Object.toString() method returns "cpusim.xxx.zzz@yyy",
    // then this method returns "xxx.zzzyyy".
    // This ID string is computed only once, in the Microinstruction
    // constructor.
    public String getID()
    {
        return ID;
    }

    //------------------------------
    // abstract methods
    // These methods should be overridden by all subclasses

    public void execute()
    {
        assert false : "The execute() method of the abstract " +
                "Microinstruction class was called.";
    }

    public abstract void copyDataTo(Microinstruction oldMicro);

    public abstract Object clone();

    public abstract String getXMLDescription();

    public abstract String getHTMLDescription();

    //------------------------------
    // returns true if this microinstruction uses m
    // (so if m is modified, this micro may need to be modified.
    public abstract boolean uses(Module m);


}  // end of class Microinstruction
