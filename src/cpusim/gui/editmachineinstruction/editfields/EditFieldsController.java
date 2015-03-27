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

import cpusim.Field;
import cpusim.Field.Type;
import cpusim.FieldValue;
import cpusim.MachineInstruction;
import cpusim.gui.editmachineinstruction.EditMachineInstructionController;
import cpusim.gui.util.EditingLongCell;
import cpusim.gui.util.EditingNonNegativeIntCell;
import cpusim.util.Dialogs;
import cpusim.util.Validate;
import cpusim.util.ValidationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * @author Ben Borchard
 */
public class EditFieldsController implements Initializable {

    /** clone of all the Fields */
    ObservableList<Field> allFields;
    /** key = a field, value = a string with the names of all instructions using the field*/
    HashMap<Field, String> containingInstructions;
    
    ObservableList<MachineInstruction> instructions;
    
    Stage stage;
    
    @FXML BorderPane mainPane;
    
    @FXML TableView<Field> table;
    @FXML TableColumn<Field,String> name;
    @FXML TableColumn<Field,Field.Type> type;
    @FXML TableColumn<Field,Integer> numBits;
    @FXML TableColumn<Field,Long> defaultValue;
    @FXML TableColumn<Field,Field.Relativity> relativity;
    @FXML TableColumn<Field,Boolean> signed;
    
    @FXML Button deleteButton;
    @FXML Button duplicateButton;
    @FXML Button valuesButton;
    
    private Field selectedField;
    private EditMachineInstructionController editMachineInstructionController;

