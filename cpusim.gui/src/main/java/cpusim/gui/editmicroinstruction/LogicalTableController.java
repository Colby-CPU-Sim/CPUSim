package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.model.microinstruction.Logical;
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
 * The controller for editing the Logical command in the EditMicroDialog.
 *
 * @author Jinghui Yu
 * @author Michael Goldenberg
 * @author Ben Borchard
 * @author Kevin Brightwell (Nava2)
 *
 * @since 2013-06-06
 */
class LogicalTableController extends MicroController<Logical> implements Initializable {

    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Logical,String> name;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Logical,Register> source1;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Logical,Register> source2;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Logical,Register> destination;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Logical,String> type;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    LogicalTableController(Mediator mediator){
        super(mediator, "LogicalTable.fxml", Logical.class);
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
        
        final double FACTOR = 100/20.0;
        name.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        source1.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        source2.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        destination.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        type.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));

        Callback<TableColumn<Logical,String>,TableCell<Logical,String>> cellStrFactory =
                setStringTableColumn -> new cpusim.gui.util.EditingStrCell<>();
        Callback<TableColumn<Logical,String>,TableCell<Logical,String>> cellTypeFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        FXCollections.observableArrayList(
                                "AND",
                                "OR",
                                "NAND",
                                "NOR",
                                "XOR",
                                "NOT"
                        )
                );
        Callback<TableColumn<Logical,Register>,TableCell<Logical,Register>> cellComboFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        machine.getAllRegisters());

        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        source1.setCellValueFactory(new PropertyValueFactory<>("source1"));
        source2.setCellValueFactory(new PropertyValueFactory<>("source2"));
        destination.setCellValueFactory(new PropertyValueFactory<>("destination"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(new NameColumnHandler());

        type.setCellFactory(cellTypeFactory);
        type.setOnEditCommit(
                text -> text.getRowValue().setType(text.getNewValue())
        );

        source1.setCellFactory(cellComboFactory);
        source1.setOnEditCommit(
                text -> text.getRowValue().setSource1(
                        text.getNewValue())
        );

        source2.setCellFactory(cellComboFactory);
        source2.setOnEditCommit(
                text -> text.getRowValue().setSource2(
                        text.getNewValue())
        );

        destination.setCellFactory(cellComboFactory);
        destination.setOnEditCommit(
                text -> text.getRowValue().setDestination(
                        text.getNewValue())
        );

    }

    @Override
    public Logical getPrototype()
    {
        Register r = (machine.getAllRegisters().size() == 0 ? null :
                machine.getAllRegisters().get(0));
        return new Logical("???", machine, "AND", r, r, r);
    }
    
    /**
     * returns a string about the type of the 
     * @return a string about the type of the 
     */
    @Override
    public String toString()
    {
        return "Logical";
    }
    
    @Override
    public void updateMachineFromItems()
    {
        machine.setMicros(Logical.class, getItems());
    }
    
    @Override
    public void checkValidity(ObservableList<Logical> micros)
    {
        super.checkValidity(micros);
        
        Logical.validateRegistersHaveEqualWidths(micros);
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
        return "Logical";
    }

    @Override
    public void updateTable()
    {
        name.setVisible(false);
        name.setVisible(true);
        
        super.updateTable();
    }

}
