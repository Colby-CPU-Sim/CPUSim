package cpusim.gui.editmachineinstruction.editfields;

import cpusim.gui.editmachineinstruction.FieldListControl;
import cpusim.gui.util.FXMLLoaderFactory;
import cpusim.model.Field;
import cpusim.model.FieldValue;
import cpusim.model.Machine;
import cpusim.model.util.MachineBound;
import cpusim.model.util.ValidationException;
import cpusim.util.Dialogs;
import javafx.beans.binding.Bindings;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.Subscription;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dialog for adding/editing fields
 *
 * @since 2013-06-04
 */
public class EditFieldsController
        extends BorderPane
        implements MachineBound {


    private static final String FXML_FILE = "EditFields.fxml";

    @FXML
    private FieldListControl fieldsList;

    @FXML
    private TableView<FieldValue> valuesTable;

    private TableColumn<FieldValue, String> nameColumn;

    private TableColumn<FieldValue, Long> valueColumn;

    @FXML
    private TextField nameText;

    @FXML
    private ChoiceBox<Field.Type> typeChoice;

    @FXML
    private Spinner<Long> defaultValueSpinner;

    @FXML
    private CheckBox unsignedCheckbox;

    @FXML
    private Spinner<Integer> bitsSpinner;

    @FXML
    private ChoiceBox<Field.Relativity> relativityChoice;

    private final ObjectProperty<Machine> machine;

    private final ObjectProperty<Field> selectedField;

    /**
     * constructor
     */
    public EditFieldsController() {

        this.machine = new SimpleObjectProperty<>(this, "machine", null);
        this.selectedField = new SimpleObjectProperty<>(this, "selectedField", null);

        // clone all the fields to fill up allFields
        // FIXME KB
//        for (Field field : editMachineInstructionController.getFields()){
//            List<FieldValue> realFieldValues = field.getValues();
//            ObservableList<FieldValue> fieldValues = FXCollections.observableArrayList();
//            for (FieldValue fieldValue : realFieldValues){
//                fieldValues.add(new FieldValue(fieldValue.getName(), UUID.randomUUID(),
//                        getMachine(), fieldValue.getValue()));
//            }
//            allFields.add(new Field(field.getName(), UUID.randomUUID(), getMachine(),
//                    field.getNumBits(), field.getRelativity(),
//                    fieldValues, field.getDefaultValue(),
//                    field.getSigned(), field.getType()
//            ));
//        }

        // clone the machine instructions using the cloned fields
//        for (MachineInstruction instr : editMachineInstructionController.getInstructions()){
//
//            final List<Field> oldInstrFields = instr.getInstructionFields();
//            final List<Field> newInstrFields = new ArrayList<>();
//            for (Field field : oldInstrFields){
//                for (Field aField : allFields) {
//                    if (field.getName().equals(aField.getName())) {
//                        newInstrFields.add(aField);
//                    }
//                }
//            }
//
//            List<Field> oldAssemblyFields = instr.getAssemblyFields();
//            List<Field> newAssemblyFields = new ArrayList<>();
//            for (Field field : oldAssemblyFields){
//                for (Field aField : allFields) {
//                    if (field.getName().equals(aField.getName())) {
//                        newAssemblyFields.add(aField);
//                    }
//                }
//            }
//
//            MachineInstruction instrToAdd = new MachineInstruction(instr.getName(),
//                    UUID.randomUUID(),
//                    getMachine(),
//                    instr.getOpcode(),
//                    newInstrFields, newAssemblyFields);
//
//            instrToAdd.setMicros(instr.getMicros());
//
//            instructions.add(instrToAdd);
//        }

        try {
            FXMLLoaderFactory.fromRootController(this, FXML_FILE).load();
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize() {

        fieldsList.machineProperty().bind(machine);

        ObservableList<Field.Type> types = FXCollections.observableArrayList();
        Collections.addAll(types, Field.Type.values());
        typeChoice.setItems(types);

        ObservableList<Field.Relativity> realitivityTypes = FXCollections.observableArrayList();
        Collections.addAll(realitivityTypes, Field.Relativity.values());
        relativityChoice.setItems(realitivityTypes);


        LongSpinnerValueFactory defaultValueFactory = new LongSpinnerValueFactory();
        defaultValueFactory.setValue(0L);

        SpinnerValueFactory.IntegerSpinnerValueFactory bitsSpinnerFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 63, 4);

        defaultValueFactory.maxProperty().bind(Bindings.createIntegerBinding(() ->
                (1 << (bitsSpinnerFactory.getValue() + 1)) - 1));

        defaultValueSpinner.setValueFactory(defaultValueFactory);
        bitsSpinner.setValueFactory(bitsSpinnerFactory);

        List<Subscription> currentSubs = new ArrayList<>();

        this.selectedField.bind(fieldsList.selectedFieldProperty());
        this.selectedField.addListener((v, oldField, currentField) -> {
            if (oldField != null) {
                Bindings.unbindBidirectional(nameText.textProperty(), oldField.nameProperty());
                currentSubs.forEach(Subscription::unsubscribe);
                currentSubs.clear();
            }

            if (currentField != null) {
                // Now bind all of the parameters to the current Field.
                nameText.textProperty().bindBidirectional(currentField.nameProperty());

                // Field type
                SelectionModel<Field.Type> typeSelectionModel = typeChoice.getSelectionModel();
                currentSubs.add(EasyBind.subscribe(currentField.typeProperty(), fromField -> {
                    Field.Type fromSelected = typeSelectionModel.getSelectedItem();
                    if (fromField != fromSelected) {
                        typeSelectionModel.select(fromField);
                    }
                }));
                currentSubs.add(EasyBind.subscribe(typeSelectionModel.selectedItemProperty(), fromControl -> {
                    Field.Type fromField = currentField.typeProperty().get();
                    if (fromField != fromControl) {
                        currentField.typeProperty().set(fromControl);
                    }
                }));

                // Default value
                currentSubs.add(EasyBind.subscribe(currentField.defaultValueProperty(), fromField -> {
                    if (fromField.longValue() != defaultValueSpinner.getValueFactory().getValue()) {
                        defaultValueSpinner.getValueFactory().setValue(fromField.longValue());
                    }
                }));
                currentSubs.add(EasyBind.subscribe(defaultValueSpinner.getValueFactory().valueProperty(), fromSpinner -> {
                    if (fromSpinner != currentField.getDefaultValue()) {
                        currentField.setDefaultValue(fromSpinner);
                    }
                }));

                // Bit width
                currentSubs.add(EasyBind.subscribe(currentField.numBitsProperty(), fromField -> {
                    if (fromField.longValue() != bitsSpinner.getValue()) {
                        bitsSpinner.getValueFactory().setValue(fromField.intValue());
                    }
                }));
                currentSubs.add(EasyBind.subscribe(bitsSpinner.valueProperty(), fromSpinner -> {
                    if (fromSpinner != currentField.getNumBits()) {
                        currentField.setNumBits(fromSpinner);
                    }
                }));

                // Unsigned:
                currentSubs.add(EasyBind.subscribe(currentField.signedProperty(), fromField -> {
                    if ((fromField == Field.SignedType.Unsigned && !unsignedCheckbox.isSelected())
                            || (fromField == Field.SignedType.Signed && unsignedCheckbox.isSelected())) {
                        unsignedCheckbox.setSelected(fromField == Field.SignedType.Unsigned);
                    }
                }));
                currentSubs.add(EasyBind.subscribe(unsignedCheckbox.selectedProperty(), fromControl -> {
                    if ((fromControl && currentField.getSigned() != Field.SignedType.Unsigned)
                            || (!fromControl && currentField.getSigned() != Field.SignedType.Signed)) {
                        currentField.setSigned(fromControl ? Field.SignedType.Unsigned : Field.SignedType.Unsigned);
                    }
                }));

                // Relativity
                SelectionModel<Field.Relativity> relativitySelectionModel = relativityChoice.getSelectionModel();
                currentSubs.add(EasyBind.subscribe(currentField.relativityProperty(), fromField -> {
                    Field.Relativity fromSelected = relativitySelectionModel.getSelectedItem();
                    if (fromField != fromSelected) {
                        relativitySelectionModel.select(fromField);
                    }
                }));
                currentSubs.add(EasyBind.subscribe(relativitySelectionModel.selectedItemProperty(), fromControl -> {
                    Field.Relativity fromField = currentField.relativityProperty().get();
                    if (fromField != fromControl) {
                        currentField.relativityProperty().set(fromControl);
                    }
                }));
            }
        });
    }

    @Override
    public ObjectProperty<Machine> machineProperty() {
        return machine;
    }

    @FXML
    protected void handleOkay(ActionEvent ae){
        //get a handle to the stage.
//        Stage stage = (Stage) table.getScene().getWindow();

        // check that all the editing done results in legal fields
        try {
            fieldsList.checkValidity();
        }
        catch(ValidationException ex) {
            Dialogs.createErrorDialog(fieldsList.getScene().getWindow(),
                    "Field Error", ex.getMessage()).showAndWait();
            return;
        }

//        for (MachineInstruction instruction : instructions){
//            //adds ignored instruction fields and associated colors to the assembly fields
//            for (Field field : instruction.getInstructionFields()){
//                if (!instruction.getAssemblyFields().contains(field) && field.getType() != Type.ignored){
//                    instruction.getAssemblyFields().add(field);
//                    // FIXME https://github.com/Colby-CPU-Sim/CPUSimFX2015/issues/109
////                    instruction.getAssemblyColors().add(instruction.getInstructionColors().get(
////                            instruction.getInstructionFields().indexOf(field)));
//                }
//            }
//            //removes ignored instruction fields and associated colors
//            for (Field field : instruction.getAssemblyFields()){
//                if (field.getType() == Type.ignored){
//                    // FIXME https://github.com/Colby-CPU-Sim/CPUSimFX2015/issues/109
////                    instruction.getAssemblyColors().remove(instruction.getAssemblyFields().indexOf(field));
//                    instruction.getAssemblyFields().remove(field);
//                }
//            }
//        }
        
        //editMachineInstructionController.setFields(allFields);
        // FIXME KB
//        editMachineInstructionController.setInstructions(instructions);
//        stage.close();
    }

    /**
     * closes the window without saving any changes made
     * @param ae the ActionEvent referring to the cancel action
     */
    @FXML
    protected void handleCancel(ActionEvent ae){
        //get a handle to the stage.
//        Stage stage = (Stage) table.getScene().getWindow();
//
//        //close window.
//        stage.close();
    }

    private static class LongSpinnerValueFactory extends SpinnerValueFactory<Long> {
        private final LongProperty min;
        private final LongProperty max;

        public LongSpinnerValueFactory(long min, long max) {
            this.min = new SimpleLongProperty(this, "min", min);
            this.max = new SimpleLongProperty(this, "max", max);
        }

        public LongSpinnerValueFactory() {
            this(0, Long.MAX_VALUE);
        }

        private long railToBars(long newV) {
            // rail newV
            long v = newV;
            if (v > max.get()) {
                v = max.get();
            } else if (v < min.get()) {
                v = min.get();
            }

            return v;
        }

        @Override
        public void decrement(int steps) {
            setValue(railToBars(getValue() - steps));
        }

        @Override
        public void increment(int steps) {
            setValue(railToBars(getValue() + steps));
        }

        public long getMin() {
            return min.get();
        }

        public LongProperty minProperty() {
            return min;
        }

        public void setMin(long min) {
            this.min.set(min);
        }

        public long getMax() {
            return max.get();
        }

        public LongProperty maxProperty() {
            return max;
        }

        public void setMax(long max) {
            this.max.set(max);
        }
    }

    // FIXME put in the dialogbutton controller
//    /**
//     * checks whether everything is okay to be saved
//     * @return true if everything is okay, else false
//     */
//    private boolean isAbleToClose() {
//        ArrayList<String> fieldNames = new ArrayList<>();
//        for (Field field : allFields) {
//            if (field.getName().indexOf(" ") != -1) {
//                Dialogs.createErrorDialog(stage,
//                        "Field Name Error", "Field name '" + field.getName() + "' is " +
//                                "not valid.").showAndWait();
//
//                return false;
//            }
//            if (fieldNames.contains(field.getName())) {
//                Dialogs.createErrorDialog(stage, "Field Name Error",
//                        "You cannot have two fields with the same name (" + field
//                                .getName() + ")").showAndWait();
//
//                return false;
//            }
//            fieldNames.add(field.getName());
//        }
//        return true;
//    }
}