    /**
     * constructor
     * @param editMachineInstructionController the controller for the machine instr dialog
     * @param stage the Stage for the application
     */
    public EditFieldsController(
            EditMachineInstructionController editMachineInstructionController,
            Stage stage) {
        allFields = FXCollections.observableArrayList();
        
        instructions = FXCollections.observableArrayList();
        
        this.editMachineInstructionController = editMachineInstructionController;
        
        this.stage = stage;

        // clone all the fields to fill up allFields
        for (Field field : editMachineInstructionController.getFields()){
            List<FieldValue> realFieldValues = field.getValues();
            ObservableList<FieldValue> fieldValues = FXCollections.observableArrayList();
            for (FieldValue fieldValue : realFieldValues){
                fieldValues.add(new FieldValue(fieldValue.getName(), fieldValue.getValue()));
            }
            allFields.add(new Field(field.getName(), field.getType(), field.getNumBits(),
                    field.getRelativity(), fieldValues, field.getDefaultValue(), 
                    field.isSigned()));
        }

        // clone the machine instructions using the cloned fields
        for (MachineInstruction instr : editMachineInstructionController.getInstructions()){
            
            ArrayList<Field> oldInstrFields = instr.getInstructionFields();
            ArrayList<Field> newInstrFields = new ArrayList<>();
            for (Field field : oldInstrFields){
                for (Field aField : allFields) {
                    if (field.getName().equals(aField.getName())) {
                        newInstrFields.add(aField);
                    }
                }
            }
            
            ArrayList<Field> oldAssemblyFields = instr.getAssemblyFields();
            ArrayList<Field> newAssemblyFields = new ArrayList<>();
            for (Field field : oldAssemblyFields){
                for (Field aField : allFields) {
                    if (field.getName().equals(aField.getName())) {
                        newAssemblyFields.add(aField);
                    }
                }
            }
            
            MachineInstruction instrToAdd = new MachineInstruction(instr.getName(), instr.getOpcode(),
                newInstrFields, newAssemblyFields, instr.getInstructionColors(), instr.getAssemblyColors(),
                    editMachineInstructionController.getMachine());
            
            instrToAdd.setMicros(instr.getMicros());
            
            instructions.add(instrToAdd);
        }
        
        containingInstructions = new HashMap<>();
        
        //initialize the containingInstructions hash map
        for (Field field : allFields){
            
            //put all the instructions containing the field in a list
            ObservableList<String> instructionList = FXCollections.observableArrayList();
            for (MachineInstruction instruction : instructions){
                for (Field instrField : instruction.getInstructionFields()){
                    if (instrField.getName().equals(field.getName())){
                        instructionList.add(instruction.getName());
                        break;
                    }
                }
                for (Field instrField : instruction.getAssemblyFields()){
                    if (instrField.getName().equals(field.getName()) && 
                            !instructionList.contains(instruction.getName())){
                        instructionList.add(instruction.getName());
                        break;
                    }
                }
            }
           
            //put the instructions in the hashmap
            if (!instructionList.isEmpty()){
                String instrs = "";
                for (String instructionName : instructionList){
                    instrs += instructionName+System.lineSeparator();
                }
                containingInstructions.put(field, instrs);
            }
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        name.prefWidthProperty().bind(table.prefWidthProperty().divide(100/19.0));
        type.prefWidthProperty().bind(table.prefWidthProperty().divide(100/19.0));
        numBits.prefWidthProperty().bind(table.prefWidthProperty().divide(100/16.0));
        defaultValue.prefWidthProperty().bind(table.prefWidthProperty().divide(100/12.0));
        relativity.prefWidthProperty().bind(table.prefWidthProperty().divide(100/23.0));
        signed.prefWidthProperty().bind(table.prefWidthProperty().divide(100/11.0));
        
        AnchorPane.setTopAnchor(mainPane, 0.0);
        AnchorPane.setRightAnchor(mainPane, 0.0);
        AnchorPane.setLeftAnchor(mainPane, 0.0);
        AnchorPane.setBottomAnchor(mainPane, 0.0);
        
        selectedField = null;
        deleteButton.setDisable(true);
        duplicateButton.setDisable(true);
        valuesButton.setDisable(true);

        final ObservableList<Field.Relativity> relBoxOptions = FXCollections.observableArrayList();
        
        relBoxOptions.addAll(Field.Relativity.absolute, Field.Relativity.pcRelativePreIncr,
                Field.Relativity.pcRelativePostIncr);
        
        final ObservableList<Field.Type> typeBoxOptions = FXCollections.observableArrayList();
        
        typeBoxOptions.addAll(Field.Type.required, Field.Type.optional, Field.Type.ignored);
        
        Callback<TableColumn<Field, String>, TableCell<Field, String>> cellStrFactory =
                setStringTableColumn -> new cpusim.gui.util.EditingStrCell<>();
        Callback<TableColumn<Field,Integer>, TableCell<Field,Integer>> cellIntFactory =
                setIntegerTableColumn -> new EditingNonNegativeIntCell<>();
        Callback<TableColumn<Field,Long>, TableCell<Field,Long>> cellLongFactory =
                setLongTableColumn -> new EditingLongCell<>();
        Callback<TableColumn<Field,Field.Relativity>,
                TableCell<Field,Field.Relativity>> cellRelFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(relBoxOptions);
        Callback<TableColumn<Field,Boolean>,TableCell<Field,Boolean>> cellBoolFactory =
                booleanTableColumn -> new CheckBoxTableCell<>();
        Callback<TableColumn<Field,Field.Type>,
                TableCell<Field,Field.Type>> cellTypeFactory =
                setStringTableColumn -> new ComboBoxTableCell<>(typeBoxOptions);

        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        numBits.setCellValueFactory(new PropertyValueFactory<>("numBits"));
        defaultValue.setCellValueFactory(new PropertyValueFactory<>("defaultValue"));
        relativity.setCellValueFactory(new PropertyValueFactory<>("relativity"));
        signed.setCellValueFactory(new PropertyValueFactory<>("signed"));
        
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
                    catch (ValidationException ex) {
                        (text.getRowValue()).setName(oldName);
                    }
                    updateTable();
                }
        );
        
