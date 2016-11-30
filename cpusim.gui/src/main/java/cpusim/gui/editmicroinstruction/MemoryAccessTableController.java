/**
 * Authoer: Jinghui Yu
 * Last editing date: 6/7/2013
 */

/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Changed the return value of checkValidity from a boolean to void (the functionality
 * enabled by that boolean value is now controlled by throwing ValidationException)
 * 2.) Changed the edit commit method on the name column so that it calls Validate.nameableObjects()
 * which throws a ValidationException in lieu of returning a boolean value
 */
package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.model.microinstruction.MemoryAccess;
import cpusim.model.module.RAM;
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
 * The controller for editing the {@link MemoryAccess} command in the {@link EditMicroinstructionsController}.
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
    private TableColumn<MemoryAccess,String> direction;
    
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
                        machine.getAllRegisters());
        Callback<TableColumn<MemoryAccess,String>,TableCell<MemoryAccess,String>> cellDircFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        FXCollections.observableArrayList(
                                "read",
                                "write"
                        )
                );
        Callback<TableColumn<MemoryAccess,RAM>,TableCell<MemoryAccess,RAM>> cellRAMFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        machine.getAllRAMs()
                );

        direction.setCellValueFactory(new PropertyValueFactory<>("direction"));
        memory.setCellValueFactory(new PropertyValueFactory<>("memory"));
        data.setCellValueFactory(new PropertyValueFactory<>("data"));
        address.setCellValueFactory(new PropertyValueFactory<>("address"));

        //Add for Editable Cell of each field, in String or in Integer
        direction.setCellFactory(cellDircFactory);
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
        Register r = (machine.getAllRegisters().isEmpty() ? null : machine.getAllRegisters().get(0));
        RAM ram = (machine.getModule(RAM.class).isEmpty() ? null : machine.getModule(RAM.class).get(0));
        return new MemoryAccess("???", machine, "read", ram, r, r);
    }

    @Override
    public boolean isNewButtonEnabled() {
        return areRegistersAvailable() && !machine.getModule(RAM.class).isEmpty();
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
