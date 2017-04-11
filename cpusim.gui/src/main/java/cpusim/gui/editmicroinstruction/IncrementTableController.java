package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.table.EditingLongCell;
import cpusim.gui.util.table.MachineObjectCellFactories;
import cpusim.model.Machine;
import cpusim.model.microinstruction.Increment;
import cpusim.model.module.ConditionBit;
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
 * The controller for editing the Increment command in the EditMicroDialog.
 *
 * @since 2013-06-05
 */
class IncrementTableController
        extends MicroinstructionTableController<Increment> {

    /**
     * Marker used when building tabs.
     */
    static final String FX_ID = "incrementTab";
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Increment, Register> register;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Increment, ConditionBit> overflowBit;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Increment, ConditionBit> carryBit;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Increment, Long> delta;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    IncrementTableController(Mediator mediator){
        super(mediator, "IncrementTable.fxml", Increment.class);
        loadFXML();
    }

    @Override
    public void initializeTable() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        final double FACTOR = 100/20.0;
        name.prefWidthProperty().bind(widthProperty().divide(FACTOR));
        register.prefWidthProperty().bind(widthProperty().divide(FACTOR));
        overflowBit.prefWidthProperty().bind(widthProperty().divide(FACTOR));
        carryBit.prefWidthProperty().bind(widthProperty().divide(FACTOR));
        delta.prefWidthProperty().bind(widthProperty().divide(FACTOR));

        Callback<TableColumn<Increment,Long>,TableCell<Increment,Long>> cellLongFactory =
                setIntegerTableColumn -> new EditingLongCell<>();
        Callback<TableColumn<Increment,Register>,TableCell<Increment,Register>> cellRegFactory =
                MachineObjectCellFactories.modulesProperty(machineProperty(), Register.class);
        Callback<TableColumn<Increment,ConditionBit>,TableCell<Increment,ConditionBit>> cellCondFactory =
                MachineObjectCellFactories.modulesProperty(machineProperty(), ConditionBit.class);

        register.setCellValueFactory(new PropertyValueFactory<>("register"));
        overflowBit.setCellValueFactory(new PropertyValueFactory<>("overflowBit"));
        carryBit.setCellValueFactory(new PropertyValueFactory<>("carryBit"));
        delta.setCellValueFactory(new PropertyValueFactory<>("delta"));

        //Add for Editable Cell of each field, in String or in Integer
        register.setCellFactory(cellRegFactory);
        register.setOnEditCommit(text -> text.getRowValue().setRegister(text.getNewValue()));

        overflowBit.setCellFactory(cellCondFactory);
        overflowBit.setOnEditCommit(text -> text.getRowValue().setOverflowBit(text.getNewValue()));

        carryBit.setCellFactory(cellCondFactory);
        carryBit.setOnEditCommit(text -> text.getRowValue().setCarryBit(text.getNewValue()));

        delta.setCellFactory(cellLongFactory);
        delta.setOnEditCommit(text -> text.getRowValue().setDelta(text.getNewValue()));
    }

    @Override
    String getFxId() {
        return FX_ID;
    }

    @Override
    public Supplier<Increment> getSupplier() {
        return () -> {
            final Machine machine = this.machine.get();
            Register r = (machine.getRegisters().size() == 0 ? null : machine.getRegisters().get(0));
            return new Increment("???", UUID.randomUUID(), machine, r, 1L,
                    null, null, null);
        };
    }

    @Override
    public String toString()
    {
        return "Increment";
    }

    @Override
    public void bindNewButtonDisabled(@Nonnull BooleanProperty toBind) {
        bindAreRegistersNotAvailable(toBind);
    }

    @Override
    public String getHelpPageID()
    {
        return "Increment";
    }


}