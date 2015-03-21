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
import cpusim.Module;
import cpusim.gui.util.EditingIntCell;
import cpusim.gui.util.EditingStrCell;
import cpusim.module.Register;
import cpusim.module.RegisterArray;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * The controller for editing the Register arrays in the EditModules dialog.
 */
public class RegisterArrayTableController
        extends ModuleController implements Initializable {
    @FXML
    TableView<RegisterArray> table;
    @FXML
    TableColumn<RegisterArray,String> name;
    @FXML TableColumn<RegisterArray,Integer> length;
    @FXML TableColumn<RegisterArray,Integer> width;

    private ObservableList currentModules;;
    private RegisterArray prototype;
    private ConditionBitTableController bitController;

    /**
     * Constructor
     * @param mediator holds the machine and information needed
     */
    public RegisterArrayTableController(Mediator mediator){
        super(mediator);
        this.currentModules = machine.getModule("registerArrays");
        this.prototype = new RegisterArray("???",4, 32);
        clones = (Module[]) createClones();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "registerArrayTable.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        for (int i = 0; i < clones.length; i++){
            table.getItems().add((RegisterArray)clones[i]);
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
        name.prefWidthProperty().bind(table.prefWidthProperty().divide(100/34.0));
        length.prefWidthProperty().bind(table.prefWidthProperty().divide(100/33.0));
        width.prefWidthProperty().bind(table.prefWidthProperty().divide(100/33.0));

        Callback<TableColumn<RegisterArray,String>,TableCell<RegisterArray,String>> cellStrFactory =
                new Callback<TableColumn<RegisterArray, String>, TableCell<RegisterArray, String>>() {
                    @Override
                    public TableCell<RegisterArray, String> call(
                            TableColumn<RegisterArray, String> setStringTableColumn) {
                        return new EditingStrCell<RegisterArray>();
                    }
                };
        Callback<TableColumn<RegisterArray,Integer>,TableCell<RegisterArray,Integer>> cellIntFactory =
                new Callback<TableColumn<RegisterArray, Integer>, TableCell<RegisterArray, Integer>>() {
                    @Override
                    public TableCell<RegisterArray, Integer> call(
                            TableColumn<RegisterArray, Integer> setIntegerTableColumn) {
                        return new EditingIntCell<RegisterArray>();
                    }
                };

        name.setCellValueFactory(new PropertyValueFactory<RegisterArray, String>("name"));
        length.setCellValueFactory(new PropertyValueFactory<RegisterArray, Integer>("length"));
        width.setCellValueFactory(new PropertyValueFactory<RegisterArray, Integer>("width"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<RegisterArray, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<RegisterArray, String> text) {
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

        length.setCellFactory(cellIntFactory);
        length.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<RegisterArray, Integer>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<RegisterArray, Integer> text) {
                        ((RegisterArray)text.getRowValue()).setLength(
                                text.getNewValue());
                    }
                }
        );

        width.setCellFactory(cellIntFactory);
        width.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<RegisterArray, Integer>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<RegisterArray, Integer> text) {
                        ((RegisterArray)text.getRowValue()).setWidth(
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
     * getter for the class object for the controller's objects
     * @return the class object
     */
    public Class getModuleClass()
    {
        return RegisterArray.class;
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
        return "RegisterArray";
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
        RegisterArray[] registerArrays = new RegisterArray[newClones.size()];
        for (int i = 0; i < newClones.size(); i++) {
            registerArrays[i] = (RegisterArray) newClones.get(i);
        }
        clones = registerArrays;
    }

    /**
     * Check validity of array of Objects' properties.
     */
    public void checkValidity()
    {
        // convert the array to an array of RegisterArrays
        RegisterArray[] registerArrays = new RegisterArray[table.getItems().size()];

        for (int i = 0; i < table.getItems().size(); i++) {
            registerArrays[i] = (RegisterArray) table.getItems().get(i);
        }

        //build up a HashMap of old registers and new widths
        HashMap h = new HashMap();
        for (int i = 0; i < registerArrays.length; i++) {
            RegisterArray oldArray =
                    (RegisterArray) getCurrentFromClone(registerArrays[i]);
            if (oldArray != null &&
                    registerArrays[i].getWidth() != oldArray.getWidth()) {
                for (int j = 0; j < Math.min(registerArrays[i].getLength(),
                        oldArray.getLength()); j++) {
                    h.put(oldArray.registers().get(j),
                            new Integer(registerArrays[i].getWidth()));
                }
            }
        }

        //now do all the tests
        Validate.rangesAreInBound(registerArrays);
        Validate.registerArrayWidthsAreOkay(bitController,registerArrays);
        Validate.registerWidthsAreOkayForMicros(machine,h);
        Validate.registerArrayWidthsAreOkayForTransferMicros(machine,registerArrays,this);
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
    public Register getOriginalOf(Register cloneRegister)
    {
        //first find the clone array that contains the clone register
        for (int j = 0; j < clones.length; j++) {
            RegisterArray cloneArray = (RegisterArray) clones[j];
            RegisterArray originalArray = (RegisterArray) assocList.get(cloneArray);
            for (int i = 0; i < cloneArray.registers().size(); i++)
                if (cloneArray.registers().get(i) == cloneRegister)
                    //we found the clone register!!
                    if (originalArray.registers().size() > i)
                        return originalArray.registers().get(i);
                    else
                        return null;  //the clone is part of a new longer array
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
        for (int j = 0; j < clones.length; j++) {
            RegisterArray cloneArray = (RegisterArray) clones[j];
            RegisterArray originalArray = (RegisterArray) assocList.get(cloneArray);
            for (int i = 0; i < originalArray.registers().size(); i++)
                if (originalArray.registers().get(i) == originalRegister)
                    //we found the original register!!
                    if (cloneArray.registers().size() > i)
                        return cloneArray.registers().get(i);
                    else
                        return null;  //it no longer has a clone
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
        double w =  table.getWidth();
        table.setPrefWidth(w-1);
        table.setPrefWidth(w);
    }

}
