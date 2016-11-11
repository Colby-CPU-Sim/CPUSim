package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.EditingNonNegativeIntCell;
import cpusim.gui.util.EditingStrCell;
import cpusim.model.microinstruction.Branch;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * The controller for editing the Branch command in the EditMicroDialog.
 *
 * @author Jinghui Yu
 * @author Michael Goldenberg
 * @author Ben Borchard
 * @author Kevin Brightwell (Nava2)
 *
 * @since 2013-10-27
 */
class BranchTableController extends MicroController<Branch> implements Initializable {
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Branch,String> name;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Branch,Integer> amount;

    /**
     * Constructor
     * @param mediator stores the information that will be shown in the tables
     */
    BranchTableController(Mediator mediator){
        super(mediator, "BranchTable.fxml", Branch.class);
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
        
        final double FACTOR = 100.0/50.0;
        name.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        amount.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));

        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        amount.setCellValueFactory(new PropertyValueFactory<>("amount"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory((setStringTableColumn) -> new EditingStrCell<>());
        name.setOnEditCommit(new NameColumnHandler());

        amount.setCellFactory(setIntegerTableColumn -> new EditingNonNegativeIntCell<>());
        amount.setOnEditCommit(
                text -> text.getRowValue().setAmount(text.getNewValue())
        );
    }
    
    @Override
    public Branch getPrototype() {
        return new Branch("???", machine, 0, machine.getControlUnit());
    }

    /**
     * returns a string about the type of the table.
     * @return a string about the type of the table.
     */
    @Override
    public String toString()
    {
        return "Branch";
    }

    @Override
    public void updateMachineFromItems()
    {
        machine.setMicros(Branch.class, getItems());
    }

    @Override
    public boolean newMicrosAreAllowed()
    {
        return true;
    }
    
    @Override
    public String getHelpPageID()
    {
        return "Branch";
    }

    @Override
    public void updateTable()
    {
        name.setVisible(false);
        name.setVisible(true);
     
        super.updateTable();
    }

}
