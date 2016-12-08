package cpusim.gui.editmicroinstruction;

import com.google.common.collect.ImmutableMap;
import cpusim.Mediator;
import cpusim.gui.util.ControlButtonController;
import cpusim.gui.util.DialogButtonController;
import cpusim.model.util.MachineBound;
import cpusim.model.util.ReadOnlyMachineBound;
import cpusim.model.Machine;
import cpusim.model.microinstruction.Microinstruction;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

import static com.google.common.base.Preconditions.*;

/**
 * This class is the controller for the dialog box that is used for
 * editing microinstructions.
 *
 * @since 2013-06-05
 */
public class EditMicroinstructionsController extends BorderPane
        implements DialogButtonController.InteractionHandler, MachineBound {
    
    @FXML @SuppressWarnings("unused")
    private TabPane contentPane;
    
    @FXML @SuppressWarnings("unused")
    private Button okButton;
    
    @FXML @SuppressWarnings("unused")
    private Button cancelButton;
    
    @FXML @SuppressWarnings("unused")
    private Button helpButton;

    @FXML @SuppressWarnings("unused")
    private DialogButtonController dialogButtonController;

    private final ObjectProperty<Machine> machine;
    private final Mediator mediator;


    // List of all Contollers, it's long and nasty, but has to live somewhere. Try to keep in lexographic order
    // for maintainability sake.
    private final ArithmeticTableController arithmeticTableController;
    private final BranchTableController branchTableController;
    private final DecodeTableController decodeTableController;
    private final IncrementTableController incrementTableController;
    private final IOTableController ioTableController;
    private final LogicalTableController logicalTableController;
    private final MemoryAccessTableController memoryAccessTableController;
    private final SetCondBitTableController setCondBitTableController;
    private final SetTableController setTableController;
    private final ShiftTableController shiftTableController;
    private final TestTableController testTableController;
    private final TransferAtoRTableController transferAtoRTableController;
    private final TransferRtoATableController transferRtoATableController;
    private final TransferRtoRTableController transferRtoRTableController;

    private ImmutableMap<Class<? extends Microinstruction>, MicroinstructionTab<?>> classMicroinstructionTabMap;

    public EditMicroinstructionsController(Mediator mediator) {
        this.mediator = checkNotNull(mediator);

        this.machine = new SimpleObjectProperty<>(this, "machine", null);
        this.machine.bind(mediator.machineProperty());

        this.arithmeticTableController = new ArithmeticTableController(mediator);
        this.branchTableController = new BranchTableController(mediator);
        this.decodeTableController = new DecodeTableController(mediator);
        this.incrementTableController = new IncrementTableController(mediator);
        this.ioTableController = new IOTableController(mediator);
        this.logicalTableController = new LogicalTableController(mediator);
        this.memoryAccessTableController = new MemoryAccessTableController(mediator);
        this.setCondBitTableController = new SetCondBitTableController(mediator);
        this.setTableController = new SetTableController(mediator);
        this.shiftTableController = new ShiftTableController(mediator);
        this.testTableController = new TestTableController(mediator);
        this.transferAtoRTableController = new TransferAtoRTableController(mediator);
        this.transferRtoATableController = new TransferRtoATableController(mediator);
        this.transferRtoRTableController = new TransferRtoRTableController(mediator);


    }
    
    /**
     * initializes the dialog window after its root element has been processed.
     * contains a listener to the combo box, so that the content of the table will
     * change according to the selected type of microinstruction.
     */
    @FXML @SuppressWarnings("unused")
    private void initialize() {
        // Define an event filter for the ComboBox for Mouse_released events
        final EventHandler<Event> selectionHandler = event -> {
            // When we change tabs, make sure the current tab is valid. If it fails, we don't change tabs.
            final MicroinstructionTab<?> source = (MicroinstructionTab<?>)event.getSource();
            if (source != null && !source.isSelected()) {
                // This means the source is LEAVING
                source.getTableController().checkValidity();
            }

            final MicroinstructionTab<?> target = (MicroinstructionTab<?>) event.getTarget();
            if (target != null && target.isSelected()) {
                target.onTabSelected();
            }
        };


        ImmutableMap.Builder<Class<? extends Microinstruction>, MicroinstructionTab<?>> microinstructionTabMapBuilder =
                ImmutableMap.builder();

        ObservableList<Tab> tabs = contentPane.getTabs();
        for (int i = 0; i < tabs.size(); ++i) {
            Tab tab = tabs.get(i);

            MicroinstructionTab<?> newTab;

            // Nasty, nasty switch created by copying the fields and then applying a search/replace
            // with the following regex:
            // find: private final (\w+TableController)\s(\w+);
            // replace: case $1.FX_ID:\nnewTab = new MicroinstructionTab<>(tab.getText(), $2);\nbreak;

            switch (tab.getId()) {
                case ArithmeticTableController.FX_ID:
                    newTab = new MicroinstructionTab<>(tab.getText(), arithmeticTableController);
                    break;

                case BranchTableController.FX_ID:
                    newTab = new MicroinstructionTab<>(tab.getText(), branchTableController);
                    break;

                case DecodeTableController.FX_ID:
                    newTab = new MicroinstructionTab<>(tab.getText(), decodeTableController);
                    break;

                case IncrementTableController.FX_ID:
                    newTab = new MicroinstructionTab<>(tab.getText(), incrementTableController);
                    break;

                case IOTableController.FX_ID:
                    newTab = new MicroinstructionTab<>(tab.getText(), ioTableController);
                    break;

                case LogicalTableController.FX_ID:
                    newTab = new MicroinstructionTab<>(tab.getText(), logicalTableController);
                    break;

                case MemoryAccessTableController.FX_ID:
                    newTab = new MicroinstructionTab<>(tab.getText(), memoryAccessTableController);
                    break;

                case SetCondBitTableController.FX_ID:
                    newTab = new MicroinstructionTab<>(tab.getText(), setCondBitTableController);
                    break;

                case SetTableController.FX_ID:
                    newTab = new MicroinstructionTab<>(tab.getText(), setTableController);
                    break;

                case ShiftTableController.FX_ID:
                    newTab = new MicroinstructionTab<>(tab.getText(), shiftTableController);
                    break;

                case TestTableController.FX_ID:
                    newTab = new MicroinstructionTab<>(tab.getText(), testTableController);
                    break;

                case TransferAtoRTableController.FX_ID:
                    newTab = new MicroinstructionTab<>(tab.getText(), transferAtoRTableController);
                    break;

                case TransferRtoATableController.FX_ID:
                    newTab = new MicroinstructionTab<>(tab.getText(), transferRtoATableController);
                    break;

                case TransferRtoRTableController.FX_ID:
                    newTab = new MicroinstructionTab<>(tab.getText(), transferRtoRTableController);
                    break;

                default:
                    throw new IllegalStateException("Unknown tab found: " + tab.getId());
            }

            newTab.setOnSelectionChanged(selectionHandler);

            tabs.set(i, newTab);
            microinstructionTabMapBuilder.put(newTab.getTableController().getMicroClass(), newTab);
        }
        classMicroinstructionTabMap = microinstructionTabMapBuilder.build();

        // Select the first tab, seems like a good default.
        contentPane.getSelectionModel().select(0);

        dialogButtonController.setRequired(machine, this,
                arithmeticTableController,
                branchTableController,
                decodeTableController,
                incrementTableController,
                ioTableController,
                logicalTableController,
                memoryAccessTableController,
                setCondBitTableController,
                setTableController,
                shiftTableController,
                testTableController,
                transferAtoRTableController,
                transferRtoATableController,
                transferRtoRTableController);
    }

    /**
     * Given a {@link Microinstruction} instance, this method will cause the correct tab to be shown and the requested
     * {@link Microinstruction} will be selected in the table by default.
     * @param micro The instruction that is currently in focus.
     */
    public void showTabForMicroinstruction(final Microinstruction<?> micro) {
        MicroinstructionTab<?> tab = classMicroinstructionTabMap.get(micro.getClass());
        if (tab == null) {
            throw new IllegalArgumentException("Unknown micro passed: " + micro);
        }

        // Select the tab
        contentPane.getSelectionModel().select(tab);

        // Now, given the controller, find the item in the table and if it exists, show it.
        MicroinstructionTableController<? extends Microinstruction> ctrl = tab.getTableController();
        List<? extends Microinstruction> items = ctrl.getItems();
        final int idx = items.indexOf(micro);
        if (idx != -1) {
            ctrl.getSelectionModel().select(idx);
            ctrl.scrollTo(idx);
        }
    }
    
    @Override
    public ObjectProperty<Machine> machineProperty() {
        return machine;
    }
    
    /*
     * Delegates to DesktopController#showHelpDialog(String)
     */
    @Override
    public void displayHelpDialog(final String helpPageId) {
        mediator.getDesktopController().showHelpDialog(helpPageId);
    }
    
    @Override
    public boolean onOkButtonClick() {
        return true;
    }

    /**
     * Called whenever the dialog is exited via the 'ok' button
     * and the machine needs to be updated based on the changes
     * made while the dialog was open
     */
    @Override
    public void onMachineUpdated() {
        mediator.setMachineDirty(true);
    }

    @Override
    public boolean onHelpButtonClick() {
        return true;
    }

    @Override
    public boolean onCancelButtonClick() {
        return true;
    }

    /**
     * Class combining a {@link ControlButtonController} and a {@link MicroinstructionTableController}.
     * @param <T>
     */
    private class MicroinstructionTab<T extends Microinstruction<T>> extends Tab {

        private final ControlButtonController<T> buttonCtrlr;

        private final MicroinstructionTableController<T> tableCtrlr;

        private final VBox layout;

        MicroinstructionTab(final String text, final MicroinstructionTableController<T> tableCtrlr) {
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
        MicroinstructionTableController<T> getTableController() {
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
