package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.model.microinstruction.Decode;
import cpusim.model.module.Register;
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
 * The controller for editing the Branch command in the EditMicroDialog.
 *
 * @author Jinghui Yu
 * @author Michael Goldenberg
 * @author Ben Borchard
 * @author Kevin Brightwell (Nava2)
 *
 * @since 2013-06-07
 */
class DecodeTableController extends MicroController<Decode> implements Initializable {
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Decode, String> name;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Decode, Register> ir;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    DecodeTableController(Mediator mediator){
        super(mediator, "DecodeTable.fxml", Decode.class);
    }

    /**
     * initializes the dialog window after its root element has been processed.
     * makes all the cells ediand the use can edit the cell directly and
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
        name.prefWidthProperty().bind(prefWidthProperty().divide(100/50.0));
        ir.prefWidthProperty().bind(prefWidthProperty().divide(100/50.0));

        Callback<TableColumn<Decode,String>,TableCell<Decode,String>> cellStrFactory =
                setStringTableColumn -> new cpusim.gui.util.EditingStrCell<>();
        Callback<TableColumn<Decode,Register>,TableCell<Decode,Register>> cellRegFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        machine.getAllRegisters());

        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        ir.setCellValueFactory(new PropertyValueFactory<>("ir"));

        //Add for EdiCell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(new NameColumnHandler());

        ir.setCellFactory(cellRegFactory);
        ir.setOnEditCommit(
                text -> ((Decode)text.getRowValue()).setIr(
                        text.getNewValue())
        );
    }

    /**
     * getter for prototype of the right subclass
     * @return the prototype of the subclass
     */
    @Override
    public Decode getPrototype() {
        Register r = (machine.getAllRegisters().size() == 0 ? null :
                (Register) machine.getAllRegisters().get(0));
        return new Decode("???", machine, r);
    }

    /**
     * returns a string about the type of the 
     * @return a string about the type of the 
     */
    @Override
    public String toString()
    {
        return "Decode";
    }

    /**
     * use clones to replace existing Microinstructions
     * in the machine, and update the machine to delete
     * all references to the deleted Microinstructions.
     */
    @Override
    public void updateMachineFromItems()
    {
        machine.setMicros(Decode.class, getItems());
    }
    
    /**
     * returns true if new micros of this class can be created.
     */
    @Override
    public boolean newMicrosAreAllowed() {
        return (machine.getModule("registers").size() > 0 ||
                machine.getModule("registerArrays").size() > 0);
    }

    /**
     * get the ID of the corresponding help page
     * @return the ID of the page
     */
    @Override
    public String getHelpPageID()
    {
        return "Decode";
    }

    /**
     * updates the by removing all the items and adding all back.
     * for refreshing the display.
     */
    @Override
    public void updateTable() {
        name.setVisible(false);
        name.setVisible(true);
        
        super.updateTable();
    }

}
