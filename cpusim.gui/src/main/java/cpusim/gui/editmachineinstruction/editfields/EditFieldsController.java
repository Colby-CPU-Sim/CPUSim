package cpusim.gui.editmachineinstruction.editfields;

import cpusim.Mediator;
import cpusim.gui.editmachineinstruction.FieldListControl;
import cpusim.gui.util.*;
import cpusim.gui.util.table.EditingLongCell;
import cpusim.gui.util.table.EditingStrCell;
import cpusim.model.Field;
import cpusim.model.FieldValue;
import cpusim.model.Machine;
import cpusim.model.util.MachineBound;
import cpusim.model.util.ValidationException;
import cpusim.model.util.conversion.ConvertStrings;
import cpusim.util.Dialogs;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.Subscription;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Dialog for adding/editing fields
 *
 * @since 2013-06-04
 */
public class EditFieldsController
        extends BorderPane
        implements MachineBound,
                   HelpPageEnabled,
                   DialogButtonController.InteractionHandler {


    private static final String FXML_FILE = "EditFields.fxml";

    @FXML
    private FieldListControl fieldsList;

    @FXML
    private TableView<FieldValue> valuesTable;

    @FXML
    private TableColumn<FieldValue, String> nameColumn;

    @FXML
    private TableColumn<FieldValue, Long> valueColumn;

    @FXML
    private DefaultControlButtonController<FieldValue> valuesButtons;

    @FXML
    private TextField nameText;

    @FXML
    private ComboBox<Field.Type> typeChoice;

    @FXML
    private Spinner<Long> defaultValueSpinner;

    @FXML
    private CheckBox unsignedCheckbox;

    @FXML
    private Spinner<Integer> bitsSpinner;

    @FXML
    private ComboBox<Field.Relativity> relativityChoice;

    @FXML
    private DialogButtonController dialogButtons;

    private final ObjectProperty<Machine> machine;

    private final ObjectProperty<Field> selectedField;

    private final Mediator mediator;

    public EditFieldsController() {
        this(null);
    }

    /**
     * constructor
     */
    public EditFieldsController(Mediator mediator) {
        this.mediator = mediator;
        this.machine = new SimpleObjectProperty<>(this, "machine", null);
        this.selectedField = new SimpleObjectProperty<>(this, "selectedField", null);

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
    protected void initialize() {

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
                1 << (bitsSpinnerFactory.getValue() + 1), bitsSpinnerFactory.valueProperty()));

        defaultValueSpinner.setValueFactory(defaultValueFactory);
        // commit the spinner: http://stackoverflow.com/a/39380146
        // Stupid hack.
        defaultValueSpinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                defaultValueSpinner.increment(0); // won't change value, but will commit editor
            }
        });
        bitsSpinner.setValueFactory(bitsSpinnerFactory);
        // commit the spinner: http://stackoverflow.com/a/39380146
        bitsSpinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                bitsSpinner.increment(0); // won't change value, but will commit editor
            }
        });

        List<Subscription> currentSubs = new ArrayList<>();

        this.selectedField.bind(fieldsList.selectedFieldProperty());
        getCenter().disableProperty().bind(this.selectedField.isNull());

        this.selectedField.addListener((v, oldField, currentField) -> {
            if (oldField != null) {
                Bindings.unbindBidirectional(nameText.textProperty(), oldField.nameProperty());
                currentSubs.forEach(Subscription::unsubscribe);
                currentSubs.clear();

                valuesTable.setItems(FXCollections.observableArrayList());
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

                valuesTable.setItems(currentField.getValues());
            }
        });

        // Initialize the values table:

        valuesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        nameColumn.prefWidthProperty().bind(valuesTable.widthProperty().multiply(.67));
        valueColumn.prefWidthProperty().bind(valuesTable.widthProperty().multiply(.33));

        Callback<TableColumn<FieldValue, String>, TableCell<FieldValue, String>> cellStrFactory =
                setStringTableColumn -> new EditingStrCell<>();
        Callback<TableColumn<FieldValue, Long>, TableCell<FieldValue, Long>> cellLongFactory =
                setIntegerTableColumn -> new EditingLongCell<>();

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("name"));
        valueColumn.setCellValueFactory(
                new PropertyValueFactory<>("value"));

        //Add for Editable Cell of each field, in String or in Integer
        nameColumn.setCellFactory(cellStrFactory);
        nameColumn.setOnEditCommit(new NamedColumnHandler<>(valuesTable));

        valueColumn.setCellFactory(cellLongFactory);
        valueColumn.setOnEditCommit(ev -> ev.getRowValue().setValue(ev.getNewValue()));

        ValuesHandler valuesHandler = new ValuesHandler();
        valuesHandler.selectedItem.bind(valuesTable.getSelectionModel().selectedItemProperty());

        valuesButtons.setInteractionHandler(valuesHandler);

        dialogButtons.setInteractionHandler(this);
        dialogButtons.setCurrentHelpable(this);
    }

    @Override
    public ObjectProperty<Machine> machineProperty() {
        return machine;
    }

    @Override
    public boolean onOkButtonClick() {
        try {
            fieldsList.checkValidity();
        } catch(ValidationException ex) {
            Dialogs.createErrorDialog(fieldsList.getScene().getWindow(),
                    "Field Error", ex.getMessage()).showAndWait();
            return false;
        }

        return true;
    }

    @Override
    public void onMachineUpdated() {
        
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
        return "Fields";
    }

    @FXML
    protected void handleOkay(ActionEvent ae){
        //get a handle to the stage.
//        Stage stage = (Stage) table.getScene().getWindow();

        // check that all the editing done results in legal fields


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
        

//        stage.close();
    }

    private static class LongSpinnerValueFactory extends SpinnerValueFactory<Long> {
        private final LongProperty min;
        private final LongProperty max;

        public LongSpinnerValueFactory(long min, long max) {
            this.min = new SimpleLongProperty(this, "min", min);
            this.max = new SimpleLongProperty(this, "max", max);

            super.setConverter(new StringConverter<Long>() {
                @Override
                public String toString(Long object) {
                    return object.toString();
                }

                @Override
                public Long fromString(String string) {
                    return ConvertStrings.toLong(string);
                }
            });
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

    class ValuesHandler implements ControlButtonController.InteractionHandler<FieldValue> {

        private ObjectProperty<FieldValue> selectedItem =
                new SimpleObjectProperty<>(this, "selectedItem", null);

        @Override
        public void bindNewButtonDisabled(@Nonnull BooleanProperty toBind) {
            toBind.bind(new ReadOnlyBooleanWrapper(false));
        }

        @Override
        public void bindDeleteButtonDisabled(@Nonnull BooleanProperty toBind) {
            toBind.bind(selectedItem.isNull());
        }

        @Override
        public void bindDuplicateButtonDisabled(@Nonnull BooleanProperty toBind) {
            toBind.bind(selectedItem.isNull());
        }

        @Override
        public void bindItems(@Nonnull Property<ObservableList<FieldValue>> toBind) {
            toBind.bind(valuesTable.itemsProperty());
        }

        @Override
        public void selectionModelBinding(@Nonnull ObjectProperty<SelectionModel<FieldValue>> toBind) {
            toBind.bind(valuesTable.selectionModelProperty());
        }

        @Override
        public Supplier<FieldValue> getSupplier() {
            return () -> new FieldValue("?", UUID.randomUUID(), getMachine(), 0L);
        }
    }
}
