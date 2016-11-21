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
import cpusim.gui.util.NamedColumnHandler;
import cpusim.gui.util.EditingNonNegativeIntCell;
import cpusim.gui.util.EditingStrCell;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import cpusim.model.util.Validate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: jyu
 * Date: 6/13/13
 * Time: 11:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConditionBitTableController extends ModuleController<ConditionBit> implements Initializable {

    @FXML @SuppressWarnings("unused")
    private TableColumn<ConditionBit,String> name;
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
        fixClonesToUseCloneRegisters();
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

        final double FACTOR = 100.0/25.0;
        name.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        register.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        bit.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        halt.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));

        Callback<TableColumn<ConditionBit,String>,TableCell<ConditionBit,String>> cellStrFactory =
                setStringTableColumn -> new EditingStrCell<>();
        Callback<TableColumn<ConditionBit,Integer>,TableCell<ConditionBit,Integer>> cellIntFactory =
                setIntegerTableColumn -> new EditingNonNegativeIntCell<>();

        registerList = FXCollections.observableArrayList();
        registerList.addAll(registerController.getItems());
        for (Object r : arrayController.getItems())
            registerList.addAll(((RegisterArray)r).registers());

        Callback<TableColumn<ConditionBit,Register>,TableCell<ConditionBit,Register>> cellRegFactory =
                conditionBitRegisterTableColumn -> new ComboBoxTableCell<>(
                        registerList
                );

        Callback<TableColumn<ConditionBit,Boolean>,TableCell<ConditionBit,Boolean>> cellHaltFactory =
                conditionBitBooleanTableColumn -> new CheckBoxTableCell<>();

        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        register.setCellValueFactory(new PropertyValueFactory<>("register"));
        bit.setCellValueFactory(new PropertyValueFactory<>("bit"));
        halt.setCellValueFactory(new PropertyValueFactory<>("halt"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(new NamedColumnHandler<>(this));

        register.setCellFactory(cellRegFactory);
        register.setOnEditCommit(text -> text.getRowValue().setRegister(text.getNewValue()));

        bit.setCellFactory(cellIntFactory);
        bit.setOnEditCommit(text -> text.getRowValue().setBit(text.getNewValue()));

        halt.setCellFactory(cellHaltFactory);
        halt.setOnEditCommit(text -> text.getRowValue().setHalt(text.getNewValue()));
    }

    /**
     * fixClonesToUseCloneRegisters changes the clones so that
     * they refer to the clones of the original registers instead of
     * the original registers themselves
     */
    private void fixClonesToUseCloneRegisters()
    {
        for (ConditionBit bit: getClones()) {
            Register originalRegister = bit.getRegister();
            Register cloneRegister = registerController.getAssociated(originalRegister)
                    .orElseGet(() -> arrayController.getCloneOf(originalRegister));

            if (cloneRegister == null) {
                throw new IllegalStateException("No clone register of register " + originalRegister +
                        " in ConditionBitController.fixClonesToUseCloneRegisters().");
            }

            bit.setRegister(cloneRegister);
        }
    }

    /**
     * getter for prototype of the right subclass
     * @return the prototype of the subclass
     */
    public ConditionBit getPrototype() {
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
    void updateRegisters(){
        registerList.clear();
        registerList.addAll(registerController.getItems());
        for (Object r : arrayController.getItems())
            registerList.addAll(((RegisterArray)r).registers());
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
     * Check validity of array of Objects' properties.
     */
    public void checkValidity()
    {
        // check that all names are unique and nonempty and that none of the registers
        // are read only
        final List<ConditionBit> items = getItems();
        Validate.bitInBounds(items);
        Validate.someNameIsNone(items);
        Validate.registersNotReadOnly(items);
    }

    /**
     * returns true if new modules of this class can be created.
     */
    public boolean newModulesAreAllowed()
    {
        List<Register> registers = registerController.getItems();
        List<RegisterArray> arrays = arrayController.getItems();
        //return true if there exists at least one register
        return (registers.size() > 0 || arrays.size() > 0);
    }


    /**
     * returns a Vector of the conditionBit clones that use the given Module
     * (either a register or a register in a register array).
     * @param array A {@link RegisterArray} to check
     * @return a vector of the conditionBit clones
     */
    List<ConditionBit> getBitClonesThatUse(RegisterArray array)
    {
        final Set<Register> arrSet = new HashSet<>();
        arrSet.addAll(array.registers());

        return getClones().stream()
                .filter(cl -> arrSet.contains(cl.getRegister()))
                .collect(Collectors.toList());
    }

    public List<ConditionBit> getBitClonesThatUse(Register register) {
        return getClones().stream()
                .filter(cl -> cl.getRegister() == register)
                .collect(Collectors.toList());
    }

    /**
     * returns a list of updated ConditionBits based on the objects
     * in the list.  It replaces the objects in the list with their
     * associated objects, if any, after updating the fields of those
     * old objects.
     * This method overrides the superclass's version of this method in order
     * to fix the registers referenced by each clone to refer to the original
     * register.
     * @param list a list of modules
     * @return a new list of updated ConditionBits
     */
    @Override
    public List<ConditionBit> createNewModulesList(List<? extends ConditionBit> list)
    {
        List<ConditionBit> newBits = new ArrayList<>();
        for (ConditionBit bit : list) {
            Optional<ConditionBit> o_oldBit = getAssociated(bit);
            if (o_oldBit.isPresent()) {
                final ConditionBit oldBit = o_oldBit.get();
                //if the new bit is just an edited clone of an old bit,
                //then just copy the new data to the old bit
                bit.copyTo(oldBit);

                //now fix it to refer to the original register instead of
                //the clone.
                Register oldRegister = registerController.getAssociated(bit.getRegister())
                        .orElseGet(() -> arrayController.getOriginalOf(bit.getRegister()));
                oldBit.setRegister(oldRegister);
                newBits.add(oldBit);
            }
            else { //bit is brand new
                newBits.add(bit);
                //now fix it to refer to the original register instead of
                //the clone.
                Register oldRegister = registerController.getAssociated(bit.getRegister())
                        .orElseGet(() -> arrayController.getOriginalOf(bit.getRegister()));
                bit.setRegister(oldRegister);
            }
        }
        return newBits;
    }

    /**
     * get the ID of the corresponding help page
     * @return the ID of the page
     */
    public String getHelpPageID()
    {
        return "Condition Bits";
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