        type.setCellFactory(cellTypeFactory);
        type.setOnEditCommit(text -> text.getRowValue().setType(text.getNewValue()));

        numBits.setCellFactory(cellIntFactory);
        numBits.setOnEditCommit(text -> text.getRowValue().setNumBits(text.getNewValue()));

        defaultValue.setCellFactory(cellLongFactory);
        defaultValue.setOnEditCommit(text ->
                text.getRowValue().setDefaultValue(text.getNewValue()));

        relativity.setCellFactory(cellRelFactory);
        relativity.setOnEditCommit(text -> text.getRowValue().setRelativity(text.getNewValue()));

        signed.setCellFactory(cellBoolFactory);
        signed.setOnEditCommit(text -> text.getRowValue().setSigned(text.getNewValue()));

        table.getSelectionModel().selectedItemProperty().addListener((ov, t, t1) -> {
            deleteButton.setDisable(false);
            duplicateButton.setDisable(false);
            valuesButton.setDisable(false);
            selectedField = t1;
        });
                
        table.setItems(allFields);
    }
    
    /**
     * creates a new field with a unique name based off of '?'
     * @param ae unused action event
     */
    @FXML
    protected void handleNew(ActionEvent ae){
        String uniqueName = createUniqueName(table.getItems(), "?");
        Field newField = new Field(uniqueName);
        allFields.add(0, newField);
        table.scrollTo(0);
        table.getSelectionModel().selectFirst();
    }
    
    /**
     * duplicates the currently selected field to make a new field with the same name
     * but with the string 'copy' tagged onto the end
     * @param ae unused action event
     */
    @FXML
    protected void handleDuplicate(ActionEvent ae){
        String newName = selectedField.getName()+"copy";
        int i = 2;
        while (fieldNameTaken(newName)){
            newName = selectedField.getName()+"copy"+i;
            i++;
        }
        Field duplicateField = (Field) selectedField.clone();
        duplicateField.setName(newName);
        allFields.add(0, duplicateField);
        table.scrollTo(0);
        table.getSelectionModel().selectFirst();
    }

    /**
     * deletes the currently selected field
     * @param ae unused action event
     */
    @FXML
    protected void handleDelete(ActionEvent ae){
        //check if the selected field is contained in any instructions.  If so, inform the
        //user and ask him/her to confirm the delete
        if (containingInstructions.containsKey(selectedField)){
            Alert dialog = Dialogs.createConfirmationDialog(stage,
                    "Delete Field In Use?", "The following instructions contain this field:"+
                            System.lineSeparator()+containingInstructions.get(selectedField)
                            +"If you delete this field all instances of "
                            + "this field will be removed.  Are you sure you want to delete this "
                            + "field?");

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.get() == ButtonType.OK){
                for (MachineInstruction instr : instructions){
                    instr.getInstructionFields().remove(selectedField);
                    instr.getAssemblyFields().remove(selectedField);
                }
                deleteField();
            }
        }
        else{
            deleteField();
        }
    }
    
    /**
     * opens the values dialog box so that the user can edit the field values for the currently 
     * selected field
     * @param ae unused action event
     */
    @FXML
    protected void handleValues(ActionEvent ae){
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource(
                "editFieldValues.fxml"));

        Stage fieldStage = new Stage();
        Pane dialogRoot = null;
        
        EditFieldValuesController controller =
                new EditFieldValuesController(selectedField, fieldStage);
        fxmlLoader.setController(controller);

        try {
            dialogRoot = fxmlLoader.load();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        Scene dialogScene = new Scene(dialogRoot);
        fieldStage.setScene(dialogScene);
        fieldStage.initOwner(table.getScene().getWindow());
        fieldStage.initModality(Modality.WINDOW_MODAL);
        fieldStage.setTitle("Edit field values for " + selectedField.getName());
        fieldStage.show();
    }
    
    @FXML
    protected void handleOkay(ActionEvent ae){
        //get a handle to the stage.
        Stage stage = (Stage) table.getScene().getWindow();

        // check that all the editing done results in legal fields
        try {
            checkValidity(allFields);
        }
        catch(ValidationException ex) {
            Dialogs.createErrorDialog(stage, "Field Error", ex.getMessage()).showAndWait();
            return;
        }

        for (MachineInstruction instruction : instructions){
            //adds ignored instruction fields and associated colors to the assembly fields
            for (Field field : instruction.getInstructionFields()){
                if (!instruction.getAssemblyFields().contains(field) && field.getType() != Type.ignored){
                    instruction.getAssemblyFields().add(field);
                    instruction.getAssemblyColors().add(instruction.getInstructionColors().get(
                            instruction.getInstructionFields().indexOf(field)));
                }
            }
            //removes ignored instruction fields and associated colors
            for (Field field : instruction.getAssemblyFields()){
                if (field.getType() == Type.ignored){
                    instruction.getAssemblyColors().remove(instruction.getAssemblyFields().indexOf(field));
                    instruction.getAssemblyFields().remove(field);
                }
            }
        }
        
        editMachineInstructionController.setFields(allFields);
        editMachineInstructionController.setInstructions(instructions);
        stage.close();
    }
    
    /**
     * closes the window without saving any changes made
     * @param ae the ActionEvent referring to the cancel action
     */
    @FXML
    protected void handleCancel(ActionEvent ae){
        //get a handle to the stage.
        Stage stage = (Stage) table.getScene().getWindow();

        //close window.
        stage.close();
    }
    
    /**
     * checks whether everything is okay to be saved
     * @return true if everything is okay, else false
     */
    private boolean isAbleToClose() {
        ArrayList<String> fieldNames = new ArrayList<>();
        for (Field field : allFields) {
            if (field.getName().indexOf(" ") != -1) {
                Dialogs.createErrorDialog(stage,
                        "Field Name Error", "Field name '" + field.getName() + "' is " +
                                "not valid.").showAndWait();

                return false;
            }
            if (fieldNames.contains(field.getName())) {
                Dialogs.createErrorDialog(stage, "Field Name Error",
                        "You cannot have two fields with the same name (" + field
                                .getName() + ")").showAndWait();

                return false;
            }
            fieldNames.add(field.getName());
        }
        return true;
    }
    
    /**
     * deletes the selected field and selects the next appropriate field (either
     * the one below or above)
     */
    private void deleteField(){
        int index = allFields.indexOf(selectedField);
        allFields.remove(selectedField);

        if (allFields.isEmpty()) {
            deleteButton.setDisable(true);
            duplicateButton.setDisable(true);
            valuesButton.setDisable(true);
        }
        else if (index == 0) {
            table.getSelectionModel().select(index);
        }
        else {
            table.getSelectionModel().select(index - 1);
        }
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
    
    /**
     * Checks the validity of a list of Fields.
     * @param list a List of Fields to be checked for validity
     * @throws ValidationException if something is not valid
     */
    private void checkValidity(ObservableList<Field> list) {
        Validate.allNamesAreNonEmpty(list.toArray());
        Validate.allNamesAreUnique(list.toArray());
        for(Field field : list) {
            Validate.fieldIsValid(field);
        }
    }

    /**
     * checks whether the given name is unique among all the fields
     * @param newName the name to check for uniqueness
     * @return true if the name is unique.
     */
    private boolean fieldNameTaken(String newName) {
        for (Field field : allFields){
            if (field.getName().equals(newName)){
                return true;
            }
        }
        return false;
    }

    /**
     * redisplay the table so that the new values are properly shown.
     */
    private void updateTable() {
        // This is a hack.  There should be a better way to do this.
        name.setVisible(false);
        name.setVisible(true);
        defaultValue.setVisible(false);
        defaultValue.setVisible(true);
        double w =  table.getWidth();
        table.setPrefWidth(w-1);
        table.setPrefWidth(w);
    }
}
