/**
 * File: HelpController
 * Author(Modified Only): Scott Franchi, Pratap Luitel, Stephen Webel
 * Date: 11/12/13
 * <p>
 * Fields added:
 * private searchController
 * Methods modified:
 * public void initializeSearchField()
 * Methods added:
 * private void initializeSearchList(Map<String,Integer> nameOccurrenceMap,
 * Map<String,String> nameFileMap)
 * <p>
 * --------------------------------------
 * <p>
 * Date Modified: 12/6/2013
 * <p>
 * Fields added:
 *
 * @FXML private ComboBox searchModeCB;
 * @FXML private BorderPane helpPane;
 * @FXML private TabPane tabPane;
 * @FXML private Tab searchTab;
 * @FXML private HBox searchBox;
 * @FXML private ListView<String> searchList;
 * <p>
 * <p>
 * Methods modified:
 * public void initialize(URL arg0, ResourceBundle arg1)
 * public HelpController(DesktopController d, String startPage, String as)
 * public void initializeWebView()
 * public void initializeSearchField()
 * Methods added:
 * private void initializeSearchGuiElements()
 * public void initializeSearchChoiceBox()
 */

package cpusim.gui.help;

import cpusim.gui.desktop.DesktopController;
import cpusim.gui.util.FXMLLoaderFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker.State;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/*
 * Michael Goldenberg, Ben Borchard, and Jinghui Yu made the following changes in 12/5/13
 * 
 * 1.) Added the global pseudoinstruction html page to the help dialog by adding it as a 
 * name URL pair
 * 
 */

public class HelpController implements Initializable {

    @FXML
    private TreeView<String> treeView;
    @FXML
    private WebView webView;
    @FXML
    private Button backButton;
    @FXML
    private Button forwardButton;
    @FXML
    private Button closeButton;
    @FXML
    private TextField searchTF;
    @FXML
    private ComboBox searchModeCB;
    @FXML
    private BorderPane helpPane;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab searchTab;
    @FXML
    private HBox searchBox;
    //holds the files that contain the search string
    @FXML
    private ListView<String> searchList;
    //reference to the object responsible for searching
    //and populating the searchList
    private SearchController searchController;
    private final SimpleStringProperty lastViewedFileName;

