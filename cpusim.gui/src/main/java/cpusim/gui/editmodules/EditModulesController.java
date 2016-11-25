/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Removed one try catch and allOkay variable and replaced it with a try catch for
 * a validation exception
 */
package cpusim.gui.editmodules;

import com.google.common.collect.ImmutableList;
import cpusim.Mediator;
import cpusim.gui.desktop.DesktopController;
import cpusim.gui.help.HelpController;
import cpusim.model.Machine;
import cpusim.model.Module;
import cpusim.model.module.RAM;
import cpusim.model.util.ValidationException;
import cpusim.util.Dialogs;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.*;

/**
 * This class is the controller for the dialog box that is used for
 * editing the properties of each register in a register array.
 */
public class EditModulesController {
    
    private final Mediator mediator;
    private final Machine machine;
    private final DesktopController desktop;

    @FXML @SuppressWarnings("unused")
    private TabPane modulePane;

    @FXML @SuppressWarnings("unused")
    private Button okButton;

    @FXML @SuppressWarnings("unused")
    private Button cancelButton;

    private final ConditionBitTableController conditionBitTableController;
    private final RAMsTableController ramsTableController;
    private final RegisterArrayTableController registerArrayTableController;
    private final RegistersTableController registersTableController;

    private final ImmutableList<ModuleTableController<?>> allTableControllers;

    public EditModulesController(Mediator mediator, DesktopController desktop) {
        this.mediator = mediator;
        this.machine = mediator.getMachine();
        this.desktop = desktop;

        registersTableController = new RegistersTableController(mediator);
        registerArrayTableController = new RegisterArrayTableController(mediator);
        conditionBitTableController = new ConditionBitTableController(
                mediator,
                registersTableController,
                registerArrayTableController);
        
        registersTableController.setConditionBitController(conditionBitTableController);
        registerArrayTableController.setConditionBitController(conditionBitTableController);
        
        ramsTableController = new RAMsTableController(mediator);

        allTableControllers = ImmutableList.of(conditionBitTableController, registersTableController,
                registerArrayTableController, ramsTableController);
    }

    @FXML
    public void initialize() {
        final EventHandler<Event> selectionHandler = event -> {
            // When we change tabs, make sure the current tab is valid. If it fails, we don't change tabs.
            final ModuleTab<?> source = (ModuleTab<?>)event.getSource();
            if (source != null && !source.isSelected()) {
                // This means the source is LEAVING
                source.getTableController().checkValidity();
            }

            final ModuleTab<?> target = (ModuleTab<?>) event.getTarget();
            if (target != null && target.isSelected()) {
                target.onTabSelected();
            }
        };

        // Replace all of the tabs with ModuleTabs because they have some added functionality.
        ObservableList<Tab> tabs = modulePane.getTabs();
        for (int i = 0; i < tabs.size(); ++i) {
            Tab tab = tabs.get(i);
            
            ModuleTab<?> newTab;
            
            switch (tab.getId()) {
                case RegisterArrayTableController.FX_ID:
                    newTab = new ModuleTab<>(tab.getText(), registerArrayTableController);
                    break;

                case RegistersTableController.FX_ID:
                    newTab = new ModuleTab<>(tab.getText(), registersTableController);
                    break;

                case ConditionBitTableController.FX_ID:
                    newTab = new ModuleTab<>(tab.getText(), conditionBitTableController);
                    break;

                case RAMsTableController.FX_ID:
                    newTab = new ModuleTab<>(tab.getText(), ramsTableController);
                    break;

                default:
                    throw new IllegalStateException("Found unknown ID: " + tab.getId());
            }

            newTab.setOnSelectionChanged(selectionHandler);
            
            tabs.set(i, newTab);
        }

        // Call the current content controller's #onTabSelected(). This is done so that it will always update itself
        // to show the updated information. This is then called in the selection handler later.
        modulePane.getSelectionModel().select(0);
    }

    

    

