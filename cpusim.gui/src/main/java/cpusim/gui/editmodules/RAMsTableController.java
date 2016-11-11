/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Changed the return value of checkValidity from a boolean to void (the functionality
 * enabled by that boolean value is now controlled by throwing ValidationException)
 * 2.) Changed the edit commit method on the name column so that it calls Validate.nameableObjects()
 * which throws a ValidationException in lieu of returning a boolean value
 * 3.) Moved cellSizesAreValid and lengthsArePositive method to the Validate class and changed the return value to void
 * from boolean
 */
package cpusim.gui.editmodules;

import cpusim.Mediator;
import cpusim.model.Module;
import cpusim.gui.util.EditingNonNegativeIntCell;
import cpusim.gui.util.EditingStrCell;
import cpusim.gui.util.FXMLLoaderFactory;
import cpusim.model.module.RAM;
import cpusim.model.util.Validate;
import cpusim.model.util.ValidationException;

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
public class RAMsTableController
        extends ModuleController<RAM> implements Initializable {
    @FXML TableView<RAM> table;
    @FXML TableColumn<RAM,String> name;
    @FXML TableColumn<RAM,Integer> length;
    @FXML TableColumn<RAM,Integer> cellSize;

    private ObservableList<RAM> currentRAMs;
    private RAM prototype;

    /**
     * Constructor
     * @param mediator holds the machine and information needed
     */
    public RAMsTableController(Mediator mediator){
        super(mediator, RAM.class);
        this.currentRAMs = machine.getAllRAMs();
        this.prototype = new RAM("???",128, 8);

        FXMLLoader fxmlLoader = FXMLLoaderFactory.fromRootController(this, "RamTable.fxml");

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            // should never happen
            assert false : "Unable to load file: RamTable.fxml";
        }

        loadClonesIntoTableView(table);
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
        length.prefWidthProperty().bind(table.prefWidthProperty().divide(100/35.0));
        cellSize.prefWidthProperty().bind(table.prefWidthProperty().divide(100/35.0));

        Callback<TableColumn<RAM,String>,TableCell<RAM,String>> cellStrFactory =
                setStringTableColumn -> new EditingStrCell<>();
        Callback<TableColumn<RAM,Integer>,TableCell<RAM,Integer>> cellIntFactory =
                setIntegerTableColumn -> new EditingNonNegativeIntCell<>();

        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        length.setCellValueFactory(new PropertyValueFactory<>("length"));
        cellSize.setCellValueFactory(new PropertyValueFactory<>("cellSize"));

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

        length.setCellFactory(cellIntFactory);
        length.setOnEditCommit(text -> text.getRowValue().setLength(text.getNewValue()));

        cellSize.setCellFactory(cellIntFactory);
        cellSize.setOnEditCommit(text -> text.getRowValue().setCellSize(text.getNewValue()));
    }

    /**
     * getter for prototype of the right subclass
     * @return the prototype of the subclass
     */
    public RAM getPrototype()
    {
        return prototype;
    }

    /**
     * getter for the current hardware module
     * @return the current hardware module
     */
    public ObservableList<RAM> getCurrentModules() {
        return currentRAMs;
    }

    /**
     * returns a string of the types of the controller
     * @return a string of the types of the controller
     */
    public String toString()
    {
        return "RAM";
    }

    /**
     * Check validity of array of Objects' properties.
     */
    public void checkValidity()
    {
        // check that all names are unique and nonempty
        Validate.lengthsArePositive(table.getItems());
        Validate.cellSizesAreValid(table.getItems());
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
        return "RAMs";
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
