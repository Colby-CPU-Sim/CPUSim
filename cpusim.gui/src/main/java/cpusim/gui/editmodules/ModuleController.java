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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static com.google.common.base.Preconditions.*;

/**
 *
 * FIXME Why does {@link #clones} exist?
 * @param <T>
 */
abstract class ModuleController<T extends Module<T>>
        extends TableView
{
    protected Mediator mediator;
    protected Machine machine;      //the current machine being simulated
    protected Map<T, T> assocList;  //associates the current modules
    //with the edited clones; key = new clone, value = original
    protected ObservableList<T> clones;  //the current clones
    protected Node parentFrame; //for the parent of error messages
    
    protected final Class<T> moduleClass;

    //-------------------------------
    /**
     * Constructor
     * @param mediator holds the information to be shown in tables
     */
    public ModuleController(Mediator mediator, Class<T> moduleClass)
    {
        this.mediator = mediator;
        this.machine = mediator.getMachine();
        assocList = new HashMap<>();
        clones = null;  //subclasses must initialize clones via createClones()
        parentFrame = null;
        
        this.moduleClass = moduleClass;
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
     * returns an array of clones of the current module.
     *
     * @return an array of clones of the current module.
     */
    public List<T> getClones()
    {
        if (clones == null) {
            throw new NullPointerException("clones == null");
        }
        
        return clones;
    }

    /**
     * returns the original module (in the machine) whose clone is given.
     * @param clone the clone of the original module
     * @return the original hardware module of the given clone.
     */
    public T getCurrentFromClone(final Module<?> clone)
    {
        checkNotNull(clone);
        
        return assocList.get(clone);
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
    @SuppressWarnings("unchecked")
    public void setClones(List<? extends Module<?>> clones) {
        // TODO Investigate why this is even used
        FXCollections.copy(this.clones, (List<T>)clones);
    }

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
     * returns a list of updated modules based on the objects
     * in the list.  It replaces the objects in the list with their
     * associated objects, if any, after updating the fields of those old objects.
     *
     * @param list a list of modules
     * @return a list of updated modules
     */
    public final List<T> createNewModulesList(List<? extends T> list)
    {
        List<T> newModules = new ArrayList<>();
        
        for (final T module : list) {
            final T oldModule = assocList.get(module);
            if (oldModule != null) {
                //if the new incr is just an edited clone of an old module,
                //then just copy the new data to the old module
                module.copyTo(oldModule);
                newModules.add(oldModule);
            }
        }
        
        return newModules;
    }
    
    /**
     * Utilizes {@link #getCurrentModules()} to get a list of the {@link Module} in use, then clones the values into
     * new instances placing them into {@link #assocList}.
     *
     * @return Non-{@code null} {@link List} of the cloned objects.
     */
    protected final List<T> loadClonesFromCurrentModules() {
        List<T> currentModules = getCurrentModules();
        
        List<T> clones = new ArrayList<>();
        for (final T m: currentModules) {
            final T clone = m.cloneOf();
            clones.add(clone);
            assocList.put(clone, m);
        }
        
        return clones;
    }
    
    /**
     * Loads all of the values from {@link #getClones()} into the {@link TableView} passed.
     * @param table Table to load into
     */
    protected void loadClonesIntoTableView(TableView<T> table) {
        checkNotNull(table);
 
        ObservableList<T> items = table.getItems();
        getClones().forEach(items::add);
    }
    
    /**
     * returns the class object for the controller's objects
     *
     * @return the class object for the controller's objects
     */
    public final Class<T> getModuleClass() {
        return moduleClass;
    }

    //========================================
    // abstract utility methods to be overridden by each subclass

    /**
     * returns the prototype of the right subclass
     *
     * @return the prototype of the right subclass
     */
    public abstract T getPrototype();

    

    /**
     * returns a list of the current modules
     *
     * @return a list of the current modules
     */
    public abstract ObservableList<T> getCurrentModules();

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