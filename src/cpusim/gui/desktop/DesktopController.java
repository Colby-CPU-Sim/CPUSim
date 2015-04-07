/**
 * File: DesktopController
 * @author: Ben Borchard
 * Modified: 6/4/13
 */

/**
 * File: DesktopController
 * Author: Pratap Luitel, Scott Franchi, Stephen Webel
 * Date: 10/27/13
 *
 * Fields removed:
 *      private SimpleBooleanProperty machineDirty
 *      private File machineFile
 *      private SimpleStringProperty machineDirtyString
 *      private String currentMachineDirectory
 *
 * Methods added:
 *      public ArrayDeque<String> getReopenMachineFiles()
 *      public void setReopenMachineFiles()
 *      public ConsoleManager getConsoleManager()
 *
 * Methods removed:
 *      public void machineChanged()
 *      public SimpleStringProperty getMachineDirtyProperty()
 *      private void addMachineStateListeners()
 *      private void saveMachine()
 *      private void saveAsMachine()
 *      public void newMachine()
 *      public void openMachine(File fileToOpen)
 *
 * Methods modified:
 *      protected void handleNewMachine(ActionEvent event)
 *      protected void handleOpenMachine(ActionEvent event)
 *      public void updateReopenMachineMenu()
 *      public void updateReopenMachineFiles()
 *      private boolean confirmClosing()
 *      public void clearTables()
 *      public void loadPreferences()
 *      public void storePreferences()
 *      protected void handleSaveMachine(ActionEvent event)
 *      protected void handleSaveAsMachine(ActionEvent event)
 *      public void initFileChooser(FileChooser fileChooser, String title, boolean text)
 *
 */

/**
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 11/7/13
 * with the following changes:
 *
 * 1.) added capability for the register and ram tables to handle the Unsigned Decimal 
 * and Ascii bases except in the ram address column
 *
 * on 11/25:
 *
 * 1.) Changed saveAsHTMLMachine() method so that the fileChooser dialog has the .html
 * file extenstion
 * filter
 * 2.) Changed saveAs() method so that the fileChooser dialog has the .a file
 * extenstion filter
 * extension
 *
 */
package cpusim.gui.desktop;

import cpusim.*;
import cpusim.assembler.Token;
import cpusim.gui.about.AboutController;
import cpusim.gui.desktop.editorpane.*;
import cpusim.gui.editmachineinstruction.EditMachineInstructionController;
import cpusim.gui.editmicroinstruction.EditMicroinstructionsController;
import cpusim.gui.editmodules.EditModulesController;
import cpusim.gui.equs.EQUsController;
import cpusim.gui.fetchsequence.EditFetchSequenceController;
import cpusim.gui.find.FindReplaceController;
import cpusim.gui.help.HelpController;
import cpusim.gui.options.OptionsController;
import cpusim.gui.preferences.PreferencesController;
import cpusim.microinstruction.IO;
import cpusim.module.RAM;
import cpusim.module.Register;
import cpusim.module.RegisterArray;
import cpusim.util.*;
import cpusim.xml.MachineHTMLWriter;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.print.JobSettings;
import javafx.print.PageLayout;
import javafx.print.PageRange;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualFlow;
import org.fxmisc.richtext.*;
import org.fxmisc.wellbehaved.event.EventHandlerHelper;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.prefs.Preferences;

import static javafx.scene.input.KeyCode.TAB;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;

/**
 * @author Ben Borchard
 */
public class DesktopController implements Initializable {

    public static final String SHORTCUT = System.getProperty("os.name").startsWith
            ("Windows") ? "Ctrl" : "Cmd";
    public static final String[][] DEFAULT_KEY_BINDINGS = {
            /* quit, undo, redo, cut, copy, paste, delete, select all are not editable */
            {"New text", SHORTCUT + "-N"},
            {"Open text...", SHORTCUT + "-O"},
            {"Close text", SHORTCUT + "-W"},
            {"Save text", SHORTCUT + "-S"},
            {"Save text as...", SHORTCUT + "-Shift-S"},
            {"New machine", SHORTCUT + "-Shift-N"},
            {"Open machine...", SHORTCUT + "-Shift-O"},
            {"Save machine", SHORTCUT + "-B"},
            {"Save machine as...", SHORTCUT + "-Shift-B"},
            {"Save machine in HTML...", SHORTCUT + "-Alt-B"},
            {"Print setup...", SHORTCUT + "-Shift-P"},
            {"Print...", SHORTCUT + "-P"},
            {"Toggle Comment", SHORTCUT + "-Slash"},
            {"Find...", SHORTCUT + "-F"},
            {"Preferences...", SHORTCUT + "-Comma"},
            {"Machine instructions...", SHORTCUT + "-M"},
            {"Microinstructions...", SHORTCUT + "-Shift-M"},
            {"Hardware Modules...", SHORTCUT + "-K"},
            {"EQUs...", SHORTCUT + "-E"},
            {"Fetch Sequence...", SHORTCUT + "-Y"},
            {"Debug Mode", SHORTCUT + "-D"},
            {"Assemble", SHORTCUT + "-1"},
            {"Assemble & load", SHORTCUT + "-2"},
            {"Assemble, load & run", SHORTCUT + "-3"},
            {"Clear, assemble, load & run", SHORTCUT + "-G"},
            {"Run", SHORTCUT + "-R"},
            {"Stop", SHORTCUT + "-Period"},
            {"Reset everything", SHORTCUT + "-Shift-R"},
            {"Clear console", SHORTCUT + "-L"},
            {"Options...", SHORTCUT + "-I"},
            {"General CPUSim Help", SHORTCUT + "-Shift-H"},
            {"About CPUSim", SHORTCUT + "-Shift-A"}
    };
    // System.getProperty("line.separator") doesn't work
    // on PCs. TextArea class may just use "\n".
    static final String NEWLINE = "\n";
    private final ButtonType buttonTypeYes = new ButtonType("Yes");
    private final ButtonType buttonTypeNo = new ButtonType("No");
    private final ButtonType buttonTypeCancel = new ButtonType("Cancel",
            ButtonBar.ButtonData.CANCEL_CLOSE);
    @FXML
    protected MenuBar menuBar;
    @FXML
    protected Menu fileMenu;
    @FXML
    protected Menu editMenu;
    @FXML
    protected Menu modifyMenu;
    @FXML
    protected Menu executeMenu;
    @FXML
    protected Menu helpMenu;
    @FXML
    private VBox mainPane;
    @FXML
    private TabPane textTabPane;
    @FXML
    private ChoiceBox<String> registerDataDisplayCB;
    @FXML
    private ChoiceBox<String> ramAddressDisplayCB;
    @FXML
    private ChoiceBox<String> ramDataDisplayCB;
    @FXML
    private Menu reopenMachineMenu;
    @FXML
    private Menu reopenTextMenu;
    @FXML
    private Menu openRamMenu;
    @FXML
    private Menu saveRamMenu;
    @FXML
    private Label noRAMLabel;
    @FXML
    private VBox ramVbox;
    @FXML
    private VBox regVbox;
    @FXML
    private SplitPane regSplitPane;
    @FXML
    private SplitPane ramSplitPane;
    @FXML
    private ToolBar ramToolBar;
    @FXML
    private StackPane ioConsolePane;
    private StyledTextArea ioConsole;
    private String currentTextDirectory;
    private PrinterJob currentPrinterJob;
    private FontData assmFontData;
    private FontData registerTableFontData;
    private FontData ramTableFontData;
    private OtherSettings otherSettings;
    private ArrayDeque<String> reopenTextFiles;
    private ArrayDeque<String> reopenMachineFiles;
    private ObservableList<RamTableController> ramControllers;
    private ObservableList<RegisterTableController> registerControllers;
    private DebugToolBarController debugToolBarController;
    private CodePaneController codePaneController;
    private MachineHTMLWriter htmlWriter; //HTML descriptions of the machine
    private Stage stage;
    private Map<String, KeyCodeInfo> keyBindings; //key=menu name, value=keyboard shortcut
    private Mediator mediator;
    private String regDataBase;
    private String ramAddressBase;
    private String ramDataBase;
    private HighlightManager highlightManager;
    private UpdateDisplayManager updateDisplayManager;
    private ConsoleManager consoleManager;
    private SimpleBooleanProperty inDebugMode;
    private SimpleBooleanProperty inRunningMode;
    private SimpleBooleanProperty inDebugOrRunningMode;
    private SimpleBooleanProperty noTabSelected;
    private SimpleBooleanProperty anchorEqualsCarret;
    private SimpleBooleanProperty codeStoreIsNull;
    private HelpController helpController;
    private FindReplaceController findReplaceController;

    /**
     * constructor method that takes in a mediator and a stage
     *
     * @param mediator handles communication between the modules, assembler etc
     *                 and the desktop controller
     * @param stage    the stage used to display the desktop
     */
    public DesktopController(Mediator mediator, Stage stage) {
        mediator.setDesktopController(this);
        this.stage = stage;
        this.mediator = mediator;
        this.ioConsole = new CodeArea();

        highlightManager = new HighlightManager(mediator, this);
        updateDisplayManager = new UpdateDisplayManager(mediator, this);
        debugToolBarController = new DebugToolBarController(mediator, this);
        codePaneController = new CodePaneController(mediator);
    }

    /**
     * Initializes the desktop controller field
     *
     * @param url unused
     * @param rb  unused
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // prep the menu bar for Mac apps
        menuBar.setUseSystemMenuBar(true);

        // add the ioConsole to the ioConsolePane
        ioConsolePane.getChildren().add(ioConsole);

        // create the consoleManager
        consoleManager = new ConsoleManager(ioConsole);

        //initialize the list of controllers
        ramControllers = FXCollections.observableArrayList();
        registerControllers = FXCollections.observableArrayList();

        //initialize the html writer
        htmlWriter = new MachineHTMLWriter();

        //add listener for the stage for closing
        stage.setOnCloseRequest(t -> {
            boolean close = confirmClosing();
            if (close) {
                if (helpController != null) {
                    helpController.getStage().close();
                }
                if (findReplaceController != null) {
                    findReplaceController.getStage().close();
                }
                storePreferences();
            } else {
                t.consume();
            }

        });

        //set up reopen queues
        reopenTextFiles = new ArrayDeque<>();
        reopenMachineFiles = new ArrayDeque<>();

        //initialize preferences data
        registerTableFontData = new FontData();
        ramTableFontData = new FontData();
        assmFontData = new FontData();
        otherSettings = new OtherSettings();

        //init key bindings
        keyBindings = new LinkedHashMap<>(); //to preserve iteration order

        //find the screen width
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();

        //fit main pane to the screen (roughly)
        if (mainPane.getPrefWidth() > screenWidth) {
            mainPane.setPrefWidth(screenWidth - 75);
        }

        if (mainPane.getPrefHeight() > screenHeight) {
            mainPane.setPrefHeight(screenHeight - 40);
        }

        //load preferences
        loadPreferences();

        //initialize key bindings between the menu item accelerators and the key codes
        bindKeys();

        //initialize the values of the choice boxes
        registerDataDisplayCB.setValue(regDataBase);
        ramAddressDisplayCB.setValue(ramAddressBase);
        ramDataDisplayCB.setValue(ramDataBase);

        //add listeners to the choice button
        addBaseChangeListener(registerDataDisplayCB, "registerData");
        addBaseChangeListener(ramAddressDisplayCB, "ramAddress");
        addBaseChangeListener(ramDataDisplayCB, "ramData");

        // For disabling/enabling
        noTabSelected = new SimpleBooleanProperty();
        textTabPane.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Tab>() {
                    @Override
                    public void changed(ObservableValue<? extends Tab> arg0, Tab
                            oldTab, Tab newTab) {
                        noTabSelected.set(newTab == null);
                        if (newTab != null) {
                            final Node node = newTab.getContent();
                            Platform.runLater(() -> node.requestFocus());
                        }
                    }
                });

        // initialize simpleBooleanProperties and disables
        inDebugMode = new SimpleBooleanProperty(false);
        inRunningMode = new SimpleBooleanProperty(false);
        inDebugOrRunningMode = new SimpleBooleanProperty(false);
        inDebugOrRunningMode.bind(inDebugMode.or(inRunningMode));
        anchorEqualsCarret = new SimpleBooleanProperty(false);
        codeStoreIsNull = new SimpleBooleanProperty(true);
        bindItemDisablesToSimpleBooleanProperties();

        // Set up channels
        ((DialogChannel) (((BufferedChannel) (CPUSimConstants.DIALOG_CHANNEL))
                .getChannel())).setStage(stage);
        ((ConsoleChannel) (((BufferedChannel) (CPUSimConstants.CONSOLE_CHANNEL))
                .getChannel())).setMediator(mediator);

        // whenever a new tab in the code text area is selected,
        // set the line numbers and line wrap and style according to the settings
        this.textTabPane.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldTab, newTab) -> {
                    if (newTab == null) return; // there are no tabs left
                    StyledTextArea codeArea = (StyledTextArea) newTab.getContent();
                    codeArea.setWrapText(otherSettings.lineWrap.get());
                    LineNumAndBreakpointFactory lFactory =
                            (LineNumAndBreakpointFactory) codeArea
                                    .getParagraphGraphicFactory();
                    if (otherSettings.showLineNumbers.get()) {
                        lFactory.setFormat(digits -> "%" + digits + "d");
                    }
                    else {
                        lFactory.setFormat(digits -> "");
                    }
                    refreshTopTabPane();
                });
    }

    //================ handlers for FILE menu ==================================

    /**
     * Adds a new untitled empty tab to the text tab pane,
     * with new title.
     *
     * @param event action event that is unused
     */
    @FXML
    protected void handleNewText(ActionEvent event) {
        ObservableList<Tab> tabs = textTabPane.getTabs();
        ArrayList<String> titles = new ArrayList<>();
        for (Tab tab : tabs) {
            titles.add(tab.getText().trim());
        }

        String s = "Untitled";
        int i = 0;
        while (titles.contains(s)) {
            i++;
            s = "Untitled " + i;
        }
        addTab("", s, null);
    }

