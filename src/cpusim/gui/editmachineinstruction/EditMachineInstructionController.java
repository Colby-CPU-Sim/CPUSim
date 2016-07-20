/*
 * Ben Borchard
 * 
 * Last Modified 6/4/13s
 */

/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) removed the isAbleToClose and checkValidity method
 * 2.) uses the validate method for nameable objects to validate the list before
 * closing instead of the isAbleToClose method
 * 3.) added the ability to dynamically check the validity of the opcodes as the user
 * inputs
 * them by calling two methods in the Validate class after there is a change event on the
 * opcode textField
 * 
 * on 11/25/13
 * 
 * 1.) added the comment microinstruction to the microInstrTreeView on the end
 * 2.) Added the functionality of the comment micro within the implementation format pane
 * by changing drag drop events and double clicking events on labels within the
 * implementation
 * format pane and the implementation format pane itself
 */
package cpusim.gui.editmachineinstruction;

import cpusim.*;
import cpusim.Field.Type;
import cpusim.gui.editmachineinstruction.editfields.EditFieldsController;
import cpusim.gui.help.HelpController;
import cpusim.gui.util.DragTreeCell;
import cpusim.microinstruction.Comment;
import cpusim.util.Convert;
import cpusim.util.Dialogs;
import cpusim.util.Validate;
import cpusim.util.ValidationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * FXML Controller class
 *
 * @author Ben Borchard
 */
public class EditMachineInstructionController {

    @FXML
    ListView<String> instructionList;

    @FXML
    TextField opcodeTextField;

    @FXML
    AnchorPane instructionFormatPane;
    @FXML
    AnchorPane assemblyFormatPane;
    @FXML
    AnchorPane fieldPane;
    @FXML
    BorderPane mainPane;
    @FXML
    AnchorPane implementationFormatPane;
    @FXML
    ScrollPane implementationFormatScrollPane;

    @FXML
    VBox fieldsList;
    @FXML
    Label lengthLabel;
    @FXML
    VBox noFieldsLabel;
    @FXML
    Button newButton;
    @FXML
    Button dupButton;
    @FXML
    Button deleteButton;
    @FXML
    TreeView microInstrTreeView;

    Mediator mediator;
    MachineInstruction currentInstr;
    Field draggingField;
    String draggingColor;
    Pane originPane;
    Microinstruction currentCommentMicro;
    TextField commentEditor;
    List<MachineInstruction> instructions;
    ObservableList<Field> allFields;
    boolean dragging;
    boolean dropped;
    boolean exited;

    int draggingIndex;
    int currInstrIndex;
    ObservableList<String> instrNames;
    ObservableList<String> fieldNames;

    public EditMachineInstructionController(Mediator mediator) {
        this.mediator = mediator;
        currentInstr = null;
        dragging = false;
    }

    /**
     * Initializes the controller class.
     */
    public void initialize() {

        //initialize fields
        currInstrIndex = -1;

        instrNames = FXCollections.observableArrayList();

        fieldNames = FXCollections.observableArrayList();

        initializeInstructionsAndFields();

        initializeInstructionList();

        setUpMicroTreeView();

        initializeOpcodeTextField();

        dupButton.setDisable(true);
        deleteButton.setDisable(true);

        lengthLabel.setText("");
        noFieldsLabel.setVisible(false);

        updateFieldNames();

        mainPane.widthProperty().addListener((ov, t, t1) -> {
            updateInstructionDisplay();
            updateAssemblyDisplay();
        });

        microInstrTreeView.setOnMousePressed(t -> {
            if (commentEditor != null) {
                commitCommentEdit();
            }
        });

        // set up listeners for the panes for drag & drop
        initializeInstructionFormatPane();
        initializeAssemblyFormatPane();
        initializeImplementationFormatPane();
    }

    private void initializeOpcodeTextField() {

        opcodeTextField.setDisable(true);

        opcodeTextField.textProperty().addListener((observable, oldValue, newValue) -> {

            try {
                Validate.stringIsLegalBinHexOrDecNumber(newValue);
                long newOpcode = Convert.fromAnyBaseStringToLong(newValue);
                currentInstr.setOpcode(newOpcode);
                Validate.instructionsOpcodeIsValid(instructions, currentInstr);
                opcodeTextField.setStyle("-fx-border-width:1;" +
                        "-fx-background-color:white; -fx-border-color:black; " +
                        "-fx-border-style:solid;");
                opcodeTextField.setTooltip(new Tooltip("Binary: " +
                        Long.toBinaryString(newOpcode) +
                        System.getProperty("line.separator") + "Decimal: " +
                        newOpcode +
                        System.getProperty("line.separator") + "Hex: " +
                        Long.toHexString(newOpcode)));
            } catch (ValidationException ex) {
                opcodeTextField.setStyle("-fx-border-width:1;" +
                        "-fx-background-color:red; -fx-border-color:black; " +
                        "-fx-border-style:solid;");
                opcodeTextField.setTooltip(new Tooltip(ex.getMessage()));
            }
        });
    }