    public static final String pref = "/html/help";
    public static final String[][] nameURLPairs = {
            {"CPU Sim Help", pref + "/CPUSimHelpBlank.html"},

            // General Help Files
            {"General Help", pref + "/GeneralHelpBlank.html"},
            {"Using Help", pref + "/generalHelp/usingHelp.html"},
            {"Introduction", pref + "/generalHelp/introduction.html"},

            {"A Tour of CPU Sim", pref + "/generalHelp/tour/tutorial.html"},
            {"Part 1: Running CPU Sim", pref + "/generalHelp/tour/runningMachines.html"},
            {"Part 2: Creating New Machines", pref +
                    "/generalHelp/tour/creatingNewMachines.html"},
            {"Part 3: Modifying Existing Machines", pref +
                    "/generalHelp/tour/modifyingMachines.html"},


            // Menus
            {"Menus", pref + "/MenusBlank.html"},
            {"File Menu", pref + "/menus/file.html"},
            {"Edit Menu", pref + "/menus/edit.html"},
            {"Modify Menu", pref + "/menus/modify.html"},
            {"Execute Menu", pref + "/menus/execute.html"},
            {"Help Menu", pref + "/menus/help.html"},

            // Windows
            {"Windows", pref + "/windowsBlank.html"},
            {"Desktop Window", pref + "/windows/mainDisplay.html"},
            {"Machine Instruction Dialog", pref + "/windows/machineInstructions.html"},
            {"Machine Instruction Fields Dialog", pref + "/windows/fields.html"},
            {"Fetch Sequence Dialog", pref + "/windows/fetchSequence.html"},
            {"Hardware Modules Dialog", pref + "/windows/hardwareModules.html"},
            {"Hardware Modules Register Arrays Dialog", pref + "/windows/registerArrays" +
                    ".html"},
            {"Microinstructions Dialog", pref + "/windows/microinstructions.html"},
            {"EQU Editor", pref + "/windows/globalEQUs.html"},
            {"Preferences Dialog", pref + "/windows/preferences.html"},
            {"Options Dialog", pref + "/windows/OptionsDialog.html"},

            // Other Features
            {"Other Features", pref + "/otherFeaturesBlank.html"},
            {"Global EQUs", pref + "/other/globalEqus.html"},
            {"Debug Mode", pref + "/other/debugMode.html"},
            {"Keyboard Shortcuts", pref + "/other/keyboardShortcuts.html"},

            // Machine Specification
            {"Machine Specifications", pref + "/machineSpecificationsBlank.html"},
            {"Names", pref + "/specifications/names.html"},
            {"Fetch Sequence", pref + "/specifications/fetchSequence.html"},
            {"Machine Instructions", pref + "/specifications/machineInstrs.html"},
            {"Fields", pref + "/specifications/fields.html"},
            {"Hardware Modules", pref + "/specifications/hardware.html"},
            {"Microinstructions", pref + "/specifications/microInstrs.html"},
            {"Assembly Language", pref + "/assemblyLanguageBlank.html"},

            {"Registers", pref + "/specifications/hardware/register.html"},
            {"Register Arrays", pref + "/specifications/hardware/registerArray.html"},
            {"RAMs", pref + "/specifications/hardware/ram.html"},
            {"Condition Bits", pref + "/specifications/hardware/conditionBit.html"},

            {"Arithmetic", pref + "/specifications/micros/arithmetic.html"},
            {"Branch", pref + "/specifications/micros/branch.html"},
            {"Decode", pref + "/specifications/micros/decode.html"},
            {"End", pref + "/specifications/micros/end.html"},
            {"Increment", pref + "/specifications/micros/increment.html"},
            {"IO", pref + "/specifications/micros/io.html"},
            {"Logical", pref + "/specifications/micros/logical.html"},
            {"MemoryAccess", pref + "/specifications/micros/memoryAccess.html"},
            {"Set", pref + "/specifications/micros/set.html"},
            {"SetCondBit", pref + "/specifications/micros/setCondBit.html"},
            {"Shift", pref + "/specifications/micros/shift.html"},
            {"Test", pref + "/specifications/micros/test.html"},
            {"Transfer", pref + "/specifications/micros/transfer.html"},

            {"Syntax", pref + "/specifications/assemblyLanguage/syntax.html"},
            {"Regular Instructions", pref +
                    "/specifications/assemblyLanguage/regularInstrs.html"},
            {".data Statements", pref + "/specifications/assemblyLanguage/dataInstrs" +
                    ".html"},
            {".include Statements", pref +
                    "/specifications/assemblyLanguage/includeDirectives.html"},
            {".ascii Statements", pref + "/specifications/assemblyLanguage/asciiInstrs" +
                    ".html"},
            {".global Statements", pref +
                    "/specifications/assemblyLanguage/globalInstrs.html"},
            {"Macros", pref + "/specifications/assemblyLanguage/macroCalls.html"},
            {"EQUs", pref + "/specifications/assemblyLanguage/equDeclaration.html"}};

    private Stack<TreeItem<String>> backStack;
    private Stack<TreeItem<String>> forwardStack;
    private boolean selectionFromButton;
    private MultipleSelectionModel<TreeItem<String>> msm;
    private HashMap<String, String> urls;
    private TreeItem<String> previousItem;

    private String startingPage;
    private String appendString;
    private boolean useAppendString;
    private DesktopController desktop;
    private SimpleStringProperty searchMode;

    //////////////////// Constructor and Initializer ////////////////////

    /**
     * Constructor which only takes desktop controller.
     *
     * @param d - The DestkopController for the current
     *          running application.
     */
    public HelpController(DesktopController d) {
        this(d, null, "");
    }

    /**
     * Constructor which takes in DesktopController
     * and initial starting page ID string.
     *
     * @param d         - The DestkopController for the current
     *                  running application.
     * @param startPage - The ID string for the initial
     *                  page.
     */
    public HelpController(DesktopController d, String startPage) {
        this(d, startPage, "");
    }

    /**
     * Constructor which takes in DesktopController,
     * initial starting page ID string, and anchor string
     * for scrolling purposes.
     *
     * @param d         - The DestkopController for the current
     *                  running application.
     * @param startPage - The ID string for the initial
     *                  page.
     * @param as        - The anchor string, for how far to scroll
     *                  down the HTML page.
     */
    public HelpController(DesktopController d, String startPage, String as) {

        desktop = d;
        startingPage = startPage;
        appendString = as;

        backStack = new Stack<>();
        forwardStack = new Stack<>();
        urls = new HashMap<>();

        //CHANGE
        searchMode = new SimpleStringProperty("Normal");
        searchController = new SearchController(nameURLPairs, searchMode);
        lastViewedFileName = new SimpleStringProperty("");


        // Initialize Map
        for (String[] arr : nameURLPairs) {
            urls.put(arr[1], arr[0]);
            urls.put(arr[0], arr[1]);
        }

        useAppendString = false;
    }

