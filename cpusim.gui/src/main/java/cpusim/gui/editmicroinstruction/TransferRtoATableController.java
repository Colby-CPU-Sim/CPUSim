/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Changed the return value of checkValidity from a boolean to void (the functionality
 * enabled by that boolean value is now controlled by throwing ValidationException)
 * 2.) Changed the edit commit method on the name column so that it calls Validate.nameableObjects()
 * which throws a ValidationException in lieu of returning a boolean value
 * 3.) Moved rangesInBound method to the Validate class and changed the return value to void
 * from boolean
 */
package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.table.EditingNonNegativeIntCell;
import cpusim.gui.util.table.MachineObjectCellFactories;
import cpusim.model.Machine;
import cpusim.model.microinstruction.TransferRtoA;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
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
 * The controller for editing the {@link TransferRtoA} command in the {@link EditMicroinstructionsController}.
 */
class TransferRtoATableController extends MicroinstructionTableController<TransferRtoA> {

    /**
     * Marker used when building tabs.
     *
     * @see #getFxId()
     */
    final static String FX_ID = "transferRtoATab";

    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferRtoA,Register> source;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferRtoA,Integer> srcStartBit;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferRtoA,RegisterArray> dest;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferRtoA,Integer> destStartBit;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferRtoA,Integer> numBits;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferRtoA,Register> index;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferRtoA,Integer> indexStart;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferRtoA,Integer> indexNumBits;


    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    TransferRtoATableController(Mediator mediator) {
        super(mediator, "TransferRtoATable.fxml", TransferRtoA.class);
        loadFXML();
    }

    @Override
    void initialize() {
        super.initialize();

        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        final double FACTOR = 100.0/11.0;
        name.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        source.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        srcStartBit.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        dest.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        destStartBit.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        numBits.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        index.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        indexStart.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        indexNumBits.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));

        Callback<TableColumn<TransferRtoA,Integer>,
                TableCell<TransferRtoA,Integer>> cellIntFactory =
                setIntegerTableColumn -> new EditingNonNegativeIntCell<>();
        Callback<TableColumn<TransferRtoA,Register>,
                TableCell<TransferRtoA,Register>> cellRegFactory =
                MachineObjectCellFactories.modulesProperty(machineProperty(), Register.class);
        Callback<TableColumn<TransferRtoA,RegisterArray>,
                TableCell<TransferRtoA,RegisterArray>> cellRegAFactory =
                MachineObjectCellFactories.modulesProperty(machineProperty(), RegisterArray.class);

        source.setCellValueFactory(new PropertyValueFactory<>("source"));
        srcStartBit.setCellValueFactory(new PropertyValueFactory<>("srcStartBit"));
        dest.setCellValueFactory(new PropertyValueFactory<>("dest"));
        destStartBit.setCellValueFactory(new PropertyValueFactory<>("destStartBit"));
        numBits.setCellValueFactory(new PropertyValueFactory<>("numBits"));
        index.setCellValueFactory(new PropertyValueFactory<>("index"));
        indexStart.setCellValueFactory(new PropertyValueFactory<>("indexStart"));
        indexNumBits.setCellValueFactory(new PropertyValueFactory<>("indexNumBits"));

        //Add for Editable Cell of each field, in String or in Integer
        
        source.setCellFactory(cellRegFactory);
        source.setOnEditCommit(text -> text.getRowValue().setSource(text.getNewValue()));

        srcStartBit.setCellFactory(cellIntFactory);
        srcStartBit.setOnEditCommit(text -> text.getRowValue().setSrcStartBit(text.getNewValue()));

        dest.setCellFactory(cellRegAFactory);
        dest.setOnEditCommit(text -> text.getRowValue().setDest(text.getNewValue()));

        destStartBit.setCellFactory(cellIntFactory);
        destStartBit.setOnEditCommit(text -> text.getRowValue().setDestStartBit(text.getNewValue()));

        numBits.setCellFactory(cellIntFactory);
        numBits.setOnEditCommit(text -> text.getRowValue().setNumBits(text.getNewValue()));

        index.setCellFactory(cellRegFactory);
        index.setOnEditCommit(text -> text.getRowValue().setIndex(text.getNewValue()));

        indexStart.setCellFactory(cellIntFactory);
        indexStart.setOnEditCommit(text -> text.getRowValue().setIndexStart(text.getNewValue()));

        indexNumBits.setCellFactory(cellIntFactory);
        indexNumBits.setOnEditCommit(
                text -> text.getRowValue().setIndexNumBits(text.getNewValue())
        );
    }

    @Override
    String getFxId() {
        return FX_ID;
    }
    
    @Override
    public Supplier<TransferRtoA> getSupplier() {
        return () -> {
            Machine machine = this.machine.get();
            final RegisterArray a = (machine.getModules(RegisterArray.class).size() == 0 ? null :
                    machine.getModules(RegisterArray.class).get(0));
            final Register r = (machine.getRegisters().size() == 0 ? null :
                    machine.getRegisters().get(0));
            return new TransferRtoA("???", UUID.randomUUID(), machine, r, 0, a,
                    0, 0, r,0, 0);
        };
    }

    @Override
    public void bindNewButtonDisabled(@Nonnull BooleanProperty toBind) {
        // Need at least one RegisterArray AND Register
        // Need at least one RegisterArray AND Register
        ObservableList<RegisterArray> arrays = machine.get().getModules(RegisterArray.class);
        ObservableList<Register> registers = machine.get().getModules(Register.class);

        toBind.bind(Bindings.isEmpty(arrays).or(Bindings.isEmpty(registers)));
    }

    @Override
    public String getHelpPageID()
    {
        return "Transfer";
    }
}
