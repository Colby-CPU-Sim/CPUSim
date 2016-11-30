package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.model.microinstruction.Decode;
import cpusim.model.module.Register;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

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
    void initializeTable() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        name.prefWidthProperty().bind(prefWidthProperty().divide(100/50.0));
        ir.prefWidthProperty().bind(prefWidthProperty().divide(100/50.0));

        Callback<TableColumn<Decode,Register>,TableCell<Decode,Register>> cellRegFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        machine.getAllRegisters());

        ir.setCellValueFactory(new PropertyValueFactory<>("ir"));

        //Add for EdiCell of each field, in String or in Integer
        ir.setCellFactory(cellRegFactory);
        ir.setOnEditCommit(
                text -> text.getRowValue().setIr(text.getNewValue())
        );
    }

    @Override
    String getFxId() {
        return FX_ID;
    }

    @Override
    public Decode createInstance() {
        Register r = (machine.getAllRegisters().size() == 0 ? null :
                machine.getAllRegisters().get(0));
        return new Decode("???", machine, r);
    }

    @Override
    public String toString()
    {
        return "Decode";
    }

    @Override
    public boolean isNewButtonEnabled() {
        return areRegistersAvailable();
    }

    @Override
    public String getHelpPageID()
    {
        return "Decode";
    }

}
