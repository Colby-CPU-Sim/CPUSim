/**
 * Jinghui Yu, Ben Borchard and Michael Goldenberg made the following modifications to
 * this class on 11/11/13:
 *
 * 1.) Added a column readOnly to decide if the value in that register is immutable
 * 2.) Changed initialize method so that it initializes cell factory of readOnly property
 */

package cpusim.gui.editmodules.arrayregisters;

import cpusim.model.Module;
import cpusim.gui.util.EditingNonNegativeIntCell;
import cpusim.gui.util.EditingLongCell;
import cpusim.gui.util.EditingStrCell;
import cpusim.model.module.Register;
import cpusim.util.Validate;
import cpusim.util.ValidationException;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.*;

/**
 * This class is the TableView for one of the register arrays.  One of these
 * TableViews is created for each register array.
 */
public class RegisterArrayTableView extends TableView implements Initializable {
    @FXML
    TableView<Register> table;
    @FXML
    TableColumn<Register,String> name;
    @FXML TableColumn<Register,Long> initialValue;
    @FXML TableColumn<Register,Boolean> readOnly;

    HashMap assocList;  //associates the current modules
    //with the edited clones; key = new clone, value = original
    public Module[] clones;  //the current clones
    Node parentFrame; //for the parent of error messages
    String arrayName;

    private ObservableList currentRegisters;

    /**
     * Constructor
     * @param registers holds the register arrays and information needed
     */
    public RegisterArrayTableView(String arrayName, ObservableList registers){
        this.arrayName = arrayName;
        this.currentRegisters = registers;
        assocList = new HashMap();
        clones = (Module[]) createClones();

        parentFrame = null;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Tables.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            // should never happen
            assert false : "Unable to load file: Tables.fxml";
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
        name.prefWidthProperty().bind(table.prefWidthProperty().divide(100/40.0));
        initialValue.prefWidthProperty().bind(table.prefWidthProperty().divide(100/40.0));
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
                        return new EditingNonNegativeIntCell<>();
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
                        return new CheckBoxTableCell<Register,Boolean>();
                    }
                };

        name.setCellValueFactory(new PropertyValueFactory<Register, String>("name"));
        initialValue.setCellValueFactory(new PropertyValueFactory<Register, Long>("initialValue"));
        readOnly.setCellValueFactory(new PropertyValueFactory<Register, Boolean>("readOnly"));

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
     * gets the name of the register array
     * @return the name of the register array
     */
    public String getArrayName(){
        return arrayName;
    }

    /**
     * gets the tableview object
     * @return the tableview object
     */
    public TableView getTable() {
        return table;
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
        return currentRegisters;
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
     * @param modules an array of Objects to check.
     * @return boolean denoting whether array has objects with
     * valid properties or not
     */
    public void checkValidity(ObservableList modules)
    {
        // convert the array to an array of Registers
        Register[] registers = new Register[modules.size()];
        for (int i = 0; i < modules.size(); i++) {
            registers[i] = (Register) modules.get(i);
        }

        // check that all names are unique and nonempty
        Validate.initialValuesAreInbound(registers);

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

    //========================================
    // public methods

    /**
     * sets the value of parentFrame.
     */
    public void setParentFrame(Node parent)
    {
        parentFrame = parent;
    }

    /**
     * returns an array of clones of the current module.
     *
     * @return an array of clones of the current module.
     */
    public Module[] getClones()
    {
        assert clones != null :
                "clones == null in ModuleFactory.getClones()";
        return clones;
    }

    /**
     * returns the original module (in the machine) whose clone is given.
     * @param clone the clone of the original module
     * @return the original hardware module of the given clone.
     */
    public Module getCurrentFromClone(Module clone)
    {
        return (Module) assocList.get(clone);
    }

    /**
     * creates an array of clones of the current modules,
     * adding the appropriate ChangeListeners to its properties
     *
     * @return the clones of the current modules
     */
    public Object[] createClones()
    {
        ObservableList<? extends Module> currentModules = getCurrentModules();
        Module[] clones = (Module[])
                Array.newInstance(this.getModuleClass(), currentModules.size());
        for (int i = 0; i < currentModules.size(); i++) {
            Module clone = (Module)
                    ((Module) currentModules.get(i)).clone();
            clones[i] = clone;
            assocList.put(clone, currentModules.get(i));
        }
        return clones;
    }

    /**
     * returns a list of updated modules based on the objects
     * in the list.  It replaces the objects in the list with their
     * associated objects, if any, after updating the fields of those old objects.
     * It also sorts the micros by name.
     *
     * @param list a list of modules
     * @return a list of updated modules
     */
    public Vector createNewModulesList(Module[] list)
    {
        Vector newModules = new Vector();
        for (int i = 0; i < list.length; i++) {
            Module module = list[i];
            Module oldModule = (Module) assocList.get(module);
            if (oldModule != null) {
                //if the new incr is just an edited clone of an old module,
                //then just copy the new data to the old module
                module.copyDataTo(oldModule);
                newModules.addElement(oldModule);
            }
        }
        return newModules;
    }

    /**
     * Check validity of array of Objects' names.
     *
     * @param modules an array of Objects to check.
     * @return boolean denoting whether array has objects with
     * valid names or not
     */
    public boolean checkNameValidity(Object[] modules)
    {
        boolean result = true;
        try {
            Validate.allNamesAreUnique(modules);
            for (Object f : modules) {
                Validate.nameIsNonEmpty(((Module)f).getName());
            }
        } catch (ValidationException e) {
            result = false;
        }
        return result;
    }



}
