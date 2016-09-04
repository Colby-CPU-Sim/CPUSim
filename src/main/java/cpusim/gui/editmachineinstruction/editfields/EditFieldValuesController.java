/*
 * Ben Borchard
 * 
 * Last Modified 6/4/13s
 */

/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Removed the isAbleToClose and checkValidity method
 * 2.) Removed the validation of names upon closing becuase we now do that dynamically
 * 3.) Changed the onEditCommit for the name tableColumn so that it dynamically checks 
 * the validity of the name the user gives.  It will change the invalid name to the old
 * name as soon the user enters
 * 4.) Added an updateTable method so that to allow for dynamic validity checking of the
 * name tableColumn
 */
package cpusim.gui.editmachineinstruction.editfields;

import cpusim.model.Field;
import cpusim.model.FieldValue;
import cpusim.gui.util.EditingLongCell;
import cpusim.util.Dialogs;
import cpusim.util.Validate;
import cpusim.util.ValidationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author Ben Borchard
 */
public class EditFieldValuesController implements Initializable {
    
    ObservableList<FieldValue> allFieldValues;
    
    Stage stage;
    
    @FXML BorderPane mainPane;
    
    @FXML TableView<FieldValue> table;
    @FXML TableColumn<FieldValue,String> name;
    @FXML TableColumn<FieldValue,Long> value;
    
    @FXML Button delete;
    @FXML Button duplicate;
    
    private FieldValue selectedFieldValueName;
    private Field field;

    /**
     * constructor
     */
    public EditFieldValuesController(Field f, Stage stage) {
        allFieldValues = FXCollections.observableArrayList();
        
        this.field = f;
        
        this.stage = stage;
        
        for (FieldValue fieldValue : field.getValues()){
            allFieldValues.add(new FieldValue(fieldValue.getName(), fieldValue.getValue()));
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
//        name.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(.5));
//        value.prefWidthProperty().bind(table.widthProperty().subtract(2).multiply(.5));
        
        selectedFieldValueName = null;
        delete.setDisable(true);
        duplicate.setDisable(true);
        
        Callback<TableColumn<FieldValue, String>,
                TableCell<FieldValue, String>> cellStrFactory =
                setStringTableColumn -> new cpusim.gui.util.EditingStrCell<>();
        Callback<TableColumn<FieldValue,Long>,
                TableCell<FieldValue,Long>> cellLongFactory =
                setIntegerTableColumn -> new EditingLongCell<>();

        name.setCellValueFactory(
                new PropertyValueFactory<>("name"));
        value.setCellValueFactory(
                new PropertyValueFactory<>("value"));
        
        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(
                text -> {
                    String newName = text.getNewValue();
                    String oldName = text.getOldValue();
                    ( text.getRowValue()).setName(newName);
                    try{
                        Validate.namedObjectsAreUniqueAndNonempty(table.getItems().toArray());
                    }
                    catch(ValidationException ex) {
                        (text.getRowValue()).setName(oldName);
                    }
                    updateTable();
                }
        );

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
    
    /**
     * creates a new field name with a unique name based on '?'
     * @param ae unused action event
     */
    @FXML
    protected void handleNew(ActionEvent ae){
        String uniqueName = createUniqueName(table.getItems(), "?");
        allFieldValues.add(0, new FieldValue(uniqueName, 0));
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
        String newName = selectedFieldValueName.getName()+"copy";
        int i = 2;
        while (fieldValueNameTaken(newName)){
            newName = selectedFieldValueName.getName()+"copy"+i;
            i++;
        }
        allFieldValues.add(0, new FieldValue(newName, 
                selectedFieldValueName.getValue()));
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
            Validate.fieldValuesAreValid(field, allFieldValues);
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
        
        ArrayList<String> fieldNames = new ArrayList<>();
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
    
    /**
     * returns a String that is different from all names of
     * existing objects in the given list.  It checks whether proposedName
     * is unique and if so, it returns it.  Otherwise, it
     * proposes a new name of proposedName + "?" and tries again.
     *
     * @param list list of existing objects
     * @param proposedName a given proposed name
     * @return the unique name
     */
    public String createUniqueName(ObservableList list, String proposedName)
    {
        String oldName;
        for (Object obj : list) {
            oldName = obj.toString();
            if (oldName != null && oldName.equals(proposedName))
                return createUniqueName(list, proposedName + "?");
        }
        return proposedName;
    }
    
    private boolean fieldValueNameTaken(String newName) {
        for (FieldValue field : allFieldValues){
            if (field.getName().equals(newName)){
                return true;
            }
        }
        return false;
    }
    
    private void updateTable() {
        name.setVisible(false);
        name.setVisible(true);
        double w =  table.getWidth();
        table.setPrefWidth(w-1);
        table.setPrefWidth(w);
    }
}
