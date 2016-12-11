package cpusim.gui.editmodules;

import cpusim.gui.util.table.EditingNonNegativeIntCell;
import cpusim.model.module.RAM;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * The controller for editing {@link RAM} components
 */
public class RAMsTableController extends ModuleTableController<RAM> {

    static final String FX_ID = "ramTab";

    @FXML @SuppressWarnings("unused")
    private TableColumn<RAM,Integer> length;

    @FXML @SuppressWarnings("unused")
    private TableColumn<RAM,Integer> cellSize;

    /**
     * Constructor
     */
    RAMsTableController(){
        super("RamTable.fxml", RAM.class);
    
        loadFXML();
    }

    /**
     * initializes the dialog window after its root element has been processed.
     * makes all the cells editable and the use can edit the cell directly and
     * hit enter to save the changes.
     */
    @Override
    public void initialize() {
        super.initialize();

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
    public Supplier<RAM> supplierBinding() {
        return () -> new RAM("???", UUID.randomUUID(), getMachine(), 128, 8);
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
