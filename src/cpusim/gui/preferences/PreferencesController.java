/**
 * auther: Jinghui Yu
 * last edit date: 6/3/2013
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
import cpusim.util.CPUSimConstants;
import cpusim.util.Dialogs;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

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
    AnchorPane keyBindingsPane;
    @FXML
    AnchorPane menuItemsPane;

    @FXML
    ScrollPane kbScrollPane;

    @FXML
    BorderPane mainPane;

    @FXML
    TabPane tabPane;

    @FXML
    Tab keyBindingsTab;

    private final Mediator mediator;
    private final DesktopController desktopController;

    // fields for the key bindings tab
    private List<String> keyBindings;  // local copy of the bindings for editing
    private boolean listening;
    private Label currLabel; // the current binding label
    private String currBinding; // the binding in the currLabel


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
        // fit main pane to the screen (roughly)
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();
        if (mainPane.getPrefWidth() > screenWidth) {
            mainPane.setPrefWidth(screenWidth - 75);
        }
        if (mainPane.getPrefHeight() > screenHeight) {
            mainPane.setPrefHeight(screenHeight - 40);
        }

        initializeKeyTab();
        initializeFontTab();
        initializeOtherTab();
    }

    private void initializeKeyTab() {
        listening = false;
        kbScrollPane.setStyle("-fx-background-color:white;");
        tabPane.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number
                    t1) {
                if (t.intValue() == 1 && listening) {
                    currLabel.setStyle("-fx-background-color:white;" +
                            "-fx-border-color:white;");
                    keyBindings.set(keyBindingsPane.getChildren().indexOf(currLabel),
                            currBinding);
                    updateKeyBindingDisplay();
                    stopListening();
                }
            }
        });
        initKeyBindingsField();
        updateKeyBindingDisplay();
        currLabel = (Label) keyBindingsPane.getChildren().get(0);
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
        // initialize the font family, font size, and background color for all panes
        FontData assmFontData =
                desktopController.getAssemblyPaneFontData();
        FontData registerTableFontData =
                desktopController.getRegisterTableFontData();
        FontData ramTableFontData =
                desktopController.getRamTableFontData();

        assmFont.setValue(assmFontData.font);
        assmFontSize.setValue(assmFontData.fontSize);
        assmBackground.setValue(Color.web(assmFontData.background));

        registersFont.setValue(registerTableFontData.font);
        registersFontSize.setValue(registerTableFontData.fontSize);
        registersBackground.setValue(Color.web(registerTableFontData.background));

        ramsFont.setValue(ramTableFontData.font);
        ramsFontSize.setValue(ramTableFontData.fontSize);
        ramsBackground.setValue(Color.web(ramTableFontData.background));

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
        registerTableFontData.font = registersFont.getValue();
        registerTableFontData.fontSize = registersFontSize.getValue();
        registerTableFontData.background = toRGBCode(registersBackground.getValue());
        ramTableFontData.font = ramsFont.getValue();
        ramTableFontData.fontSize = ramsFontSize.getValue();
        ramTableFontData.background = toRGBCode(ramsBackground.getValue());

        // save the assembly language styles
        saveAssemblyLanguageStyles();
    }

    private void saveKeyBindingsTab() {
        Map<String,KeyCodeInfo> realBindings = desktopController.getKeyBindings();
        int i = 0;
        for(KeyCodeInfo info : realBindings.values()) {
            info.setKeyCode(keyBindings.get(i));
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
    protected void handleDefault(ActionEvent e) {
        keyBindings.clear();
        for (String[] kb : desktopController.DEFAULT_KEY_BINDINGS) {
            keyBindings.add(kb[1]);
        }
        updateKeyBindingDisplay();
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
     * initializes the keyBindings variable based on what the key bindings according
     * to the desktop controller
     */
    private void initKeyBindingsField() {
        keyBindings = new ArrayList<>();

        Map<String, KeyCodeInfo> realKeyBindings = desktopController.getKeyBindings();

        for (String menuItem : realKeyBindings.keySet()) {
            keyBindings.add(realKeyBindings.get(menuItem).getKeyCode());
        }
    }

    /**
     * updates the display of the key bindings on the left side of the key bindings
     * tab
     */
    private void updateKeyBindingDisplay() {
        keyBindingsPane.getChildren().clear();
        int currY = 2;
        ContextMenu contextMenu = new ContextMenu();
        MenuItem setToDefault = new MenuItem("setToDefault");
        MenuItem setToNoKeyBinding = new MenuItem("setToNoKeyBinding");
        for (String binding : keyBindings) {

            final Label kbLabel = new Label(binding);
            kbLabel.setLayoutY(currY);
            kbLabel.setOnContextMenuRequested(t -> currLabel = kbLabel);
            kbLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent t) {
                    if (t.getButton() == MouseButton.PRIMARY) {
                        currLabel.setStyle("-fx-background-color:white;" +
                                "-fx-border-color:white;");
                        kbLabel.setStyle("-fx-background-color:lightblue;" +
                                "-fx-border-style:solid;"
                                + "-fx-border-color:black;");
                        currBinding = kbLabel.getText();
                        currLabel = kbLabel;
                        listenForKeyEvents();
                    }
                }
            });
            setToDefault.setOnAction(t -> {
                String kbString = DesktopController
                        .DEFAULT_KEY_BINDINGS[keyBindingsPane.getChildren().indexOf
                        (currLabel)][1];
                boolean conflict = false;
                for (Node label : keyBindingsPane.getChildren()) {
                    if (((Label) label).getText().equals(kbString)
                            && !label.equals(currLabel)) {
                        conflict = true;
                        String conflictMenuItem = ((Label) menuItemsPane.getChildren()
                                .get(
                                        keyBindingsPane.getChildren().indexOf(label)))
                                .getText();
                        Optional<ButtonType> result = Dialogs.createConfirmationDialog(
                                keyBindingsPane.getScene().getWindow(),
                                "Key Binding Conflict",
                                "The key binding of the menu item '" +
                                        conflictMenuItem
                                        + "' already has the key binding '" + kbString
                                        + "'.  Would you"
                                        + "like to reassign the key binding of '" +
                                        conflictMenuItem + "'?").showAndWait();

                        if (result.get() == ButtonType.OK) {
                            keyBindings.set(keyBindingsPane.getChildren().indexOf
                                    (label), "          ");
                            currBinding = ((Label) label).getText();
                            currLabel.setText(kbString);
                            currLabel.setStyle("-fx-background-color:white;" +
                                    "-fx-border-color:white;");
                            keyBindings.set(keyBindingsPane.getChildren().indexOf
                                    (currLabel), kbString);
                            currLabel = (Label) label;
                            currLabel.setText("         ");
                            currLabel.setStyle("-fx-background-color:lightblue;" +
                                    "-fx-border-style:solid;"
                                    + "-fx-border-color:black;");
                            listenForKeyEvents();

                        }
                        else if (result.get() == ButtonType.CANCEL) {
                            return;
                        }
                        else /* response == Actions.NO */ {
                            keyBindings.set(keyBindingsPane.getChildren().indexOf
                                    (label), "          ");
                            currLabel.setText(kbString);
                            keyBindings.set(keyBindingsPane.getChildren().indexOf
                                    (currLabel), kbString);
                            currLabel = (Label) label;
                            currLabel.setText("         ");
                        }
                        break;
                    }
                }
                if (!conflict) {
                    currLabel.setText(kbString);
                    keyBindings.set(keyBindingsPane.getChildren().indexOf(currLabel),
                            kbString);
                }
            });
            setToNoKeyBinding.setOnAction(t -> {
                currLabel.setText("         ");
                keyBindings.set(keyBindingsPane.getChildren().indexOf(currLabel), "" +
                        "       ");
                stopListening();
                currLabel.setStyle("-fx-background-color:white;" +
                        "-fx-border-color:white;");
            });
            contextMenu.getItems().clear();
            contextMenu.getItems().addAll(setToDefault, setToNoKeyBinding);
            kbLabel.setContextMenu(contextMenu);
            keyBindingsPane.getChildren().add(kbLabel);
            currY += 16;
        }
    }

    /**
     * sets a key listener to active so that you the user can properly set the key
     * binding to whatever key they would like
     */
    private void listenForKeyEvents() {
        listening = true;

        keyBindingsPane.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                //System.out.println(t.getCode());

                if (t.getCode().isModifierKey()) {
                    return;
                }

                String keyString = String.valueOf(t.getCode());
                if (keyString.indexOf("DIGIT") != -1) {
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
                        kbString = desktopController.SHORTCUT + "-Shift-Alt-" + keyString;
                    }
                    else {
                        kbString = desktopController.SHORTCUT + "-Shift-Alt-Ctrl-" +
                                keyString;
                    }
                }
                else if (t.isAltDown() && t.isControlDown()) {
                    if (System.getProperty("os.name").startsWith("Windows")) {
                        kbString = desktopController.SHORTCUT + "-Alt-" + keyString;
                    }
                    else {
                        kbString = desktopController.SHORTCUT + "-Alt-Ctrl-" + keyString;
                    }
                }
                else if (t.isShiftDown() && t.isControlDown()) {
                    if (System.getProperty("os.name").startsWith("Windows")) {
                        kbString = desktopController.SHORTCUT + "-Shift-" + keyString;
                    }
                    else {
                        kbString = desktopController.SHORTCUT + "-Shift-Ctrl-" +
                                keyString;
                    }
                }
                else if (t.isControlDown()) {
                    if (System.getProperty("os.name").startsWith("Windows")) {
                        kbString = desktopController.SHORTCUT + "-" + keyString;
                    }
                    else {
                        kbString = desktopController.SHORTCUT + "-Shift-Ctrl-" +
                                keyString;
                    }
                }
                else if (t.isShiftDown() && t.isAltDown()) {
                    kbString = desktopController.SHORTCUT + "-Shift-Alt-" + keyString;
                }
                else if (t.isShiftDown()) {
                    kbString = desktopController.SHORTCUT + "-Shift-" + keyString;
                }
                else if (t.isAltDown()) {
                    kbString = desktopController.SHORTCUT + "-Alt-" + keyString;
                }
                else {
                    kbString = desktopController.SHORTCUT + "-" + keyString;
                }
                boolean stillListening = false;
                int conflictIndex = 0;
                for (Node label : keyBindingsPane.getChildren()) {
                    if (((Label) label).getText().equals(kbString)
                            && !((Label) label).equals(currLabel)) {

                        String conflictMenuItem = ((Label) menuItemsPane.getChildren()
                                .get(
                                        keyBindingsPane.getChildren().indexOf(label))).getText();
                        Optional<ButtonType> result = Dialogs.createConfirmationDialog(
                                keyBindingsPane.getScene().getWindow(),
                                "Key Binding Conflict",
                                "The key binding of the menu item '" +
                                        conflictMenuItem
                                        + "' already has the key binding '" + kbString
                                        + "'.  Would you"
                                        + "like to reassign the key binding of '" +
                                        conflictMenuItem + "'?").showAndWait();

                        if (result.get() == ButtonType.OK) {
                            keyBindings.set(keyBindingsPane.getChildren().indexOf
                                    (label), "          ");
                            currBinding = ((Label) label).getText();
                            stillListening = true;

                        }
                        else if (result.get() == ButtonType.CANCEL) {
                            return;
                        }
                        else {
                            keyBindings.set(keyBindingsPane.getChildren().indexOf
                                    (label), "          ");

                        }
                        break;
                    }
                    conflictIndex++;
                }

                //don't let the user assign key bindings that cannot be changed
                if (kbString.equals("Ctrl-X") || kbString.equals("Ctrl-V") || kbString
                        .equals("Ctrl-A")
                        || kbString.equals("Ctrl-C") || kbString.equals("Ctrl-Z") ||
                        kbString.equals("Ctrl-X")
                        || kbString.equals("Ctrl-Shift-Z") || kbString.equals("Cmd-X")
                        || kbString.equals("Cmd-V") || kbString.equals("Cmd-A")
                        || kbString.equals("Cmd-C") || kbString.equals("Cmd-Z") ||
                        kbString.equals("Cmd-X")
                        || kbString.equals("Cmd-Shift-Z")) {
                    Dialogs.createErrorDialog(keyBindingsPane.getScene().getWindow(),
                            "Reserved Key Binding", "The key binding " + kbString + " cannot be assigned"
                                    + " to a menu item").showAndWait();
                    return;

                }


                keyBindings.set(keyBindingsPane.getChildren().indexOf(currLabel),
                        kbString);
                updateKeyBindingDisplay();
                stopListening();
                if (stillListening) {
                    currLabel = (Label) keyBindingsPane.getChildren().get(conflictIndex);
                    currLabel.setStyle("-fx-background-color:lightblue;" +
                            "-fx-border-style:solid;"
                            + "-fx-border-color:black;");
                    listenForKeyEvents();
                }


            }
        });
    }

    /**
     * sets the key event listener to inactive
     */
    private void stopListening() {
        listening = false;
        keyBindingsPane.getScene().setOnKeyPressed(null);
    }

    public static String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}
