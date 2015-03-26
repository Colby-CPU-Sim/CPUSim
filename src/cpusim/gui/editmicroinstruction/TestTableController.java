/**
 * auther: Jinghui Yu
 * last edit date: 6/4/2013
 */


/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Changed the return value of checkValidity from a boolean to void (the functionality
 * enabled by that boolean value is now controlled by throwing ValidationException)
 * 2.) Changed the edit commit method on the name column so that it calls Validate.nameableObjects()
 * which throws a ValidationException in lieu of returning a boolean value
 * 3.) Moved rangeInBound method to the Validate class and changed the return value to void
 * from boolean
 */
package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.Microinstruction;
import cpusim.gui.util.EditingNonNegativeIntCell;
import cpusim.gui.util.EditingLongCell;
import cpusim.gui.util.EditingStrCell;
import cpusim.microinstruction.Test;
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
 * The controller for editing the Test command in the EditMicroDialog.
 */
public class TestTableController
        extends MicroController implements Initializable {
    @FXML TableView<Test> table;
    @FXML TableColumn<Test,String> name;
    @FXML TableColumn<Test,Register> register;
    @FXML TableColumn<Test,Integer> start;
    @FXML TableColumn<Test,Integer> numBits;
    @FXML TableColumn<Test,String> comparison;
    @FXML TableColumn<Test,Long> value;
    @FXML TableColumn<Test,Integer> omission;

    private ObservableList currentMicros;
    private Test prototype;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    public TestTableController(Mediator mediator){
        super(mediator);
        this.mediator = mediator;
        this.machine = this.mediator.getMachine();
        this.currentMicros = machine.getMicros("test");
        Register r = (machine.getAllRegisters().size() == 0 ? null :
                (Register) machine.getAllRegisters().get(0));
        this.prototype = new Test("???", machine, r, 0, 1, "EQ", Long.valueOf(0), 0);
        clones = (Microinstruction[]) createClones();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "testTable.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        for (int i = 0; i < clones.length; i++){
            table.getItems().add((Test)clones[i]);
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
        name.prefWidthProperty().bind(table.prefWidthProperty().divide(100/16.0));
        register.prefWidthProperty().bind(table.prefWidthProperty().divide(100/14.0));
        start.prefWidthProperty().bind(table.prefWidthProperty().divide(100/14.0));
        numBits.prefWidthProperty().bind(table.prefWidthProperty().divide(100/14.0));
        comparison.prefWidthProperty().bind(table.prefWidthProperty().divide(100/14.0));
        value.prefWidthProperty().bind(table.prefWidthProperty().divide(100/14.0));
        omission.prefWidthProperty().bind(table.prefWidthProperty().divide(100 / 14.0));

        Callback<TableColumn<Test, String>, TableCell<Test, String>> cellStrFactory =
                new Callback<TableColumn<Test, String>, TableCell<Test, String>>() {
                    @Override
                    public TableCell call(
                            TableColumn setStringTableColumn) {
                        return new EditingStrCell<Test>();
                    }
                };
        Callback<TableColumn<Test,Integer>,TableCell<Test,Integer>> cellIntFactory =
                new Callback<TableColumn<Test,Integer>, TableCell<Test, Integer>>() {
                    @Override
                    public TableCell call(
                            TableColumn<Test, Integer> setIntegerTableColumn) {
                        return new EditingNonNegativeIntCell<Test>();
                    }
                };
        Callback<TableColumn<Test,Long>,TableCell<Test,Long>> cellLongFactory =
                new Callback<TableColumn<Test,Long>, TableCell<Test, Long>>() {
                    @Override
                    public TableCell call(
                            TableColumn<Test, Long> setIntegerTableColumn) {
                        return new EditingLongCell<Test>();
                    }
                };
        Callback<TableColumn<Test,Register>,TableCell<Test,Register>> cellRegFactory =
                new Callback<TableColumn<Test, Register>, TableCell<Test, Register>>() {
                    @Override
                    public TableCell<Test,Register> call(
                            TableColumn<Test, Register> setStringTableColumn) {
                        return new ComboBoxTableCell<Test,Register>(
                                machine.getAllRegisters());
                    }
                };

        Callback<TableColumn<Test,String>,TableCell<Test,String>> cellCompFactory =
                new Callback<TableColumn<Test, String>, TableCell<Test, String>>() {
                    @Override
                    public TableCell<Test, String> call(
                            TableColumn<Test, String> setStringTableColumn) {
                        return new ComboBoxTableCell<Test,String>(
                                FXCollections.observableArrayList(
                                        "EQ",
                                        "NE",
                                        "LT",
                                        "GT",
                                        "LE",
                                        "GE"
                                )
                        );
                    }
                };

        name.setCellValueFactory(new PropertyValueFactory<Test, String>("name"));
        register.setCellValueFactory(new PropertyValueFactory<Test, Register>("register"));
        start.setCellValueFactory(new PropertyValueFactory<Test, Integer>("start"));
        numBits.setCellValueFactory(new PropertyValueFactory<Test, Integer>("numBits"));
        comparison.setCellValueFactory(new PropertyValueFactory<Test, String>("comparison"));
        value.setCellValueFactory(new PropertyValueFactory<Test, Long>("value"));
        omission.setCellValueFactory(new PropertyValueFactory<Test, Integer>("omission"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Test, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Test, String> text) {
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

        register.setCellFactory(cellRegFactory);
        register.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Test, Register>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Test, Register> text) {
                        ((Test)text.getRowValue()).setRegister(
                                text.getNewValue());
                    }
                }
        );

        start.setCellFactory(cellIntFactory);
        start.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Test, Integer>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Test, Integer> text) {
                        ((Test)text.getRowValue()).setStart(
                                text.getNewValue());
                    }
                }
        );

        numBits.setCellFactory(cellIntFactory);
        numBits.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Test, Integer>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Test, Integer> text) {
                        ((Test)text.getRowValue()).setNumBits(
                                text.getNewValue());
                    }
                }
        );

        comparison.setCellFactory(cellCompFactory);
        comparison.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Test, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Test, String> text) {
                        ((Test)text.getRowValue()).setComparison(
                                text.getNewValue());
                    }
                }
        );

        value.setCellFactory(cellLongFactory);
        value.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Test, Long>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Test, Long> text) {
                        ((Test)text.getRowValue()).setValue(
                                text.getNewValue());
                    }
                }
        );

        omission.setCellFactory(cellIntFactory);
        omission.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Test, Integer>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Test, Integer> text) {
                        ((Test)text.getRowValue()).setOmission(
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
        return Test.class;
    }

    /**
     * getter for the current Test Microinstructions.
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
        return "Test";
    }

    /**
     * gets properties
     * @return an array of String representations of the
     * various properties of this type of microinstruction
     */
//    public String[] getProperties()
//    {
//        return new String[]{"name", "register", "start", "numBits",
//                "comparison", "value", "omission"};
//    }

    /**
     * use clones to replace existing Microinstructions
     * in the machine, and update the machine to delete
     * all references to the deleted Microinstructions.
     */
    public void updateCurrentMicrosFromClones()
    {
        machine.setMicros("test", createNewMicroList(clones));
    }

    /**
     * Set the clones to the new array passed as a parameter.
     * Does not check for validity.
     *
     * @param newClones Object array containing new set of clones
     */
    public void setClones(ObservableList newClones)
    {
        Test[] tests = new Test[newClones.size()];
        for (int i = 0; i < newClones.size(); i++) {
            tests[i] = (Test) newClones.get(i);
        }
        clones = tests;
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
        Test[] tests = new Test[micros.size()];
        for (int i = 0; i < micros.size(); i++) {
            tests[i] = (Test) micros.get(i);
        }

        // check that all names are unique and nonempty
        Validate.rangeInBound(tests);

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
        return "Test";
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
