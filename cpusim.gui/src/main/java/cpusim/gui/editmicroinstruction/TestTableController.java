package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.table.EditingLongCell;
import cpusim.gui.util.table.EditingNonNegativeIntCell;
import cpusim.gui.util.table.EnumCellFactory;
import cpusim.model.Machine;
import cpusim.model.microinstruction.Test;
import cpusim.model.module.Register;
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
 * The controller for editing the {@link Test} command in the {@link EditMicroinstructionsController}.
 *
 * @since 2013-06-04
 */
class TestTableController extends MicroinstructionTableController<Test> {

    /**
     * Marker used when building tabs.
     *
     * @see #getFxId()
     */
    final static String FX_ID = "testTab";

    @FXML @SuppressWarnings("unused")
    private TableColumn<Test, Register> register;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Test, Integer> start;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Test, Integer> numBits;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Test, Test.Operation> comparison;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Test, Long> value;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Test, Integer> omission;
    

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    TestTableController(Mediator mediator){
        super(mediator, "TestTable.fxml", Test.class);
        loadFXML();
    }

    @Override
    public void initializeTable() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        final double FACTOR = 100/16.0;
        name.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        register.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        start.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        numBits.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        comparison.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        value.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        omission.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));

        Callback<TableColumn<Test,Integer>,TableCell<Test,Integer>> cellIntFactory =
                setIntegerTableColumn -> new EditingNonNegativeIntCell<>();
        Callback<TableColumn<Test,Long>,TableCell<Test,Long>> cellLongFactory =
                setIntegerTableColumn -> new EditingLongCell<>();
        Callback<TableColumn<Test,Register>,TableCell<Test,Register>> cellRegFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        machine.get().getRegisters());

        register.setCellValueFactory(new PropertyValueFactory<>("register"));
        start.setCellValueFactory(new PropertyValueFactory<>("start"));
        numBits.setCellValueFactory(new PropertyValueFactory<>("numBits"));
        comparison.setCellValueFactory(new PropertyValueFactory<>("comparison"));
        value.setCellValueFactory(new PropertyValueFactory<>("value"));
        omission.setCellValueFactory(new PropertyValueFactory<>("omission"));

        //Add for Editable Cell of each field, in String or in Integer
        register.setCellFactory(cellRegFactory);
        register.setOnEditCommit(text -> text.getRowValue().setRegister(text.getNewValue()));

        start.setCellFactory(cellIntFactory);
        start.setOnEditCommit(text -> text.getRowValue().setStart(text.getNewValue()));

        numBits.setCellFactory(cellIntFactory);
        numBits.setOnEditCommit(text -> text.getRowValue().setNumBits(text.getNewValue()));

        comparison.setCellFactory(new EnumCellFactory<>(Test.Operation.class));
        comparison.setOnEditCommit((text) ->
                text.getRowValue().setComparison(text.getNewValue()));

        value.setCellFactory(cellLongFactory);
        value.setOnEditCommit(text -> text.getRowValue().setValue(text.getNewValue()));

        omission.setCellFactory(cellIntFactory);
        omission.setOnEditCommit(text -> text.getRowValue().setOmission(text.getNewValue()));
    }

    @Override
    String getFxId() {
        return FX_ID;
    }

    @Override
    public Supplier<Test> getSupplier() {
        return () -> {
            final Machine machine = this.machine.get();
            final Register r = (machine.getRegisters().size() == 0 ? null :
                    machine.getRegisters().get(0));
            return new Test("???", UUID.randomUUID(), machine, r,
                    0, 1, Test.Operation.EQ,
                    0, 0);
        };
    }

    @Override
    public void bindNewButtonDisabled(@Nonnull BooleanProperty toBind) {
        bindAreRegistersNotAvailable(toBind);
    }

    @Override
    public String getHelpPageID()
    {
        return "Test";
    }
}
