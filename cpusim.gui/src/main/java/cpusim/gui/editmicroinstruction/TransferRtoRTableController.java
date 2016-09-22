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
 * 3.) Moved rangesInBound method to the Validate class and changed the return value to void
 * from boolean
 *
 * Jinghui Yu, Ben Borchard and Michael Goldenberg modified this file on 11/11/13
 * with the following changes:
 *
 * 1.) Changed checkValidity method so that it calls Validate.registerIsNotReadOnly to check
 * if any destination register is read-only
 */
package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.model.Microinstruction;
import cpusim.gui.util.EditingNonNegativeIntCell;
import cpusim.gui.util.FXMLLoaderFactory;
import cpusim.model.microinstruction.TransferRtoR;
import cpusim.model.module.Register;
import cpusim.model.util.Validate;
import cpusim.model.util.ValidationException;

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
 * The controller for editing the TranferRtoR command in the EditMicroDialog.
 */
public class TransferRtoRTableController
        extends MicroController implements Initializable {
    @FXML TableView<TransferRtoR> table;
    @FXML TableColumn<TransferRtoR,String> name;
    @FXML TableColumn<TransferRtoR,Register> source;
    @FXML TableColumn<TransferRtoR,Integer> srcStartBit;
    @FXML TableColumn<TransferRtoR,Register> dest;
    @FXML TableColumn<TransferRtoR,Integer> destStartBit;
    @FXML TableColumn<TransferRtoR,Integer> numBits;

    private ObservableList currentMicros;
    private TransferRtoR prototype;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    public TransferRtoRTableController(Mediator mediator){
        super(mediator);
        this.mediator = mediator;
        this.machine = this.mediator.getMachine();
        this.currentMicros = machine.getMicros("transferRtoR");
        Register r = (machine.getAllRegisters().size() == 0 ? null :
                (Register) machine.getAllRegisters().get(0));
        this.prototype = new TransferRtoR("???", machine, r, 0, r, 0, 0);
        clones = (Microinstruction[]) createClones();

        FXMLLoader fxmlLoader = FXMLLoaderFactory.fromRootController(this, "TransferRtoRTable.fxml");

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            // should never happen
            assert false : "Unable to load file: TransferRtoRTable.fxml";
        }

        for (int i = 0; i < clones.length; i++){
            table.getItems().add((TransferRtoR)clones[i]);
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
        srcStartBit.prefWidthProperty().bind(table.prefWidthProperty().divide(100/17.0));
        dest.prefWidthProperty().bind(table.prefWidthProperty().divide(100/17.0));
        destStartBit.prefWidthProperty().bind(table.prefWidthProperty().divide(100/16.0));
        numBits.prefWidthProperty().bind(table.prefWidthProperty().divide(100/16.0));

        Callback<TableColumn<TransferRtoR, String>,
                TableCell<TransferRtoR, String>> cellStrFactory =
                new Callback<TableColumn<TransferRtoR, String>,
                        TableCell<TransferRtoR, String>>() {
                    @Override
                    public TableCell call(
                            TableColumn setStringTableColumn) {
                        return new cpusim.gui.util.EditingStrCell<TransferRtoR>();
                    }
                };
        Callback<TableColumn<TransferRtoR,Integer>,
                TableCell<TransferRtoR,Integer>> cellIntFactory =
                new Callback<TableColumn<TransferRtoR,Integer>,
                        TableCell<TransferRtoR, Integer>>() {
                    @Override
                    public TableCell call(
                            TableColumn<TransferRtoR, Integer> setIntegerTableColumn) {
                        return new EditingNonNegativeIntCell<TransferRtoR>();
                    }
                };
        Callback<TableColumn<TransferRtoR,Register>,
                TableCell<TransferRtoR,Register>> cellRegFactory =
                new Callback<TableColumn<TransferRtoR, Register>,
                        TableCell<TransferRtoR, Register>>() {
                    @Override
                    public TableCell<TransferRtoR,Register> call(
                            TableColumn<TransferRtoR, Register> setStringTableColumn) {
                        return new ComboBoxTableCell<TransferRtoR,Register>(
                                machine.getAllRegisters());
                    }
                };

        name.setCellValueFactory(
                new PropertyValueFactory<TransferRtoR, String>("name"));
        source.setCellValueFactory(
                new PropertyValueFactory<TransferRtoR, Register>("source"));
        srcStartBit.setCellValueFactory(
                new PropertyValueFactory<TransferRtoR, Integer>("srcStartBit"));
        dest.setCellValueFactory(
                new PropertyValueFactory<TransferRtoR, Register>("dest"));
        destStartBit.setCellValueFactory(
                new PropertyValueFactory<TransferRtoR, Integer>("destStartBit"));
        numBits.setCellValueFactory(
                new PropertyValueFactory<TransferRtoR, Integer>("numBits"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<TransferRtoR, String>>() {
                    @Override
                    public void handle(
                            TableColumn.CellEditEvent<TransferRtoR, String> text) {
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

        source.setCellFactory(cellRegFactory);
        source.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<TransferRtoR, Register>>() {
                    @Override
                    public void handle(
                            TableColumn.CellEditEvent<TransferRtoR, Register> text) {
                        ((TransferRtoR)text.getRowValue()).setSource(
                                text.getNewValue());
                    }
                }
        );

        srcStartBit.setCellFactory(cellIntFactory);
        srcStartBit.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<TransferRtoR, Integer>>() {
                    @Override
                    public void handle(
                            TableColumn.CellEditEvent<TransferRtoR, Integer> text) {
                        ((TransferRtoR)text.getRowValue()).setSrcStartBit(
                                text.getNewValue());
                    }
                }
        );

        dest.setCellFactory(cellRegFactory);
        dest.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<TransferRtoR, Register>>() {
                    @Override
                    public void handle(
                            TableColumn.CellEditEvent<TransferRtoR, Register> text) {
                        ((TransferRtoR)text.getRowValue()).setDest(
                                text.getNewValue());
                    }
                }
        );

        destStartBit.setCellFactory(cellIntFactory);
        destStartBit.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<TransferRtoR, Integer>>() {
                    @Override
                    public void handle(
                            TableColumn.CellEditEvent<TransferRtoR, Integer> text) {
                        ((TransferRtoR)text.getRowValue()).setDestStartBit(
                                text.getNewValue());
                    }
                }
        );

        numBits.setCellFactory(cellIntFactory);
        numBits.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<TransferRtoR, Integer>>() {
                    @Override
                    public void handle(
                            TableColumn.CellEditEvent<TransferRtoR, Integer> text) {
                        ((TransferRtoR)text.getRowValue()).setNumBits(
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
        return TransferRtoR.class;
    }

    /**
     * getter for the current TranferRtoR Microinstructions.
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
        return "TransferRtoR";
    }

    /**
     * gets properties
     * @return an array of String representations of the
     * various properties of this type of microinstruction
     */
//    public String[] getProperties()
//    {
//        return new String[]{"name", "source", "srcStartBit", "dest", "destStartBit",
//                "numBits"};
//    }

    /**
     * use clones to replace existing Microinstructions
     * in the machine, and update the machine to delete
     * all references to the deleted Microinstructions.
     */
    public void updateCurrentMicrosFromClones()
    {
        machine.setMicros("transferRtoR", createNewMicroList(clones));
    }

    /**
     * Set the clones to the new array passed as a parameter.
     * Does not check for validity.
     *
     * @param newClones Object array containing new set of clones
     */
    public void setClones(ObservableList newClones)
    {
        TransferRtoR[] transferRtoRs = new TransferRtoR[newClones.size()];
        for (int i = 0; i < newClones.size(); i++) {
            transferRtoRs[i] = (TransferRtoR) newClones.get(i);
        }
        clones = transferRtoRs;
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
        TransferRtoR[] transferRtoRs = new TransferRtoR[micros.size()];

        for (int i = 0; i < micros.size(); i++) {
            transferRtoRs[i] = (TransferRtoR) micros.get(i);

            Validate.registerIsNotReadOnly(
                    ((TransferRtoR)micros.get(i)).getDest(),
                    ((TransferRtoR) micros.get(i)).getName());
        }

        // check that all names are unique and nonempty
        Validate.rangesAreInBound(transferRtoRs);

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
        return "Transfer";
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