    /**
     * Opens user specified text from computer memory.
     *
     * @param event action event that is unused
     */
    @FXML
    protected void handleOpenText(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
                /*fileChooser.getExtensionFilters().add(new ExtensionFilter("Assembly
                Language File (.a)",
                        "*.a"));*/
        initFileChooser(fileChooser, "Open Text", true);

        File fileToOpen = fileChooser.showOpenDialog(stage);
        if (fileToOpen == null) {
            return;
        }

        open(fileToOpen);
    }

    /**
     * Nothing needs to be done here.
     */
    @FXML
    protected void handleReopenText(ActionEvent event) {
    }

    /**
     * Closes whichever tab is selected.
     *
     * @param event unused action event
     */
    @FXML
    protected void handleCloseText(ActionEvent event) {
        Tab tab = textTabPane.getSelectionModel().getSelectedItem();
        closeTab(tab, true);
    }

    /**
     * Saves whichever tag is selected.
     * opens a file chooser if there is not already
     * a file associated with the tab.
     *
     * @param event unused action event
     */
    @FXML
    protected void handleSaveText(ActionEvent event) {
        Tab tab = textTabPane.getSelectionModel().getSelectedItem();
        save(tab);
    }

    /**
     * Allows the user to specify a file name and directory
     * to save the selected tab in.
     *
     * @param event unused action event
     */
    @FXML
    protected void handleSaveAsText(ActionEvent event) {
        Tab selectedTab = textTabPane.getSelectionModel().getSelectedItem();
        saveAs(selectedTab);
    }

    /**
     * Creates and opens a new machine.
     *
     * @param event unused action event
     */
    @FXML
    protected void handleNewMachine(ActionEvent event) {
        if (mediator.isMachineDirty()) {
            Alert dialog = Dialogs.createCustomizedConfirmationDialog(stage, "Save Machine",
                    "The machine you are currently working on is unsaved.  " +
                            "Would you like to save it before you open a new machine?");
            dialog.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.get() == buttonTypeYes) {
                handleSaveMachine(event);
            } else if (result.get() == buttonTypeCancel) {
                return;
            }
        }

        Machine machine = new Machine("New");

