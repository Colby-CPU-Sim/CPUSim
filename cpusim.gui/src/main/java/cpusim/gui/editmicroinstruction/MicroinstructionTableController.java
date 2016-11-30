package cpusim.gui.editmicroinstruction;

import cpusim.Mediator;
import cpusim.gui.util.*;
import cpusim.model.Machine;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import cpusim.model.util.Copyable;
import cpusim.model.util.Validatable;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * MicroinstructionTableController class parent of all the microinstruction controllers
 */
abstract class MicroinstructionTableController<T extends Microinstruction & Copyable<T>>
        extends TableView<T>
        implements ControlButtonController.InteractionHandler<T>,
                    MachineModificationController<T>,
                    cpusim.gui.util.HelpPageEnabled {


    protected final Mediator mediator;
    protected final Machine machine;      //the current machine being simulated

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
        this.mediator = mediator;
        this.machine = mediator.getMachine();
        this.fxmlFile = checkNotNull(fxmlFile);
        this.microClass = checkNotNull(clazz);

        // Clone all of the modules already in use. This allows us to change the values of the machine without changing
        // the underlying machine values until we are done.
        final ObservableList<T> items = getItems();
        items.clear();
        machine.getMicros(clazz).stream().map(Copyable::cloneOf).forEach(items::add);
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
    final void initialize() {
        // This is duplicated within ModuleTableController, but it will likely change thus its not worth
        // actually moving to a common interface/class.
        Callback<TableColumn<T, String>, TableCell<T, String>> cellStrFactory = setStringTableColumn -> new EditingStrCell<>();

        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(new NamedColumnHandler<>(this));

        initializeTable();
    }

    /**
     * Initializes the {@link TableView} and it's {@link TableColumn} values. This is called after the
     * {@link FXMLLoader#load()} call.
     */
    abstract void initializeTable();

    /**
     * Get the {@code fx:id} used for this controller.
     * @return ID string.
     */
    abstract String getFxId();

    @Override
    public final Machine getMachine() {
        return machine;
    }

    /**
     * Get the {@link Class} of the value {@code T} for this controller.
     * @return Class of the generic {@code T}
     */
    public final Class<? extends Microinstruction> getMicroClass() {
        return microClass;
    }

    @Override
    public void updateMachine() {
        checkValidity(getItems());

        ObservableList<T> machineMicros = machine.getMicros(microClass);
        machineMicros.clear();
        machineMicros.addAll(getItems());
    }

    @Override
    public void checkValidity(List<T> micros) {
        Validatable.all(micros);
    }

    /**
     * Delegate method to {@link #checkValidity(List)} passing the result from {@link #getItems()}.
     */
    public final void checkValidity() {
        checkValidity(getItems());
    }

    /**
     * Checks if the underling {@link Machine} has registers present. This is a convenience method.
     * @return {@code true} if there is at least one {@link Register} available.
     */
    protected boolean areRegistersAvailable() {
        return !(machine.getModule(Register.class).isEmpty()
                && machine.getModule(RegisterArray.class).isEmpty());
    }

    /**
     * Creates a {@link ControlButtonController} instance
     * @return Non-{@code null} instance
     */
    ControlButtonController<T> createControlButtonController() {
        return new MicroinstructionControlButtonController<>(this);
    }

    /**
     * Called when a table's tab gets selected, by default this implementation is no-op.
     */
    void onTabSelected() {
        // no-op
    }

    /**
     * returns the type of the controller
     *
     * @return the type of the controller
     */
    @Override
    public abstract String toString();

    @Override
    public abstract T createInstance();

    @Override
    public boolean isNewButtonEnabled() {
        return true;
    }

    @Override
    public boolean isDelButtonEnabled() {
        return !getSelectionModel().isEmpty();
    }

    @Override
    public boolean isDupButtonEnabled() {
        return !getSelectionModel().isEmpty();
    }

    @Override
    public boolean isPropButtonEnabled() {
        return false;
    }

    @Override
    public final TableView<T> getTableView() {
        return this;
    }
}
