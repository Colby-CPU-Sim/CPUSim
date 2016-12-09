package cpusim.gui.util;

import cpusim.gui.help.HelpController;
import cpusim.model.Machine;
import cpusim.model.util.ReadOnlyMachineBound;
import cpusim.model.util.ValidationException;
import cpusim.util.Dialogs;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implements the controller for the buttons at the bottom of the dialogs (e.g. OK, Cancel and ?).
 *
 * @since 2016-11-30
 */
public final class DialogButtonController extends VBox implements ReadOnlyMachineBound {

    /**
     * FXML file loaded for the controls.
     */
    private static final String FXML_FILE = "DialogControls.fxml";

    @FXML @SuppressWarnings("unused")
    private Button okButton;

    @FXML @SuppressWarnings("unused")
    private Button cancelButton;

    @FXML @SuppressWarnings("unused")
    private Button helpButton;

    private final ObjectProperty<Machine> machine;

    private InteractionHandler interactionHandler;

    /**
     * {@link List} of {@link MachineModificationController}s that can be used to update the machine when the
     * dialog tries to close.
     */
    private Set<MachineModificationController> controllers;

    private final ObjectProperty<HelpPageEnabled> currentHelpable;

    /**
     * After construction, the caller must set:
     * <ul>
     *     <li>{@link #setInteractionHandler(InteractionHandler)}</li>
     *     <li>{@link #setControllers(Iterable)}</li>
     *     <li>Bind a {@link Machine} property to {@link #machineProperty()}</li>
     * </ul>
     *
     * This must be done <strong>before</strong> any interactions occur, otherwise behaviour is undefined (likely
     * causing {@link NullPointerException}s).
     */
    public DialogButtonController() {
        this.interactionHandler = null;
        this.machine = new SimpleObjectProperty<>(this, "machine", null);
        this.currentHelpable = new SimpleObjectProperty<>(this, "currentHelpable", null);
        this.controllers = new HashSet<>();

        FXMLLoader fxmlLoader = FXMLLoaderFactory.fromRootController(this, FXML_FILE);

        try {
            fxmlLoader.load();
        } catch (IOException ioe) {
            // should never happen
            throw new IllegalStateException("Unable to load file: " + FXML_FILE, ioe);
        }
    }

    @FXML @SuppressWarnings("unused")
    private void initialize() {
        this.currentHelpable.addListener((observable, oldValue, newValue) -> {
            helpButton.setDisable(newValue == null);
        });
        helpButton.setDisable(currentHelpable.get() == null);
    }

    @Override
    public ReadOnlyObjectProperty<Machine> machineProperty() {
        return machine;
    }
    
    /**
     * Convenience method to set the required parameters outlined by {@link #DialogButtonController()}. Delegates to
     * other methods.
     *
     * @param machineProperty The {@link Machine} this instance "looks after"
     * @param handler Handles all interactions, see {@link #setInteractionHandler(InteractionHandler)}.
     * @param controllers Controllers that will cause the underlying {@link Machine} to update, see
     *                      {@link #setControllers(Iterable)}.
     *
     * @see #DialogButtonController()
     * @see #setInteractionHandler(InteractionHandler)
     * @see #setControllers(Iterable)
     */
    public void setRequired(ObjectProperty<Machine> machineProperty,
                            InteractionHandler handler,
                            MachineModificationController... controllers) {
        this.machine.bind(checkNotNull(machineProperty));

        setInteractionHandler(handler);
        setControllers(Arrays.asList(controllers));
    }

    /**
     * Get the property for the current instance that can be "helped".
     * @return Read-only version of the property.
     */
    public ReadOnlyObjectProperty<HelpPageEnabled> currentHelpableProperty() {
        return currentHelpable;
    }

    /**
     * Sets the {@link InteractionHandler} for this controller. This must be set before interactions occur.
     * @param interactionHandler new instance to handle interactions with this controller.
     */
    public void setInteractionHandler(InteractionHandler interactionHandler) {
        this.interactionHandler = checkNotNull(interactionHandler);
    }

