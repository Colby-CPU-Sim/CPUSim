/**
 * auther: Jinghui Yu
 * last edit date: 6/4/2013
 */


/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Changed the return value of checkValidity from a boolean to void (the functionality
 * enabled by that boolean value is now controlled by throwing ValidationException)
 * 2.) Changed the edit commit method on the name column so that it calls Validate.nameableObjects()
 * which throws a ValidationException in lieu of returning a boolean value
 * 3.) Moved rangeInBound method to the Validate class and changed the return value to void
 * from boolean
 */
package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.EditingLongCell;
import cpusim.gui.util.EditingNonNegativeIntCell;
import cpusim.gui.util.EditingStrCell;
import cpusim.gui.util.NamedColumnHandler;
import cpusim.model.microinstruction.Test;
import cpusim.model.module.Register;
import javafx.collections.FXCollections;
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
 * The controller for editing the Test command in the EditMicroDialog.
 */
class TestTableController
        extends MicroController<Test> implements Initializable {
    
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Test,String> name;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Test,Register> register;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Test,Integer> start;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Test,Integer> numBits;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Test,String> comparison;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Test,Long> value;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Test,Integer> omission;
    

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    TestTableController(Mediator mediator){
        super(mediator, "TestTable.fxml", Test.class);
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
        
        final double FACTOR = 100/16.0;
        name.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        register.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        start.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        numBits.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        comparison.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        value.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        omission.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));

        Callback<TableColumn<Test, String>, TableCell<Test, String>> cellStrFactory =
                setStringTableColumn -> new EditingStrCell<>();
        Callback<TableColumn<Test,Integer>,TableCell<Test,Integer>> cellIntFactory =
                setIntegerTableColumn -> new EditingNonNegativeIntCell<>();
        Callback<TableColumn<Test,Long>,TableCell<Test,Long>> cellLongFactory =
                setIntegerTableColumn -> new EditingLongCell<>();
        Callback<TableColumn<Test,Register>,TableCell<Test,Register>> cellRegFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        machine.getAllRegisters());

        Callback<TableColumn<Test,String>,TableCell<Test,String>> cellCompFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        FXCollections.observableArrayList(
                                "EQ",
                                "NE",
                                "LT",
                                "GT",
                                "LE",
                                "GE"
                        )
                );

        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        register.setCellValueFactory(new PropertyValueFactory<>("register"));
        start.setCellValueFactory(new PropertyValueFactory<>("start"));
        numBits.setCellValueFactory(new PropertyValueFactory<>("numBits"));
        comparison.setCellValueFactory(new PropertyValueFactory<>("comparison"));
        value.setCellValueFactory(new PropertyValueFactory<>("value"));
        omission.setCellValueFactory(new PropertyValueFactory<>("omission"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(new NamedColumnHandler<>(this));

        register.setCellFactory(cellRegFactory);
        register.setOnEditCommit(
                text -> text.getRowValue().setRegister(
                        text.getNewValue())
        );

        start.setCellFactory(cellIntFactory);
        start.setOnEditCommit(
                text -> text.getRowValue().setStart(
                        text.getNewValue())
        );

        numBits.setCellFactory(cellIntFactory);
        numBits.setOnEditCommit(
                text -> text.getRowValue().setNumBits(
                        text.getNewValue())
        );

        comparison.setCellFactory(cellCompFactory);
        comparison.setOnEditCommit((text) ->
                text.getRowValue().setComparison(Test.Operation.valueOf(text.getNewValue())));

        value.setCellFactory(cellLongFactory);
        value.setOnEditCommit(
                text -> text.getRowValue().setValue(
                        text.getNewValue())
        );

        omission.setCellFactory(cellIntFactory);
        omission.setOnEditCommit(
                text -> text.getRowValue().setOmission(
                        text.getNewValue())
        );
    }

    /**
     * getter for prototype of the right subclass
     * @return the prototype of the subclass
     */
    @Override
    public Test getPrototype()
    {
        final Register r = (machine.getAllRegisters().size() == 0 ? null :
                machine.getAllRegisters().get(0));
        return new Test("???", machine, r, 0, 1, "EQ", 0, 0);
    }
    
    /**
     * returns a string about the type of the 
     * @return a string about the type of the 
     */
    @Override
    public String toString()
    {
        return "Test";
    }


    /**
     * use clones to replace existing Microinstructions
     * in the machine, and update the machine to delete
     * all references to the deleted Microinstructions.
     */
    @Override
    public void updateMachineFromItems()
    {
        machine.setMicros(Test.class, getItems());
    }

    @Override
    public void checkValidity(ObservableList<Test> micros) {
        super.checkValidity(micros);
        
        Test.validateRangeInBound(micros);
    }

    @Override
    public boolean newMicrosAreAllowed() {
        return (machine.getModule("registers").size() > 0 ||
                machine.getModule("registerArrays").size() > 0);
    }

    /**
     * get the ID of the corresponding help page
     * @return the ID of the page
     */
    public String getHelpPageID()
    {
        return "Test";
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
