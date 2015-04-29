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
import cpusim.Microinstruction;
import cpusim.microinstruction.IO;
import cpusim.module.Register;
import cpusim.util.Validate;
import cpusim.util.ValidationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * The controller for editing the Logical command in the EditMicroDialog.
 */
public class IOTableController
        extends MicroController implements Initializable {
    @FXML TableView<IO> table;
    @FXML TableColumn<IO,String> name;
    @FXML TableColumn<IO,String> type;
    @FXML TableColumn<IO,Register> buffer;
    @FXML TableColumn<IO,String> direction;

    private ObservableList currentMicros;
    private IO prototype;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    public IOTableController(Mediator mediator){
        super(mediator);
        this.mediator = mediator;
        this.machine = this.mediator.getMachine();
        this.currentMicros = machine.getMicros("io");
        Register r = (machine.getAllRegisters().size() == 0 ? null :
                (Register) machine.getAllRegisters().get(0));
        this.prototype = new IO("???", machine, "integer", r, "input");
        clones = (Microinstruction[]) createClones();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "ioTable.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            // should never happen
            assert false : "Unable to load file: ioTable.fxml";
        }

        for (int i = 0; i < clones.length; i++){
            table.getItems().add((IO)clones[i]);
        }
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
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        name.prefWidthProperty().bind(table.prefWidthProperty().divide(100/25.0));
        buffer.prefWidthProperty().bind(table.prefWidthProperty().divide(100/25.0));
        direction.prefWidthProperty().bind(table.prefWidthProperty().divide(100/25.0));
        type.prefWidthProperty().bind(table.prefWidthProperty().divide(100/25.0));

        Callback<TableColumn<IO,String>,TableCell<IO,String>> cellStrFactory =
                new Callback<TableColumn<IO, String>, TableCell<IO, String>>() {
                    @Override
                    public TableCell<IO, String> call(
                            TableColumn<IO, String> setStringTableColumn) {
                        return new cpusim.gui.util.EditingStrCell<IO>();
                    }
                };
        Callback<TableColumn<IO,String>,TableCell<IO,String>> cellTypeFactory =
                new Callback<TableColumn<IO, String>, TableCell<IO, String>>() {
                    @Override
                    public TableCell<IO, String> call(
                            TableColumn<IO, String> setStringTableColumn) {
                        return new ComboBoxTableCell<IO,String>(
                                FXCollections.observableArrayList(
                                        "integer",
                                        "ascii",
                                        "unicode"
                                )
                        );
                    }
                };
        Callback<TableColumn<IO,Register>,TableCell<IO,Register>> cellRegFactory =
                new Callback<TableColumn<IO, Register>, TableCell<IO, Register>>() {
                    @Override
                    public TableCell<IO, Register> call(
                            TableColumn<IO, Register> setStringTableColumn) {
                        return new ComboBoxTableCell<IO,Register>(
                                machine.getAllRegisters());
                    }
                };
        Callback<TableColumn<IO,String>,TableCell<IO,String>> cellDircFactory =
                new Callback<TableColumn<IO, String>, TableCell<IO, String>>() {
                    @Override
                    public TableCell<IO, String> call(
                            TableColumn<IO, String> setStringTableColumn) {
                        return new ComboBoxTableCell<IO,String>(
                                FXCollections.observableArrayList(
                                        "input",
                                        "output"
                                )
                        );
                    }
                };

        name.setCellValueFactory(new PropertyValueFactory<IO, String>("name"));
        type.setCellValueFactory(new PropertyValueFactory<IO, String>("type"));
        buffer.setCellValueFactory(new PropertyValueFactory<IO, Register>("buffer"));
        direction.setCellValueFactory(new PropertyValueFactory<IO, String>("direction"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<IO, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<IO, String> text) {
                        String newName = text.getNewValue();
                        String oldName = text.getOldValue();
                        ( text.getRowValue()).setName(newName);
                        try{
                            Validate.namedObjectsAreUniqueAndNonempty(table.getItems().toArray());
                        } catch (ValidationException ex){
                            (text.getRowValue()).setName(oldName);
                            updateTable();
                        }
                    }
                }
        );

        type.setCellFactory(cellTypeFactory);
        type.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<IO, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<IO, String> text) {
                        ((IO)text.getRowValue()).setType(
                                text.getNewValue());
                    }
                }
        );

        buffer.setCellFactory(cellRegFactory);
        buffer.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<IO, Register>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<IO, Register> text) {
                        ((IO)text.getRowValue()).setBuffer(
                                text.getNewValue());
                    }
                }
        );

        direction.setCellFactory(cellDircFactory);
        direction.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<IO, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<IO, String> text) {
                        ((IO)text.getRowValue()).setDirection(
                                text.getNewValue());
                    }
                }
        );

    }

    /**
     * getter for prototype of the right subclass
     * @return the prototype of the subclass
     */
    public Microinstruction getPrototype()
    {
        return prototype;
    }

    /**
     * getter for the class object for the controller's objects
     * @return the class object
     */
    public Class getMicroClass()
    {
        return IO.class;
    }

    /**
     * getter for the current IO Microinstructions.
     * @return a list of current microinstructions.
     */
    public ObservableList getCurrentMicros()
    {
        return currentMicros;
    }

    /**
     * returns a string about the type of the table.
     * @return a string about the type of the table.
     */
    public String toString()
    {
        return "IO";
    }

    /**
     * gets properties
     * @return an array of String representations of the
     * various properties of this type of microinstruction
//     */
//    public String[] getProperties()
//    {
//        return new String[]{"name", "type", "buffer", "direction"};
//    }

    /**
     * use clones to replace existing Microinstructions
     * in the machine, and update the machine to delete
     * all references to the deleted Microinstructions.
     */
    public void updateCurrentMicrosFromClones()
    {
        machine.setMicros("io", createNewMicroList(clones));
    }

    /**
     * Set the clones to the new array passed as a parameter.
     * Does not check for validity.
     *
     * @param newClones Object array containing new set of clones
     */
    public void setClones(ObservableList newClones)
    {
        IO[] ios = new IO[newClones.size()];
        for (int i = 0; i < newClones.size(); i++) {
            ios[i] = (IO) newClones.get(i);
        }
        clones = ios;
    }

    /**
     * Check validity of array of Objects' properties.
     * @param micros an array of Objects to check.
     * @return boolean denoting whether array has objects with
     * valid properties or not
     */
    public void checkValidity(ObservableList micros)
    {
        //convert it to an array of io microinstructions
        IO[] ios = new IO[micros.size()];
        for (int i = 0; i < ios.length; i++)
            ios[i] = (IO) micros.get(i);

        //check that all names are unique and nonempty
        Validate.buffersAreWideEnough(ios);
    }



    /**
     * returns true if new micros of this class can be created.
     */
    public boolean newMicrosAreAllowed()
    {
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
    public void updateTable()
    {
        name.setVisible(false);
        name.setVisible(true);
        double w =  table.getWidth();
        table.setPrefWidth(w-1);
        table.setPrefWidth(w);
    }
}
