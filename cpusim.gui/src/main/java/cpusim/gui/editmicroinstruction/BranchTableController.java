package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.table.EditingNonNegativeIntCell;
import cpusim.model.microinstruction.Branch;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.UUID;

/**
 * The controller for editing the Branch command in the EditMicroDialog.
 *
 * @since 2013-10-27
 */
class BranchTableController extends MicroinstructionTableController<Branch> {

    /**
     * Marker used when building tabs.
     */
    static final String FX_ID = "branchTab";
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Branch, Integer> amount;

    /**
     * Constructor
     * @param mediator stores the information that will be shown in the tables
     */
    BranchTableController(Mediator mediator){
        super(mediator, "BranchTable.fxml", Branch.class);
        loadFXML();
    }

    @Override
    void initializeTable() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        final double FACTOR = 100.0/50.0;
        name.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        amount.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));


        //Add for Editable Cell of each field, in String or in Integer
        amount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amount.setCellFactory(setIntegerTableColumn -> new EditingNonNegativeIntCell<>());
        amount.setOnEditCommit(text -> text.getRowValue().setAmount(text.getNewValue()));
    }

    @Override
    String getFxId() {
        return FX_ID;
    }

    @Override
    public Branch createInstance() {
        return new Branch("???", UUID.randomUUID(), machine.get(), 0);
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
    public String getHelpPageID()
    {
        return "Branch";
    }

}
