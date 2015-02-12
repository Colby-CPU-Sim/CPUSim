/**
 * auther: Jinghui Yu
 * last edit date: 6/5/2013
 */

/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Changed the return value of checkValidity from a boolean to void (the functionality
 * enabled by that boolean value is now controlled by throwing ValidationException)
 * 2.) Changed the edit commit method on the name column so that it calls Validate.nameableObjects()
 * which throws a ValidationException in lieu of returning a boolean value
 */
package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.Microinstruction;
import cpusim.gui.util.EditingLongCell;
import cpusim.gui.util.EditingStrCell;
import cpusim.microinstruction.Increment;
import cpusim.module.ConditionBit;
import cpusim.module.Register;
import cpusim.util.CPUSimConstants;
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
 * The controller for editing the Increment command in the EditMicroDialog.
 */
public class IncrementTableController
        extends MicroController
        implements Initializable, CPUSimConstants {
    @FXML TableView<Increment> table;
    @FXML TableColumn<Increment,String> name;
    @FXML TableColumn<Increment,Register> register;
    @FXML TableColumn<Increment,ConditionBit> overflowBit;
    @FXML TableColumn<Increment,Long> delta;

    private ObservableList currentMicros;
    private Increment prototype;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    public IncrementTableController(Mediator mediator){
        super(mediator);
        this.mediator = mediator;
        this.machine = this.mediator.getMachine();
        this.currentMicros = machine.getMicros("increment");
        Register r = (machine.getAllRegisters().size() == 0 ? null :
                (Register) machine.getAllRegisters().get(0));
        this.prototype = new Increment("???", machine, r, NO_CONDITIONBIT, Long.valueOf(1));
        clones = (Microinstruction[]) createClones();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "incrementTable.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        for (int i = 0; i < clones.length; i++){
            table.getItems().add((Increment)clones[i]);
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
        name.prefWidthProperty().bind(table.widthProperty().divide(100/25.0));
        register.prefWidthProperty().bind(table.widthProperty().divide(100/25.0));
        overflowBit.prefWidthProperty().bind(table.widthProperty().divide(100/25.0));
        delta.prefWidthProperty().bind(table.widthProperty().divide(100 / 25.0));

        Callback<TableColumn<Increment,String>,TableCell<Increment,String>> cellStrFactory =
                new Callback<TableColumn<Increment, String>, TableCell<Increment, String>>() {
                    @Override
                    public TableCell<Increment, String> call(
                            TableColumn<Increment, String> setStringTableColumn) {
                        return new EditingStrCell<Increment>();
                    }
                };
        Callback<TableColumn<Increment,Long>,TableCell<Increment,Long>> cellLongFactory =
                new Callback<TableColumn<Increment,Long>, TableCell<Increment, Long>>() {
                    @Override
                    public TableCell call(
                            TableColumn<Increment, Long> setIntegerTableColumn) {
                        return new EditingLongCell<Increment>();
                    }
                };
        Callback<TableColumn<Increment,Register>,TableCell<Increment,Register>> cellRegFactory =
                new Callback<TableColumn<Increment, Register>, TableCell<Increment, Register>>() {
                    @Override
                    public TableCell<Increment, Register> call(
                            TableColumn<Increment, Register> setStringTableColumn) {
                        return new ComboBoxTableCell<Increment,Register>(
                                machine.getAllRegisters());
                    }
                };

        final ObservableList condBit = FXCollections.observableArrayList(NO_CONDITIONBIT);
        condBit.addAll((ObservableList<ConditionBit>)machine.getModule("conditionBits"));
        Callback<TableColumn<Increment,ConditionBit>,TableCell<Increment,ConditionBit>> cellCondFactory =
                new Callback<TableColumn<Increment, ConditionBit>, TableCell<Increment, ConditionBit>>() {
                    @Override
                    public TableCell<Increment, ConditionBit> call(
                            TableColumn<Increment, ConditionBit> setStringTableColumn) {
                        return new ComboBoxTableCell<Increment,ConditionBit>(
                                condBit
                        );
                    }
                };

        name.setCellValueFactory(new PropertyValueFactory<Increment, String>("name"));
        register.setCellValueFactory(new PropertyValueFactory<Increment, Register>("register"));
        overflowBit.setCellValueFactory(new PropertyValueFactory<Increment, ConditionBit>("overflowBit"));
        delta.setCellValueFactory(new PropertyValueFactory<Increment, Long>("delta"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Increment, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Increment, String> text) {
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
                new EventHandler<TableColumn.CellEditEvent<Increment, Register>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Increment, Register> text) {
                        ((Increment)text.getRowValue()).setRegister(
                                text.getNewValue()
                        );
                    }
                }
        );

        overflowBit.setCellFactory(cellCondFactory);
        overflowBit.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Increment, ConditionBit>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Increment, ConditionBit> text) {
                        ((Increment)text.getRowValue()).setOverflowBit(
                                text.getNewValue()
                        );
                    }
                }
        );

        delta.setCellFactory(cellLongFactory);
        delta.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Increment, Long>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Increment, Long> text) {
                        ((Increment)text.getRowValue()).setDelta(
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
        return Increment.class;
    }

    /**
     * getter for the current Increment Microinstructions.
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
        return "Increment";
    }

    /**
     * gets properties
     * @return an array of String representations of the
     * various properties of this type of microinstruction
     */
//    public String[] getProperties()
//    {
//        return new String[]{"name", "register", "overFlowBit", "delta"};
//    }

    /**
     * use clones to replace existing Microinstructions
     * in the machine, and update the machine to delete
     * all references to the deleted Microinstructions.
     */
    public void updateCurrentMicrosFromClones()
    {
        machine.setMicros("increment", createNewMicroList(clones));
    }

    /**
     * Set the clones to the new array passed as a parameter.
     * Does not check for validity.
     *
     * @param newClones Object array containing new set of clones
     */
    public void setClones(ObservableList newClones)
    {
        Increment[] increments = new Increment[newClones.size()];
        for (int i = 0; i < newClones.size(); i++) {
            increments[i] = (Increment) newClones.get(i);
        }
        clones = increments;
    }

    /**
     * Check validity of array of Objects' properties.
     * @param micros an array of Objects to check.
     * @return boolean denoting whether array has objects with
     * valid properties or not
     */
    public void checkValidity(ObservableList micros)
    {    }


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
        return "Increment";
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