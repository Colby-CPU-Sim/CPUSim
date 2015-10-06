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
import cpusim.Module;
import cpusim.gui.util.EditingNonNegativeIntCell;
import cpusim.gui.util.EditingStrCell;
import cpusim.module.ConditionBit;
import cpusim.module.Register;
import cpusim.module.RegisterArray;
import cpusim.util.Validate;
import cpusim.util.ValidationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: jyu
 * Date: 6/13/13
 * Time: 11:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConditionBitTableController extends ModuleController implements Initializable {
    @FXML
    TableView<ConditionBit> table;
    @FXML
    TableColumn<ConditionBit,String> name;
    @FXML TableColumn<ConditionBit,Register> register;
    @FXML TableColumn<ConditionBit,Integer> bit;
    @FXML TableColumn<ConditionBit,Boolean> halt;

    private ObservableList currentModules;
    private RegistersTableController registerController;
    private RegisterArrayTableController arrayController;
    ObservableList registerList;

    /**
     * Constructor
     * @param mediator store the machine and other information needed
     * @param registerController the controller holds registers
     * @param arrayController the controller holds register arrays
     */
    public ConditionBitTableController(Mediator mediator,
                                       RegistersTableController registerController,
                                       RegisterArrayTableController arrayController){
        super(mediator);
        this.currentModules = machine.getModule("conditionBits");
        this.registerController = registerController;
        this.arrayController = arrayController;
        clones = (Module[]) createClones();
        fixClonesToUseCloneRegisters();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "conditionBitTable.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            // should never happen
            assert false : "Unable to load file: conditionBitTable.fxml";
        }

        for (int i = 0; i < clones.length; i++){
            table.getItems().add((ConditionBit)clones[i]);
        }

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
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        name.prefWidthProperty().bind(table.prefWidthProperty().divide(100/25.0));
        register.prefWidthProperty().bind(table.prefWidthProperty().divide(100/25.0));
        bit.prefWidthProperty().bind(table.prefWidthProperty().divide(100/25.0));
        halt.prefWidthProperty().bind(table.prefWidthProperty().divide(100/25.0));

        Callback<TableColumn<ConditionBit,String>,TableCell<ConditionBit,String>> cellStrFactory =
                new Callback<TableColumn<ConditionBit, String>, TableCell<ConditionBit, String>>() {
                    @Override
                    public TableCell<ConditionBit, String> call(
                            TableColumn<ConditionBit, String> setStringTableColumn) {
                        return new EditingStrCell<ConditionBit>();
                    }
                };
        Callback<TableColumn<ConditionBit,Integer>,TableCell<ConditionBit,Integer>> cellIntFactory =
                new Callback<TableColumn<ConditionBit, Integer>, TableCell<ConditionBit, Integer>>() {
                    @Override
                    public TableCell<ConditionBit, Integer> call(
                            TableColumn<ConditionBit, Integer> setIntegerTableColumn) {
                        return new EditingNonNegativeIntCell<ConditionBit>();
                    }
                };

        registerList = FXCollections.observableArrayList();
        registerList.addAll(registerController.getItems());
        for (Object r : arrayController.getItems())
            registerList.addAll(((RegisterArray)r).registers());

        Callback<TableColumn<ConditionBit,Register>,TableCell<ConditionBit,Register>> cellRegFactory =
                new Callback<TableColumn<ConditionBit, Register>, TableCell<ConditionBit, Register>>() {
                    @Override
                    public TableCell<ConditionBit, Register> call(
                            TableColumn<ConditionBit, Register> conditionBitRegisterTableColumn) {
                        return new ComboBoxTableCell<ConditionBit,Register>(
                                registerList
                        );
                    }
                };

        Callback<TableColumn<ConditionBit,Boolean>,TableCell<ConditionBit,Boolean>> cellHaltFactory =
                new Callback<TableColumn<ConditionBit, Boolean>, TableCell<ConditionBit, Boolean>>() {
                    @Override
                    public TableCell<ConditionBit, Boolean> call(
                            TableColumn<ConditionBit, Boolean> conditionBitBooleanTableColumn) {
                        return new CheckBoxTableCell<ConditionBit,Boolean>();
                    }
                };

        name.setCellValueFactory(new PropertyValueFactory<ConditionBit, String>("name"));
        register.setCellValueFactory(new PropertyValueFactory<ConditionBit, Register>("register"));
        bit.setCellValueFactory(new PropertyValueFactory<ConditionBit, Integer>("bit"));
        halt.setCellValueFactory(new PropertyValueFactory<ConditionBit, Boolean>("halt"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<ConditionBit, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<ConditionBit, String> text) {
                        String newName = text.getNewValue();
                        String oldName = text.getOldValue();
                        ( text.getRowValue()).setName(newName);
                        try{
                            Validate.namedObjectsAreUniqueAndNonempty(table.getItems().toArray());
                        } catch (ValidationException ex){
                            (text.getRowValue()).setName(oldName);
                            updateTable();
                        }
                    }
                }
        );

        register.setCellFactory(cellRegFactory);
        register.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<ConditionBit, Register>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<ConditionBit, Register> text) {
                        ((ConditionBit) text.getRowValue()).setRegister(
                                text.getNewValue());
                    }
                }
        );

        bit.setCellFactory(cellIntFactory);
        bit.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<ConditionBit, Integer>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<ConditionBit, Integer> text) {
                        ((ConditionBit) text.getRowValue()).setBit(
                                text.getNewValue());
                    }
                }
        );

        halt.setCellFactory(cellHaltFactory);
        halt.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<ConditionBit, Boolean>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<ConditionBit, Boolean> text) {
                        ((ConditionBit) text.getRowValue()).setHalt(
                                text.getNewValue());
                    }
                }
        );
    }

    /**
     * fixClonesToUseCloneRegisters changes the clones so that
     * they refer to the clones of the original registers instead of
     * the original registers themselves
     */
    private void fixClonesToUseCloneRegisters()
    {
        for (int i = 0; i < clones.length; i++) {
            ConditionBit bit = (ConditionBit) clones[i];
            Register originalRegister = bit.getRegister();
            Register cloneRegister =
                    registerController.getCloneOf(originalRegister);
            if (cloneRegister == null)
                cloneRegister = arrayController.getCloneOf(originalRegister);
            assert cloneRegister != null: "No clone register of " +
                    "register " + originalRegister + " in ConditionBitController." +
                    "fixClonesToUseCloneRegisters().";
            bit.setRegister(cloneRegister);
        }
    }

    /**
     * getter for prototype of the right subclass
     * @return the prototype of the subclass
     */
    public Module getPrototype()
    {
        ObservableList<Register> registers = registerController.getTable().getItems();
        ObservableList<RegisterArray> arrays = arrayController.getTable().getItems();
        return new ConditionBit("???", machine,
                (registers.size() > 0 ?
                        registers.get(0) :
                        (arrays.size() > 0 ?
                                arrays.get(0).registers().get(0) :
                                null))
                , 0, false);
    }

    /**
     * getter for the class object for the controller's objects
     * @return the class object
     */
    public Class getModuleClass()
    {
        return ConditionBit.class;
    }

    /**
     * updates the registers shown in the combobox
     */
    public void updateRegisters(){
        registerList.clear();
        registerList.addAll(registerController.getItems());
        for (Object r : arrayController.getItems())
            registerList.addAll(((RegisterArray)r).registers());
    }


    /**
     * getter for the current hardware module
     * @return the current hardware module
     */
    public ObservableList getCurrentModules()
    {
        return currentModules;
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
     * gets properties
     * @return an array of String representations of the
     * various properties of this type of microinstruction
     */
//    public String[] getProperties()
//    {
//        return new String[]{"name", "amount"};
//    }



    /**
     * setClones()
     * Set the clones to the new array passed as a parameter.
     * Does not check for validity.
     *
     * @param newClones Object array containing new set of clones
     */
    public void setClones(ObservableList newClones)
    {
        ConditionBit[] condBits = new ConditionBit[newClones.size()];
        for (int i = 0; i < newClones.size(); i++) {
            condBits[i] = (ConditionBit) newClones.get(i);
        }
        clones = condBits;
    }

    /**
     * Check validity of array of Objects' properties.
     */
    public void checkValidity()
    {
        // convert the array to an array of Transfers
        ConditionBit[] conditionBits = new ConditionBit[table.getItems().size()];

        for (int i = 0; i < table.getItems().size(); i++) {
            conditionBits[i] = (ConditionBit) table.getItems().get(i);
        }

        // check that all names are unique and nonempty and that none of the registers
        // are read only
        Validate.bitInBounds(conditionBits);
        Validate.someNameIsNone(conditionBits);
        Validate.registersNotReadOnly(conditionBits);
    }

    /**
     * returns true if new modules of this class can be created.
     */
    public boolean newModulesAreAllowed()
    {
        ObservableList registers = registerController.getTable().getItems();
        ObservableList arrays = arrayController.getTable().getItems();
        //return true if there exists at least one register
        return (registers.size() > 0 || arrays.size() > 0);
    }


    /**
     * returns a Vector of the conditionBit clones that use the given Module
     * (either a register or a register in a register array).
     * @param theModule an module object
     * @return a vector of the conditionBit clones
     */
    public Vector getBitClonesThatUse(Module theModule)
    {
        Vector result = new Vector();
        if (theModule instanceof Register) {
            for (int i = 0; i < clones.length; i++)
                if (((ConditionBit) clones[i]).getRegister() == theModule)
                    result.addElement(clones[i]);
        }
        else { //theModule is an instance of RegisterArray
            RegisterArray array = (RegisterArray) theModule;
            for (int j = 0; j < array.getLength(); j++)
                for (int k = 0; k < clones.length; k++)
                    if (((ConditionBit) clones[k]).getRegister() ==
                            array.registers().get(j))
                        result.addElement(clones[k]);
        }
        return result;
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
    public Vector createNewModulesList(Module[] list)
    {
        Vector newBits = new Vector();
        for (int i = 0; i < list.length; i++) {
            ConditionBit bit = (ConditionBit) list[i];
            ConditionBit oldBit = (ConditionBit) assocList.get(bit);
            if (oldBit != null) {
                //if the new bit is just an edited clone of an old bit,
                //then just copy the new data to the old bit
                bit.copyDataTo(oldBit);
                //now fix it to refer to the original register instead of
                //the clone.
                Register oldRegister = (Register)
                        registerController.getCurrentFromClone(bit.getRegister());
                if (oldRegister != null)
                    oldBit.setRegister(oldRegister);
                else { //the old register must be part of a register array
                    oldRegister = arrayController.getOriginalOf(bit.getRegister());
                    if (oldRegister != null)
                        oldBit.setRegister(oldRegister);
                }
                newBits.addElement(oldBit);
            }
            else { //bit is brand new
                newBits.addElement(bit);
                //now fix it to refer to the original register instead of
                //the clone.
                Register oldRegister = (Register)
                        registerController.getCurrentFromClone(bit.getRegister());
                if (oldRegister != null)
                    bit.setRegister(oldRegister);
                else { //the old register must be part of a register array
                    oldRegister = arrayController.getOriginalOf(bit.getRegister());
                    if (oldRegister != null)
                        bit.setRegister(oldRegister);
                }
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
        double w =  table.getWidth();
        table.setPrefWidth(w-1);
        table.setPrefWidth(w);
    }

}