        mediator.setMachine(machine);
        mediator.setMachineFile(null);
        mediator.setMachineDirty(true);
        clearTables();
        setUpTables();
    }

    /**
     * opens a machine from a file
     * if the current machine is not saved, it will confirm the opening of a new
     * machine
     *
     * @param event unused
     */
    @FXML
    protected void handleOpenMachine(ActionEvent event) {
        if (mediator.isMachineDirty()) {

            Alert dialog = Dialogs.createCustomizedConfirmationDialog(stage, "Save Machine",
                    "The machine you are currently working on is unsaved.  " +
                            "Would you like to save it before you open a new machine?");
            dialog.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.get() == buttonTypeYes) {
                handleSaveMachine(event);
            } else if (result.get() == buttonTypeCancel) {
                return;
            }
        }

        FileChooser fileChooser = new FileChooser();
                /*fileChooser.getExtensionFilters().add(new ExtensionFilter("Machine
                File (.cpu)",
                        "*.cpu"));*/

        initFileChooser(fileChooser, "Open Machine", false);


        //in the case that the tab is already open for the
        File fileToOpen = fileChooser.showOpenDialog(stage);
        if (fileToOpen == null) {
            return;
        }

        mediator.openMachine(fileToOpen);

    }

    /**
     * Nothing needs to be done here.
     *
     * @param event unused
     */
    @FXML
    protected void handleReopenMachine(ActionEvent event) {
    }

    /**
     * Saves the current Machine.
     *
     * @param event unused
     */
    @FXML
    protected void handleSaveMachine(ActionEvent event) {
        mediator.saveMachine();
    }

    /**
     * Save As for current machine.
     *
     * @param event unused
     */
    @FXML
    protected void handleSaveAsMachine(ActionEvent event) {
        mediator.saveAsMachine();
    }

    /**
     * Saves the Machine as HTML.
     *
     * @param event unused
     */
    @FXML
    protected void handleSaveAsHTMLMachine(ActionEvent event) throws
            FileNotFoundException {
        FileChooser fileChooser = new FileChooser();
        initFileChooser(fileChooser, "Save Machine", false);
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Hyper Text Markup " +
                "Language file (.html)",
                "*.html"));
        fileChooser.setInitialFileName(mediator.getMachine().getName());

        File fileToSave = fileChooser.showSaveDialog(stage);
        if (fileToSave == null) {
            return;
        }

        PrintWriter printWriter = new PrintWriter(fileToSave);

        htmlWriter.writeMachineInHTML(mediator.getMachine(), printWriter);
    }

    /**
     * opens the page setup dialog to set the printer options for the current job
     *
     * @param event unused
     */
    @FXML
    protected void handlePrintSetup(ActionEvent event) {

        if(currentPrinterJob == null)
            currentPrinterJob = PrinterJob.createPrinterJob();
        currentPrinterJob.showPageSetupDialog(stage);
    }

    /**
     * prints all the code from the selected tab of assembly programs
     *
     * @param event the ignored Event (selection of Print from File menu)
     */
    @FXML
    protected void handlePrint(ActionEvent event) {
        // the current job may have been set in the page setup dialog
        if (currentPrinterJob == null)
            currentPrinterJob = PrinterJob.createPrinterJob();
        boolean print = currentPrinterJob.showPrintDialog(stage);
        if (print) {
            Node nodeToBePrinted = textTabPane.getSelectionModel().getSelectedItem().getContent();

            // break the node into pages and print them
            final List<Node> pages =
                    getPagesForPrinting((InlineStyleTextArea<StyleInfo>) nodeToBePrinted);
            PageRange[] ranges = currentPrinterJob.getJobSettings().getPageRanges();
            if( ranges != null && ranges.length > 0)
                for(PageRange range : ranges) {
                    for(int i = range.getStartPage(); i <= range.getEndPage(); i++)
                        currentPrinterJob.printPage(pages.get(i));
                }
            else
                pages.forEach(currentPrinterJob::printPage);
            currentPrinterJob.endJob();
            currentPrinterJob = null;
        }

        /*
        OLD VERSION that just prints one page.
        if( currentPrinterJob == null)
            currentPrinterJob = PrinterJob.createPrinterJob();
        if (currentPrinterJob != null) {
            // show the print dialog
            boolean ok = currentPrinterJob.showPrintDialog(stage);
            // if it wasn't cancelled then print the current text area
            if (ok) {
                currentPrinterJob.getJobSettings().setPageRanges(new PageRange(1, 5));
                Node nodeToBePrinted =
                        textTabPane.getSelectionModel().getSelectedItem().getContent();

                // Now do the actual printing
                boolean success = currentPrinterJob.printPage(nodeToBePrinted);
                if (success ) {
                    currentPrinterJob.endJob();
                }
            }
        }
        currentPrinterJob = null;
        */

        /*
            // Sample code that scales the node to be printed, based on the standard
            //                letter size in portrait mode.
            Printer printer = Printer.getDefaultPrinter();
            PageLayout pageLayout = printer.createPageLayout(Paper.NA_LETTER,
                                   PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);
            double scaleX = pageLayout.getPrintableWidth() /
                            node.getBoundsInParent().getWidth();
            double scaleY = pageLayout.getPrintableHeight() /
                            node.getBoundsInParent().getHeight();
            node.getTransforms().add(new Scale(scaleX, scaleY));
       */
    }

    /**
     * constructs a list of pages to be printed
     *
     * @param nodeToBePrinted the StyledTextArea that has all the data to be printed
     * @return a List of the StyledTextAreas each constituting one page to be printed.
     */
    private List<Node> getPagesForPrinting(InlineStyleTextArea<StyleInfo> nodeToBePrinted) {
        PageLayout layout = currentPrinterJob.getJobSettings().getPageLayout();
        LinkedList<Node> result = new LinkedList<>();
        double scale = layout.getPrintableWidth() /
                                        nodeToBePrinted.getBoundsInParent().getWidth();
        if(scale > 1)  scale = 1;
        double lineHeight = scale * computeParagraphHeight(nodeToBePrinted);
        double printableHeight = layout.getPrintableHeight();
        // HACK:  the next line subtracts one because computeParagraphHeight() returns
        //        a little less than it should.  Subtracting 1 seems to fix it.
        int numLinesPerPage = (int) Math.floor(printableHeight / lineHeight)-1;
        int lineCount = Integer.MAX_VALUE;  // the number of lines so far on current page
        int pageCount = 0; // the number of pages so far
        int totalNumLines = nodeToBePrinted.getParagraphs().size();
        InlineStyleTextArea<StyleInfo> page = null;
        for (Paragraph<StyleInfo> p : nodeToBePrinted.getParagraphs()) {
            if (lineCount >= numLinesPerPage) {
                // start a new page to be filled with lines of text
                page = new InlineStyleTextArea<>(new StyleInfo(), StyleInfo::toCss);
                assmFontData.setFontAndBackground(page);
                page.setWrapText(false); // can't print wrapped text
                page.setParagraphGraphicFactory(LineNumPrintingFactory.get(page,
                        numLinesPerPage * pageCount, totalNumLines,
                        otherSettings.showLineNumbers.get() ?
                                (digits -> "%" + digits + "d") : (digits -> "")));
                final InlineStyleTextArea<StyleInfo> immutablePage = page;
                page.textProperty().addListener((obs, oldText, newText) -> {
                    immutablePage.setStyleSpans(0, codePaneController.computeStyleSpans
                            (newText));
                });
                lineCount = 0;
                pageCount++;
                page.setPrefWidth(layout.getPrintableWidth() / scale);
                page.setPrefHeight(printableHeight / scale);
                page.getTransforms().add(new Scale(scale, scale));
                result.add(page);
            }
            lineCount++;
            if (page != null) // added to stop a compiler warning: possibly uninitialized
                if (lineCount == numLinesPerPage)
                    page.appendText(p.toString()); // skip newline char for the last line
                else
                    page.appendText(p.fullText()); // text plus newline

        }
        return result;
    }

    /**
     * computes the number of points in the text's height, based on the current font
     * and font size for assembly language panes
     *
     * @return the number of points in height of each line
     */
    private double computeParagraphHeight(InlineStyleTextArea<StyleInfo> node)
    {
        VirtualFlow<?, ?> vf = (VirtualFlow<?, ?>) node.lookup(".virtual-flow");
        return vf.visibleCells().get(0).getNode().getLayoutBounds().getHeight();

//        // attempt 1 (failed)
//        Text text = new Text("HELLO");
//        text.setFont(new Font(assmFontData.font,
//                Double.valueOf(assmFontData.fontSize)));
//        TextFlow flow = new TextFlow(text);
//        new Scene(new Group(flow));  // to get it to layout the Text
//        Bounds layoutBounds = flow.getLayoutBounds();
//        Bounds localBounds = flow.getBoundsInLocal();
//        Bounds parentBounds = flow.getBoundsInParent();
//        double layoutHeight = layoutBounds.getHeight();
//        double localHeight = localBounds.getHeight();
//        double parentHeight = parentBounds.getHeight();
//        return parentHeight;

//        // attempt 2 (failed)
//        int numParagraphs = node.getParagraphs().size();
//        double height = node.getHeight();
//        double paragraphHeight = height/numParagraphs;
//        return paragraphHeight;

//        // attempt 3 (failed)
//        Paragraph<StyleInfo> p = node.getParagraph(0);
//        InlineStyleTextArea<StyleInfo> page =
//                new InlineStyleTextArea<>(new StyleInfo(), StyleInfo::toCss);
//        assmFontData.setFontAndBackground(page);
//        page.setWrapText(false); // can't print wrapped text
//        page.setParagraphGraphicFactory(LineNumPrintingFactory.get(page,
//                0, 1, otherSettings.showLineNumbers.get() ?
//                        (digits -> "%" + digits + "d") : (digits -> "")));
//        final InlineStyleTextArea<StyleInfo> immutablePage = page;
//        page.textProperty().addListener((obs, oldText, newText) -> {
//            immutablePage.setStyleSpans(0, codePaneController.computeStyleSpans
//                    (newText));
//        });
//        page.appendText(p.toString()); // skip newline char
//        mainPane.getChildren().add(1, page);
//        double lineHeight = page.getBoundsInParent().getHeight();
//        mainPane.getChildren().remove(1);
//        return lineHeight;
    }

    /**
     * Exits the program
     *
     * @param event the action event causing the quit request
     */
    @FXML
    protected void handleQuit(ActionEvent event) {
        boolean close = confirmClosing();
        if (close) {
            storePreferences();
            if (helpController != null) {
                helpController.getStage().close();
            }
            if (findReplaceController != null) {
                findReplaceController.getStage().close();
            }
            System.exit(0);
        }
    }


    //================= handlers for EDIT menu =================================

    /**
     * add the keyboard shortcuts of some menu items so that they also work
     * properly when focus is on the given codeArea.
     */
    public void addMenuKeyboardShortcuts(InlineStyleTextArea<StyleInfo> codeArea) {
        codeArea.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.Y) && event.isShortcutDown())
            // ctrl-Y opens Fetch sequence dialog
            {
                DesktopController.this.handleFetchSequence(null);
            }
            else if (event.getCode().equals(KeyCode.Z) && event.isShortcutDown() &&
                    event.isShiftDown())
            // ctrl-shift-Z is redo
            {
                handleRedo(null);
            }
            else if (event.getCode().equals(KeyCode.Z) && event.isShortcutDown())
            // ctrl-Z is undo
            {
                handleUndo(null);
            }
        });
    }

    /**
     * Undoes the last thing done in the current
     * text tab.
     */
    @FXML
    protected void handleUndo(ActionEvent event) {
        InlineStyleTextArea codeArea = (InlineStyleTextArea)
                textTabPane.getSelectionModel().getSelectedItem().getContent();
        codeArea.undo();
    }

    /**
     * Re-does the last thing that was un-done in the
     * current text tab.
     */
    @FXML
    protected void handleRedo(ActionEvent event) {
        InlineStyleTextArea codeArea = (InlineStyleTextArea)
                textTabPane.getSelectionModel().getSelectedItem().getContent();
        codeArea.redo();
    }

    /**
     * Cuts selected text in the selected tab
     *
     * @param event unused action event
     */
    @FXML
    protected void handleCut(ActionEvent event) {
        InlineStyleTextArea codeArea = (InlineStyleTextArea)
                textTabPane.getSelectionModel().getSelectedItem().getContent();
        codeArea.cut();
    }

    /**
     * copies selected text in the selected tab
     *
     * @param event unused action event
     */
    @FXML
    protected void handleCopy(ActionEvent event) {
        InlineStyleTextArea codeArea = (InlineStyleTextArea)
                textTabPane.getSelectionModel().getSelectedItem().getContent();
        codeArea.copy();
    }

    /**
     * Pastes text that was cut or copied into the selected tab.
     *
     * @param event unused action event
     */
    @FXML
    protected void handlePaste(ActionEvent event) {
        InlineStyleTextArea codeArea = (InlineStyleTextArea)
                textTabPane.getSelectionModel().getSelectedItem().getContent();
        codeArea.paste();
    }

    /**
     * Selects all the text in the selected tab.
     *
     * @param event unused action event
     */
    @FXML
    protected void handleSelectAll(ActionEvent event) {
        InlineStyleTextArea codeArea = (InlineStyleTextArea)
                textTabPane.getSelectionModel().getSelectedItem().getContent();
        codeArea.selectAll();
    }

    /**
     * Toggles the selected text's lines to be commented
     * or uncommented.
     *
     * @param event unused action event
     */
    @FXML
    protected void handleToggleComment(ActionEvent event) {
        Tab currTab = textTabPane.getSelectionModel().getSelectedItem();
        InlineStyleTextArea codeArea = (InlineStyleTextArea) currTab.getContent();

        int lower = Math.min(codeArea.getCaretPosition(), codeArea.getAnchor());
        int upper = Math.max(codeArea.getCaretPosition(), codeArea.getAnchor());

        String text = codeArea.getText();

        //handle the case if there is a line at the end with nothing in it
        //give it something so that it will have its own line in the array
        if (text.endsWith(NEWLINE)) {
            text = text + " ";
        }

        String[] splitArray = text.split(NEWLINE);

        //undo the change we did before we split the array
        if (text.endsWith(NEWLINE)) {
            splitArray[splitArray.length - 1] = "";
        }


        if (text.length() == 0) {
            return;
        }

        int lineStart = -1;
        int lineEnd = -1;

        //index of the line on which the highlighting begins
        int F = 0;

        //index of the line on which the highlighting ends
        int L = 0;


        //initialize F and L
        for (int i = 0; i < splitArray.length; i++) {
            String line = splitArray[i];
            lineStart = lineEnd + 1;
            lineEnd = lineStart + line.length();

            if (lineStart <= lower && lower <= lineEnd) {
                F = i;
            }
            if (lineStart <= upper && upper <= lineEnd) {
                L = i;
            }
        }

        //get the current comment character
        String commentChar = String.valueOf(mediator.getMachine().getCommentChar());

        //the new text after to be put in the text area after after
        //comment characters are added/removed as appropriate
        String newText = "";

        //if F is not within the bounds of the array then get out of this method
        if (!(0 <= F && F < splitArray.length)) {
            return;
        }

        //determine whether we will be commenting or un-commenting by checking
        //the first character of the place where highlighting begins
        boolean commenting = false;
        for (int i = 0; i < splitArray.length; i++) {
            if (F <= i && i <= L) {
                commenting |= !splitArray[i].startsWith(commentChar);
            }
        }

        int numIncreasedChars = 0;

        //the line with comment appropriately added or removed
        String editedLine;


        for (int i = 0; i < splitArray.length; i++) {
            String origLine = splitArray[i];
            if (F <= i && i <= L) {
                //Modification: changed the content of the if-else loop
                //				to have the toggle work properly
                if (commenting) {
                    editedLine = (commentChar + origLine);
                }
                //do not delete the first character if it isn't the comment
                //character
                else if (origLine.startsWith(commentChar)) {
                    editedLine = (origLine.substring(1));
                } else {
                    editedLine = origLine;
                }
                newText += editedLine;
                numIncreasedChars += editedLine.length() - origLine.length();
            } else {
                newText += origLine;
            }
            //add a newline character unless we are on the last line
            if (i != splitArray.length - 1) {
                newText += NEWLINE;
            }
        }

        // Note that the below implementation is a major hack.
        // We select the text we want to replace, paste the
        // contents in to replace, then return the Clipboard to
        // its original state. This way the actions can be un-done
        // and re-done.
        codeArea.selectAll();
        Clipboard clipboard = Clipboard.getSystemClipboard();

        boolean setBack = true;
        DataFormat df = null;
        Object oldVal = null;
        try {
            df = (DataFormat) (clipboard.getContentTypes().toArray()[0]);
            oldVal = (clipboard.getContent(df));
        } catch (Exception e) {
            setBack = false;
        }

        ClipboardContent content = new ClipboardContent();
        content.putString(newText);
        clipboard.setContent(content);
        codeArea.paste();

        if (setBack) {
            ClipboardContent oldContent = new ClipboardContent();
            oldContent.put(df, oldVal);
            clipboard.setContent(oldContent);
        }

        if (commenting) {
            codeArea.selectRange(lower + 1, upper + numIncreasedChars);
        } else {
            codeArea.selectRange(lower - 1, upper + numIncreasedChars);
        }
    }

    /**
     * Opens the find/replace dialog.
     *
     * @param event unused action event
     */
    @FXML
    protected void handleFind(ActionEvent event) {
        if (findReplaceController == null) {
            findReplaceController = FindReplaceController.openFindReplaceDialog
                    (mediator, ioConsole.isFocused());
        } else {
            findReplaceController.setUseConsole(ioConsole.isFocused());
            findReplaceController.getStage().toFront();
            findReplaceController.getStage().requestFocus();
        }
    }

    /**
     * Opens the preferences dialog.
     *
     * @param event unused action event
     */
    @FXML
    protected void handlePreferences(ActionEvent event) {
        openModalDialog("Preferences", "gui/preferences/Preferences.fxml",
                new PreferencesController(mediator, this));
    }

    //============== handlers for MODIFY menu ==================================

    /**
     * Opens the machine instructions dialog.
     *
     * @param event
     */
    @FXML
    protected void handleMachineInstructions(ActionEvent event) {
        EditMachineInstructionController controller = new
                EditMachineInstructionController(mediator);
        openModalDialog("Edit Machine Instructions",
                "gui/editmachineinstruction/editMachineInstruction.fxml", controller);
    }

    /**
     * Opens the microinstructions dialog.
     *
     * @param event unused action event
     */
    @FXML
    protected void handleMicroinstructions(ActionEvent event) {
        EditMicroinstructionsController controller = new
                EditMicroinstructionsController(mediator);
        openModalDialog("Edit Microinstructions",
                "gui/editmicroinstruction/EditMicroinstructions.fxml", controller);
    }

    /**
     * Opens the hardware modules dialog.
     *
     * @param event unused action event
     */
    @FXML
    protected void handleHardwareModules(ActionEvent event) {
        openHardwareModulesDialog(0);
    }

    /**
     * Opens the global EQUs dialog.
     *
     * @param event unused action event
     */
    @FXML
    protected void handleEQUs(ActionEvent event) {
        openModalDialog("EQUs", "gui/equs/EQUs.fxml",
                new EQUsController(mediator));
    }

    /**
     * Opens the Fetch Sequence dialog.
     *
     * @param event unused action event
     */
    @FXML
    protected void handleFetchSequence(ActionEvent event) {
        EditFetchSequenceController controller = new EditFetchSequenceController
                (mediator);
        openModalDialog("Edit Fetch Sequence",
                "gui/fetchsequence/editFetchSequence.fxml", controller);
    }


    //================= handlers for the EXECUTE menu ==========================

    /**
     * Method called when user clicks "Debug"
     * within the Execute drop-down menu.
     *
     * @param event - unused event.
     */
    @FXML
    protected void handleDebug(ActionEvent event) {
        setInDebugMode(!getInDebugMode());
    }

    /**
     * Method called when user clicks "Assemble"
     * within the Execute drop-down menu.
     *
     * @param event - unused event.
     */
    @FXML
    protected void handleAssemble(ActionEvent event) {
        File currFile = getFileToAssemble();
        if (currFile != null) {
            mediator.Assemble(currFile.getAbsolutePath());
        }
    }

    /**
     * Method called when user clicks "Assemble
     * & Load" from the Execute drop-down menu.
     *
     * @param event - unused event.
     */
    @FXML
    protected void handleAssembleLoad(ActionEvent event) {
        File currFile = getFileToAssemble();
        if (currFile != null) {
            mediator.AssembleLoad(currFile.getAbsolutePath());
        }
    }

    /**
     * Method called when user clicks "Assemble,
     * Load, & Run" from the Execute drop-down menu.
     *
     * @param event - unused event.
     */
    @FXML
    protected void handleAssembleLoadRun(ActionEvent event) {
        File currFile = getFileToAssemble();
        if (currFile != null) {
            mediator.AssembleLoadRun(currFile.getAbsolutePath());
        }
    }

    /**
     * Method called when user clicks "Clear, Assemble,
     * Load, & Run" from the Execute drop-down menu.
     *
     * @param event - unused event.
     */
    @FXML
    protected void handleClearAssembleLoadRun(ActionEvent event) {
        File currFile = getFileToAssemble();
        if (currFile != null) {
            mediator.ClearAssembleLoadRun(currFile.getAbsolutePath());
        }
    }

    /**
     * Runs the current program through the mediator.
     *
     * @param event - unused event.
     */
    @FXML
    protected void handleRun(ActionEvent event) {
        File currFile = getFileToAssemble();
        if (currFile != null) {
            mediator.Run();
        }
    }

    /**
     * Stops the currently running program
     * through the mediator.
     *
     * @param event - unused event.
     */
    @FXML
    protected void handleStop(ActionEvent event) {
        mediator.Stop();
    }

    /**
     * Resets everything, all RAM and RAM arrays.
     * Done through the mediator.
     *
     * @param event - unused event.
     */
    @FXML
    protected void handleResetEverything(ActionEvent event) {
        mediator.ResetEverything();
    }

    /**
     * Resets everything, all RAM and RAM arrays.
     * Done through the mediator.
     *
     * @param event - unused event.
     */
    @FXML
    protected void handleClearConsole(ActionEvent event) {
        ioConsole.clear();
    }

    /**
     * Opens the Options dialog.
     *
     * @param event - unused event.
     */
    @FXML
    protected void handleOptions(ActionEvent event) {
        openOptionsDialog(0);
    }

    //================= handler for HELP menu ==================================

    /**
     * Opens the help dialog.
     *
     * @param event unused action event
     */
    @FXML
    protected void handleGeneralCPUSimHelp(ActionEvent event) {
        if (helpController == null) {
            helpController = HelpController.openHelpDialog(this);
        } else {
            helpController.selectTreeItem("Introduction");
            helpController.getStage().toFront();
        }
    }

    /**
     * Opens the about dialog.
     *
     * @param event unused action event
     */
    @FXML
    protected void handleAboutCPUSim(ActionEvent event) {
        openModalDialog("About CPU Sim", "gui/about/AboutFXML.fxml",
                new AboutController());
    }

    //======================= auxiliary methods ================================

    /**
     * If the user has unsaved content in a tab, this handles
     * asking the user if he would like to save it before closing.
     *
     * @param event unused action event
     */
    @FXML
    protected void handleTabClosed(Event event) {
        Tab closingTab = (Tab) event.getSource();
        closeTab(closingTab, false);
    }

    /**
     * Closes a tab, when close = true. In some cases
     * the closing is already set in progress and we
     * only need do a few other things to keep everything
     * up to date. Use this method with close = false for
     * this occasion.
     *
     * @param tab   - The tab to close.
     * @param close - boolean to indicate whether we
     *              should also close the tab here. If not, it is assumed
     *              it is done elsewhere.
     */
    private void closeTab(Tab tab, boolean close) {
        if (((CodePaneTab) tab).getDirty()) {
            Alert dialog = Dialogs.createCustomizedConfirmationDialog(stage, "Save File",
                    "Would you like to save your work before you close this " +
                            "tab?");
            dialog.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.get() == buttonTypeYes) {
                if (save(tab) && close) {
                    textTabPane.getTabs().remove(tab);
                }
            } else if (result.get() == buttonTypeNo) {
                if (close) {
                    textTabPane.getTabs().remove(tab);
                }
            } else {
                if (!close) {
                    textTabPane.getTabs().add(tab);
                    textTabPane.getSelectionModel().selectLast();
                }
            }
        } else {
            if (close) {
                textTabPane.getTabs().remove(tab);
            }
        }
    }


    /**
     * get the File to be assembled.
     * It is the file associated with the currently-selected tab in the desktop.
     * If there are no tabs, null is returned.  If there is a tab, but there is
     * no associated file or if the tab is dirty, a dialog is opened to save the
     * tab to a file.  If the user cancels, null is returned.
     *
     * @return the File to be assembled.
     */
    public File getFileToAssemble() {
        CodePaneTab currTab = (CodePaneTab) textTabPane.getSelectionModel()
                .getSelectedItem();
        if (currTab.getFile() != null && !currTab.getDirty()) {
            return currTab.getFile();
        } else if (otherSettings.autoSave) {
            boolean savedSuccessfully = save(currTab);
            if (savedSuccessfully) {
                return currTab.getFile();
            }
        } else {  //there is no file or there is a file but the tab is dirty.
            Alert dialog = Dialogs.createConfirmationDialog(stage, "Save File?",
                    "Current Tab is not saved. It needs to be saved"
                            + " before assembly. Save and continue?");
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.get() == ButtonType.OK) {
                boolean savedSuccessfully = save(currTab);
                if (savedSuccessfully) {
                    return currTab.getFile();
                }
            }
        }
        return null;
    }

    /**
     * redraws all tab panes to take into account any preference changes.
     * Does nothing if there are no tab panes.
     */
    public void refreshTopTabPane() {
        CodePaneTab t = (CodePaneTab) textTabPane.getSelectionModel().getSelectedItem();
        if (t == null) {
            return;
        }
        InlineStyleTextArea codeArea = (InlineStyleTextArea) t.getContent();
        assmFontData.setFontAndBackground(codeArea);
        String text = codeArea.getText();
        StyleSpans<StyleInfo> styleSpans = codePaneController.computeStyleSpans(text);
        codeArea.setStyleSpans(0, styleSpans);
        codeArea.moveTo(0);
    }

    /**
     * adds a new tab to the text tab pane
     *
     * @param content the text that is in the file
     * @param title   the title of the file
     * @param file    the file object to be associated with this tab (null for unsaved
     *                files)
     */
    public void addTab(String content, String title, File file) {

        // create the new tab and text area
        CodePaneTab newTab = new CodePaneTab();
        InlineStyleTextArea<StyleInfo> codeArea =
                new InlineStyleTextArea<>(new StyleInfo(), StyleInfo::toCss);
        codeArea.setWrapText(otherSettings.lineWrap.get());
        assmFontData.setFontAndBackground(codeArea);
        codeArea.setParagraphGraphicFactory(LineNumAndBreakpointFactory.get(codeArea,
                otherSettings.showLineNumbers.get() ? (digits -> "%" + digits + "d") :
                        (digits -> "")));
        addMenuKeyboardShortcuts(codeArea);

        // replace tabs with 4 or fewer spaces
        EventHandler<? super KeyEvent> tabHandler = EventHandlerHelper
                .on(keyPressed(TAB)).act(event -> {
                    String spaces = "";
                    int numSpaces = 4 - codeArea.getCaretColumn() % 4;
                    for (int i = 0; i < numSpaces; i++) {
                        spaces += " ";
                    }
                    codeArea.replaceSelection(spaces);
                })
                .create();
        EventHandlerHelper.install(codeArea.onKeyPressedProperty(), tabHandler);

        newTab.setContent(codeArea);

        // whenever the text is changed, recompute the highlighting and set it dirty
        codeArea.textProperty().addListener(obs -> {
            codeArea.setStyleSpans(0, codePaneController.computeStyleSpans(codeArea.getText()));
            newTab.setDirty(true);
        });
        // these next two approaches didn't work quite right
        // codeArea.richChanges().subscribe(change -> {
        //     codeArea.setStyleSpans(0, codePaneController.computeStyleSpans(codeArea.getText()));
        //     newTab.setDirty(true);
        // });
        // codeArea.textProperty().addListener((obs, oldText, newText) -> {
        //     codeArea.setStyleSpans(0, codePaneController.computeStyleSpans(newText));
        //     newTab.setDirty(true);
        // });

        // add the content, set what to do when closed, and set the tooltip
        codeArea.replaceText(0, 0, content);
        newTab.setDirty(false); // not initially dirty
        newTab.setOnClosed(this::handleTabClosed);
        if (file != null) {
            newTab.setTooltip(new Tooltip(file.getAbsolutePath()));
        } else {
            newTab.setTooltip(new Tooltip("File has not been saved."));
        }

        // set the file, title, and context menu
        newTab.setFile(file);
        newTab.setText(title);
        addContextMenu(newTab);

        // add a listener to the codeArea's set of breakpoints
        // so that breakpoints can be added dynamically as the code is being stepped through
        // when in debug mode
        ((LineNumAndBreakpointFactory) codeArea.getParagraphGraphicFactory()).getBreakPoints().
                addListener((SetChangeListener<Paragraph>) change -> {
                    if (newTab.getFile() != null) {
                        boolean set = change.wasAdded();
                        String fileName = newTab.getFile().getAbsolutePath();
                        Paragraph paragraph = set ? change
                                .getElementAdded() : change.getElementRemoved();
                        int line = codeArea.getParagraphs().indexOf(paragraph);
                        SourceLine sourceLine = new SourceLine(line, fileName);
                        mediator.setBreakPointInRAM(sourceLine, set);
                    }
                });

        textTabPane.getTabs().add(newTab);
        textTabPane.getSelectionModel().selectLast();
    }


    public Set<Integer> getAllBreakPointsForFile(String fileName) {
        return ((LineNumAndBreakpointFactory) ((InlineStyleTextArea)
                getTabForFile(new File(fileName)).getContent())
                .getParagraphGraphicFactory()).getAllBreakPointLineNumbers();
    }


    /**
     * creates and adds a context menu for the new Tab
     *
     * @param newTab the Tab that gets the context menu
     */
    private void addContextMenu(CodePaneTab newTab) {
        //set up the context menu
        MenuItem close = new MenuItem("Close");
        close.setOnAction(e -> closeTab(newTab, true));

        MenuItem closeAll = new MenuItem("Close All");
        closeAll.setOnAction(e -> {
            ArrayList<Tab> tabs = new ArrayList<Tab>();
            for (Tab tab : textTabPane.getTabs()) {
                tabs.add(tab);
            }
            for (Tab tab : tabs) {
                closeTab(tab, true);
            }
        });

        MenuItem closeOthers = new MenuItem("Close Others");
        closeOthers.setOnAction(e -> {
            ArrayList<Tab> tabs = new ArrayList<Tab>();
            for (Tab tab : textTabPane.getTabs()) {
                tabs.add(tab);
            }
            for (Tab tab : tabs) {
                if (!tab.equals(newTab)) {
                    closeTab(tab, true);
                }
            }
        });

        MenuItem copyPath = new MenuItem("Copy Path Name");
        copyPath.setOnAction(e -> {
            if (newTab.getFile() != null) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content1 = new ClipboardContent();
                content1.putString(newTab.getFile().getAbsolutePath());
                clipboard.setContent(content1);
            }
        });
        copyPath.disableProperty().bind(
                newTab.tooltipProperty().get().textProperty().isEqualTo("File has not " +
                        "been saved."));

        ContextMenu cm = new ContextMenu();
        cm.getItems().addAll(close, closeAll, closeOthers, copyPath);
        newTab.setContextMenu(cm);
    }

    /**
     * Returns the tab for the given assembly text file.
     * If the file is not already open, it is opened and the new tab is returned.
     *
     * @param file the file contains the assembly program
     * @return the tab
     */
    public Tab getTabForFile(File file) {
        assert file != null : "Null passed as parameter to getTabForFile";
        Optional<Tab> existingTab = textTabPane.getTabs().stream()
                .filter(t -> file.equals(((CodePaneTab) t).getFile()))
                .findFirst();
        if (existingTab.isPresent()) {
            Tab currentTab = existingTab.get();
            textTabPane.getSelectionModel().select(currentTab);
            return currentTab;
        } else {
            open(file);
            return textTabPane.getTabs().get(textTabPane.getTabs().size() - 1);
        }
    }

    /**
     * displays a message listing all the halt bits that are set.
     */
    public void displayHaltBitsThatAreSet() {
        Vector setHaltedBits = mediator.getMachine().haltBitsThatAreSet();
        if (setHaltedBits.size() > 0) {
            String message = "The following halt condition bits are set:  ";
            for (int i = 0; i < setHaltedBits.size(); i++)
                message += setHaltedBits.elementAt(i) + "  ";
            consoleManager.printlnToConsole(message);
        }
    }

    /**
     * returns the stage
     *
     * @return the stage
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * Returns the pane of tabs.
     *
     * @return the pane of tabs.
     */
    public TabPane getTextTabPane() {
        return textTabPane;
    }

    /**
     * Returns the ioConsole TextArea.
     *
     * @return the ioConosle TextArea.
     */
    public StyledTextArea getIOConsole() {
        return ioConsole;
    }

    /**
     * returns the hightlightManager.
     *
     * @return the hightlightManager.
     */
    public HighlightManager getHighlightManager() {
        return this.highlightManager;
    }

    /**
     * returns the updateDisplayManager.
     *
     * @return the updateDisplayManager.
     */
    public UpdateDisplayManager getUpdateDisplayManager() {
        return this.updateDisplayManager;
    }

    /**
     * Getter for the help controller.
     * Null if there is no window open right now.
     *
     * @return The current HelpController.
     */
    public HelpController getHelpController() {
        return helpController;
    }

    /**
     * Sets the current help controller.
     * Done when opening a new help window.
     *
     * @param hc The HelpController reference.
     */
    public void setHelpController(HelpController hc) {
        helpController = hc;
    }

    /**
     * Gives the current FindReplaceController.
     *
     * @return the current FindReplaceController.
     */
    public FindReplaceController getFindReplaceController() {
        return findReplaceController;
    }

    /**
     * Sets the current FindReplaceController.
     *
     * @param frc the current FindReplaceController.
     */
    public void setFindReplaceController(FindReplaceController frc) {
        this.findReplaceController = frc;
    }