    /**
     * save the current changes and close the window when clicking on OK button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML @SuppressWarnings("unused")
    public void onOKButtonClick(ActionEvent e) {
        //get the current edited clones
        //ObservableList objList = activeTable.getItems();
        try {

            allTableControllers.forEach(ModuleTableController::checkValidity);

            //update the machine with the new values
            updateMachine();
            //get a handle to the stage.
            Stage stage = (Stage) okButton.getScene().getWindow();
            //close window.
            stage.close();

            mediator.addPropertyChangeListenerToAllModules(mediator.getBackupManager());
            desktop.getHighlightManager().updatePairsForNewRegistersAndRAMs();
        } catch (ValidationException ex) {
            Dialogs.createErrorDialog(modulePane.getScene().getWindow(),
                    "Modules Error",
                    ex.getMessage()).showAndWait();
        }

    }

    /**
     * close the window without saving the changes.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML @SuppressWarnings("unused")
    public void onCancelButtonClick(ActionEvent e) {
        //get a handle to the stage.
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        //close window.
        stage.close();
    }

    /**
     * Get the current controller inside the {@link #modulePane}.
     * @return the current {@link ModuleTab} showing
     */
    private Optional<ModuleTab<?>> getCurrentContent() {
        return Optional.of((ModuleTab<?>)modulePane.getSelectionModel().getSelectedItem());
    }

    /**
     * open a help window when clicking on the help button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML @SuppressWarnings("unused")
    public void onHelpButtonClick(ActionEvent e) {
        getCurrentContent().ifPresent(tab -> {
            String startString = tab.getTableController().getHelpPageID();
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
     * Called whenever the dialog is exited via the 'ok' button
     * and the machine needs to be updated based on the changes
     * made while the dialog was open
     */
    private void updateMachine() {
        machine.setRAMs(ramsTableController.getItems());
        machine.setRegisters(registersTableController.getItems());
        machine.setRegisterArrays(registerArrayTableController.getItems());
        machine.setConditionBits(conditionBitTableController.getItems());
        
        List<RAM> rams = machine.getModule(RAM.class);
        if (!rams.contains(machine.getCodeStore())) {
            //the code store was deleted so set a different
            //RAM to be the code store
            if (rams.size() != 0) {
                machine.setCodeStore(rams.get(0));
            }
            else {
                machine.setCodeStore(null);
            }
        }
        desktop.adjustTablesForNewModules();
        mediator.setMachineDirty(true);
        mediator.clearRAMs();
        mediator.clearRegisterArrays();
        mediator.clearRegisters();
    }
    
    /**
     * Class combining a {@link ControlButtonController} and a {@link ModuleTableController}.
     * @param <T>
     */
    private class ModuleTab<T extends Module<T>> extends Tab {
        
        private final ControlButtonController<T> buttonCtrlr;
    
        private final ModuleTableController<T> tableCtrlr;
        
        private final VBox layout;
    
        ModuleTab(final String text, final ModuleTableController<T> tableCtrlr) {
            super(text);
            this.tableCtrlr = checkNotNull(tableCtrlr);
            this.buttonCtrlr = tableCtrlr.createControlButtonController();
            
            layout = new VBox(12);
            layout.getChildren().addAll(tableCtrlr, buttonCtrlr);
            
            // when added to a TabPane, we set the internal TableView's preferred height to be the size of the
            // table - the buttonCtrlr preferred height.
            tabPaneProperty().addListener((observable, oldValue, newValue) -> {
                final DoubleBinding diffBind = newValue.prefHeightProperty().subtract(buttonCtrlr.prefHeightProperty());
                tableCtrlr.prefHeightProperty().bind(diffBind);
            });
            
            setContent(layout);
        }
    
        /**
         * Getter for {@link #buttonCtrlr}.
         * @return Current button controller
         */
        @SuppressWarnings("unused")
        ControlButtonController<T> getButtonController() {
            return buttonCtrlr;
        }
    
        /**
         * Getter for {@link #tableCtrlr}.
         * @return Current controller
         */
        ModuleTableController<T> getTableController() {
            return tableCtrlr;
        }
    
        /**
         * Called when selecting a new tab
         */
        void onTabSelected() {
            tableCtrlr.onTabSelected();
            buttonCtrlr.updateControlButtonStatus();
        }
    }
}
