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
import cpusim.Microinstruction;
import cpusim.gui.util.EditingNonNegativeIntCell;
import cpusim.microinstruction.TransferAtoR;
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
public class TransferAtoRTableController
        extends MicroController implements Initializable {
    @FXML
    TableView<TransferAtoR> table;
    @FXML
    TableColumn<TransferAtoR,String> name;
    @FXML TableColumn<TransferAtoR,RegisterArray> source;
    @FXML TableColumn<TransferAtoR,Integer> srcStartBit;
    @FXML TableColumn<TransferAtoR,Register> dest;
    @FXML TableColumn<TransferAtoR,Integer> destStartBit;
    @FXML TableColumn<TransferAtoR,Integer> numBits;
    @FXML TableColumn<TransferAtoR,Register> index;
    @FXML TableColumn<TransferAtoR,Integer> indexStart;
    @FXML TableColumn<TransferAtoR,Integer> indexNumBits;

    private ObservableList currentMicros;
    private TransferAtoR prototype;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    public TransferAtoRTableController(Mediator mediator){
        super(mediator);
        this.mediator = mediator;
        this.machine = this.mediator.getMachine();
        this.currentMicros = machine.getMicros("transferAtoR");
        RegisterArray a = (machine.getModule("registerArrays").size() == 0 ? null :
                (RegisterArray) machine.getModule("registerArrays").get(0));
        Register r = (machine.getAllRegisters().size() == 0 ? null :
                (Register) machine.getAllRegisters().get(0));
        this.prototype = new TransferAtoR("???", machine, a, 0, r, 0, 0, r,0, 0);
        clones = (Microinstruction[]) createClones();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "transferAtoRTable.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            // should never happen
            assert false : "Unable to load file: transferAtoRTable.fxml";
        }

        for (int i = 0; i < clones.length; i++){
            table.getItems().add((TransferAtoR)clones[i]);
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

        Callback<TableColumn<TransferAtoR, String>,
                TableCell<TransferAtoR, String>> cellStrFactory =
                new Callback<TableColumn<TransferAtoR, String>,
                        TableCell<TransferAtoR, String>>() {
                    @Override
                    public TableCell call(
                            TableColumn setStringTableColumn) {
                        return new cpusim.gui.util.EditingStrCell<TransferAtoR>();
                    }
                };
        Callback<TableColumn<TransferAtoR,Integer>,
                TableCell<TransferAtoR,Integer>> cellIntFactory =
                new Callback<TableColumn<TransferAtoR,Integer>,
                        TableCell<TransferAtoR, Integer>>() {
                    @Override
                    public TableCell call(
                            TableColumn<TransferAtoR, Integer> setIntegerTableColumn) {
                        return new EditingNonNegativeIntCell<TransferAtoR>();
                    }
                };
        Callback<TableColumn<TransferAtoR,Register>,
                TableCell<TransferAtoR,Register>> cellRegFactory =
                new Callback<TableColumn<TransferAtoR, Register>,
                        TableCell<TransferAtoR, Register>>() {
                    @Override
                    public TableCell<TransferAtoR,Register> call(
                            TableColumn<TransferAtoR, Register> setStringTableColumn) {
                        return new ComboBoxTableCell<TransferAtoR,Register>(
                                machine.getAllRegisters()
                        );
                    }
                };
        Callback<TableColumn<TransferAtoR,RegisterArray>,
                TableCell<TransferAtoR,RegisterArray>> cellRegAFactory =
                new Callback<TableColumn<TransferAtoR, RegisterArray>,
                        TableCell<TransferAtoR, RegisterArray>>() {
                    @Override
                    public TableCell<TransferAtoR,RegisterArray> call(
                            TableColumn<TransferAtoR, RegisterArray> setStringTableColumn) {
                        return new ComboBoxTableCell<TransferAtoR,RegisterArray>(
                                (ObservableList<RegisterArray>)machine.getModule("registerArrays")
                        );
                    }
                };

        name.setCellValueFactory(
                new PropertyValueFactory<TransferAtoR, String>("name"));
        source.setCellValueFactory(
                new PropertyValueFactory<TransferAtoR, RegisterArray>("source"));
        srcStartBit.setCellValueFactory(
                new PropertyValueFactory<TransferAtoR, Integer>("srcStartBit"));
        dest.setCellValueFactory(
                new PropertyValueFactory<TransferAtoR, Register>("dest"));
        destStartBit.setCellValueFactory(
                new PropertyValueFactory<TransferAtoR, Integer>("destStartBit"));
        numBits.setCellValueFactory(
                new PropertyValueFactory<TransferAtoR, Integer>("numBits"));
        index.setCellValueFactory(
                new PropertyValueFactory<TransferAtoR, Register>("index"));
        indexStart.setCellValueFactory(
                new PropertyValueFactory<TransferAtoR, Integer>("indexStart"));
        indexNumBits.setCellValueFactory(
                new PropertyValueFactory<TransferAtoR, Integer>("indexNumBits"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<TransferAtoR, String>>() {
                    @Override
                    public void handle(
                            TableColumn.CellEditEvent<TransferAtoR, String> text) {
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

        source.setCellFactory(cellRegAFactory);
        source.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<TransferAtoR, RegisterArray>>() {
                    @Override
                    public void handle(
                            TableColumn.CellEditEvent<TransferAtoR, RegisterArray> text) {
                        ((TransferAtoR)text.getRowValue()).setSource(
                                text.getNewValue());
                    }
                }
        );

        srcStartBit.setCellFactory(cellIntFactory);
        srcStartBit.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<TransferAtoR, Integer>>() {
                    @Override
                    public void handle(
                            TableColumn.CellEditEvent<TransferAtoR, Integer> text) {
                        ((TransferAtoR)text.getRowValue()).setSrcStartBit(
                                text.getNewValue());
                    }
                }
        );

        dest.setCellFactory(cellRegFactory);
        dest.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<TransferAtoR, Register>>() {
                    @Override
                    public void handle(
                            TableColumn.CellEditEvent<TransferAtoR, Register> text) {
                        ((TransferAtoR)text.getRowValue()).setDest(
                                text.getNewValue());
                    }
                }
        );

        destStartBit.setCellFactory(cellIntFactory);
        destStartBit.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<TransferAtoR, Integer>>() {
                    @Override
                    public void handle(
                            TableColumn.CellEditEvent<TransferAtoR, Integer> text) {
                        ((TransferAtoR)text.getRowValue()).setDestStartBit(
                                text.getNewValue());
                    }
                }
        );

        numBits.setCellFactory(cellIntFactory);
        numBits.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<TransferAtoR, Integer>>() {
                    @Override
                    public void handle(
                            TableColumn.CellEditEvent<TransferAtoR, Integer> text) {
                        ((TransferAtoR)text.getRowValue()).setNumBits(
                                text.getNewValue());
                    }
                }
        );

        index.setCellFactory(cellRegFactory);
        index.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<TransferAtoR, Register>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<TransferAtoR, Register> text) {
                        ((TransferAtoR)text.getRowValue()).setIndex(
                                text.getNewValue());
                    }
                }
        );

        indexStart.setCellFactory(cellIntFactory);
        indexStart.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<TransferAtoR, Integer>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<TransferAtoR, Integer> text) {
                        ((TransferAtoR)text.getRowValue()).setIndexStart(
                                text.getNewValue());
                    }
                }
        );

        indexNumBits.setCellFactory(cellIntFactory);
        indexNumBits.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<TransferAtoR, Integer>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<TransferAtoR, Integer> text) {
                        ((TransferAtoR)text.getRowValue()).setIndexNumBits(
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
        return TransferAtoR.class;
    }

    /**
     * getter for the current TranferAtoR Microinstructions.
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
        return "TransferAtoR";
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
        machine.setMicros("transferAtoR", createNewMicroList(clones));
    }

    /**
     * Set the clones to the new array passed as a parameter.
     * Does not check for validity.
     *
     * @param newClones Object array containing new set of clones
     */
    public void setClones(ObservableList newClones)
    {
        TransferAtoR[] transferAtoRs = new TransferAtoR[newClones.size()];
        for (int i = 0; i < newClones.size(); i++) {
            transferAtoRs[i] = (TransferAtoR) newClones.get(i);
        }
        clones = transferAtoRs;
    }

    /**
     * Check validity of array of Objects' properties.
     * @param micros an array of Objects to check.
     * @return boolean denoting whether array has objects with
     * valid properties or not
     */
    public void checkValidity(ObservableList micros)
    {
        // convert the array to an array of TransferAtoRs
        TransferAtoR[] transferAtoRs = new TransferAtoR[micros.size()];

        for (int i = 0; i < micros.size(); i++) {
            transferAtoRs[i] = (TransferAtoR) micros.get(i);

            Validate.registerIsNotReadOnly(
                    ((TransferAtoR) micros.get(i)).getDest(),
                    ((TransferAtoR) micros.get(i)).getName());
        }

        // check that all names are unique and nonempty
        Validate.rangesAreInBound(transferAtoRs);

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