//------------------------------------------------------------------------------
//Added methods by Pratap, Scott, and Stephen

    /**
     * Gives the current reopenMachineFiles value.
     *
     * @return the current reopenMachineFiles.
     */
    public ArrayDeque<String> getReopenMachineFiles() {
        return reopenMachineFiles;
    }

    /**
     * Sets the current reopenMachineFiles.
     *
     * @param s the string to set the current reopenMachineFiles to.
     */
    public void setReopenMachineFiles(ArrayDeque<String> s) {
        this.reopenMachineFiles = s;
    }

    /**
     * returns the consoleManager.
     *
     * @return the consoleManager.
     */
    public ConsoleManager getConsoleManager() {
        return this.consoleManager;
    }
//------------------------------------------------------------------------------

    /**
     * Returns the boolean describing whether or not
     * the desktop is currently in debug mode.
     *
     * @return boolean describing whether or not
     * the desktop is currently in debug mode.
     */
    public boolean getInDebugMode() {
        return inDebugMode.get();
    }

    /**
     * Sets the desktop into debug mode
     * if b is true, into regular mode if false.
     *
     * @param inDebug true for debug mode, false for
     *                normal mode
     */
    private void setInDebugMode(boolean inDebug) {
        inDebugMode.set(inDebug);
        if (inDebug) {
            mediator.getMachine().getControlUnit().reset();
            mediator.getMachine().resetAllChannels();

            debugToolBarController.updateDisplay(true, false, mediator);
            //debugToolBarController = new DebugToolBarController(mediator, this);
            mainPane.getChildren().add(1, debugToolBarController);
            debugToolBarController.prefWidthProperty().bind(mainPane.widthProperty());
            debugToolBarController.addButtonAccelerators();
        } else {
            debugToolBarController.removeButtonAccelerators();
            mainPane.getChildren().remove(1);
            debugToolBarController.clearAllOutlines();
            mediator.getBackupManager().flushBackups();
            mediator.getMachine().getControlUnit().setMicroIndex(0);
        }
        RAM codeStore = mediator.getMachine().getCodeStore();
        if (codeStore != null) {
            codeStore.setHaltAtBreaks(inDebug);
        }
        mediator.getBackupManager().setListening(inDebug);
        ((CheckMenuItem) (executeMenu.getItems().get(0))).setSelected(inDebug);
    }


    /**
     * Gives the SimpleBooleanProperty describing
     * whether or not we are in debug mode.
     *
     * @return The SimpleBooleanProperty describing
     * whether or not we are in debug mode.
     */
    public SimpleBooleanProperty inDebugModeProperty() {
        return inDebugMode;
    }

    /**
     * Returns a boolean describing whether
     * or not we are currently in running mode.
     */
    public boolean getInRunningMode() {
        return inRunningMode.get();
    }

    /**
     * Notifies the desktop that the machine is in
     * running mode.
     */
    public void setInRunningMode(boolean irm) {
        inRunningMode.set(irm);
    }

    /**
     * Returns the SimpleBooleanProperty describing whether
     * or not we are currently in running mode.
     */
    public SimpleBooleanProperty inRunningModeProperty() {
        return inRunningMode;
    }

    /**
     * Returns the SimpleBooleanProperty describing whether
     * or not we are currently in running mode or currently
     * in debugging mode.
     */
    public SimpleBooleanProperty inDebugOrRunningModeProperty() {
        return inDebugOrRunningMode;
    }

    /**
     * Binds all the menu items to the appropriate
     * SimpleBooleanProperties so that the are
     * enabled/disabled appropriately.
     */
    public void bindItemDisablesToSimpleBooleanProperties() {

        // File Menu
        fileMenu.setOnMenuValidation(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                for (int i = 7; i < 9; i++) {
                    fileMenu.getItems().get(i).setDisable(ramControllers.isEmpty());
                }
            }
        });
        // Close Text
        fileMenu.getItems().get(3).disableProperty().bind(noTabSelected);
        // Save Text
        fileMenu.getItems().get(4).disableProperty().bind(noTabSelected);
        // Save Text As
        fileMenu.getItems().get(5).disableProperty().bind(noTabSelected);
        // New Machine
        fileMenu.getItems().get(10).disableProperty().bind(inRunningMode);
        // Open Machine
        fileMenu.getItems().get(11).disableProperty().bind(inRunningMode);
        // Reopen Machine
        fileMenu.getItems().get(12).disableProperty().bind(inRunningMode);
        // Print
        fileMenu.getItems().get(19).disableProperty().bind(noTabSelected);


        // Edit Menu
