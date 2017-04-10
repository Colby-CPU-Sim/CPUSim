package cpusim.gui.editmachineinstruction;

import cpusim.Mediator;
import cpusim.gui.editmachineinstruction.editfields.EditFieldsController;
import cpusim.gui.util.*;
import cpusim.gui.util.list.StringPropertyListCell;
import cpusim.model.Field;
import cpusim.model.Machine;
import cpusim.model.MachineInstruction;
import cpusim.model.util.*;
import cpusim.model.util.conversion.ConvertLongs;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.fxmisc.easybind.EasyBind;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

/**
 * FXML Controller class for modifying {@link MachineInstruction} values.
 */
public class EditMachineInstructionController
        implements DialogButtonController.InteractionHandler,
                    HelpPageEnabled,
                    MachineModificationController,
                    ControlButtonController.InteractionHandler<MachineInstruction> {

    @FXML @SuppressWarnings("unused")
    private ListView<MachineInstruction> instructionList;

    /**
     * Pane labeled: "Format" holds fields and assembly layout.
     */
    @FXML @SuppressWarnings("unused")
    private BorderPane formatTabPane;
    
    
    @FXML @SuppressWarnings("unused")
    private TextField opcodeTextField;

    @FXML @SuppressWarnings("unused")
    private FieldsLayoutPane instructionLayout;
    
    @FXML @SuppressWarnings("unused")
    private FieldsLayoutPane assemblyLayout;
    
    @FXML
    private FieldListControl fieldList;

    @FXML
    private Label lengthLabel;

    @FXML
    private VBox noFieldsLabel;
    
    @FXML @SuppressWarnings("unused")
    private Label currentInstructionTitle;

    @FXML @SuppressWarnings("unused")
    private TabPane implTabPane;

    @FXML @SuppressWarnings("unused")
    private MachineInstructionImplTableController instImplTableController;

    @FXML
    private ControlButtonController<MachineInstruction> instButtonController;

    @FXML @SuppressWarnings("unused")
    private MicroinstructionTreeView microinstTreeView;

    @FXML @SuppressWarnings("unused")
    private DialogButtonController dialogButtonController;

    private Mediator mediator;
    private ObjectProperty<MachineInstruction> currentInstr;
//    private Field draggingField;
//    private String draggingColor;
//    private Pane originPane;
//    private boolean dropped;
//    private boolean exited;
//
//    private int draggingIndex;

    private final ObjectProperty<Machine> machine;

    public EditMachineInstructionController(Mediator mediator) {
        this.mediator = mediator;
        this.currentInstr = new SimpleObjectProperty<>(this, "currentInstruction", null);

        this.machine = new SimpleObjectProperty<>(this, "machine", null);
        this.machine.bind(mediator.machineProperty());
    }

    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize() {

        //initialize fields

        initializeInstructionList();

        initializeOpcodeTextField();

        ReadOnlyObjectProperty<MachineInstruction> currentInstruct =
                instructionList.getSelectionModel().selectedItemProperty();
        currentInstr.bind(currentInstruct);

        BooleanBinding isInstructionSelected = currentInstruct.isNull();
        implTabPane.disableProperty().bind(isInstructionSelected);

        EasyBind.subscribe(this.currentInstr, newValue -> {
            currentInstructionTitle.textProperty().unbind();

            if (newValue != null) {
                currentInstructionTitle.textProperty().bind(newValue.nameProperty());
            } else {
                currentInstructionTitle.setText("Unspecified");
            }
        });
        
        instImplTableController.currentInstructionProperty().bind(currentInstr);

//         updates the length label for the selected instruction based on said
//         instruction's field
        StringBinding lengthTextBinding = Bindings.createStringBinding(() -> {
            if (currentInstr.getValue() != null) {
                int sum = currentInstr.getValue().getInstructionFields().stream()
                        .mapToInt(Field::getNumBits)
                        .sum();

                if (sum > 0) {
                    return Integer.toString(sum);
                }
            }

            return "";
        }, currentInstr);

        lengthLabel.textProperty().bind(lengthTextBinding);

        BooleanBinding currentInstrHasAssocFields = Bindings.createBooleanBinding(() -> {
            MachineInstruction inst = currentInstr.getValue();

            return inst != null && (inst.getInstructionFields().size() > 0
                    || inst.getAssemblyFields().size() > 0);
        }, currentInstr);

        noFieldsLabel.visibleProperty().bind(currentInstrHasAssocFields);

        this.instructionList.itemsProperty()
                .bind(Bindings.createObjectBinding(() -> getMachine().instructionsProperty(),
                        machineProperty()));

        // Children's machines need to be bound as well.
        this.microinstTreeView.machineProperty().bind(machine);
        this.instImplTableController.machineProperty().bind(machine);
        this.fieldList.machineProperty().bind(machine);

        this.instButtonController.setInteractionHandler(this);

        this.dialogButtonController.setRequired(machine, this, this);
        this.dialogButtonController.setCurrentHelpable(this);

        this.instructionLayout.machineProperty().bind(machineProperty());
        this.assemblyLayout.machineProperty().bind(machineProperty());

        // Must set the color map first, before the instructions are bound
        // TODO Make this behave as a property, so when the map changes, the FieldLabels update their colours
        Map<Field, Color> fieldColors = new HashMap<>();
        this.instructionLayout.setFieldColorMap(fieldColors);
        this.assemblyLayout.setFieldColorMap(fieldColors);

        this.instructionLayout.currentInstructionProperty().bind(currentInstr);
        this.assemblyLayout.currentInstructionProperty().bind(currentInstr);
    }

    private void initializeOpcodeTextField() {
        opcodeTextField.textProperty().addListener((observable, oldValue, newValue) -> {

            MachineInstruction inst = currentInstr.getValue();
            if (inst == null) return;

            try {
                Validate.stringIsLegalBinHexOrDecNumber(newValue);
                long newOpcode = Convert.fromAnyBaseStringToLong(newValue);
                inst.setOpcode(newOpcode);
                Validate.instructionsOpcodeIsValid(instructionList.getItems(), inst);
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

    @Override
    public ObjectProperty<Machine> machineProperty() {
        return machine;
    }

    public List<MachineInstruction> getInstructions() {
        return instructionList.getItems();
    }

    private void bindSelectedItemNotNull(@Nonnull BooleanProperty toBind) {
        ControlButtonController.bindSelectedItemIsNull(toBind, instructionList.selectionModelProperty());
    }

    @Override
    public Supplier<MachineInstruction> getSupplier() {
        return () -> {
            int opcode = getUniqueOpcode();
            String uniqueName = NamedObject.createUniqueName(instructionList.getItems(), "?");
            return new MachineInstruction(uniqueName, UUID.randomUUID(), getMachine(), opcode,
                            null, null);
        };
    }

    @Override
    public void bindNewButtonDisabled(@Nonnull BooleanProperty toBind) {
        toBind.bind(new ReadOnlyBooleanWrapper(false));
    }

    @Override
    public void bindDeleteButtonDisabled(@Nonnull BooleanProperty toBind) {
        bindSelectedItemNotNull(toBind);
    }

    @Override
    public void bindDuplicateButtonDisabled(@Nonnull BooleanProperty toBind) {
        bindSelectedItemNotNull(toBind);
    }

    @Override
    public void bindPropertiesButtonDisabled(@Nonnull BooleanProperty toBind) {
        toBind.bind(new ReadOnlyBooleanWrapper(false));
    }

    @Override
    public void onNewValueCreated(@Nonnull MachineInstruction newValue) {
        // Make sure the opcode is unique
        newValue.setOpcode(getUniqueOpcode());
    }

    @Override
    public void bindItems(@Nonnull Property<ObservableList<MachineInstruction>> toBind) {
        toBind.bind(instructionList.itemsProperty());
    }

    @Override
    public void selectionModelBinding(@Nonnull ObjectProperty<SelectionModel<MachineInstruction>> toBind) {
        toBind.bind(instructionList.selectionModelProperty());
    }

    /**
     * Initializes the instruction list by making it editable, setting its items,
     * and giving it a change listener
     */
    private void initializeInstructionList() {
        instructionList.setCellFactory(list -> {
                    StringPropertyListCell<MachineInstruction> cell
                            = new StringPropertyListCell<>(MachineInstruction::nameProperty);
                    cell.getTextField().focusedProperty().addListener((observable, oldValue, newValue) -> {
                        if (oldValue && !newValue) {
                            instructionList.getSelectionModel().select(cell.getIndex());
                        }
                    });

                    return cell;
                });

        instructionList.getSelectionModel().selectedItemProperty().addListener(
                (ov, value, new_value) -> {

                    if (new_value != null) {

                        int numOpcodeBits;
                        if (new_value.getOpcode() != 0) {
                            numOpcodeBits = 64 - Long.numberOfLeadingZeros(new_value.getOpcode());
                        } else {
                            numOpcodeBits = 1;
                        }
                        opcodeTextField.setText("0x" + ConvertLongs.toHexString(new_value.getOpcode(), numOpcodeBits));

                    }
                    updateMicros();
                });
    }

    @Override
    public boolean onOkButtonClick() {
        return true;
    }

    @Override
    public void onMachineUpdated() {
        // nothing really to do, #updateMachine() handles the actual writing.
    }

    @Override
    public boolean onHelpButtonClick() {
        return true;
    }

    @Override
    public void displayHelpDialog(String helpPageId) {
        mediator.getDesktopController().showHelpDialog(helpPageId);
    }

    @Override
    public boolean onCancelButtonClick() {
        return true;
    }

    @Override
    public String getHelpPageID() {
        return "Machine Instruction Dialog";
    }

    @Override
    public void updateMachine() {
        instImplTableController.updateMachine();
        mediator.getMachine().setInstructions(instructionList.getItems());
        mediator.setMachineDirty(true);
    }

    @Override
    public void checkValidity() {
        Validatable.all(instructionList.getItems());

        Validate.machineInstructions(instructionList.getItems(), machineProperty().getValue());

        instImplTableController.checkValidity();
    }

    /**
     * opens the edit fields dialog when the user presses the edit fields button
     *
     * @param ae unused action event
     */
    @FXML
    protected void handleEditFields(ActionEvent ae) {
        EditFieldsController.showDialog(mediator, lengthLabel.getScene().getWindow());
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


        // FIXME replace with Node#contains(pt)
        List<Double> cutoffXLocs = new ArrayList<>();
        cutoffXLocs.add(0.0);
//        for (int i = 0; i < instructionFormatPane.getChildren().size() - 1; i += 2) {
//            cutoffXLocs.add(instructionFormatPane.getChildren().get(i).getLayoutX() +
//                    ((Label) instructionFormatPane.getChildren().get(i)).getPrefWidth()
//                            * .5);
//        }
//        cutoffXLocs.add(instructionFormatPane.getPrefWidth());

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

        // FIXME replace with Node#contains(pt)

//        ArrayList<Double> cutoffXLocs = new ArrayList<>();
//        cutoffXLocs.add(0.0);
//        for (int i = 0; i < assemblyFormatPane.getChildren().size(); i += 1) {
//            cutoffXLocs.add(assemblyFormatPane.getChildren().get(i).getLayoutX() +
//                    ((Label) assemblyFormatPane.getChildren().get(i)).getPrefWidth() *
//                            .5);
//        }
//        cutoffXLocs.add(assemblyFormatPane.getPrefWidth());
//
//        for (int i = 0; i < cutoffXLocs.size() - 1; i++) {
//            if (localX > cutoffXLocs.get(i) && localX < cutoffXLocs.get(i + 1)) {
//                return i;
//            }
//        }
        return 0;
    }

//    /**
//     * uses the current instruction's fields to update the display for the instruction
//     * format pane
//     */
//    private void updateInstructionDisplay() {
//        if( currentInstr.getValue() == null) {
//            return;  // that's all we want to do
//        }
//
//        final MachineInstruction inst = currentInstr.getValue();
//
////        instructionFormatPane.getChildren().clear();
//
//        List<Field> fields = inst.getInstructionFields();
//        int totalBits = 0;
//        for (Field field : fields) {
//            totalBits += field.getNumBits();
//        }
//
//        int curX = 0;
//        int i = 0;
//        for (Field field : fields) {
//            final Label fieldName = new Label();
//            final Label fieldWidth = new Label();
//
//
//            fieldName.setText(field.getName());
//            fieldWidth.setText(String.valueOf(field.getNumBits()));
//
////            fieldName.setPrefWidth(((float) field.getNumBits() / totalBits) *
////                    instructionFormatPane.getPrefWidth());
//            fieldWidth.setPrefWidth(30);
//
//            fieldName.setLayoutY(30);
//            fieldWidth.setLayoutY(0);
//
//            fieldName.setPrefHeight(25);
//            fieldWidth.setPrefHeight(25);
//
//
//            // FIXME https://github.com/Colby-CPU-Sim/CPUSimFX2015/issues/109
////            fieldWidth.setStyle("-fx-background-color:" + inst.getInstructionColors().get(i) + ";");
////            fieldName.setStyle("-fx-background-color:" + inst.getInstructionColors().get(i) + ";");
//
//            fieldWidth.setAlignment(Pos.CENTER);
//            fieldName.setAlignment(Pos.CENTER);
//
//            fieldName.setLayoutX(curX);
//            fieldWidth.setLayoutX(curX + (.5 * fieldName.getPrefWidth()) - 15);
//
//            curX += fieldName.getPrefWidth();
//
//            fieldName.setOnDragDetected(event -> {
//                /* drag was detected, start a drag-and-drop gesture*/
//                /* allow any transfer mode */
//                final MachineInstruction currentInstrValue = currentInstr.getValue();
//
//                Dragboard db = fieldName.startDragAndDrop(TransferMode.ANY);
//
//                int index = 0;//instructionFormatPane.getChildren().indexOf(fieldName) / 2;
//
//                final List<Field> fields1 = currentInstrValue.getInstructionFields();
//
//                draggingField = fields1.get(index);
//
//                // FIXME https://github.com/Colby-CPU-Sim/CPUSimFX2015/issues/109
////                draggingColor = currentInstrValue.getInstructionColors().get(index);
//
//                draggingIndex = currentInstrValue.getAssemblyFields().indexOf(draggingField);
////                originPane = instructionFormatPane;
//
//                currentInstrValue.getInstructionFields().remove(index);
//                // FIXME https://github.com/Colby-CPU-Sim/CPUSimFX2015/issues/109
////                currentInstrValue.getInstructionColors().remove(index);
//
//                if (draggingField.getType() != Type.ignored) {
//                    currentInstrValue.getAssemblyFields().remove(draggingIndex);
////                    currentInstrValue.getAssemblyColors().remove(draggingIndex);
//                }
//
//                updateInstructionDisplay();
//                /* Put a string on a dragboard */
//                ClipboardContent content = new ClipboardContent();
//                content.putString(fieldName.getText());
//                db.setContent(content);
//
//                event.consume();
//            });
//
////            instructionFormatPane.getChildren().addAll(fieldName, fieldWidth);
//            i++;
//        }
//
//    }

//    /**
//     * uses the current instruction's assembly fields to update the display for the
//     * assembly
//     * format pane
//     */
//    private void updateAssemblyDisplay() {
//        if( currentInstr.getValue() == null)
//            return; // nothing to do in that case
//
//
//        final MachineInstruction inst = currentInstr.getValue();
////        assemblyFormatPane.getChildren().clear();
//
//        List<Field> fields = inst.getAssemblyFields();
//        int curX = 0;
//        int i = 0;
//        for (Field field : fields) {
//            final Label fieldName = new Label();
//
//
//            fieldName.setText(field.getName());
//
//            fieldName.setPrefWidth(assemblyFormatPane.getPrefWidth() / fields.size());
//
//            fieldName.setAlignment(Pos.CENTER);
//
//            // FIXME https://github.com/Colby-CPU-Sim/CPUSimFX2015/issues/109
////            fieldName.setStyle("-fx-background-color:" + inst.getAssemblyColors().get(i) + ";");
//
//            fieldName.setPrefHeight(25);
//
//            fieldName.setLayoutX(curX);
//            fieldName.setLayoutY((assemblyFormatPane.getPrefHeight() / 2) + fieldName
//                    .getHeight() / 2);
//
//            curX += fieldName.getPrefWidth();
//
//            fieldName.setOnDragDetected(event -> {
//                /* drag was detected, start a drag-and-drop gesture*/
//                /* allow any transfer mode */
//
//                final MachineInstruction currentInstruction = currentInstr.getValue();
//
//                Dragboard db = fieldName.startDragAndDrop(TransferMode.ANY);
//
//                int index = assemblyFormatPane.getChildren().indexOf(fieldName);
//                draggingIndex = index;
//
//                final List<Field> fields1 = currentInstruction.getAssemblyFields();
//
//                draggingField = fields1.get(index);
//                // FIXME https://github.com/Colby-CPU-Sim/CPUSimFX2015/issues/109
////                draggingColor = currentInstruction.getAssemblyColors().get(index);
//                originPane = assemblyFormatPane;
//
//                currentInstruction.getAssemblyFields().remove(index);
//                // FIXME https://github.com/Colby-CPU-Sim/CPUSimFX2015/issues/109
////                currentInstruction.getAssemblyColors().remove(index);
//
//                //it hasn't exited the pane as soon as the drag starts
//                exited = false;
//
//                updateAssemblyDisplay();
//                /* Put a string on a drag board */
//                ClipboardContent content = new ClipboardContent();
//                content.putString(fieldName.getText());
//                db.setContent(content);
//
//                event.consume();
//            });
//
//            assemblyFormatPane.getChildren().addAll(fieldName);
//            i++;
//        }
//
//    }

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
        List<Field> fields = currentInstr.getValue().getInstructionFields();

        List<Field> tempFields = new ArrayList<>();
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
//            double prefWidth = ((float) field.getNumBits() / totalBits) *
//                    instructionFormatPane.getPrefWidth();

            if (i / 2 == index && !openSpaceFilled) {
//                currentXPos += prefWidth;
                openSpaceFilled = true;
                continue;
            }

//            ((Label) instructionFormatPane.getChildren().get(i)).setPrefWidth(prefWidth);
//            ((Label) instructionFormatPane.getChildren().get(i + 1)).setPrefWidth(30);
//
//            instructionFormatPane.getChildren().get(i).setLayoutX(currentXPos);
//            instructionFormatPane.getChildren().get(i + 1).setLayoutX(currentXPos +
//                    (.5 * ((Label) instructionFormatPane.getChildren().get(i))
//                            .getPrefWidth()) - 15);
//
//
//            currentXPos += prefWidth;
            i += 2;
        }
    }

//    /**
//     * updates the display in the assembly format pane based on the field being
//     * dragged in said pane and the index determined by the mouses position in said pane
//     *
//     * @param draggingField the field being dragged
//     * @param index         the index at which the field would be inserted in the
//     *                      instruction's
//     *                      assembly fields
//     *                      if dropped
//     */
//    private void insertAssemblyField(Field draggingField, int index) {
//        List<Field> fields = currentInstr.getValue().getAssemblyFields();
//
//        ArrayList<Field> tempFields = new ArrayList<>();
//        for (Field field : fields) {
//            tempFields.add(field);
//        }
//        tempFields.add(index, draggingField);
//        int currentXPos = 0;
//        int i = 0;
//        boolean openSpaceFilled = false;
//
//        for (Field field : tempFields) {
//            double prefWidth = assemblyFormatPane.getPrefWidth() / tempFields.size();
//
//            if (i == index && !openSpaceFilled) {
//                currentXPos += prefWidth;
//                openSpaceFilled = true;
//                continue;
//            }
//
//            ((Label) assemblyFormatPane.getChildren().get(i)).setPrefWidth(prefWidth);
//
//            assemblyFormatPane.getChildren().get(i).setLayoutX(currentXPos);
//
//            currentXPos += prefWidth;
//            i += 1;
//        }
//    }



//    /**
//     * updates the display of the fields on the left based on all the fields in the
//     * machine
//     */
//    private void updateFieldNames() {
//        fieldPane.getChildren().clear();
//        fieldNames.clear();
//        double labelWidth = 130;
//        double labelHeight = 30;
//        int i = 0;
//        for (Field field : allFields) {
//            fieldNames.add(field.getName());
//            final Label fieldLabel = new Label(field.getName());
//            fieldLabel.setPrefWidth(labelWidth);
//            fieldLabel.setPrefHeight(labelHeight);
//            fieldLabel.setLayoutY(i);
//            fieldLabel.setOnDragDetected(event -> { // FIXME Extract this to handle when we drag a field
//                /* drag was detected, start a drag-and-drop gesture*/
//                /* allow any transfer mode */
//                Dragboard db = fieldLabel.startDragAndDrop(TransferMode.ANY);
//
//                for (Field aField : allFields) {
//                    if (aField.getName().equals(fieldLabel.getText())) {
//                        draggingField = aField;
//                    }
//                }
//
//                draggingColor = Colors.generateRandomLightHtmlColor();
//                if (currentInstr != null) {
//                    draggingIndex = currentInstr.getAssemblyFields().size();
//                }
//                originPane = fieldPane;
//
//                /* Put a string on a dragboard */
//                ClipboardContent content = new ClipboardContent();
//                content.putString(fieldLabel.getText());
//                db.setContent(content);
//
//                event.consume();
//            });
//
//
//            i += labelHeight;
//            fieldPane.getChildren().add(fieldLabel);
//        }
//    }

    /**
     * displays the micros of the implementation of the currently selected instruction
     */
    public void updateMicros() {
        if (currentInstr.getValue() == null) {
            return;
        }

//        implementationFormatPane.getChildren().clear();
//        int newLabelHeight = 30;
//        int nextYPosition = 0;
//        for (final Microinstruction<?> micro : currentInstr.getMicros()) {
//            final Label microLabel = new Label(micro.getName());
//            boolean commentLabel = false;
//            if (Comment.class.isAssignableFrom(micro.getClass())) {
//                microLabel.setStyle("-fx-font-family:Monaco; -fx-text-fill:gray; " +
//                        "-fx-font-style:italic;");
//                commentLabel = true;
//            }
//            else {
//                microLabel.setStyle("-fx-font-family:Courier;");
//            }
//
//            // The following line is commented out because it somehow causes the
//            // scroll pane's width to increase sometimes when dragging micros
//            //microLabel.prefWidthProperty().bind(implementationFormatScrollPane.widthProperty());
//            microLabel.setPrefHeight(newLabelHeight);
//            microLabel.setLayoutY(nextYPosition);
//            microLabel.setTooltip(new Tooltip(micro.getClass().getSimpleName()));
//
//            //handles what happens when the label begins to get dragged
//            microLabel.setOnDragDetected(event -> {
//
//                //This is to make sure no error is thrown when the drag is detected
//                //while still editing the text of a comment
//                if (implementationFormatPane.getChildren().contains(microLabel)) {
//                    /* drag was detected, start a drag-and-drop gesture*/
//                    /* allow any transfer mode */
//                    Dragboard db = microLabel.startDragAndDrop(TransferMode.ANY);
//
//                    // remove the micro at the current index
//                    currentInstr.getMicros().remove(
//                            implementationFormatPane.getChildren().indexOf(microLabel));
//                    updateMicros();
//
//                    ClipboardContent content = new ClipboardContent();
//                    content.putString(microLabel.getText() + "," + microLabel.getTooltip().getText());
//                    db.setContent(content);
//
//                    event.consume();
//                }
//            });
//
//            //determines what happens when the label is doubleClicked
//            if (!commentLabel) {
//                // FIXME KB
////                microLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
////                    @Override
////                    public void handle(MouseEvent mouseEvent) {
////                        if (mouseEvent.getButton().equals(MouseButton.PRIMARY) &&
////                                mouseEvent.getClickCount() == 2) {
////                            ObservableList<TreeItem<String>> list = microInstrTreeView.getRoot().getChildren();
////
////                            for (TreeItem<String> t : list) {
////                                if (t.getValue().equals(micro.getMicroClass())) {
////                                    t.setExpanded(true);
////                                    microInstrTreeView.scrollTo(list.indexOf(t));
////                                    ObservableList<TreeItem<String>> nodes = t.getChildren();
////
////                                    for (TreeItem<String> tt : nodes) {
////                                        if (tt.getValue().equals(micro.getName())) {
////                                            microInstrTreeView.getSelectionModel().select(
////                                                    list.indexOf(t) + nodes.indexOf(tt)
////                                                            + 2);
////                                        }
////                                    }
////                                } else {
////                                    t.setExpanded(false);
////                                }
////                            }
////                        }
////                    }
////                });
//            } else {
//                microLabel.setOnMouseClicked(mouseEvent -> {
//                    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)
//                            && mouseEvent.getClickCount() == 2) {
//                        commentEditor = new TextField(microLabel.getText());
//                        commentEditor.setStyle("-fx-font-family:Courier;-fx-font-size:14");
//                        commentEditor.setPrefWidth(implementationFormatPane
//                                .getWidth());
//                        commentEditor.setOnKeyPressed(t -> {
//                            if (t.getCode() == KeyCode.ENTER) {
//                                commitCommentEdit();
//                            }
//                        });
//                        int index = implementationFormatPane.getChildren().indexOf
//                                (microLabel);
//                        microLabel.setVisible(false);
//                        implementationFormatPane.getChildren().add(index,
//                                commentEditor);
//                        commentEditor.setPrefHeight(newLabelHeight);
//                        commentEditor.setLayoutY(index * newLabelHeight);
//                        currentCommentMicro = micro;
//                    }
//                });
//            }
//
//
//            nextYPosition += newLabelHeight;
//            implementationFormatPane.getChildren().add(microLabel);
//        }
    }

//    /**
//     * returns the fields as they have been changed so far in the dialog
//     *
//     * @return the fields as they have been changed so far in the dialog
//     */
//    public ObservableList<Field> getFields() {
//        return fieldsList.getItems();
//    }

//    /**
//     * sets the fields as they have been changed so far in the dialog
//     *
//     * @param fields the fields to set as the fields as they have been changed so far
//     *               in the dialog
//     */
//    public void setFields(List<Field> fields) {
//        fieldsList.getItems().clear();
//        fieldsList.getItems().addAll(fields);
//    }

//    /**
//     * returns the index at which the micro being dragged would be inserted into the
//     * currently selected instruction's micros if it were dropped
//     *
//     * @param localY the y position of the mouse from the perspective of the
//     *               implementation format pane
//     * @return the index at which the micro being dragged would be inserted into the
//     * currently selected instruction's micros if it were dropped
//     */
//    private int getMicroinstrIndex(double localY) {
//        List<Double> cutOffLocs = new ArrayList<>();
//        cutOffLocs.add(0.0);
//        for (Node instr : implementationFormatPane.getChildren()) {
//            Label label = (Label) instr;
//            cutOffLocs.add(label.getLayoutY() + .5 * label.getPrefHeight());
//        }
//        cutOffLocs.add(implementationFormatPane.getHeight());
//        int index = 0;
//        for (int i = 0; i < cutOffLocs.size() - 1; i++) {
//            if (localY >= cutOffLocs.get(i) && localY < cutOffLocs.get(i + 1)) {
//                index = i;
//            }
//        }
//        return index;
//    }

//    /**
//     * updates the display of the implementation format pane by moving the
//     * other micros away from the given index
//     * at which the micro would be inserted if dropped
//     *
//     * @param index index at which the micro would be inserted if dropped
//     */
//    private void moveMicrosToMakeRoom(int index) {
//        int i = 0;
////        currentInstr.getMicros().clear();
////        currentInstr.getMicros().add(index, new Branch("",0,new ControlUnit("",
//// mediator.getMachine())));
//        updateMicros();
//        for (Node instr : implementationFormatPane.getChildren()) {
//            Label label = (Label) instr;
//            if (i >= index) { // FIXME use a bind is this is actually needed..
//                label.setPrefHeight(3*label.getPrefHeight());
//            }
//            i++;
//        }
//
//    }


    /**
     * generates a unique opcode based on the opcodes taken by instruction that currently
     * exit.  The opcode the lowest positive number that is not taken
     *
     * @return a unique opcode
     */
    private int getUniqueOpcode() {
        int opcode = 0;
        while (true) {
            boolean opcodeTaken = false;
            for (MachineInstruction instr : instructionList.getItems()) {
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
}
