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
import cpusim.gui.util.table.EditingNonNegativeIntCell;
import cpusim.model.module.RAM;

import cpusim.model.util.IdentifiedObject;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

/**
 * The controller for editing the Branch command in the EditMicroDialog.
 */
public class RAMsTableController extends ModuleTableController<RAM> {

    static final String FX_ID = "ramTab";

    @FXML @SuppressWarnings("unused")
    private TableColumn<RAM,Integer> length;

    @FXML @SuppressWarnings("unused")
    private TableColumn<RAM,Integer> cellSize;

    /**
     * Constructor
     * @param mediator holds the machine and information needed
     */
    RAMsTableController(Mediator mediator){
        super(mediator, "RamTable.fxml", RAM.class);
    
        loadFXML();
    }

    /**
     * initializes the dialog window after its root element has been processed.
     * makes all the cells editable and the use can edit the cell directly and
     * hit enter to save the changes.
     */
    @Override
    public void initializeTable() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        name.prefWidthProperty().bind(prefWidthProperty().divide(100/40.0));
        length.prefWidthProperty().bind(prefWidthProperty().divide(100/30.0));
        cellSize.prefWidthProperty().bind(prefWidthProperty().divide(100/30.0));

        Callback<TableColumn<RAM, Integer>, TableCell<RAM, Integer>> cellIntFactory =
                setIntegerTableColumn -> new EditingNonNegativeIntCell<>();

        length.setCellValueFactory(new PropertyValueFactory<>("length"));
        cellSize.setCellValueFactory(new PropertyValueFactory<>("cellSize"));

        //Add for Editable Cell of each field, in String or in Integer
        
        length.setCellFactory(cellIntFactory);
        length.setOnEditCommit(text -> text.getRowValue().setLength(text.getNewValue()));

        cellSize.setCellFactory(cellIntFactory);
        cellSize.setOnEditCommit(text -> text.getRowValue().setCellSize(text.getNewValue()));
    }

    /**
     * getter for prototype of the right subclass
     * @return the prototype of the subclass
     */
    @Override
    public RAM createInstance() {
        return new RAM("???", IdentifiedObject.generateRandomID(), machine.get(), 128, 8);
    }

    /**
     * returns a string of the types of the controller
     * @return a string of the types of the controller
     */
    @Override
    public String toString() {
        return "RAM";
    }

    /**
     * get the ID of the corresponding help page
     * @return the ID of the page
     */
    @Override
    public String getHelpPageID()
    {
        return "RAMs";
    }
    
}
