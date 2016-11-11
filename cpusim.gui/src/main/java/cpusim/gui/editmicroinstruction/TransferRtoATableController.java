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
import cpusim.gui.util.EditingNonNegativeIntCell;
import cpusim.model.microinstruction.TransferRtoA;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * The controller for editing the TransferRtoA command in the EditMicroDialog.
 */
public class TransferRtoATableController
        extends MicroController<TransferRtoA> implements Initializable {
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferRtoA,String> name;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferRtoA,Register> source;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferRtoA,Integer> srcStartBit;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferRtoA,RegisterArray> dest;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferRtoA,Integer> destStartBit;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferRtoA,Integer> numBits;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferRtoA,Register> index;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferRtoA,Integer> indexStart;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferRtoA,Integer> indexNumBits;


    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    TransferRtoATableController(Mediator mediator) {
        super(mediator, "TransferRtoATable.fxml", TransferRtoA.class);
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
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        final double FACTOR = 100.0/11.0;
        name.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        source.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        srcStartBit.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        dest.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        destStartBit.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        numBits.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        index.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        indexStart.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        indexNumBits.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));

        Callback<TableColumn<TransferRtoA, String>,
                TableCell<TransferRtoA, String>> cellStrFactory =
                setStringTableColumn -> new cpusim.gui.util.EditingStrCell<>();
        Callback<TableColumn<TransferRtoA,Integer>,
                TableCell<TransferRtoA,Integer>> cellIntFactory =
                setIntegerTableColumn -> new EditingNonNegativeIntCell<>();
        Callback<TableColumn<TransferRtoA,Register>,
                TableCell<TransferRtoA,Register>> cellRegFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        machine.getAllRegisters()
                );
        Callback<TableColumn<TransferRtoA,RegisterArray>,
                TableCell<TransferRtoA,RegisterArray>> cellRegAFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        machine.getModule("registerArrays", RegisterArray.class));

        name.setCellValueFactory(
                new PropertyValueFactory<>("name"));
        source.setCellValueFactory(
                new PropertyValueFactory<>("source"));
        srcStartBit.setCellValueFactory(
                new PropertyValueFactory<>("srcStartBit"));
        dest.setCellValueFactory(
                new PropertyValueFactory<>("dest"));
        destStartBit.setCellValueFactory(
                new PropertyValueFactory<>("destStartBit"));
        numBits.setCellValueFactory(
                new PropertyValueFactory<>("numBits"));
        index.setCellValueFactory(
                new PropertyValueFactory<>("index"));
        indexStart.setCellValueFactory(
                new PropertyValueFactory<>("indexStart"));
        indexNumBits.setCellValueFactory(
                new PropertyValueFactory<>("indexNumBits"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(new NameColumnHandler());

        source.setCellFactory(cellRegFactory);
        source.setOnEditCommit(
                text -> text.getRowValue().setSource(
                        text.getNewValue())
        );

        srcStartBit.setCellFactory(cellIntFactory);
        srcStartBit.setOnEditCommit(
                text -> text.getRowValue().setSrcStartBit(
                        text.getNewValue())
        );

        dest.setCellFactory(cellRegAFactory);
        dest.setOnEditCommit(
                text -> text.getRowValue().setDest(
                        text.getNewValue())
        );

        destStartBit.setCellFactory(cellIntFactory);
        destStartBit.setOnEditCommit(
                text -> text.getRowValue().setDestStartBit(
                        text.getNewValue())
        );

        numBits.setCellFactory(cellIntFactory);
        numBits.setOnEditCommit(
                text -> text.getRowValue().setNumBits(
                        text.getNewValue())
        );

        index.setCellFactory(cellRegFactory);
        index.setOnEditCommit(
                text -> text.getRowValue().setIndex(
                        text.getNewValue())
        );

        indexStart.setCellFactory(cellIntFactory);
        indexStart.setOnEditCommit(
                text -> text.getRowValue().setIndexStart(
                        text.getNewValue())
        );

        indexNumBits.setCellFactory(cellIntFactory);
        indexNumBits.setOnEditCommit(
                text -> text.getRowValue().setIndexNumBits(
                        text.getNewValue())
        );
    }
    
    @Override
    public TransferRtoA getPrototype() {
        final RegisterArray a = (machine.getModule("registerArrays").size() == 0 ? null :
                machine.getModule("registerArrays", RegisterArray.class).get(0));
        final Register r = (machine.getAllRegisters().size() == 0 ? null :
                machine.getAllRegisters().get(0));
        return new TransferRtoA("???", machine, r, 0, a, 0, 0, r,0, 0);
    }
    
    /**
     * returns a string about the type of the 
     * @return a string about the type of the 
     */
    public String toString()
    {
        return "TransferRtoA";
    }

    @Override
    public void updateMachineFromItems() {
        machine.setMicros(TransferRtoA.class, getItems());
    }

    @Override
    public void checkValidity(ObservableList<TransferRtoA> micros)
    {
        super.checkValidity(micros);
        // convert the array to an array of TransferRtoAs
        TransferRtoA.validateRangesInBounds(micros);
    }

    /**
     * returns true if new micros of this class can be created.
     */
    @Override
    public boolean newMicrosAreAllowed()
    {
        return (machine.getModule("registerArrays").size() > 0);
    }

    /**
     * get the ID of the corresponding help page
     * @return the ID of the page
     */
    @Override
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
        
        super.updateTable();
    }

}
