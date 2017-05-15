package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.table.EditingNonNegativeIntCell;
import cpusim.gui.util.table.EnumCellFactory;
import cpusim.gui.util.table.MachineObjectCellFactories;
import cpusim.model.microinstruction.Shift;
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
 * The controller for editing the shift command in the EditMicroDialog
 *
 * @since 2013-06-06
 */
class ShiftTableController extends MicroinstructionTableController<Shift> {

    /**
     * Marker used when building tabs.
     *
     * @see #getFxId()
     */
    final static String FX_ID = "shiftTab";

    @FXML @SuppressWarnings("unused")
    private TableColumn<Shift,Register> source;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Shift,Register> destination;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Shift, Shift.ShiftType> type;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Shift, Shift.ShiftDirection> direction;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Shift,Integer> distance;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    ShiftTableController(Mediator mediator){
        super(mediator, "ShiftTable.fxml", Shift.class);
        loadFXML();
    }

    @Override
    public void initializeTable() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
       
        final double FACTOR = 100.0/17.0;
        name.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        source.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        destination.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        type.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        direction.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        distance.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));

        Callback<TableColumn<Shift,Integer>,TableCell<Shift,Integer>> cellIntFactory =
                setIntegerTableColumn -> new EditingNonNegativeIntCell<>();
        Callback<TableColumn<Shift,Register>,TableCell<Shift,Register>> cellComboFactory =
                MachineObjectCellFactories.modulesProperty(machineProperty(), Register.class);

        source.setCellValueFactory(new PropertyValueFactory<>("source"));
        destination.setCellValueFactory(new PropertyValueFactory<>("destination"));
        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        direction.setCellValueFactory(new PropertyValueFactory<>("direction"));
        distance.setCellValueFactory(new PropertyValueFactory<>("distance"));

        //Add for Editable Cell of each field, in String or in Integer
        source.setCellFactory(cellComboFactory);
        source.setOnEditCommit(text -> text.getRowValue().setSource(text.getNewValue()));

        destination.setCellFactory(cellComboFactory);
        destination.setOnEditCommit(text -> text.getRowValue().setDest(text.getNewValue()));

        type.setCellFactory(new EnumCellFactory<>(Shift.ShiftType.class));
        type.setOnEditCommit(text -> text.getRowValue().setType(text.getNewValue()));

        direction.setCellFactory(new EnumCellFactory<>(Shift.ShiftDirection.class));
        direction.setOnEditCommit(text -> text.getRowValue().setDirection(text.getNewValue()));

        distance.setCellFactory(cellIntFactory);
        distance.setOnEditCommit(text -> text.getRowValue().setDistance(text.getNewValue()));
    }

    @Override
    String getFxId() {
        return FX_ID;
    }
    
    @Override
    public Supplier<Shift> getSupplier() {
        return () -> {
            final Register r = (machine.get().getRegisters().size() == 0 ? null :
                    machine.get().getRegisters().get(0));
            return new Shift("???", UUID.randomUUID(), machine.get(), r, r,
                    Shift.ShiftType.Logical, Shift.ShiftDirection.Left, 1);
        };
    }

    @Override
    public void bindNewButtonDisabled(@Nonnull BooleanProperty toBind) {
        bindAreRegistersNotAvailable(toBind);
    }

    @Override
    public String getHelpPageID() {
        return "Shift";
    }
}
