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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import cpusim.Mediator;
import cpusim.gui.util.EditingNonNegativeIntCell;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import cpusim.model.util.Validate;
import cpusim.util.Dialogs;
import cpusim.util.ValidateControllers;
import javafx.event.ActionEvent;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.*;

/**
 * The controller for editing the Register arrays in the EditModules dialog.
 */
public class RegisterArrayTableController extends ModuleTableController<RegisterArray> {

    static final String FX_ID = "registerArraysTab";

    private final TableColumn<RegisterArray,Integer> length;

    private final TableColumn<RegisterArray,Integer> width;

    private ConditionBitTableController bitController;

    /**
     * Constructor
     * @param mediator holds the machine and information needed
     */
    RegisterArrayTableController(Mediator mediator){
        super(mediator,"RegisterArrayTable.fxml", RegisterArray.class);

        length = new TableColumn<>("Length");
        width = new TableColumn<>("Width");

        super.loadFXML();
    }

    @Override
    public void initializeTable(TableView<RegisterArray> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        name.prefWidthProperty().bind(table.prefWidthProperty().divide(100/40.0));
        length.prefWidthProperty().bind(table.prefWidthProperty().divide(100/30.0));
        width.prefWidthProperty().bind(table.prefWidthProperty().divide(100/30.0));

        Callback<TableColumn<RegisterArray,Integer>,TableCell<RegisterArray,Integer>> cellIntFactory =
                setIntegerTableColumn -> new EditingNonNegativeIntCell<>();

        length.setCellValueFactory(new PropertyValueFactory<>("length"));
        width.setCellValueFactory(new PropertyValueFactory<>("width"));

        //Add for Editable Cell of each field, in String or in Integer
        length.setCellFactory(cellIntFactory);
        length.setOnEditCommit(text -> text.getRowValue().setLength(text.getNewValue()));

        width.setCellFactory(cellIntFactory);
        width.setOnEditCommit(text -> text.getRowValue().setWidth(text.getNewValue()));
    }
    
    /**
     * Sets the {@link ConditionBitTableController} stored.
     * @param ctrl Sets the {@link ConditionBitTableController}
     */
    void setConditionBitController(ConditionBitTableController ctrl) {
        bitController = checkNotNull(ctrl);
    }

    @Override
    protected ImmutableList<TableColumn<RegisterArray, ?>> getSubTableColumns() {
        return ImmutableList.of(name, length, width);
    }
    
    @Override
    protected ControlButtonController<RegisterArray> createControlButtonController() {
        return new ControlButtonController<RegisterArray>(this, this, true) {
            @Override
            void onPropertiesButtonClick(final ActionEvent e) {
                super.onPropertiesButtonClick(e);
                
                // FIXME
            }
    
            @Override
            protected boolean checkDelete(final RegisterArray toDelete) {
                boolean shouldDelete = super.checkDelete(toDelete);
                if (!shouldDelete) return false; // short circuit
    
                //see if a RegisterArray is used for a ConditionBit and,
                //if so, warn the user and return false
                List<ConditionBit> cBitsThatUseIt = bitController.getBitClonesThatUse(toDelete);
    
                if (cBitsThatUseIt.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Register Array ");
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
    
    @Override
    public boolean isPropButtonEnabled() {
        return !getSelectionModel().isEmpty();
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
        List<RegisterArray> registerArrays = getItems();
        //build up a HashMap of old registers and new widths
        final Map<Register, Integer> regWidths = registerArrays.stream()
                .flatMap(r -> r.registers().stream()) // get all of the registers in the arrays
                .distinct() // remove any duplicates (possibility)
                .collect(Collectors.toMap(Function.identity(), Register::getWidth)); // map them to their widths
        
        //now do all the tests
        Validate.registerWidthsAreOkayForMicros(machine, regWidths);
        
        ValidateControllers.registerArrayWidthsAreOkay(bitController, registerArrays);
        ValidateControllers.registerArrayWidthsAreOkayForTransferMicros(machine, registerArrays, this);
    }
    
    /**
     * Runs through all of the known {@link RegisterArray} components and searches for a copy of the original
     * {@link Register} passed. If the value is found, it returns {@link Optional#of(Object)}.
     *
     * @param original The original {@link Register} from the {@link cpusim.model.Machine}.
     * @return {@link Optional#of(Object)} or {@link Optional#empty()} if not found.
     */
    Optional<Register> getRegisterClone(Register original) {
        return getItems().stream()
                .flatMap(arr -> arr.registers().stream())
                .filter(r -> r.equals(original))
                .findFirst();
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
    public void updateTable() {
//        name.setVisible(false);
//        name.setVisible(true);
//        double w =  getWidth();
//        setPrefWidth(w-1);
//        setPrefWidth(w);
    }

}
