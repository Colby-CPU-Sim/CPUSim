/**
 * auther: Jinghui Yu
 * last edit date: 6/3/2013
 * <p>
 * File: PreferencesController
 * Authors: Joseph Harwood and Jake Epstein
 * Date: 11/15/13
 * <p>
 * Added field clearConsoleOnRun as a CheckBox from an fxml source.
 * (Additionally added the separator and checkbox in Preferences.fxml)
 * <p>
 * Edited method saveOtherTab to save the value of the clearConsoleOnRun CheckBox
 * to the DesktopController's OtherSettings object.
 * <p>
 * Edited method setValues to load the CheckBox with the appropriate state as
 * signified by the DesktopController's OtherSettings object.
 */

/**
 * File: PreferencesController
 * Authors: Joseph Harwood and Jake Epstein
 * Date: 11/15/13
 *
 * Added field clearConsoleOnRun as a CheckBox from an fxml source.
 * (Additionally added the separator and checkbox in Preferences.fxml)
 *
 * Edited method saveOtherTab to save the value of the clearConsoleOnRun CheckBox
 * to the DesktopController's OtherSettings object.
 *
 * Edited method setValues to load the CheckBox with the appropriate state as
 * signified by the DesktopController's OtherSettings object.
 */

package cpusim.gui.preferences;

import cpusim.Mediator;
import cpusim.gui.desktop.DesktopController;
import cpusim.gui.desktop.FontData;
import cpusim.gui.desktop.KeyCodeInfo;
import cpusim.gui.desktop.editorpane.CodePaneController;
import cpusim.gui.desktop.editorpane.StyleInfo;
import cpusim.gui.help.HelpController;
import cpusim.util.Dialogs;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * The controller for the Preferences Dialog.
 */
public class PreferencesController implements Initializable {
    @FXML
    ChoiceBox<String> assmFont;
    @FXML
    ChoiceBox<String> registersFont;
    @FXML
    ChoiceBox<String> ramsFont;
    @FXML
    ChoiceBox<String> assmFontSize;
    @FXML
    ChoiceBox<String> registersFontSize;
    @FXML
    ChoiceBox<String> ramsFontSize;
    @FXML
    ColorPicker assmBackground;
    @FXML
    ColorPicker registersBackground;
    @FXML
    ColorPicker ramsBackground;
    @FXML
    ColorPicker assmSelection;
    @FXML
    ColorPicker registersSelection;
    @FXML
    ColorPicker ramsSelection;

    @FXML
    CheckBox instrsBold;
    @FXML
    CheckBox keywordsBold;
    @FXML
    CheckBox labelsBold;
    @FXML
    CheckBox literalsBold;
    @FXML
    CheckBox symbolsBold;
    @FXML
    CheckBox stringsBold;
    @FXML
    CheckBox commentsBold;
    @FXML
    CheckBox instrsItalic;
    @FXML
    CheckBox keywordsItalic;
    @FXML
    CheckBox labelsItalic;
    @FXML
    CheckBox literalsItalic;
    @FXML
    CheckBox symbolsItalic;
    @FXML
    CheckBox stringsItalic;
    @FXML
    CheckBox commentsItalic;
    @FXML
    ColorPicker instrsColor;
    @FXML
    ColorPicker keywordsColor;
    @FXML
    ColorPicker labelsColor;
    @FXML
    ColorPicker literalsColor;
    @FXML
    ColorPicker symbolsColor;
    @FXML
    ColorPicker stringsColor;
    @FXML
    ColorPicker commentsColor;

    @FXML
    CheckBox autoSave;
    @FXML
    CheckBox showLineNumbers;
    @FXML
    CheckBox lineWrap;
    @FXML
    CheckBox clearConsoleOnRun;

    @FXML
    Button applyButton;
    @FXML
    Button okayButton;
    @FXML
    Button closeButton;

    @FXML
    GridPane keyPrefsPane;

    @FXML
    TabPane tabPane;

    private final Mediator mediator;
    private final DesktopController desktopController;

    // fields for the key bindings tab
    private boolean listening;
    private Label currLabel; // the current binding label
    private Label[] keyBindingLabels; // the labels in the right column of the table


    public PreferencesController(Mediator mediator, DesktopController desktopController) {
        this.mediator = mediator;
        this.desktopController = desktopController;
    }

