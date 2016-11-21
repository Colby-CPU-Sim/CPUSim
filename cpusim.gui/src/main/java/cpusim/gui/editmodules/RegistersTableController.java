/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Changed the return value of checkValidity from a boolean to void (the functionality
 * enabled by that boolean value is now controlled by throwing ValidationException)
 * 2.) Changed the edit commit method on the name column so that it calls Validate.nameableObjects()
 * which throws a ValidationException in lieu of returning a boolean value
 * 3.) Moved rangesAreInBounds method to the Validate class and changed the return value to void
 * from boolean
 *
 * on 11/11/13:
 *
 * 1.) Added a column readOnly to decide if the value in that register is immutable
 * 2.) Changed initialize method so that it initializes cell factory of readOnly property
 * 3.) Changed checkValidity method so that it calls the Validate.readOnlyRegistersAreImmutable
 * to check if any read-only register is used as the destination register in microinstructions
 * transferAtoR and transferRtoR.
 *
 * on 12/2/2013:
 *
 * 1.) Changed checkValidity method so that it also checks if any read-only register is used as the
 * flag in microinstruction setCondBit.
 */
package cpusim.gui.editmodules;

import cpusim.Mediator;
import cpusim.gui.util.EditingNonNegativeIntCell;
import cpusim.gui.util.EditingStrCell;
import cpusim.gui.util.EditingLongCell;
import cpusim.gui.util.NamedColumnHandler;
import cpusim.model.microinstruction.SetCondBit;
import cpusim.model.microinstruction.TransferAtoR;
import cpusim.model.microinstruction.TransferRtoR;
import cpusim.model.module.Register;
import cpusim.model.util.Validate;
import cpusim.model.util.ValidationException;

import cpusim.util.ValidateControllers;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.net.URL;
import java.util.*;

/**
 * The controller for editing the Registers in the EditModules dialog.
 */
public class RegistersTableController
        extends ModuleController<Register> implements Initializable {

    @FXML @SuppressWarnings("unused")
    private TableColumn<Register,String> name;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Register,Integer> width;

    @FXML @SuppressWarnings("unused")
    private TableColumn<Register,Long> initialValue;

    @FXML @SuppressWarnings("unused")
    private TableColumn<Register,Boolean> readOnly;

    private ConditionBitTableController bitController;

    /**
     * Constructor
     * @param mediator holds the machine and information needed
     */

    RegistersTableController(Mediator mediator){
        super(mediator, "RegistersTable.fxml", Register.class);
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
        name.prefWidthProperty().bind(prefWidthProperty().divide(100/30.0));
        width.prefWidthProperty().bind(prefWidthProperty().divide(100/20.0));
        initialValue.prefWidthProperty().bind(prefWidthProperty().divide(100/30.0));
        readOnly.prefWidthProperty().bind(prefWidthProperty().divide(100/20.0));

        Callback<TableColumn<Register,String>,TableCell<Register,String>> cellStrFactory =
                setStringTableColumn -> new EditingStrCell<>();
        Callback<TableColumn<Register,Integer>,TableCell<Register,Integer>> cellIntFactory =
                setIntegerTableColumn -> new EditingNonNegativeIntCell<>();
        Callback<TableColumn<Register,Long>,TableCell<Register,Long>> cellLongFactory =
                setLongTableColumn -> new EditingLongCell<>();
        Callback<TableColumn<Register,Boolean>,TableCell<Register,Boolean>> cellBooleanFactory =
                registerBooleanTableColumn -> new CheckBoxTableCell<>();


        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        width.setCellValueFactory(new PropertyValueFactory<>("width"));
        initialValue.setCellValueFactory(new PropertyValueFactory<>("initialValue"));
        readOnly.setCellValueFactory(new PropertyValueFactory<>("readOnly"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(new NamedColumnHandler<>(this));

        width.setCellFactory(cellIntFactory);
        width.setOnEditCommit(text -> text.getRowValue().setWidth(text.getNewValue()));

        initialValue.setCellFactory(cellLongFactory);
        initialValue.setOnEditCommit(text -> text.getRowValue().setInitialValue(text.getNewValue()));

        readOnly.setCellFactory(cellBooleanFactory);
        readOnly.setOnEditCommit(text -> text.getRowValue().setReadOnly(text.getNewValue()));
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
    public Register getPrototype() {
        return new Register("???", 16, 0, false);
    }

    /**
     * returns a string of the types of the controller
     * @return a string of the types of the controller
     */
    @Override
    public String toString()
    {
        return "Register";
    }

    /**
     * Check validity of array of Objects' properties.
     */
    @Override
    public void checkValidity()
    {
        // convert the array to an array of Registers

        List<Register> registers = getCurrentModules();
        //build up a HashMap of old registers and new widths
        Map<Register, Integer> table = new HashMap<>();
        for (Register register : registers) {
            getAssociated(register).ifPresent(oldRegister -> {
                if (oldRegister.getWidth() != register.getWidth()) {
                    table.put(oldRegister, register.getWidth());
                }
            });
        }

        // check that all names are unique and nonempty
        Validate.widthsAreInBound(registers);
        Validate.initialValuesAreInbound(registers);
        
        //Validate.registersNotReadOnly();

        ValidateControllers.registerWidthsAreOkay(bitController,registers);
        Validate.registerWidthsAreOkayForMicros(machine,table);
        Validate.readOnlyRegistersAreImmutable(registers,
                machine.getMicros(TransferAtoR.class),
                machine.getMicros(TransferRtoR.class),
                machine.getMicros(SetCondBit.class));

    }

    /**
     * returns true if new micros of this class can be created.
     */
    @Override
    public boolean newModulesAreAllowed()
    {
        return true;
    }

    /**
     * get the ID of the corresponding help page
     * @return the ID of the page
     */
    public String getHelpPageID()
    {
        return "Registers";
    }

    /**
     * updates the table by removing all the items and adding all back.
     * for refreshing the display.
     */
    @Override
    public void updateTable() {
        name.setVisible(false);
        name.setVisible(true);
        double w =  getWidth();
        setPrefWidth(w-1);
        setPrefWidth(w);
    }

}
