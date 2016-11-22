///////////////////////////////////////////////////////////////////////////////
// File:    	ControlUnit.java
// Author:		Dale Skrien
// Project: 	CPU Sim
// Date:    	May, 2001
//
// Description:
//   This file contains the code for the ControlUnit module.
//   This class is currently unused.


///////////////////////////////////////////////////////////////////////////////
// the package in which our project resides

package cpusim.model.module;

import cpusim.model.Machine;
import cpusim.model.MachineInstruction;
import cpusim.model.Module;
import java.io.Serializable;

import static com.google.common.base.Preconditions.*;

/**
 * The Control Unit class
 */
public class ControlUnit extends Module<ControlUnit> implements Serializable
{

    private MachineInstruction currentInstruction;
    //the instr currently or about to be executed
    private int microIndex;
    //the index of the micro sequence
    private Machine machine;
    //the machine of which this is the control unit

    /**
     * Constructor
     * @param name name of the control unit
     * @param machine machine that implement it
     */
    public ControlUnit(String name, Machine machine)
    {
        super(name);
        microIndex = 0;
        currentInstruction = machine.getFetchSequence();
        this.machine = machine;
    }

    /**
     * getter for the micro index
     * @return the micro index
     */
    public int getMicroIndex()
    {
        return this.microIndex;
    }

    /**
     * setter for the micro index
     * @param index new index value
     */
    public void setMicroIndex(int index)
    {
        this.microIndex = index;
    }

    /**
     * increment the index of micro instruction
     * @param amount specify how much to increment
     */
    public void incrementMicroIndex(int amount)
    {
        this.microIndex += amount;
    }

    /**
     * getter for the current instruction
     * @return the current instruction
     */
    public MachineInstruction getCurrentInstruction()
    {
        return this.currentInstruction;
    }

    /**
     * setter for the current instruction
     * @param instr the new instruction to be set as the current one
     */
    public void setCurrentInstruction(MachineInstruction instr)
    {
        this.currentInstruction = instr;
    }

    /**
     * reset everything
     */
    public void reset()
    {
        microIndex = 0;
        currentInstruction = machine.getFetchSequence();
    }

    /**
     * copies the data from the current module to a specific module
     * @param newControl the micro instruction that will be updated
     */
    @Override
    public void copyTo(ControlUnit newControl)
    {
    	checkNotNull(newControl);
    	
        newControl.setMicroIndex(microIndex);
        newControl.setCurrentInstruction(currentInstruction);
        newControl.machine = machine;
    }
    
    @Override
    protected void validateState() {
        // no-op
    }
    
    /**
     * returns the XML description
     * @return the XML description
     */
    @Override
    public String getXMLDescription(String indent)
    {
        return getHTMLName();
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
    @Override
    public String getHTMLDescription(String indent)
    {
        return getHTMLName();
    }

    /**
     * getter for the current state
     * @return the current state property
     */
    public State getCurrentState()
    {
        return new State(currentInstruction, microIndex);
    }

    /**
     * reset the state
     * @param state the state that should be reset
     */
    public void restoreState(State state)
    {
        state.restoreControlUnitToThisState();
    }

    /**
     * This inner class represents the state of the ControlUnit at any point
     * in time by storing the current instruction and the index of the current
     * micro.
     */
    public class State
    {
        private MachineInstruction  instr;
        private int microIndex;

        public State(MachineInstruction instr, int microIndex)
        {
            this.instr = instr;
            this.microIndex = microIndex;
        }

        public void restoreControlUnitToThisState()
        {
            ControlUnit.this.currentInstruction = instr;
            ControlUnit.this.microIndex = microIndex;
        }

        public int getIndex() { return microIndex; }

        public MachineInstruction getInstr() { return instr; }
    }

}
