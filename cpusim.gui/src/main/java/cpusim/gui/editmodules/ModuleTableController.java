package cpusim.gui.editmodules;

import cpusim.gui.util.*;
import cpusim.gui.util.table.EditingStrCell;
import cpusim.model.Machine;
import cpusim.model.module.Module;
import cpusim.model.util.Validatable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.fxmisc.easybind.EasyBind;

import java.io.IOException;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Deals with control over the data that the interface interacts with within the user interface.
 * @param <T> Module subtype
 */
public abstract class ModuleTableController<T extends Module<T>>
        extends TableView<T>
        implements ControlButtonController.InteractionHandler<T>,
                    MachineModificationController,
                    HelpPageEnabled {

    /**
     * Underlying machine being modified.
     */
    private final ObjectProperty<Machine> machine;

    private final Class<T> moduleClass;

    private final String fxmlTablePath;
    
    @FXML @SuppressWarnings("unused")
    protected TableColumn<T, String> name;
    
    //-------------------------------
    /**
     * Constructor
     * @param fxmlFile Name of FXML file to load from
     * @param moduleClass Marker to set the class of the {@link ModuleTableController}
     */
    ModuleTableController(final String fxmlFile,
                          final Class<T> moduleClass)
    {
        this.machine = new SimpleObjectProperty<>(null);

        this.fxmlTablePath = checkNotNull(fxmlFile);
        this.moduleClass = checkNotNull(moduleClass);
    }
    
    /**
     * Initializes the table after {@link FXMLLoader#load()} is called.
     */
    @FXML @SuppressWarnings("unused")
    protected void initialize() {
        Callback<TableColumn<T, String>, TableCell<T, String>> cellStrFactory = setStringTableColumn -> new EditingStrCell<>();
        
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(new NamedColumnHandler<>(this));

        EasyBind.subscribe(machineProperty(), (newValue) -> {
            // Clone all of the modules already in use. This allows us to change the values of the machine without changing
            // the underlying machine values until we are done.
            if (newValue != null) {
                ObservableList<T> items = getItems();
                items.clear();
                newValue.getModules(moduleClass).stream().map(Module::cloneOf).forEach(items::add);
            }
        });

        initializeTable();
    }

    /**
     * Called after the {@link FXMLLoader#load()} is called.
     */
    protected void initializeTable() {}

    /**
     * Loads the FXML controller, running {@link FXMLLoader} pipeline.
     */
    protected void loadFXML() {
        try {
            FXMLLoaderFactory.fromRootController(this, fxmlTablePath).load();
        } catch (IOException ioe) {
            // should never happen
            throw new IllegalStateException("Unable to load file: " + fxmlTablePath, ioe);
        }
    }
    
    /**
     * Factory function to get a {@link ModuleControlButtonController} instance. By default, it returns
     * the default implementations of the {@link ModuleControlButtonController}. Override this method to
     * change the behaviour of the {@link ModuleControlButtonController}.
     *
     * @return Non-{@code null} instance of {@link ModuleControlButtonController}.
     */
    protected ControlButtonController<T> createControlButtonController() {
        return new ModuleControlButtonController<>(this, false);
    }
    
    @Override
    public ObjectProperty<Machine> machineProperty() {
        return machine;
    }
    
    @Override
    public void updateMachine() {
        // By default, check the contents, then just replace the machine's values.
        final ObservableList<T> modules = machine.get().getModules(moduleClass);
        modules.clear();
        modules.addAll(getItems());
    }

    @Override
    public void checkValidity() {
        Validatable.all(getItems());
    }

    @Override
    public abstract Supplier<T> supplierBinding();

    @Override
    public String getHelpPageID()
    {
        return "Hardware Modules";
    }
    // public abstract methods to be overridden by each subclass

    
    // -- Implementations for ControlButtonController.InteractionHandler

    protected final BooleanBinding selectedItemIsNotNullBinding() {
        return ControlButtonController.selectedItemIsNotNullBinding(selectionModelProperty());
    }

    @Override
    public BooleanBinding newButtonEnabledBinding() {
        return Bindings.createBooleanBinding(() -> true);
    }

    @Override
    public BooleanBinding deleteButtonEnabledBinding() {
        return selectedItemIsNotNullBinding();
    }

    @Override
    public BooleanBinding duplicateButtonEnabledBinding() {
        return selectedItemIsNotNullBinding();
    }

    @Override
    public BooleanBinding propertiesButtonEnabledBinding() {
        return selectedItemIsNotNullBinding();
    }

    @Override
    public ObjectBinding<ObservableList<T>> itemsBinding() {
        return Bindings.createObjectBinding(() -> itemsProperty().get(), itemsProperty());
    }

    @Override
    public ObjectBinding<SelectionModel<T>> selectionModelBinding() {
        return Bindings.createObjectBinding(() -> selectionModelProperty().get(), selectionModelProperty());
    }
}