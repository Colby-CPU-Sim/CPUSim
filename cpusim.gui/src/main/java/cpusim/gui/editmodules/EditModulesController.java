/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Removed one try catch and allOkay variable and replaced it with a try catch for
 * a validation exception
 */
package cpusim.gui.editmodules;

import cpusim.Mediator;
import cpusim.gui.desktop.DesktopController;
import cpusim.gui.util.ControlButtonController;
import cpusim.gui.util.DialogButtonController;
import cpusim.model.util.MachineBound;
import cpusim.model.Machine;
import cpusim.model.module.Module;
import cpusim.model.module.RAM;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

import static com.google.common.base.Preconditions.*;

/**
 * This class is the controller for the dialog box that is used for
 * editing the properties of each register in a register array.
 */
public final class EditModulesController extends BorderPane
        implements DialogButtonController.InteractionHandler, MachineBound {
    
    private final Mediator mediator;
    private final ObjectProperty<Machine> machine;
    private final DesktopController desktop;

    @FXML @SuppressWarnings("unused")
    private TabPane modulePane;

    private final ConditionBitTableController conditionBitTableController;
    private final RAMsTableController ramsTableController;
    private final RegisterArrayTableController registerArrayTableController;
    private final RegistersTableController registersTableController;

    @FXML @SuppressWarnings("unused")
    private DialogButtonController dialogButtonController;

    public EditModulesController(Mediator mediator, DesktopController desktop) {
        this.mediator = mediator;
        this.machine = new SimpleObjectProperty<>(this, "machine", null);
        this.machine.bind(mediator.machineProperty());
        
        this.desktop = desktop;

        registersTableController = new RegistersTableController();
        registerArrayTableController = new RegisterArrayTableController();
        conditionBitTableController = new ConditionBitTableController(
                registersTableController,
                registerArrayTableController);
        
        registersTableController.setConditionBitController(conditionBitTableController);
        registerArrayTableController.setConditionBitController(conditionBitTableController);
        
        ramsTableController = new RAMsTableController();

        registersTableController.machineProperty().bindBidirectional(machineProperty());
        registerArrayTableController.machineProperty().bindBidirectional(machineProperty());
        conditionBitTableController.machineProperty().bindBidirectional(machineProperty());
        ramsTableController.machineProperty().bindBidirectional(machineProperty());
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

        // Set the bottom to hold the dialog buttons
        dialogButtonController.setRequired(machine, this,
                conditionBitTableController,
                registersTableController,
                registerArrayTableController,
                ramsTableController);
    }

    @Override
    public void onMachineUpdated() {
        mediator.addPropertyChangeListenerToAllModules(mediator.getBackupManager());
        desktop.getHighlightManager().updatePairsForNewRegistersAndRAMs();
        
        final Machine machine = this.machine.get();

        machine.getCodeStore().ifPresent(codeStore -> {
            List<RAM> rams = machine.getModules(RAM.class);
            if (!rams.contains(codeStore)) {
                //the code store was deleted so set a different
                //RAM to be the code store
                if (rams.size() != 0) {
                    machine.setCodeStore(rams.get(0));
                }
                else {
                    machine.setCodeStore(null);
                }
            }
        });

        desktop.adjustTablesForNewModules();
        mediator.setMachineDirty(true);
        mediator.clearRAMs();
        mediator.clearRegisterArrays();
        mediator.clearRegisters();
    }

    @Override
    public boolean onOkButtonClick() {
        return true;
    }

    @Override
    public boolean onHelpButtonClick() {
        return true;
    }

    @Override
    public boolean onCancelButtonClick() {
        return true;
    }
    
    @Override
    public void displayHelpDialog(final String helpPageId) {
        desktop.showHelpDialog(helpPageId);
    }
    
    @Override
    public ObjectProperty<Machine> machineProperty() {
        return machine;
    }

    /**
     * Class combining a {@link ModuleControlButtonController} and a {@link ModuleTableController}.
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

            VBox.setVgrow(tableCtrlr, Priority.ALWAYS);
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

            // Tell the DialogButtonController that we have a helpable now.
            dialogButtonController.setCurrentHelpable(tableCtrlr);
        }
    }
}
