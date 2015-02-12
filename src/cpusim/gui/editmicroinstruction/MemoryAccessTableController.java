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
import cpusim.Microinstruction;
import cpusim.microinstruction.MemoryAccess;
import cpusim.module.RAM;
import cpusim.module.Register;
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
 * The controller for editing the Logical command in the EditMicroDialog.
 */
public class MemoryAccessTableController
        extends MicroController implements Initializable {
    @FXML TableView<MemoryAccess> table;
    @FXML TableColumn<MemoryAccess,String> name;
    @FXML TableColumn<MemoryAccess,String> direction;
    @FXML TableColumn<MemoryAccess,RAM> memory;
    @FXML TableColumn<MemoryAccess,Register> data;
    @FXML TableColumn<MemoryAccess,Register> address;

    private ObservableList currentMicros;
    private MemoryAccess prototype;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    public MemoryAccessTableController(Mediator mediator){
        super(mediator);
        this.mediator = mediator;
        this.machine = this.mediator.getMachine();
        this.currentMicros = machine.getMicros("memoryAccess");
        Register r = (machine.getAllRegisters().size() == 0 ? null :
                (Register) machine.getAllRegisters().get(0));
        RAM ram = (machine.getModule("rams").size() == 0 ? null :
                (RAM) machine.getModule("rams").get(0));
        this.prototype = new MemoryAccess("???", machine, "read", ram, r, r);
        clones = (Microinstruction[]) createClones();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "memoryAccessTable.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        for (int i = 0; i < clones.length; i++){
            table.getItems().add((MemoryAccess)clones[i]);
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
        name.prefWidthProperty().bind(table.prefWidthProperty().divide(100/20.0));
        direction.prefWidthProperty().bind(table.prefWidthProperty().divide(100/20.0));
        memory.prefWidthProperty().bind(table.prefWidthProperty().divide(100/20.0));
        data.prefWidthProperty().bind(table.prefWidthProperty().divide(100/20.0));
        address.prefWidthProperty().bind(table.prefWidthProperty().divide(100/20.0));

        Callback<TableColumn<MemoryAccess,String>,TableCell<MemoryAccess,String>> cellStrFactory =
                new Callback<TableColumn<MemoryAccess, String>, TableCell<MemoryAccess, String>>() {
                    @Override
                    public TableCell<MemoryAccess, String> call(
                            TableColumn<MemoryAccess, String> setStringTableColumn) {
                        return new cpusim.gui.util.EditingStrCell<MemoryAccess>();
                    }
                };
        Callback<TableColumn<MemoryAccess,Register>,TableCell<MemoryAccess,Register>> cellRegFactory =
                new Callback<TableColumn<MemoryAccess, Register>, TableCell<MemoryAccess, Register>>() {
                    @Override
                    public TableCell<MemoryAccess, Register> call(
                            TableColumn<MemoryAccess, Register> setStringTableColumn) {
                        return new ComboBoxTableCell<MemoryAccess,Register>(
                                machine.getAllRegisters());
                    }
                };
        Callback<TableColumn<MemoryAccess,String>,TableCell<MemoryAccess,String>> cellDircFactory =
                new Callback<TableColumn<MemoryAccess, String>, TableCell<MemoryAccess, String>>() {
                    @Override
                    public TableCell<MemoryAccess, String> call(
                            TableColumn<MemoryAccess, String> setStringTableColumn) {
                        return new ComboBoxTableCell<MemoryAccess,String>(
                                FXCollections.observableArrayList(
                                        "read",
                                        "write"
                                )
                        );
                    }
                };
        Callback<TableColumn<MemoryAccess,RAM>,TableCell<MemoryAccess,RAM>> cellRAMFactory =
                new Callback<TableColumn<MemoryAccess, RAM>, TableCell<MemoryAccess, RAM>>() {
                    @Override
                    public TableCell<MemoryAccess, RAM> call(
                            TableColumn<MemoryAccess, RAM> setStringTableColumn) {
                        return new ComboBoxTableCell<MemoryAccess,RAM>(
                                machine.getAllRAMs()
                                );
                    }
                };

        name.setCellValueFactory(new PropertyValueFactory<MemoryAccess, String>("name"));
        direction.setCellValueFactory(new PropertyValueFactory<MemoryAccess, String>("direction"));
        memory.setCellValueFactory(new PropertyValueFactory<MemoryAccess, RAM>("memory"));
        data.setCellValueFactory(new PropertyValueFactory<MemoryAccess, Register>("data"));
        address.setCellValueFactory(new PropertyValueFactory<MemoryAccess, Register>("address"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<MemoryAccess, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<MemoryAccess, String> text) {
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

        direction.setCellFactory(cellDircFactory);
        direction.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<MemoryAccess, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<MemoryAccess, String> text) {
                        ((MemoryAccess)text.getRowValue()).setDirection(
                                text.getNewValue());
                    }
                }
        );

        memory.setCellFactory(cellRAMFactory);
        memory.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<MemoryAccess, RAM>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<MemoryAccess, RAM> text) {
                        ((MemoryAccess)text.getRowValue()).setMemory(
                                text.getNewValue());
                    }
                }
        );

        data.setCellFactory(cellRegFactory);
        data.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<MemoryAccess, Register>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<MemoryAccess, Register> text) {
                        ((MemoryAccess)text.getRowValue()).setData(
                                text.getNewValue());
                    }
                }
        );

        address.setCellFactory(cellRegFactory);
        address.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<MemoryAccess, Register>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<MemoryAccess, Register> text) {
                        ((MemoryAccess)text.getRowValue()).setAddress(
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
        return MemoryAccess.class;
    }

    /**
     * getter for the current MemoryAccess Microinstructions.
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
        return "MemoryAccess";
    }

    /**
     * gets properties
     * @return an array of String representations of the
     * various properties of this type of microinstruction
     */
//    public String[] getProperties()
//    {
//        return new String[]{"name", "direction", "memory",
//                "data", "address"};
//    }

    /**
     * use clones to replace existing Microinstructions
     * in the machine, and update the machine to delete
     * all references to the deleted Microinstructions.
     */
    public void updateCurrentMicrosFromClones()
    {
        machine.setMicros("memoryAccess", createNewMicroList(clones));
    }

    /**
     * Set the clones to the new array passed as a parameter.
     * Does not check for validity.
     *
     * @param newClones Object array containing new set of clones
     */
    public void setClones(ObservableList newClones)
    {
        MemoryAccess[] memoryAccesses = new MemoryAccess[newClones.size()];
        for (int i = 0; i < newClones.size(); i++) {
            memoryAccesses[i] = (MemoryAccess) newClones.get(i);
        }
        clones = memoryAccesses;
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
        double w =  table.getWidth();
        table.setPrefWidth(w-1);
        table.setPrefWidth(w);
    }

}
