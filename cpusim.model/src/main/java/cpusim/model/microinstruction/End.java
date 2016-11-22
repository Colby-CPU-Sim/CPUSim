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
    
//    /**
//     * Copy constructor
//     * @param other
//     */
//    public End(End other) {
//      No copy constructor allowed.
//    	super(other.getName(), other.machine);
//    }
    
    /**
     * returns the class of the microinstruction
     * @return the class of the microinstruction
     */
    @Override
    public String getMicroClass(){
        return "end";
    }
    
    @Override
    protected void validateState() {
        // nothing to validate
    }
    
    /**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
    @Override
    public boolean uses(Module<?> m)
    {
        return false;
    }


    /**
     * execute the micro instruction from machine
     */
    @Override
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
    @Override
    public String getXMLDescription(String indent)
    {
        return "";
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
    @Override
    public String getHTMLDescription(String indent)
    {
        return "";
    }

} // end End class
