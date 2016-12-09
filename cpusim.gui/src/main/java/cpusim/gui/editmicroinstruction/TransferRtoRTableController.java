package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.table.EditingNonNegativeIntCell;
import cpusim.model.microinstruction.TransferRtoR;
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
 * The controller for editing the {@link TransferRtoR} command in the {@link EditMicroinstructionsController}.
 */
class TransferRtoRTableController extends MicroinstructionTableController<TransferRtoR> {

    /**
     * Marker used when building tabs.
     *
     * @see #getFxId()
     */
    final static String FX_ID = "transferRtoRTab";

    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferRtoR,Register> source;

    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferRtoR,Integer> srcStartBit;

    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferRtoR,Register> dest;

    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferRtoR,Integer> destStartBit;

    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferRtoR,Integer> numBits;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    TransferRtoRTableController(Mediator mediator) {
        super(mediator, "TransferRtoRTable.fxml", TransferRtoR.class);
        loadFXML();
    }

    @Override
    void initialize() {
        super.initialize();

        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        name.prefWidthProperty().bind(prefWidthProperty().divide(100/17.0));
        source.prefWidthProperty().bind(prefWidthProperty().divide(100/17.0));
        srcStartBit.prefWidthProperty().bind(prefWidthProperty().divide(100/17.0));
        dest.prefWidthProperty().bind(prefWidthProperty().divide(100/17.0));
        destStartBit.prefWidthProperty().bind(prefWidthProperty().divide(100/16.0));
        numBits.prefWidthProperty().bind(prefWidthProperty().divide(100/16.0));

        Callback<TableColumn<TransferRtoR,Integer>,
                TableCell<TransferRtoR,Integer>> cellIntFactory =
                setIntegerTableColumn -> new EditingNonNegativeIntCell<>();
        Callback<TableColumn<TransferRtoR,Register>,
                TableCell<TransferRtoR,Register>> cellRegFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        machine.get().getRegisters());

        source.setCellValueFactory(new PropertyValueFactory<>("source"));
        srcStartBit.setCellValueFactory(new PropertyValueFactory<>("srcStartBit"));
        dest.setCellValueFactory(new PropertyValueFactory<>("dest"));
        destStartBit.setCellValueFactory(new PropertyValueFactory<>("destStartBit"));
        numBits.setCellValueFactory(new PropertyValueFactory<>("numBits"));

        //Add for Editable Cell of each field, in String or in Integer
        source.setCellFactory(cellRegFactory);
        source.setOnEditCommit(text -> text.getRowValue().setSource(text.getNewValue()));

        srcStartBit.setCellFactory(cellIntFactory);
        srcStartBit.setOnEditCommit(text -> text.getRowValue().setSrcStartBit(text.getNewValue()));

        dest.setCellFactory(cellRegFactory);
        dest.setOnEditCommit(text -> text.getRowValue().setDest(text.getNewValue()));

        destStartBit.setCellFactory(cellIntFactory);
        destStartBit.setOnEditCommit(text -> text.getRowValue().setDestStartBit(text.getNewValue()));

        numBits.setCellFactory(cellIntFactory);
        numBits.setOnEditCommit(text -> text.getRowValue().setNumBits(text.getNewValue()));
    }

    @Override
    String getFxId() {
        return FX_ID;
    }

    @Override
    public TransferRtoR createInstance() {
        final Register r = (machine.get().getRegisters().size() == 0 ? null :
                machine.get().getRegisters().get(0));
        return new TransferRtoR("???", UUID.randomUUID(), machine.get(),
                r, 0, r, 0, 0);
    }

    @Override
    public boolean isNewButtonEnabled() {
        return areRegistersAvailable();
    }

    @Override
    public void checkValidity() {
        super.checkValidity();
        // convert the array to an array of TransferRtoRs

        for (TransferRtoR micro: getItems()) {
            Register.validateIsNotReadOnly(micro.getDest(), micro.getName());
        }
    }

    @Override
    public String toString()
    {
        return "TransferRtoR";
    }

    @Override
    public String getHelpPageID()
    {
        return "Transfer";
    }

}