    /**
     * Initializes drag events for the instructionFormatPane.
     */
    private void initializeInstructionFormatPane() {
        implementationFormatPane.setOnMousePressed(t -> {
            if (commentEditor != null) {
                commitCommentEdit();
            }
        });

        instructionFormatPane.setOnDragOver(event -> {
            if (currentInstr != null && !originPane.equals(assemblyFormatPane) &&
                    draggingField.getNumBits() != 0) {
                event.acceptTransferModes(TransferMode.COPY);

                double localX = instructionFormatPane.sceneToLocal(event.getSceneX
                        (), event.getSceneY()).getX();

                if (!currentInstr.getInstructionFields().isEmpty()) {
                    int index = getInstrFieldIndex(localX);
                    insertInstrField(draggingField, index);
                }
                dropped = false;


                event.consume();
            }
        });
        instructionFormatPane.setOnDragDropped(event -> {
            /* data dropped */
            /* if there is a string data on drag board, read it and use it */
            if (currentInstr != null && !originPane.equals(assemblyFormatPane) &&
                    draggingField.getNumBits() != 0) {
                double localX = instructionFormatPane.sceneToLocal(event.getSceneX
                        (), event.getSceneY()).getX();
                int index = getInstrFieldIndex(localX);
                currentInstr.getInstructionFields().add(index, draggingField);
                currentInstr.getInstructionColors().add(index, draggingColor);

                if (draggingField.getType() != Type.ignored) {
                    currentInstr.getAssemblyFields().add(draggingIndex,
                            draggingField);
                    currentInstr.getAssemblyColors().add(draggingIndex,
                            draggingColor);
                }
                /* let the source know whether the string was successfully
                 * transferred and used */
                event.setDropCompleted(true);
                dropped = true;

                event.consume();
            }
        });
        instructionFormatPane.setOnDragExited(event -> {
            if (currentInstr != null && !originPane.equals(assemblyFormatPane) &&
                    draggingField.getNumBits() != 0) {
                updateInstructionDisplay();
                updateAssemblyDisplay();
                event.consume();
            }
        });
    }

    /**
     * Initializes drag events for the assemblyFormatPane.
     */
    private void initializeAssemblyFormatPane() {
        assemblyFormatPane.setOnDragOver(event -> {
            if (currentInstr != null) {
                if (originPane.equals(assemblyFormatPane) || draggingField
                        .getNumBits() == 0) {
                    event.acceptTransferModes(TransferMode.COPY);
                    double localX = assemblyFormatPane.sceneToLocal(event.getSceneX
                            (), event.getSceneY()).getX();
                    int index = getAssemblyFieldIndex(localX);
                    insertAssemblyField(draggingField, index);
                    event.consume();
                    dropped = false;
                }

            }
        });
        assemblyFormatPane.setOnDragDropped(event -> {
            /* data dropped */
            /* if there is a string data on dragboard, read it and use it */
            if (currentInstr != null) {
                if (originPane.equals(assemblyFormatPane) || draggingField
                        .getNumBits() == 0) {
                    double localX = assemblyFormatPane.sceneToLocal(event.getSceneX
                            (), event.getSceneY()).getX();
                    int index = getAssemblyFieldIndex(localX);

                    currentInstr.getAssemblyFields().add(index, draggingField);
                    currentInstr.getAssemblyColors().add(index, draggingColor);

                    /* let the source know whether the string was successfully
                     * transferred and used */
                    event.setDropCompleted(true);
                    dropped = true;
                    exited = true;

                    event.consume();
                }
            }
        });
        assemblyFormatPane.setOnDragExited(event -> {
            if (currentInstr != null) {
                if (originPane.equals(assemblyFormatPane) || draggingField
                        .getNumBits() == 0) {
                    if (!dropped && draggingField.getNumBits() != 0) {
                        currentInstr.getAssemblyFields().add(draggingIndex,
                                draggingField);
                        currentInstr.getAssemblyColors().add(draggingIndex,
                                draggingColor);
                        exited = true;
                    }
                    updateAssemblyDisplay();
                    event.consume();
                }
            }
        });
        assemblyFormatPane.setOnDragEntered(event -> {
            if (currentInstr != null) {
                if (originPane.equals(assemblyFormatPane) || draggingField
                        .getNumBits() == 0) {
                    //so there aren't duplicate fields added whenever the user drags
                    //out of the field and back in again
                    if (!dropped && draggingField.getNumBits() != 0 && exited &&
                            !currentInstr.getAssemblyFields().isEmpty() &&
                            !currentInstr.getInstructionFields().isEmpty()) {
                        currentInstr.getAssemblyFields().remove(draggingIndex);
                        currentInstr.getAssemblyColors().remove(draggingIndex);
                        exited = false;
                    }
                    updateAssemblyDisplay();
                    event.consume();
                }
            }
        });
    }

    /**
     * Initializes drag events for the implementationFormatPane.
     */
    private void initializeImplementationFormatPane() {
        implementationFormatPane.setOnDragOver(event -> {
            if (currentInstr != null) {
                event.acceptTransferModes(TransferMode.COPY);
                double localY = implementationFormatPane.sceneToLocal(event
                        .getSceneX(), event.getSceneY()).getY();
                int index = getMicroinstrIndex(localY);
                moveMicrosToMakeRoom(index);
            }
        });
        implementationFormatPane.setOnDragDropped(event -> {
            /* data dropped */
            /* if there is a string data on dragboard, read it and use it */
            if (currentInstr != null) {
                Dragboard db = event.getDragboard();
                int lastComma = db.getString().lastIndexOf(",");
                String microName = db.getString().substring(0, lastComma);
                String className = db.getString().substring(lastComma + 1);
                Microinstruction micro = null;

                for (String string : Machine.MICRO_CLASSES) {
                    for (Microinstruction instr : mediator.getMachine().getMicros
                            (string)) {
                        if (instr.getName().equals(microName) && instr
                                .getMicroClass().equals(className)) {
                            micro = instr;
                        }
                    }
                }
                if (className.equals("comment")) {
                    // we want Comment micros used in only one place so create a new one
                    micro = new Comment();
                    micro.setName(microName);
                }
                double localY = implementationFormatPane.sceneToLocal(event
                        .getSceneX(), event.getSceneY()).getY();
                int index = getMicroinstrIndex(localY);

                currentInstr.getMicros().add(index, micro);

            }
        });
        implementationFormatPane.setOnDragExited(event -> {
            if (currentInstr != null) {
                updateMicros();
            }
        });
    }

