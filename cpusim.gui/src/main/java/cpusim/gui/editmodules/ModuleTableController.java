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

import com.google.common.collect.ImmutableList;
import cpusim.Mediator;
import cpusim.gui.util.EditingStrCell;
import cpusim.gui.util.FXMLLoaderFactory;
import cpusim.gui.util.NamedColumnHandler;
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

import static com.google.common.base.Preconditions.*;

/**
 * Deals with control over the data that the interface interacts with within the user interface.
 * @param <T>
 */
abstract class ModuleTableController<T extends Module<T>>
        extends TableView<T> implements ControlButtonController.ButtonStatusCheck
{
    
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
     */
    ModuleTableController(Mediator mediator, final String fxmlFile, Class<T> moduleClass)
    {
        this.mediator = checkNotNull(mediator);
        this.machine = mediator.getMachine();
        
        this.moduleClass = checkNotNull(moduleClass);
        this.fxmlTablePath = checkNotNull(fxmlFile);
    
        // Clone all of the modules already in use. This allows us to change the values of the machine without changing
        // the underlying machine values until we are done.
        ObservableList<T> items = getItems();
        items.clear();
        machine.getModule(moduleClass).stream().map(Module::cloneOf).forEach(items::add);
    }
    
    /**
     * Initializes the table after {@link FXMLLoader#load()} is called.
     */
    @FXML @SuppressWarnings("unused")
    void initialize() {
        Callback<TableColumn<T, String>, TableCell<T, String>> cellStrFactory = setStringTableColumn -> new EditingStrCell<T>();
        
        name.setCellValueFactory(new PropertyValueFactory<T, String>("name"));
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(new NamedColumnHandler<T>(this));
        
        initializeTable(this);
        
        ObservableList<TableColumn<T, ?>> columns = getColumns();
        columns.clear();
        columns.addAll(getSubTableColumns());
        
        columns.forEach(c -> {
            c.setMinWidth(80);
        });
    }
    
    /**
     * Loads the FXML controller, running {@link FXMLLoader} pipeline.
     */
    void loadFXML() {
        FXMLLoader fxmlLoader = FXMLLoaderFactory.fromRootController(this, fxmlTablePath);

        try {
            fxmlLoader.load();
        } catch (IOException ioe) {
            // should never happen
            throw new IllegalStateException("Unable to load file: " + fxmlTablePath, ioe);
        }
    }
    
    /**
     * Factory function to get a {@link ControlButtonController} instance. By default, it returns
     * the default implementations of the {@link ControlButtonController}. Override this method to
     * change the behaviour of the {@link ControlButtonController}.
     *
     * @return Non-{@code null} instance of {@link ControlButtonController}.
     */
    protected ControlButtonController<T> createControlButtonController() {
        return new ControlButtonController<T>(this, this, false);
    }
    
    /**
     * Called when a table's tab gets selected, by default this implementation is no-op.
     */
    void onTabSelected() {
        // no-op
    }
    
    /**
     * Get the current machine in use for the {@link ModuleTableController}.
     * @return Reference to the machine.
     */
    final Machine getMachine() {
        return machine;
    }

    protected abstract void initializeTable(TableView<T> tableView);

    /**
     * Get a {@link ImmutableList} of all {@link TableColumn} values that are present.
     * @return Non-{@code null} List of columns
     */
    protected abstract ImmutableList<TableColumn<T, ?>> getSubTableColumns();
    
    /**
     * returns the prototype of the right subclass
     *
     * @return the prototype of the right subclass
     */
    public abstract T getPrototype();
    
    /**
     * returns the class object for the controller's objects
     *
     * @return the class object for the controller's objects
     */
    public final Class<T> getModuleClass() {
        return moduleClass;
    }
    
    /**
     * returns the help page ID
     * @return a string of the ID
     */
    public String getHelpPageID()
    {
        return "Hardware Modules";
    }
    
    //========================================
    // public abstract methods to be overridden by each subclass
    
    /**
     * check if the given list of micro instructions have valid values. The default implementation uses
     * {@link Validatable#all(List)}.
     *
     * @throws cpusim.model.util.ValidationException if it is invalid
     */
    public void checkValidity() {
        Validatable.all(getItems());
    }
    
    // -- Implementations for ControlButtonController.ButtonStatusCheck
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
    
}