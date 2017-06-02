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
 * 3.) Moved valueFitsInNumBitsForSetMicros and rangeInBound method to the Validate class and changed the return value to void
 * from boolean
 *
 * on 12/2/2013
 *
 * 1.) Changed the declaration of cellComboFactory in initialize so that it takes all registers in including
 * the registers in register arrays.
 */
package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.model.Microinstruction;
import cpusim.gui.util.EditingNonNegativeIntCell;
import cpusim.gui.util.EditingLongCell;
import cpusim.gui.util.EditingStrCell;
import cpusim.gui.util.FXMLLoaderFactory;
import cpusim.model.microinstruction.CpusimSet;
import cpusim.model.module.Register;
import cpusim.util.Validate;
import cpusim.util.ValidationException;
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
import java.util.List;
import java.util.ResourceBundle;

/**
 * The controller for editing the Set command in the EditMicroDialog.
 */
public class SetTableController
        extends MicroController implements Initializable  {
    @FXML TableView<CpusimSet> table;
    @FXML TableColumn<CpusimSet,String> name;
    @FXML TableColumn<CpusimSet,Register> register;
    @FXML TableColumn<CpusimSet,Integer> start;
    @FXML TableColumn<CpusimSet,Integer> numBits;
    @FXML TableColumn<CpusimSet,Long> value;

    private ObservableList currentMicros;
    private CpusimSet prototype;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    public SetTableController(Mediator mediator){
        super(mediator);
        this.mediator = mediator;
        this.machine = this.mediator.getMachine();
        this.currentMicros = machine.getMicros("set");
        Register r = (machine.getAllRegisters().size() == 0 ? null :
                (Register) machine.getAllRegisters().get(0));
        this.prototype = new CpusimSet("???", machine, r, 0, 1, 0L);
        clones = (Microinstruction[]) createClones();


        FXMLLoader fxmlLoader = FXMLLoaderFactory.fromRootController(this, "SetTable.fxml");

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            // should never happen
            assert false : "Unable to load file: SetTable.fxml";
        }

        List<CpusimSet> items = table.getItems();
        for (Microinstruction ins: clones) {
            items.add((CpusimSet)ins);
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
        name.prefWidthProperty().bind(table.prefWidthProperty().divide(100/20));
        register.prefWidthProperty().bind(table.prefWidthProperty().divide(100/20.0));
        start.prefWidthProperty().bind(table.prefWidthProperty().divide(100/20.0));
        numBits.prefWidthProperty().bind(table.prefWidthProperty().divide(100/20.0));
        value.prefWidthProperty().bind(table.prefWidthProperty().divide(100/20.0));

        Callback<TableColumn<CpusimSet,String>,TableCell<CpusimSet,String>> cellStrFactory =
                new Callback<TableColumn<CpusimSet, String>, TableCell<CpusimSet, String>>() {
                    @Override
                    public TableCell<CpusimSet, String> call(
                            TableColumn<CpusimSet, String> setStringTableColumn) {
                        return new EditingStrCell<CpusimSet>();
                    }
                };
        Callback<TableColumn<CpusimSet,Integer>,TableCell<CpusimSet,Integer>> cellIntFactory =
                new Callback<TableColumn<CpusimSet, Integer>, TableCell<CpusimSet, Integer>>() {
                    @Override
                    public TableCell<CpusimSet, Integer> call(
                            TableColumn<CpusimSet, Integer> setIntegerTableColumn) {
                        return new EditingNonNegativeIntCell<CpusimSet>();
                    }
                };
        Callback<TableColumn<CpusimSet,Long>,TableCell<CpusimSet,Long>> cellLongFactory =
                new Callback<TableColumn<CpusimSet, Long>, TableCell<CpusimSet, Long>>() {
                    @Override
                    public TableCell<CpusimSet, Long> call(
                            TableColumn<CpusimSet, Long> setIntegerTableColumn) {
                        return new EditingLongCell<CpusimSet>();
                    }
                };
        Callback<TableColumn<CpusimSet,Register>,TableCell<CpusimSet,Register>> cellComboFactory =
                new Callback<TableColumn<CpusimSet, Register>, TableCell<CpusimSet, Register>>() {
                    @Override
                    public TableCell<CpusimSet, Register> call(
                            TableColumn<CpusimSet, Register> setStringTableColumn) {
                        return new ComboBoxTableCell<CpusimSet,Register>(
                                machine.getAllRegisters());
                    }
                };

        name.setCellValueFactory(new PropertyValueFactory<CpusimSet, String>("name"));
        register.setCellValueFactory(new PropertyValueFactory<CpusimSet, Register>("register"));
        start.setCellValueFactory(new PropertyValueFactory<CpusimSet, Integer>("start"));
        numBits.setCellValueFactory(new PropertyValueFactory<CpusimSet, Integer>("numBits"));
        value.setCellValueFactory(new PropertyValueFactory<CpusimSet, Long>("value"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<CpusimSet, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<CpusimSet, String> text) {
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

        register.setCellFactory(cellComboFactory);
        register.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<CpusimSet, Register>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<CpusimSet, Register> text) {
                        ((CpusimSet)text.getRowValue()).setRegister(
                                text.getNewValue());
                    }
                }
        );

        start.setCellFactory(cellIntFactory);
        start.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<CpusimSet, Integer>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<CpusimSet, Integer> text) {
                        ((CpusimSet)text.getRowValue()).setStart(
                                text.getNewValue());
                    }
                }
        );

        numBits.setCellFactory(cellIntFactory);
        numBits.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<CpusimSet, Integer>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<CpusimSet, Integer> text) {
                        ((CpusimSet)text.getRowValue()).setNumBits(
                                text.getNewValue());
                    }
                }
        );

        value.setCellFactory(cellLongFactory);
        value.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<CpusimSet, Long>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<CpusimSet, Long> text) {
                        ((CpusimSet)text.getRowValue()).setValue(
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
        return CpusimSet.class;
    }

    /**
     * getter for the current Set Microinstructions.
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
        return "Set";
    }

    /**
     * gets properties
     * @return an array of String representations of the
     * various properties of this type of microinstruction
     */
//    public String[] getProperties()
//    {
//        return new String[]{"name", "register", "start", "numBits", "value"};
//    }

    /**
     * use clones to replace existing Microinstructions
     * in the machine, and update the machine to delete
     * all references to the deleted Microinstructions.
     */
    public void updateCurrentMicrosFromClones()
    {
        machine.setMicros("set", createNewMicroList(clones));
    }

    /**
     * Set the clones to the new array passed as a parameter.
     * Does not check for validity.
     *
     * @param newClones Object array containing new set of clones
     */
    public void setClones(ObservableList newClones)
    {
        CpusimSet[] sets = new CpusimSet[newClones.size()];
        for (int i = 0; i < newClones.size(); i++) {
            sets[i] = (CpusimSet) newClones.get(i);
        }
        clones = sets;
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
        CpusimSet[] sets = new CpusimSet[micros.size()];
        for (int i = 0; i < micros.size(); i++) {
            sets[i] = (CpusimSet) micros.get(i);
        }

        // check that all names are unique and nonempty
        Validate.rangeInBound(sets);
        Validate.valueFitsInNumBitsForSetMicros(sets);
    }



    /**
     * returns true if new micros of this class can be created.
     * @return true if new micros of this class can be created.
     */
    public boolean newMicrosAreAllowed()
    {
        return (machine.getModule("registers").size() > 0);
    }

    /**
     * get the ID of the corresponding help page
     * @return the ID of the page
     */
    public String getHelpPageID()
    {
        return "Set";
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