    /**
     * Takes the real fields and instructions and makes copies of them that will be
     * manipulated in this dialog.  These copies will be made real if the okay button
     * is hit, if cancel is hit, they will be garbage collected without being saved to
     * the machine.
     */
    private void initializeInstructionsAndFields() {
        List<MachineInstruction> realInstructions = mediator.getMachine()
                .getInstructions();
        instructions = new ArrayList<>();

        List<Field> realAllFields = mediator.getMachine().getFields();
        allFields = FXCollections.observableArrayList();

        for (Field field : realAllFields) {
            List<FieldValue> realFieldValues = field.getValues();
            ObservableList<FieldValue> fieldValues = FXCollections.observableArrayList();
            for (FieldValue fieldValue : realFieldValues) {
                fieldValues.add(new FieldValue(fieldValue.getName(), fieldValue
                        .getValue()));
            }
            allFields.add(new Field(field.getName(), field.getType(), field.getNumBits(),
                    field.getRelativity(), fieldValues, field.getDefaultValue(),
                    field.isSigned()));
        }

        for (MachineInstruction instr : realInstructions) {

            ArrayList<Field> oldInstrFields = instr.getInstructionFields();
            ArrayList<Field> newInstrFields = new ArrayList<>();
            for (Field oldField : oldInstrFields) {
                for (Field newField : allFields) {
                    if (oldField.getName().equals(newField.getName())) {
                        newInstrFields.add(newField);
                    }
                }
            }

            ArrayList<Field> oldAssemblyFields = instr.getAssemblyFields();
            ArrayList<Field> newAssemblyFields = new ArrayList<>();
            for (Field oldField : oldAssemblyFields) {
                for (Field newField : allFields) {
                    if (oldField.getName().equals(newField.getName())) {
                        newAssemblyFields.add(newField);
                    }
                }
            }


            ArrayList<String> oldInstrColors = instr.getInstructionColors();
            ArrayList<String> newInstrColors = new ArrayList<>();
            for (String colors : oldInstrColors) {
                newInstrColors.add(colors);
            }

            ArrayList<String> oldAssemblyColors = instr.getAssemblyColors();
            ArrayList<String> newAssemblyColors = new ArrayList<>();
            for (String colors : oldAssemblyColors) {
                newAssemblyColors.add(colors);
            }

            MachineInstruction instrToAdd = new MachineInstruction(instr.getName(),
                    instr.getOpcode(),
                    newInstrFields, newAssemblyFields, newInstrColors, newAssemblyColors,
                    mediator.getMachine());

            ObservableList<Microinstruction> newMicros = FXCollections
                    .observableArrayList(
                            new ArrayList<>());
            ObservableList<Microinstruction> oldMicros = instr.getMicros();
            for (Microinstruction micro : oldMicros) {
                newMicros.add(micro);
            }
            instrToAdd.setMicros(newMicros);

            instructions.add(instrToAdd);
        }
    }

    /**
     * Initializes the instruction list by making it editable, setting its items,
     * and giving it a change listener
     */
    private void initializeInstructionList() {

        instructionList.setItems(instrNames);

        for (MachineInstruction instr : instructions) {
            instrNames.add(instr.getName());
        }

        Callback<ListView<String>, ListCell<String>> cellStrFactory =
                list -> new cpusim.gui.util.EditingStrListCell();
        instructionList.setCellFactory(cellStrFactory);

        instructionList.getSelectionModel().selectedIndexProperty().addListener(
                (ov, value, new_value) -> {

                    dupButton.setDisable(false);
                    deleteButton.setDisable(false);
                    opcodeTextField.setDisable(false);
                    noFieldsLabel.setVisible(true);

                    currInstrIndex = new_value.intValue();

                    updateInstrNames();

                    if (!instructions.isEmpty() && new_value.intValue() != -1) {
                        currentInstr = instructions.get(new_value.intValue());

                        int numOpcodeBits;
                        if (currentInstr.getOpcode() != 0) {
                            numOpcodeBits = 64 - Long.numberOfLeadingZeros
                                    (currentInstr.getOpcode());
                        } else {
                            numOpcodeBits = 1;
                        }
                        opcodeTextField.setText("0x" + Convert
                                .fromLongToHexadecimalString(
                                        currentInstr.getOpcode(), numOpcodeBits));

                    }
                    updateMicros();
                    updateInstructionDisplay();
                    updateAssemblyDisplay();
                });
    }

