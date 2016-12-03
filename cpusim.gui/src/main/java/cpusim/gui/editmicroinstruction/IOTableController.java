package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.model.Machine;
import cpusim.model.microinstruction.IO;
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
 * The controller for editing the {@link IO} command in the {@link EditMicroinstructionsController}.
 */
class IOTableController extends MicroinstructionTableController<IO> {

    /**
     * Marker used when building tabs.
     */
    final static String FX_ID = "ioTab";

    @FXML @SuppressWarnings("unused")
    private TableColumn<IO,String> type;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<IO,Register> buffer;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<IO,String> direction;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    IOTableController(Mediator mediator) {
        super(mediator, "IOTable.fxml", IO.class);
        loadFXML();
    }

    @Override
    void initializeTable() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        final double FACTOR = 100/25.0;
        name.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        buffer.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        direction.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        type.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));

        Callback<TableColumn<IO,String>,TableCell<IO,String>> cellTypeFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        FXCollections.observableArrayList(
                                "integer",
                                "ascii",
                                "unicode"
                        )
                );
        Callback<TableColumn<IO,Register>,TableCell<IO,Register>> cellRegFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        machine.get().getAllRegisters());
        Callback<TableColumn<IO,String>,TableCell<IO,String>> cellDircFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        FXCollections.observableArrayList(
                                "input",
                                "output"
                        )
                );

        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        buffer.setCellValueFactory(new PropertyValueFactory<>("buffer"));
        direction.setCellValueFactory(new PropertyValueFactory<>("direction"));

        //Add for Editable Cell of each field, in String or in Integer
        type.setCellFactory(cellTypeFactory);
        type.setOnEditCommit(text -> text.getRowValue().setType(text.getNewValue()));

        buffer.setCellFactory(cellRegFactory);
        buffer.setOnEditCommit(text -> text.getRowValue().setBuffer(text.getNewValue()));

        direction.setCellFactory(cellDircFactory);
        direction.setOnEditCommit(text -> text.getRowValue().setDirection(text.getNewValue()));
    }

    @Override
    String getFxId() {
        return FX_ID;
    }

    @Override
    public IO createInstance() {
        final Machine machine = this.machine.get();
        Register r = (machine.getAllRegisters().size() == 0 ? null :
                machine.getAllRegisters().get(0));
        return new IO("???", machine, "integer", r, "input");
    }

    @Override
    public boolean isNewButtonEnabled() {
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
