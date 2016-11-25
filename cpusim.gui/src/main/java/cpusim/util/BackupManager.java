///////////////////////////////////////////////////////////////////////////////
//File:    	BackupManager.java
//Type:    	java application file
//Author:		Dale Skrien
//Project: 	CPU Sim
//Date:    	July, 2000

//Description:
//This file contains the class that manages the backup operation
//by saving and restoring the previous state of the machine.  It
//needs to save the old values of the registers, register arrays,
//and RAMs.  If any of those modules changes in any way other than
//the value they contain, this manager flushes all the backup info.

//To be done:


///////////////////////////////////////////////////////////////////////////////
//the package in which our file resides

/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 *
 * 1). Changed restoreMicroChanges so that it back up the IOChannel when undo the IO micro.
 */
package cpusim.util;

import cpusim.model.Machine;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.microinstruction.IO;
import cpusim.model.module.ControlUnit;
import cpusim.model.module.RAM;
import cpusim.model.module.RAMLocation;
import cpusim.model.module.Register;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Stack;


///////////////////////////////////////////////////////////////////////////////
//the BackupManager class
//
///////////////////////////////////////////////////////////////////////////////
// Modifications by Cadran and Tom:
// A method called backupOneMicroInstruction was created that allows the user
// to backup by microinstructions when debugging.
//
// The backupOneMachineInstruction method was refactored and
// backRAMorRegisterChange was extracted as an independent method that is now
// used in the backupOneMicroInstruction  method and the
// backupOneMachineInstruction method.
//
// The backupAllTheWay method was refactored to include a javadoc comment.
// We also added the resetMicroWindow method into the body of the method so
// that the microInstruction window that is displayed during debugging resets
// when we back up to a state pre-execution.
//
// The flushBackups. propertyChangeListener and restoreRAMandRegisterValues
// methods were refactored to include a javadoc comments.
//
// The startNewBackupInstructionState was refactored and modified.  We added a
// javadoc comment.  We renamed the method so it was more intention revealing.
// We also modified it so that it would take our new data structure (HashMaps
// of microinstruction changes that are elements in a stack of microinstructions
// that are elements in  a stack of machineInstructions).
//
// We created a startNewBackupMicroState method.  This pushes a stack of
// microinstructions containing empty HashMaps into each element of the
// machineInstructionStack.  This method also puts two key, value pairs
// into each otherwise empty hashmap for each microInstruction.  One of these key,
// value pairs keeps track of what index value this microinstruction has within
// its parent machineInstruction.  The other key, value pair keeps track of what
// machineInstruction this microinstrucion belongs to.  These values will be used
// when updating the controlUnit while performing backing up operations in the
// debugger.
//
// We renamed the canBackupOneMachineInstr and added a canBackupOneMicroInstr
// that has the same function, but for microinstructions.
//
// The getter and setter methods for getting/setting the machineInstruction after
// backing up were renamed getBackupMachineInstructions and
// setBackupMachineInstruction.
// The return type was given the generic Stack<Stack<HashMap>>,
// as was the the setter parameter.
///////////////////////////////////////////////////////////////////////////////

