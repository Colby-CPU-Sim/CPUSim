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
import cpusim.model.util.MachineComponent;
import javafx.beans.property.*;
import org.fxmisc.easybind.EasyBind;

import java.util.UUID;

import static com.google.common.base.Preconditions.*;

/**
 * The Control Unit class
 */
public class ControlUnit extends Module<ControlUnit> {
    
    //the instr currently or about to be executed
    @DependantComponent
    private final ObjectProperty<MachineInstruction> currentInstruction;
    
    //the index of the micro sequence
    private final IntegerProperty microIndex;

    private final ReadOnlySetProperty<MachineComponent> dependencies;


    /**
     * Constructor
     * @param name name of the control unit
     *
     */
    public ControlUnit(String name, UUID id, Machine machine) {
        super(name, id, machine);
        microIndex = new SimpleIntegerProperty(0);
        currentInstruction = new SimpleObjectProperty<>(this, "currentInstruction", machine.getFetchSequence());

        // When the current instruction changes, we reset the value to 0
        EasyBind.subscribe(currentInstruction, newValue -> setMicroIndex(0));

        this.dependencies = MachineComponent.collectDependancies(this)
                .buildSet(this, "dependencies");
    }

    @Override
    public ReadOnlySetProperty<MachineComponent> getDependantComponents() {
        return dependencies;
    }

    /**
     * getter for the micro index
     * @return the micro index
     */
    public int getMicroIndex()
    {
        return this.microIndex.get();
    }

    /**
     * setter for the micro index
     * @param index new index value
     */
    public void setMicroIndex(int index) {
        this.microIndex.set(index);
    }

    public IntegerProperty microIndexProperty() {
        return microIndex;
    }

    /**
     * increment the index of micro instruction
     * @param amount specify how much to increment
     */
    public void incrementMicroIndex(int amount) {
        this.microIndex.add(amount);
    }

    public ObjectProperty<MachineInstruction> currentInstructionProperty() {
        return currentInstruction;
    }

    /**
     * getter for the current instruction
     * @return the current instruction
     */
    public MachineInstruction getCurrentInstruction()
    {
        return this.currentInstruction.get();
    }

    /**
     * setter for the current instruction
     * @param instr the new instruction to be set as the current one
     */
    public void setCurrentInstruction(MachineInstruction instr)
    {
        this.currentInstruction.set(instr);
    }

    /**
     * reset everything
     */
    public void reset()
    {
        microIndex.set(0);
        currentInstruction.set(getMachine().getFetchSequence());
    }

    @Override
    public ControlUnit cloneFor(MachineComponent.IdentifierMap oldToNew) {
        checkNotNull(oldToNew);

        ControlUnit newInst = new ControlUnit(getName(), UUID.randomUUID(), oldToNew.getNewMachine());
        oldToNew.copyProperty(this, newInst, ControlUnit::currentInstructionProperty);

        return newInst;
    }

    @Override
    public <U extends ControlUnit> void copyTo(U other) {
        checkNotNull(other);

        other.setMicroIndex(getMicroIndex());
        other.setCurrentInstruction(getCurrentInstruction());
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
        return new State(currentInstruction.get(), microIndex.get());
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
        private MachineInstruction instr;
        private int microIndex;

        public State(MachineInstruction instr, int microIndex)
        {
            this.instr = instr;
            this.microIndex = microIndex;
        }

        public void restoreControlUnitToThisState()
        {
            ControlUnit.this.currentInstruction.setValue(instr);
            ControlUnit.this.microIndex.set(microIndex);
        }

        public int getIndex() { return microIndex; }

        public MachineInstruction getInstr() { return instr; }
    }

}