//        editMenu.setOnMenuValidation(event -> {
//            boolean canUndo = false;
//            boolean canRedo = false;
//            boolean ancEqCar = false;
//            if (!noTabSelected.get()) {
//                Tab currTab = textTabPane.getSelectionModel().getSelectedItem();
//                InlineStyleTextArea codeArea = (InlineStyleTextArea) currTab.getContent();
//                Object o = codeArea.getSkin();
//                System.out.println(o.getClass());
//                TextInputControlBehavior<?> behavior =
//                        ((TextInputControlSkin<?, ?>) codeArea.getSkin()).getBehavior();
//                canUndo = behavior.canUndo();
//                canRedo = behavior.canRedo();
//                ancEqCar = (codeArea.getAnchor() == codeArea.getCaretPosition());
//            }
//            canUndoProperty.set(canUndo);
//            canRedoProperty.set(canRedo);
//            anchorEqualsCarret.set(ancEqCar);
//        });
        // Undo
        editMenu.getItems().get(0).disableProperty().bind(noTabSelected);
        // Redo
        editMenu.getItems().get(1).disableProperty().bind(noTabSelected);
        // Cut
        editMenu.getItems().get(3).disableProperty().bind(noTabSelected.or
                (anchorEqualsCarret));
        // Copy
        editMenu.getItems().get(4).disableProperty().bind(noTabSelected.or
                (anchorEqualsCarret));
        // Paste
        editMenu.getItems().get(5).disableProperty().bind(noTabSelected);
        // Select All
        editMenu.getItems().get(6).disableProperty().bind(noTabSelected);
        // Toggle Comment
        editMenu.getItems().get(8).disableProperty().bind(noTabSelected);
        // Find
        editMenu.getItems().get(9).disableProperty().bind(noTabSelected);

        // Modify Menu
        modifyMenu.disableProperty().bind(inDebugOrRunningMode);
        // All sub-items disabled at same time.
        for (int i = 0; i < 5; i++) {
            modifyMenu.getItems().get(i).disableProperty().bind(inDebugOrRunningMode);
        }

        // Execute Menu
        // Debug Mode
        executeMenu.getItems().get(0).disableProperty().bind((inRunningMode.or
                (noTabSelected)).or(
                codeStoreIsNull));
        // Assemble
        executeMenu.getItems().get(2).disableProperty().bind(noTabSelected.or(
                codeStoreIsNull));
        // Assemble & Load
        executeMenu.getItems().get(3).disableProperty().bind(noTabSelected.or(
                codeStoreIsNull));
        // Assemble Load & Run
        executeMenu.getItems().get(4).disableProperty().bind((inDebugOrRunningMode
                .or(noTabSelected)).or(codeStoreIsNull));
        // Clear, assemble, load & run
        executeMenu.getItems().get(5).disableProperty().bind((inDebugOrRunningMode
                .or(noTabSelected)).or(codeStoreIsNull));
        // Run
        executeMenu.getItems().get(6).disableProperty().bind((inDebugOrRunningMode
                .or(noTabSelected)).or(codeStoreIsNull));
        // Stop
        executeMenu.getItems().get(7).disableProperty().bind(inRunningMode.not());
        // Reset Everything
        executeMenu.getItems().get(8).disableProperty().bind(inDebugOrRunningMode);
        // IO Options
        executeMenu.getItems().get(10).disableProperty().bind(inDebugOrRunningMode.or(
                codeStoreIsNull));
        // Update codeStoreIsNull
        executeMenu.setOnMenuValidation(event ->
                codeStoreIsNull.set(mediator.getMachine().getCodeStore() == null));

        // if using the console for IO, then set the console's background to yellow
        // during input and set it to white during output and at the end.
        inRunningMode.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0,
                                Boolean wasDebugMode, Boolean nowDebugMode) {
                ObservableList<Microinstruction> ios = mediator.getMachine().getMicros
                        ("io");
                boolean consoleIsInputOrOutputChannel = false;
                for (Microinstruction micro : ios) {
                    IO io = (IO) micro;
                    if (io.getConnection().equals(CPUSimConstants.CONSOLE_CHANNEL)) {
                        consoleIsInputOrOutputChannel = true;
                    }
                }
                if (consoleIsInputOrOutputChannel) {
                    if (nowDebugMode) {
                        ioConsole.setStyle("-fx-background-color: yellow");
                        ioConsole.requestFocus();
                    } else {
                        ioConsole.setStyle("-fx-background-color: white");
                    }
                }
            }
        });
    }

    /**
     * adds a change listener to a choice box so that it can keep track of which
     * choice is selected and do things cased on that
     *
     * @param choiceBox The ChoiceBox who is getting the listener added
     * @param type      a String indicating the type of TableView that the ChoiceBox
     *                  affects ("registerData", "ramData", "ramAddress")
     */
    public void addBaseChangeListener(ChoiceBox<String> choiceBox, String type) {
        final String finalType = type;
        choiceBox.getSelectionModel().selectedIndexProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue ov, Number value, Number
                            new_value) {
                        if (finalType.equals("registerData")) {
                            if (new_value.equals(0)) {
                                regDataBase = "Dec";
                            } else if (new_value.equals(1)) {
                                regDataBase = "Bin";
                            } else if (new_value.equals(2)) {
                                regDataBase = "Hex";
                            } else if (new_value.equals(3)) {
                                regDataBase = "Unsigned Dec";
                            } else {
                                regDataBase = "Ascii";
                            }
                            for (RegisterTableController registerTableController
                                    : registerControllers) {
                                registerTableController.setDataBase(regDataBase);
                            }
                        } else if (finalType.equals("ramAddress")) {
                            if (new_value.equals(0)) {
                                ramAddressBase = "Dec";
                            } else if (new_value.equals(1)) {
                                ramAddressBase = "Bin";
                            } else {
                                ramAddressBase = "Hex";
                            }
                            for (RamTableController ramTableController
                                    : ramControllers) {
                                ramTableController.setAddressBase(ramAddressBase);
                            }
                        } else {  //type == "ramData"
                            if (new_value.equals(0)) {
                                ramDataBase = "Dec";
                            } else if (new_value.equals(1)) {
                                ramDataBase = "Bin";
                            } else if (new_value.equals(2)) {
                                ramDataBase = "Hex";
                            } else if (new_value.equals(3)) {
                                ramDataBase = "Unsigned Dec";
                            } else {
                                ramDataBase = "Ascii";
                            }
                            for (RamTableController ramTableController
                                    : ramControllers) {
                                ramTableController.setDataBase(ramDataBase);
                            }
                        }
                    }
                });
    }

    /**
     * gets the register controllers in an observable list.
     *
     * @return the observable list of RegisterTableControllers
     */
    public ObservableList<RegisterTableController> getRegisterControllers() {
        return registerControllers;
    }

    /**
     * gets the ram controller in an observable list.
     *
     * @return the observable list of RamTableControllers.
     */
    public ObservableList<RamTableController> getRAMControllers() {
        return ramControllers;
    }

    /**
     * gets the DebugToolbarController.
     *
     * @return the DebugToolbarController.
     */
    public DebugToolBarController getDebugToolBarController() {
        return debugToolBarController;
    }

    /**
     * gets the CodePaneController.
     *
     * @return the CodePaneController.
     */
    public CodePaneController getCodePaneController() {
        return codePaneController;
    }

    /**
     * Opens the hardware modules dialog with the
     * specified selected section index.
     *
     * @param initialSection
     */
    public void openHardwareModulesDialog(int initialSection) {
        if (0 <= initialSection && initialSection <= 3) {
            EditModulesController controller = new EditModulesController(mediator, this);
            openModalDialog("Edit Modules",
                    "gui/editmodules/EditModules.fxml", controller);
            controller.selectSection(initialSection);
        } else {
            openHardwareModulesDialog(0);
        }
    }

    /**
     * Opens the options dialog with the
     * specified selected section index.
     *
     * @param initialSection
     */
    public void openOptionsDialog(int initialSection) {
        if (0 <= initialSection && initialSection <= 3) {
            OptionsController controller = new OptionsController(mediator);
            openModalDialog("Options", "gui/options/OptionsFXML.fxml",
                    controller);
            controller.selectTab(initialSection);
        } else {
            openOptionsDialog(0);
        }
    }

    /**
     * Opens a dialog modal to the main window.
     *
     * @param title    - desired title for the window to be created.
     * @param fxmlPath - path to the fxml file that contains the
     *                 formatting for the window to be opened.
     */
    public void openModalDialog(String title, String fxmlPath) {
        openModalDialog(title, fxmlPath, null);
    }

    /**
     * Opens a dialog modal to the main window.
     *
     * @param title      - desired title for the window to be created.
     * @param fxmlPath   - path to the fxml file that contains the
     *                   formatting for the window to be opened.
     * @param controller - The controller of the FXML.
     */
    public void openModalDialog(String title, String fxmlPath, Object controller) {

        openModalDialog(title, fxmlPath, controller, -1, -1);
    }

    /**
     * Opens a dialog modal to the main window.
     *
     * @param title      - desired title for the window to be created.
     * @param fxmlPath   - path to the fxml file that contains the
     *                   formatting for the window to be opened.
     * @param controller - The controller of the FXML.
     * @param x          - the horizonal distance from the left edge of the
     *                   desktop window to the left edge of the new window.
     * @param y          - the vertical distance from the top edge of the
     *                   desktop window to the top edge of the new window.
     */
    public void openModalDialog(String title, String fxmlPath, Object controller, int
            x, int y) {
        openDialog(title, fxmlPath, controller, x, y, Modality.APPLICATION_MODAL);
    }

    /**
     * Opens a dialog with no modality.
     *
     * @param title    - desired title for the window to be created.
     * @param fxmlPath - path to the fxml file that contains the
     *                 formatting for the window to be opened.
     */
    public void openNonModalDialog(String title, String fxmlPath) {
        openNonModalDialog(title, fxmlPath, null);
    }

    /**
     * Opens a dialog with no modality.
     *
     * @param title      - desired title for the window to be created.
     * @param fxmlPath   - path to the fxml file that contains the
     *                   formatting for the window to be opened.
     * @param controller - The controller of the FXML.
     */
    public void openNonModalDialog(String title, String fxmlPath, Object controller) {
        openNonModalDialog(title, fxmlPath, controller, -1, -1);
    }

    /**
     * Opens a dialog with no modality.
     *
     * @param title      - desired title for the window to be created.
     * @param fxmlPath   - path to the fxml file that contains the
     *                   formatting for the window to be opened.
     * @param controller - The controller of the FXML.
     * @param x          - the horizonal distance from the left edge of the
     *                   desktop window to the left edge of the new window.
     * @param y          - the vertical distance from the top edge of the
     *                   desktop window to the top edge of the new window.
     */
    public void openNonModalDialog(String title, String fxmlPath, Object controller,
                                   int x, int y) {
        openDialog(title, fxmlPath, controller, x, y, Modality.NONE);
    }

    /**
     * Private generic method to open a new window.
     *
     * @param title      - desired title for the window to be created.
     * @param fxmlPath   - path to the fxml file that contains the
     *                   formatting for the window to be opened.
     * @param controller - The controller of the FXML.
     * @param x          - the horizonal distance from the left edge of the
     *                   desktop window to the left edge of the new window.
     * @param y          - the vertical distance from the top edge of the
     *                   desktop window to the top edge of the new window.
     * @param modality   - The modality of the new window.
     */
    private void openDialog(String title, String fxmlPath,
                            Object controller, int x, int y, Modality modality) {
        FXMLLoader fxmlLoader = new FXMLLoader(mediator.getClass().getResource(fxmlPath));
        if (controller != null) {
            fxmlLoader.setController(controller);
        }
        final Stage dialogStage = new Stage();
        // Load in icon for the new dialog
        URL url = getClass().getResource("/cpusim/gui/about/cpusim_icon.jpg");
        Image icon = new Image(url.toExternalForm());
        dialogStage.getIcons().add(icon);
        if (controller instanceof PreferencesController ||
                controller instanceof EditMachineInstructionController ||
                controller instanceof EditFetchSequenceController) {
            dialogStage.setResizable(false);
        }
        Pane dialogRoot = null;

        try {
            dialogRoot = fxmlLoader.load();
        } catch (IOException e) {
            //TODO: something better...
            System.out.println(e.getMessage());
        }
        Scene dialogScene = new Scene(dialogRoot);
        dialogStage.setScene(dialogScene);
        //dialogStage.initOwner(stage);
        dialogStage.initModality(modality);
        dialogStage.setTitle(title);
        if (x >= 0 && y >= 0) {
            dialogStage.setX(stage.getX() + x);
            dialogStage.setY(stage.getY() + y);
        }
        // pressing escape key causes the dialog to close without saving changes
        dialogScene.addEventFilter(
                KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent event) {
                        if (event.getCode().equals(KeyCode.ESCAPE)) {
                            if (dialogStage.isFocused()) {
                                dialogStage.close();
                            }
                        }
                    }
                });
        dialogStage.show();
    }

    /**
     * gives a fileChooser object certain properties
     *
     * @param fileChooser fileChooser to be modified
     * @param title       title of fileChooser window
     * @param text        true for saving an assembly language program, false for a machine
     */
    public void initFileChooser(FileChooser fileChooser, String title, boolean text) {
        fileChooser.setTitle(title);
        if (text) {
            fileChooser.setInitialDirectory(new File(currentTextDirectory));
        } else {
            fileChooser.setInitialDirectory(new File(mediator
                    .getCurrentMachineDirectory()));
        }
    }

    /**
     * extracts all the text from a file including new lines
     *
     * @param fileToOpen file to extract text from
     * @return the text contained in the file as a string
     */
    private String extractTextFromFile(File fileToOpen) {
        try {
            String content = "";
            FileReader freader = new FileReader(fileToOpen);
            BufferedReader breader = new BufferedReader(freader);
            while (true) {
                String line = breader.readLine();
                if (line == null) {
                    break;
                }
                content += line + NEWLINE;
            }

            freader.close();
            breader.close();

            return content;
        } catch (IOException ioe) {
            //TODO: something...
            System.out.println("IO fail");

        }
        return null;


    }

    /**
     * saves text to a file.  If unable to save, a dialog appears
     * indicating the problem.
     *
     * @param fileToSave file to be saved
     * @param text       text to be put in the file to be saved
     * @return true if the save was successful.
     */
    private boolean saveTextFile(File fileToSave, String text) {
        try {
            FileWriter fwriter = new FileWriter(fileToSave);
            BufferedWriter bwriter = new BufferedWriter(fwriter);

            fwriter.write(text);

            fwriter.close();
            bwriter.close();
            return true;
        } catch (IOException ioe) {
            Dialogs.createErrorDialog(stage, "Error", "Unable to save the text to a file.").showAndWait();
            return false;
        }
    }

    /**
     * saves the contents of a tab to a file if the current contents of that
     * tab have not already been saved.
     *
     * @param tab tab whose contents needs to be saved
     * @return true if the tab was successfully saved to a file.
     */
    private boolean save(Tab tab) {
        CodePaneTab theTab = (CodePaneTab) tab;
        if (theTab.getFile() == null) {
            return saveAs(theTab);
        }

        InlineStyleTextArea textToSave = (InlineStyleTextArea) theTab.getContent();
        if (theTab.getDirty()) {
            boolean successfulSave = saveTextFile(theTab.getFile(), textToSave.getText
                    ());

            if (successfulSave) {
                theTab.setDirty(false);
                return true;
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * saves the content of a tab in a new file
     *
     * @param tab tab whose content needs to be saved
     * @return true if the tab was successfully saved to a file
     */
    private boolean saveAs(Tab tab) {
        FileChooser fileChooser = new FileChooser();
        initFileChooser(fileChooser, "Save Text", true);
                /*fileChooser.getExtensionFilters().add(new ExtensionFilter("Assembly
                Language File (.a)",
                        "*.a"));*/

        File fileToSave = fileChooser.showSaveDialog(stage);
        final File finalFileToSave;

        if (fileToSave == null) {
            finalFileToSave = null;
        }
                /*else if (fileToSave.getAbsolutePath().lastIndexOf(".a") != 
                        fileToSave.getAbsolutePath().length() - 2) {
                    finalFileToSave = new File(fileToSave.getAbsolutePath() + ".a");
                }*/
        else {
            finalFileToSave = new File(fileToSave.getAbsolutePath());
        }

        if (finalFileToSave != null) {

            InlineStyleTextArea textToSave = (InlineStyleTextArea) tab.getContent();

            saveTextFile(finalFileToSave, textToSave.getText());

            ((CodePaneTab) tab).setFile(finalFileToSave);
            tab.getTooltip().setText(finalFileToSave.getAbsolutePath());

            // Update Menu
            MenuItem copyPath = new MenuItem("Copy Path Name ");
            copyPath.setOnAction(e -> {
                if (finalFileToSave != null) {
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent content = new ClipboardContent();
                    content.putString(finalFileToSave.getAbsolutePath());
                    clipboard.setContent(content);
                }
            });
            ObservableList<MenuItem> mi = tab.getContextMenu().getItems();
            mi.remove(3);
            mi.add(copyPath);
        }
        return finalFileToSave != null;
    }

    /**
     * opens a text file
     *
     * @param fileToOpen file to be opened
     */
    public void open(File fileToOpen) {
        currentTextDirectory = fileToOpen.getParent();
        String content = extractTextFromFile(fileToOpen);
        if (content == null) {
            Dialogs.createErrorDialog(stage, "Nonexistant File", "There is no longer a file at the path " +
                    fileToOpen.getAbsolutePath()).showAndWait();

            if (reopenTextFiles.contains(fileToOpen.getAbsolutePath())) {
                reopenTextFiles.remove(fileToOpen.getAbsolutePath());
            }
            updateReopenTextMenu();
            return;
        }
        //update the reopen menu
        updateReopenTextFiles(fileToOpen);

        //if text is already open, just select the proper tab else open a new tab
        Optional<Tab> existingTab = textTabPane.getTabs().stream()
                .filter(t -> ((CodePaneTab) t).getFile() != null)
                .filter(t -> fileToOpen.getAbsolutePath().equals(
                        ((CodePaneTab) t).getFile().getAbsolutePath()))
                .findFirst();
        if (existingTab.isPresent()) {
            textTabPane.getSelectionModel().select(existingTab.get());
            currentTextDirectory = fileToOpen.getParent();
        } else {
            addTab(content, fileToOpen.getName(), fileToOpen);
        }
    }

    /**
     * puts the input file at the front of the reopenTextFiles queue and removes
     * the last entry if the queue has more than 10 elements
     *
     * @param fileToOpen input file
     */
    private void updateReopenTextFiles(File fileToOpen) {
        if (reopenTextFiles.contains(fileToOpen.getAbsolutePath())) {
            reopenTextFiles.remove(fileToOpen.getAbsolutePath());
        }
        reopenTextFiles.addFirst(fileToOpen.getAbsolutePath());
        if (reopenTextFiles.size() > 10) {
            reopenTextFiles.removeLast();
        }

        updateReopenTextMenu();
    }

    /**
     * Updates the reopenTextMenu so that it contains the proper sublist of
     * files as dictated by the reopenTextFiles queue
     */
    private void updateReopenTextMenu() {
        reopenTextMenu.getItems().clear();

        for (String filePath : reopenTextFiles) {
            //this is a workaround that may need to be changed...
            final File finalFile = new File(filePath);
            MenuItem menuItem = new MenuItem(filePath);
            menuItem.setOnAction(e -> open(finalFile));
            reopenTextMenu.getItems().add(menuItem);
        }
    }

    /**
     * puts the input file at the front of the reopenMachineFiles queue and removes
     * the last entry if the queue has more than 10 elements
     *
     * @param fileToOpen input file
     */
    public void updateReopenMachineFiles(File fileToOpen) {
        if (reopenMachineFiles.contains(fileToOpen.getAbsolutePath())) {
            reopenMachineFiles.remove(fileToOpen.getAbsolutePath());
        }
        reopenMachineFiles.addFirst(fileToOpen.getAbsolutePath());
        if (reopenMachineFiles.size() > 10) {
            reopenMachineFiles.removeLast();
        }
        updateReopenMachineMenu();
    }

    /**
     * Updates the reopenMachineMenu so that it contains the proper sublist of
     * files as dictated by the reopenMachineFiles queue
     */
    public void updateReopenMachineMenu() {
        reopenMachineMenu.getItems().clear();

        for (String filePath : reopenMachineFiles) {
            //this is a workaround that may need to be changed...
            final File finalFile = new File(filePath);
            MenuItem menuItem = new MenuItem(filePath);
            menuItem.setOnAction(e -> {
                if (mediator.isMachineDirty()) {
                    Alert dialog = Dialogs.createCustomizedConfirmationDialog(stage, "Save Machine",
                            "The machine you are currently working on is unsaved.  " +
                                    "Would you like to save it before you open a new machine?");
                    dialog.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);
                    Optional<ButtonType> result = dialog.showAndWait();
                    if (result.get() == buttonTypeYes) {
                        handleSaveMachine(e);
                    } else if (result.get() == buttonTypeCancel) {
                        return;
                    }
                }
                mediator.openMachine(finalFile);
            });
            reopenMachineMenu.getItems().add(menuItem);
        }
    }

    /**
     * Adds a tab with the default settings.
     */
    public void addDefaultTab() {
        addTab("", "Untitled", null);
    }

    /**
     * updates the Save and Load RAM menus so that the the contain a menu item
     * for each RAM in the current machine
     */
    private void updateRamMenus() {
        ObservableList<RAM> rams = (ObservableList<RAM>)
                mediator.getMachine().getModule("rams");
        saveRamMenu.getItems().clear();
        openRamMenu.getItems().clear();

        for (RAM ram : rams) {
            final RAM finalRam = ram;
            MenuItem saveMenuItem = new MenuItem("from " + ram.getName() + "...");
            saveMenuItem.setOnAction(e -> saveRam(finalRam));
            saveRamMenu.getItems().add(saveMenuItem);

            MenuItem openMenuItem = new MenuItem("into " + ram.getName() + "...");
            openMenuItem.setOnAction(e -> openRam(finalRam));
            openRamMenu.getItems().add(openMenuItem);
        }
    }

    /**
     * opens data from a mif or hex file chosen by the user into a certain RAM
     *
     * @param ram
     */
    private void openRam(RAM ram) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(currentTextDirectory));

        fileChooser.setTitle("Open RAM");
        fileChooser.getExtensionFilters().add(
                new ExtensionFilter("Memory Text Files (.mif), (.hex)", "*.mif", "*" +
                        ".hex"));

        File fileToOpen = fileChooser.showOpenDialog(stage);
        if( fileToOpen == null) // user cancelled
            return;

        if (fileToOpen.getName().lastIndexOf(".mif") == fileToOpen.getName().length() -
                4) {
            try {
                mediator.parseMIFFile(extractTextFromFile(fileToOpen), ram,
                        fileToOpen.getAbsolutePath());
            } catch (MIFReaderException e) {
                Dialogs.createErrorDialog(stage, "MIF Parse Error", e.getMessage()).showAndWait();
            }
        } else {
            mediator.parseIntelHexFile(extractTextFromFile(fileToOpen), ram,
                    fileToOpen.getAbsolutePath());
        }

        for (RamTableController rc : ramControllers) {
            rc.updateTable();
        }

    }

    /**
     * saves the contents of a particular ram to an mif or hex file (as dictated by the
     * user)
     *
     * @param ram
     */
    private void saveRam(RAM ram) {

        List<String> choices = new ArrayList<String>();

        choices.add("Machine Instruction File (.mif)");
        choices.add("Intel Hex Format (.hex)");


        // NOTE: This choicebox dialog does not have a default choice, which
        // is not ideal. The problem is that if there is a default choice,
        // and the user is fine with that default and just clicks okay without
        // choosing something else the fileFormat string that will be returned
        // will be null.  This would be fine except that we can tell that the
        // user canceled the dialog only by checking if the string is null
        ChoiceDialog<String> dialog = Dialogs.createChoiceDialog(stage, "File Format Choice",
                "In what file format should your ram information be saved?",
                "Machine Instruction File (.mif)", choices);
        Optional<String> fileFormat = dialog.showAndWait();

        if (!fileFormat.isPresent()) {
            return;
        }

        ExtensionFilter extensionFilter;
        boolean asMIF;
        if (fileFormat.equals("Machine Instruction File (.mif)")) {
            extensionFilter = new ExtensionFilter(
                    "Machine Instruction Files (.mif)", "*.mif");
            asMIF = true;
        } else {
            extensionFilter = new ExtensionFilter(
                    "Intel Hex Format (.hex)", "*.hex");
            asMIF = false;
        }


        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(currentTextDirectory));

        fileChooser.setTitle("Save RAM");
        fileChooser.getExtensionFilters().add(extensionFilter);

        File fileToSave = fileChooser.showSaveDialog(stage);

        if (fileToSave == null) {
            return;
        }


        if (asMIF) {

            if (fileToSave.getAbsolutePath().lastIndexOf(".mif") !=
                    fileToSave.getAbsolutePath().length() - 4) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".mif");
            }

            try {
                FileWriter fwriter = new FileWriter(fileToSave);
                BufferedWriter bwriter = new BufferedWriter(fwriter);

                fwriter.write(mediator.ramToMIF(ram));

                fwriter.close();
                bwriter.close();
            } catch (IOException ioe) {
                Dialogs.createErrorDialog(stage, "Error", "Unable to save the ram to a file.").showAndWait();
            }

        } else {
            if (fileToSave.getAbsolutePath().lastIndexOf(".hex") !=
                    fileToSave.getAbsolutePath().length() - 4) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".hex");
            }

            try {
                FileWriter fwriter = new FileWriter(fileToSave);
                BufferedWriter bwriter = new BufferedWriter(fwriter);

                fwriter.write(mediator.ramToIntelHex(ram));

                fwriter.close();
                bwriter.close();
            } catch (IOException ioe) {
                Dialogs.createErrorDialog(stage, "Error", "Unable to save the ram to a file.").showAndWait();
            }
        }


    }

    /**
     * sets the value of the ram, register, and register array tables
     */
    public void setUpTables() {
        registerControllers.clear();
        ramControllers.clear();

        updateStyleOfTables();

        ObservableList<Register> registers =
                (ObservableList<Register>) mediator.getMachine().getModule("registers");

        RegisterTableController registerTableController =
                new RegisterTableController(this, registers, "Registers");
        FXMLLoader registerFxmlLoader = new FXMLLoader(
                mediator.getClass().getResource("gui/desktop/RegisterTable.fxml"));
        registerFxmlLoader.setController(registerTableController);
        registerControllers.add(registerTableController);


        Pane registerTableRoot = null;

        try {
            registerTableRoot = (Pane) registerFxmlLoader.load();
        } catch (IOException e) {
            //TODO: something more meaningful
            System.out.println("IOException: " + e.getMessage());
        }
        registerTableController.setDataBase(regDataBase);

        regVbox.setVgrow(regSplitPane, Priority.ALWAYS);

        regSplitPane.getItems().add(registerTableRoot);


        ObservableList<RegisterArray> registerArrays = (ObservableList<RegisterArray>)
                mediator.getMachine().getModule("registerArrays");

        Pane registerArrayTableRoot = null;

        if (!registerArrays.isEmpty()) {
            for (int i = 0; i < registerArrays.size(); i++) {
                FXMLLoader registerArrayFxmlLoader = new FXMLLoader(
                        mediator.getClass().getResource("gui/desktop/RegisterTable" +
                                ".fxml"));

                RegisterTableController registerArrayTableController = new
                        RegisterTableController(
                        this,
                        registerArrays.get(i).registers(),
                        registerArrays.get(i).getName());
                registerArrayFxmlLoader.setController(registerArrayTableController);

                registerControllers.add(registerArrayTableController);


                try {
                    registerArrayTableRoot = (Pane) registerArrayFxmlLoader.load();
                } catch (IOException e) {
                    //TODO: something...
                }
                registerArrayTableController.setDataBase(regDataBase);

                regSplitPane.getItems().add(registerArrayTableRoot);
            }
        }

        double numRegSplitPanes = regSplitPane.getItems().size();
        double regdpos = 0;
        for (int i = 0; i < numRegSplitPanes - 1; i++) {
            regdpos += (1.0 / numRegSplitPanes);
            regSplitPane.setDividerPosition(i, regdpos);
        }

        ObservableList<RAM> rams =
                (ObservableList<RAM>) mediator.getMachine().getModule("rams");

        if (!rams.isEmpty()) {
            ramVbox.getChildren().remove(noRAMLabel);
            ramToolBar.setDisable(false);

            Pane ramTableRoot = null;
            RamTableController ramTableController;

            for (int i = 0; i < rams.size(); i++) {
                FXMLLoader ramFxmlLoader = new FXMLLoader(
                        mediator.getClass().getResource("gui/desktop/RamTable.fxml"));
                ramTableController = new RamTableController(
                        this,
                        rams.get(i),
                        rams.get(i).getName());
                ramFxmlLoader.setController(ramTableController);

                ramControllers.add(ramTableController);


                try {
                    ramTableRoot = (Pane) ramFxmlLoader.load();
                } catch (IOException e) {
                    //TODO: something...
                }

                ramTableController.setDataBase(ramDataBase);
                ramTableController.setAddressBase(ramAddressBase);

                ramVbox.setVgrow(ramSplitPane, Priority.ALWAYS);
                ramSplitPane.getItems().add(ramTableRoot);

            }

            for (int i = 0; i < ramSplitPane.getDividers().size(); i++) {
                ramSplitPane.setDividerPosition(i,
                        1.0 / (ramSplitPane.getDividers().size() + 1) * (i + 1));
            }

            updateRamMenus();
            ramVbox.getChildren().addAll();
        } else {
            if (!ramVbox.getChildren().contains(noRAMLabel)) {
                ramVbox.getChildren().add(1, noRAMLabel);
            }
            ramToolBar.setDisable(true);
        }

        double numRamSplitPanes = ramSplitPane.getItems().size();
        double ramdpos = 0;
        for (int i = 0; i < numRamSplitPanes - 1; i++) {
            ramdpos += (1.0 / numRamSplitPanes);
            ramSplitPane.setDividerPosition(i, ramdpos);
        }


    }

    /**
     * gets rid of all register and ram tables
     */
    public void clearTables() {
        ramSplitPane.getItems().clear();
        regSplitPane.getItems().clear();
    }

    public void adjustTablesForNewModules() {
        if (regSplitPane.getItems().size() > 1) {
            regSplitPane.getItems().remove(1, regSplitPane.getItems().size());
        }
        registerControllers.remove(1, registerControllers.size());

        ObservableList<RegisterArray> registerArrays = (ObservableList<RegisterArray>)
                mediator.getMachine().getModule("registerArrays");

        Pane registerArrayTableRoot = null;

        if (!registerArrays.isEmpty()) {
            for (int i = 0; i < registerArrays.size(); i++) {
                FXMLLoader registerArrayFxmlLoader = new FXMLLoader(
                        mediator.getClass().getResource("gui/desktop/RegisterTable" +
                                ".fxml"));

                RegisterTableController registerArrayTableController = new
                        RegisterTableController(
                        this,
                        registerArrays.get(i).registers(),
                        registerArrays.get(i).getName());
                registerArrayFxmlLoader.setController(registerArrayTableController);

                registerControllers.add(registerArrayTableController);


                try {
                    registerArrayTableRoot = (Pane) registerArrayFxmlLoader.load();
                } catch (IOException e) {
                    //TODO: something...
                }
                registerArrayTableController.setDataBase(regDataBase);

                regSplitPane.getItems().add(registerArrayTableRoot);
            }
        }

        double numRegSplitPanes = regSplitPane.getItems().size();
        double regdpos = 0;
        for (int i = 0; i < numRegSplitPanes - 1; i++) {
            regdpos += (1.0 / numRegSplitPanes);
            regSplitPane.setDividerPosition(i, regdpos);
        }

        ramSplitPane.getItems().clear();
        ObservableList<RAM> rams =
                (ObservableList<RAM>) mediator.getMachine().getModule("rams");
        ramControllers.clear();

        if (!rams.isEmpty()) {
            ramVbox.getChildren().remove(noRAMLabel);
            ramToolBar.setDisable(false);

            Pane ramTableRoot = null;
            RamTableController ramTableController;

            for (int i = 0; i < rams.size(); i++) {
                FXMLLoader ramFxmlLoader = new FXMLLoader(
                        mediator.getClass().getResource("gui/desktop/RamTable.fxml"));
                ramTableController = new RamTableController(
                        this,
                        rams.get(i),
                        rams.get(i).getName());
                ramFxmlLoader.setController(ramTableController);

                ramControllers.add(ramTableController);


                try {
                    ramTableRoot = (Pane) ramFxmlLoader.load();
                } catch (IOException e) {
                    //TODO: something...
                }

                ramTableController.setDataBase(ramDataBase);
                ramTableController.setAddressBase(ramAddressBase);

                ramVbox.setVgrow(ramSplitPane, Priority.ALWAYS);
                ramSplitPane.getItems().add(ramTableRoot);
            }

            updateRamMenus();
            ramVbox.getChildren().addAll();
        } else {
            if (!ramVbox.getChildren().contains(noRAMLabel)) {
                ramVbox.getChildren().add(0, noRAMLabel);
            }
            ramToolBar.setDisable(true);

        }

        double numRamSplitPanes = ramSplitPane.getItems().size();
        double ramdpos = 0;
        for (int i = 0; i < numRamSplitPanes - 1; i++) {
            ramdpos += (1.0 / numRamSplitPanes);
            ramSplitPane.setDividerPosition(i, ramdpos);
        }

        updateRegisterAndRAMDisplays();

    }

    public void updateRegisterAndRAMDisplays() {
        for (RamTableController rtc : ramControllers) {
            rtc.updateTable();
        }
        for (RegisterTableController rtc : registerControllers) {
            rtc.updateTable();
        }
    }

    /**
     * Looks for all unsaved work and asks the user if he would like to save any of
     * the work before closing
     *
     * @return whether or not the window should be closed
     */
    private boolean confirmClosing() {
        if (inRunningMode.get()) {
            Alert dialog = Dialogs.createCustomizedConfirmationDialog(stage, "Running Program",
                    "There is a program running. " +
                            "Closing the application will also quit the program. " +
                            "Do you want to quit the running program?");
            dialog.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.get() == buttonTypeNo || result.get() == buttonTypeCancel) {
                return false;
            }
        }
        if (mediator.isMachineDirty()) {
            Alert dialog = Dialogs.createCustomizedConfirmationDialog(stage, "Save Machine",
                    "The machine you are currently working on is unsaved.  " +
                            "Would you like to save it before you close?");
            dialog.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.get() == buttonTypeYes) {
                mediator.saveMachine();
            } else if (result.get() == buttonTypeCancel) {
                return false;
            }
        }
        for (Tab tab : textTabPane.getTabs()) {
            if (((CodePaneTab) tab).getDirty()) {
                Alert dialog = Dialogs.createCustomizedConfirmationDialog(stage, "Save Text",
                        "Would you like to save your work before you "
                                + "close " + tab.getText().substring(1) + "?");
                dialog.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);
                Optional<ButtonType> result = dialog.showAndWait();
                if (result.get() == buttonTypeYes) {
                    save(tab);
                } else if (result.get() == buttonTypeCancel) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * stores certain preferences and other things that reflect a specific user's
     * experience with cpusim
     */
    public void storePreferences() {
        Preferences prefs = Preferences.userNodeForPackage(getClass());

        //save current text and machine directories
        prefs.put("machineDirectory", mediator.getCurrentMachineDirectory());
        prefs.put("textDirectory", currentTextDirectory);

        //save recently opened text files
        int i = 0;
        for (String filePath : reopenTextFiles) {
            prefs.put("reopenTextFile" + i, filePath);
            i++;
        }
        prefs.putInt("numTextFiles", reopenTextFiles.size());

        i = 0;
        for (String filePath : reopenMachineFiles) {
            prefs.put("reopenMachineFile" + i, filePath);
            i++;
        }
        prefs.putInt("numMachineFiles", reopenMachineFiles.size());

        prefs.put("regDataBase", regDataBase);
        prefs.put("ramAddressBase", ramAddressBase);
        prefs.put("ramDataBase", ramDataBase);

        prefs.put("assmFontSize", assmFontData.fontSize);
        prefs.put("assmFont", assmFontData.font);
        prefs.put("assmBackground", assmFontData.background);

        prefs.put("registerTableFontSize", registerTableFontData.fontSize);
        prefs.put("registerTableFont", registerTableFontData.font);
        prefs.put("registerTableBackground", registerTableFontData.background);

        prefs.put("ramTableFontSize", ramTableFontData.fontSize);
        prefs.put("ramTableFont", ramTableFontData.font);
        prefs.put("ramTableBackground", ramTableFontData.background);

        for (Map.Entry<String, KeyCodeInfo> binding : keyBindings.entrySet()) {
            prefs.put(binding.getKey(), binding.getValue().getKeyCode());
        }

        prefs.putBoolean("autoSave", otherSettings.autoSave);
        prefs.putBoolean("showLineNumbers", otherSettings.showLineNumbers.get());
        prefs.putBoolean("clearConsoleOnRun", otherSettings.clearConsoleOnRun);
        prefs.putBoolean("lineWrap", otherSettings.lineWrap.get());
    }

    /**
     * Loads preferences
     */
    public void loadPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        mediator.setCurrentMachineDirectory(System.getProperty("user.dir"));
        currentTextDirectory = System.getProperty("user.dir");
        // the next two lines sometimes cause problems (exceptions to be thrown)
        //        currentMachineDirectory = prefs.get("machineDirectory", System
        // .getProperty("user.dir"));
        //        currentTextDirectory = prefs.get("textDirectory", System.getProperty
        // ("user.dir"));


        int numTextFiles = prefs.getInt("numTextFiles", 0);
        for (int i = 0; i < numTextFiles; i++) {
            reopenTextFiles.offer(prefs.get("reopenTextFile" + i, ""));
        }

        updateReopenTextMenu();

        int numMachineFiles = prefs.getInt("numMachineFiles", 0);
        for (int i = 0; i < numMachineFiles; i++) {
            reopenMachineFiles.offer(prefs.get("reopenMachineFile" + i, ""));
        }

        updateReopenMachineMenu();

        regDataBase = prefs.get("regDataBase", "Dec");
        ramAddressBase = prefs.get("ramAddressBase", "Dec");
        ramDataBase = prefs.get("ramDataBase", "Decimal");

        assmFontData.fontSize = prefs.get("assmFontSize", "12");
        assmFontData.font = prefs.get("assmFont", "Courier New");
        assmFontData.background = prefs.get("assmBackground", "#fff");

        registerTableFontData.fontSize = prefs.get("registerTableFontSize", "12");
        registerTableFontData.font = prefs.get("registerTableFont", "Courier New");
        registerTableFontData.background = prefs.get("registerTableBackground", "#fff");

        ramTableFontData.fontSize = prefs.get("ramTableFontSize", "12");
        ramTableFontData.font = prefs.get("ramTableFont", "Courier New");
        ramTableFontData.background = prefs.get("ramTableBackground", "#fff");

        for (String[] defaultBinding : DEFAULT_KEY_BINDINGS) {
            String menuName = defaultBinding[0];
            String keyCode = prefs.get(menuName, defaultBinding[1]);
            keyBindings.put(menuName, new KeyCodeInfo(keyCode));
        }

        otherSettings.autoSave = prefs.getBoolean("autoSave", false);
        otherSettings.showLineNumbers.set(prefs.getBoolean("showLineNumbers", true));
        otherSettings.clearConsoleOnRun = prefs.getBoolean("clearConsoleOnRun", true);
        otherSettings.lineWrap.set(prefs.getBoolean("lineWrap", false));
    }

    /**
     * sets the style of the text in the tables area;
     */
    public void updateStyleOfTables() {
        //  WHAT IS THIS CODE FOR?????
        //  I COULDN'T TELL SO I COMMENTED IT OUT.
//        if (mainPane.getStyleClass().size() > 1) {
//            mainPane.getStyleClass().remove(1);
//        }

        // old code from when background was a choicebox instead of colorpicker
//        if (!backgroundSetting.keySet().contains(registerTableFontData.background)) {
//            registerTableFontData.background = "#fff";
//        }
//
//        mainPane.getStylesheets().add(backgroundSetting.get(registerTableFontData.background));

        for (RegisterTableController rtc : registerControllers) {
            rtc.updateTable();
        }
        for (RamTableController rtc : ramControllers) {
            rtc.updateTable();
        }
    }

    /**
     * Returns the font data object for the assembly code panes
     *
     * @return the font data object
     */
    public FontData getAssemblyPaneFontData() {
        return assmFontData;
    }

    /**
     * Returns the register table font data object
     *
     * @return the register table font data object
     */
    public FontData getRegisterTableFontData() {
        return registerTableFontData;
    }

    /**
     * Returns the other settings of preference
     *
     * @return the other settings of preference
     */
    public OtherSettings getOtherSettings() {
        return otherSettings;
    }

    /**
     * Uses the key code combinations in the keyCodeCombinations list to bind the proper
     * menu items to the proper key combinations.
     */
    private void bindKeys() {

        //List of menu items to give key codes
        Set<MenuItem> menuItems = new HashSet<>();

        //put appropriate menu items from the file menu into the array
        for (MenuItem menuItem : fileMenu.getItems()) {
            if (menuItem.getText() == null) {
                // it's just a separator line
            } else if (menuItem.getText().equals("Quit")) {
                menuItem.setAccelerator(new KeyCodeCombination(KeyCode.Q,
                        KeyCodeCombination.
                                ModifierValue.UP, KeyCodeCombination.ModifierValue.UP,
                        KeyCodeCombination.
                                ModifierValue.UP, KeyCodeCombination.ModifierValue.UP,
                        KeyCodeCombination.
                                ModifierValue.DOWN));

            } else
                menuItems.add(menuItem);
        }

        //put appropriate menu items from the edit menu into the array and
        //give appropriate menu items their default (final) value
        for (MenuItem menuItem : editMenu.getItems()) {
            if (menuItem.getText() == null) {
                // it's just a separator line
            }
            // Delete: DELETE
            else if (menuItem.getText().equals("Delete")) {
                menuItem.setAccelerator(new KeyCodeCombination(KeyCode.DELETE,
                        KeyCodeCombination.
                                ModifierValue.UP, KeyCodeCombination.ModifierValue.UP,
                        KeyCodeCombination.
                                ModifierValue.UP, KeyCodeCombination.ModifierValue.UP,
                        KeyCodeCombination.
                                ModifierValue.UP));
            }
            // Undo: SHORTCUT-Z
            else if (menuItem.getText().equals("Undo")) {
                menuItem.setAccelerator(new KeyCodeCombination(KeyCode.Z,
                        KeyCodeCombination.
                                ModifierValue.UP, KeyCodeCombination.ModifierValue.UP,
                        KeyCodeCombination.
                                ModifierValue.UP, KeyCodeCombination.ModifierValue.UP,
                        KeyCodeCombination.
                                ModifierValue.DOWN));
            }
            // Redo: SHORTCUT-Shift-Z
            else if (menuItem.getText().equals("Redo")) {
                menuItem.setAccelerator(new KeyCodeCombination(KeyCode.Z,
                        KeyCodeCombination.
                                ModifierValue.DOWN, KeyCodeCombination.ModifierValue.UP,
                        KeyCodeCombination.
                                ModifierValue.UP, KeyCodeCombination.ModifierValue.UP,
                        KeyCodeCombination.
                                ModifierValue.DOWN));
            }
            // Cut: SHORTCUT-X
            else if (menuItem.getText().equals("Cut")) {
                menuItem.setAccelerator(new KeyCodeCombination(KeyCode.X,
                        KeyCodeCombination.
                                ModifierValue.UP, KeyCodeCombination.ModifierValue.UP,
                        KeyCodeCombination.
                                ModifierValue.UP, KeyCodeCombination.ModifierValue.UP,
                        KeyCodeCombination.
                                ModifierValue.DOWN));
            }
            // Copy: SHORTCUT-C
            else if (menuItem.getText().equals("Copy")) {
                menuItem.setAccelerator(new KeyCodeCombination(KeyCode.C,
                        KeyCodeCombination.
                                ModifierValue.UP, KeyCodeCombination.ModifierValue.UP,
                        KeyCodeCombination.
                                ModifierValue.UP, KeyCodeCombination.ModifierValue.UP,
                        KeyCodeCombination.
                                ModifierValue.DOWN));
            }
            // Paste: SHORTCUT-Y
            else if (menuItem.getText().equals("Paste")) {
                menuItem.setAccelerator(new KeyCodeCombination(KeyCode.V,
                        KeyCodeCombination.
                                ModifierValue.UP, KeyCodeCombination.ModifierValue.UP,
                        KeyCodeCombination.
                                ModifierValue.UP, KeyCodeCombination.ModifierValue.UP,
                        KeyCodeCombination.
                                ModifierValue.DOWN));
            } else if (menuItem.getText().equals("Select All")) {
                menuItem.setAccelerator(new KeyCodeCombination(KeyCode.A,
                        KeyCodeCombination.
                                ModifierValue.UP, KeyCodeCombination.ModifierValue.UP,
                        KeyCodeCombination.
                                ModifierValue.UP, KeyCodeCombination.ModifierValue.UP,
                        KeyCodeCombination.
                                ModifierValue.DOWN));
            } else
                menuItems.add(menuItem);
        }

        //get appropriate menu items (all fo them) from the rest of the menu items
        for (MenuItem menuItem : modifyMenu.getItems()) {
            if (menuItem.getText() != null)
                menuItems.add(menuItem);
        }

        for (MenuItem menuItem : executeMenu.getItems()) {
            if (menuItem.getText() != null)
                menuItems.add(menuItem);
        }

        for (MenuItem menuItem : helpMenu.getItems()) {
            menuItems.add(menuItem);
        }

        //bind the key proper keycode to the every menu item
        for (MenuItem menuItem : menuItems) {
            if (keyBindings.containsKey(menuItem.getText()))
                keyBindings.get(menuItem.getText()).bindToMenuItem(menuItem);
        }
    }


    /**
     * returns the current key bindings for the menu items
     *
     * @returns the current key bindings for the menu items
     */
    public Map<String, KeyCodeInfo> getKeyBindings() {
        return keyBindings;
    }

    /**
     * Sets the key bindings of the menu items and binds those keys to the menu items
     * (this doesn't seem to work unless it is being done upon loading the program)
     *
     * @param keyBindings The new key bindings for the menu items
     */
    public void setKeyBindings(Map<String, KeyCodeInfo> keyBindings) {
        this.keyBindings = keyBindings;
    }

    /**
     * highlights the token in the text pane for the file containing the token.
     * If there is no tab for that file, then a new tab is created.
     *
     * @param token the token to be highlighted
     */
    public void highlightToken(Token token) {
        File file = new File(token.filename);
        if (!file.canRead()) {
            Dialogs.createErrorDialog(stage, "IO Error", "CPU Sim could not find the file to open and "
                    + "highlight: " + file.getAbsolutePath()).showAndWait();
            return;
        }
        InlineStyleTextArea textArea = (InlineStyleTextArea) getTabForFile(file)
                .getContent();
        textArea.selectRange(token.offset, token.offset + token.contents.length());
    }

    public void printlnToConsole(String s) {
        consoleManager.printlnToConsole(s);
    }

    public FontData getRamTableFontData() {
        return ramTableFontData;
    }



    /**
     * A class to hold all other preference settings
     */
    public class OtherSettings {
        public boolean autoSave;
        public SimpleBooleanProperty showLineNumbers;
        public boolean clearConsoleOnRun;
        public SimpleBooleanProperty lineWrap;

        public OtherSettings() {
            showLineNumbers = new SimpleBooleanProperty(true);
            // add a listener that changes the line numbers for the selected tab
            // The line numbers for other tabs are not changed until they are selected.
            showLineNumbers.addListener((arg0, oldVal, newVal) -> {
                Tab t = textTabPane.getSelectionModel().getSelectedItem();
                if (t == null) {
                    return;
                }
                StyledTextArea codeArea = (StyledTextArea) t.getContent();
                LineNumAndBreakpointFactory lFactory =
                        (LineNumAndBreakpointFactory) codeArea.getParagraphGraphicFactory();

                if (newVal) { // show line numbers
                    lFactory.setFormat(digits -> "%" + digits + "d");
                } else { // hide line numbers
                    lFactory.setFormat(digits -> "");
                }
            });

            lineWrap = new SimpleBooleanProperty(false);
            lineWrap.addListener((arg0, oldVal, newVal) -> {
                Tab t = textTabPane.getSelectionModel().getSelectedItem();
                if (t == null) {
                    return;
                }
                StyledTextArea codeArea = (StyledTextArea) t.getContent();
                codeArea.setWrapText(newVal);
            });
        }
    }
}