    /**
     * initializes the dialog window after its root element has been processed.
     * set the number of visible rows of combo boxes to be 8.
     *
     * @param url the location used to resolve relative paths for the root
     *            object, or null if the location is not known.
     * @param rb  the resources used to localize the root object, or null if the root
     *            object was not localized.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // fit the Preferences dialog to be smaller than the desktop stage
        tabPane.setMaxHeight(desktopController.getStage().getHeight() - 100);
        //keyPrefsPane.setGridLinesVisible(true); // for debugging

        initializeFontTab();
        initializeKeyTab();
        initializeOtherTab();
    }

    private void initializeKeyTab() {
        initializeKeyBindingColumn();
        listening = false;
        currLabel = keyBindingLabels[0];

        //if you change tabs away from the Keys tab, be sure the currLabel is de-selected
        tabPane.getSelectionModel().selectedIndexProperty().addListener(
                (ov, prevIndex, newIndex) -> {
                    if (prevIndex.intValue() == 1 && listening) {
                        PreferencesController.this.stopListening();
                    }
                });
    }

    private void initializeOtherTab() {
        autoSave.selectedProperty().set(desktopController.getOtherSettings().autoSave);
        showLineNumbers.selectedProperty().set(desktopController.getOtherSettings()
                .showLineNumbers.get());
        lineWrap.selectedProperty().set(desktopController.getOtherSettings().lineWrap
                .get());
        clearConsoleOnRun.selectedProperty().set(desktopController.getOtherSettings()
                .clearConsoleOnRun);
    }

    /**
     * sets the initial values for the combo boxes based on what the values are
     * in the desktopController
     */
    private void initializeFontTab() {
        // initialize the font family, font size, background color and
        // selection color for all panes
        FontData assmFontData =
                desktopController.getAssemblyPaneFontData();
        FontData registerTableFontData =
                desktopController.getRegisterTableFontData();
        FontData ramTableFontData =
                desktopController.getRamTableFontData();

        assmFont.setValue(assmFontData.font);
        assmFontSize.setValue(assmFontData.fontSize);
        assmBackground.setValue(Color.web(assmFontData.background));
        assmSelection.setValue(Color.web(assmFontData.selection));

        registersFont.setValue(registerTableFontData.font);
        registersFontSize.setValue(registerTableFontData.fontSize);
        registersBackground.setValue(Color.web(registerTableFontData.background));
        registersSelection.setValue(Color.web(registerTableFontData.selection));

        ramsFont.setValue(ramTableFontData.font);
        ramsFontSize.setValue(ramTableFontData.fontSize);
        ramsBackground.setValue(Color.web(ramTableFontData.background));
        ramsSelection.setValue(Color.web(ramTableFontData.selection));

        // initialize the settings for styles for the parts of an assembly language
        // program
        initializeAssemblyLanguageStyles();
    }

