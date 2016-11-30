package cpusim.gui.util;

import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.util.Copyable;
import cpusim.model.util.NamedObject;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

import java.io.IOException;

/**
 * Controller for a "New", "Duplicate", and "Remove" button.
 * @since 2016-11-29
 */
public abstract class ControlButtonController<T extends NamedObject & Copyable<T>> extends HBox {

    /**
     * Defines the FXML file used to load the controls.
     */
    private static final String FXML_PATH = "TableControlButtons.fxml";

    @FXML @SuppressWarnings("unused")
    private Button newButton;
    @FXML @SuppressWarnings("unused")
    private Button deleteButton;
    @FXML @SuppressWarnings("unused")
    private Button duplicateButton;
    @FXML @SuppressWarnings("unused")
    private Button propertiesButton;

    private final InteractionHandler<T> interactionHandler;

    private final ButtonChangeListener buttonListener;

    private final boolean hasExtendedProperties;

    public ControlButtonController(final boolean hasExtendedProperties, InteractionHandler<T> interactionHandler) {
        this.hasExtendedProperties = hasExtendedProperties;
        this.interactionHandler = interactionHandler;
        this.buttonListener = new ButtonChangeListener();

        FXMLLoader fxmlLoader = FXMLLoaderFactory.fromRootController(this, FXML_PATH);
        try {
            fxmlLoader.load();
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    @FXML @SuppressWarnings("unused")
    private void initialize() {
        if (!hasExtendedProperties) {
            getChildren().remove(propertiesButton);
        }

        initializeSubclass();
    }

    protected abstract void initializeSubclass();


    /**
     * Gets a {@link ChangeListener} that updates the "enabled" state of the buttons in the control if there is a
     * change.
     * @return Non-{@code null} ChangeListener.
     */
    protected ChangeListener<T> getButtonChangeListener() {
        return buttonListener;
    }

    public void updateControlButtonStatus() {
        newButton.setDisable(!interactionHandler.isNewButtonEnabled());
        deleteButton.setDisable(!interactionHandler.isDelButtonEnabled());
        duplicateButton.setDisable(!interactionHandler.isDupButtonEnabled());
        propertiesButton.setDisable(!interactionHandler.isPropButtonEnabled());
    }

    /**
     * creates a new instruction when clicking on New button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML @SuppressWarnings("unused")
    protected void onNewButtonClick(ActionEvent e) {
        //add a new item at the end of the list.
        TableView<T> table = interactionHandler.getTableView();
        String uniqueName = NamedObject.createUniqueName(table.getItems(), '?');
        final T newValue = interactionHandler.createInstance();
        newValue.setName(uniqueName);

        table.getItems().add(newValue);
        table.getSelectionModel().select(newValue);

        updateControlButtonStatus();
    }

    /**
     * Return {@code false} if the value to delete is not acceptable. This is an extension point
     * for subclasses, by default, this method checks that the module is not in use by any
     * {@link Microinstruction} instances, and if so, it will confirm with the user before returning
     * {@code true}.
     *
     * @param toDelete Value that is requested to be deleted.
     * @return {@code true} if the deletion is allowed.
     */
    protected abstract boolean checkDelete(T toDelete);

    /**
     * deletes an existing instruction when clicking on Delete button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML @SuppressWarnings("unused")
    protected void onDeleteButtonClick(ActionEvent e) {
        TableView<T> table = interactionHandler.getTableView();
        SelectionModel<T> selectionModel = table.getSelectionModel();
        final T selectedValue = selectionModel.getSelectedItem();

        if (!checkDelete(selectedValue)) {
            return;
        }

        table.getItems().remove(selectionModel.getSelectedIndex());
        final int selected = selectionModel.getSelectedIndex();
        if (selected == 0) {
            selectionModel.select(0);
        } else {
            selectionModel.select(selected - 1);
        }

        updateControlButtonStatus();
    }

    /**
     * duplicates the selected instruction when clicking on Duplicate button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML @SuppressWarnings("unused")
    protected void onDuplicateButtonClick(ActionEvent e) {
        //add a new item at the end of the list.
        final TableView<T> table = interactionHandler.getTableView();
        final T newObject = table.getSelectionModel().getSelectedItem().cloneOf();
        newObject.setName(newObject.getName() + "_copy");
        table.getItems().add(0, newObject);

        table.scrollTo(0);
        table.getSelectionModel().select(0);

        updateControlButtonStatus();
    }

    /**
     * edits the selected register array
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML @SuppressWarnings("unused")
    protected void onPropertiesButtonClick(ActionEvent e) {
        if (!hasExtendedProperties) {
            throw new IllegalStateException("Called onPropertiesButtonClick on something with no extended properties");
        } else {
            throw new Error("The method, onPropertiesButtonClick(ActionEvent) must be implemented if advanced " +
                    "properties are present.");
        }
    }

    /**
     * Interface to define what status checks are important for the status.
     */
    public interface InteractionHandler<T> {

        /**
         * Denotes if the "New" button is enabled
         * @return {@code true} if the button is enabled.
         */
        boolean isNewButtonEnabled();

        /**
         * Denotes if the delete button is enabled.
         * @return {@code true} if the button is enabled.
         */
        boolean isDelButtonEnabled();

        /**
         * Denotes if the delete button is enabled.
         * @return {@code true} if the button is enabled.
         */
        boolean isDupButtonEnabled();

        /**
         * Denotes if the properties button is enabled.
         * @return {@code true} if the button is enabled.
         */
        boolean isPropButtonEnabled();

        /**
         * Get the {@link TableView} this control is associated with.
         * @return Reference to the {@link TableView}
         */
        TableView<T> getTableView();

        /**
         * Creates a new instance
         * @return a new, non-{@code null} instance
         */
        T createInstance();
    }

    /**
     * a listener listening for changes to the table selection and
     * update the status of buttons.
     */
    private class ButtonChangeListener implements ChangeListener<T> {

        @Override
        public void changed(ObservableValue<? extends T> selected, T oldModule, T newModule) {
            updateControlButtonStatus();
        }
    }
}
