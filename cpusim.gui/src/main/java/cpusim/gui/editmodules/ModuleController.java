/**
 * Author: Jinghui Yu
 * Editing date: 7/29/2013
 */

/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Changed the return value of checkValidity from a boolean to void (the functionality
 * enabled by that boolean value is now controlled by throwing ValidationException)
 */
package cpusim.gui.editmodules;

import cpusim.model.Machine;
import cpusim.Mediator;
import cpusim.model.Module;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableView;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Vector;

public abstract class ModuleController
        extends TableView
{
    public Mediator mediator;
    public Machine machine;      //the current machine being simulated
    HashMap assocList;  //associates the current modules
    //with the edited clones; key = new clone, value = original
    public Module[] clones;  //the current clones
    Node parentFrame; //for the parent of error messages

    //-------------------------------
    /**
     * Constructor
     * @param mediator holds the information to be shown in tables
     */
    public ModuleController(Mediator mediator)
    {
        this.mediator = mediator;
        this.machine = mediator.getMachine();
        assocList = new HashMap();
        clones = null;  //subclasses must initialize clones via createClones()
        parentFrame = null;
    }


    //========================================
    // public methods

    /**
     * sets the value of parentFrame.
     */
    public void setParentFrame(Node parent)
    {
        parentFrame = parent;
    }

    /**
     * returns a new Module with the given name
     *
     * @param newName the name of the new hardware module
     * @return a new hardware module with the given name
     */
    public Module getNewModule(String newName)
    {
        Module prototype = getPrototype();
        Module clone = (Module) prototype.clone();
        clone.setName(newName);
        return clone;
    }

    /**
     * returns an array of clones of the current module.
     *
     * @return an array of clones of the current module.
     */
    public Module[] getClones()
    {
        assert clones != null :
                "clones == null in ModuleFactory.getClones()";
        return clones;
    }

    /**
     * returns the original module (in the machine) whose clone is given.
     * @param clone the clone of the original module
     * @return the original hardware module of the given clone.
     */
    public Module getCurrentFromClone(Module clone)
    {
        return (Module) assocList.get(clone);
    }

    /**
     * returns the help page ID
     * @return a string of the ID
     */
    public String getHelpPageID()
    {
        return "Hardware Modules";
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
     * check if the given list of micro instructions have valid values.
     */
    public abstract void checkValidity();

    /**
     * returns the type of the controller
     *
     * @return the type of the controller
     */
    public abstract String toString();

    //-----------------------------------
    // returns an array of Strings representing the various properties
    // of the Module factory
    // public abstract String[] getProperties();

    //========================================
    // utility methods

    /**
     * creates an array of clones of the current modules,
     * adding the appropriate ChangeListeners to its properties
     *
     * @return the clones of the current modules
     */
    public Module[] createClones()
    {
        ObservableList<? extends Module> currentModules = getCurrentModules();
        Module[] clones = (Module[])
                Array.newInstance(this.getModuleClass(), currentModules.size());
        for (int i = 0; i < currentModules.size(); i++) {
            Module clone = (Module) currentModules.get(i).clone();
            clones[i] = clone;
            assocList.put(clone, currentModules.get(i));
        }
        return clones;
    }

    /**
     * returns a list of updated modules based on the objects
     * in the list.  It replaces the objects in the list with their
     * associated objects, if any, after updating the fields of those old objects.
     * It also sorts the micros by name.
     *
     * @param list a list of modules
     * @return a list of updated modules
     */
    public Vector<Module> createNewModulesList(Module[] list)
    {
        Vector<Module> newModules = new Vector<>();
        for (Module module : list) {
            Module oldModule = (Module) assocList.get(module);
            if (oldModule != null) {
                //if the new incr is just an edited clone of an old module,
                //then just copy the new data to the old module
                module.copyDataTo(oldModule);
                newModules.addElement(oldModule);
            }
            else {
                if (module instanceof Register) {
                    ((Register) module).setValue(((Register) module).getInitialValue());
                }
                if (module instanceof RegisterArray) {
                    RegisterArray registerArray = (RegisterArray) module;
                    for (Register r : registerArray.registers())
                        r.setValue(r.getInitialValue());
                }
                newModules.addElement(module);
            }
        }
        return newModules;
    }

    //========================================
    // methods inherited from Controller interface

    /**
     * returns a new module object with the given name
     *
     * @param name the name of the object.
     * @return a new module
     */
    public Object getNewObject(String name)
    {
        return getNewModule(name);
    }

    //========================================
    // abstract utility methods to be overridden by each subclass

    /**
     * returns the prototype of the right subclass
     *
     * @return the prototype of the right subclass
     */
    abstract Module getPrototype();

    /**
     * returns the class object for the controller's objects
     *
     * @return the class object for the controller's objects
     */
    abstract Class getModuleClass();

    /**
     * returns a list of the current modules
     *
     * @return a list of the current modules
     */
    public abstract ObservableList<? extends Module> getCurrentModules();

    /**
     * checks if new modules of this class can be created.
     * This may be false, for example, if you wanted to fromRootController
     * a Transfer before any Registers have been created.
     * @return true if new modules of this class can be created.
     */
    public abstract boolean newModulesAreAllowed();

    /**
     * updates the table by removing all the items and adding all back.
     * for refreshing the display.
     */
    public abstract void updateTable();

}