    private void initializeAssemblyLanguageStyles() {
        CodePaneController codePaneController = desktopController.getCodePaneController();
        StyleInfo defaultStyle = codePaneController.getStyleInfo("default");

        StyleInfo instrStyle = codePaneController.getStyleInfo("instr");
        instrsBold.setSelected(instrStyle.bold.isPresent() ? instrStyle.bold.get()
                : defaultStyle.bold.get());
        instrsItalic.setSelected(instrStyle.italic.isPresent() ? instrStyle.italic.get()
                : defaultStyle.italic.get());
        instrsColor.setValue(instrStyle.textColor.isPresent() ?
                Color.web(instrStyle.textColor.get()) :
                Color.web(defaultStyle.textColor.get()));

        StyleInfo keywordStyle = codePaneController.getStyleInfo("keyword");
        keywordsBold.setSelected(keywordStyle.bold.isPresent() ? keywordStyle.bold.get()
                : defaultStyle.bold.get());
        keywordsItalic.setSelected(keywordStyle.italic.isPresent() ? keywordStyle
                .italic.get()
                : defaultStyle.italic.get());
        keywordsColor.setValue(keywordStyle.textColor.isPresent() ?
                Color.web(keywordStyle.textColor.get()) :
                Color.web(defaultStyle.textColor.get()));

        StyleInfo labelStyle = codePaneController.getStyleInfo("label");
        labelsBold.setSelected(labelStyle.bold.isPresent() ? labelStyle.bold.get()
                : defaultStyle.bold.get());
        labelsItalic.setSelected(labelStyle.italic.isPresent() ? labelStyle.italic.get()
                : defaultStyle.italic.get());
        labelsColor.setValue(labelStyle.textColor.isPresent() ?
                Color.web(labelStyle.textColor.get()) :
                Color.web(defaultStyle.textColor.get()));

        StyleInfo commentStyle = codePaneController.getStyleInfo("comment");
        commentsBold.setSelected(commentStyle.bold.isPresent() ? commentStyle.bold.get()
                : defaultStyle.bold.get());
        commentsItalic.setSelected(commentStyle.italic.isPresent() ? commentStyle
                .italic.get()
                : defaultStyle.italic.get());
        commentsColor.setValue(commentStyle.textColor.isPresent() ?
                Color.web(commentStyle.textColor.get()) :
                Color.web(defaultStyle.textColor.get()));

        StyleInfo stringStyle = codePaneController.getStyleInfo("string");
        stringsBold.setSelected(stringStyle.bold.isPresent() ? stringStyle.bold.get()
                : defaultStyle.bold.get());
        stringsItalic.setSelected(stringStyle.italic.isPresent() ? stringStyle.italic
                .get()
                : defaultStyle.italic.get());
        stringsColor.setValue(stringStyle.textColor.isPresent() ?
                Color.web(stringStyle.textColor.get()) :
                Color.web(defaultStyle.textColor.get()));

        StyleInfo symbolStyle = codePaneController.getStyleInfo("symbol");
        symbolsBold.setSelected(symbolStyle.bold.isPresent() ? symbolStyle.bold.get()
                : defaultStyle.bold.get());
        symbolsItalic.setSelected(symbolStyle.italic.isPresent() ? symbolStyle.italic
                .get()
                : defaultStyle.italic.get());
        symbolsColor.setValue(symbolStyle.textColor.isPresent() ?
                Color.web(symbolStyle.textColor.get()) :
                Color.web(defaultStyle.textColor.get()));

        StyleInfo literalStyle = codePaneController.getStyleInfo("literal");
        literalsBold.setSelected(literalStyle.bold.isPresent() ? literalStyle.bold.get()
                : defaultStyle.bold.get());
        literalsItalic.setSelected(literalStyle.italic.isPresent() ? literalStyle
                .italic.get()
                : defaultStyle.italic.get());
        literalsColor.setValue(literalStyle.textColor.isPresent() ?
                Color.web(literalStyle.textColor.get()) :
                Color.web(defaultStyle.textColor.get()));
    }

    @FXML
    protected void onApplyButtonClick(ActionEvent e) {
        if (tabPane.getTabs().get(0).isSelected()) {
            saveFontTab();
        }
        else if (tabPane.getTabs().get(1).isSelected()) {
            saveKeyBindingsTab();
        }
        else {
            saveOtherTab();
        }
        desktopController.updateStyleOfTables();
        desktopController.refreshTopTabPane();
    }

    /**
     * save the changes after clicking the ok button.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    protected void onOKButtonClick(ActionEvent e) {
        saveFontTab();
        saveKeyBindingsTab();
        saveOtherTab();

        desktopController.updateStyleOfTables();
        desktopController.refreshTopTabPane();

        onCloseButtonClick(null);
    }

    private void saveFontTab() {
        FontData assmFontData =
                desktopController.getAssemblyPaneFontData();
        FontData registerTableFontData =
                desktopController.getRegisterTableFontData();
        FontData ramTableFontData =
                desktopController.getRamTableFontData();

        assmFontData.font = assmFont.getValue();
        assmFontData.fontSize = assmFontSize.getValue();
        assmFontData.background = toRGBCode(assmBackground.getValue());
        assmFontData.selection = toRGBCode(assmSelection.getValue());

        registerTableFontData.font = registersFont.getValue();
        registerTableFontData.fontSize = registersFontSize.getValue();
        registerTableFontData.background = toRGBCode(registersBackground.getValue());
        registerTableFontData.selection = toRGBCode(registersSelection.getValue());

        ramTableFontData.font = ramsFont.getValue();
        ramTableFontData.fontSize = ramsFontSize.getValue();
        ramTableFontData.background = toRGBCode(ramsBackground.getValue());
        ramTableFontData.selection = toRGBCode(ramsSelection.getValue());

        // save the assembly language styles
        saveAssemblyLanguageStyles();
    }

    private void saveKeyBindingsTab() {
        Map<String, KeyCodeInfo> realBindings = desktopController.getKeyBindings();
        int i = 0;
        for (KeyCodeInfo info : realBindings.values()) {
            info.setKeyCode(keyBindingLabels[i].getText());
            i++;
        }
    }

    private void saveOtherTab() {
        desktopController.getOtherSettings().autoSave = this.autoSave.isSelected();
        desktopController.getOtherSettings().showLineNumbers.set(this.showLineNumbers
                .isSelected());
        desktopController.getOtherSettings().lineWrap.set(this.lineWrap.isSelected());
        desktopController.getOtherSettings().clearConsoleOnRun = this.clearConsoleOnRun
                .isSelected();
    }


    /**
     * close the window without saving the changes.
     *
     * @param e a type of action when a button is clicked.
     */
    @FXML
    protected void onCloseButtonClick(ActionEvent e) {
        //get a handle to the stage.
        Stage stage = (Stage) closeButton.getScene().getWindow();

        // WHY WERE THE FOLLOWING TWO LINES OF CODE INCLUDED?
        // I COULDN'T SEE WHY SO I COMMENTED THEM OUT.
        //reset values for the Fonts tab and Other tab
//        initializeFontTab();
//        initializeOtherTab();

        //close window.
        stage.close();
    }

