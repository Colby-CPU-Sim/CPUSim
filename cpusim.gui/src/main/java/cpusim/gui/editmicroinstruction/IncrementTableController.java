/**
 * auther: Jinghui Yu
 * last edit date:
 */


package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.EditingLongCell;
import cpusim.gui.util.EditingStrCell;
import cpusim.gui.util.NamedColumnHandler;
import cpusim.model.microinstruction.Increment;
import cpusim.model.module.ConditionBit;
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
 * The controller for editing the Increment command in the EditMicroDialog.
 *
 * @author Jinghui Yu
 * @author Michael Goldenberg
 * @author Ben Borchard
 * @since 2013-06-05
 */
class IncrementTableController
        extends MicroController<Increment> implements Initializable {
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Increment,String> name;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Increment,Register> register;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Increment,ConditionBit> overflowBit;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Increment,ConditionBit> carryBit;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<Increment,Long> delta;

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    IncrementTableController(Mediator mediator){
        super(mediator, "IncrementTable.fxml", Increment.class);
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
        
        final double FACTOR = 100/20.0;
        name.prefWidthProperty().bind(widthProperty().divide(FACTOR));
        register.prefWidthProperty().bind(widthProperty().divide(FACTOR));
        overflowBit.prefWidthProperty().bind(widthProperty().divide(FACTOR));
        carryBit.prefWidthProperty().bind(widthProperty().divide(FACTOR));
        delta.prefWidthProperty().bind(widthProperty().divide(FACTOR));

        Callback<TableColumn<Increment,String>,TableCell<Increment,String>> cellStrFactory =
                setStringTableColumn -> new EditingStrCell<>();
        Callback<TableColumn<Increment,Long>,TableCell<Increment,Long>> cellLongFactory =
                setIntegerTableColumn -> new EditingLongCell<>();
        Callback<TableColumn<Increment,Register>,TableCell<Increment,Register>> cellRegFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(machine.getAllRegisters());

        final ObservableList<ConditionBit> condBit = FXCollections.observableArrayList(ConditionBit.none());
        condBit.addAll(machine.getModule(ConditionBit.class));
        
        Callback<TableColumn<Increment,ConditionBit>,TableCell<Increment,ConditionBit>> cellCondFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(condBit);

        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        register.setCellValueFactory(new PropertyValueFactory<>("register"));
        overflowBit.setCellValueFactory(new PropertyValueFactory<>("overflowBit"));
        carryBit.setCellValueFactory(new PropertyValueFactory<>("carryBit"));
        delta.setCellValueFactory(new PropertyValueFactory<>("delta"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(new NamedColumnHandler<>(this));

        register.setCellFactory(cellRegFactory);
        register.setOnEditCommit(
                text -> text.getRowValue().setRegister(text.getNewValue()
                )
        );

        overflowBit.setCellFactory(cellCondFactory);
        overflowBit.setOnEditCommit(
                text -> text.getRowValue().setOverflowBit(text.getNewValue()
                )
        );

        carryBit.setCellFactory(cellCondFactory);
        carryBit.setOnEditCommit(
                text -> text.getRowValue().setCarryBit(text.getNewValue()
                )
        );

        delta.setCellFactory(cellLongFactory);
        delta.setOnEditCommit(
                text -> text.getRowValue().setDelta(text.getNewValue())
        );
    }
    
    @Override
    public Increment getPrototype() {
        Register r = (machine.getAllRegisters().size() == 0 ? null : machine.getAllRegisters().get(0));
        return new Increment("???", machine, r, ConditionBit.none(), ConditionBit.none(), 1L);
    }

    /**
     * returns a string about the type of the 
     * @return a string about the type of the 
     */
    @Override
    public String toString()
    {
        return "Increment";
    }
    
    @Override
    public void updateMachineFromItems() {
        machine.setMicros(Increment.class, getItems());
    }

    /**
     * returns true if new micros of this class can be created.
     * @return true if new micros of this class can be created.
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
        return "Increment";
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