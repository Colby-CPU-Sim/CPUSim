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

import cpusim.Mediator;
import cpusim.gui.util.FXMLLoaderFactory;
import cpusim.model.Machine;
import cpusim.model.MachineInstruction;
import cpusim.model.Microinstruction;
import cpusim.model.util.Copyable;
import cpusim.model.util.NamedObject;
import cpusim.util.Dialogs;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * MicroController class parent of all the microinstruction controller
 *
 * @author Kevin Brightwell (Nava2)
 */
abstract class MicroController<T extends Microinstruction & Copyable<T>>
        extends TableView<T>  {
    protected final Mediator mediator;
    protected final Machine machine;      //the current machine being simulated
    
    private final Class<T> microClass;
    
    private Node parentFrame;   //for the parent of the error dialog

    /**
     * Constructor
     * @param mediator holds the information to be shown in tables
     */
    MicroController(Mediator mediator, final String fxmlFile, Class<T> clazz) {
        this.mediator = mediator;
        this.machine = mediator.getMachine();
        this.microClass = clazz;
        
        this.parentFrame = null;//subclasses must initialize clones by calling createClones()
    
        FXMLLoader fxmlLoader = FXMLLoaderFactory.fromRootController(this, checkNotNull(fxmlFile));
    
        try {
            fxmlLoader.load();
        } catch (IOException ioe) {
            // should never happen
            throw new IllegalStateException("Unable to load file: " + fxmlFile, ioe);
        }
    }

    /**
     * sets the value of parentFrame.
     */
    void setParentFrame(Node parent)
    {
        parentFrame = parent;
    }
    
    /**
     * Get the stored class.
     * @return The internal {@link Class} of the items
     */
    final Class<T> getMicroClass() {
        return microClass;
    }

    /**
     * returns a new Microinstruction with the given name
     *
     * @param newName the name of the new microinstruction
     * @return a new Microinstruction with the given name
     */
    private T getNewMicro(String newName)
    {
        T clone = getPrototype().cloneOf();
        clone.setName(newName);
        return clone;
    }

    /**
     * returns the help page ID
     * @return a string of the ID
     */
    public String getHelpPageID()
    {
        return "";
    }
    
    /**
     * update the machine's micros from {@link #getItems()}
     */
    public abstract void updateMachineFromItems();

    /**
     * check if the given list of micro instructions have valid values.
     * @param micros the list of micro instructions
     */
    public void checkValidity(ObservableList<T> micros) {
        // check that all names are unique and nonempty
        NamedObject.validateUniqueAndNonempty(micros);
    }
    
    /**
     * Calles {@link #checkValidity(ObservableList)} with the items in the {@link #getItems()}.
     */
    public final void checkValidity() {
        checkValidity(getItems());
    }

    /**
     * returns the type of the controller
     *
     * @return the type of the controller
     */
    public abstract String toString();
    
    //========================================
    // methods inherited from Controller interface

    /**
     * returns a new microinstruction object with the given name
     *
     * @param name the name of the object.
     * @return a new microinstruction
     */
    private T getNewObject(String name)
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
    // FIXME Make this deprecated, use a Default Constructor
    public abstract T getPrototype();
    
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
    public void updateTable() {
        double w = getWidth();
        setPrefWidth(w-1);
        setPrefWidth(w);
    }
    
    
    /**
     * Called by the UI when the duplicate button is clicked.
     */
    public void createDuplicateEntry() {
        T newObject = getPrototype().cloneOf();
        String uniqueName = NamedObject.createUniqueDuplicatedName(getItems(), newObject);
        newObject.setName(uniqueName);
        getItems().add(0, newObject);
        
        //update display
        updateTable();
        scrollTo(1);
        getSelectionModel().select(0);
    }
    
    /**
     * Called by an owner when a new entry should be added
     */
    public void createNewEntry() {
    
        //add a new item at the end of the list.
        String uniqueName = NamedObject.createUniqueName(getItems());
        T newObject = getNewObject(uniqueName);
    
        // A really ugly hack to fromRootController a unique opcode
        // required by InstructionDialog
        /**
         if (EditDialog.this instanceof InstructionDialog) {
         MachineInstruction instr = (MachineInstruction) newObject;
         long uniqueOpcode =
         ((MachineInstrFactory) getCurrentFactory()
         ).createUniqueOpcode(model.getAllObjects());
         instr.setOpcode(uniqueOpcode);
         }*/
        getItems().add(0, newObject);
        updateTable();
    }
    
    /**
     * Deletes an entry at {@code index}.
     *
     * @param index The index to remove.
     */
    public void deleteEntry(int index) {
        final T theMicro = getItems().get(index);
    
        //first see if it is used by any machine instructions and,
        // if so, warn the user.
        List<MachineInstruction> instrsThatUseIt = mediator.getMachine().getInstructionsThatUse(theMicro);
        if (instrsThatUseIt.size() > 0) {
            String message = theMicro + " is used by the " +
                    "following machine instructions: \n  ";
            for (int i = 0; i < instrsThatUseIt.size(); i++)
                message += instrsThatUseIt.get(i) + " ";
            message += ".\nReally delete it?";
        
            Alert dialog = Dialogs.createConfirmationDialog(parentFrame.getScene().getWindow(),
                    "Confirm Deletion", message);
            final Optional<ButtonType> result = dialog.showAndWait();
            
            if (result.isPresent()) {
                final ButtonType res = result.get();
                if(res == ButtonType.CANCEL ||
                        res == ButtonType.NO ||
                        res == ButtonType.CLOSE)
                    return; //don't delete anything
            }
        }
    
        // actually remove it:
        getItems().remove(index);
    
        if (index == 0) {
            getSelectionModel().select(0);
        }
        else{
            getSelectionModel().select(index - 1);
        }
    }

}
