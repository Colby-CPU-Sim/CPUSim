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
import cpusim.model.microinstruction.Test;
import cpusim.model.module.Register;
import cpusim.model.util.IdentifiedObject;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

/**
 * The controller for editing the {@link Test} command in the {@link EditMicroinstructionsController}.
 */
class TestTableController extends MicroinstructionTableController<Test> {

    /**
     * Marker used when building tabs.
     *
     * @see #getFxId()
     */
    final static String FX_ID = "testTab";

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
        loadFXML();
    }

    @Override
    public void initializeTable() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        final double FACTOR = 100/16.0;
        name.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        register.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        start.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        numBits.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        comparison.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        value.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));
        omission.prefWidthProperty().bind(prefWidthProperty().divide(FACTOR));

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

        register.setCellValueFactory(new PropertyValueFactory<>("register"));
        start.setCellValueFactory(new PropertyValueFactory<>("start"));
        numBits.setCellValueFactory(new PropertyValueFactory<>("numBits"));
        comparison.setCellValueFactory(new PropertyValueFactory<>("comparison"));
        value.setCellValueFactory(new PropertyValueFactory<>("value"));
        omission.setCellValueFactory(new PropertyValueFactory<>("omission"));

        //Add for Editable Cell of each field, in String or in Integer
        register.setCellFactory(cellRegFactory);
        register.setOnEditCommit(text -> text.getRowValue().setRegister(text.getNewValue()));

        start.setCellFactory(cellIntFactory);
        start.setOnEditCommit(text -> text.getRowValue().setStart(text.getNewValue()));

        numBits.setCellFactory(cellIntFactory);
        numBits.setOnEditCommit(text -> text.getRowValue().setNumBits(text.getNewValue()));

        comparison.setCellFactory(cellCompFactory);
        comparison.setOnEditCommit((text) ->
                text.getRowValue().setComparison(Test.Operation.valueOf(text.getNewValue())));

        value.setCellFactory(cellLongFactory);
        value.setOnEditCommit(text -> text.getRowValue().setValue(text.getNewValue()));

        omission.setCellFactory(cellIntFactory);
        omission.setOnEditCommit(text -> text.getRowValue().setOmission(text.getNewValue()));
    }

    @Override
    String getFxId() {
        return FX_ID;
    }

    @Override
    public Test createInstance() {
        final Register r = (machine.getAllRegisters().size() == 0 ? null :
                machine.getAllRegisters().get(0));
        return new Test("???", IdentifiedObject.generateRandomID(), machine, r,
                0, 1, Test.Operation.EQ,
                0, 0);
    }

    @Override
    public boolean isNewButtonEnabled() {
        return areRegistersAvailable();
    }

    @Override
    public String toString()
    {
        return "Test";
    }

    /**
     * get the ID of the corresponding help page
     * @return the ID of the page
     */
    public String getHelpPageID()
    {
        return "Test";
    }
}