public class BackupManager
            implements ChangeListener<Object>
{
	// FIXME Create a Machine#ChangeSet or something similar that can then be stored, this current setup
	// FIXME is not good enough and creates some scary type issues.

	//instance variables
    private boolean listening; //if false, ignore property changes
    private Stack<Stack<HashMap>> machineInstructionStack;
                            //stack elements will be microinstruction stacks
	//------------------------------
	// constructor

	public BackupManager()
	{
        this.machineInstructionStack = new Stack<>();
        listening = false;
    }


	/**
	 * Restores the state of the machine back to the start of previous
	 * microinstruction
	 */
	// C.T. method added
	public void backupOneMicroInstruction()
	{
		if(canBackupOneMachineInstr()){
			if (canBackupOneMicroInstr()){
				restoreMicroChanges();
			}
			else{
				machineInstructionStack.pop(); //top stack was empty so dump it
                backupOneMicroInstruction(); //try again
            }
		}
	}


    public void setListening(boolean listening)
    {
        this.listening = listening;
    }


    /**
	 * Replace Register or RAM value stored with old value stored in HashMap.
     * Precondition:  The machineInstructionStack is not empty and its top
     *                stack is not empty
	 */
	//C.T. refactored: extracted method
	private void restoreMicroChanges() {
		HashMap table = machineInstructionStack.peek().pop();
		//the restoration will cause all restorations to be saved in
		//a backup state, so we'll fromRootController a new segment here and then
		//dump it after doing the restorations.
		startNewBackupMicroState(null);

		//CT update control unit to reflect where we have backed up to
		ControlUnit.State state = (ControlUnit.State)
                                            table.remove("control unit state");
		state.restoreControlUnitToThisState();

        Microinstruction currentMicro = state.getInstr().getMicros().get(state.getIndex());
        if ( currentMicro instanceof IO ){
            ((IO) currentMicro).undoExecute();
        }

        for (Object module : table.keySet()) {
            if (module instanceof Register) {
                ((Register) module).setValue((Long) table.get(module));
            }
            else {
                RAM ram = (RAM) module;
                HashMap dataTable = (HashMap) table.get(ram);
                for (Object aD : dataTable.keySet()) {
                    Integer indexObject = (Integer) aD;
                    long value =
                            (Long) dataTable.get(aD);
                    ram.setData(indexObject, value);
                }
            }
        }
        //now dump the backup state full of these restoration changes.
		//theoretically, we could save this state for undoing the backup,
		//if we ever wanted to implement such a feature.
        machineInstructionStack.peek().pop();
	}
	/**
	 * Restore the state of the machine back to start of last
	 * fetch sequence.
	 */
	//C.T. Refactored and modified
	public void backupOneMachineInstruction()
	{
		if(canBackupOneMachineInstr())
		{
			while (canBackupOneMicroInstr()){
				backupOneMicroInstruction();
			}
			//pop the empty machine instruction
			machineInstructionStack.pop();
		}
	}
	/**
	 * Restore the state of the machine using all saved backup
	 * information
	 */
	//C.T. Refactored: Javadoc comment, modified
	public void backupAllTheWay()
	{
		while (canBackupOneMachineInstr())
			backupOneMachineInstruction();
	}

	/**
	 * Remove all saved backup information.  This is done
	 * when the modules change in a way other than their values.
	 */
	//C.T.  Refactored: Javadoc comment
	public void flushBackups()
	{
        backupAllTheWay();
        machineInstructionStack.clear();
	}

	/**
	 * Pushes a new Stack of micro Stacks which contain
	 * backup changes
	 */
	//	C.T. Refactored: Javadoc comment, renamed method, modified
	public void startNewBackupInstructionState()
	{
		machineInstructionStack.push(new Stack<>());
	}

	/**
	 * Pushes a microinstruction stack containing a Hashmap
	 * that will contain backup changes
     * @param state the current state of the control unit, which will be
     *              saved in the hash map.
     */
	//C.T. created method
	public void startNewBackupMicroState(ControlUnit.State state){
		HashMap microChanges = new HashMap();
		machineInstructionStack.peek().push(microChanges);
		microChanges.put("control unit state", state);
	}

    /**
	 * Returns true if there are backup machineinstruction states currently saved
	 * @return boolean value
	 */
	//C.T. Refactored: renamed
	public boolean canBackupOneMachineInstr()
	{
		return (!machineInstructionStack.empty());
	}
	/**
	 * Returns true if there are backup microinstruction states currently saved
	 * @return boolean value
	 */
	//C.T. New method
	public boolean canBackupOneMicroInstr()
	{
		return canBackupOneMachineInstr() &&
                !machineInstructionStack.peek().empty();
	}


	//--------------------------------------
	// --- PropertyChangeListener Methods---
	//--------------------------------------

	/**
	 * Receive notification that a module has modified a property
     * This method is called in the SwingWorker's thread in
     * Machine.execute() and so no GUI stuff is allowed here.
     * HACK--If either the machineInstructionStack or the topmost micro stack
     * get too big (>100,000), the BackupManager just stops saving the
     * backup states to avoid an OutOfMemoryError.
	 * @param event
     * @param oldState
     * @param newState
	 */
	//C.T. Refactored: javadoc comment
	public void changed(ObservableValue<? extends Object> event,
                        Object oldState,
                        Object newState)
	{

		if (! listening) {
            //if not in debug mode, ignore all changes.
        }
        else if (machineInstructionStack.size() > 10000 ||
                (machineInstructionStack.size() > 0 &&
                   machineInstructionStack.peek().size() > 10000)) {
            //Likely there is an infinite loop in the program
            //so to avoid OutOfMemoryError we will stop saving state
            //and instead do nothing.  We could put up a warning dialog
            //to the user here. But we have to access to the stage.
            
            /*Dialogs.showWarningDialog(null, "There is an infinite loop in the program " +
                    "so to avoid OutOfMemoryError we will stop saving state " +
                    "and instead do nothing.","Warning", "Debug Mode");*/

        }
        else if (((Property)event).getBean() instanceof Machine) {
            if (((Machine.StateWrapper)newState).getState() == Machine.State.START_OF_MACHINE_CYCLE) {
                startNewBackupInstructionState();
            }
            //C.T.
            else if (((Machine.StateWrapper)newState).getState() == Machine.State.START_OF_MICROINSTRUCTION) {
                if (canBackupOneMachineInstr()){
                    startNewBackupMicroState((ControlUnit.State)((Machine.StateWrapper) newState).getValue());
                }
            }
        }
        else if (canBackupOneMachineInstr()) {
            storeOldRAMandRegisterValues(event, oldState, newState);
        }
    }


	/**
	 * save old Ram and Register Values changed during execution.
     * This method is executed in the SwingWorker's thread in Machine.execute
     * so no GUI things are allowed here.
	 *
	 * @param event PropertyChangeEvent
	 * @param oldState old value
     * @param newState new value
	 */
	//C.T. Refactored: added javadoc comment
	private void storeOldRAMandRegisterValues(ObservableValue event,
			Object oldState, Object newState)
	{
		Stack<HashMap> tempMicroStack = machineInstructionStack.peek();
		HashMap table = tempMicroStack.peek();
		if (((Property)event).getBean() instanceof RAM) {
			//it was a RAM's data that changed
			RAM ram = (RAM) ((Property)event).getBean();
			HashMap dataTable = (HashMap) table.get(ram);
			if (dataTable == null) {
				dataTable = new HashMap();
				table.put(ram, dataTable);
			}

			ObservableList<RAMLocation> changedData = (ObservableList) newState;
			for (RAMLocation data : changedData){
				dataTable.put((int)(data.getAddress()+0), data.getValue());
            }
		}
		else if (((Property)event).getBean() instanceof Register) {
			//it was a Register that changed
			Register register = (Register) ((Property)event).getBean();
			if (table.get(register) == null) {
				//the register's old value has not yet been saved
				table.put(register, oldState);
			}
		}
	}

    /**
     * Returns a HashMap holding the register and ram changes caused by the
     * most recent microinstruction execution.
     * If no changes exist, returns an empty HashMap.
     *
     * @return HashMap containing most recent changes to RAM and Reg.  The keys
     *         are modules and the values are another HashMap if the module is
     *         of type RAM and otherwise the values are the old Integer values
     *         for registers.
     */
    //Charlie and Mike:  11/06
    public HashMap getLatestBackup()
    {
        if(machineInstructionStack.empty() ||
                machineInstructionStack.peek().empty()) {
            return new HashMap();
        }
        else {
            return machineInstructionStack.peek().peek();
        }
    }

} //end of class BackupManager
