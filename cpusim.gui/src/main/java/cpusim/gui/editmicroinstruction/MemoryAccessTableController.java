package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.table.EnumCellFactory;
import cpusim.model.Machine;
import cpusim.model.microinstruction.IODirection;
import cpusim.model.microinstruction.MemoryAccess;
import cpusim.model.module.RAM;
import cpusim.model.module.Register;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.util.UUID;

/**
 * The controller for editing the {@link MemoryAccess} command in the {@link EditMicroinstructionsController}.
 *
 * @since 2013-06-07
 */
class MemoryAccessTableController
        extends MicroinstructionTableController<MemoryAccess> {

    /**
     * Marker used when building tabs.
     *
     * @see #getFxId()
     */
    final static String FX_ID = "memoryAccessTab";
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<MemoryAccess, IODirection> direction;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<MemoryAccess,RAM> memory;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<MemoryAccess,Register> data;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<MemoryAccess,Register> address;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */

    MemoryAccessTableController(Mediator mediator){
        super(mediator, "MemoryAccessTable.fxml", MemoryAccess.class);
        loadFXML();
    }


    @Override
    void initializeTable() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        final double FACTOR = 100.0/20.0;
        name.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        direction.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        memory.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        data.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        address.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));

        Callback<TableColumn<MemoryAccess,Register>,TableCell<MemoryAccess,Register>> cellRegFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        machine.get().getRegisters());
        Callback<TableColumn<MemoryAccess,RAM>,TableCell<MemoryAccess,RAM>> cellRAMFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        machine.get().getModules(RAM.class));

        direction.setCellValueFactory(new PropertyValueFactory<>("direction"));
        memory.setCellValueFactory(new PropertyValueFactory<>("memory"));
        data.setCellValueFactory(new PropertyValueFactory<>("data"));
        address.setCellValueFactory(new PropertyValueFactory<>("address"));

        //Add for Editable Cell of each field, in String or in Integer
        direction.setCellFactory(new EnumCellFactory<>(IODirection.class));
        direction.setOnEditCommit(text -> text.getRowValue().setDirection(text.getNewValue()));

        memory.setCellFactory(cellRAMFactory);
        memory.setOnEditCommit(text -> text.getRowValue().setMemory(text.getNewValue()));

        data.setCellFactory(cellRegFactory);
        data.setOnEditCommit(text -> text.getRowValue().setData(text.getNewValue()));

        address.setCellFactory(cellRegFactory);
        address.setOnEditCommit(text -> text.getRowValue().setAddress(text.getNewValue()));
    }

    @Override
    String getFxId() {
        return FX_ID;
    }

    @Override
    public MemoryAccess createInstance() {
        final Machine machine = this.machine.get();
        
        Register r = (machine.getRegisters().isEmpty() ? null : machine.getRegisters().get(0));
        RAM ram = (machine.getModules(RAM.class).isEmpty() ? null : machine.getModules(RAM.class).get(0));
        return new MemoryAccess("???", UUID.randomUUID(), machine, IODirection.Read, ram, r, r);
    }

    @Override
    public boolean isNewButtonEnabled() {
        return areRegistersAvailable() && !machine.get().getModules(RAM.class).isEmpty();
    }

    @Override
    public String toString()
    {
        return "MemoryAccess";
    }

    @Override
    public String getHelpPageID()
    {
        return "MemoryAccess";
    }
}
