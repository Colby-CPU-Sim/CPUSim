/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Changed the return value of checkValidity from a boolean to void (the functionality
 * enabled by that boolean value is now controlled by throwing ValidationException)
 * 2.) Changed the edit commit method on the name column so that it calls Validate.nameableObjects()
 * which throws a ValidationException in lieu of returning a boolean value
 * 3.) Moved sameNameIsDone and bitInBounds methods to the Validate class and changed the return value to void
 * from boolean
 */
package cpusim.gui.editmodules;

import cpusim.gui.util.table.EditingNonNegativeIntCell;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import cpusim.model.util.MoreBindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.*;

/**
 * Controller for the {@link ConditionBit} table
 */
public class ConditionBitTableController extends ModuleTableController<ConditionBit> {

    static final String FX_ID = "conditionBitsTab";

    @FXML @SuppressWarnings("unused")
    private TableColumn<ConditionBit, Register> register;

    @FXML @SuppressWarnings("unused")
    private TableColumn<ConditionBit, Integer> bit;

    @FXML @SuppressWarnings("unused")
    private TableColumn<ConditionBit, Boolean> halt;
    
    private final ReadOnlyListProperty<Register> registerList;

    /**
     * Constructor
     * @param registerTableController Controller for {@link Register} table.
     * @param registerArrayTableController Controller for {@link RegisterArray} table.
     */
    ConditionBitTableController(RegistersTableController registerTableController,
                                RegisterArrayTableController registerArrayTableController){
        super("ConditionBitTable.fxml", ConditionBit.class);
        
        // Add some listeners so when they update, it will update the registers that condition bits can use

        ObservableList<Register> registers = checkNotNull(registerTableController).getItems();
        ObservableList<? extends Register> fromArrays =
                MoreBindings.flatMapValue(checkNotNull(registerArrayTableController).getItems(), RegisterArray::registersProperty);

        ObservableList<Register> concatRegisters = MoreBindings.concat(FXCollections.observableArrayList(registers, fromArrays));

        registerList = new ReadOnlyListWrapper<>(this, "allRegisters", concatRegisters);

        fixItemsToUseClones(registerTableController, registerArrayTableController);
        
        loadFXML();
    }


    @Override
    public void initialize() {
        super.initialize();

        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        name.prefWidthProperty().bind(prefWidthProperty().multiply(3.0/8.0));
        register.prefWidthProperty().bind(prefWidthProperty().multiply(3.0/8.0));
        bit.prefWidthProperty().bind(prefWidthProperty().multiply(2.0/8.0));
        halt.prefWidthProperty().bind(prefWidthProperty().multiply(2.0/8.0));

        Callback<TableColumn<ConditionBit, Integer>,TableCell<ConditionBit, Integer>> cellIntFactory =
                setIntegerTableColumn -> new EditingNonNegativeIntCell<>();
        
        Callback<TableColumn<ConditionBit, Register>,TableCell<ConditionBit, Register>> cellRegFactory =
                conditionBitRegisterTableColumn -> new ComboBoxTableCell<>(registerList);

        Callback<TableColumn<ConditionBit, Boolean>,TableCell<ConditionBit, Boolean>> cellHaltFactory =
                conditionBitBooleanTableColumn -> new CheckBoxTableCell<>();

        //Add for Editable Cell of each field, in String or in Integer
        register.setCellValueFactory(new PropertyValueFactory<>("register"));
        register.setCellFactory(cellRegFactory);
        register.setOnEditCommit(text -> text.getRowValue().setRegister(text.getNewValue()));

        bit.setCellValueFactory(new PropertyValueFactory<>("bit"));
        bit.setCellFactory(cellIntFactory);
        bit.setOnEditCommit(text -> text.getRowValue().setBit(text.getNewValue()));

        halt.setCellValueFactory(new PropertyValueFactory<>("halt"));
        halt.setCellFactory(cellHaltFactory);
        halt.setOnEditCommit(text -> text.getRowValue().setHalt(text.getNewValue()));
    }

    @Override
    public BooleanBinding newButtonEnabledBinding() {
        return super.newButtonEnabledBinding().and(registerList.emptyProperty().not());
    }

    /**
     * fixClonesToUseCloneRegisters changes the clones so that
     * they refer to the clones of the original registers instead of
     * the original registers themselves
     */
    private void fixItemsToUseClones(RegistersTableController registersTableController,
                                     RegisterArrayTableController registerArrayTableController)
    {
        getItems().forEach(bit -> {
            Register originalRegister = bit.getRegister();
            
            Optional<Register> fromRegisterCtrl = registersTableController.getRegisterClone(originalRegister);
            Optional<Register> fromArrayCtrl = registerArrayTableController.getRegisterClone(originalRegister);

            if (fromRegisterCtrl.isPresent()) {
                bit.setRegister(fromRegisterCtrl.get());
            } else if (fromArrayCtrl.isPresent()) {
                bit.setRegister(fromArrayCtrl.get());
            } else {
                throw new IllegalStateException("No clone register of register " + originalRegister +
                        " in ConditionBitController.fixClonesToUseCloneRegisters().");
            }
        });
    }

    /**
     * getter for prototype of the right subclass
     * @return the prototype of the subclass
     */
    public Supplier<ConditionBit> supplierBinding() {
        return () -> {
            Register defaultRegister = null;
            if (!registerList.isEmpty()) {
                defaultRegister = registerList.get(0);
            }

            return new ConditionBit("???", UUID.randomUUID(), getMachine(), defaultRegister, 0, false);
        };
    }

    /**
     * returns a string of the types of the controller
     * @return a string of the types of the controller
     */
    public String toString()
    {
        return "ConditionBit";
    }


    /**
     * returns a Vector of the conditionBit clones that use the given Module
     * (either a register or a register in a register array).
     * @param array A {@link RegisterArray} to check
     * @return a vector of the conditionBit clones
     */
    List<ConditionBit> getBitClonesThatUse(RegisterArray array) {
        final Set<Register> arrSet = new HashSet<>();
        arrSet.addAll(array.getRegisters());

        return getItems().stream()
                .filter(cl -> arrSet.contains(cl.getRegister()))
                .collect(Collectors.toList());
    }

    public List<ConditionBit> getBitClonesThatUse(Register register) {
        return getItems().stream()
                .filter(cl -> cl.getRegister().equals(register))
                .collect(Collectors.toList());
    }
    

    /**
     * get the ID of the corresponding help page
     * @return the ID of the page
     */
    public String getHelpPageID()
    {
        return "Condition Bits";
    }

}
