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
import cpusim.microinstruction.Logical;
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
public class LogicalTableController
        extends MicroController implements Initializable {
    @FXML TableView<Logical> table;
    @FXML TableColumn<Logical,String> name;
    @FXML TableColumn<Logical,Register> source1;
    @FXML TableColumn<Logical,Register> source2;
    @FXML TableColumn<Logical,Register> destination;
    @FXML TableColumn<Logical,String> type;

    private ObservableList currentMicros;
    private Logical prototype;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    public LogicalTableController(Mediator mediator){
        super(mediator);
        this.mediator = mediator;
        this.machine = this.mediator.getMachine();
        this.currentMicros = machine.getMicros("logical");
        Register r = (machine.getAllRegisters().size() == 0 ? null :
                (Register) machine.getAllRegisters().get(0));
        this.prototype = new Logical("???", machine, "AND", r, r, r);
        clones = (Microinstruction[]) createClones();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "logicalTable.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        for (int i = 0; i < clones.length; i++){
            table.getItems().add((Logical)clones[i]);
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
        name.prefWidthProperty().bind(table.prefWidthProperty().divide(100/20.0));
        source1.prefWidthProperty().bind(table.prefWidthProperty().divide(100/20.0));
        source2.prefWidthProperty().bind(table.prefWidthProperty().divide(100/20.0));
        destination.prefWidthProperty().bind(table.prefWidthProperty().divide(100/20.0));
        type.prefWidthProperty().bind(table.prefWidthProperty().divide(100/20.0));

        Callback<TableColumn<Logical,String>,TableCell<Logical,String>> cellStrFactory =
                new Callback<TableColumn<Logical, String>, TableCell<Logical, String>>() {
                    @Override
                    public TableCell<Logical, String> call(
                            TableColumn<Logical, String> setStringTableColumn) {
                        return new cpusim.gui.util.EditingStrCell<Logical>();
                    }
                };
        Callback<TableColumn<Logical,String>,TableCell<Logical,String>> cellTypeFactory =
                new Callback<TableColumn<Logical, String>, TableCell<Logical, String>>() {
                    @Override
                    public TableCell<Logical, String> call(
                            TableColumn<Logical, String> setStringTableColumn) {
                        return new ComboBoxTableCell<Logical,String>(
                                FXCollections.observableArrayList(
                                        "AND",
                                        "OR",
                                        "NAND",
                                        "NOR",
                                        "XOR",
                                        "NOT"
                                )
                        );
                    }
                };
        Callback<TableColumn<Logical,Register>,TableCell<Logical,Register>> cellComboFactory =
                new Callback<TableColumn<Logical, Register>, TableCell<Logical, Register>>() {
                    @Override
                    public TableCell<Logical, Register> call(
                            TableColumn<Logical, Register> setStringTableColumn) {
                        return new ComboBoxTableCell<Logical,Register>(
                                machine.getAllRegisters());
                    }
                };

        name.setCellValueFactory(new PropertyValueFactory<Logical, String>("name"));
        type.setCellValueFactory(new PropertyValueFactory<Logical, String>("type"));
        source1.setCellValueFactory(new PropertyValueFactory<Logical, Register>("source1"));
        source2.setCellValueFactory(new PropertyValueFactory<Logical, Register>("source2"));
        destination.setCellValueFactory(new PropertyValueFactory<Logical, Register>("destination"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Logical, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Logical, String> text) {
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
                new EventHandler<TableColumn.CellEditEvent<Logical, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Logical, String> text) {
                        ((Logical)text.getRowValue()).setType(
                                text.getNewValue());
                    }
                }
        );

        source1.setCellFactory(cellComboFactory);
        source1.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Logical, Register>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Logical, Register> text) {
                        ((Logical)text.getRowValue()).setSource1(
                                text.getNewValue());
                    }
                }
        );

        source2.setCellFactory(cellComboFactory);
        source2.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Logical, Register>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Logical, Register> text) {
                        ((Logical)text.getRowValue()).setSource2(
                                text.getNewValue());
                    }
                }
        );

        destination.setCellFactory(cellComboFactory);
        destination.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Logical, Register>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Logical, Register> text) {
                        ((Logical)text.getRowValue()).setDestination(
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
        return Logical.class;
    }

    /**
     * getter for the current Logical Microinstructions.
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
        return "Logical";
    }

    /**
     * gets properties
     * @return an array of String representations of the
     * various properties of this type of microinstruction
     */
//    public String[] getProperties()
//    {
//        return new String[]{"name", "type", "source1", "source2", "destination"};
//    }

    /**
     * use clones to replace existing Microinstructions
     * in the machine, and update the machine to delete
     * all references to the deleted Microinstructions.
     */
    public void updateCurrentMicrosFromClones()
    {
        machine.setMicros("logical", createNewMicroList(clones));
    }

    /**
     * Set the clones to the new array passed as a parameter.
     * Does not check for validity.
     *
     * @param newClones Object array containing new set of clones
     */
    public void setClones(ObservableList newClones)
    {
        Logical[] logicals = new Logical[newClones.size()];
        for (int i = 0; i < newClones.size(); i++) {
            logicals[i] = (Logical) newClones.get(i);
        }
        clones = logicals;
    }

    /**
     * Check validity of array of Objects' properties.
     * @param micros an array of Objects to check.
     * @return boolean denoting whether array has objects with
     * valid properties or not
     */
    public void checkValidity(ObservableList micros)
    {
        // convert the array to an array of Branches
        Logical[] logicals = new Logical[micros.size()];

        for (int i = 0; i < micros.size(); i++) {
            logicals[i] = (Logical) micros.get(i);
        }

        // check that all names are unique and nonempty
        Validate.registersHaveEqualWidths(logicals);

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
        return "Logical";
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
