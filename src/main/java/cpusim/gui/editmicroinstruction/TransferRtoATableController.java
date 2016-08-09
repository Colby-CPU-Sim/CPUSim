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
 */
package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.Microinstruction;
import cpusim.gui.util.EditingNonNegativeIntCell;
import cpusim.gui.util.FXMLLoaderFactory;
import cpusim.microinstruction.TransferRtoA;
import cpusim.module.Register;
import cpusim.module.RegisterArray;
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
 * The controller for editing the TransferRtoA command in the EditMicroDialog.
 */
public class TransferRtoATableController
        extends MicroController implements Initializable {
    @FXML TableView<TransferRtoA> table;
    @FXML TableColumn<TransferRtoA,String> name;
    @FXML TableColumn<TransferRtoA,Register> source;
    @FXML TableColumn<TransferRtoA,Integer> srcStartBit;
    @FXML TableColumn<TransferRtoA,RegisterArray> dest;
    @FXML TableColumn<TransferRtoA,Integer> destStartBit;
    @FXML TableColumn<TransferRtoA,Integer> numBits;
    @FXML TableColumn<TransferRtoA,Register> index;
    @FXML TableColumn<TransferRtoA,Integer> indexStart;
    @FXML TableColumn<TransferRtoA,Integer> indexNumBits;

    private ObservableList currentMicros;
    private TransferRtoA prototype;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    public TransferRtoATableController(Mediator mediator){
        super(mediator);
        this.mediator = mediator;
        this.machine = this.mediator.getMachine();
        this.currentMicros = machine.getMicros("transferRtoA");
        RegisterArray a = (machine.getModule("registerArrays").size() == 0 ? null :
                (RegisterArray) machine.getModule("registerArrays").get(0));
        Register r = (machine.getAllRegisters().size() == 0 ? null :
                (Register) machine.getAllRegisters().get(0));
        this.prototype = new TransferRtoA("???", machine, r, 0, a, 0, 0, r,0, 0);
        clones = (Microinstruction[]) createClones();

        FXMLLoader fxmlLoader = FXMLLoaderFactory.fromRootController(this, "transferRtoATable.fxml");

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            // should never happen
            assert false : "Unable to load file: transferRtoATable.fxml";
        }

        for (int i = 0; i < clones.length; i++){
            table.getItems().add((TransferRtoA)clones[i]);
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
        name.prefWidthProperty().bind(table.prefWidthProperty().divide(100/12.0));
        source.prefWidthProperty().bind(table.prefWidthProperty().divide(100/11.0));
        srcStartBit.prefWidthProperty().bind(table.prefWidthProperty().divide(100/11.0));
        dest.prefWidthProperty().bind(table.prefWidthProperty().divide(100/11.0));
        destStartBit.prefWidthProperty().bind(table.prefWidthProperty().divide(100/11.0));
        numBits.prefWidthProperty().bind(table.prefWidthProperty().divide(100/11.0));
        index.prefWidthProperty().bind(table.prefWidthProperty().divide(100/11.0));
        indexStart.prefWidthProperty().bind(table.prefWidthProperty().divide(100/11.0));
        indexNumBits.prefWidthProperty().bind(table.prefWidthProperty().divide(100/11.0));

        Callback<TableColumn<TransferRtoA, String>,
                TableCell<TransferRtoA, String>> cellStrFactory =
                new Callback<TableColumn<TransferRtoA, String>,
                        TableCell<TransferRtoA, String>>() {
                    @Override
                    public TableCell call(
                            TableColumn setStringTableColumn) {
                        return new cpusim.gui.util.EditingStrCell<TransferRtoA>();
                    }
                };
        Callback<TableColumn<TransferRtoA,Integer>,
                TableCell<TransferRtoA,Integer>> cellIntFactory =
                new Callback<TableColumn<TransferRtoA,Integer>,
                        TableCell<TransferRtoA, Integer>>() {
                    @Override
                    public TableCell call(
                            TableColumn<TransferRtoA, Integer> setIntegerTableColumn) {
                        return new EditingNonNegativeIntCell<TransferRtoA>();
                    }
                };
        Callback<TableColumn<TransferRtoA,Register>,
                TableCell<TransferRtoA,Register>> cellRegFactory =
                new Callback<TableColumn<TransferRtoA, Register>,
                        TableCell<TransferRtoA, Register>>() {
                    @Override
                    public TableCell<TransferRtoA,Register> call(
                            TableColumn<TransferRtoA, Register> setStringTableColumn) {
                        return new ComboBoxTableCell<TransferRtoA,Register>(
                                machine.getAllRegisters()
                        );
                    }
                };
        Callback<TableColumn<TransferRtoA,RegisterArray>,
                TableCell<TransferRtoA,RegisterArray>> cellRegAFactory =
                new Callback<TableColumn<TransferRtoA, RegisterArray>,
                        TableCell<TransferRtoA, RegisterArray>>() {
                    @Override
                    public TableCell<TransferRtoA,RegisterArray> call(
                            TableColumn<TransferRtoA, RegisterArray> setStringTableColumn) {
                        return new ComboBoxTableCell<TransferRtoA,RegisterArray>(
                                (ObservableList<RegisterArray>)machine.getModule("registerArrays")
                        );
                    }
                };

        name.setCellValueFactory(
                new PropertyValueFactory<TransferRtoA, String>("name"));
        source.setCellValueFactory(
                new PropertyValueFactory<TransferRtoA, Register>("source"));
        srcStartBit.setCellValueFactory(
                new PropertyValueFactory<TransferRtoA, Integer>("srcStartBit"));
        dest.setCellValueFactory(
                new PropertyValueFactory<TransferRtoA, RegisterArray>("dest"));
        destStartBit.setCellValueFactory(
                new PropertyValueFactory<TransferRtoA, Integer>("destStartBit"));
        numBits.setCellValueFactory(
                new PropertyValueFactory<TransferRtoA, Integer>("numBits"));
        index.setCellValueFactory(
                new PropertyValueFactory<TransferRtoA, Register>("index"));
        indexStart.setCellValueFactory(
                new PropertyValueFactory<TransferRtoA, Integer>("indexStart"));
        indexNumBits.setCellValueFactory(
                new PropertyValueFactory<TransferRtoA, Integer>("indexNumBits"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<TransferRtoA, String>>() {
                    @Override
                    public void handle(
                            TableColumn.CellEditEvent<TransferRtoA, String> text) {
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
                new EventHandler<TableColumn.CellEditEvent<TransferRtoA, Register>>() {
                    @Override
                    public void handle(
                            TableColumn.CellEditEvent<TransferRtoA, Register> text) {
                        ((TransferRtoA)text.getRowValue()).setSource(
                                text.getNewValue());
                    }
                }
        );

        srcStartBit.setCellFactory(cellIntFactory);
        srcStartBit.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<TransferRtoA, Integer>>() {
                    @Override
                    public void handle(
                            TableColumn.CellEditEvent<TransferRtoA, Integer> text) {
                        ((TransferRtoA)text.getRowValue()).setSrcStartBit(
                                text.getNewValue());
                    }
                }
        );

        dest.setCellFactory(cellRegAFactory);
        dest.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<TransferRtoA, RegisterArray>>() {
                    @Override
                    public void handle(
                            TableColumn.CellEditEvent<TransferRtoA, RegisterArray> text) {
                        ((TransferRtoA)text.getRowValue()).setDest(
                                text.getNewValue());
                    }
                }
        );

        destStartBit.setCellFactory(cellIntFactory);
        destStartBit.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<TransferRtoA, Integer>>() {
                    @Override
                    public void handle(
                            TableColumn.CellEditEvent<TransferRtoA, Integer> text) {
                        ((TransferRtoA)text.getRowValue()).setDestStartBit(
                                text.getNewValue());
                    }
                }
        );

        numBits.setCellFactory(cellIntFactory);
        numBits.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<TransferRtoA, Integer>>() {
                    @Override
                    public void handle(
                            TableColumn.CellEditEvent<TransferRtoA, Integer> text) {
                        ((TransferRtoA)text.getRowValue()).setNumBits(
                                text.getNewValue());
                    }
                }
        );

        index.setCellFactory(cellRegFactory);
        index.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<TransferRtoA, Register>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<TransferRtoA, Register> text) {
                        ((TransferRtoA)text.getRowValue()).setIndex(
                                text.getNewValue());
                    }
                }
        );

        indexStart.setCellFactory(cellIntFactory);
        indexStart.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<TransferRtoA, Integer>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<TransferRtoA, Integer> text) {
                        ((TransferRtoA)text.getRowValue()).setIndexStart(
                                text.getNewValue());
                    }
                }
        );

        indexNumBits.setCellFactory(cellIntFactory);
        indexNumBits.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<TransferRtoA, Integer>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<TransferRtoA, Integer> text) {
                        ((TransferRtoA)text.getRowValue()).setIndexNumBits(
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
        return TransferRtoA.class;
    }

    /**
     * getter for the current TranferRtoA Microinstructions.
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
        return "TransferRtoA";
    }

    /**
     * gets properties
     * @return an array of String representations of the
     * various properties of this type of microinstruction
     */
//    public String[] getProperties()
//    {
//        return new String[]{"name", "source", "srcStartBit", "dest",
//                "destStartBit", "numBits", "index",
//                "indexStart", "indexNumBits"};
//    }

    /**
     * use clones to replace existing Microinstructions
     * in the machine, and update the machine to delete
     * all references to the deleted Microinstructions.
     */
    public void updateCurrentMicrosFromClones()
    {
        machine.setMicros("transferRtoA", createNewMicroList(clones));
    }

    /**
     * Set the clones to the new array passed as a parameter.
     * Does not check for validity.
     *
     * @param newClones Object array containing new set of clones
     */
    public void setClones(ObservableList newClones)
    {
        TransferRtoA[] transferRtoAs = new TransferRtoA[newClones.size()];
        for (int i = 0; i < newClones.size(); i++) {
            transferRtoAs[i] = (TransferRtoA) newClones.get(i);
        }
        clones = transferRtoAs;
    }

    /**
     * Check validity of array of Objects' properties.
     * @param micros an array of Objects to check.
     * @return boolean denoting whether array has objects with
     * valid properties or not
     */
    public void checkValidity(ObservableList micros)
    {
        // convert the array to an array of TransferRtoAs
        TransferRtoA[] transferRtoAs = new TransferRtoA[micros.size()];

        for (int i = 0; i < micros.size(); i++) {
            transferRtoAs[i] = (TransferRtoA) micros.get(i);
        }

        // check that all names are unique and nonempty
        Validate.rangesAreInBound(transferRtoAs);

    }

    /**
     * returns true if new micros of this class can be created.
     */
    public boolean newMicrosAreAllowed()
    {
        return (machine.getModule("registerArrays").size() > 0);
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
