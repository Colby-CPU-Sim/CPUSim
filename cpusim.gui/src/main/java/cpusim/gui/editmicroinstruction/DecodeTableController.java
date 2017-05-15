package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.table.MachineObjectCellFactories;
import cpusim.model.Machine;
import cpusim.model.microinstruction.Decode;
import cpusim.model.module.Register;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * The controller for editing the Branch command in the EditMicroDialog.
 *
 * @since 2013-06-07
 */
class DecodeTableController extends MicroinstructionTableController<Decode> {

    /**
     * Marker used when building tabs.
     */
    static final String FX_ID = "decodeTab";
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Decode, Register> ir;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    DecodeTableController(Mediator mediator){
        super(mediator, "DecodeTable.fxml", Decode.class);
        loadFXML();
    }


    @Override
    void initialize() {
        super.initialize();

        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        name.prefWidthProperty().bind(prefWidthProperty().divide(100/50.0));
        ir.prefWidthProperty().bind(prefWidthProperty().divide(100/50.0));

        ir.setCellValueFactory(new PropertyValueFactory<>("ir"));

        //Add for EdiCell of each field, in String or in Integer
        ir.setCellFactory(
                MachineObjectCellFactories.modulesProperty(machineProperty(), Register.class));
        ir.setOnEditCommit(
                text -> text.getRowValue().setIr(text.getNewValue())
        );
    }

    @Override
    String getFxId() {
        return FX_ID;
    }

    @Override
    public Supplier<Decode> getSupplier() {
        return () -> {
            final Machine machine = this.machine.get();
            Register r = (machine.getRegisters().size() == 0 ? null :
                    machine.getRegisters().get(0));
            return new Decode("???", UUID.randomUUID(), machine, r);
        };
    }

    @Override
    public void bindNewButtonDisabled(@Nonnull BooleanProperty toBind) {
        bindAreRegistersNotAvailable(toBind);
    }

    @Override
    public String getHelpPageID()
    {
        return "Decode";
    }

}