    @FXML
    protected void handleSetDefaultKeyBindings(ActionEvent e) {
        int i = 0;
        for (String[] kb : DesktopController.DEFAULT_KEY_BINDINGS) {
            keyBindingLabels[i].setText(kb[1]);
            i++;
        }
        stopListening();
    }

    @FXML
    protected void handleHelp(ActionEvent e) {
        String startString = "Preferences Dialog";
        if (mediator.getDesktopController().getHelpController() == null) {
            HelpController helpController = HelpController.openHelpDialog(
                    mediator.getDesktopController(), startString);
            mediator.getDesktopController().setHelpController(helpController);
        }
        else {
            HelpController hc = mediator.getDesktopController().getHelpController();
            hc.getStage().toFront();
            hc.selectTreeItem(startString);
        }
    }

    private void saveAssemblyLanguageStyles() {

        CodePaneController codePaneController = desktopController.getCodePaneController();
        StyleInfo defaultStyle = codePaneController.getStyleInfo("default");

        StyleInfo newStyle = defaultStyle.
                updateBold(instrsBold.isSelected()).
                updateItalic(instrsItalic.isSelected()).
                updateTextColor(toRGBCode(instrsColor.getValue()));
        codePaneController.setStyleInfo("instr", newStyle);

        newStyle = defaultStyle.
                updateBold(keywordsBold.isSelected()).
                updateItalic(keywordsItalic.isSelected()).
                updateTextColor(toRGBCode(keywordsColor.getValue()));
        codePaneController.setStyleInfo("keyword", newStyle);

        newStyle = defaultStyle.
                updateBold(labelsBold.isSelected()).
                updateItalic(labelsItalic.isSelected()).
                updateTextColor(toRGBCode(labelsColor.getValue()));
        codePaneController.setStyleInfo("label", newStyle);

        newStyle = defaultStyle.
                updateBold(commentsBold.isSelected()).
                updateItalic(commentsItalic.isSelected()).
                updateTextColor(toRGBCode(commentsColor.getValue()));
        codePaneController.setStyleInfo("comment", newStyle);

        newStyle = defaultStyle.
                updateBold(stringsBold.isSelected()).
                updateItalic(stringsItalic.isSelected()).
                updateTextColor(toRGBCode(stringsColor.getValue()));
        codePaneController.setStyleInfo("string", newStyle);

        newStyle = defaultStyle.
                updateBold(symbolsBold.isSelected()).
                updateItalic(symbolsItalic.isSelected()).
                updateTextColor(toRGBCode(symbolsColor.getValue()));
        codePaneController.setStyleInfo("symbol", newStyle);

        newStyle = defaultStyle.
                updateBold(literalsBold.isSelected()).
                updateItalic(literalsItalic.isSelected()).
                updateTextColor(toRGBCode(literalsColor.getValue()));
        codePaneController.setStyleInfo("literal", newStyle);
    }

