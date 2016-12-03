/**
 * Jinghui Yu, Ben Borchard and Michael Goldenberg made the following modifications to
 * this class on 11/11/13:
 *
 * 1.) Added a column readOnly to decide if the value in that register is immutable
 * 2.) Changed initialize method so that it initializes cell factory of readOnly property
 */

package cpusim.gui.editmodules.arrayregisters;

import cpusim.gui.util.table.EditingLongCell;
import cpusim.gui.util.table.EditingStrCell;
import cpusim.model.module.Modules;
import cpusim.model.module.Register;
import cpusim.model.util.Validate;
import cpusim.model.util.ValidationException;
import javafx.collections.ObservableList;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * This class is the TableView for one of the register arrays.  One of these
 * TableViews is created for each register array.
 */
public class RegisterArrayTableView extends TableView<Register> implements Initializable {
    
    @FXML
    private TableView<Register> table;
    
    @FXML
    private TableColumn<Register,String> name;
    
    @FXML
    private TableColumn<Register,Long> initialValue;
    
    @FXML
    private TableColumn<Register,Boolean> readOnly;

    private Map<Register, Register> assocList;  //associates the current modules
    //with the edited clones; key = new clone, value = original
    private List<Register> clones;  //the current clones
    private Node parentFrame; //for the parent of error messages
    private String arrayName;

    private ObservableList<Register> currentRegisters;

    /**
     * Constructor
     * @param registers holds the register arrays and information needed
     */
    public RegisterArrayTableView(String arrayName, ObservableList<Register> registers){
        this.arrayName = arrayName;
        this.currentRegisters = registers;
        assocList = new HashMap<>();
        clones = createClones();

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

        final List<Register> items = table.getItems();
        clones.stream().forEach(items::add);
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
                setStringTableColumn -> new EditingStrCell<>();
        Callback<TableColumn<Register,Long>,TableCell<Register,Long>> cellLongFactory =
                setLongTableColumn -> new EditingLongCell<>();
        Callback<TableColumn<Register,Boolean>,TableCell<Register,Boolean>> cellBooleanFactory =
                registerBooleanTableColumn -> new CheckBoxTableCell<>();

        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        initialValue.setCellValueFactory(new PropertyValueFactory<>("initialValue"));
        readOnly.setCellValueFactory(new PropertyValueFactory<>("readOnly"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(
                text -> {
                    String newName = text.getNewValue();
                    String oldName = text.getOldValue();
                    ( text.getRowValue()).setName(newName);
                    try{
                        Validate.namedObjectsAreUniqueAndNonempty(table.getItems());
                    } catch (ValidationException ex){
                        (text.getRowValue()).setName(oldName);
                        updateTable();
                    }
                }
        );

        initialValue.setCellFactory(cellLongFactory);
        initialValue.setOnEditCommit(
                text -> text.getRowValue().setInitialValue(text.getNewValue())
        );
        readOnly.setCellFactory(cellBooleanFactory);
        readOnly.setOnEditCommit(
                text -> text.getRowValue().setReadOnly(
                        text.getNewValue())
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
    public Class<?> getModuleClass()
    {
        return Register.class;
    }

    /**
     * getter for the current hardware module
     * @return the current hardware module
     */
    public ObservableList<Register> getCurrentModules()
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
     * Set the clones to the new array passed as a parameter.
     * Does not check for validity.
     *
     * @param newClones Object array containing new set of clones
     */
    public void setClones(ObservableList<Register> newClones)
    {
        clones = new ArrayList<>(newClones);
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
    public List<Register> getClones()
    {
        if (clones == null) {
            throw new IllegalStateException("clones == null in ModuleFactory.getClones()");
        }
        
        return clones;
    }
    
    /**
     * Creates a clone of the current list of modules.
     * @return
     */
    public List<Register> createNewModulesList() {
       return Modules.createNewModulesListWithAssociation(getClones(), assocList);
    }
    
    /**
     * creates an array of clones of the current modules,
     * adding the appropriate ChangeListeners to its properties
     *
     * @return the clones of the current modules
     */
    public List<Register> createClones()
    {
        ObservableList<Register> currentModules = getCurrentModules();
        List<Register> clones = new ArrayList<>(currentModules.size());
        for (Register r: currentModules) {
            final Register clone = r.cloneOf();
            
            clones.add(clone);
            assocList.put(clone, r);
        }
        
        return clones;
    }



}
