package cpusim.gui.editmachineinstruction;

import cpusim.gui.util.*;
import cpusim.gui.util.list.StringPropertyListCell;
import cpusim.model.Field;
import cpusim.model.Machine;
import cpusim.model.MachineInstruction;
import cpusim.model.util.MachineBound;
import cpusim.util.Dialogs;
import javafx.beans.NamedArg;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fxmisc.easybind.EasyBind;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Controller that stores a {@link javafx.scene.control.ListView} and {@link javafx.scene.control.Button}
 *
 * @since 2016-12-09
 */
public class FieldListControl
        extends VBox
        implements MachineBound,
                   MachineModificationController,
                   ControlButtonController.InteractionHandler<Field> {

    private static final String FXML_FILE = "FieldList.fxml";

    private final Logger logger = LogManager.getLogger(FieldListControl.class);

    @FXML @SuppressWarnings("unused")
    private HBox titleHBox;

    @FXML @SuppressWarnings("unused")
    private Label title;

    @FXML @SuppressWarnings("unused")
    private Button showEditFieldsButton;

    @FXML @SuppressWarnings("unused")
    private ListView<Field> fieldListView;

    @FXML @SuppressWarnings("unused")
    private DefaultControlButtonController<Field> controlButtons;

    private final ObjectProperty<Machine> machine;

    private final ObjectProperty<Field> selectedField;

    private final ObjectProperty<EventHandler<ActionEvent>> onShowEditAction;

    private final StringProperty titleText;

    private final boolean allowDrag;
    private final boolean allowEditing;

    public FieldListControl(@NamedArg("allowDrag") boolean allowDrag,
                            @NamedArg("allowEditing") boolean allowEditing) {

        this.machine = new SimpleObjectProperty<>(this, "machine", null);
        this.titleText = new SimpleStringProperty(this, "titleText", null);
        this.selectedField = new SimpleObjectProperty<>(this, "selectedField");
        this.onShowEditAction = new SimpleObjectProperty<>(this, "onShowEditAction");

        this.allowDrag = allowDrag;
        this.allowEditing = allowEditing;

        try {
            FXMLLoaderFactory.fromRootController(this, FXML_FILE).load();
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    @FXML @SuppressWarnings("unused")
    void initialize() {

        titleText.bindBidirectional(title.textProperty());

        if (!allowEditing) {
            getChildren().remove(controlButtons);
        }

        controlButtons.setInteractionHandler(this);

        EasyBind.subscribe(this.onShowEditAction, value -> {
            if (value == null) {
                titleHBox.getChildren().remove(showEditFieldsButton);
            } else {
                titleHBox.getChildren().add(showEditFieldsButton);
            }
        });

        this.showEditFieldsButton.onActionProperty().bind(onShowEditAction);

        selectedField.bind(fieldListView.getSelectionModel().selectedItemProperty());

        fieldListView.setCellFactory(param -> {
            ListCell<Field> cell = new StringPropertyListCell<Field>(Field::nameProperty);

            cell.setEditable(allowEditing);

            // Starting a drag
            cell.setOnDragDetected(event -> {
                if (allowDrag && !cell.isEmpty()) {
                    Field field = cell.getItem();
                    Dragboard db = cell.startDragAndDrop(TransferMode.COPY);
                    db.setDragView(cell.snapshot(null, null));

                    DragHelper helper = new DragHelper(machineProperty(), db);
                    helper.setFieldContent(field);

                    event.setDragDetect(true);
                    event.consume();
                    logger.trace("row.setOnDragDetected() started MOVE, field = \"{}\"@{}",
                            field.getName(), field.getID());
                }
            });

            // A drag entered the row
//            cell.setOnDragEntered(ev -> {});

            // Drag moved over, must set Transfer if used
//            cell.setOnDragOver(ev -> {});

            // Drag left the row
//            row.setOnDragExited(ev -> {});

            // This row started a drag, and is now done
            cell.setOnDragDone(ev -> {
                if (ev.getTransferMode() == TransferMode.COPY) {
                    logger.trace("Row Drag successfully completed: row.getItem() = {}", cell.getItem());
                    ev.consume();
                }
            });

           return cell;
        });

        EasyBind.subscribe(machine, newMachine -> {
            if (newMachine != null) {
                fieldListView.itemsProperty().bindBidirectional(newMachine.fieldsProperty());
            }
        });

    }

    @Override
    public ObjectProperty<Machine> machineProperty() {
        return machine;
    }

    public Field getSelectedField() {
        return selectedField.get();
    }

    public ReadOnlyObjectProperty<Field> selectedFieldProperty() {
        return selectedField;
    }

    public void setOnShowEditAction(EventHandler<ActionEvent> onShowEditAction) {
        this.onShowEditAction.set(onShowEditAction);
    }

    public EventHandler<ActionEvent> getOnShowEditAction() {
        return this.onShowEditAction.get();
    }

    @Override
    public void bindNewButtonDisabled(@Nonnull BooleanProperty toBind) {
        toBind.bind(new ReadOnlyBooleanWrapper(false));
    }

    @Override
    public void bindDeleteButtonDisabled(@Nonnull BooleanProperty toBind) {
        toBind.bind(selectedField.isNull());
    }

    @Override
    public void bindDuplicateButtonDisabled(@Nonnull BooleanProperty toBind) {
        toBind.bind(selectedField.isNull());
    }

    @Override
    public void bindItems(@Nonnull Property<ObservableList<Field>> toBind) {
        toBind.bind(fieldListView.itemsProperty());
    }

    @Override
    public void selectionModelBinding(@Nonnull ObjectProperty<SelectionModel<Field>> toBind) {
        toBind.bind(fieldListView.selectionModelProperty());
    }

    @Override
    public Supplier<Field> getSupplier() {
        return () -> new Field("?", UUID.randomUUID(), getMachine(),
                0, Field.Relativity.absolute, FXCollections.observableArrayList(),
                0L, Field.SignedType.Unsigned, Field.Type.required);
    }

    @Override
    public boolean checkDelete(@Nonnull Field value) {
        //check if the selected field is contained in any instructions.  If so, inform the
        //user and ask him/her to confirm the delete
        List<MachineInstruction> instructions = getMachine().getInstructionsThatUse(selectedField.get());
        if (!instructions.isEmpty()) {
            Alert dialog = Dialogs.createConfirmationDialog(getScene().getWindow(),
                    "Delete Field In Use?", "The following instructions contain this field:"+
                            System.lineSeparator() + instructions
                            +"If you delete this field all instances of "
                            + "this field will be removed.  Are you sure you want to delete this "
                            + "field?");

            Optional<ButtonType> result = dialog.showAndWait();

            if (!result.isPresent() || result.get() != ButtonType.OK) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void updateMachine() {

    }

    @Override
    public void checkValidity() {

    }
}
