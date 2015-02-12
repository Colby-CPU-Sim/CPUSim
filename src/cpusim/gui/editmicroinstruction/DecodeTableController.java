/**
 * Author: Jinghui Yu
 * LastEditingDate: 6/7/2013
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
import cpusim.microinstruction.Decode;
import cpusim.module.Register;
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
import java.util.ResourceBundle;

/**
 * The controller for editing the Branch command in the EditMicroDialog.
 */
public class DecodeTableController
        extends MicroController implements Initializable {
    @FXML TableView<Decode> table;
    @FXML TableColumn<Decode,String> name;
    @FXML TableColumn<Decode,Register> ir;

    private ObservableList currentMicros;
    private Decode prototype;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    public DecodeTableController(Mediator mediator){
        super(mediator);
        this.mediator = mediator;
        this.machine = this.mediator.getMachine();
        this.currentMicros = machine.getMicros("decode");
        Register r = (machine.getAllRegisters().size() == 0 ? null :
                (Register) machine.getAllRegisters().get(0));
        this.prototype = new Decode("???", machine, r);
        clones = (Microinstruction[]) createClones();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "decodeTable.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        for (int i = 0; i < clones.length; i++){
            table.getItems().add((Decode)clones[i]);
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
        name.prefWidthProperty().bind(table.prefWidthProperty().divide(100/50.0));
        ir.prefWidthProperty().bind(table.prefWidthProperty().divide(100/50.0));

        Callback<TableColumn<Decode,String>,TableCell<Decode,String>> cellStrFactory =
                new Callback<TableColumn<Decode, String>, TableCell<Decode, String>>() {
                    @Override
                    public TableCell<Decode, String> call(
                            TableColumn<Decode, String> setStringTableColumn) {
                        return new cpusim.gui.util.EditingStrCell<Decode>();
                    }
                };
        Callback<TableColumn<Decode,Register>,TableCell<Decode,Register>> cellRegFactory =
                new Callback<TableColumn<Decode, Register>, TableCell<Decode, Register>>() {
                    @Override
                    public TableCell<Decode, Register> call(
                            TableColumn<Decode, Register> setStringTableColumn) {
                        return new ComboBoxTableCell<Decode,Register>(
                                machine.getAllRegisters());
                    }
                };

        name.setCellValueFactory(new PropertyValueFactory<Decode, String>("name"));
        ir.setCellValueFactory(new PropertyValueFactory<Decode, Register>("ir"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Decode, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Decode, String> text) {
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

        ir.setCellFactory(cellRegFactory);
        ir.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Decode, Register>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Decode, Register> text) {
                        ((Decode)text.getRowValue()).setIr(
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
        return Decode.class;
    }

    /**
     * getter for the current Decode Microinstructions.
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
        return "Decode";
    }

    /**
     * gets properties
     * @return an array of String representations of the
     * various properties of this type of microinstruction
     */
//    public String[] getProperties()
//    {
//        return new String[]{"name", "ir"};
//    }

    /**
     * use clones to replace existing Microinstructions
     * in the machine, and update the machine to delete
     * all references to the deleted Microinstructions.
     */
    public void updateCurrentMicrosFromClones()
    {
        machine.setMicros("decode", createNewMicroList(clones));
    }

    /**
     * Set the clones to the new array passed as a parameter.
     * Does not check for validity.
     *
     * @param newClones Object array containing new set of clones
     */
    public void setClones(ObservableList newClones)
    {
        Decode[] decodes = new Decode[newClones.size()];
        for (int i = 0; i < newClones.size(); i++)
            decodes[i] = (Decode) newClones.get(i);
        clones = decodes;
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
        return "Decode";
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
