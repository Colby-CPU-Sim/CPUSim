/**
 * Author: Jinghui Yu
 * LastEditingDate: 6/10/2013
 */

/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Changed the return value of checkValidity from a boolean to void (the functionality
 * enabled by that boolean value is now controlled by throwing ValidationException)
 */

package cpusim.gui.editmicroinstruction;

import cpusim.Machine;
import cpusim.Mediator;
import cpusim.Microinstruction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableView;

import java.lang.reflect.Array;
import java.util.HashMap;

/**
 * MicroController class parent of all the microinstruction controller
 */
public abstract class MicroController
        extends TableView  {
    public Mediator mediator;
    public Machine machine;      //the current machine being simulated
    HashMap assocList;  //associates the current micros
    //with the edited clones; key = new clone, value = original
    public Microinstruction[] clones;  //the current clones
    Node parentFrame;   //for the parent of the error dialog

    /**
     * Constructor
     * @param mediator holds the information to be shown in tables
     */
    public MicroController(Mediator mediator)
    {
        this.mediator = mediator;
        this.machine = mediator.getMachine();
        assocList = new HashMap();
        clones = null;
        parentFrame = null;//subclasses must initialize clones by calling createClones()
    }

    /**
     * sets the value of parentFrame.
     */
    public void setParentFrame(Node parent)
    {
        parentFrame = parent;
    }

    /**
     * returns a new Microinstruction with the given name
     *
     * @param newName the name of the new microinstruction
     * @return a new Microinstruction with the given name
     */
    public Microinstruction getNewMicro(String newName)
    {
        Microinstruction prototype = getPrototype();
        Microinstruction clone = (Microinstruction) prototype.clone();
        clone.setName(newName);
        return clone;
    }

    /**
     * returns an array of clones of the current microinstructions.
     *
     * @return an array of clones of the current microinstructions.
     */
    public Microinstruction[] getClones()
    {
        assert clones != null :
                "clones == null in MicroController.getClones()";
        return clones;
    }

    /**
     * returns the original micro (in the machine) whose clone is given.
     * @param clone the clone of the original micro instruction
     * @return the original micro instruction of the given clone.
     */
    public Microinstruction getCurrentFromClone(Microinstruction clone)
    {
        return (Microinstruction) assocList.get(clone);
    }

    /**
     * returns the help page ID
     * @return a string of the ID
     */
    public String getHelpPageID()
    {
        return "";
    }

    //========================================
    // public abstract methods to be overridden by each subclass

    /**
     * sets the clones to the new array.
     * It does not check for validity
     * @param clones the clones that will be set to the new array.
     */
    public abstract void setClones(ObservableList clones);

    /**
     * update the machine's micros from the array of clones
     */
    public abstract void updateCurrentMicrosFromClones();

    /**
     * check if the given list of micro instructions have valid values.
     * @param objects the list of micro instructions
     * @return true if the array of objects have valid properties
     */
    public abstract void checkValidity(ObservableList objects);

    /**
     * returns the type of the controller
     *
     * @return the type of the controller
     */
    public abstract String toString();

    //========================================
    // utility methods

    /**
     * creates an array of clones of the current microinstructions,
     * adding the appropriate ChangeListeners to its properties
     *
     * @return the clones of the current instructions
     */
    public Object[] createClones()
    {
        ObservableList currentMicros = getCurrentMicros();
        Microinstruction[] clones = (Microinstruction[])Array.newInstance(
                this.getMicroClass(), currentMicros.size());
        for (int i = 0; i < currentMicros.size(); i++) {
            Microinstruction clone = (Microinstruction)
                    ((Microinstruction) currentMicros.get(i)).clone();
            clones[i] = clone;
            assocList.put(clone, currentMicros.get(i));
        }
        return clones;
    }

    /**
     * returns a list of updated microinstructions based on the objects
     * in the list.  It replaces the objects in the list with their
     * associated objects, if any, after updating the fields of those old objects.
     * It also sorts the micros by name.
     *
     * @param list a list of micro instructions
     * @return a list of updated micro instructions
     */
    public ObservableList createNewMicroList(Microinstruction[] list)
    {
        ObservableList newMicros = FXCollections.observableArrayList();
        for (int i = 0; i < list.length; i++) {
            Microinstruction micro = list[i];
            Microinstruction oldMicro = (Microinstruction) assocList.get(micro);
            if (oldMicro != null) {
                //if the new micro is just an edited clone of an old micro,
                //then just copy the new data to the old micro
                micro.copyDataTo(oldMicro);
                newMicros.add(oldMicro);
            }
            else
                newMicros.add(micro);
        }
        return sortVectorByName(newMicros);
    }

    /**
     * sorts the given list of Microinstructions in place by name
     * using Selection Sort.  It returns the modified ObservableList.
     *
     * @param micros a list of micro instructions to be sorted
     * @return a list of sorted micro instruction
     */
    private ObservableList sortVectorByName(ObservableList micros)
    {
        for (int i = 0; i < micros.size() - 1; i++) {
            //find the smallest from positions i to the end
            String nameOfSmallest =
                    ((Microinstruction) micros.get(i)).getName();
            int indexOfSmallest = i;
            for (int j = i + 1; j < micros.size(); j++) {
                Microinstruction next = (Microinstruction) micros.get(j);
                if (next.getName().compareTo(nameOfSmallest) < 0) {
                    indexOfSmallest = j;
                    nameOfSmallest = next.getName();
                }
            }
            //swap smallest into position i
            Object temp = micros.get(i);
            micros.set(i, micros.get(indexOfSmallest));
            micros.set(indexOfSmallest, temp);
        }
        return micros;
    }
    
    //========================================
    // methods inherited from Controller interface

    /**
     * returns a new microinstruction object with the given name
     *
     * @param name the name of the object.
     * @return a new microinstruction
     */
    public Object getNewObject(String name)
    {
        return getNewMicro(name);
    }

    //========================================
    // abstract utility methods to be overridden by each subclass

    /**
     * returns the prototype of the right subclass
     *
     * @return the prototype of the right subclass
     */
    public abstract Microinstruction getPrototype();

    /**
     * returns the class object for the controller's objects
     *
     * @return the class object for the controller's objects
     */
    public abstract Class getMicroClass();

    /**
     * returns a list of the current microinstructions
     *
     * @return a list of the current microinsturctions
     */
    public abstract ObservableList getCurrentMicros();

    /**
     * checks if new micros of this class can be created.
     * This may be false, for example, if you wanted to fromRootController
     * a Transfer before any Registers have been created.
     * @return true if new micros of this class can be created.
     */
    public abstract boolean newMicrosAreAllowed();

    /**
     * updates the table by removing all the items and adding all back.
     * for refreshing the display.
     */
    public abstract void updateTable();

}
