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

import com.google.common.base.Joiner;
import cpusim.Mediator;
import cpusim.gui.util.ControlButtonController;
import cpusim.gui.util.EditingLongCell;
import cpusim.gui.util.EditingNonNegativeIntCell;
import cpusim.model.microinstruction.SetCondBit;
import cpusim.model.microinstruction.TransferAtoR;
import cpusim.model.microinstruction.TransferRtoR;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.Register;
import cpusim.model.util.IdentifiedObject;
import cpusim.model.util.Validate;
import cpusim.util.Dialogs;
import cpusim.util.ValidateControllers;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.*;

/**
 * The controller for editing the Registers in the EditModules dialog.
 */
public class RegistersTableController extends ModuleTableController<Register> {

    static final String FX_ID = "registersTab";

    @FXML @SuppressWarnings("unused")
    private TableColumn<Register, Integer> width;

    @FXML @SuppressWarnings("unused")
    private TableColumn<Register, Long> initialValue;

    @FXML @SuppressWarnings("unused")
    private TableColumn<Register, Boolean> readOnly;

    private ConditionBitTableController bitController;

    /**
     * Constructor
     * @param mediator holds the machine and information needed
     */

    RegistersTableController(Mediator mediator){
        super(mediator, "RegistersTable.fxml", Register.class);
    
        loadFXML();
    }

    @Override
    public void initializeTable() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        name.prefWidthProperty().bind(prefWidthProperty().divide(100/30.0));
        width.prefWidthProperty().bind(prefWidthProperty().divide(100/20.0));
        initialValue.prefWidthProperty().bind(prefWidthProperty().divide(100/30.0));
        readOnly.prefWidthProperty().bind(prefWidthProperty().divide(100/20.0));

        Callback<TableColumn<Register,Integer>,TableCell<Register,Integer>> cellIntFactory =
                setIntegerTableColumn -> new EditingNonNegativeIntCell<>();
        Callback<TableColumn<Register,Long>,TableCell<Register,Long>> cellLongFactory =
                setLongTableColumn -> new EditingLongCell<>();
        Callback<TableColumn<Register,Boolean>,TableCell<Register,Boolean>> cellBooleanFactory =
                registerBooleanTableColumn -> new CheckBoxTableCell<>();


        width.setCellValueFactory(new PropertyValueFactory<>("width"));
        initialValue.setCellValueFactory(new PropertyValueFactory<>("initialValue"));
        readOnly.setCellValueFactory(new PropertyValueFactory<>("readOnly"));

        //Add for Editable Cell of each field, in String or in Integer
        width.setCellFactory(cellIntFactory);
        width.setOnEditCommit(text -> text.getRowValue().setWidth(text.getNewValue()));

        initialValue.setCellFactory(cellLongFactory);
        initialValue.setOnEditCommit(text -> text.getRowValue().setInitialValue(text.getNewValue()));

        readOnly.setCellFactory(cellBooleanFactory);
        readOnly.setOnEditCommit(text -> text.getRowValue().setReadOnly(text.getNewValue()));
    }
    
    @Override
    protected ControlButtonController<Register> createControlButtonController() {
        return new ModuleControlButtonController<Register>(this, false) {
            @Override
            protected boolean checkDelete(final Register toDelete) {
                boolean shouldDelete = super.checkDelete(toDelete);
                if (!shouldDelete) return false; // short circuit
                
                //see if it is a register used for a ConditionBit and,
                //if so, warn the user and return.
                List<ConditionBit> cBitsThatUseIt = bitController.getBitClonesThatUse(toDelete);
                
                if (cBitsThatUseIt.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Register ");
                    sb.append(toDelete.getName());
                    sb.append(" is used by the following condition bits: \n  ");
                    
                    Joiner.on(", ").appendTo(sb, cBitsThatUseIt);
                    sb.append(".\nYou need to delete those condition bits first.");
                    Dialogs.createErrorDialog(getScene().getWindow(),
                            "Deletion Error",
                            sb.toString()).showAndWait();
                    shouldDelete = false;
                }
                
                return shouldDelete;
            }
        };
    }
    
    /**
     * Sets the {@link ConditionBitTableController} stored.
     * @param ctrl Sets the {@link ConditionBitTableController}
     */
    void setConditionBitController(ConditionBitTableController ctrl) {
        bitController = checkNotNull(ctrl);
    }
    
    /**
     * Searches through the current {@link #getItems()} to see if the {@code original} {@link Register} was cloned.
     * @param original The {@link Register} from the underlying {@link cpusim.model.Machine}.
     * @return Non-{@link Optional#empty()} is found.
     */
    Optional<Register> getRegisterClone(Register original) {
        return getItems().stream()
                .filter(r -> r.equals(original))
                .findFirst();
    }

    /**
     * getter for prototype of the right subclass
     * @return the prototype of the subclass
     */
    @Override
    public Register createInstance() {
        return new Register("???", IdentifiedObject.generateRandomID(), machine,16, 0, Register.Access.readWrite());
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

        List<Register> registers = getItems();
        //build up a HashMap of old registers and new widths
        Map<Register, Integer> table = registers.stream()
                .collect(Collectors.toMap(Function.identity(), Register::getWidth));
        
        Validate.registerWidthsAreOkayForMicros(machine, table);
        
        ValidateControllers.registerWidthsAreOkay(bitController,registers);
        Validate.readOnlyRegistersAreImmutable(registers,
                machine.getMicros(TransferAtoR.class),
                machine.getMicros(TransferRtoR.class),
                machine.getMicros(SetCondBit.class));
    }

    /**
     * get the ID of the corresponding help page
     * @return the ID of the page
     */
    public String getHelpPageID()
    {
        return "Registers";
    }

}
