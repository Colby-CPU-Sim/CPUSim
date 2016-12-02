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
import cpusim.model.Module;
import cpusim.gui.util.EditingNonNegativeIntCell;
import cpusim.gui.util.EditingStrCell;
import cpusim.gui.util.EditingLongCell;
import cpusim.gui.util.FXMLLoaderFactory;
import cpusim.model.module.Register;
import cpusim.util.Validate;
import cpusim.util.ValidationException;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * The controller for editing the Registers in the EditModules dialog.
 */
public class RegistersTableController
        extends ModuleController implements Initializable {
    @FXML
    TableView<Register> table;
    @FXML
    TableColumn<Register,String> name;
    @FXML TableColumn<Register,Integer> width;
    @FXML TableColumn<Register,Long> initialValue;
    @FXML TableColumn<Register,Boolean> readOnly;

    private ObservableList currentModules;
    private Register prototype;
    private ConditionBitTableController bitController;

    /**
     * Constructor
     * @param mediator holds the machine and information needed
     */
    public RegistersTableController(Mediator mediator){
        super(mediator);
        this.currentModules = machine.getModule("registers");
        this.prototype = new Register("???", 16, 0, false);
        clones = (Module[]) createClones();

        FXMLLoader fxmlLoader = FXMLLoaderFactory.fromRootController(this, "RegistersTable.fxml");

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            // should never happen
            assert false : "Unable to load file: RegistersTable.fxml";
        }

        for (int i = 0; i < clones.length; i++){
            table.getItems().add((Register)clones[i]);
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
        name.prefWidthProperty().bind(table.prefWidthProperty().divide(100/30.0));
        width.prefWidthProperty().bind(table.prefWidthProperty().divide(100/20.0));
        initialValue.prefWidthProperty().bind(table.prefWidthProperty().divide(100/30.0));
        readOnly.prefWidthProperty().bind(table.prefWidthProperty().divide(100/20.0));

        Callback<TableColumn<Register,String>,TableCell<Register,String>> cellStrFactory =
                new Callback<TableColumn<Register, String>, TableCell<Register, String>>() {
                    @Override
                    public TableCell<Register, String> call(
                            TableColumn<Register, String> setStringTableColumn) {
                        return new EditingStrCell<Register>();
                    }
                };
        Callback<TableColumn<Register,Integer>,TableCell<Register,Integer>> cellIntFactory =
                new Callback<TableColumn<Register, Integer>, TableCell<Register, Integer>>() {
                    @Override
                    public TableCell<Register, Integer> call(
                            TableColumn<Register, Integer> setIntegerTableColumn) {
                        return new EditingNonNegativeIntCell<Register>();
                    }
                };
        Callback<TableColumn<Register,Long>,TableCell<Register,Long>> cellLongFactory =
                new Callback<TableColumn<Register, Long>, TableCell<Register, Long>>() {
                    @Override
                    public TableCell<Register, Long> call(
                            TableColumn<Register, Long> setLongTableColumn) {
                        return new EditingLongCell<Register>();
                    }
                };
        Callback<TableColumn<Register,Boolean>,TableCell<Register,Boolean>> cellBooleanFactory =
                new Callback<TableColumn<Register, Boolean>, TableCell<Register, Boolean>>() {
                    @Override
                    public TableCell<Register, Boolean> call(
                            TableColumn<Register, Boolean> registerBooleanTableColumn) {
                        return new CheckBoxTableCell<>();
                    }
                };


        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        width.setCellValueFactory(new PropertyValueFactory<>("width"));
        initialValue.setCellValueFactory(new PropertyValueFactory<>("initialValue"));
        readOnly.setCellValueFactory(new PropertyValueFactory<>("readOnly"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Register, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Register, String> text) {
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

        width.setCellFactory(cellIntFactory);
        width.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Register, Integer>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Register, Integer> text) {
                        ((Register)text.getRowValue()).setWidth(
                                text.getNewValue());
                    }
                }
        );

        initialValue.setCellFactory(cellLongFactory);
        initialValue.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Register, Long>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Register, Long> text) {
                        ((Register)text.getRowValue()).setInitialValue(
                                text.getNewValue());
                    }
                }
        );

        readOnly.setCellFactory(cellBooleanFactory);
        readOnly.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Register, Boolean>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Register, Boolean> text) {
                        ((Register)text.getRowValue()).setReadOnly(
                                text.getNewValue());
                    }
                }
        );


    }

    /**
     * gets the tableview object
     * @return the tableview object
     */
    public TableView getTable() {
        return table;
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
    public Module getPrototype()
    {
        return prototype;
    }

    /**
     * returns the clone register associated with the given original register.
     * @param original the original register to be cloned
     * @return null if there is no such clone register.
     */
    public Register getCloneOf(Register original)
    {
        Set e = assocList.keySet();
        Iterator it = e.iterator();
        while(it.hasNext())
        {
            Register clone = (Register) it.next();
            if (assocList.get(clone) == original)
                return clone;
        }
        return null;
    }

    /**
     * getter for the class object for the controller's objects
     * @return the class object
     */
    public Class getModuleClass()
    {
        return Register.class;
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
        return "Register";
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
     * Set the clones to the new array passed as a parameter.
     * Does not check for validity.
     *
     * @param newClones Object array containing new set of clones
     */
    public void setClones(ObservableList newClones)
    {
        Register[] branches = new Register[newClones.size()];
        for (int i = 0; i < newClones.size(); i++) {
            branches[i] = (Register) newClones.get(i);
        }
        clones = branches;
    }

    /**
     * Check validity of array of Objects' properties.
     */
    public void checkValidity()
    {
        // convert the array to an array of Registers
        Register[] registers = new Register[table.getItems().size()];

        for (int i = 0; i < table.getItems().size(); i++) {
            registers[i] = table.getItems().get(i);
        }

        //build up a HashMap of old registers and new widths
        HashMap<Register,Integer> table = new HashMap<>();
        for (Register register : registers) {
            Register oldRegister = (Register) getCurrentFromClone(register);
            if (oldRegister != null && oldRegister.getWidth() != register.getWidth()) {
                table.put(oldRegister, register.getWidth());
            }
        }

        // check that all names are unique and nonempty
        Validate.widthsAreInBound(registers);
        Validate.initialValuesAreInbound(registers);
        
        //Validate.registersNotReadOnly();

        Validate.registerWidthsAreOkay(bitController,registers);
        Validate.registerWidthsAreOkayForMicros(machine,table);
        Validate.readOnlyRegistersAreImmutable(registers,
                machine.getMicros("transferAtoR"),
                machine.getMicros("transferRtoR"),
                machine.getMicros("setCondBit"));

    }

    /**
     * returns true if new micros of this class can be created.
     */
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
    public void updateTable()
    {
        name.setVisible(false);
        name.setVisible(true);
        double w =  table.getWidth();
        table.setPrefWidth(w-1);
        table.setPrefWidth(w);
    }

}