    /**
     * initializes the display of the key bindings on the left side of the key bindings
     * tab
     */
    private void initializeKeyBindingColumn() {
        keyBindingLabels = new Label[DesktopController.DEFAULT_KEY_BINDINGS.length];

        int currRow = 0;
        ContextMenu contextMenu = new ContextMenu();
        MenuItem setToDefault = new MenuItem("setToDefault");
        MenuItem setToNoKeyBinding = new MenuItem("setToNoKeyBinding");

        Map<String, KeyCodeInfo> realKeyBindings = desktopController.getKeyBindings();
        for (String menuItem : realKeyBindings.keySet()) {
            // Note:  The correct order is preserved since realKeyBindings is
            //        actually a LinkedHashMap
            String binding = realKeyBindings.get(menuItem).getKeyCode();

            final Label kbLabel = new Label(binding);
            keyPrefsPane.add(kbLabel, 1, currRow);
            keyBindingLabels[currRow] = kbLabel;
            kbLabel.setOnMouseClicked(t -> {
                    currLabel.setStyle("-fx-background-color:white;" +
                            "-fx-border-color:white;");
                    kbLabel.setStyle("-fx-background-color:lightblue;" +
                            "-fx-border-style:solid; -fx-padding:1;"
                            + "-fx-border-color:black;");
                    currLabel = kbLabel;
                    listenForKeyEvents();
            });
            setToDefault.setOnAction(t -> {
                String kbString = DesktopController
                        .DEFAULT_KEY_BINDINGS[GridPane.getRowIndex(currLabel)][1];
                boolean conflict = false;
                int conflictIndex = 0;
                for (Label label : keyBindingLabels) {
                    if (label.getText().equals(kbString)
                            && !label.equals(currLabel)) {
                        conflict = true;
                        String conflictMenuItem =
                                DesktopController.DEFAULT_KEY_BINDINGS[conflictIndex][0];

                        Optional<ButtonType> result = Dialogs.createConfirmationDialog(
                                keyPrefsPane.getScene().getWindow(),
                                "Key Binding Conflict",
                                "The menu item '" +
                                        conflictMenuItem
                                        + "' already uses the key binding '" + kbString
                                        + "'.  Would you like to "
                                        + "reassign the key binding?").showAndWait();

                        if (result.get() == ButtonType.OK) {
                            label.setText("          ");
                            currLabel.setText(kbString);
                            currLabel.setStyle("-fx-background-color:white;" +
                                    "-fx-border-color:white;");
                            currLabel = label;
                            currLabel.setText("         ");
                            currLabel.setStyle("-fx-background-color:lightblue;" +
                                    "-fx-border-style:solid; -fx-padding:1"
                                    + "-fx-border-color:black;");
                            listenForKeyEvents();

                        }
                        else if (result.get() == ButtonType.CANCEL) {
                            return;
                        }
                        else /* response == Actions.NO */ {
                            label.setText("          ");
                            currLabel.setText(kbString);
                            currLabel = label;
                            currLabel.setText("         ");
                        }
                        break;
                    }
                    conflictIndex++;
                }
                if (!conflict) {
                    currLabel.setText(kbString);
                }
            });
            setToNoKeyBinding.setOnAction(t -> {
                currLabel.setText("         ");
                stopListening();
            });
            contextMenu.getItems().clear();
            contextMenu.getItems().addAll(setToDefault, setToNoKeyBinding);
            kbLabel.setContextMenu(contextMenu);
            currRow++;
        }
    }

