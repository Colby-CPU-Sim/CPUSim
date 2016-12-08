package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.model.Machine;
import cpusim.model.microinstruction.SetCondBit;
import cpusim.model.module.ConditionBit;
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
 * The controller for editing the Branch command in the EditMicroDialog.
 *
 * @since 2013-06-07
 */
class SetCondBitTableController extends MicroinstructionTableController<SetCondBit> {

    /**
     * Marker used when building tabs.
     *
     * @see #getFxId()
     */
    final static String FX_ID = "setCondBitTab";

    @FXML @SuppressWarnings("unused")
    private TableColumn<SetCondBit,ConditionBit> bit;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<SetCondBit,String> value;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    SetCondBitTableController(Mediator mediator){
        super(mediator, "SetCondBitTable.fxml", SetCondBit.class);
        loadFXML();
    }

    @Override
    public void initializeTable() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        name.prefWidthProperty().bind(prefWidthProperty().divide(100/34.0));
        bit.prefWidthProperty().bind(prefWidthProperty().divide(100/33.0));
        value.prefWidthProperty().bind(prefWidthProperty().divide(100/33.0));

        Callback<TableColumn<SetCondBit,String>,TableCell<SetCondBit,String>> cellIntFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        FXCollections.observableArrayList(
                                "0",
                                "1"
                        ));
        Callback<TableColumn<SetCondBit,ConditionBit>,TableCell<SetCondBit,ConditionBit>> cellCondFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(machine.get().getModules(ConditionBit.class));

        bit.setCellValueFactory(new PropertyValueFactory<>("bit"));
        value.setCellValueFactory(new PropertyValueFactory<>("value"));

        //Add for Editable Cell of each field, in String or in Integer
        bit.setCellFactory(cellCondFactory);
        bit.setOnEditCommit(text -> text.getRowValue().setBit(text.getNewValue()));

        value.setCellFactory(cellIntFactory);
        value.setOnEditCommit(text -> text.getRowValue().setValue(text.getNewValue()));
    }

    @Override
    String getFxId() {
        return FX_ID;
    }
    
    @Override
    public SetCondBit createInstance() {
        final Machine machine = this.machine.get();
        ConditionBit cBit = (machine.getModules(ConditionBit.class).isEmpty() ? null :
                machine.getModules(ConditionBit.class).get(0));
        return new SetCondBit("???", machine, cBit, "0");
    }

    @Override
    public String toString()
    {
        return "SetCondBit";
    }

    @Override
    public void checkValidity() {
        super.checkValidity();
        
        for (SetCondBit micro: getItems()) {
            Register.validateIsNotReadOnly(micro.getBit().getRegister(), micro.getName());
        }
    }

    @Override
    public boolean isNewButtonEnabled()
    {
        return !machine.get().getModules(ConditionBit.class).isEmpty();
    }

    @Override
    public String getHelpPageID()
    {
        return "SetCondBit";
    }
}
