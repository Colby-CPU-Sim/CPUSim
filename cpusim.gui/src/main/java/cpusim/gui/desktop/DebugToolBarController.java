/**
 * Author: Jinghui Yu
 * date:7/29/2013
 */

package cpusim.gui.desktop;

import cpusim.model.Machine;
import cpusim.model.MachineInstruction;
import cpusim.Mediator;
import cpusim.model.Microinstruction;
import cpusim.gui.util.FXMLLoaderFactory;
import cpusim.gui.util.StyledListCell;
import cpusim.util.BackupManager;
import cpusim.util.CPUSimConstants;
import cpusim.util.OutlineChangesManager;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * A tool bar used for debug mode.
 */
public class DebugToolBarController extends ToolBar implements Initializable,
        CPUSimConstants, ChangeListener<Machine.StateWrapper>
{
    @FXML
    Button goButton;
    @FXML
    Button stepByInstrButton;
    @FXML
    Button stepByMicroButton;
    @FXML
    Button backupMachineInstrButton;
    @FXML
    Button backupMicroInstrButton;
    @FXML
    Button startOverButton;
    @FXML
    Label currentInstrLabel;
    @FXML
    ListView<Microinstruction> currentMicrosList;

    private static final KeyCodeCombination GO_ACCELERATOR = new KeyCodeCombination
            (KeyCode.G, KeyCombination.ALT_DOWN, KeyCombination.SHORTCUT_DOWN);
    private static final KeyCodeCombination STEP_BY_INSTR_ACCELERATOR = new
            KeyCodeCombination(KeyCode.I, KeyCombination.ALT_DOWN, KeyCombination
            .SHORTCUT_DOWN);
    private static final KeyCodeCombination STEP_BY_MICRO_ACCELERATOR = new
            KeyCodeCombination(KeyCode.M, KeyCombination.ALT_DOWN, KeyCombination
            .SHORTCUT_DOWN);
    private static final KeyCodeCombination BACKUP_BY_INSTR_ACCELERATOR = new
            KeyCodeCombination(KeyCode.B, KeyCombination.ALT_DOWN, KeyCombination
            .SHORTCUT_DOWN);
    private static final KeyCodeCombination BACKUP_BY_MICRO_ACCELERATOR = new
            KeyCodeCombination(KeyCode.K, KeyCombination.ALT_DOWN, KeyCombination
            .SHORTCUT_DOWN);
    private static final KeyCodeCombination START_OVER_ACCELERATOR = new
            KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN, KeyCombination
            .SHORTCUT_DOWN);


    private Machine machine;
    private BackupManager backupManager;
    private OutlineChangesManager outlineChangesManager;
    private DesktopController desktop;

    /**
     * Constructor
     *
     * @param mediator mediator with the current machine
     * @param desktop  the desktop controller
     */
    public DebugToolBarController(Mediator mediator, DesktopController desktop) {
        this.machine = mediator.getMachine();
        this.backupManager = mediator.getBackupManager();
        this.outlineChangesManager = new OutlineChangesManager(backupManager, desktop);
        this.desktop = desktop;

        FXMLLoader fxmlLoader = FXMLLoaderFactory.fromRootController(this, "DebugToolBar.fxml");

        try {
            fxmlLoader.load();
        } catch (IOException ioe) {
            throw new IllegalStateException("Unable to load file: DebugToolBar.fxml", ioe);
        }
    }


    /**
     * initialize the tool bar
     *
     * @param url the location used to resolve relative paths for the root
     *            object, or null if the location is not known.
     * @param rb  the resources used to localize the root object, or null if the root
     *            object was not localized.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentMicrosList.setCellFactory(param -> new StyledListCell<Microinstruction>());
    }

    /**
     * enable or disable any buttons to continue execution.
     * While the current execute thread is executing, we don't want the user
     * to start another new thread executing before the first finishes, so we
     * disable the buttons until the thread finishes.
     *
     * @param disable true if we want to disable the buttons.
     */
    public void setDisableAllButtons(boolean disable) {
        // this method is called only in the changed method
        goButton.setDisable(disable);
        stepByInstrButton.setDisable(disable);
        stepByMicroButton.setDisable(disable);
        backupMachineInstrButton.setDisable(disable);
        backupMicroInstrButton.setDisable(disable);
        startOverButton.setDisable(disable);
    }

    /**
     * run the program that is assembled.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    public void onGoButtonClick(ActionEvent e) {
        machine.execute(Machine.RunModes.RUN_AND_FIRE_CYCLES);
    }

    /**
     * steps by one machine instruction
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    public void onStepByInstrClick(ActionEvent e) {
        machine.execute(Machine.RunModes.STEP_BY_INSTR);
    }

    /**
     * steps by one micro instruction
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    public void onStepByMicroClick(ActionEvent e) {
        machine.execute(Machine.RunModes.STEP_BY_MICRO);
    }

    /**
     * back up one machine instruction
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    public void onBackupMachineInstrClick(ActionEvent e) {
        // Can't back up the IO Channels--this is mentioned in user manual.
        backupManager.backupOneMachineInstruction();
        machine.getControlUnit().reset();
        machine.setState(Machine.State.EXECUTION_HALTED, false);
        updateDisplay();
        enableForwardButtons();
    }

    /**
     * back up one micro instruction
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    public void onBackupMicroInstrClick(ActionEvent e) {
        // Can't back up the IO Channels--this is mentioned in user manual.
        backupManager.backupOneMicroInstruction();
        machine.setState(Machine.State.EXECUTION_HALTED, false);
        updateDisplay();
        outlineChangesManager.updateOutlines();
        enableForwardButtons();
    }

    /**
     * start over the running program
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    public void onStartOverClick(ActionEvent e) {
        backupManager.backupAllTheWay();
        machine.getControlUnit().reset();
        machine.resetAllChannels();
        updateDisplay();
        enableForwardButtons();
    }

    /**
     * enable all debug buttons that go forward in the program
     */
    private void enableForwardButtons() {
        // this method is used only by the backup by instr, backup by micro and
        // start over buttons when they've been clicked
        this.goButton.setDisable(false);
        this.stepByInstrButton.setDisable(false);
        this.stepByMicroButton.setDisable(false);
    }

    /**
     * included for backward compatability
     */
    public void updateDisplay() {
        updateDisplay(false, false);
    }

    /**
     * This method exists if the machine needs to be updated before the display gets
     * updated
     *
     * @param newToolbar     true if opening a new toolbar
     * @param outlineChanges true of the outline has been changed
     * @param mediator       new mediator containing the new machine
     */
    public void updateDisplay(boolean newToolbar, boolean outlineChanges, Mediator
            mediator) {
        this.machine = mediator.getMachine();
        updateDisplay(newToolbar, outlineChanges);
    }

    /**
     * updates the current instruction and microinstruction labels
     * and enables or disables the appropriate buttons and tells the
     * desktop to highlight the appropriate RAM cells if at the start of
     * the fetch sequence.
     *
     * @param newToolbar     whether opening a new tool bar
     * @param outlineChanges true if the outlines of changed register and ram cells should be displayed
     */
    public void updateDisplay(boolean newToolbar, boolean outlineChanges) {
        MachineInstruction currentInstruction = machine.getControlUnit()
                .getCurrentInstruction();
        currentMicrosList.setItems(currentInstruction.getMicros());
        currentInstrLabel.setText(currentInstruction.getName() + ": ");


        int index = machine.getControlUnit().getMicroIndex();
        if (index == currentMicrosList.getItems().size() - 1) {
            currentMicrosList.scrollTo(index + 1);
        }
        else if (index == 0) {
            if (newToolbar == false) {
                currentMicrosList.scrollTo(-1);
            }
        }
        else {
            //ensure that index row is the middle one of the three visible rows
            //currentMicrosList.scrollTo(index + 2);
            currentMicrosList.scrollTo(index);
        }
        currentMicrosList.getSelectionModel().clearSelection();
        currentMicrosList.getSelectionModel().select(index);

        if (backupManager.canBackupOneMachineInstr()) {
            backupMicroInstrButton.setDisable(false);
            backupMachineInstrButton.setDisable(false);
            startOverButton.setDisable(false);
        }
        else {
            backupMicroInstrButton.setDisable(true);
            backupMachineInstrButton.setDisable(true);
            startOverButton.setDisable(true);
        }
        // Can this next if stmt be commented out because the start of cycle values are already saved
        // whenever the machine state is START_OF_MACHINE_CYCLE in HighlightManager.changed()?
        if (index == 0 && machine.getFetchSequence() == currentInstruction) {
            desktop.getHighlightManager().saveStartOfCycleValues();
        }
        desktop.getHighlightManager().highlightCellsAndText();
        if (outlineChanges) {
            outlineChangesManager.updateOutlines();
        }
        else {
            outlineChangesManager.clearAllOutlines();
        }

    }

    /**
     * clear all the outlines in both tables
     */
    public void clearAllOutlines() {
        outlineChangesManager.clearAllOutlines();
    }


    //added in by Ben Borchard on 2/25/14 to make sure that the execution is halted by
    // a halt bit even
    //when stepping by Micro (and therefore halting after every micro)

    /**
     * Receive notifications that a module
     * has modified a property. This method is called within the
     * run thread and so any GUI events it generates need to be called
     * using invokeLater
     *
     * @param machineState    the variable that is being listened
     * @param oldStateWrapper the value of the state before changed
     * @param newStateWrapper the new state object
     */
    @Override
    public void changed(ObservableValue<? extends Machine.StateWrapper> machineState,
                        Machine.StateWrapper oldStateWrapper, Machine.StateWrapper
                                    newStateWrapper) {

        //don't do any of these things unless we are in debug mode
        if (this.desktop.getInDebugMode()) {

            //System.out.println("new: " + newStateWrapper);

            if (newStateWrapper.getState() == Machine.State.START_OF_EXECUTE_THREAD) {
                //disable all buttons after at the start of execution
                Platform.runLater(() -> setDisableAllButtons(true));
            }
            else if (newStateWrapper.getState() == Machine.State.EXCEPTION_THROWN ||
                    newStateWrapper.getState() == Machine.State.EXECUTION_HALTED ||
                    newStateWrapper.getState() == Machine.State.BREAK ||
                    newStateWrapper.getState() == Machine.State.EXECUTION_ABORTED ||
                    newStateWrapper.getState() == Machine.State.HALTED_STEP_BY_MICRO) {
                //enable all buttons after execution finished
                Platform.runLater(() -> setDisableAllButtons(false));
            }
        }
    }

    /**
     * add keyboard accelerators to the buttons in the debug toolbar
     */
    public void addButtonAccelerators() {
        getScene().getAccelerators().put(GO_ACCELERATOR, () -> goButton.fire());
        getScene().getAccelerators().put(STEP_BY_INSTR_ACCELERATOR, () ->
                stepByInstrButton.fire());
        getScene().getAccelerators().put(STEP_BY_MICRO_ACCELERATOR, () ->
                stepByMicroButton.fire());
        getScene().getAccelerators().put(BACKUP_BY_INSTR_ACCELERATOR, () ->
                backupMachineInstrButton.fire());
        getScene().getAccelerators().put(BACKUP_BY_MICRO_ACCELERATOR, () ->
                backupMicroInstrButton.fire());
        getScene().getAccelerators().put(START_OVER_ACCELERATOR, () -> startOverButton
                .fire());
    }

    /**
     * add keyboard accelerators to the buttons in the debug toolbar
     */
    public void removeButtonAccelerators() {
        getScene().getAccelerators().remove(GO_ACCELERATOR);
        getScene().getAccelerators().remove(STEP_BY_INSTR_ACCELERATOR);
        getScene().getAccelerators().remove(STEP_BY_MICRO_ACCELERATOR);
        getScene().getAccelerators().remove(BACKUP_BY_INSTR_ACCELERATOR);
        getScene().getAccelerators().remove(BACKUP_BY_MICRO_ACCELERATOR);
        getScene().getAccelerators().remove(START_OVER_ACCELERATOR);
    }

}