    /**
     * Copy the parameter to the {@link Set} of controllers that will be updated when closing the dialog.
     * @param controllers
     */
    public void setControllers(Iterable<? extends MachineModificationController> controllers) {
        this.controllers.clear();
        controllers.forEach(this.controllers::add);
    }

    /**
     * Get the {@link ObservableList} of controllers. Changing these values has no effect outside of when the
     * "OK" button is pressed.
     * @return List of controller values
     */
    public Set<MachineModificationController> getControllers() {
        return this.controllers;
    }

    /**
     * Gets the currently in use {@link HelpPageEnabled} entity.
     * @return Optional with content if an entity is present.
     */
    public Optional<HelpPageEnabled> getCurrentHelpable() {
        return Optional.ofNullable(currentHelpable.get());
    }

    /**
     * Sets the {@link HelpPageEnabled} entity, so when
     * @param currentHelpable Non-{@code null} {@code HelpPageEnabled} entity that will be used to show a help content.
     */
    public void setCurrentHelpable(@Nullable HelpPageEnabled currentHelpable) {
        this.currentHelpable.setValue(currentHelpable);
    }

    /**
     * Convenience method to close the parent {@link Stage}.
     */
    private void closeParentWindow() {
        ((Stage) okButton.getScene().getWindow()).close();
    }

    /**
     * save the current changes to the {@link cpusim.model.Machine} via
     * {@link MachineModificationController#updateMachine()} and close the window when clicking on OK button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML @SuppressWarnings("unused")
    private void onOKButtonClick(ActionEvent e) {
        if (!interactionHandler.onOkButtonClick()) {
            // the owner said "no" and no means no.
            return ;
        }

        try {
            controllers.forEach(MachineModificationController::checkValidity);

            controllers.forEach(MachineModificationController::updateMachine);

            // Now we tell the parent that we updated the Machine (they can do some extra actions as required).
            interactionHandler.onMachineUpdated();

            // Close the stage
            closeParentWindow();
        } catch (ValidationException ex) {
            Dialogs.createErrorDialog(okButton.getScene().getWindow(),
                    "Error",
                    ex.getMessage()).showAndWait();
        }
    }

    /**
     * close the window without saving the changes.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML @SuppressWarnings("unused")
    private void onCancelButtonClick(ActionEvent e) {
        if (interactionHandler.onCancelButtonClick()) {
            // Close the stage
            closeParentWindow();
        }
    }

    /**
     * open a help window when clicking on the help button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML @SuppressWarnings("unused")
    private void onHelpButtonClick(ActionEvent e) {
        if (interactionHandler.onHelpButtonClick()) {
            getCurrentHelpable().ifPresent(helpable ->
                    interactionHandler.displayHelpDialog(helpable.getHelpPageID()));
        }
    }

    /**
     * Defines required interactions between {@link DialogButtonController} and the owning class.
     */
    public interface InteractionHandler {

        /**
         * Called before running the content of {@link DialogButtonController#onOKButtonClick(ActionEvent)}.
         *
         * @return {@code true} to continue.
         */
        boolean onOkButtonClick();

        /**
         * Called after the machine gets updated, usually by
         * {@link DialogButtonController#onOKButtonClick(ActionEvent)}.
         */
        void onMachineUpdated();

        /**
         * Called before running the content of {@link DialogButtonController#onHelpButtonClick(ActionEvent)}.
         *
         * @return {@code true} to continue.
         */
        boolean onHelpButtonClick();

        /**
         * Displays the {@link HelpController} dialog over the current scene.
         * @param helpPageId The ID of the help page requested.
         */
        void displayHelpDialog(final String helpPageId);

        /**
         * Called before running the content of {@link DialogButtonController#onCancelButtonClick(ActionEvent)}.
         *
         * @return {@code true} to continue.
         */
        boolean onCancelButtonClick();
    }
}
