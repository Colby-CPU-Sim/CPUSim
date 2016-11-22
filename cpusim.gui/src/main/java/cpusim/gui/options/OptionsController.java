/**
 * Controller for the Options Dialog.
 *
 * @author Stephen Morse
 */

/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 *
 * 1.) changed saveLoadingTab and saveHighlightingOptions methods so that they work
 * with the Validate class to validate their data before saving said data
 * 2.) removed one try catch and allOkay variable and replaced it with a try catch for a validation exception
 *
 *  on 11/6/13
 * 1.) fixed the problem in the savePunctChars() method where it was returning true instead
 * of false and vice-versa
 * 2.) fixed the problem with saving the punctuation characters.  It was adding all the punctChar
 * values on the right side to those on the left side, so it had all the two copies of half the
 * punctChars, which was causing problems because there were two comments, labels, and pseudos, when
 * there were really only one.  We fixed it so it doesn't do that anymore.
 *
 *  on 12/2
 * 1.) fixed a bug where there was a classcastexception when the filechooser dialog was closed
 */

package cpusim.gui.options;

import cpusim.Mediator;
import cpusim.gui.help.HelpController;
import cpusim.gui.util.EditingStrCell;
import cpusim.model.Machine;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.assembler.PunctChar;
import cpusim.model.iochannel.BufferedChannel;
import cpusim.model.iochannel.FileChannel;
import cpusim.model.iochannel.IOChannel;
import cpusim.model.microinstruction.IO;
import cpusim.model.module.RAM;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterRAMPair;
import cpusim.model.util.Convert;
import cpusim.model.util.Validate;
import cpusim.model.util.ValidationException;
import cpusim.util.Dialogs;
import cpusim.util.GUIChannels;
import cpusim.util.ValidateControllers;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class OptionsController implements Initializable {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private Button helpButton;
    @FXML
    private Button OKButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button newButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button duplicateButton;
    @FXML
    private Button applyButton;

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab IOOptionsTab;
    @FXML
    private Tab highlightingTab;
    @FXML
    private Tab loadingTab;
    @FXML
    private Tab punctuationTab;
    @FXML
    private Tab indexingTab;
    @FXML
    private Tab breakPointsTab;

    @FXML
    private TableView<IOOptionsData> IOOptionsTable;
    @FXML
    private TableColumn<IOOptionsData, IO> nameColumn;
    @FXML
    private TableColumn<IOOptionsData, IOChannel> connectionColumn;
    private ObservableList<IOChannel> allChannels;

    @FXML
    private TableView<RegisterRAMPair> highlightingTable;
    @FXML
    private TableColumn<RegisterRAMPair, Register> registerColumn;
    @FXML
    private TableColumn<RegisterRAMPair, RAM> RAMColumn;
    @FXML
    private TableColumn<RegisterRAMPair, Boolean> dynamicColumn;

    @FXML
    private ChoiceBox<RAM> codeStore;
    @FXML
    private TextField startingAddress;

    @FXML
    private ChoiceBox<String> indexChoice;

    @FXML
    private ChoiceBox<Register> programCounterChoice;

    @FXML
    private TableView<PunctChar> leftPunctuationTable;
    @FXML
    private TableColumn<PunctChar, String> leftASCIIColumn;
    @FXML
    private TableColumn<PunctChar, PunctChar.Use> leftTypeColumn;
    @FXML
    private TableView<PunctChar> rightPunctuationTable;
    @FXML
    private TableColumn<PunctChar, String> rightASCIIColumn;
    @FXML
    private TableColumn<PunctChar, PunctChar.Use> rightTypeColumn;

    @FXML
    private ComboBox<ComboBoxChannel> changeAllCombo;


    private RegisterRAMPair highlightingSelectedSet;
    private IOOptionsData IOOptionsSelectedSet;
    private Mediator mediator;
    private ObservableList<Register> registers;
    private ObservableList<RAM> RAMs;
    private boolean changeIndexingDirection;  // true if changing to the opposite indexing direction

    /**
     * Constructor with mediator passed
     * from the desktop controller.
     *
     * @param med The mediator.
     */
    public OptionsController(Mediator med) {
        mediator = med;
        registers = mediator.getMachine().getAllRegisters();
        RAMs = mediator.getMachine().getAllRAMs();
    }

    /**
     * Initializes the options window.
     *
     * @param arg0 Standard URL
     * @param arg1 Standard ResourceBundle
     */
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        initializeIOOptionsTab();
        initializeHighlightingTab();
        initializeLoadingTab();
        initializePunctuationTab();
        initializeIndexingTab();
        initializeBreakPointsTab();
    }

    /////////////// Buttons ///////////////

    /**
     * Called whenever the Help ("?") button
     * is clicked.
     */
    public void onHelpButtonClicked() {
        String startString = "Options Dialog";
        String appendString = "";
        if (IOOptionsTab.isSelected())
            appendString = "#IOConnections";
        else if (highlightingTab.isSelected())
            appendString = "#Highlighting";
        else if (loadingTab.isSelected())
            appendString = "#Loading";
        else if (punctuationTab.isSelected())
            appendString = "#Punctuation";

        if (mediator.getDesktopController().getHelpController() == null) {
            HelpController helpController = HelpController.openHelpDialog(
                    mediator.getDesktopController(), startString, appendString);
            mediator.getDesktopController().setHelpController(helpController);
            return;
        } else {
            HelpController hc = mediator.getDesktopController().getHelpController();
            hc.getStage().toFront();
            hc.selectTreeItem(startString, appendString);
        }
    }

    /**
     * Called whenever the Apply button
     * is clicked.
     */
    public void onApplyButtonClicked() {
        if (highlightingTab.isSelected()) {
            saveHighlightingTab();
        } else if (IOOptionsTab.isSelected()) {
            saveIOOptionsTab();
        } else if (loadingTab.isSelected()) {
            saveLoadingTab();
        } else if (punctuationTab.isSelected()) {
            savePunctuationTab();
        } else if (breakPointsTab.isSelected()) {
            saveBreakPointsTab();
        } else if (indexingTab.isSelected()) {
            saveIndexingTab();
        }
    }

    /**
     * Called whenever the OK button
     * is clicked.
     */
    public void onOKButtonClicked() {
        // Save individual tabs

        boolean canClose = true;
        canClose &= saveIndexingTab();
        canClose &= saveHighlightingTab();
        canClose &= saveIOOptionsTab();
        canClose &= saveBreakPointsTab();
        canClose &= saveLoadingTab();
        canClose &= savePunctuationTab();

        if (canClose) {
            // Machine changed & close window.
            mediator.setMachineDirty(true);
            Stage stage = (Stage) OKButton.getScene().getWindow();
            //close window.
            stage.close();
        }

    }

    /**
     * Called whenever the Close button
     * is clicked.
     */
    public void onCloseButtonClicked() {
         ((Stage) (helpButton.getScene().getWindow())).close();
    }

    /**
     * Called whenever the New button is clicked.
     * Button is only within the highlighting tab.
     */
    public void onNewButtonClicked() {
        // Add newSet
        ObservableList<RegisterRAMPair> data = highlightingTable.getItems();
        RegisterRAMPair newSet = new RegisterRAMPair(
                registers.get(0), RAMs.get(0), false);
        data.add(0, newSet);

        // Select first and scroll to top
        highlightingTable.getSelectionModel().clearSelection();
        highlightingTable.getSelectionModel().selectFirst();
        highlightingTable.scrollTo(0);

        updateHighlightingClickables();
    }

    /**
     * Called whenever the Delete button is clicked.
     * Button is only within the highlighting tab.
     */
    public void onDeleteButtonClicked() {
        ObservableList<RegisterRAMPair> data = highlightingTable.getItems();
        int index = data.indexOf(highlightingSelectedSet);
        if (index >= 0) {
            data.remove(index);
            highlightingTable.setItems(data);
        }

        // Select Correctly 
        int indexToSelect = index - 1 < 0 ? index : index - 1;
        if (highlightingTable.getItems().size() > 0) {
            highlightingTable.getSelectionModel().clearSelection();
            highlightingTable.getSelectionModel().select(indexToSelect);
        }

        updateHighlightingClickables();
    }

    /**
     * Called whenever the New button is clicked.
     * Button is only within the highlighting tab.
     */
    public void onDuplicateButtonClicked() {
        ObservableList<RegisterRAMPair> data = highlightingTable.getItems();
        int index = data.indexOf(highlightingSelectedSet);
        if (index >= 0) {
            // Make newSet and add to table
            RegisterRAMPair newSet = highlightingSelectedSet.clone();
            data.add(0, newSet);

            // Select first and scroll to top
            highlightingTable.getSelectionModel().clearSelection();
            highlightingTable.getSelectionModel().selectFirst();
            highlightingTable.scrollTo(0);
        }
        updateHighlightingClickables();
    }

    /////////////// Saving Tabs ///////////////

    private boolean saveHighlightingTab() {
        if (!highlightingTab.isDisabled()) {
            ObservableList<RegisterRAMPair> data = highlightingTable.getItems();
            ValidateControllers.allRegisterRAMPairAreUnique(data);
            mediator.setRegisterRamPairs(data);
        }
        return true;
    }

    private boolean saveIOOptionsTab() {
        if (!IOOptionsTab.isDisabled()) {
            ObservableList<IO> ios = mediator.getMachine().getMicros(IO.class);
            ObservableList<IOOptionsData> data = IOOptionsTable.getItems();
            for (IOOptionsData d : data) {
                IO micro = (IO) ios.get(ios.indexOf(d.getIo()));
                micro.setConnection(d.getChannel());
            }
            mediator.getMachine().setMicros(IO.class, ios);
        }
        return true;
    }

    private boolean saveLoadingTab() {
        if (!loadingTab.isDisabled()) {
            // save the ram used for storing instructions
            mediator.getMachine().setCodeStore(codeStore.getValue());

            // Save starting address
            int ramLength = codeStore.getValue().getLength();
            String startString = startingAddress.getText();

            try {
                Validate.startingAddressIsValid(startString, ramLength);
                mediator.getMachine().setStartingAddressForLoading(
                        (int) Convert.fromAnyBaseStringToLong(startString));
                return true;
            } catch (ValidationException ex) {
                if (tabPane.getSelectionModel().getSelectedItem() != loadingTab) {
                    tabPane.getSelectionModel().select(loadingTab);
                }
                Dialogs.createErrorDialog(OKButton.getScene().getWindow(),
                        "Starting Address Error", ex.getMessage()).showAndWait();
                return false;
            }
        }
        return true;
    }

    private boolean savePunctuationTab() {
        if (!punctuationTab.isDisabled()) {
            ObservableList<PunctChar> punctChars = FXCollections.observableArrayList();
            punctChars.addAll(leftPunctuationTable.getItems());
            punctChars.addAll(rightPunctuationTable.getItems());
            try {
                Validate.punctChars(punctChars);
                mediator.getMachine().setPunctChars(new ArrayList<>(punctChars));
                return true;
            } catch (ValidationException ex) {
                if (tabPane.getSelectionModel().getSelectedItem() != punctuationTab) {
                    tabPane.getSelectionModel().select(punctuationTab);
                }
                Dialogs.createErrorDialog(OKButton.getScene().getWindow(),
                        "Punctuation Character Error", ex.getMessage()).showAndWait();
                return false;
            }
        }
        return true;
    }

    private boolean saveIndexingTab() {
        if (!indexingTab.isDisabled()) {
            if (changeIndexingDirection) {
                Optional<ButtonType> result =
                    Dialogs.createConfirmationDialog(OKButton.getScene().getWindow(),
                        "Confirm Index Change",
                        "Changing the indexing direction may change the behavior of"
                            + " some of your microinstructions.  Would you like to"
                            + " automatically change these microinstructions"
                            + " so that you machine will have the same behavior it"
                            + " had before you changed the indexing direction?").showAndWait();
                if (result.get() == ButtonType.OK) {
                    mediator.getMachine().changeStartBits();
                } else if (result.get() == ButtonType.CANCEL) {
                    return false;
                }
                //else do nothing
            }


            mediator.getMachine().setIndexFromRight(indexChoice.getValue().equals("right"));

        }
        return true;
    }

    private boolean saveBreakPointsTab() {
        if (!breakPointsTab.isDisabled()) {
             mediator.getMachine().setProgramCounter(programCounterChoice.getValue());
        }
        return true;
    }

    /////////////// Button Disable/Enablers ///////////////

    /**
     * Used to control the disabling/enabling of
     * the buttons within the highlighting tab.
     * Calling this method should update
     * the New, Delete, and Duplicate buttons according to the state of the
     * window.
     */
    private void updateHighlightingClickables() {
        if (highlightingTable.getItems().isEmpty()) {
            deleteButton.setDisable(true);
            duplicateButton.setDisable(true);
        } else {
            if (highlightingSelectedSet == null) {
                deleteButton.setDisable(true);
                duplicateButton.setDisable(true);
            } else {
                if (highlightingTable.getItems().indexOf(highlightingSelectedSet) >= 0) {
                    deleteButton.setDisable(false);
                    duplicateButton.setDisable(false);
                } else {
                    deleteButton.setDisable(true);
                    duplicateButton.setDisable(true);
                }
            }
        }
    }

    /**
     * Used to control the disabling/enabling of
     * the three buttons along the bottom of the frame.
     * Calling this method should update the Apply, Cancel,
     * and OK buttons according to the state of the window.
     */
    private void updateGlobalClickables() {
        // update apply
    }

    /////////////// Update Tabs ///////////////

    /**
     * This method is called each time the tab
     * changes.
     */
    public void onSelectionChanged() {
        if (IOOptionsTab.isSelected()) {
            updateGlobalClickables();
        } else if (highlightingTab.isSelected()) {
            updateHighlightingClickables();
            updateGlobalClickables();
        } else if (loadingTab.isSelected()) {
            updateGlobalClickables();
        } else if (punctuationTab.isSelected()) {
            updateGlobalClickables();
        }

    }

    ///////////// Setting Up Tables //////////////

    /**
     * Initializes the Highlighting tab.
     */
    private void initializeHighlightingTab() {

        // Disable tab if there is nothing to show.
        if (registers.size() < 1 || RAMs.size() < 1) {
            highlightingTab.setDisable(true);
            return;
        }

        // Making column widths adjust properly
        highlightingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // updates selectedSet, disables/enables buttons
        highlightingTable.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<RegisterRAMPair>() {
                    @Override
                    public void changed(ObservableValue<? extends RegisterRAMPair> selected,
                                        RegisterRAMPair oldSet, RegisterRAMPair newSet) {
                        highlightingSelectedSet = newSet;
                        updateHighlightingClickables();
                    }
                });

        // Accounts for width changes.
        highlightingTable.widthProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                        Double newWidth = (Double) newValue;
                        Double sum = registerColumn.getWidth()
                                + RAMColumn.getWidth()
                                + dynamicColumn.getWidth();
                        Double perc = sum / oldValue.doubleValue() * .94;

                        registerColumn.setPrefWidth(newWidth * perc *
                                registerColumn.getWidth() / sum);
                        RAMColumn.setPrefWidth(newWidth * perc *
                                RAMColumn.getWidth() / sum);
                        dynamicColumn.setPrefWidth(newWidth * perc *
                                dynamicColumn.getWidth() / sum);
                    }
                });

        // Callbacks
        Callback<TableColumn<RegisterRAMPair, Register>, TableCell<RegisterRAMPair, Register>>
                cellComboRegisterFactory =
                new Callback<TableColumn<RegisterRAMPair, Register>, TableCell<RegisterRAMPair, Register>>() {
                    @Override
                    public TableCell<RegisterRAMPair, Register> call(
                            TableColumn<RegisterRAMPair, Register> setStringTableColumn) {
                        return new ComboBoxTableCell<>(registers);
                    }
                };

        Callback<TableColumn<RegisterRAMPair, RAM>, TableCell<RegisterRAMPair, RAM>>
                cellComboRAMFactory = setStringTableColumn -> new ComboBoxTableCell<>(RAMs);

        Callback<TableColumn<RegisterRAMPair, Boolean>, TableCell<RegisterRAMPair, Boolean>>
                cellCheckBoxFactory = setStringTableColumn -> {
                    CheckBoxTableCell<RegisterRAMPair, Boolean> cbtc =
                            new CheckBoxTableCell<>();
                    cbtc.setAlignment(Pos.CENTER);
                    return cbtc;
                };

        // SetCellValueFactories
        registerColumn.setCellValueFactory(new PropertyValueFactory<>("register"));
        RAMColumn.setCellValueFactory(new PropertyValueFactory<>("ram"));
        dynamicColumn.setCellValueFactory(new PropertyValueFactory<>("dynamic"));

        // Register Factories and setOnEditCommits
        registerColumn.setCellFactory(cellComboRegisterFactory);
        registerColumn.setOnEditCommit(text -> text.getRowValue().setRegister(
                text.getNewValue()));

        RAMColumn.setCellFactory(cellComboRAMFactory);
        RAMColumn.setOnEditCommit(text -> text.getRowValue().setRam(
                text.getNewValue()));

        dynamicColumn.setCellFactory(cellCheckBoxFactory);
        dynamicColumn.setOnEditCommit(text -> text.getRowValue().setDynamic(
                text.getNewValue()));

        // Load in Rows
        ObservableList<RegisterRAMPair> data = highlightingTable.getItems();
        ObservableList<RegisterRAMPair> regRamPairs = mediator.getRegisterRAMPairs();
        for (RegisterRAMPair rrp : regRamPairs) {
            data.add(rrp.clone());
        }
        highlightingTable.setItems(data);
    }

    /**
     * Initializes the Loading tab.
     */
    private void initializeLoadingTab() {
        if (RAMs.size() < 1) {
            loadingTab.setDisable(true);
        } else {
            codeStore.setItems(RAMs);
            codeStore.setValue(mediator.getMachine().getCodeStore());
            startingAddress.textProperty().addListener((observable, oldValue, newValue) ->
                setStartingAddressTooltip());
            startingAddress.setText(String.valueOf(
                    mediator.getMachine().getStartingAddressForLoading()));
        }
    }

    private void setStartingAddressTooltip() {
        try{
            long startAddress = Convert.fromAnyBaseStringToLong(startingAddress.getText());
            startingAddress.setTooltip(new Tooltip("Binary: " +
                    Long.toBinaryString(startAddress) +
                    System.getProperty("line.separator") + "Decimal: " + startAddress +
                    System.getProperty("line.separator") + "Hex: " +
                    Long.toHexString(startAddress)));
        }
        catch(NumberFormatException ex) {
            startingAddress.setTooltip(new Tooltip("Illegal value"));
        }
    }

    /**
     * Initializes the Punctuation tab.
     */
    private void initializePunctuationTab() {

        // Making column widths adjust properly
        leftPunctuationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        rightPunctuationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Callbacks
        Callback<TableColumn<PunctChar, String>, TableCell<PunctChar, String>>
                cellStrFactory = setStringTableColumn -> {
            EditingStrCell<PunctChar> esc = new EditingStrCell<>();
            esc.setAlignment(Pos.CENTER);
            esc.setFont(new Font("Courier", 18));
            return esc;
        };

        Callback<TableColumn<PunctChar, PunctChar.Use>, TableCell<PunctChar, PunctChar.Use>> cellComboFactory1
                = setStringTableColumn -> new ComboBoxTableCell<>(PunctChar.Use.values());

        Callback<TableColumn<PunctChar, PunctChar.Use>, TableCell<PunctChar, PunctChar.Use>>
            cellComboFactory2 =
                setStringTableColumn -> new ComboBoxTableCell<>(PunctChar.Use.values());

        // Set cellValue Factory
        leftASCIIColumn.setCellValueFactory(new PropertyValueFactory<>("Char"));
        leftTypeColumn.setCellValueFactory(new PropertyValueFactory<>("Use"));
        rightASCIIColumn.setCellValueFactory(new PropertyValueFactory<>("Char"));
        rightTypeColumn.setCellValueFactory(new PropertyValueFactory<>("Use"));

        // Set cell factory and onEditCommit
        leftASCIIColumn.setCellFactory(cellStrFactory);
        rightASCIIColumn.setCellFactory(cellStrFactory);
        // no on edit necessary

        leftTypeColumn.setCellFactory(cellComboFactory1);
        leftTypeColumn.setOnEditCommit(text -> text.getRowValue().setUse(
                text.getNewValue()));
        rightTypeColumn.setCellFactory(cellComboFactory2);
        rightTypeColumn.setOnEditCommit(text -> text.getRowValue().setUse(
                text.getNewValue()));

        // Put values into table
        ObservableList<PunctChar> leftData = leftPunctuationTable.getItems();
        ObservableList<PunctChar> rightData = rightPunctuationTable.getItems();
        List<PunctChar> originalPunctChars = mediator.getMachine().getPunctChars();
        int leftSize = (originalPunctChars.size()) / 2;
        int rightSize = originalPunctChars.size() - leftSize;
        for (int i = 0; i < leftSize + rightSize; i++) {
            if (i < leftSize) {
                leftData.add(originalPunctChars.get(i).copy());
            } else {
                rightData.add(originalPunctChars.get(i).copy());
            }
        }
        leftPunctuationTable.setItems(leftData);
        rightPunctuationTable.setItems(rightData);

        // for disabling appropriately
        punctuationTab.disableProperty().bind(mediator.getDesktopController().inDebugOrRunningModeProperty());
    }

    /**
     * Initializes the IOOptions tab.
     */
    private void initializeIOOptionsTab() {

        // make initial GUIChannels
        allChannels = FXCollections.observableArrayList(
                GUIChannels.CONSOLE,
                GUIChannels.DIALOG,
                GUIChannels.FILE);

        ObservableList<Microinstruction> ios = mediator.getMachine().getMicros("io");
        for (int i = 0; i < ios.size(); i++) {
            IOChannel channel = ((IO) ios.get(i)).getConnection();
            if (channel instanceof FileChannel) {
                allChannels.add(channel);
            }
        }

        // Accounts for width changes.
        IOOptionsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        nameColumn.prefWidthProperty().bind(IOOptionsTable.widthProperty().subtract(4).multiply(.25));
        connectionColumn.prefWidthProperty().bind(IOOptionsTable.widthProperty().subtract(4).multiply(.75));

       // updates selectedSet
        IOOptionsTable.getSelectionModel().selectedItemProperty().addListener(
                (selected, oldSet, newSet) -> {
            IOOptionsSelectedSet = newSet;
            updateIOTableDisplay();
        });

        // a cellFactory for the IOChannel column
        Callback<TableColumn<IOOptionsData, IOChannel>, TableCell<IOOptionsData, IOChannel>>
                cellComboIOChannelFactory =
                setStringTableColumn -> new IOComboBoxTableCell<>(allChannels);

        // Set cellValue Factory
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("io"));
        connectionColumn.setCellValueFactory(new PropertyValueFactory<>("channel"));

        // Set cell factory and onEditCommit, only for connection column because
        // IO column is not editable.
        connectionColumn.setCellFactory(cellComboIOChannelFactory);
        connectionColumn.setOnEditCommit(text -> {
            if (!text.getNewValue().equals(GUIChannels.FILE)) {
                text.getRowValue().setChannel(text.getNewValue());
            } else {
                IOChannel newChannel = getFileChannelFromChooser(text.getOldValue());
                IOOptionsSelectedSet.setChannel(newChannel);
            }
        });

        // Load in rows
        ObservableList<IOOptionsData> data = IOOptionsTable.getItems();
        for (int i = 0; i < ios.size(); i++) {
            IO channel = ((IO) ios.get(i));
            data.add(new IOOptionsData(channel, channel.getConnection()));
        }
        IOOptionsTable.setItems(data);

        // for disabling appropriately
        IOOptionsTab.disableProperty().bind(
                mediator.getDesktopController().inDebugOrRunningModeProperty());
        if (IOOptionsTab.isDisabled()) {
            for (Tab tab : tabPane.getTabs()) {
                if (!tab.isDisabled()) {
                    tabPane.getSelectionModel().select(tab);
                    break;
                }
            }
        }

        // Set up change all combo box

        // This uses the Null Object Pattern to ensure that
        // the change listener is called appropriately.
        final ComboBoxChannel nullChannel = new NullComboBoxChannel("individually");
        ComboBoxChannel dialogCBChannel = new ComboBoxChannel(
                "to " + GUIChannels.DIALOG.toString(),
                GUIChannels.DIALOG);
        ComboBoxChannel consoleCBChannel = new ComboBoxChannel(
                "to " + GUIChannels.CONSOLE.toString(),
                GUIChannels.CONSOLE);
        this.changeAllCombo.getItems().addAll(
                nullChannel, dialogCBChannel, consoleCBChannel);
        this.changeAllCombo.setValue(nullChannel);

        changeAllCombo.valueProperty().addListener(new ChangeListener<ComboBoxChannel>() {
            @Override
            public void changed(ObservableValue<? extends ComboBoxChannel> arg0,
                                ComboBoxChannel oldChannel, ComboBoxChannel newChannel) {
                // Change all data in table
                // If new channel is null channel then this does nothing,
                // as it should.
                for (IOOptionsData ioData : OptionsController.this.IOOptionsTable.getItems()) {
                    newChannel.setChannelInIOOptionsData(ioData);
                }

                // For some reason, need to run later so that table
                // and other gui components have time to update
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        changeAllCombo.setValue(nullChannel);
                    }
                });

            }
        });
    }


    ////////////// OTHER METHODS ///////////////////


    /**
     * Gets a file channel from the user.
     *
     * @param oldChannel - The default return if the file chooser
     *                   fails for some reason to give a valid file.
     * @return IOChannel - The new channel if successful, oldValue
     * if not.
     */
    private IOChannel getFileChannelFromChooser(IOChannel oldChannel) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Data File");
        fileChooser.setInitialDirectory(null);
        File file = fileChooser.showOpenDialog(helpButton.getScene().getWindow());

        try {
            if (file != null) {
                FileChannel newFileChannel = new FileChannel(file);

                // add it to the ComboBox if not already there
                boolean alreadyIn = false;
                for (IOChannel ioc : allChannels) {
                    if (newFileChannel.toString().equals(ioc.toString())) {
                        alreadyIn = true;
                        newFileChannel = (FileChannel) ioc;
                    }
                }
                if (!alreadyIn) {
                    allChannels.add(newFileChannel);
                }

                return newFileChannel;
            }
        } catch (Exception e) {
            Dialogs.createErrorDialog(helpButton.getScene().getWindow(), "File Load Error",
                    "Something went wrong while trying to " +
                            "load the file. Please make " +
                            "sure the file is valid and try again.").showAndWait();
        }
        return oldChannel;
    }

    /**
     * Initializes the indexing tab
     */
    private void initializeIndexingTab() {

        changeIndexingDirection = false;

        ChangeListener indexChangeListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object t, Object t1) {
                changeIndexingDirection = !changeIndexingDirection;
            }
        };

        indexChoice.setValue(mediator.getMachine().getIndexFromRight() ? "right" : "left");
        indexChoice.getSelectionModel().selectedIndexProperty().addListener(indexChangeListener);
    }


    /**
     * Initializes the breakpoints tab.
     */
    private void initializeBreakPointsTab() {
        if (registers.size() < 1) {
            breakPointsTab.setDisable(true);
        } else {
            ObservableList<Register> registers =
                                  FXCollections.observableArrayList(this.registers);
            registers.add(0, Machine.PLACE_HOLDER_REGISTER);
            programCounterChoice.setItems(registers);
            programCounterChoice.setValue(mediator.getMachine().getProgramCounter());
        }
    }

    /**
     * updates the values in the connection column using a hack
     */
    private void updateIOTableDisplay() {
        nameColumn.setVisible(false);
        nameColumn.setVisible(true);
        connectionColumn.setVisible(false);
        connectionColumn.setVisible(true);
    }

    //=========== inner class for IO Options tab ============

    class IOComboBoxTableCell<S, T> extends ComboBoxTableCell<S, T> {

        public IOComboBoxTableCell(ObservableList<T> items) {
            super(items);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void updateItem(T item, boolean empty) {
            if (!item.equals(GUIChannels.FILE) ||
                    IOOptionsSelectedSet == null) {
                super.updateItem(item, empty);
                if (item instanceof FileChannel) {
                    File file = ((FileChannel) item).getFile();
                    setText(file.getName());
                    setTooltip(new Tooltip(file.toString()));
                } else {
                    if (item != null) {
                        setTooltip(new Tooltip(item.toString()));
                    }
                }
            } else if (IOOptionsSelectedSet.getChannel() instanceof FileChannel) {
                //if item is FILE_CHANNEL & a row is selected,
                // just display that selected channel -- this is
                // to fix a bug in ComboBoxTableCell
                // also set a tooltip to the full path name
                super.updateItem((T) IOOptionsSelectedSet.getChannel(), empty);
                File file = ((FileChannel)
                        IOOptionsSelectedSet.getChannel()).getFile();
                setText(file.getName());
                setTooltip(new Tooltip(file.toString()));
            }
            //added on 12/3/13 to make sure that when the hits cancel on the filechooser
            //dialog, an error will not be thrown and the values in the table will update
            //properly
            else {
                updateIOTableDisplay();
            }
        }
    }

    ////////////////////// Other Methods //////////////////////

    public void selectTab(int index) {
        if (0 <= index && index <= 3) {
            if (!tabPane.getTabs().get(index).isDisabled()) {
                tabPane.getSelectionModel().select(index);
            }
        }
    }

    /**
     * This is a private inner class for the
     * set all connections combo box.
     */
    private class ComboBoxChannel {
        private String name;
        private BufferedChannel buffChannel;

        public ComboBoxChannel(String name, BufferedChannel ioChan) {
            setName(name);
            setChannel(ioChan);
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BufferedChannel getChannel() {
            return this.buffChannel;
        }

        public void setChannel(BufferedChannel buffChannel) {
            this.buffChannel = buffChannel;
        }

        public String toString() {
            return getName();
        }

        public void setChannelInIOOptionsData(IOOptionsData ioOpsData) {
            ioOpsData.setChannel(getChannel());
        }
    }

    /**
     * A null combo box to use Null object pattern.
     * Overrides setChannelInIOOptionsData to do nothing
     * so that dynamic method invocation can be used
     * in change listener correctly.
     */
    private class NullComboBoxChannel extends ComboBoxChannel {

        public NullComboBoxChannel(String name) {
            super(name, null);
        }

        @Override
        public void setChannelInIOOptionsData(IOOptionsData ioOpsData) {
            // Do nothing, Null Object pattern used here
        }

    }
}