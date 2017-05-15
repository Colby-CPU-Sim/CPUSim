package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.table.EditingLongCell;
import cpusim.gui.util.table.EditingNonNegativeIntCell;
import cpusim.gui.util.table.MachineObjectCellFactories;
import cpusim.model.Machine;
import cpusim.model.microinstruction.SetBits;
import cpusim.model.module.Register;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * The controller for editing the Set command in the EditMicroDialog.
 *
 * @since 2013-06-06
 */
class SetTableController extends MicroinstructionTableController<SetBits> {

    /**
     * Marker used when building tabs.
     *
     * @see #getFxId()
     */
    final static String FX_ID = "setTab";

    @FXML @SuppressWarnings("unused")
    private TableColumn<SetBits,Register> register;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<SetBits,Integer> start;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<SetBits,Integer> numBits;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<SetBits,Long> value;
    

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    SetTableController(Mediator mediator) {
        super(mediator, "SetTable.fxml", SetBits.class);
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

        Callback<TableColumn<SetBits,Integer>,TableCell<SetBits,Integer>> cellIntFactory =
                setIntegerTableColumn -> new EditingNonNegativeIntCell<>();
        Callback<TableColumn<SetBits,Long>,TableCell<SetBits,Long>> cellLongFactory =
                setIntegerTableColumn -> new EditingLongCell<>();
        Callback<TableColumn<SetBits,Register>,TableCell<SetBits,Register>> cellComboFactory =
                MachineObjectCellFactories.modulesProperty(machineProperty(), Register.class);

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
    public Supplier<SetBits> getSupplier() {
        return () -> {
            final Machine machine = this.machine.get();
            Register r = (machine.getRegisters().isEmpty() ? null : machine.getRegisters().get(0));
            return new SetBits("???", UUID.randomUUID(), machine, r, 0, 1, 0L);
        };
    }

    @Override
    public void bindNewButtonDisabled(@Nonnull BooleanProperty toBind) {
        bindAreRegistersNotAvailable(toBind);
    }
    
    @Override
    public String getHelpPageID()
    {
        return "Set";
    }


}
