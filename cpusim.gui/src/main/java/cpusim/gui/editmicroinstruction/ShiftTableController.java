package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.EditingNonNegativeIntCell;
import cpusim.gui.util.NamedColumnHandler;
import cpusim.model.microinstruction.Shift;
import cpusim.model.module.Register;
import javafx.collections.FXCollections;
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
 * The controller for editing the shift command in the EditMicroDialog
 *
 * @author Jinghui Yu
 * @author Michael Goldenberg
 * @author Ben Borchard
 * @author Kevin Brightwell (Nava2)
 *
 * @since 2013-06-06
 */
class ShiftTableController extends MicroController<Shift> implements Initializable {
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Shift,String> name;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Shift,Register> source;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Shift,Register> destination;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Shift,String> type;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Shift,String> direction;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Shift,Integer> distance;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    ShiftTableController(Mediator mediator){
        super(mediator, "ShiftTable.fxml", Shift.class);
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
       
        final double FACTOR = 100.0/17.0;
        name.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        source.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        destination.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        type.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        direction.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        distance.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));

        // TODO cleanup with lambdas and remove wasteful casts
        Callback<TableColumn<Shift,String>,TableCell<Shift,String>> cellStrFactory =
                setStringTableColumn -> new cpusim.gui.util.EditingStrCell<>();
        Callback<TableColumn<Shift,String>,TableCell<Shift,String>> cellTypeFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        FXCollections.observableArrayList(
                                "logical",
                                "arithmetic",
                                "cyclic"
                        )
                );
        Callback<TableColumn<Shift,String>,TableCell<Shift,String>> cellDircFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        FXCollections.observableArrayList(
                                "left",
                                "right"
                        )
                );
        Callback<TableColumn<Shift,Integer>,TableCell<Shift,Integer>> cellIntFactory =
                setIntegerTableColumn -> new EditingNonNegativeIntCell<>();
        Callback<TableColumn<Shift,Register>,TableCell<Shift,Register>> cellComboFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        machine.getAllRegisters());

        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        source.setCellValueFactory(new PropertyValueFactory<>("source"));
        destination.setCellValueFactory(new PropertyValueFactory<>("destination"));
        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        direction.setCellValueFactory(new PropertyValueFactory<>("direction"));
        distance.setCellValueFactory(new PropertyValueFactory<>("distance"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(new NamedColumnHandler<>(this));

        source.setCellFactory(cellComboFactory);
        source.setOnEditCommit(
                text -> text.getRowValue().setSource(
                        text.getNewValue())
        );

        destination.setCellFactory(cellComboFactory);
        destination.setOnEditCommit(
                text -> text.getRowValue().setDestination(
                        text.getNewValue())
        );

        type.setCellFactory(cellTypeFactory);
        type.setOnEditCommit(
                text -> text.getRowValue().setType(
                        text.getNewValue())
        );

        direction.setCellFactory(cellDircFactory);
        direction.setOnEditCommit(
                text -> text.getRowValue().setDirection(
                        text.getNewValue())
        );

        distance.setCellFactory(cellIntFactory);
        distance.setOnEditCommit(
                text -> text.getRowValue().setDistance(
                        text.getNewValue())
        );
    }
    
    @Override
    public Shift getPrototype()
    {
        final Register r = (machine.getAllRegisters().size() == 0 ? null :
                machine.getAllRegisters().get(0));
        return new Shift("???", machine, r, r,"logical", "left", 1);
    }

    /**
     * returns a string about the type of the 
     * @return a string about the type of the 
     */
    public String toString()
    {
        return "Shift";
    }
    
    @Override
    public void updateMachineFromItems()
    {
        machine.setMicros(Shift.class, getItems());
    }
    
    @Override
    public void checkValidity(ObservableList<Shift> micros)
    {
        //check that all names are unique and nonempty
        Shift.validateNoNegativeDistances(micros);
        Shift.validateRegistersHaveEqualWidths(micros);
    }
    
    @Override
    public boolean newMicrosAreAllowed()
    {
        return (machine.getModule("registers").size() > 0 ||
                machine.getModule("registerArrays").size() > 0);
    }
    
    @Override
    public String getHelpPageID()
    {
        return "Shift";
    }
    
    @Override
    public void updateTable()
    {
        name.setVisible(false);
        name.setVisible(true);
        
        super.updateTable();
    }

}
