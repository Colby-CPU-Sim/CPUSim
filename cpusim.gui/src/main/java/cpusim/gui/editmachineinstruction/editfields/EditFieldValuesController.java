package cpusim.gui.editmachineinstruction.editfields;

import cpusim.gui.util.MachineModificationController;
import cpusim.gui.util.NamedColumnHandler;
import cpusim.gui.util.table.EditingLongCell;
import cpusim.gui.util.table.EditingStrCell;
import cpusim.model.Field;
import cpusim.model.FieldValue;
import cpusim.model.Machine;
import cpusim.model.util.NamedObject;
import cpusim.model.util.ValidationException;
import cpusim.util.Dialogs;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * FXML Controller class
 *
 * @since 2013-06-04
 */
public class EditFieldValuesController implements MachineModificationController {

    private final ObjectProperty<Machine> machine;

    private ObservableList<FieldValue> allFieldValues;
    
    private Stage stage;
    
    @FXML BorderPane mainPane;
    
    @FXML
    private TableView<FieldValue> table;
    @FXML
    private TableColumn<FieldValue, String> name;
    @FXML
    private TableColumn<FieldValue, Long> value;
    
    @FXML
    private Button delete;
    @FXML
    private Button duplicate;
    
    private FieldValue selectedFieldValueName;
    private Field field;

    /**
     * constructor
     */
    public EditFieldValuesController(Field f, Stage stage) {
        this.field = f;
        
        this.stage = stage;
        
        allFieldValues = field.getValues();

        machine = new SimpleObjectProperty<>(this, "machine", null);
    }

    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
//        name.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(.5));
//        value.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(.5));
        
        Callback<TableColumn<FieldValue, String>,
                TableCell<FieldValue, String>> cellStrFactory =
                setStringTableColumn -> new EditingStrCell<>();
        Callback<TableColumn<FieldValue,Long>,
                TableCell<FieldValue,Long>> cellLongFactory =
                setIntegerTableColumn -> new EditingLongCell<>();

        name.setCellValueFactory(
                new PropertyValueFactory<>("name"));
        value.setCellValueFactory(
                new PropertyValueFactory<>("value"));
        
        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(new NamedColumnHandler<>(table));

        value.setCellFactory(cellLongFactory);
        value.setOnEditCommit(
                text -> text.getRowValue().setValue(text.getNewValue())
        );
        
        table.getSelectionModel().selectedItemProperty().addListener((ov, t, t1) -> {
            delete.setDisable(false);
            duplicate.setDisable(false);
            selectedFieldValueName = t1;
        });
                
        table.setItems(allFieldValues);
    }

    @Override
    public ObjectProperty<Machine> machineProperty() {
        return this.machine;
    }

    /**
     * creates a new field name with a unique name based on '?'
     * @param ae unused action event
     */
    @FXML @SuppressWarnings("unused")
    protected void handleNew(ActionEvent ae){
        String uniqueName = NamedObject.createUniqueName(table.getItems());
        allFieldValues.add(0, new FieldValue(uniqueName, UUID.randomUUID(), getMachine(), 0));
        table.scrollTo(0);
        table.getSelectionModel().selectFirst();
    }
    
    /**
     * duplicates the currently selected field value giving it the name of the selected field value
     * with 'copy' tagged on at the end
     * @param ae unused action event
     */
    @FXML
    protected void handleDuplicate(ActionEvent ae){
        String newName = selectedFieldValueName.getName()+"_copy";
        int i = 2;
        while (fieldValueNameTaken(newName)){
            newName = selectedFieldValueName.getName()+"_copy"+i;
            i++;
        }
        allFieldValues.add(0, new FieldValue(newName, UUID.randomUUID(),
                getMachine(), selectedFieldValueName.getValue()));
        table.scrollTo(0);
        table.getSelectionModel().selectFirst();
    }
    
    /**
     * deletes the currently selected field value
     * @param ae unused action event
     */
    @FXML
    protected void handleDelete(ActionEvent ae){
        int index = allFieldValues.indexOf(selectedFieldValueName);
        allFieldValues.remove(selectedFieldValueName);
        
        if (allFieldValues.isEmpty()){
            delete.setDisable(true);
            duplicate.setDisable(true);
        }
        
        if (index == 0){
            table.getSelectionModel().select(index);
        }
        else{
            table.getSelectionModel().select(index-1);
        }
    }
    
    /**
     * saves changes if changes are valid and then closes the window
     * @param ae unused action event
     */
    @FXML
    protected void handleOkay(ActionEvent ae){
        try {
            checkValidity();
        }
        catch (ValidationException ex) {
            Dialogs.createErrorDialog(stage, "Field Value Error",
                    ex.getMessage()).showAndWait();
            return;
        }
        field.setValues(allFieldValues);
        stage.close();
    }
    
    /**
     * closes the window without saving any changes
     * @param ae unused action event
     */
    @FXML
    protected void handleCancel(ActionEvent ae){

        // just close window.
        stage.close();
    }
    
    /**
     * checks if the changes made are valid and can be saved
     * @return true if the changes are valid, else false
     */
    private boolean isAbleToClose(){
        
        List<String> fieldNames = new ArrayList<>();
        for (FieldValue fieldValue : allFieldValues){
            if (fieldValue.getName().contains(" ")){
                Dialogs.createErrorDialog(stage, "Field Name Error",
                        "Field name '"+fieldValue.getName()+"' is not valid.").showAndWait();

                return false;
            }
            if (fieldNames.contains(fieldValue.getName())){
                Dialogs.createErrorDialog(stage, "Field Name Error",
                        "You cannot have two fields with the "
                                + "same name ("+fieldValue.getName()+")").showAndWait();

                return false;
            }
            fieldNames.add(fieldValue.getName());
        }
        return true;
    }


    @Override
    public void updateMachine() {

    }

    @Override
    public void checkValidity() {
        Field.validateFieldValues(field, allFieldValues);
    }
    
    private boolean fieldValueNameTaken(String newName) {
        for (FieldValue field : allFieldValues){
            if (field.getName().equals(newName)){
                return true;
            }
        }
        return false;
    }
}
