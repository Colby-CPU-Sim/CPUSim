/**
 * Authoer: Jinghui Yu
 * Last editing date: 6/6/2013
 */

/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Changed the return value of checkValidity from a boolean to void (the functionality
 * enabled by that boolean value is now controlled by throwing ValidationException)
 * 2.) Changed the edit commit method on the name column so that it calls Validate.nameableObjects()
 * which throws a ValidationException in lieu of returning a boolean value
 * 3.) Moved registersHaveEqualWidths method to the Validate class and changed the return value to void
 * from boolean
 */
package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.NamedColumnHandler;
import cpusim.model.microinstruction.IO;
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
 */
class IOTableController extends MicroController<IO> implements Initializable {
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<IO,String> name;
    
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
    IOTableController(Mediator mediator){
        super(mediator, "IOTable.fxml", IO.class);
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
        
        final double FACTOR = 100/25.0;
        name.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        buffer.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        direction.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        type.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));

        Callback<TableColumn<IO,String>,TableCell<IO,String>> cellStrFactory =
                setStringTableColumn -> new cpusim.gui.util.EditingStrCell<>();
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
                        machine.getAllRegisters());
        Callback<TableColumn<IO,String>,TableCell<IO,String>> cellDircFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        FXCollections.observableArrayList(
                                "input",
                                "output"
                        )
                );

        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        buffer.setCellValueFactory(new PropertyValueFactory<>("buffer"));
        direction.setCellValueFactory(new PropertyValueFactory<>("direction"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(new NamedColumnHandler<>(this));

        type.setCellFactory(cellTypeFactory);
        type.setOnEditCommit(
                text -> text.getRowValue().setType(
                        text.getNewValue())
        );

        buffer.setCellFactory(cellRegFactory);
        buffer.setOnEditCommit(
                text -> text.getRowValue().setBuffer(
                        text.getNewValue())
        );

        direction.setCellFactory(cellDircFactory);
        direction.setOnEditCommit(
                text -> text.getRowValue().setDirection(
                        text.getNewValue())
        );

    }

    /**
     * getter for prototype of the right subclass
     * @return the prototype of the subclass
     */
    @Override
    public IO getPrototype() {
        Register r = (machine.getAllRegisters().size() == 0 ? null :
                machine.getAllRegisters().get(0));
        return new IO("???", machine, "integer", r, "input");
    }
    /**
     * returns a string about the type of the 
     * @return a string about the type of the 
     */
    public String toString()
    {
        return "IO";
    }
    
    @Override
    public void updateMachineFromItems()
    {
        machine.setMicros(IO.class, getItems());
    }
    
    @Override
    public void checkValidity(ObservableList<IO> micros)
    {
        super.checkValidity(micros);
        
        IO.validateBuffersAreWideEnough(micros);
    }
    
    /**
     * returns true if new micros of this class can be created.
     */
    @Override
    public boolean newMicrosAreAllowed() {
        return (machine.getModule("registers").size() > 0 ||
                machine.getModule("registerArrays").size() > 0);
    }

    /**
     * get the ID of the corresponding help page
     * @return the ID of the page
     */
    public String getHelpPageID()
    {
        return "IO";
    }

    /**
     * updates the table by removing all the items and adding all back.
     * for refreshing the display.
     */
    public void updateTable() {
        name.setVisible(false);
        name.setVisible(true);
        
        super.updateTable();
    }
}
