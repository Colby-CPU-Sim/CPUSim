/**
 * author: Jinghui Yu
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
 *
 * on 12/2/13:
 *
 * 1.) Changed the checkValidity to check if the SetCondBit micro writes to a register that is read-only
 */
package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.model.Microinstruction;
import cpusim.gui.util.FXMLLoaderFactory;
import cpusim.model.microinstruction.SetCondBit;
import cpusim.model.module.ConditionBit;
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
 * The controller for editing the Branch command in the EditMicroDialog.
 */
public class SetCondBitTableController
        extends MicroController implements Initializable {
    @FXML TableView<SetCondBit> table;
    @FXML TableColumn<SetCondBit,String> name;
    @FXML TableColumn<SetCondBit,ConditionBit> bit;
    @FXML TableColumn<SetCondBit,String> value;

    private ObservableList currentMicros;
    private SetCondBit prototype;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    public SetCondBitTableController(Mediator mediator){
        super(mediator);
        this.mediator = mediator;
        this.machine = this.mediator.getMachine();
        this.currentMicros = machine.getMicros("setCondBit");
        ConditionBit cBit = (machine.getModule("conditionBits").size() == 0 ?
                null : (ConditionBit) machine.getModule("conditionBits").get(0));
        this.prototype = new SetCondBit("???", machine, cBit, "0");
        clones = (Microinstruction[]) createClones();

        FXMLLoader fxmlLoader = FXMLLoaderFactory.fromRootController(this, "SetCondBitTable.fxml");

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            // should never happen
            assert false : "Unable to load file: SetCondBitTable.fxml";
        }

        for (int i = 0; i < clones.length; i++){
            table.getItems().add((SetCondBit)clones[i]);
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
        name.prefWidthProperty().bind(table.prefWidthProperty().divide(100/34.0));
        bit.prefWidthProperty().bind(table.prefWidthProperty().divide(100/33.0));
        value.prefWidthProperty().bind(table.prefWidthProperty().divide(100/33.0));

        Callback<TableColumn<SetCondBit,String>,TableCell<SetCondBit,String>> cellStrFactory =
                new Callback<TableColumn<SetCondBit, String>, TableCell<SetCondBit, String>>() {
                    @Override
                    public TableCell<SetCondBit, String> call(
                            TableColumn<SetCondBit, String> setStringTableColumn) {
                        return new cpusim.gui.util.EditingStrCell<SetCondBit>();
                    }
                };
        Callback<TableColumn<SetCondBit,String>,TableCell<SetCondBit,String>> cellIntFactory =
                new Callback<TableColumn<SetCondBit, String>, TableCell<SetCondBit, String>>() {
                    @Override
                    public TableCell<SetCondBit, String> call(
                            TableColumn<SetCondBit, String> setStringTableColumn) {
                        return new ComboBoxTableCell<SetCondBit,String>(
                                FXCollections.observableArrayList(
                                        "0",
                                        "1"
                                ));
                    }
                };
        Callback<TableColumn<SetCondBit,ConditionBit>,TableCell<SetCondBit,ConditionBit>> cellCondFactory =
                new Callback<TableColumn<SetCondBit, ConditionBit>, TableCell<SetCondBit, ConditionBit>>() {
                    @Override
                    public TableCell<SetCondBit, ConditionBit> call(
                            TableColumn<SetCondBit, ConditionBit> setStringTableColumn) {
                        return new ComboBoxTableCell<SetCondBit,ConditionBit>(
                                (ObservableList<ConditionBit>)machine.getModule("conditionBits")
                        );
                    }
                };

        name.setCellValueFactory(new PropertyValueFactory<SetCondBit, String>("name"));
        bit.setCellValueFactory(new PropertyValueFactory<SetCondBit, ConditionBit>("bit"));
        value.setCellValueFactory(new PropertyValueFactory<SetCondBit, String>("value"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<SetCondBit, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<SetCondBit, String> text) {
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

        bit.setCellFactory(cellCondFactory);
        bit.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<SetCondBit, ConditionBit>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<SetCondBit, ConditionBit> text) {
                        ((SetCondBit)text.getRowValue()).setBit(
                                text.getNewValue());
                    }
                }
        );

        value.setCellFactory(cellIntFactory);
        value.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<SetCondBit, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<SetCondBit, String> text) {
                        ((SetCondBit)text.getRowValue()).setValue(
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
        return SetCondBit.class;
    }

    /**
     * getter for the current SetCondBit Microinstructions.
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
        return "SetCondBit";
    }

    /**
     * gets properties
     * @return an array of String representations of the
     * various properties of this type of microinstruction
     */
//    public String[] getProperties()
//    {
//        return new String[]{"name", "bit", "value"};
//    }

    /**
     * use clones to replace existing Microinstructions
     * in the machine, and update the machine to delete
     * all references to the deleted Microinstructions.
     */
    public void updateCurrentMicrosFromClones()
    {
        machine.setMicros("setCondBit", createNewMicroList(clones));
    }

    /**
     * Set the clones to the new array passed as a parameter.
     * Does not check for validity.
     *
     * @param newClones Object array containing new set of clones
     */
    public void setClones(ObservableList newClones)
    {
        SetCondBit[] setCondBits = new SetCondBit[newClones.size()];
        for (int i = 0; i < newClones.size(); i++) {
            setCondBits[i] = (SetCondBit) newClones.get(i);
        }
        clones = setCondBits;
    }

    /**
     * Check validity of array of Objects' properties.
     * @param micros an array of Objects to check.
     * @return boolean denoting whether array has objects with
     * valid properties or not
     */
    public void checkValidity(ObservableList micros)
    {
        // convert the array to an array of TransferRtoRs
        SetCondBit[] setCondBits = new SetCondBit[micros.size()];

        for (int i = 0; i < micros.size(); i++) {
            setCondBits[i] = (SetCondBit) micros.get(i);

            Validate.registerIsNotReadOnly(
                    ((SetCondBit)micros.get(i)).getBit().getRegister(),
                    ((SetCondBit) micros.get(i)).getName());
        }

    }

    /**
     * returns true if new micros of this class can be created.
     */
    public boolean newMicrosAreAllowed()
    {
        return (machine.getModule("conditionBits").size() > 0);
    }

    /**
     * get the ID of the corresponding help page
     * @return the ID of the page
     */
    public String getHelpPageID()
    {
        return "SetCondBit";
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
