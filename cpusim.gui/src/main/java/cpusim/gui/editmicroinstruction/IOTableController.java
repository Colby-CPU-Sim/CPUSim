package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.table.EnumCellFactory;
import cpusim.model.Machine;
import cpusim.model.microinstruction.IO;
import cpusim.model.microinstruction.IODirection;
import cpusim.model.module.Register;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * The controller for editing the {@link IO} command in the {@link EditMicroinstructionsController}.
 */
class IOTableController extends MicroinstructionTableController<IO> {

    /**
     * Marker used when building tabs.
     */
    final static String FX_ID = "ioTab";

    @FXML @SuppressWarnings("unused")
    private TableColumn<IO, IO.Type> type;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<IO, Register> buffer;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<IO, IODirection> direction;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    IOTableController(Mediator mediator) {
        super(mediator, "IOTable.fxml", IO.class);
        loadFXML();
    }

    @Override
    void initialize() {
        super.initialize();

        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        final double FACTOR = 100/25.0;
        name.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        buffer.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        direction.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        type.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));

        Callback<TableColumn<IO,Register>,TableCell<IO,Register>> cellRegFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        machine.get().getRegisters());

        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        buffer.setCellValueFactory(new PropertyValueFactory<>("buffer"));
        direction.setCellValueFactory(new PropertyValueFactory<>("direction"));

        //Add for Editable Cell of each field, in String or in Integer
        type.setCellFactory(new EnumCellFactory<>(IO.Type.class));
        type.setOnEditCommit(text -> text.getRowValue().setType(text.getNewValue()));

        buffer.setCellFactory(cellRegFactory);
        buffer.setOnEditCommit(text -> text.getRowValue().setBuffer(text.getNewValue()));

        direction.setCellFactory(new EnumCellFactory<>(IODirection.class));
        direction.setOnEditCommit(text -> text.getRowValue().setDirection(text.getNewValue()));
    }

    @Override
    String getFxId() {
        return FX_ID;
    }

    @Override
    public Supplier<IO> supplierBinding() {
        return () -> {
            final Machine machine = this.machine.get();
            Register r = (machine.getRegisters().size() == 0 ? null :
                    machine.getRegisters().get(0));
            return new IO("???", UUID.randomUUID(), machine, IO.Type.Integer, r, IODirection.Read, null);
        };
    }

    @Override
    public BooleanBinding newButtonEnabledBinding() {
        return areRegistersAvailable();
    }

    @Override
    public String toString()
    {
        return "IO";
    }

    @Override
    public String getHelpPageID()
    {
        return "IO";
    }
}
