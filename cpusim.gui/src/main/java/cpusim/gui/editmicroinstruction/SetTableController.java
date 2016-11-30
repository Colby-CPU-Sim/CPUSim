package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.EditingLongCell;
import cpusim.gui.util.EditingNonNegativeIntCell;
import cpusim.model.microinstruction.CpusimSet;
import cpusim.model.module.Register;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

/**
 * The controller for editing the Set command in the EditMicroDialog.
 *
 * @since 2013-06-06
 */
class SetTableController extends MicroinstructionTableController<CpusimSet> {

    /**
     * Marker used when building tabs.
     *
     * @see #getFxId()
     */
    final static String FX_ID = "setTab";

    @FXML @SuppressWarnings("unused")
    private TableColumn<CpusimSet,Register> register;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<CpusimSet,Integer> start;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<CpusimSet,Integer> numBits;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<CpusimSet,Long> value;
    

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    SetTableController(Mediator mediator) {
        super(mediator, "SetTable.fxml", CpusimSet.class);
        loadFXML();
    }

    @Override
    public void initializeTable() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        final double FACTOR = 100.0/20.0;
        name.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        register.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        start.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        numBits.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        value.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));

        Callback<TableColumn<CpusimSet,Integer>,TableCell<CpusimSet,Integer>> cellIntFactory =
                setIntegerTableColumn -> new EditingNonNegativeIntCell<>();
        Callback<TableColumn<CpusimSet,Long>,TableCell<CpusimSet,Long>> cellLongFactory =
                setIntegerTableColumn -> new EditingLongCell<>();
        Callback<TableColumn<CpusimSet,Register>,TableCell<CpusimSet,Register>> cellComboFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        machine.getAllRegisters());

        register.setCellValueFactory(new PropertyValueFactory<>("register"));
        start.setCellValueFactory(new PropertyValueFactory<>("start"));
        numBits.setCellValueFactory(new PropertyValueFactory<>("numBits"));
        value.setCellValueFactory(new PropertyValueFactory<>("value"));

        //Add for Editable Cell of each field, in String or in Integer
        register.setCellFactory(cellComboFactory);
        register.setOnEditCommit(text -> text.getRowValue().setRegister(text.getNewValue()));

        start.setCellFactory(cellIntFactory);
        start.setOnEditCommit(text -> text.getRowValue().setStart(text.getNewValue()));

        numBits.setCellFactory(cellIntFactory);
        numBits.setOnEditCommit(text -> text.getRowValue().setNumBits(text.getNewValue()));

        value.setCellFactory(cellLongFactory);
        value.setOnEditCommit(text -> text.getRowValue().setValue(text.getNewValue()));
    }

    @Override
    String getFxId() {
        return FX_ID;
    }
    
    @Override
    public CpusimSet createInstance() {
        Register r = (machine.getAllRegisters().isEmpty() ? null : machine.getAllRegisters().get(0));
        return new CpusimSet("???", machine, r, 0, 1, 0L);
    }

    @Override
    public boolean isNewButtonEnabled() {
        return areRegistersAvailable();
    }

    @Override
    public String toString() {
        return "Set";
    }
    
    @Override
    public String getHelpPageID()
    {
        return "Set";
    }


}
