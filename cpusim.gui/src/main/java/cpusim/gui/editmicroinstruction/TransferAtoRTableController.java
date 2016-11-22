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
import cpusim.gui.util.EditingNonNegativeIntCell;
import cpusim.gui.util.NamedColumnHandler;
import cpusim.model.microinstruction.TransferAtoR;
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
class TransferAtoRTableController
        extends MicroController<TransferAtoR> implements Initializable {
   
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferAtoR,String> name;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferAtoR,RegisterArray> source;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferAtoR,Integer> srcStartBit;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferAtoR,Register> dest;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferAtoR,Integer> destStartBit;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferAtoR,Integer> numBits;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferAtoR,Register> index;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferAtoR,Integer> indexStart;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<TransferAtoR,Integer> indexNumBits;
    
    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    TransferAtoRTableController(Mediator mediator){
        super(mediator, "TransferAtoRTable.fxml", TransferAtoR.class);
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
        name.prefWidthProperty().bind(prefWidthProperty().divide(100/12.0));
        source.prefWidthProperty().bind(prefWidthProperty().divide(100/11.0));
        srcStartBit.prefWidthProperty().bind(prefWidthProperty().divide(100/11.0));
        dest.prefWidthProperty().bind(prefWidthProperty().divide(100/11.0));
        destStartBit.prefWidthProperty().bind(prefWidthProperty().divide(100/11.0));
        numBits.prefWidthProperty().bind(prefWidthProperty().divide(100/11.0));
        index.prefWidthProperty().bind(prefWidthProperty().divide(100/11.0));
        indexStart.prefWidthProperty().bind(prefWidthProperty().divide(100/11.0));
        indexNumBits.prefWidthProperty().bind(prefWidthProperty().divide(100/11.0));
        
        Callback<TableColumn<TransferAtoR,Integer>,
                TableCell<TransferAtoR,Integer>> cellIntFactory = _ignore -> new EditingNonNegativeIntCell<>();
        
        Callback<TableColumn<TransferAtoR,Register>,
                TableCell<TransferAtoR,Register>> cellRegFactory =
                    _ignore -> new ComboBoxTableCell<>(machine.getAllRegisters());
        
        Callback<TableColumn<TransferAtoR,RegisterArray>,
                TableCell<TransferAtoR,RegisterArray>> cellRegAFactory =
                    _ignore -> new ComboBoxTableCell<>(machine.getModule(RegisterArray.class));

        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        source.setCellValueFactory(new PropertyValueFactory<>("source"));
        srcStartBit.setCellValueFactory(new PropertyValueFactory<>("srcStartBit"));
        dest.setCellValueFactory(new PropertyValueFactory<>("dest"));
        destStartBit.setCellValueFactory(new PropertyValueFactory<>("destStartBit"));
        numBits.setCellValueFactory(new PropertyValueFactory<>("numBits"));
        index.setCellValueFactory(new PropertyValueFactory<>("index"));
        indexStart.setCellValueFactory(new PropertyValueFactory<>("indexStart"));
        indexNumBits.setCellValueFactory(new PropertyValueFactory<>("indexNumBits"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(_ignore -> new cpusim.gui.util.EditingStrCell<>());
        name.setOnEditCommit(new NamedColumnHandler<>(this));

        source.setCellFactory(cellRegAFactory);
        source.setOnEditCommit(text -> text.getRowValue().setSource(text.getNewValue()));

        srcStartBit.setCellFactory(_ignore -> new EditingNonNegativeIntCell<>());
        srcStartBit.setOnEditCommit(text -> text.getRowValue().setSrcStartBit(text.getNewValue()));

        dest.setCellFactory(cellRegFactory);
        dest.setOnEditCommit(text -> text.getRowValue().setDest(text.getNewValue()));

        destStartBit.setCellFactory(_ignore -> new EditingNonNegativeIntCell<>());
        destStartBit.setOnEditCommit(text -> text.getRowValue().setDestStartBit(text.getNewValue()));

        numBits.setCellFactory(cellIntFactory);
        numBits.setOnEditCommit(text -> text.getRowValue().setNumBits(text.getNewValue()));

        index.setCellFactory(cellRegFactory);
        index.setOnEditCommit(text -> text.getRowValue().setIndex(text.getNewValue()));

        indexStart.setCellFactory(cellIntFactory);
        indexStart.setOnEditCommit(text -> text.getRowValue().setIndexStart(text.getNewValue()));

        indexNumBits.setCellFactory(cellIntFactory);
        indexNumBits.setOnEditCommit(text -> text.getRowValue().setIndexNumBits(text.getNewValue()));
    }
    
    /**
     * returns a string about the type of the 
     * @return a string about the type of the 
     */
    public String toString()
    {
        return "TransferAtoR";
    }
    
    @Override
    public TransferAtoR getPrototype() {
        RegisterArray a = (machine.getModule("registerArrays").size() == 0 ? null :
                (RegisterArray) machine.getModule("registerArrays").get(0));
        Register r = (machine.getAllRegisters().size() == 0 ? null : machine.getAllRegisters().get(0));
        return new TransferAtoR("???", machine, a, 0, r, 0, 0, r,0, 0);
    }
    
    /**
     * use clones to replace existing Microinstructions
     * in the machine, and update the machine to delete
     * all references to the deleted Microinstructions.
     */
    @Override
    public void updateMachineFromItems()
    {
        machine.setMicros(TransferAtoR.class, getItems());
    }

    /**
     * Check validity of array of Objects' properties.
     * @param micros an array of Objects to check.
     */
    @Override
    public void checkValidity(ObservableList<TransferAtoR> micros)
    {
        super.checkValidity(micros);
        
        // convert the array to an array of TransferAtoRs
        for (TransferAtoR check: micros) {
            Register.validateIsNotReadOnly(check.getDest(), check.getName());
        }
        
        // check that all names are unique and nonempty
        TransferAtoR.validateRangesInBound(micros);
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
    @Override
    public void updateTable()
    {
        name.setVisible(false);
        name.setVisible(true);
        
        super.updateTable();
    }
}