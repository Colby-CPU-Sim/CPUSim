/**
 * Author: Jinghui Yu
 * Editing date: 7/29/2013
 */

/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Changed the return value of checkValidity from a boolean to void (the functionality
 * enabled by that boolean value is now controlled by throwing ValidationException)
 */
package cpusim.gui.editmodules;

import cpusim.Mediator;
import cpusim.gui.util.*;
import cpusim.model.Machine;
import cpusim.model.Module;
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
 * Deals with control over the data that the interface interacts with within the user interface.
 * @param <T>
 */
abstract class ModuleTableController<T extends Module<T>>
        extends TableView<T>
        implements ControlButtonController.InteractionHandler<T>,
                    MachineModificationController<T>,
                    HelpPageEnabled {

    /**
     * Mediator used between the controller and the machine.
     */
    protected Mediator mediator;
    
    /**
     * Underlying machine being modified.
     */
    protected Machine machine;

    private final Class<T> moduleClass;

    private final String fxmlTablePath;
    
    @FXML @SuppressWarnings("unused")
    protected TableColumn<T, String> name;
    
    //-------------------------------
    /**
     * Constructor
     * @param mediator holds the information to be shown in tables
     * @param fxmlFile Name of FXML file to load from
     * @param moduleClass Marker to set the class of the {@link ModuleTableController}
     */
    ModuleTableController(Mediator mediator,
                          final String fxmlFile,
                          final Class<T> moduleClass)
    {
        this.mediator = checkNotNull(mediator);
        this.machine = mediator.getMachine();

        this.fxmlTablePath = checkNotNull(fxmlFile);
        this.moduleClass = checkNotNull(moduleClass);
    
        // Clone all of the modules already in use. This allows us to change the values of the machine without changing
        // the underlying machine values until we are done.
        ObservableList<T> items = getItems();
        items.clear();
        machine.getModule(moduleClass).stream().map(Module::cloneOf).forEach(items::add);

        loadFXML();
    }
    
    /**
     * Initializes the table after {@link FXMLLoader#load()} is called.
     */
    @FXML @SuppressWarnings("unused")
    private void initialize() {
        Callback<TableColumn<T, String>, TableCell<T, String>> cellStrFactory = setStringTableColumn -> new EditingStrCell<>();
        
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(new NamedColumnHandler<>(this));
        
        initializeTable();
    }

    /**
     * Called after the {@link FXMLLoader#load()} is called.
     */
    protected abstract void initializeTable();

    /**
     * Loads the FXML controller, running {@link FXMLLoader} pipeline.
     */
    private void loadFXML() {
        FXMLLoader fxmlLoader = FXMLLoaderFactory.fromRootController(this, fxmlTablePath);

        try {
            fxmlLoader.load();
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
    
    /**
     * Called when a table's tab gets selected, by default this implementation is no-op.
     */
    void onTabSelected() {
        // no-op
    }

    @Override
    public final Machine getMachine() {
        return machine;
    }


    // Defaults to Validatable#all(List)
    @Override
    public void checkValidity(List<T> items) {
        Validatable.all(items);
    }

    @Override
    public void updateMachine() {
        // By default, check the contents, then just replace the machine's values.
        checkValidity(getItems());

        final ObservableList<T> modules = machine.getModule(moduleClass);
        modules.clear();
        modules.addAll(getItems());
    }

    /**
     * Delegate to {@link #checkValidity(List)}
     *
     * @see #checkValidity(List)
     */
    public void checkValidity() {
        checkValidity(getItems());
    }

    @Override
    public abstract T createInstance();

    @Override
    public String getHelpPageID()
    {
        return "Hardware Modules";
    }
    // public abstract methods to be overridden by each subclass

    
    // -- Implementations for ControlButtonController.InteractionHandler
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
        return !getSelectionModel().isEmpty();
    }

    @Override
    public final TableView<T> getTableView() {
        return this;
    }
}