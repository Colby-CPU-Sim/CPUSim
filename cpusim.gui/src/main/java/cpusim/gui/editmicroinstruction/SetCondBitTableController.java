package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.model.microinstruction.SetCondBit;
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
 * The controller for editing the Branch command in the EditMicroDialog.
 *
 * @author Jinghui Yu
 * @author Michael Goldenberg
 * @author Ben Borchard
 * @author Kevin Brightwell (Nava2)
 *
 * @since 2013-06-07
 */
class SetCondBitTableController extends MicroController<SetCondBit> implements Initializable {

    @FXML @SuppressWarnings("unused")
    private TableColumn<SetCondBit,String> name;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<SetCondBit,ConditionBit> bit;
    
    @FXML @SuppressWarnings("unused")
    private TableColumn<SetCondBit,String> value;
    

    /**
     * Constructor
     * @param mediator the mediator used to store the machine
     */
    SetCondBitTableController(Mediator mediator){
        super(mediator, "SetCondBitTable.fxml", SetCondBit.class);
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
        name.prefWidthProperty().bind(prefWidthProperty().divide(100/34.0));
        bit.prefWidthProperty().bind(prefWidthProperty().divide(100/33.0));
        value.prefWidthProperty().bind(prefWidthProperty().divide(100/33.0));

        Callback<TableColumn<SetCondBit,String>,TableCell<SetCondBit,String>> cellStrFactory =
                setStringTableColumn -> new cpusim.gui.util.EditingStrCell<>();
        Callback<TableColumn<SetCondBit,String>,TableCell<SetCondBit,String>> cellIntFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(
                        FXCollections.observableArrayList(
                                "0",
                                "1"
                        ));
        Callback<TableColumn<SetCondBit,ConditionBit>,TableCell<SetCondBit,ConditionBit>> cellCondFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(machine.getModule("conditionBits", ConditionBit.class));

        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        bit.setCellValueFactory(new PropertyValueFactory<>("bit"));
        value.setCellValueFactory(new PropertyValueFactory<>("value"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(new NameColumnHandler());

        bit.setCellFactory(cellCondFactory);
        bit.setOnEditCommit(
                text -> text.getRowValue().setBit(
                        text.getNewValue())
        );

        value.setCellFactory(cellIntFactory);
        value.setOnEditCommit(
                text -> text.getRowValue().setValue(
                        text.getNewValue())
        );
    }
    
    @Override
    public SetCondBit getPrototype()
    {
        ConditionBit cBit = (machine.getModule("conditionBits").size() == 0 ?
                null : machine.getModule("conditionBits", ConditionBit.class).get(0));
        return new SetCondBit("???", machine, cBit, "0");
    }

    /**
     * returns a string about the type of the 
     * @return a string about the type of the 
     */
    @Override
    public String toString()
    {
        return "SetCondBit";
    }
    
    @Override
    public void updateMachineFromItems()
    {
        machine.setMicros(SetCondBit.class, getItems());
    }


    @Override
    public void checkValidity(ObservableList<SetCondBit> micros)
    {
        super.checkValidity(micros);
        
        for (SetCondBit micro: micros) {
            Register.validateIsNotReadOnly(micro.getBit().getRegister(), micro.getName());
        }
    }

    @Override
    public boolean newMicrosAreAllowed()
    {
        return (machine.getModule("conditionBits").size() > 0);
    }

   
    @Override
    public String getHelpPageID()
    {
        return "SetCondBit";
    }

    @Override
    public void updateTable()
    {
        name.setVisible(false);
        name.setVisible(true);
        
        super.updateTable();
    }
}
