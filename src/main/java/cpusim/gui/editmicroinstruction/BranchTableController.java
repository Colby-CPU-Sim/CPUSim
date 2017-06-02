/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Changed the return value of checkValidity from a boolean to void (the functionality
 * enabled by that boolean value is now controlled by throwing ValidationException)
 * 2.) Changed the edit commit method on the name column so that it calls Validate.nameableObjects()
 * which throws a ValidationException in lieu of returning a boolean value
 */
package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.EditingIntCell;
import cpusim.gui.util.EditingStrCell;
import cpusim.gui.util.FXMLLoaderFactory;
import cpusim.model.Microinstruction;
import cpusim.model.microinstruction.Branch;
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
import java.util.ResourceBundle;

/**
 * The controller for editing the Branch command in the EditMicroDialog.
 */
public class BranchTableController
        extends MicroController implements Initializable {
    @FXML
    TableView<Branch> table;
    @FXML
    TableColumn<Branch,String> name;
    @FXML TableColumn<Branch,Integer> amount;

    private ObservableList currentMicros;
    private Branch prototype;

    /**
     * Constructor
     * @param mediator stores the information that will be shown in the tables
     */
    public BranchTableController(Mediator mediator){
        super(mediator);
        this.mediator = mediator;
        this.machine = this.mediator.getMachine();
        this.currentMicros = machine.getMicros("branch");
        this.prototype = new Branch("???", machine, 0, machine.getControlUnit());
        clones = (Microinstruction[]) createClones();

        FXMLLoader fxmlLoader = FXMLLoaderFactory.fromRootController(this, "branchTable.fxml");

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            // should never happen
            assert false : "Unable to load file: branchTable.fxml";
        }

        for (int i = 0; i < clones.length; i++){
            table.getItems().add((Branch)clones[i]);
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
        name.prefWidthProperty().bind(table.prefWidthProperty().divide(100/50.0));
        amount.prefWidthProperty().bind(table.prefWidthProperty().divide(100/50.0));

        Callback<TableColumn<Branch,String>,TableCell<Branch,String>> cellStrFactory =
                new Callback<TableColumn<Branch, String>, TableCell<Branch, String>>() {
                    @Override
                    public TableCell<Branch, String> call(
                            TableColumn<Branch, String> setStringTableColumn) {
                        return new EditingStrCell<Branch>();
                    }
                };
        Callback<TableColumn<Branch,Integer>,TableCell<Branch,Integer>> cellIntFactory =
                new Callback<TableColumn<Branch, Integer>, TableCell<Branch, Integer>>() {
                    @Override
                    public TableCell<Branch, Integer> call(
                            TableColumn<Branch, Integer> setIntegerTableColumn) {
                        return new EditingIntCell<Branch>();
                    }
                };

        name.setCellValueFactory(new PropertyValueFactory<Branch, String>("name"));
        amount.setCellValueFactory(new PropertyValueFactory<Branch, Integer>("amount"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Branch, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Branch, String> text) {
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

        amount.setCellFactory(cellIntFactory);
        amount.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Branch, Integer>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Branch, Integer> text) {
                        ((Branch)text.getRowValue()).setAmount(
                                text.getNewValue());
                    }
                }
        );
    }

    /**
     * getter for prototype of the right subclass
     * @return the prototype of the subclass
     */
    public Microinstruction getPrototype()
    {
        return prototype;
    }

    /**
     * getter for the class object for the controller's objects
     * @return the class object
     */
    public Class getMicroClass()
    {
        return Branch.class;
    }

    /**
     * getter for the current Branch Microinstructions.
     * @return a list of current microinstructions.
     */
    public ObservableList getCurrentMicros()
    {
        return currentMicros;
    }

    /**
     * returns a string about the type of the table.
     * @return a string about the type of the table.
     */
    public String toString()
    {
        return "Branch";
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
     * use clones to replace existing Microinstructions
     * in the machine, and update the machine to delete
     * all references to the deleted Microinstructions.
     */
    public void updateCurrentMicrosFromClones()
    {
        machine.setMicros("branch", createNewMicroList(clones));
    }

    /**
     * Set the clones to the new array passed as a parameter.
     * Does not check for validity.
     *
     * @param newClones Object array containing new set of clones
     */
    public void setClones(ObservableList newClones)
    {
        Branch[] branches = new Branch[newClones.size()];
        for (int i = 0; i < newClones.size(); i++) {
            branches[i] = (Branch) newClones.get(i);
        }
        clones = branches;
    }

    /**
     * Check validity of array of Objects' properties.
     *
     * @param micros an array of Objects to check.
     * @return boolean denoting whether array has objects with
     * valid properties or not
     */
    public void checkValidity(ObservableList micros)
    {    }

    /**
     * returns true if new micros of this class can be created.
     */
    public boolean newMicrosAreAllowed()
    {
        return true;
    }

    /**
     * get the ID of the corresponding help page
     * @return the ID of the page
     */
    public String getHelpPageID()
    {
        return "Branch";
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
