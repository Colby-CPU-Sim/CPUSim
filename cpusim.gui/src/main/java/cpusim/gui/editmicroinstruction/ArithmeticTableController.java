package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.table.EnumCellFactory;
import cpusim.gui.util.table.MachineObjectCellFactories;
import cpusim.gui.util.table.NamedObjectCellFactories;
import cpusim.model.Machine;
import cpusim.model.microinstruction.Arithmetic;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.Register;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * The controller for editing the arithmetic command in the EditMicroDialog.
 *
 * @since 2013-10-27
 */
class ArithmeticTableController extends ALUOpTableController<Arithmetic> {

    /**
     * Marker used when building tabs.
     */
    static final String FX_ID = "arithmeticTab";


    @FXML @SuppressWarnings("unused")
    private TableColumn<Arithmetic, Arithmetic.Type> type;

    @FXML @SuppressWarnings("unused")
    private TableColumn<Arithmetic, ConditionBit> overflowBit;
    @FXML @SuppressWarnings("unused")
    private TableColumn<Arithmetic, ConditionBit> carryBit;

    /**
     * Constructor
     * @param mediator stores the information that will be shown in the 
     */
    ArithmeticTableController(Mediator mediator){
        super(mediator, "ArithmeticTable.fxml", Arithmetic.class);

        loadFXML();
    }


    /**
     * initializes the dialog window after its root element has been processed.
     * makes all the cells edittable and the use can edit the cell directly and
     * hit enter to save the changes.
     */
    @Override
    void initialize() {
        super.initialize();

        final double FACTOR = 100/14.0;
        name.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        lhs.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        rhs.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        destination.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        type.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        overflowBit.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        carryBit.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));

        NamedObjectCellFactories.ComboBox<Arithmetic, ConditionBit> cellCondFactory =
                MachineObjectCellFactories.modulesProperty(machineProperty(), ConditionBit.class);

        //Add for EdiCell of each field, in String or in Integer

        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        type.setCellFactory(new EnumCellFactory<>(Arithmetic.Type.class));
        type.setOnEditCommit(text -> text.getRowValue().setOperation(text.getNewValue()));

        overflowBit.setCellValueFactory(new PropertyValueFactory<>("overflowBit"));
        overflowBit.setCellFactory(cellCondFactory);
        overflowBit.setOnEditCommit(text -> text.getRowValue().setOverflowBit(text.getNewValue()));

        carryBit.setCellValueFactory(new PropertyValueFactory<>("carryBit"));
        carryBit.setCellFactory(cellCondFactory);
        carryBit.setOnEditCommit(text -> text.getRowValue().setCarryBit(text.getNewValue()));
    }

    @Override
    String getFxId() {
        return FX_ID;
    }

    /**
     * getter for prototype of the right subclass
     * @return the prototype of the subclass
     */
    @Override
    public Supplier<Arithmetic> getSupplier() {
        return () -> {
            final Machine machine = this.machine.get();
            final Register r = (machine.getRegisters().isEmpty() ? null : machine.getRegisters().get(0));
            return new Arithmetic("???", UUID.randomUUID(), machine, Arithmetic.Type.ADD, r, r, r,
                    null, null, null);
        };
    }


    @Override
    public void bindNewButtonDisabled(@Nonnull BooleanProperty toBind) {
        bindAreRegistersNotAvailable(toBind);
    }
    
    @Override
    public String getHelpPageID()
    {
        return "Arithmetic";
    }
}