    /**
     * opens the edit fields dialog when the user presses the edit fields button
     *
     * @param ae unused action event
     */
    @FXML
    protected void handleEditFields(ActionEvent ae) {
        FXMLLoader fxmlLoader = new FXMLLoader(mediator.getClass().getResource(
                "gui/editmachineinstruction/editfields/editFields.fxml"));

        final Stage fieldStage = new Stage();
        Pane dialogRoot = null;

        EditFieldsController controller = new EditFieldsController(this, fieldStage);
        fxmlLoader.setController(controller);

        try {
            dialogRoot = fxmlLoader.load();
        } catch (IOException e) {
            // should never happen
            assert false : "Unable to load file: " +
                    "gui/editmachineinstruction/editfields/editFields.fxml";
        }
        Scene dialogScene = new Scene(dialogRoot);
        fieldStage.setScene(dialogScene);
        fieldStage.initOwner(lengthLabel.getScene().getWindow());
        fieldStage.initModality(Modality.WINDOW_MODAL);
        fieldStage.setTitle("Edit Fields");
        dialogScene.addEventFilter(
                KeyEvent.KEY_RELEASED, event -> {
                    if (event.getCode().equals(KeyCode.ESCAPE)) {
                        if (fieldStage.isFocused()) {
                            fieldStage.close();
                        }
                    }
                });
        fieldStage.show();
    }

    /**
     * creates a new instruction with the first opcode that is not used and '?'
     * as a name
     *
     * @param ae unused ActionEvent
     */
    @FXML
    protected void handleNewInstruction(ActionEvent ae) {
        int opcode = getUniqueOpcode();
        String uniqueName = createUniqueName(instructionList.getItems(), "?");
        instructions.add(0, new MachineInstruction(uniqueName, opcode, new
                ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                mediator.getMachine()));
        instrNames.add(0, uniqueName);
        //instructionList.scrollTo(0);
        //instructionList.getSelectionModel().selectFirst();
    }

    /**
     * duplicates the selected instruction
     *
     * @param ae unused action event
     */
    @FXML
    protected void handleDuplicateInstruction(ActionEvent ae) {
        int opcode = getUniqueOpcode();
        String newName = currentInstr.getName() + "copy";
        int i = 2;
        while (instrNames.contains(newName)) {
            newName = currentInstr.getName() + "copy" + i;
            i++;
        }
        ArrayList<Field> instrFieldsCopy = new ArrayList<>();
        for (Field micro : currentInstr.getInstructionFields()) {
            instrFieldsCopy.add(micro);
        }
        ArrayList<Field> assembFieldsCopy = new ArrayList<>();
        for (Field micro : currentInstr.getAssemblyFields()) {
            assembFieldsCopy.add(micro);
        }
        ArrayList<String> instrColorCopy = new ArrayList<>();
        for (String micro : currentInstr.getInstructionColors()) {
            instrColorCopy.add(micro);
        }
        ArrayList<String> assembColorCopy = new ArrayList<>();
        for (String micro : currentInstr.getAssemblyColors()) {
            assembColorCopy.add(micro);
        }

        MachineInstruction newMI = new MachineInstruction(newName, opcode,
                instrFieldsCopy, assembFieldsCopy,
                instrColorCopy, assembColorCopy, currentInstr.getMachine());
        ObservableList<Microinstruction> microCopies = FXCollections
                .observableArrayList();
        for (Microinstruction micro : currentInstr.getMicros()) {
            if( micro instanceof Comment) {
                // create a clone of it since we want each Comment used only one place
                String contents = micro.getName();
                micro = new Comment();
                micro.setName(contents);
            }
            microCopies.add(micro);
        }
        newMI.setMicros(microCopies);
        instructions.add(0, newMI);
        instructionList.scrollTo(0);
        instrNames.add(0, newName);
        instructionList.getSelectionModel().select(0);
    }

    /**
     * deletes the selected instruction
     *
     * @param ae unused action event
     */
    @FXML
    protected void handleDeleteInstruction(ActionEvent ae) {
        int indx = instrNames.indexOf(currentInstr.getName());
        instructions.remove(currentInstr);
        instrNames.remove(currentInstr.getName());
        if (indx != 0) {
            instructionList.getSelectionModel().select(indx - 1);
        } else {
            instructionList.getSelectionModel().select(indx + 1);
            instructionList.getSelectionModel().select(indx);
            if (instructions.size() != 0) {
                currentInstr = instructions.get(indx);
            } else {
                currentInstr = null;
                dupButton.setDisable(true);
                deleteButton.setDisable(true);
                opcodeTextField.setDisable(true);
                opcodeTextField.setText("");
                lengthLabel.setText("");
                noFieldsLabel.setVisible(false);
                instructionFormatPane.getChildren().clear();
            }
        }
    }

    /**
     * closes the window without saving any of the changes made
     *
     * @param ae unused action event
     */
    @FXML
    protected void handleCancel(ActionEvent ae) {
        ((Stage) newButton.getScene().getWindow()).close();
    }

    /**
     * saves the changes made and then closes the window.  If something is wrong
     * there will be a error dialog box and the window will not close not will the changes
     * be saved
     *
     * @param ae unused action event
     */
    @FXML
    protected void handleOkay(ActionEvent ae) {
        updateInstrNames();
        try {
            Validate.machineInstructions(instructions, mediator.getMachine());
            if (commentEditor != null) {
                commitCommentEdit();
            }
            mediator.getMachine().setInstructions(instructions);
            mediator.getMachine().setFields(allFields);
            mediator.setMachineDirty(true);
            ((Stage) newButton.getScene().getWindow()).close();
        } catch (ValidationException ex) {
            Dialogs.createErrorDialog(newButton.getScene().getWindow(),
                    "Machine Instruction Error", ex.getMessage()).showAndWait();
        }
    }

