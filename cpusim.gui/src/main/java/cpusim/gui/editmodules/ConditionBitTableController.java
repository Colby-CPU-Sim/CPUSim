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

import cpusim.Mediator;
import cpusim.gui.util.EditingNonNegativeIntCell;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for the {@link ConditionBit} table
 */
public class ConditionBitTableController extends ModuleTableController<ConditionBit> {

    static final String FX_ID = "conditionBitsTab";

    @FXML @SuppressWarnings("unused")
    private TableColumn<ConditionBit,Register> register;

    @FXML @SuppressWarnings("unused")
    private TableColumn<ConditionBit,Integer> bit;

    @FXML @SuppressWarnings("unused")
    private TableColumn<ConditionBit,Boolean> halt;

    private RegistersTableController registerController;
    private RegisterArrayTableController arrayController;

    private ObservableList<Register> registerList;

    /**
     * Constructor
     * @param mediator store the machine and other information needed
     * @param registerController the controller holds registers
     * @param arrayController the controller holds register arrays
     */
    ConditionBitTableController(Mediator mediator,
                                RegistersTableController registerController,
                                RegisterArrayTableController arrayController){
        super(mediator, "ConditionBitTable.fxml", ConditionBit.class);
        this.registerController = registerController;
        this.arrayController = arrayController;
    }


    @Override
    public void initializeTable() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        name.prefWidthProperty().bind(prefWidthProperty().divide(100.0/30.0));
        register.prefWidthProperty().bind(prefWidthProperty().divide(100.0/30.0));
        bit.prefWidthProperty().bind(prefWidthProperty().divide(100.0/20.0));
        halt.prefWidthProperty().bind(prefWidthProperty().divide(100.0/20.0));

        Callback<TableColumn<ConditionBit,Integer>,TableCell<ConditionBit,Integer>> cellIntFactory =
                setIntegerTableColumn -> new EditingNonNegativeIntCell<>();

        registerList = FXCollections.observableArrayList();
        registerList.addAll(registerController.getItems());
        for (RegisterArray r : arrayController.getItems())
            registerList.addAll(r.registers());

        Callback<TableColumn<ConditionBit,Register>,TableCell<ConditionBit,Register>> cellRegFactory =
                conditionBitRegisterTableColumn -> new ComboBoxTableCell<>(registerList);

        Callback<TableColumn<ConditionBit,Boolean>,TableCell<ConditionBit,Boolean>> cellHaltFactory =
                conditionBitBooleanTableColumn -> new CheckBoxTableCell<>();

        register.setCellValueFactory(new PropertyValueFactory<>("register"));
        bit.setCellValueFactory(new PropertyValueFactory<>("bit"));
        halt.setCellValueFactory(new PropertyValueFactory<>("halt"));

        //Add for Editable Cell of each field, in String or in Integer
        register.setCellFactory(cellRegFactory);
        register.setOnEditCommit(text -> text.getRowValue().setRegister(text.getNewValue()));

        bit.setCellFactory(cellIntFactory);
        bit.setOnEditCommit(text -> text.getRowValue().setBit(text.getNewValue()));

        halt.setCellFactory(cellHaltFactory);
        halt.setOnEditCommit(text -> text.getRowValue().setHalt(text.getNewValue()));
        
        this.fixItemsToUseClones();
    }

    @Override
    public boolean isNewButtonEnabled() {
        return super.isNewButtonEnabled() && !registerList.isEmpty();
    }

    @Override
    void onTabSelected() {
        super.onTabSelected();

        updateRegisters();
    }

    /**
     * fixClonesToUseCloneRegisters changes the clones so that
     * they refer to the clones of the original registers instead of
     * the original registers themselves
     */
    private void fixItemsToUseClones()
    {
        getItems().forEach(bit -> {
            Register originalRegister = bit.getRegister();
            
            Optional<Register> fromRegisterCtrl = registerController.getRegisterClone(originalRegister);
            Optional<Register> fromArrayCtrl = arrayController.getRegisterClone(originalRegister);

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
    public ConditionBit createInstance() {
        ObservableList<Register> registers = registerController.getItems();
        ObservableList<RegisterArray> arrays = arrayController.getItems();
        return new ConditionBit("???", machine,
                (registers.size() > 0 ?
                        registers.get(0) :
                        (arrays.size() > 0 ?
                                arrays.get(0).registers().get(0) :
                                null))
                , 0, false);
    }

    /**
     * updates the registers shown in the combobox
     */
    private void updateRegisters(){
        registerList.clear();
        registerList.addAll(registerController.getItems());
        for (RegisterArray r : arrayController.getItems()) {
            registerList.addAll(r.registers());
        }
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
        arrSet.addAll(array.registers());

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
