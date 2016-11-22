/**
 * Authoer: Jinghui Yu
 * Last editing date: 6/7/2013
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
import cpusim.gui.util.NamedColumnHandler;
import cpusim.model.microinstruction.MemoryAccess;
import cpusim.model.module.RAM;
import cpusim.model.module.Register;
import javafx.collections.FXCollections;
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
 * The controller for editing the Logical command in the EditMicroDialog.
 */
public class MemoryAccessTableController
        extends MicroController<MemoryAccess> implements Initializable {
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<MemoryAccess,String> name;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<MemoryAccess,String> direction;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<MemoryAccess,RAM> memory;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<MemoryAccess,Register> data;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<MemoryAccess,Register> address;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
     MemoryAccessTableController(Mediator mediator){
        super(mediator, "MemoryAccessTable.fxml", MemoryAccess.class);
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
        final double FACTOR = 100.0/20.0;
        name.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        direction.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        memory.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        data.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        address.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));

        Callback<TableColumn<MemoryAccess,String>,TableCell<MemoryAccess,String>> cellStrFactory =
                setStringTableColumn -> new cpusim.gui.util.EditingStrCell<>();
        Callback<TableColumn<MemoryAccess,Register>,TableCell<MemoryAccess,Register>> cellRegFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        machine.getAllRegisters());
        Callback<TableColumn<MemoryAccess,String>,TableCell<MemoryAccess,String>> cellDircFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        FXCollections.observableArrayList(
                                "read",
                                "write"
                        )
                );
        Callback<TableColumn<MemoryAccess,RAM>,TableCell<MemoryAccess,RAM>> cellRAMFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        machine.getAllRAMs()
                );

        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        direction.setCellValueFactory(new PropertyValueFactory<>("direction"));
        memory.setCellValueFactory(new PropertyValueFactory<>("memory"));
        data.setCellValueFactory(new PropertyValueFactory<>("data"));
        address.setCellValueFactory(new PropertyValueFactory<>("address"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(new NamedColumnHandler<>(this));

        direction.setCellFactory(cellDircFactory);
        direction.setOnEditCommit(
                text -> text.getRowValue().setDirection(text.getNewValue())
        );

        memory.setCellFactory(cellRAMFactory);
        memory.setOnEditCommit(
                text -> text.getRowValue().setMemory(text.getNewValue())
        );

        data.setCellFactory(cellRegFactory);
        data.setOnEditCommit(
                text -> text.getRowValue().setData(text.getNewValue())
        );

        address.setCellFactory(cellRegFactory);
        address.setOnEditCommit(
                text -> text.getRowValue().setAddress(text.getNewValue())
        );

    }

    /**
     * getter for prototype of the right subclass
     * @return the prototype of the subclass
     */
    @Override
    public MemoryAccess getPrototype()
    {
        Register r = (machine.getAllRegisters().size() == 0 ? null :
                machine.getAllRegisters().get(0));
        RAM ram = (machine.getModule("rams").size() == 0 ? null :
                (RAM) machine.getModule("rams").get(0));
        return new MemoryAccess("???", machine, "read", ram, r, r);
    }

    /**
     * returns a string about the type of the 
     * @return a string about the type of the 
     */
    public String toString()
    {
        return "MemoryAccess";
    }

    /**
     * use clones to replace existing Microinstructions
     * in the machine, and update the machine to delete
     * all references to the deleted Microinstructions.
     */
    @Override
    public void updateMachineFromItems()
    {
        machine.setMicros(MemoryAccess.class, getItems());
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
        return "MemoryAccess";
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
