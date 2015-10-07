/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Changed the return value of checkValidity from a boolean to void (the functionality
 * enabled by that boolean value is now controlled by throwing ValidationException)
 * 2.) Changed the edit commit method on the name column so that it calls Validate.nameableObjects()
 * which throws a ValidationException in lieu of returning a boolean value
 *
 * on 12/2/2013:
 *
 * 1.) Changed the declaration of cellRegFactory in initialize so that it takes all registers in including
 * the registers in register arrays.
 */

package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.Microinstruction;
import cpusim.gui.util.FXMLLoaderFactory;
import cpusim.microinstruction.Arithmetic;
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
 * The controller for editing the arithmetic command in the EditMicroDialog.
 */
public class ArithmeticTableController
        extends MicroController
        implements Initializable, CPUSimConstants {
    @FXML TableView<Arithmetic> table;
    @FXML TableColumn<Arithmetic,String> name;
    @FXML TableColumn<Arithmetic,Register> source1;
    @FXML TableColumn<Arithmetic,Register> source2;
    @FXML TableColumn<Arithmetic,Register> destination;
    @FXML TableColumn<Arithmetic,String> type;
    @FXML TableColumn<Arithmetic,ConditionBit> overflowBit;
    @FXML TableColumn<Arithmetic,ConditionBit> carryBit;

    private ObservableList currentMicros;
    private Arithmetic prototype;

    /**
     * Constructor
     * @param mediator stores the information that will be shown in the tables
     */
    public ArithmeticTableController(Mediator mediator){
        super(mediator);
        this.mediator = mediator;
        this.machine = this.mediator.getMachine();
        this.currentMicros = machine.getMicros("arithmetic");
        Register r = (machine.getAllRegisters().size() == 0 ? null :
                (Register) machine.getAllRegisters().get(0));
        this.prototype = new Arithmetic("???", machine, "ADD", r, r, r,
                NO_CONDITIONBIT, NO_CONDITIONBIT);
        clones = (Microinstruction[]) createClones();

        FXMLLoader fxmlLoader = FXMLLoaderFactory.fromRootController(this, "arithmeticTable.fxml");

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            // should never happen
            assert false : "Unable to load file: arithmeticTable.fxml";
        }

        for (int i = 0; i < clones.length; i++){
            table.getItems().add((Arithmetic)clones[i]);
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
        name.prefWidthProperty().bind(table.prefWidthProperty().divide(100/16));
        source1.prefWidthProperty().bind(table.prefWidthProperty().divide(100/14.0));
        source2.prefWidthProperty().bind(table.prefWidthProperty().divide(100/14.0));
        destination.prefWidthProperty().bind(table.prefWidthProperty().divide(100/14.0));
        type.prefWidthProperty().bind(table.prefWidthProperty().divide(100/14.0));
        overflowBit.prefWidthProperty().bind(table.prefWidthProperty().divide(100/14.0));
        carryBit.prefWidthProperty().bind(table.prefWidthProperty().divide(100/14.0));

        Callback<TableColumn<Arithmetic,String>,TableCell<Arithmetic,String>> cellStrFactory =
                new Callback<TableColumn<Arithmetic, String>, TableCell<Arithmetic, String>>() {
                    @Override
                    public TableCell<Arithmetic, String> call(
                            TableColumn<Arithmetic, String> setStringTableColumn) {
                        return new cpusim.gui.util.EditingStrCell<Arithmetic>();
                    }
                };
        Callback<TableColumn<Arithmetic,String>,TableCell<Arithmetic,String>> cellTypeFactory =
                new Callback<TableColumn<Arithmetic, String>, TableCell<Arithmetic, String>>() {
                    @Override
                    public TableCell<Arithmetic, String> call(
                            TableColumn<Arithmetic, String> setStringTableColumn) {
                        return new ComboBoxTableCell<Arithmetic,String>(
                                FXCollections.observableArrayList(
                                        "ADD",
                                        "SUBTRACT",
                                        "MULTIPLY",
                                        "DIVIDE"
                                )
                        );
                    }
                };


        Callback<TableColumn<Arithmetic,Register>,TableCell<Arithmetic,Register>> cellRegFactory =
                new Callback<TableColumn<Arithmetic, Register>, TableCell<Arithmetic, Register>>() {
                    @Override
                    public TableCell<Arithmetic, Register> call(
                            TableColumn<Arithmetic, Register> setStringTableColumn) {
                        return new ComboBoxTableCell<Arithmetic,Register>(
                                machine.getAllRegisters());
                    }
                };

        final ObservableList condBit = FXCollections.observableArrayList(NO_CONDITIONBIT);
        condBit.addAll((ObservableList<ConditionBit>)machine.getModule("conditionBits"));
        Callback<TableColumn<Arithmetic,ConditionBit>,TableCell<Arithmetic,ConditionBit>> cellCondFactory =
                new Callback<TableColumn<Arithmetic, ConditionBit>, TableCell<Arithmetic, ConditionBit>>() {
                    @Override
                    public TableCell<Arithmetic, ConditionBit> call(
                            TableColumn<Arithmetic, ConditionBit> setStringTableColumn) {
                        return new ComboBoxTableCell<Arithmetic,ConditionBit>(
                                condBit
                        );
                    }
                };

        name.setCellValueFactory(new PropertyValueFactory<Arithmetic, String>("name"));
        type.setCellValueFactory(new PropertyValueFactory<Arithmetic, String>("type"));
        source1.setCellValueFactory(new PropertyValueFactory<Arithmetic, Register>("source1"));
        source2.setCellValueFactory(new PropertyValueFactory<Arithmetic, Register>("source2"));
        destination.setCellValueFactory(new PropertyValueFactory<Arithmetic, Register>("destination"));
        overflowBit.setCellValueFactory(new PropertyValueFactory<Arithmetic, ConditionBit>("overflowBit"));
        carryBit.setCellValueFactory(new PropertyValueFactory<Arithmetic, ConditionBit>("carryBit"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Arithmetic, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Arithmetic, String> text) {
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
                new EventHandler<TableColumn.CellEditEvent<Arithmetic, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Arithmetic, String> text) {
                        ((Arithmetic)text.getRowValue()).setType(
                                text.getNewValue());
                    }
                }
        );

        source1.setCellFactory(cellRegFactory);
        source1.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Arithmetic, Register>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Arithmetic, Register> text) {
                        ((Arithmetic)text.getRowValue()).setSource1(
                                text.getNewValue());
                    }
                }
        );

        source2.setCellFactory(cellRegFactory);
        source2.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Arithmetic, Register>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Arithmetic, Register> text) {
                        ((Arithmetic)text.getRowValue()).setSource2(
                                text.getNewValue());
                    }
                }
        );

        destination.setCellFactory(cellRegFactory);
        destination.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Arithmetic, Register>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Arithmetic, Register> text) {
                        ((Arithmetic)text.getRowValue()).setDestination(
                                text.getNewValue());
                    }
                }
        );

        overflowBit.setCellFactory(cellCondFactory);
        overflowBit.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Arithmetic, ConditionBit>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Arithmetic, ConditionBit> text) {
                        ((Arithmetic)text.getRowValue()).setOverflowBit(
                                text.getNewValue());
                    }
                }
        );

        carryBit.setCellFactory(cellCondFactory);
        carryBit.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Arithmetic, ConditionBit>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Arithmetic, ConditionBit> text) {
                        ((Arithmetic)text.getRowValue()).setCarryBit(
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
        return Arithmetic.class;
    }


    /**
     * getter for the current Arithmetic Microinstructions.
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
        return "Arithmetic";
    }

    /**
     * gets properties
     * @return an array of String representations of the
     * various properties of this type of microinstruction
     */
//    public String[] getProperties()
//    {
//        return new String[]{"name", "type", "source1", "source2", "destination",
//                "overflowBit", "carryBit"};
//    }

    /**
     * use clones to replace existing Microinstructions
     * in the machine, and update the machine to delete
     * all references to the deleted Microinstructions.
     */
    public void updateCurrentMicrosFromClones()
    {
        machine.setMicros("arithmetic", createNewMicroList(clones));
    }

    /**
     * Set the clones to the new array passed as a parameter.
     * Does not check for validity.
     *
     * @param newClones Object array containing new set of clones
     */
    public void setClones(ObservableList newClones)
    {
        Arithmetic[] arithmetics = new Arithmetic[newClones.size()];
        for (int i = 0; i < newClones.size(); i++) {
            arithmetics[i] = (Arithmetic) newClones.get(i);
        }
        clones = arithmetics;
    }

    /**
     * Check validity of array of Objects' properties.
     *
     * @param micros an array of Objects to check.
     * @return boolean denoting whether array has objects with
     * valid properties or not
     */
    public void checkValidity(ObservableList micros) {}


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
        return "Arithmetic";
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
