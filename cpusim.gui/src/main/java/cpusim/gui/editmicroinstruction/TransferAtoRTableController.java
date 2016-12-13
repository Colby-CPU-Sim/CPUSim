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
 *
 * Jinghui Yu, Ben Borchard and Michael Goldenberg modified this file on 11/11/13
 * with the following changes:
 *
 * 1.) Changed checkValidity method so that it calls Validate.registerIsNotReadOnly to check
 * if any destination register is read-only
 */
package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.table.EditingNonNegativeIntCell;
import cpusim.model.Machine;
import cpusim.model.microinstruction.TransferAtoR;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * The controller for editing the {@link TransferAtoR} command in the {@link EditMicroinstructionsController}.
 */
class TransferAtoRTableController
        extends MicroinstructionTableController<TransferAtoR> {

    /**
     * Marker used when building tabs.
     *
     * @see #getFxId()
     */
    final static String FX_ID = "transferAtoRTab";

    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferAtoR,RegisterArray> source;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferAtoR,Integer> srcStartBit;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferAtoR,Register> dest;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferAtoR,Integer> destStartBit;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferAtoR,Integer> numBits;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferAtoR,Register> index;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferAtoR,Integer> indexStart;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferAtoR,Integer> indexNumBits;
    
    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    TransferAtoRTableController(Mediator mediator){
        super(mediator, "TransferAtoRTable.fxml", TransferAtoR.class);
        loadFXML();
    }

    @Override
    void initialize() {
        super.initialize();

        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        name.prefWidthProperty().bind(prefWidthProperty().divide(100/12.0));
        source.prefWidthProperty().bind(prefWidthProperty().divide(100/11.0));
        srcStartBit.prefWidthProperty().bind(prefWidthProperty().divide(100/11.0));
        dest.prefWidthProperty().bind(prefWidthProperty().divide(100/11.0));
        destStartBit.prefWidthProperty().bind(prefWidthProperty().divide(100/11.0));
        numBits.prefWidthProperty().bind(prefWidthProperty().divide(100/11.0));
        index.prefWidthProperty().bind(prefWidthProperty().divide(100/11.0));
        indexStart.prefWidthProperty().bind(prefWidthProperty().divide(100/11.0));
        indexNumBits.prefWidthProperty().bind(prefWidthProperty().divide(100/11.0));
        
        Callback<TableColumn<TransferAtoR,Integer>,
                TableCell<TransferAtoR,Integer>> cellIntFactory = _ignore -> new EditingNonNegativeIntCell<>();
        
        Callback<TableColumn<TransferAtoR,Register>,
                TableCell<TransferAtoR,Register>> cellRegFactory =
                    _ignore -> new ComboBoxTableCell<>(machine.get().getRegisters());
        
        Callback<TableColumn<TransferAtoR,RegisterArray>,
                TableCell<TransferAtoR,RegisterArray>> cellRegAFactory =
                    _ignore -> new ComboBoxTableCell<>(machine.get().getModules(RegisterArray.class));

        source.setCellValueFactory(new PropertyValueFactory<>("source"));
        srcStartBit.setCellValueFactory(new PropertyValueFactory<>("srcStartBit"));
        dest.setCellValueFactory(new PropertyValueFactory<>("dest"));
        destStartBit.setCellValueFactory(new PropertyValueFactory<>("destStartBit"));
        numBits.setCellValueFactory(new PropertyValueFactory<>("numBits"));
        index.setCellValueFactory(new PropertyValueFactory<>("index"));
        indexStart.setCellValueFactory(new PropertyValueFactory<>("indexStart"));
        indexNumBits.setCellValueFactory(new PropertyValueFactory<>("indexNumBits"));

        //Add for Editable Cell of each field, in String or in Integer
        source.setCellFactory(cellRegAFactory);
        source.setOnEditCommit(text -> text.getRowValue().setSource(text.getNewValue()));

        srcStartBit.setCellFactory(_ignore -> new EditingNonNegativeIntCell<>());
        srcStartBit.setOnEditCommit(text -> text.getRowValue().setSrcStartBit(text.getNewValue()));

        dest.setCellFactory(cellRegFactory);
        dest.setOnEditCommit(text -> text.getRowValue().setDest(text.getNewValue()));

        destStartBit.setCellFactory(_ignore -> new EditingNonNegativeIntCell<>());
        destStartBit.setOnEditCommit(text -> text.getRowValue().setDestStartBit(text.getNewValue()));

        numBits.setCellFactory(cellIntFactory);
        numBits.setOnEditCommit(text -> text.getRowValue().setNumBits(text.getNewValue()));

        index.setCellFactory(cellRegFactory);
        index.setOnEditCommit(text -> text.getRowValue().setIndex(text.getNewValue()));

        indexStart.setCellFactory(cellIntFactory);
        indexStart.setOnEditCommit(text -> text.getRowValue().setIndexStart(text.getNewValue()));

        indexNumBits.setCellFactory(cellIntFactory);
        indexNumBits.setOnEditCommit(text -> text.getRowValue().setIndexNumBits(text.getNewValue()));
    }

    
    @Override
    public Supplier<TransferAtoR> getSupplier() {
        return () -> {
            final Machine machine = this.machine.get();
            RegisterArray a = (machine.getModules(RegisterArray.class).isEmpty() ? null :
                    machine.getModules(RegisterArray.class).get(0));
            Register r = (machine.getRegisters().isEmpty() ? null : machine.getRegisters().get(0));
            return new TransferAtoR("???", UUID.randomUUID(), machine, a, 0,
                    r, 0, 0, r, 0, 0);
        };
    }

    @Override
    public void bindNewButtonDisabled(@Nonnull BooleanProperty toBind) {
        // Need at least one RegisterArray AND Register
        ObservableList<RegisterArray> arrays = machine.get().getModules(RegisterArray.class);
        ObservableList<Register> registers = machine.get().getModules(Register.class);

        toBind.bind(Bindings.isEmpty(arrays).or(Bindings.isEmpty(registers)));
    }

    @Override
    public void checkValidity() {
        super.checkValidity();
        
        // convert the array to an array of TransferAtoRs
        for (TransferAtoR check: getItems()) {
            Register.validateIsNotReadOnly(check.getDest(), check.getName());
        }
    }

    @Override
    String getFxId() {
        return FX_ID;
    }

    @Override
    public String getHelpPageID()
    {
        return "Transfer";
    }

}