package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
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

/**
 * The controller for editing the {@link Logical} command in the {@link EditMicroinstructionsController}.
 *
 * @since 2013-06-06
 */
class LogicalTableController extends MicroinstructionTableController<Logical> {

    /**
     * Marker used when building tabs.
     */
    final static String FX_ID = "logicalTab";

    @FXML @SuppressWarnings("unused")
    private TableColumn<Logical,Register> source1;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Logical,Register> source2;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Logical,Register> destination;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Logical,String> type;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    LogicalTableController(Mediator mediator){
        super(mediator, "LogicalTable.fxml", Logical.class);
        loadFXML();
    }

    @Override
    public void initializeTable() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        final double FACTOR = 100/20.0;
        name.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        source1.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        source2.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        destination.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        type.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));

        Callback<TableColumn<Logical,String>,TableCell<Logical,String>> cellTypeFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        FXCollections.observableArrayList(
                                "AND",
                                "OR",
                                "NAND",
                                "NOR",
                                "XOR",
                                "NOT"
                        )
                );
        Callback<TableColumn<Logical,Register>,TableCell<Logical,Register>> cellComboFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        machine.get().getAllRegisters());

        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        source1.setCellValueFactory(new PropertyValueFactory<>("source1"));
        source2.setCellValueFactory(new PropertyValueFactory<>("source2"));
        destination.setCellValueFactory(new PropertyValueFactory<>("destination"));

        //Add for Editable Cell of each field, in String or in Integer
        type.setCellFactory(cellTypeFactory);
        type.setOnEditCommit(text -> text.getRowValue().setType(text.getNewValue()));

        source1.setCellFactory(cellComboFactory);
        source1.setOnEditCommit(text -> text.getRowValue().setSource1(text.getNewValue()));

        source2.setCellFactory(cellComboFactory);
        source2.setOnEditCommit(text -> text.getRowValue().setSource2(text.getNewValue()));

        destination.setCellFactory(cellComboFactory);
        destination.setOnEditCommit(text -> text.getRowValue().setDestination(text.getNewValue()));
    }

    @Override
    String getFxId() {
        return FX_ID;
    }

    @Override
    public Logical createInstance() {
        final Machine machine = this.machine.get();
        Register r = (machine.getAllRegisters().size() == 0 ? null :
                machine.getAllRegisters().get(0));
        return new Logical("???", machine, "AND", r, r, r);
    }

    @Override
    public boolean isNewButtonEnabled() {
        return areRegistersAvailable();
    }

    @Override
    public String toString()
    {
        return "Logical";
    }
    
    @Override
    public String getHelpPageID()
    {
        return "Logical";
    }

}
