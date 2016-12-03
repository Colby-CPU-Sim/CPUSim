package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.model.Machine;
import cpusim.model.microinstruction.Arithmetic;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.Register;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

/**
 * The controller for editing the arithmetic command in the EditMicroDialog.
 *
 * @since 2013-10-27
 */
class ArithmeticTableController extends MicroinstructionTableController<Arithmetic> {

    /**
     * Marker used when building tabs.
     */
    static final String FX_ID = "arithmeticTab";

    @FXML @SuppressWarnings("unused")
    private TableColumn<Arithmetic,Register> source1;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Arithmetic,Register> source2;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Arithmetic,Register> destination;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Arithmetic,String> type;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Arithmetic,ConditionBit> overflowBit;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Arithmetic,ConditionBit> carryBit;

    /**
     * Constructor
     * @param mediator stores the information that will be shown in the 
     */
    ArithmeticTableController(Mediator mediator){
        super(mediator, "ArithmeticTable.fxml", Arithmetic.class);

        loadFXML();
    }


    /**
     * initializes the dialog window after its root element has been processed.
     * makes all the cells edittable and the use can edit the cell directly and
     * hit enter to save the changes.
     */
    @Override
    void initializeTable() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        final double FACTOR = 100/14.0;
        name.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        source1.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        source2.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        destination.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        type.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        overflowBit.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        carryBit.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));

        Callback<TableColumn<Arithmetic,String>,TableCell<Arithmetic,String>> cellTypeFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        FXCollections.observableArrayList(
                                "ADD",
                                "SUBTRACT",
                                "MULTIPLY",
                                "DIVIDE"
                        )
                );


        Callback<TableColumn<Arithmetic,Register>,TableCell<Arithmetic,Register>> cellRegFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        machine.get().getAllRegisters());

        final ObservableList<ConditionBit> condBit = FXCollections.observableArrayList(ConditionBit.none());
        condBit.addAll(machine.get().getModule(ConditionBit.class));
        Callback<TableColumn<Arithmetic,ConditionBit>,TableCell<Arithmetic,ConditionBit>> cellCondFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(condBit);

        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        source1.setCellValueFactory(new PropertyValueFactory<>("source1"));
        source2.setCellValueFactory(new PropertyValueFactory<>("source2"));
        destination.setCellValueFactory(new PropertyValueFactory<>("destination"));
        overflowBit.setCellValueFactory(new PropertyValueFactory<>("overflowBit"));
        carryBit.setCellValueFactory(new PropertyValueFactory<>("carryBit"));

        //Add for EdiCell of each field, in String or in Integer
        type.setCellFactory(cellTypeFactory);
        type.setOnEditCommit(text -> text.getRowValue().setType(text.getNewValue()));

        source1.setCellFactory(cellRegFactory);
        source1.setOnEditCommit(text -> text.getRowValue().setSource1(text.getNewValue()));

        source2.setCellFactory(cellRegFactory);
        source2.setOnEditCommit(text -> text.getRowValue().setSource2(text.getNewValue()));

        destination.setCellFactory(cellRegFactory);
        destination.setOnEditCommit(text -> text.getRowValue().setDestination(text.getNewValue()));

        overflowBit.setCellFactory(cellCondFactory);
        overflowBit.setOnEditCommit(text -> text.getRowValue().setOverflowBit(text.getNewValue()));

        carryBit.setCellFactory(cellCondFactory);
        carryBit.setOnEditCommit(text -> text.getRowValue().setCarryBit(text.getNewValue()));
    }

    @Override
    String getFxId() {
        return FX_ID;
    }

    /**
     * getter for prototype of the right subclass
     * @return the prototype of the subclass
     */
    @Override
    public Arithmetic createInstance() {
        final Machine machine = this.machine.get();
        final Register r = (machine.getAllRegisters().isEmpty() ? null : machine.getAllRegisters().get(0));
        return new Arithmetic("???", machine, "ADD", r, r, r, ConditionBit.none(), ConditionBit.none());
    }

    /**
     * returns a string about the type of the 
     * @return a string about the type of the 
     */
    @Override
    public String toString()
    {
        return "Arithmetic";
    }

    @Override
    public boolean isNewButtonEnabled() {
        return areRegistersAvailable();
    }
    
    @Override
    public String getHelpPageID()
    {
        return "Arithmetic";
    }
}
