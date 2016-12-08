/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Removed one try catch and allOkay variable and replaced it with a try catch for a validation exception
 */
package cpusim.gui.editmodules;

import cpusim.gui.util.DialogButtonController;
import cpusim.gui.util.HelpPageEnabled;
import cpusim.gui.util.MachineModificationController;
import cpusim.model.Machine;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import cpusim.model.util.Validatable;
import cpusim.model.util.ValidationException;
import cpusim.util.Dialogs;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class EditArrayRegistersController
        implements MachineModificationController,
                    HelpPageEnabled,
                    DialogButtonController.InteractionHandler {

    @FXML
    private ComboBox<RegisterArray> arrayCombo;

    @FXML @SuppressWarnings("unused")
    private RegistersTableController registersTableController;

    private ObjectProperty<Machine> machine;

    private ObjectProperty<RegisterArray> currentArray;

    public EditArrayRegistersController() {
        this.machine = new SimpleObjectProperty<>(this, "machine", null);
        this.currentArray = new SimpleObjectProperty<>(this, "currentArray", null);

        this.machine.addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                updateMachine();
            }

            if (newValue != null) {
                this.arrayCombo.setItems(newValue.getModules(RegisterArray.class));
            }
        });

        this.currentArray.addListener((observable, oldValue, newValue) -> {
            if (oldValue != newValue) {
                arrayCombo.getSelectionModel().select(newValue);
            }
        });
    }

    @FXML @SuppressWarnings("unused")
    public void initialize() {

        this.currentArray.bind(arrayCombo.getSelectionModel().selectedItemProperty());

        arrayCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
            }

            if (oldValue != newValue && newValue != null) {
                registersTableController.setItems(newValue.getRegisters());
            }
        });

//        // listen for changes to the instruction combo box selection and update
//        // the displayed micro instruction table accordingly.
//        arrayCombo.getSelectionModel().selectedItemProperty().addListener(
//                (selected, oldType, newType) -> {
//                    activeTable.setClones(activeTable.getItems());
//                    activeTable = tableMap.getMap().get(newType);
//                    tableMap.getMap().get(oldType).getSelectionModel().clearSelection();
//                    tablePane.getChildren().clear();
//                    tablePane.getChildren().add(tableMap.getMap().get(newType));
//                    activeTable.setPrefWidth(tablePane.getWidth());
//                    activeTable.setPrefHeight(tablePane.getHeight());
//                    activeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
//
//                });
//
//        // Define an event filter for the ComboBox for Mouse_released events
//        EventHandler validityFilter = (EventHandler<InputEvent>) event -> {
//
//        };
//        arrayCombo.addEventFilter(MouseEvent.MOUSE_RELEASED, validityFilter);
    }

    public ObjectProperty<RegisterArray> currentArrayProperty() {
        return currentArray;
    }

    @Override
    public String getHelpPageID() {
        return "Register Arrays";
    }

    @Override
    public ObjectProperty<Machine> machineProperty() {
        return machine;
    }

    @Override
    public void checkValidity() {
        try {
            registersTableController.checkValidity();

            Validatable.all(arrayCombo.getItems());
        } catch (ValidationException ex) {
            Dialogs.createErrorDialog(arrayCombo.getScene().getWindow(),
                    "Registers Error", ex.getMessage()).showAndWait();
        }
    }

    @Override
    public boolean onOkButtonClick() {
        return false;
    }

    @Override
    public void onMachineUpdated() {

    }

    @Override
    public boolean onHelpButtonClick() {
        return false;
    }

    @Override
    public void displayHelpDialog(String helpPageId) {

    }

    @Override
    public boolean onCancelButtonClick() {
        return false;
    }

    /**
     * save the current changes and close the window when clicking on OK button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML @SuppressWarnings("unused")
    public void onOKButtonClick(ActionEvent e) {
        //get the current edited clones
//        ObservableList<Register> objList = activeTable.getItems();
//        try {
//            ObservableList<Register> list = FXCollections.observableArrayList();
//            list.addAll(registerController.getItems());
//            for (RegisterArrayTableView r : tableMap.getMap().values()) {
//                list.addAll(r.getItems());
//            }
//
//            Validatable.all(objList);
//            //update the machine with the new values
//            updateRegisters();
//            //get a handle to the stage.
//            Stage stage = (Stage) okButton.getScene().getWindow();
//            //close window.
//            stage.close();
//        } catch (Exception ex) {
//            Dialogs.createErrorDialog(tablePane.getScene().getWindow(),
//                    "Registers Error", ex.getMessage()).showAndWait();
//        }
    }

    /**
     * Called whenever the dialog is exited via the 'ok' button
     * and the machine needs to be updated based on the changes
     * made while the dialog was open (JRL)
     */
    @Override
    public void updateMachine() {
        // and the machine needs to be updated based on the changes made.
//        getCurrentController().setClones(activeTable.getItems());
//        for (RegisterArrayTableView t : tableMap.getMap().values()) {
//            for (RegisterArray ra : registerArrays) {
//                if (ra.getName().equals(t.getArrayName())) {
//                    ra.registers().setAll(t.createNewModulesList());
//                }
//            }
//        }

    }

}