    /**
     * opens the help dialog to the machine instruction dialog help section
     *
     * @param ae unused ActionEvent
     */
    @FXML
    protected void handleHelp(ActionEvent ae) {
        String startString = "Machine Instruction Dialog";
        if (mediator.getDesktopController().getHelpController() == null) {
            HelpController helpController = HelpController.openHelpDialog(
                    mediator.getDesktopController(), startString);
            mediator.getDesktopController().setHelpController(helpController);
        } else {
            HelpController hc = mediator.getDesktopController().getHelpController();
            hc.getStage().toFront();
            hc.selectTreeItem(startString);
        }
    }

    /**
     * using the place of the mouse on the screen, returns the index at which the
     * dragged item should be placed in the instruction fields list if dropped
     *
     * @param localX the position of the mouse from the perspective of the instruction
     *               format pane
     * @return the index at which the dragged item should be placed if dropped
     */
    private int getInstrFieldIndex(double localX) {


        ArrayList<Double> cutoffXLocs = new ArrayList<>();
        cutoffXLocs.add(0.0);
        for (int i = 0; i < instructionFormatPane.getChildren().size() - 1; i += 2) {
            cutoffXLocs.add(instructionFormatPane.getChildren().get(i).getLayoutX() +
                    ((Label) instructionFormatPane.getChildren().get(i)).getPrefWidth()
                            * .5);
        }
        cutoffXLocs.add(instructionFormatPane.getPrefWidth());

        for (int i = 0; i < cutoffXLocs.size() - 1; i++) {
            if (localX > cutoffXLocs.get(i) && localX < cutoffXLocs.get(i + 1)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * using the place of the mouse on the screen, returns the index at which the
     * dragged field should be placed in the assembly field list if dropped
     *
     * @param localX the position of the mouse from the perspective of the assembly
     *               format pane
     * @return the index at which the dragged item should be placed if dropped
     */
    private int getAssemblyFieldIndex(double localX) {


        ArrayList<Double> cutoffXLocs = new ArrayList<>();
        cutoffXLocs.add(0.0);
        for (int i = 0; i < assemblyFormatPane.getChildren().size(); i += 1) {
            cutoffXLocs.add(assemblyFormatPane.getChildren().get(i).getLayoutX() +
                    ((Label) assemblyFormatPane.getChildren().get(i)).getPrefWidth() *
                            .5);
        }
        cutoffXLocs.add(assemblyFormatPane.getPrefWidth());

        for (int i = 0; i < cutoffXLocs.size() - 1; i++) {
            if (localX > cutoffXLocs.get(i) && localX < cutoffXLocs.get(i + 1)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * uses the current instruction's fields to update the display for the instruction
     * format pane
     */
    private void updateInstructionDisplay() {

        // set the preferred width of the instr and assm panes in case
        // the width of the dialog changed
        assemblyFormatPane.setPrefWidth(mainPane.getWidth()
                - instructionList.getWidth() - fieldsList.getWidth() - 24);
        // 24 = sum of padding of instr list & format tab pane & field's VBox
        instructionFormatPane.setPrefWidth(mainPane.getWidth()
                - instructionList.getWidth() - fieldsList.getWidth() - 24);

        if( currentInstr == null ) {
            return;  // that's all we want to do
        }

        instructionFormatPane.getChildren().clear();

        List<Field> fields = currentInstr.getInstructionFields();
        int totalBits = 0;
        for (Field field : fields) {
            totalBits += field.getNumBits();
        }

        updateLengthLabel();

        int curX = 0;
        int i = 0;
        for (Field field : fields) {
            final Label fieldName = new Label();
            final Label fieldWidth = new Label();


            fieldName.setText(field.getName());
            fieldWidth.setText(String.valueOf(field.getNumBits()));

            fieldName.setPrefWidth(((float) field.getNumBits() / totalBits) *
                    instructionFormatPane.getPrefWidth());
            fieldWidth.setPrefWidth(30);

            fieldName.setLayoutY(30);
            fieldWidth.setLayoutY(0);

            fieldName.setPrefHeight(25);
            fieldWidth.setPrefHeight(25);

            fieldWidth.setStyle("-fx-background-color:" + currentInstr
                    .getInstructionColors().get(i) + ";");
            fieldName.setStyle("-fx-background-color:" + currentInstr
                    .getInstructionColors().get(i) + ";");

            fieldWidth.setAlignment(Pos.CENTER);
            fieldName.setAlignment(Pos.CENTER);

            fieldName.setLayoutX(curX);
            fieldWidth.setLayoutX(curX + (.5 * fieldName.getPrefWidth()) - 15);

            curX += fieldName.getPrefWidth();

            fieldName.setOnDragDetected(event -> {
                /* drag was detected, start a drag-and-drop gesture*/
                /* allow any transfer mode */

                Dragboard db = fieldName.startDragAndDrop(TransferMode.ANY);

                int index = instructionFormatPane.getChildren().indexOf(fieldName)
                        / 2;

                ArrayList<Field> fields1 = currentInstr.getInstructionFields();

                draggingField = fields1.get(index);
                draggingColor = currentInstr.getInstructionColors().get(index);

                draggingIndex = currentInstr.getAssemblyColors().indexOf
                        (draggingColor);
                originPane = instructionFormatPane;

                currentInstr.getInstructionFields().remove(index);
                currentInstr.getInstructionColors().remove(index);

                if (draggingField.getType() != Type.ignored) {
                    currentInstr.getAssemblyFields().remove(draggingIndex);
                    currentInstr.getAssemblyColors().remove(draggingIndex);
                }

                updateInstructionDisplay();
                /* Put a string on a dragboard */
                ClipboardContent content = new ClipboardContent();
                content.putString(fieldName.getText());
                db.setContent(content);

                event.consume();
            });

            instructionFormatPane.getChildren().addAll(fieldName, fieldWidth);
            i++;
        }

    }

    /**
     * uses the current instruction's assembly fields to update the display for the
     * assembly
     * format pane
     */
    private void updateAssemblyDisplay() {
        if( currentInstr == null)
            return; // nothing to do in that case

        assemblyFormatPane.getChildren().clear();

        List<Field> fields = currentInstr.getAssemblyFields();
        int curX = 0;
        int i = 0;
        for (Field field : fields) {
            final Label fieldName = new Label();


            fieldName.setText(field.getName());

            fieldName.setPrefWidth(assemblyFormatPane.getPrefWidth() / fields.size());

            fieldName.setAlignment(Pos.CENTER);

            fieldName.setStyle("-fx-background-color:" + currentInstr.getAssemblyColors
                    ().get(i) + ";");

            fieldName.setPrefHeight(25);

            fieldName.setLayoutX(curX);
            fieldName.setLayoutY((assemblyFormatPane.getPrefHeight() / 2) + fieldName
                    .getHeight() / 2);

            curX += fieldName.getPrefWidth();

            fieldName.setOnDragDetected(event -> {
                /* drag was detected, start a drag-and-drop gesture*/
                /* allow any transfer mode */

                Dragboard db = fieldName.startDragAndDrop(TransferMode.ANY);

                int index = assemblyFormatPane.getChildren().indexOf(fieldName);
                draggingIndex = index;

                ArrayList<Field> fields1 = currentInstr.getAssemblyFields();

                draggingField = fields1.get(index);
                draggingColor = currentInstr.getAssemblyColors().get(index);
                originPane = assemblyFormatPane;

                currentInstr.getAssemblyFields().remove(index);
                currentInstr.getAssemblyColors().remove(index);

                //it hasn't exited the pane as soon as the drag starts
                exited = false;

                updateAssemblyDisplay();
                /* Put a string on a drag board */
                ClipboardContent content = new ClipboardContent();
                content.putString(fieldName.getText());
                db.setContent(content);

                event.consume();
            });

            assemblyFormatPane.getChildren().addAll(fieldName);
            i++;
        }

    }

    /**
     * updates the display in the instruction format pane based on the field being
     * dragged in said pane and the index determined by the mouses position in said pane
     *
     * @param fieldToInsert the field being dragged
     * @param index          the index at which the field would be inserted in the
     *                      instruction's fields
     *                      if dropped
     */
    private void insertInstrField(Field fieldToInsert, int index) {
        List<Field> fields = currentInstr.getInstructionFields();

        ArrayList<Field> tempFields = new ArrayList<>();
        for (Field field : fields) {
            tempFields.add(field);
        }

        tempFields.add(index, fieldToInsert);
        int currentXPos = 0;
        int totalBits = 0;
        int i = 0;
        boolean openSpaceFilled = false;
        for (Field field : tempFields) {
            totalBits += field.getNumBits();
        }
        for (Field field : tempFields) {
            double prefWidth = ((float) field.getNumBits() / totalBits) *
                    instructionFormatPane.getPrefWidth();

            if (i / 2 == index && !openSpaceFilled) {
                currentXPos += prefWidth;
                openSpaceFilled = true;
                continue;
            }

            ((Label) instructionFormatPane.getChildren().get(i)).setPrefWidth(prefWidth);
            ((Label) instructionFormatPane.getChildren().get(i + 1)).setPrefWidth(30);

            instructionFormatPane.getChildren().get(i).setLayoutX(currentXPos);
            instructionFormatPane.getChildren().get(i + 1).setLayoutX(currentXPos +
                    (.5 * ((Label) instructionFormatPane.getChildren().get(i))
                            .getPrefWidth()) - 15);


            currentXPos += prefWidth;
            i += 2;
        }
    }

    /**
     * updates the display in the assembly format pane based on the field being
     * dragged in said pane and the index determined by the mouses position in said pane
     *
     * @param draggingField the field being dragged
     * @param index         the index at which the field would be inserted in the
     *                      instruction's
     *                      assembly fields
     *                      if dropped
     */
    private void insertAssemblyField(Field draggingField, int index) {
        List<Field> fields = currentInstr.getAssemblyFields();

        ArrayList<Field> tempFields = new ArrayList<>();
        for (Field field : fields) {
            tempFields.add(field);
        }
        tempFields.add(index, draggingField);
        int currentXPos = 0;
        int i = 0;
        boolean openSpaceFilled = false;

        for (Field field : tempFields) {
            double prefWidth = assemblyFormatPane.getPrefWidth() / tempFields.size();

            if (i == index && !openSpaceFilled) {
                currentXPos += prefWidth;
                openSpaceFilled = true;
                continue;
            }

            ((Label) assemblyFormatPane.getChildren().get(i)).setPrefWidth(prefWidth);

            assemblyFormatPane.getChildren().get(i).setLayoutX(currentXPos);

            currentXPos += prefWidth;
            i += 1;
        }
    }

    /**
     * updates the length label for the selected instruction based on said
     * instruction's field
     */
    private void updateLengthLabel() {
        List<Field> fields = currentInstr.getInstructionFields();
        int totalBits = 0;
        for (Field field : fields) {
            totalBits += field.getNumBits();
        }

        lengthLabel.setText(String.valueOf(totalBits));
    }

    /**
     * updates the display of the fields on the left based on all the fields in the
     * machine
     */
    private void updateFieldNames() {
        fieldPane.getChildren().clear();
        fieldNames.clear();
        double labelWidth = 130;
        double labelHeight = 30;
        int i = 0;
        for (Field field : allFields) {
            fieldNames.add(field.getName());
            final Label fieldLabel = new Label(field.getName());
            fieldLabel.setPrefWidth(labelWidth);
            fieldLabel.setPrefHeight(labelHeight);
            fieldLabel.setLayoutY(i);
            fieldLabel.setOnDragDetected(event -> {
                /* drag was detected, start a drag-and-drop gesture*/
                /* allow any transfer mode */
                Dragboard db = fieldLabel.startDragAndDrop(TransferMode.ANY);

                for (Field aField : allFields) {
                    if (aField.getName().equals(fieldLabel.getText())) {
                        draggingField = aField;
                    }
                }

                draggingColor = Convert.generateRandomLightColor();
                if (currentInstr != null) {
                    draggingIndex = currentInstr.getAssemblyFields().size();
                }
                originPane = fieldPane;

                /* Put a string on a dragboard */
                ClipboardContent content = new ClipboardContent();
                content.putString(fieldLabel.getText());
                db.setContent(content);

                event.consume();
            });


            i += labelHeight;
            fieldPane.getChildren().add(fieldLabel);
        }
    }

    /**
     * displays the micros of the implementation of the currently selected instruction
     */
    public void updateMicros() {
        if (currentInstr == null) {
            return;
        }

        implementationFormatPane.getChildren().clear();
        int newLabelHeight = 30;
        int nextYPosition = 0;
        for (final Microinstruction micro : currentInstr.getMicros()) {
            final Label microLabel = new Label(micro.getName());
            boolean commentLabel = false;
            if (micro instanceof Comment){
                microLabel.setStyle("-fx-font-family:Monaco; -fx-text-fill:gray; " +
                        "-fx-font-style:italic;");
                commentLabel = true;
            }
            else {
                microLabel.setStyle("-fx-font-family:Courier;");
            }
            // The following line is commented out because it somehow causes the
            // scroll pane's width to increase sometimes when dragging micros
            //microLabel.prefWidthProperty().bind(implementationFormatScrollPane.widthProperty());
            microLabel.setPrefHeight(newLabelHeight);
            microLabel.setLayoutY(nextYPosition);
            microLabel.setTooltip(new Tooltip(micro.getMicroClass()));

            //handles what happens when the label begins to get dragged
            microLabel.setOnDragDetected(event -> {

                //This is to make sure no error is thrown when the drag is detected
                //while still editing the text of a comment
                if (implementationFormatPane.getChildren().contains(microLabel)) {
                    /* drag was detected, start a drag-and-drop gesture*/
                    /* allow any transfer mode */
                    Dragboard db = microLabel.startDragAndDrop(TransferMode.ANY);

                    // remove the micro at the current index
                    currentInstr.getMicros().remove(
                            implementationFormatPane.getChildren().indexOf(microLabel));
                    updateMicros();

                    ClipboardContent content = new ClipboardContent();
                    content.putString(microLabel.getText() + "," + microLabel
                            .getTooltip().getText());
                    db.setContent(content);

                    event.consume();
                }
            });

            //determines what happens when the label is doubleClicked
            if (!commentLabel) {
                microLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if (mouseEvent.getButton().equals(MouseButton.PRIMARY) &&
                                mouseEvent.getClickCount() == 2) {
                            ObservableList<TreeItem> list = microInstrTreeView.getRoot
                                    ().getChildren();

                            for (TreeItem<String> t : list) {
                                if (t.getValue().equals(micro.getMicroClass())) {
                                    t.setExpanded(true);
                                    microInstrTreeView.scrollTo(list.indexOf(t));
                                    ObservableList<TreeItem<String>> nodes = t
                                            .getChildren();

                                    for (TreeItem<String> tt : nodes) {
                                        if (tt.getValue().equals(micro.getName())) {
                                            microInstrTreeView.getSelectionModel().select(
                                                    list.indexOf(t) + nodes.indexOf(tt)
                                                            + 2);
                                        }
                                    }
                                } else {
                                    t.setExpanded(false);
                                }
                            }
                        }
                    }
                });
            } else {
                microLabel.setOnMouseClicked(mouseEvent -> {
                    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)
                            && mouseEvent.getClickCount() == 2) {
                        commentEditor = new TextField(microLabel.getText());
                        commentEditor.setStyle("-fx-font-family:Courier;-fx-font-size:14");
                        commentEditor.setPrefWidth(implementationFormatPane
                                .getWidth());
                        commentEditor.setOnKeyPressed(t -> {
                            if (t.getCode() == KeyCode.ENTER) {
                                commitCommentEdit();
                            }
                        });
                        int index = implementationFormatPane.getChildren().indexOf
                                (microLabel);
                        microLabel.setVisible(false);
                        implementationFormatPane.getChildren().add(index,
                                commentEditor);
                        commentEditor.setPrefHeight(newLabelHeight);
                        commentEditor.setLayoutY(index * newLabelHeight);
                        currentCommentMicro = micro;
                    }
                });
            }


            nextYPosition += newLabelHeight;
            implementationFormatPane.getChildren().add(microLabel);
        }
    }

    /**
     * returns the fields as they have been changed so far in the dialog
     *
     * @return the fields as they have been changed so far in the dialog
     */
    public ObservableList<Field> getFields() {
        return allFields;
    }

    /**
     * sets the fields as they have been changed so far in the dialog
     *
     * @param fields the fields to set as the fields as they have been changed so far
     *               in the dialog
     */
    public void setFields(ObservableList<Field> fields) {
        allFields = fields;
        updateFieldNames();
    }

    /**
     * updates the observable list containing the names of the instructions (this updates
     * the list view for the instructions)
     */
    private void updateInstrNames() {
        int i = 0;
        for (MachineInstruction instr : instructions) {
            instr.setName(instrNames.get(i));
            i++;
        }
    }

    /**
     * looks at all the micros in the tree and creates a tree view appropriately
     */
    public void setUpMicroTreeView() {

        TreeItem<String> rootNode = new TreeItem<>("MicroInstructions");

        microInstrTreeView.setCellFactory(new Callback<TreeView, TreeCell>() {
            @Override
            public TreeCell call(TreeView param) {
                return new DragTreeCell(mediator,
                        (Stage) implementationFormatPane.getScene().getWindow(),
                        microInstrTreeView, EditMachineInstructionController.this.getClasses());
            }
        });

        rootNode.setExpanded(true);

        for (String microClass : Machine.MICRO_CLASSES) {
            TreeItem<String> classNode = new TreeItem<>(microClass);
            for (final Microinstruction micro : mediator.getMachine().getMicros
                    (microClass)) {
                final TreeItem<String> microNode = new TreeItem<>(micro.getName());
                classNode.getChildren().add(microNode);
            }
            rootNode.getChildren().add(classNode);
        }

        microInstrTreeView.setRoot(rootNode);

    }

    /**
     * returns the index at which the micro being dragged would be inserted into the
     * currently selected instruction's micros if it were dropped
     *
     * @param localY the y position of the mouse from the perspective of the
     *               implementation format pane
     * @return the index at which the micro being dragged would be inserted into the
     * currently selected instruction's micros if it were dropped
     */
    private int getMicroinstrIndex(double localY) {
        List<Double> cutOffLocs = new ArrayList<>();
        cutOffLocs.add(0.0);
        for (Node instr : implementationFormatPane.getChildren()) {
            Label label = (Label) instr;
            cutOffLocs.add(label.getLayoutY() + .5 * label.getPrefHeight());
        }
        cutOffLocs.add(implementationFormatPane.getHeight());
        int index = 0;
        for (int i = 0; i < cutOffLocs.size() - 1; i++) {
            if (localY >= cutOffLocs.get(i) && localY < cutOffLocs.get(i + 1)) {
                index = i;
            }
        }
        return index;
    }

    /**
     * updates the display of the implementation format pane by moving the
     * other micros away from the given index
     * at which the micro would be inserted if dropped
     *
     * @param index index at which the micro would be inserted if dropped
     */
    private void moveMicrosToMakeRoom(int index) {
        int i = 0;
//        currentInstr.getMicros().clear();
//        currentInstr.getMicros().add(index, new Branch("",0,new ControlUnit("",
// mediator.getMachine())));
        updateMicros();
        for (Node instr : implementationFormatPane.getChildren()) {
            Label label = (Label) instr;
            if (i >= index) {
                label.setPrefHeight(3*label.getPrefHeight());
            }
            i++;
        }

    }

    /**
     * returns the instructions as they have been modified so far
     *
     * @return the instructions as they have been modified so far
     */
    public List<MachineInstruction> getInstructions() {
        return instructions;
    }

    /**
     * returns the machine
     *
     * @return the machine
     */
    public Machine getMachine() {
        return mediator.getMachine();
    }

    /**
     * sets the instructions as they have been changed so far and updates the displays
     * appropriately
     *
     * @param instructions the instructions as they have been changed so far
     */
    public void setInstructions(List<MachineInstruction> instructions) {
        this.instructions = instructions;
        if (currentInstr != null) {
            if (currInstrIndex != -1) {
                currentInstr = this.instructions.get(currInstrIndex);
            }
            updateInstructionDisplay();
            updateAssemblyDisplay();
        }
    }

    /**
     * returns a String that is different from all names of
     * existing objects in the given list.  It checks whether proposedName
     * is unique and if so, it returns it.  Otherwise, it
     * proposes a new name of proposedName + "?" and tries again.
     *
     * @param list         list of existing objects
     * @param proposedName a given proposed name
     * @return the unique name
     */
    public String createUniqueName(ObservableList list, String proposedName) {
        String oldName;
        for (Object obj : list) {
            oldName = obj.toString();
            if (oldName != null && oldName.equals(proposedName)) {
                return createUniqueName(list, proposedName + "?");
            }
        }
        return proposedName;
    }

    /**
     * generates a unique opcode based on the opcodes taken by instruction that currently
     * exit.  The opcode the lowest positive number that is not taken
     *
     * @return a unique opcode
     */
    public int getUniqueOpcode() {
        int opcode = 0;
        while (true) {
            boolean opcodeTaken = false;
            for (MachineInstruction instr : instructions) {
                if (instr.getOpcode() == opcode) {
                    opcodeTaken = true;
                    break;
                }
            }
            if (!opcodeTaken) {
                break;
            }
            opcode++;
        }
        return opcode;
    }

    /**
     * commits the edit made to a comment microinstructions
     */
    public void commitCommentEdit() {
        currentCommentMicro.setName(commentEditor.getText());
        updateMicros();
        commentEditor = null;
    }

    private EditMachineInstructionController getClasses() {
        return this;
    }
}
