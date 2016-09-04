///////////////////////////////////////////////////////////////////////////////
// File:    	End.java
// Type:    	java application file
// Author:		Dale Skrien
// Project: 	CPU Sim
// Date:    	June, 2000
//
// Description:
//   This file contains the code for the End microinstruction class.
//
// Things to do:
//	1. Write the execute method.
///////////////////////////////////////////////////////////////////////////////

package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.Microinstruction;
import cpusim.model.Module;

//import cpusim.model.module.*;


public class End extends Microinstruction
{

    /**
     * Constructor
     * @param machine the machine that holds the micro
     */
    public End(Machine machine)
    {
        super("End", machine);
    } // end constructor
    
    /**
     * returns the class of the microinstruction
     * @return the class of the microinstruction
     */
    @Override
    public String getMicroClass(){
        return "end";
    }

    /**
     * clone the micro instruction
     * @return a clone of this microinstruction
     */
    public Object clone()
    {
        assert false :
                "End.cloneMe() was called.";
        return null; //to satisfy the compiler's need for a return value
    } // end clone()

    /**
     * copies the data from the current micro to a specific micro
     * @param newMicro the micro instruction that will be updated
     */
    public void copyDataTo(Microinstruction newMicro)
    {
        assert false : "End.copyDataTo() was called.";
    } // end copyDataTo()


    /**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
    public boolean uses(Module m)
    {
        return false;
    }


    /**
     * execute the micro instruction from machine
     */
    public void execute()
    {
        machine.getControlUnit().setMicroIndex(0);
        machine.getControlUnit().setCurrentInstruction(
                machine.getFetchSequence());
    } // end execute()

    /**
     * returns the XML description
     * @return the XML description
     */
    public String getXMLDescription()
    {
        return "";
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
    public String getHTMLDescription()
    {
        return "";
    }

} // end End class