    /**
     * sets a key listener to active so that you the user can properly set the key
     * binding to whatever key they would like
     */
    private void listenForKeyEvents() {
        listening = true;

        keyPrefsPane.getScene().setOnKeyPressed(t -> {
            if (t.getCode().isModifierKey()) {
                return; // wait until they press the regular key
            }

            String keyString = String.valueOf(t.getCode());
            if (keyString.contains("DIGIT")) {
                keyString = keyString.replaceAll("DIGIT", "");
            }

            if (keyString.length() > 1) {
                String[] words = keyString.split("_");
                keyString = "";
                for (String word : words) {
                    keyString += word.substring(0, 1) + word.substring(1)
                            .toLowerCase() + "_";
                }
                keyString = keyString.substring(0, keyString.length() - 1);
            }

            String kbString;
            if (t.isShiftDown() && t.isAltDown() && t.isControlDown()) {
                if (System.getProperty("os.name").startsWith("Windows")) {
                    kbString = DesktopController.SHORTCUT + "-Shift-Alt-" + keyString;
                }
                else {
                    kbString = DesktopController.SHORTCUT + "-Shift-Alt-Ctrl-" +
                            keyString;
                }
            }
            else if (t.isAltDown() && t.isControlDown()) {
                if (System.getProperty("os.name").startsWith("Windows")) {
                    kbString = DesktopController.SHORTCUT + "-Alt-" + keyString;
                }
                else {
                    kbString = DesktopController.SHORTCUT + "-Alt-Ctrl-" + keyString;
                }
            }
            else if (t.isShiftDown() && t.isControlDown()) {
                if (System.getProperty("os.name").startsWith("Windows")) {
                    kbString = DesktopController.SHORTCUT + "-Shift-" + keyString;
                }
                else {
                    kbString = DesktopController.SHORTCUT + "-Shift-Ctrl-" +
                            keyString;
                }
            }
            else if (t.isControlDown()) {
                if (System.getProperty("os.name").startsWith("Windows")) {
                    kbString = DesktopController.SHORTCUT + "-" + keyString;
                }
                else {
                    kbString = DesktopController.SHORTCUT + "-Shift-Ctrl-" +
                            keyString;
                }
            }
            else if (t.isShiftDown() && t.isAltDown()) {
                kbString = DesktopController.SHORTCUT + "-Shift-Alt-" + keyString;
            }
            else if (t.isShiftDown()) {
                kbString = DesktopController.SHORTCUT + "-Shift-" + keyString;
            }
            else if (t.isAltDown()) {
                kbString = DesktopController.SHORTCUT + "-Alt-" + keyString;
            }
            else {
                kbString = DesktopController.SHORTCUT + "-" + keyString;
            }
            boolean stillListening = false;

            // check for duplicate bindings
            int conflictIndex = 0;
            for (Label label : keyBindingLabels) {
                if (label.getText().equals(kbString)
                        && !label.equals(currLabel)) {

                    String conflictMenuItem =
                            DesktopController.DEFAULT_KEY_BINDINGS[conflictIndex][0];
                    Optional<ButtonType> result = Dialogs.createConfirmationDialog(
                            keyPrefsPane.getScene().getWindow(),
                            "Key Binding Conflict",
                            "The key binding of the menu item '" +
                                    conflictMenuItem
                                    + "' already has the key binding '" + kbString
                                    + "'.  Would you "
                                    + "like to reassign the key binding of '" +
                                    conflictMenuItem + "'?").showAndWait();

                    if (result.get() == ButtonType.OK) {
                        label.setText("          ");
                        stillListening = true;
                    }
                    else if (result.get() == ButtonType.CANCEL) {
                        return;
                    }
                    else {
                        label.setText("          ");
                    }
                    break;
                }
                conflictIndex++;
            }

            //don't let the user assign key bindings that cannot be changed
            if (kbString.equals("Ctrl-X")
                    || kbString.equals("Ctrl-V")
                    || kbString.equals("Ctrl-A")
                    || kbString.equals("Ctrl-C")
                    || kbString.equals("Ctrl-Z")
                    || kbString.equals("Ctrl-X")
                    || kbString.equals("Ctrl-Shift-Z")
                    || kbString.equals("Cmd-X")
                    || kbString.equals("Cmd-V")
                    || kbString.equals("Cmd-A")
                    || kbString.equals("Cmd-C")
                    || kbString.equals("Cmd-Z")
                    || kbString.equals("Cmd-X")
                    || kbString.equals("Cmd-Shift-Z")) {
                Dialogs.createErrorDialog(keyPrefsPane.getScene().getWindow(),
                        "Reserved Key Binding", "The key binding " + kbString +
                                " is reserved and so you cannot assign it"
                                + " to another menu item").showAndWait();
                return;

            }


            currLabel.setText(kbString);
            stopListening();
            if (stillListening) {
                currLabel = (Label) getNodeFromGridPane(keyPrefsPane, 1, conflictIndex);
                currLabel.setStyle("-fx-background-color:lightblue;" +
                        "-fx-border-style:solid; -fx-padding:1"
                        + "-fx-border-color:black;");
                listenForKeyEvents();
            }


        });
    }

    /**
     * sets the key event listener to inactive
     */
    private void stopListening() {
        listening = false;
        currLabel.setStyle("-fx-background-color:white;" +
                "-fx-border-color:white;");
        keyPrefsPane.getScene().setOnKeyPressed(null);
    }

    public static String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    /**
     * Given the row and col index, it returns the Node in that location in
     * the given GridPane.
     * (This function should have been included with the JavaFX
     * GridPane class.)
     * @param gridPane the GridPane whose cell is desired
     * @param col the col index of the desired cell
     * @param row the row index of the desired cell
     * @return the Node at the given row and col
     */
    private Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            int nodeCol = GridPane.getColumnIndex(node);
            int rowCol = GridPane.getRowIndex(node);
            if (nodeCol == col && rowCol == row) {
                return node;
            }
        }
        return null;
    }
}
