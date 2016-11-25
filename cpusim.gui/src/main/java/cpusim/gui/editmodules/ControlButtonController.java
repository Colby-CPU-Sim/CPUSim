package cpusim.gui.editmodules;

import com.google.common.base.Joiner;
import cpusim.gui.util.FXMLLoaderFactory;
import cpusim.model.Module;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.util.NamedObject;
import cpusim.util.Dialogs;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SelectionModel;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

class ControlButtonController<T extends Module<T>> extends HBox {
    
    public interface ButtonStatusCheck {
        
        /**
         * Denotes if the "New" button is enabled
         * @return
         */
        boolean isNewButtonEnabled();
    
        /**
         * Denotes if the delete button is enabled.
         * @return
         */
        boolean isDelButtonEnabled();
    
        /**
         * Denotes if the delete button is enabled.
         * @return
         */
        boolean isDupButtonEnabled();
    
        /**
         * Denotes if the properties button is enabled.
         * @return
         */
        boolean isPropButtonEnabled();
    }
    
    private static final String FXML_PATH = "TableControls.fxml";
    
    private final ModuleTableController<T> moduleController;
    
    private final ButtonStatusCheck buttonStatus;
    
    @FXML @SuppressWarnings("unused")
    private Button newButton;
    @FXML @SuppressWarnings("unused")
    private Button deleteButton;
    @FXML @SuppressWarnings("unused")
    private Button duplicateButton;
    
    final boolean hasExtendedProperties;
    
    @FXML @SuppressWarnings("unused")
    private Button propertiesButton;
    
    private final ButtonChangeListener buttonListener;
    
    protected ControlButtonController(ModuleTableController<T> moduleController, ButtonStatusCheck buttonStatus, final boolean hasExtendedProperties) {
        this.moduleController = moduleController;
        this.hasExtendedProperties = hasExtendedProperties;
        this.buttonStatus = buttonStatus;
        
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
        moduleController.getSelectionModel().selectedItemProperty().addListener(buttonListener);
        
        if (!hasExtendedProperties) {
            getChildren().remove(propertiesButton);
        }
    }
    
    void updateControlButtonStatus() {
        newButton.setDisable(!buttonStatus.isNewButtonEnabled());
        deleteButton.setDisable(!buttonStatus.isDelButtonEnabled());
        duplicateButton.setDisable(!buttonStatus.isDupButtonEnabled());
        propertiesButton.setDisable(!buttonStatus.isPropButtonEnabled());
    }
    
