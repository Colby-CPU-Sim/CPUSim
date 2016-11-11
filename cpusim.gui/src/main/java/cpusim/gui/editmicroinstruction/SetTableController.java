package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.EditingLongCell;
import cpusim.gui.util.EditingNonNegativeIntCell;
import cpusim.gui.util.EditingStrCell;
import cpusim.model.microinstruction.CpusimSet;
import cpusim.model.module.Register;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * The controller for editing the Set command in the EditMicroDialog.
 *
 * @author Jinghui Yu
 * @author Michael Goldenberg
 * @author Ben Borchard
 * @author Kevin Brightwell (Nava2)
 *
 * @since 2013-06-06
 */
class SetTableController extends MicroController<CpusimSet> implements Initializable  {
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<CpusimSet,String> name;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<CpusimSet,Register> register;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<CpusimSet,Integer> start;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<CpusimSet,Integer> numBits;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<CpusimSet,Long> value;
    

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    SetTableController(Mediator mediator){
        super(mediator, "SetTable.fxml", CpusimSet.class);
    }

    /**
     * initializes the dialog window after its root element has been processed.
     * makes all the cells editable and the use can edit the cell directly and
     * hit enter to save the changes.
     *
     * @param url the location used to resolve relative paths for the root
     *            object, or null if the location is not known.
     * @param rb  the resources used to localize the root object, or null if the root
     *            object was not localized.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        final double FACTOR = 100.0/20.0;
        name.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        register.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        start.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        numBits.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        value.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));

        Callback<TableColumn<CpusimSet,String>,TableCell<CpusimSet,String>> cellStrFactory =
                setStringTableColumn -> new EditingStrCell<>();
        Callback<TableColumn<CpusimSet,Integer>,TableCell<CpusimSet,Integer>> cellIntFactory =
                setIntegerTableColumn -> new EditingNonNegativeIntCell<>();
        Callback<TableColumn<CpusimSet,Long>,TableCell<CpusimSet,Long>> cellLongFactory =
                setIntegerTableColumn -> new EditingLongCell<>();
        Callback<TableColumn<CpusimSet,Register>,TableCell<CpusimSet,Register>> cellComboFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        machine.getAllRegisters());

        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        register.setCellValueFactory(new PropertyValueFactory<>("register"));
        start.setCellValueFactory(new PropertyValueFactory<>("start"));
        numBits.setCellValueFactory(new PropertyValueFactory<>("numBits"));
        value.setCellValueFactory(new PropertyValueFactory<>("value"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(new NameColumnHandler());

        register.setCellFactory(cellComboFactory);
        register.setOnEditCommit(
                text -> text.getRowValue().setRegister(
                        text.getNewValue())
        );

        start.setCellFactory(cellIntFactory);
        start.setOnEditCommit(
                text -> text.getRowValue().setStart(
                        text.getNewValue())
        );

        numBits.setCellFactory(cellIntFactory);
        numBits.setOnEditCommit(
                text -> text.getRowValue().setNumBits(
                        text.getNewValue())
        );

        value.setCellFactory(cellLongFactory);
        value.setOnEditCommit(
                text -> text.getRowValue().setValue(
                        text.getNewValue())
        );
    }
    
    @Override
    public CpusimSet getPrototype()
    {
        Register r = (machine.getAllRegisters().size() == 0 ? null : machine.getAllRegisters().get(0));
        return new CpusimSet("???", machine, r, 0, 1, 0L);
    }

    /**
     * returns a string about the type of the 
     * @return a string about the type of the 
     */
    @Override
    public String toString()
    {
        return "Set";
    }
    
    @Override
    public void updateMachineFromItems()
    {
        machine.setMicros(CpusimSet.class, getItems());
    }

    @Override
    public void checkValidity(ObservableList<CpusimSet> micros)
    {
        // check that all names are unique and nonempty
        CpusimSet.validateRangeInBound(micros);
        CpusimSet.validateValueFitsInNumBitsForSetMicros(micros);
        
        super.checkValidity(micros);
    }
    
    @Override
    public boolean newMicrosAreAllowed()
    {
        return (machine.getModule("registers").size() > 0);
    }
    
    @Override
    public String getHelpPageID()
    {
        return "Set";
    }

    @Override
    public void updateTable()
    {
        name.setVisible(false);
        name.setVisible(true);
        
        super.updateTable();
    }

}
