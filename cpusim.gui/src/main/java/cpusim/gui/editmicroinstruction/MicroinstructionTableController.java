package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.ControlButtonController;
import cpusim.gui.util.FXMLLoaderFactory;
import cpusim.gui.util.MachineModificationController;
import cpusim.gui.util.NamedColumnHandler;
import cpusim.gui.util.table.EditingStrCell;
import cpusim.model.Machine;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.module.Register;
import cpusim.model.util.Copyable;
import cpusim.model.util.ReadOnlyMachineBound;
import cpusim.model.util.Validatable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * MicroinstructionTableController class parent of all the microinstruction controllers
 */
abstract class MicroinstructionTableController<T extends Microinstruction<T>>
        extends TableView<T>
        implements ControlButtonController.InteractionHandler<T>,
                    MachineModificationController,
                    cpusim.gui.util.HelpPageEnabled,
        ReadOnlyMachineBound {

    protected final ObjectProperty<Machine> machine;      //the current machine being simulated

    /**
     * The file that this table is loaded from.
     */
    private final String fxmlFile;

    private final Class<T> microClass;

    @FXML @SuppressWarnings("unused")
    protected TableColumn<T, String> name;

    /**
     * Constructor
     * @param mediator holds the information to be shown in tables
     */
    MicroinstructionTableController(Mediator mediator, final String fxmlFile, Class<T> clazz) {
        this.machine = new SimpleObjectProperty<>(this, "machine", null);
        this.machine.bind(mediator.machineProperty());

        this.fxmlFile = checkNotNull(fxmlFile);
        this.microClass = checkNotNull(clazz);
    }

    final void loadFXML() {
        FXMLLoader fxmlLoader = FXMLLoaderFactory.fromRootController(this, fxmlFile);

        try {
            fxmlLoader.load();
        } catch (IOException ioe) {
            // should never happen
            throw new IllegalStateException("Unable to load file: " + fxmlFile, ioe);
        }
    }

    @FXML @SuppressWarnings("unused")
    void initialize() {
        // This is duplicated within ModuleTableController, but it will likely change thus its not worth
        // actually moving to a common interface/class.
        Callback<TableColumn<T, String>, TableCell<T, String>> cellStrFactory = setStringTableColumn -> new EditingStrCell<>();

        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(new NamedColumnHandler<>(this));

        initializeTable();

        ChangeListener<Machine> onUpdateMachine = (observable, oldValue, newValue) -> {
            // Clone all of the modules already in use. This allows us to change the values of the machine without changing
            // the underlying machine values until we are done.
            final ObservableList<T> items = getItems();
            items.clear();
            machine.get().getMicros(microClass).stream().map(Copyable::cloneOf).forEach(items::add);
        };

        onUpdateMachine.changed(machine, machine.getValue(), machine.getValue());
        machine.addListener(onUpdateMachine);
    }

    /**
     * Initializes the {@link TableView} and it's {@link TableColumn} values. This is called after the
     * {@link FXMLLoader#load()} call.
     */
    protected void initializeTable() {}

    /**
     * Get the {@code fx:id} used for this controller.
     * @return ID string.
     */
    abstract String getFxId();

    @Override
    public ObjectProperty<Machine> machineProperty() {
        return machine;
    }

    @Override
    public final Machine getMachine() {
        return machine.get();
    }

    /**
     * Get the {@link Class} of the value {@code T} for this controller.
     * @return Class of the generic {@code T}
     */
    final Class<? extends Microinstruction> getMicroClass() {
        return microClass;
    }

    @Override
    public void updateMachine() {
        ObservableList<T> machineMicros = machine.get().getMicros(microClass);
        machineMicros.clear();
        machineMicros.addAll(getItems());
    }

    @Override
    public void checkValidity() {
        Validatable.all(getItems());
    }

    /**
     * Checks if the underling {@link Machine} has registers present. This is a convenience method.
     * @return {@code true} if there is at least one {@link Register} available.
     * @param toBind
     */
    protected void bindAreRegistersNotAvailable(BooleanProperty toBind) {
        toBind.bind(Bindings.isEmpty(getMachine().getAllRegisters()));
    }

    /**
     * Creates a {@link ControlButtonController} instance
     * @return Non-{@code null} instance
     */
    ControlButtonController<T> createControlButtonController() {
        return new MicroinstructionControlButtonController<>(this);
    }

    private void bindSelectedItemIsNull(@Nonnull BooleanProperty toBind) {
        ControlButtonController.bindSelectedItemIsNull(toBind, selectionModelProperty());
    }

    @Override
    public abstract Supplier<T> getSupplier();

    @Override
    public void bindNewButtonDisabled(@Nonnull BooleanProperty toBind) {
        toBind.bind(new ReadOnlyBooleanWrapper(false));
    }

    @Override
    public void bindDeleteButtonDisabled(@Nonnull BooleanProperty toBind) {
        bindSelectedItemIsNull(toBind);
    }

    @Override
    public void bindDuplicateButtonDisabled(@Nonnull BooleanProperty toBind) {
        bindSelectedItemIsNull(toBind);
    }

    @Override
    public void bindItems(@Nonnull Property<ObservableList<T>> toBind) {
        toBind.bind(itemsProperty());
    }

    @Override
    public void selectionModelBinding(@Nonnull ObjectProperty<SelectionModel<T>> toBind) {
        toBind.bind(selectionModelProperty());
    }
}