    /**
     * Initialize the help dialog.
     */
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        initializeTreeView();
        initializeWebView();
        initializeButtons();
        initializeSearchGuiElements();

    }

    //////////////////// Button Actions ////////////////////

    /**
     * Called each time the back button is clicked.
     */
    public void onBackButtonClicked() {
        selectionFromButton = true;
        TreeItem<String> item;
        try {
            item = backStack.pop();
        } catch (EmptyStackException ese) {
            // Should never happen
            backButton.setDisable(true);
            return;
        }
        forwardStack.push(previousItem);
        forwardButton.setDisable(false);
        backButton.setDisable(backStack.isEmpty());
        msm.select(item);
    }

    /**
     * Called each time the forward button is clicked.
     */
    public void onForwardButtonClicked() {
        selectionFromButton = true;
        TreeItem<String> item;
        try {
            item = forwardStack.pop();
        } catch (EmptyStackException ese) {
            // Shouldn't ever happen
            forwardButton.setDisable(true);
            return;
        }
        backStack.push(previousItem);
        backButton.setDisable(false);
        forwardButton.setDisable(forwardStack.isEmpty());
        msm.select(item);
    }

    /**
     * Called each time the close button is clicked.
     */
    public void onCloseButtonClicked() {
        ((Stage) (closeButton.getScene().getWindow())).close();
    }

    //////////////////// Initializer Helpers ////////////////////

    /**
     * Initializes the tree view.
     */
    public void initializeTreeView() {

        msm = treeView.selectionModelProperty().getValue();
        TreeItem<String> root = treeView.rootProperty().get();
        if (startingPage == null) {
            previousItem = root.getChildren().get(0).getChildren().get(1);
            msm.select(root.getChildren().get(0).getChildren().get(1));
            lastViewedFileName.set("Introduction");
        }
        else {
            previousItem = getItemFromString(startingPage, root);
            msm.select(previousItem);
        }

        // Seleciton listener
        treeView.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<TreeItem<String>>() {
                    @Override
                    public void changed(ObservableValue<? extends TreeItem<String>>
                                                selected,
                                        TreeItem<String> oldSet, TreeItem<String>
                                                newSet) {

                        // oldSet is null whenever an item that has children is clicked.
                        // We have implemented a work-around here, but this appears to be
                        // a bug in javaFX.

                        if (newSet != null) {
                            lastViewedFileName.set(newSet.getValue());
                            if (urls.containsKey(newSet.getValue())) {
                                if (!selectionFromButton) {
                                    backStack.add(previousItem);
                                    forwardStack.clear();
                                    backButton.setDisable(false);
                                    forwardButton.setDisable(true);
                                }
                                else {
                                    selectionFromButton = false;
                                }

                                WebEngine webEngine = webView.getEngine();
                                URL url = getClass().getResource(urls.get(newSet
                                        .getValue()));
                                if (url != null) {
                                    if (useAppendString) {
                                        webEngine.load(url.toExternalForm() +
                                                appendString);
                                        useAppendString = false;
                                    }
                                    else {
                                        webEngine.load(url.toExternalForm());
                                    }
                                }
                                else {
                                    System.out.println("URL for " + newSet.getValue() +
                                            " is null in HelpController" +
                                            ".InitailizeTreeView().");
                                }
                            }
                            else {
                                WebEngine webEngine = webView.getEngine();
                                webEngine.load(getClass().getResource(pref +
                                        "/CPUSimHelpBlank" +
                                        ".html").toExternalForm());
                            }
                        }
                    }
                });

    }

    /**
     * Initializes the WebView pane, where
     * the help HTML pages are rendered.
     */
    public void initializeWebView() {
        WebEngine webEngine = webView.getEngine();
        webView.setZoom(javafx.stage.Screen.getPrimary().getDpi() / 96);

        webEngine.getLoadWorker().stateProperty().addListener(
                (arg0, oldState, newState) -> {
                    if (newState == State.SUCCEEDED) {
                        WebEngine webEngine1 = webView.getEngine();
                        //This next two lines are a work around
                        //Due to webEngine.load() problems.
                        searchController.setDocumentCopy(webEngine1.getDocument());
                        //loads tags on change. Somewhat of a hack for handling switching
                        //between files when searching is active. Highlights words
                        //immediately when displaying a new file in the web view
                        searchController.addHighlightTagsToBody(webEngine1
                                .getDocument());
                        String s = webEngine1.getLocation();
                        int i = webEngine1.getLocation().indexOf(pref);
                        String afterPref = s.substring(i);
                        if (afterPref.contains("#")) {
                            afterPref = afterPref.substring(0, afterPref.indexOf
                                    ("#"));
                        }
                        String newItemsName = urls.get(afterPref);

                        TreeItem<String> ti = getItemFromString(newItemsName,
                                treeView.getRoot());
                        if (!previousItem.getValue().equals(ti.getValue())) {
                            msm.select(ti);
                        }
                        previousItem = ti;
                    }
                });

        URL url = getClass().getResource("/html/help/generalHelp/introduction.html");
        if (startingPage != null) {
            url = getClass().getResource(urls.get(startingPage));
        }
        webEngine.load(url.toExternalForm() + appendString);
    }

    /**
     * Initializes the buttons.
     */
    public void initializeButtons() {
        backButton.setDisable(true);
        forwardButton.setDisable(true);
    }

    /**
     * initializes GUI elements in the search tab
     */
    private void initializeSearchGuiElements() {
        initializeSearchField();
        initializeSearchChoiceBox();
        helpPane.setOnKeyTyped(t -> {
            if (t.isMetaDown() && t.getCharacter().equals("f")) {
                tabPane.getSelectionModel().select(searchTab);
                searchTF.requestFocus();
            }
        });
    }

    /**
     * Initializes the search field.
     */
    public void initializeSearchField() {
//        searchTF.getStylesheets().add(getClass().getResource
//                ("/cpusim/gui/help/SearchComboBox.cpusim.gui.css").toExternalForm());
        searchTF.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                performSearchFor(searchTF.getText());
                if (event.getCode().equals(KeyCode.ENTER)) {
                    // display the file with the most occurrences of the text
                    searchList.focusModelProperty().get().focus(0);
                    String firstLine = searchList.getFocusModel()
                            .focusedItemProperty().getValue();
                    if (firstLine != null) {
                        String fileName = firstLine.split(" ", 2)[1];
                        webView.getEngine().load(getClass().getResource(urls.get
                                (fileName)).toExternalForm());
                        lastViewedFileName.set(fileName);
                    }
                }
            }
        });
    }

    /**
     * search all pages for the given text and fill in the results in the searchList
     * @param text the String to search for
     */
    public void performSearchFor(String text) {
        WebEngine webEngine = webView.getEngine();
        searchController.initializeSearch(webEngine.getDocument(),text);
        Map<String, Integer> occurrenceMap =
                searchController.getSortedNameOccurrenceMap();
        fillInSearchList(occurrenceMap);
    }


    /**
     * fills the searchList (a ListView object) with the file names containing the
     * searched string.
     * File names appear in a descending order corresponding to the number of
     * occurrences of the searched string in the file
     *
     * @param nameOccurrenceMap contains the file name and number of occurrences
     *                          of the searchedString in the file
     */
    private void fillInSearchList(Map<String, Integer> nameOccurrenceMap) {
        ObservableList<String> names =
                FXCollections.observableArrayList(nameOccurrenceMap.keySet());
        searchList.setItems(names);
        searchList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                String fileNameNoOccurrences = searchList.getFocusModel()
                        .focusedItemProperty().getValue().split(" ", 2)[1];
                WebEngine webEngine = webView.getEngine();
                webEngine.load(getClass().getResource(urls.get(fileNameNoOccurrences))
                        .toExternalForm());
                lastViewedFileName.set(fileNameNoOccurrences);
            }
        });
    }

    /**
     * Initializes the search choice box.
     */
    public void initializeSearchChoiceBox() {
        searchModeCB.getSelectionModel().selectedIndexProperty().addListener((ov, t, t1) -> {
            searchMode.set((String) searchModeCB.getItems().get((Integer) t1));
            searchTF.requestFocus();
            if(! searchTF.getText().equals(""))
                // do a new search immediately based on the new search mode
                performSearchFor(searchTF.getText());
        });
//        searchBox.getStylesheets().add(getClass().getResource
//                ("/cpusim/gui/help/SearchComboBox.cpusim.gui.css").toExternalForm());
    }

    /**
     * Selects the tree item designated by the string.
     *
     * @param treeItemString The tree item string. This must
     *                       match exactly to the name of one of the tree items.
     *                       If it doesn't, nothing happens.
     */
    public void selectTreeItem(String treeItemString) {
        TreeItem<String> ti = getItemFromString(treeItemString, treeView.getRoot());
        if (ti != null) {
            msm.select(ti);
        }
        else {
            // should never happen
            System.out.println("No Tree Item by the name: " + treeItemString
                    + " in HelpController.selectTreeItem(String)");
        }
    }

    /**
     * Selects the tree item designated by the string,
     * and scrolls to the appropriate location designated
     * by the appendString.
     *
     * @param treeItemString The tree item string. This must
     *                       match exactly to the name of one of the tree items.
     *                       If it doesn't, nothing happens.
     * @param appendString   The id of the location to scroll to.
     */
    public void selectTreeItem(String treeItemString, String appendString) {
        useAppendString = true;
        this.appendString = appendString;
        TreeItem<String> ti = getItemFromString(treeItemString, treeView.getRoot());
        if (ti != null) {
            msm.select(ti);
        }
        else {
            // should never happen
            System.out.println("No Tree Item by the name: " + treeItemString
                    + " in HelpController.selectTreeItem(String,String)");
        }
    }

    /**
     * Gives the stage of the help dialog.
     *
     * @return the stage of the help dialog.
     */
    public Stage getStage() {
        return ((Stage) (closeButton.getScene().getWindow()));
    }

    /**
     * Recursive method to give a node of the tree from just
     * the name of it's value.
     *
     * @param s    The name of the tree item desired. Name must
     *             be valid, returns null if not valid.
     * @param item The TreeItem of which all children should
     *             be checked to see if their name matches.
     * @return The node of the tree, if there is one, that
     * contains a value which is the same as the specified string.
     */
    private TreeItem<String> getItemFromString(String s, TreeItem<String> item) {
        if (item != null && s != null) {
            if (item.isLeaf()) {
                return item.getValue().equals(s) ? item : null;
            }
            if (item.getValue().equals(s)) {
                return item;
            }
            for (TreeItem<String> next : item.getChildren()) {
                TreeItem<String> candidate = getItemFromString(s, next);
                if (candidate != null) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * Opens a new help dialog and returns its HelpController.
     *
     * @param d - The desktop controller of the current application.
     * @return - The HelpController of the new Help Dialog.
     */
    public static HelpController openHelpDialog(DesktopController d) {
        return openHelpDialog(d, null, "");
    }

    /**
     * Opens a new help dialog and returns its HelpController.
     *
     * @param d             - The desktop controller of the current application.
     * @param initialWindow - The exact ID string of the window to open initially.
     * @return - The HelpController of the new Help Dialog.
     */
    public static HelpController openHelpDialog(DesktopController d, String initialWindow) {
        return openHelpDialog(d, initialWindow, "");
    }

    /**
     * Opens a new help dialog and returns its HelpController.
     *
     * @param d             - The desktop controller of the current application.
     * @param initialWindow - The exact ID string of the window to open initially.
     * @param appendString  - The id of the location to scroll to.
     * @return - The HelpController of the new Help Dialog.
     */
    public static HelpController openHelpDialog(DesktopController d, String initialWindow,
                                                String appendString) {
        final HelpController helpController = new HelpController(d, initialWindow, appendString);
        FXMLLoader fxmlLoader = FXMLLoaderFactory.fromController(helpController, "HelpFXML.fxml");
        final Stage dialogStage = new Stage();

        // Load in image
        URL url = HelpController.class.getResource("/images/icons/cpusim_icon.jpg");
        Image icon = new Image(url.toExternalForm());
        dialogStage.getIcons().add(icon);

        Pane dialogRoot = null;
        try {
            dialogRoot = fxmlLoader.load();
        } catch (IOException e) {
            // should never happen
            assert false : "Unable to load file: HelpFXML.fxml";
        }

        Scene dialogScene = new Scene(dialogRoot);
        dialogStage.setScene(dialogScene);
        dialogStage.initModality(Modality.NONE);
        dialogStage.setTitle("Help");
        dialogStage.show();

        dialogStage.setOnHidden(arg0 -> helpController.desktop.setHelpController(null));

        dialogStage.addEventFilter(
                KeyEvent.KEY_RELEASED, event -> {
                    if (event.getCode().equals(KeyCode.ESCAPE)) {
                        if (dialogStage.isFocused()) {
                            dialogStage.close();
                        }
                    }
                });

        return helpController;
    }
}