/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Changed the return value of checkValidity from a boolean to void (the functionality
 * enabled by that boolean value is now controlled by throwing ValidationException)
 * 2.) Changed the edit commit method on the name column so that it calls Validate.nameableObjects()
 * which throws a ValidationException in lieu of returning a boolean value
 * 3.) Moved widthsAreInBound and initialValueAreInBound methods to the Validate class and changed the return value to void
 * from boolean
 */

package cpusim.gui.editmodules;

import cpusim.Mediator;
import cpusim.gui.util.EditingNonNegativeIntCell;
import cpusim.gui.util.EditingStrCell;
import cpusim.gui.util.NamedColumnHandler;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import cpusim.model.util.Validate;

import cpusim.util.ValidateControllers;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.net.URL;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The controller for editing the Register arrays in the EditModules dialog.
 */
public class RegisterArrayTableController
        extends ModuleController<RegisterArray> implements Initializable {

    @FXML @SuppressWarnings("unused")
    private TableColumn<RegisterArray,String> name;
    @FXML @SuppressWarnings("unused")
    private TableColumn<RegisterArray,Integer> length;

    @FXML @SuppressWarnings("unused")
    private TableColumn<RegisterArray,Integer> width;

    private ConditionBitTableController bitController;

    /**
     * Constructor
     * @param mediator holds the machine and information needed
     */
    RegisterArrayTableController(Mediator mediator){
        super(mediator, "RegisterArrayTable.fxml", RegisterArray.class);
    }

    /**
     * initializes the dialog window after its root element has been processed.
     * makes all the cells editable and the use can edit the cell directly and
     * hit enter to save the changes.
     *
     * @param url the location used to resolve relative paths for the root
     *            object, or null if the location is not known.
     * @param rb  the resources used to localize the root object, or null if the root
     *            object was not localized.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        name.prefWidthProperty().bind(prefWidthProperty().divide(100/34.0));
        length.prefWidthProperty().bind(prefWidthProperty().divide(100/33.0));
        width.prefWidthProperty().bind(prefWidthProperty().divide(100/33.0));

        Callback<TableColumn<RegisterArray,String>,TableCell<RegisterArray,String>> cellStrFactory =
                setStringTableColumn -> new EditingStrCell<>();
        Callback<TableColumn<RegisterArray,Integer>,TableCell<RegisterArray,Integer>> cellIntFactory =
                setIntegerTableColumn -> new EditingNonNegativeIntCell<>();

        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        length.setCellValueFactory(new PropertyValueFactory<>("length"));
        width.setCellValueFactory(new PropertyValueFactory<>("width"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(new NamedColumnHandler<>(this));

        length.setCellFactory(cellIntFactory);
        length.setOnEditCommit(text -> text.getRowValue().setLength(text.getNewValue()));

        width.setCellFactory(cellIntFactory);
        width.setOnEditCommit(
                text -> text.getRowValue().setWidth(text.getNewValue())
        );
    }

    /**
     * assigns the given bitController to the instance variable by that name
     * @param bitController the bitController used for this controller
     */
    public void setBitController(ConditionBitTableController bitController)
    {
        this.bitController = bitController;
    }

    /**
     * getter for prototype of the right subclass
     * @return the prototype of the subclass
     */
    @Override
    public RegisterArray getPrototype() {
        return new RegisterArray("???",4, 32);
    }

    /**
     * returns a string of the types of the controller
     * @return a string of the types of the controller
     */
    @Override
    public String toString()
    {
        return "RegisterArray";
    }
    
    /**
     * Check validity of array of Objects' properties.
     */
    public void checkValidity()
    {
        // convert the array to an array of RegisterArrays

        //build up a HashMap of old registers and new widths
        final Map<Register, Integer> h = new HashMap<>();
        final List<RegisterArray> registerArrays = getItems();
        for (final RegisterArray array : registerArrays) {
            getAssociated(array).ifPresent(oldArray -> {
                if (array.getWidth() != oldArray.getWidth()) {
                    for (int j = 0; j < Math.min(array.getLength(), oldArray.getLength()); j++) {
                        h.put(oldArray.registers().get(j), array.getWidth());
                    }
                }
            });
        }

        //now do all the tests
        for (RegisterArray registerArray : registerArrays) {
            Validate.initialValuesAreInbound(registerArray.registers());
        }
        Validate.registerArraysRangesInBound(registerArrays);
        Validate.registerWidthsAreOkayForMicros(machine, h);
        
        ValidateControllers.registerArrayWidthsAreOkay(bitController, registerArrays);
        ValidateControllers.registerArrayWidthsAreOkayForTransferMicros(machine, registerArrays, this);
    }



    /**
     * returns true if new micros of this class can be created.
     */
    public boolean newModulesAreAllowed()
    {
        return true;
    }

    /**
     * returns the original register associated with the given clone register
     * in one of the arrays.
     * @param cloneRegister the clone of the register to be returned
     * @return the original register associated with the given clone register
     */
    public Register getOriginalOf(Register cloneRegister) {
        checkNotNull(cloneRegister);

        //first find the clone array that contains the clone register
        for (RegisterArray cloneArray : getClones()) {
            Optional<RegisterArray> opt_originalArray = getAssociated(cloneArray);
            if (opt_originalArray.isPresent()) {
                RegisterArray originalArray = opt_originalArray.get();

                for (int i = 0; i < cloneArray.registers().size(); i++) {
                    final Register clone = cloneArray.registers().get(i);
                    if (clone == cloneRegister) {
                        //we found the clone register!!
                        if (originalArray.registers().size() > i) {
                            return originalArray.registers().get(i);
                        } else {
                            return null;  //the clone is part of a new longer array
                        }
                    }
                }
            }

        }
        return null;
    }

    /**
     * returns the clone register associated with the given original register.
     * @param originalRegister the original register to be cloned
     * @return null if there is no such clone register.
     */
    public Register getCloneOf(Register originalRegister)
    {
        //first find the original array that contains the original register
        for (RegisterArray cloneArray : getClones()) {
            Optional<RegisterArray> opt_originalArray = getAssociated(cloneArray);
            if (opt_originalArray.isPresent()) {
                RegisterArray originalArray = opt_originalArray.get();

                for (int i = 0; i < originalArray.registers().size(); i++) {
                    if (originalArray.registers().get(i) == originalRegister) {
                        //we found the original register!!
                        if (cloneArray.registers().size() > i)
                            return cloneArray.registers().get(i);
                        else
                            return null;  //it no longer has a clone
                    }
                }
            }

        }
        return null;
    }

    /**
     * get the ID of the corresponding help page
     * @return the ID of the page
     */
    public String getHelpPageID()
    {
        return "Register Arrays";
    }

    /**
     * updates the table by removing all the items and adding all back.
     * for refreshing the display.
     */
    public void updateTable()
    {
        name.setVisible(false);
        name.setVisible(true);
        double w =  getWidth();
        setPrefWidth(w-1);
        setPrefWidth(w);
    }

}
