/**
 * Author: Jinghui Yu
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
 * 3.) Moved registersHaveEqualWidths and noNegativeDistances method to the Validate
 * class and changed the return value to void
 * from boolean
 */
package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.Microinstruction;
import cpusim.gui.util.EditingIntCell;
import cpusim.microinstruction.Shift;
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
 * The controller for editing the shift command in the EditMicroDialog
 */
public class ShiftTableController
        extends MicroController implements Initializable{
    @FXML TableView<Shift> table;
    @FXML TableColumn<Shift,String> name;
    @FXML TableColumn<Shift,Register> source;
    @FXML TableColumn<Shift,Register> destination;
    @FXML TableColumn<Shift,String> type;
    @FXML TableColumn<Shift,String> direction;
    @FXML TableColumn<Shift,Integer> distance;

    private ObservableList currentMicros;
    private Shift prototype;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    public ShiftTableController(Mediator mediator){
        super(mediator);
        this.mediator = mediator;
        this.machine = this.mediator.getMachine();
        this.currentMicros = machine.getMicros("shift");
        Register r = (machine.getAllRegisters().size() == 0 ? null :
                (Register) machine.getAllRegisters().get(0));
        this.prototype = new Shift("???", machine, r, r,"logical", "left", 1);
        clones = (Microinstruction[]) createClones();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "shiftTable.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        for (int i = 0; i < clones.length; i++){
            table.getItems().add((Shift)clones[i]);
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
        name.prefWidthProperty().bind(table.prefWidthProperty().divide(100/17.0));
        source.prefWidthProperty().bind(table.prefWidthProperty().divide(100/17.0));
        destination.prefWidthProperty().bind(table.prefWidthProperty().divide(100/17.0));
        type.prefWidthProperty().bind(table.prefWidthProperty().divide(100/17.0));
        direction.prefWidthProperty().bind(table.prefWidthProperty().divide(100/16.0));
        distance.prefWidthProperty().bind(table.prefWidthProperty().divide(100/16.0));

        Callback<TableColumn<Shift,String>,TableCell<Shift,String>> cellStrFactory =
                new Callback<TableColumn<Shift, String>, TableCell<Shift, String>>() {
                    @Override
                    public TableCell<Shift, String> call(
                            TableColumn<Shift, String> setStringTableColumn) {
                        return new cpusim.gui.util.EditingStrCell<Shift>();
                    }
                };
        Callback<TableColumn<Shift,String>,TableCell<Shift,String>> cellTypeFactory =
                new Callback<TableColumn<Shift, String>, TableCell<Shift, String>>() {
                    @Override
                    public TableCell<Shift, String> call(
                            TableColumn<Shift, String> setStringTableColumn) {
                        return new ComboBoxTableCell<Shift,String>(
                                FXCollections.observableArrayList(
                                        "logical",
                                        "arithmetic",
                                        "cyclic"
                                )
                        );
                    }
                };
        Callback<TableColumn<Shift,String>,TableCell<Shift,String>> cellDircFactory =
                new Callback<TableColumn<Shift, String>, TableCell<Shift, String>>() {
                    @Override
                    public TableCell<Shift, String> call(
                            TableColumn<Shift, String> setStringTableColumn) {
                        return new ComboBoxTableCell<Shift,String>(
                                FXCollections.observableArrayList(
                                        "left",
                                        "right"
                                )
                        );
                    }
                };
        Callback<TableColumn<Shift,Integer>,TableCell<Shift,Integer>> cellIntFactory =
                new Callback<TableColumn<Shift, Integer>, TableCell<Shift, Integer>>() {
                    @Override
                    public TableCell<Shift, Integer> call(
                            TableColumn<Shift, Integer> setIntegerTableColumn) {
                        return new EditingIntCell<Shift>();
                    }
                };
        Callback<TableColumn<Shift,Register>,TableCell<Shift,Register>> cellComboFactory =
                new Callback<TableColumn<Shift, Register>, TableCell<Shift, Register>>() {
                    @Override
                    public TableCell<Shift, Register> call(
                            TableColumn<Shift, Register> setStringTableColumn) {
                        return new ComboBoxTableCell<Shift,Register>(
                                machine.getAllRegisters());
                    }
                };

        name.setCellValueFactory(new PropertyValueFactory<Shift, String>("name"));
        source.setCellValueFactory(new PropertyValueFactory<Shift, Register>("source"));
        destination.setCellValueFactory(new PropertyValueFactory<Shift, Register>("destination"));
        type.setCellValueFactory(new PropertyValueFactory<Shift, String>("type"));
        direction.setCellValueFactory(new PropertyValueFactory<Shift, String>("direction"));
        distance.setCellValueFactory(new PropertyValueFactory<Shift, Integer>("distance"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Shift, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Shift, String> text) {
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

        source.setCellFactory(cellComboFactory);
        source.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Shift, Register>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Shift, Register> text) {
                        ((Shift)text.getRowValue()).setSource(
                                text.getNewValue());
                    }
                }
        );

        destination.setCellFactory(cellComboFactory);
        destination.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Shift, Register>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Shift, Register> text) {
                        ((Shift)text.getRowValue()).setDestination(
                                text.getNewValue());
                    }
                }
        );

        type.setCellFactory(cellTypeFactory);
        type.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Shift, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Shift, String> text) {
                        ((Shift)text.getRowValue()).setType(
                                text.getNewValue());
                    }
                }
        );

        direction.setCellFactory(cellDircFactory);
        direction.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Shift, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Shift, String> text) {
                        ((Shift) text.getRowValue()).setDirection(
                                text.getNewValue());
                    }
                }
        );

        distance.setCellFactory(cellIntFactory);
        distance.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Shift, Integer>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Shift, Integer> text) {
                        ((Shift) text.getRowValue()).setDistance(
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
        return Shift.class;
    }

    /**
     * getter for the current Shift Microinstructions.
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
        return "Shift";
    }

    /**
     * gets properties
     * @return an array of String representations of the
     * various properties of this type of microinstruction
     */
//    public String[] getProperties()
//    {
//        return new String[]{"name", "source", "destination", "type",
//                "direction", "distance"};
//    }

    /**
     * use clones to replace existing Microinstructions
     * in the machine, and update the machine to delete
     * all references to the deleted Microinstructions.
     */
    public void updateCurrentMicrosFromClones()
    {
        machine.setMicros("shift", createNewMicroList(clones));
    }

    /**
     * Set the clones to the new array passed as a parameter.
     * Does not check for validity.
     *
     * @param newClones Object array containing new set of clones
     */
    public void setClones(ObservableList newClones)
    {
        Shift[] shifts = new Shift[newClones.size()];
        for (int i = 0; i < newClones.size(); i++) {
            shifts[i] = (Shift) newClones.get(i);
        }
        clones = shifts;
    }

    /**
     * Check validity of array of Objects' properties.
     * @param micros an array of Objects to check.
     * @return boolean denoting whether array has objects with
     * valid properties or not
     */
    public void checkValidity(ObservableList micros)
    {
        //convert it to an array of shift microinstructions
        Shift[] shifts = new Shift[micros.size()];
        for (int i = 0; i < shifts.length; i++)
            shifts[i] = (Shift) micros.get(i);

        //check that all names are unique and nonempty
        Validate.noNegativeDistances(shifts);
        Validate.registersHaveEqualWidths(shifts);
    }

    /**
     * returns true if new micros of this class can be created.
     * @return true if new micros of this class can be created.
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
        return "Shift";
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
