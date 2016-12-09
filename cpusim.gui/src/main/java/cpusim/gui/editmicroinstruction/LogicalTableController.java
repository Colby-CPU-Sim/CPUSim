package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.table.EnumCellFactory;
import cpusim.model.Machine;
import cpusim.model.microinstruction.Logical;
import cpusim.model.module.Register;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.util.UUID;

/**
 * The controller for editing the {@link Logical} command in the {@link EditMicroinstructionsController}.
 *
 * @since 2013-06-06
 */
class LogicalTableController extends ALUOpTableController<Logical> {

    /**
     * Marker used when building tabs.
     */
    final static String FX_ID = "logicalTab";
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Logical, Logical.Type> type;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    LogicalTableController(Mediator mediator){
        super(mediator, "LogicalTable.fxml", Logical.class);

        loadFXML();
    }

    @Override
    public void initialize() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        final double FACTOR = 100/20.0;
        name.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        lhs.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        rhs.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        destination.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        type.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));

        //Add for Editable Cell of each field, in String or in Integer
        type.setCellValueFactory(new PropertyValueFactory<>("operation"));
        type.setCellFactory(new EnumCellFactory<>(Logical.Type.class));
        type.setOnEditCommit(text -> text.getRowValue().setOperation(text.getNewValue()));
    }

    @Override
    String getFxId() {
        return FX_ID;
    }

    @Override
    public Logical createInstance() {
        final Machine machine = this.machine.get();
        Register r = (machine.getRegisters().size() == 0 ? null :
                machine.getRegisters().get(0));
        return new Logical("???", UUID.randomUUID(), machine, Logical.Type.AND, r, r, r, null);
    }

    @Override
    public boolean isNewButtonEnabled() {
        return areRegistersAvailable();
    }
    
    @Override
    public String getHelpPageID()
    {
        return "Logical";
    }

}