    /**
     * creates a new instruction when clicking on New button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    @SuppressWarnings("unused")
    void onNewButtonClick(ActionEvent e) {
        //add a new item at the end of the list.
        String uniqueName = NamedObject.createUniqueName(moduleController.getItems(), '?');
        final T newValue = moduleController.getPrototype();
        newValue.setName(uniqueName);
        
        moduleController.getItems().add(newValue);
        moduleController.getSelectionModel().select(newValue);
    
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
    protected boolean checkDelete(T toDelete) {
        
        boolean shouldDelete = true;
        //now test to see if it is used by any micros and if so,
        //warn the user that those micros will be deleted too.
        
        Map<Microinstruction, ObservableList<Microinstruction>> microsThatUseIt =
                moduleController.getMachine().getMicrosThatUse(toDelete);
        if (microsThatUseIt.size() > 0) {
            StringBuilder message = new StringBuilder(toDelete.toString());
            message.append(" is used by the following microinstructions: \n  ");
        
            Joiner.on(", ").appendTo(message, microsThatUseIt.keySet());
        
            message.append(".\n  If you delete it, all these microinstructions will also be deleted.  ");
            message.append("Really delete it?");
            Optional<ButtonType> result = Dialogs.createConfirmationDialog(moduleController.getScene().getWindow(),
                    "Confirm Deletion", message.toString()).showAndWait();
            shouldDelete = !(result.get() == ButtonType.CANCEL ||
                    result.get() == ButtonType.NO ||
                    result.get() == ButtonType.CLOSE);
        }
        
        return shouldDelete;
    }
    
    /**
     * deletes an existing instruction when clicking on Delete button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML @SuppressWarnings("unused")
    void onDeleteButtonClick(ActionEvent e) {
    
        SelectionModel<T> selectionModel = moduleController.getSelectionModel();
        final T selectedValue = selectionModel.getSelectedItem();
        
        if (!checkDelete(selectedValue)) {
            return;
        }
        
        moduleController.getItems().remove(selectionModel.getSelectedIndex());
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
    @FXML
    @SuppressWarnings("unused")
    void onDuplicateButtonClick(ActionEvent e) {
        //add a new item at the end of the list.
        final T newObject = moduleController.getSelectionModel().getSelectedItem().cloneOf();
        newObject.setName(newObject.getName() + "_copy");
        moduleController.getItems().add(0, newObject);
        
        moduleController.scrollTo(0);
        moduleController.getSelectionModel().select(0);
    
        updateControlButtonStatus();
    }
    
    /**
     * edits the selected register array
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    @SuppressWarnings("unused")
    void onPropertiesButtonClick(ActionEvent e) {
        if (!hasExtendedProperties) {
            throw new IllegalStateException("Called onPropertiesButtonClick on something with no extended properties");
        } else {
            throw new Error("The method, onPropertiesButtonClick(ActionEvent) must be implemented if advanced " +
                    "properties are present.");
        }
        // FIXME Put into EditArrayRegisters subclass
//        EditArrayRegistersController controller;
//        if (activeTable.getSelectionModel().getSelectedIndex() == -1) {
//            controller = new EditArrayRegistersController(mediator,
//                    (RegistersTableController) tableMap.get(Register.class),
//                    (RegisterArrayTableController) tableMap.get(RegisterArray.class));
//        }
//        else {
//            controller = new EditArrayRegistersController(mediator,
//                    (RegistersTableController) tableMap.get(Register.class),
//                    (RegisterArrayTableController) tableMap.get(RegisterArray.class),
//                    (activeTable.getItems().get(
//                            activeTable.getSelectionModel().getSelectedIndex()
//                    )).getName()
//            );
//        }
//
//        //controller
//        FXMLLoader fxmlLoader = new FXMLLoader(mediator.getClass().getResource(
//                "gui/editmodules/arrayregisters/EditRegisters.fxml"));
//        fxmlLoader.setController(controller);
//
//        final Stage dialogStage = new Stage();
//        Pane dialogRoot = null;
//        try {
//            dialogRoot = fxmlLoader.load();
//        } catch (IOException ex) {
//            // should never happen
//            assert false : "Unable to load file: EditRegisters.fxml";
//        }
//        Scene dialogScene = new Scene(dialogRoot);
//        dialogStage.setScene(dialogScene);
//        dialogStage.initOwner(propertiesButton.getScene().getWindow());
//        dialogStage.initModality(Modality.WINDOW_MODAL);
//        dialogStage.setTitle("Edit Register Arrays");
//
//        dialogScene.addEventFilter(
//                KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
//                    @Override
//                    public void handle(KeyEvent event) {
//                        if (event.getCode().equals(KeyCode.ESCAPE)) {
//                            if (dialogStage.isFocused()) {
//                                dialogStage.close();
//                            }
//                        }
//                    }
//                });
//        dialogStage.show();
//        updateControlButtonStatus();
    
    }
    
    /**
     * a listener listening for changes to the table selection and
     * update the status of buttons.
     */
    private class ButtonChangeListener implements ChangeListener<T> {
        
        @Override
        public void changed(ObservableValue<? extends T> selected, T oldModule, T newModule) {
            if (newModule == null) {
                deleteButton.setDisable(true);
                duplicateButton.setDisable(true);
            } else {
                deleteButton.setDisable(false);
                duplicateButton.setDisable(false);
            }
        }
    }
}