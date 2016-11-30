package cpusim.gui.util;

import com.google.common.collect.ImmutableList;
import cpusim.Mediator;
import cpusim.gui.help.HelpController;
import cpusim.model.util.ValidationException;
import cpusim.util.Dialogs;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implements the controller for the buttons at the bottom of the dialogs (e.g. OK, Cancel and ?).
 *
 * @since 30/11/2016.
 */
public final class DialogButtonController extends VBox {

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

    private final Mediator mediator;

    private final InteractionHandler interactionHandler;

    /**
     * {@link List} of {@link MachineModificationController}s that can be used to update the machine when the
     * dialog tries to close.
     */
    private final ImmutableList<MachineModificationController<?>> controllers;

    private HelpPageEnabled currentHelpable;

    public DialogButtonController(Mediator mediator,
                                  InteractionHandler interactionHandler,
                                  Iterable<? extends MachineModificationController<?>> controllers) {
        this.mediator = checkNotNull(mediator);
        this.interactionHandler = checkNotNull(interactionHandler);
        this.controllers = ImmutableList.copyOf(controllers);

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
        // currently no-op, buttons are pretty easy
    }

    /**
     * Gets the currently in use {@link HelpPageEnabled} entity.
     * @return Optional with content if an entity is present.
     */
    public Optional<HelpPageEnabled> getCurrentHelpable() {
        return Optional.ofNullable(currentHelpable);
    }

    /**
     * Sets the {@link HelpPageEnabled} entity, so when
     * @param currentHelpable Non-{@code null} {@code HelpPageEnabled} entity that will be used to show a help content.
     */
    public void setCurrentHelpable(HelpPageEnabled currentHelpable) {
        this.currentHelpable = checkNotNull(currentHelpable);
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
     * open the help window
     */
    public void showHelpDialog() {
        getCurrentHelpable().ifPresent(helpable -> {
            String startString = helpable.getHelpPageID();
            if (mediator.getDesktopController().getHelpController() == null) {
                HelpController helpController = HelpController.openHelpDialog(
                        mediator.getDesktopController(), startString);
                mediator.getDesktopController().setHelpController(helpController);
            } else {
                HelpController hc = mediator.getDesktopController().getHelpController();
                hc.getStage().toFront();
                hc.selectTreeItem(startString);
            }
        });
    }

    /**
     * open a help window when clicking on the help button. Delegates to {@link #showHelpDialog()}.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML @SuppressWarnings("unused")
    private void onHelpButtonClick(ActionEvent e) {
        if (interactionHandler.onHelpButtonClick()) {
            showHelpDialog();
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
         * Called before running the content of {@link DialogButtonController#onCancelButtonClick(ActionEvent)}.
         *
         * @return {@code true} to continue.
         */
        boolean onCancelButtonClick();
    }
}
