package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.model.Machine;
import cpusim.model.microinstruction.SetCondBit;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.Register;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
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
    private TableColumn<SetCondBit, Boolean> value;

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

        Callback<TableColumn<SetCondBit,ConditionBit>,TableCell<SetCondBit,ConditionBit>> cellCondFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(machine.get().getModules(ConditionBit.class));

        bit.setCellValueFactory(new PropertyValueFactory<>("bit"));
        value.setCellValueFactory(new PropertyValueFactory<>("value"));

        //Add for Editable Cell of each field, in String or in Integer
        bit.setCellFactory(cellCondFactory);
        bit.setOnEditCommit(text -> text.getRowValue().setBit(text.getNewValue()));

        value.setCellFactory(param ->
                new TableCell<SetCondBit, Boolean>() {
                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);

                        if (!empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.toString());
                        }
                    }
                });
        value.setOnEditCommit(text -> text.getRowValue().setValue(text.getNewValue()));
    }

    @Override
    String getFxId() {
        return FX_ID;
    }
    
    @Override
    public Supplier<SetCondBit> getSupplier() {
        return () -> {
            final Machine machine = this.machine.get();
            ConditionBit cBit = (machine.getModules(ConditionBit.class).isEmpty() ? null :
                    machine.getModules(ConditionBit.class).get(0));
            return new SetCondBit("???", UUID.randomUUID(), machine, cBit, false);
        };
    }

    @Override
    public void checkValidity() {
        super.checkValidity();
        
        for (SetCondBit micro: getItems()) {
            Register.validateIsNotReadOnly(micro.getBit().getRegister(), micro.getName());
        }
    }

    @Override
    public void bindNewButtonDisabled(@Nonnull BooleanProperty toBind) {
        toBind.bind(Bindings.isNotEmpty(machine.get().getModules(ConditionBit.class)));
    }

    @Override
    public String getHelpPageID()
    {
        return "SetCondBit";
    }
}
