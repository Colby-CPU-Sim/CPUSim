package cpusim.gui.find;

/**
 * Modified by Devon Cormack, Jonathon Brink-Roby, and Nick Aalberg on 11/29/2013
 * <p>
 * Changes:	onReplaceAllButtonClicked() was rewritten so that undoing a replace-all
 * would undo
 * the entire action rather than each replacement case at a time.
 */

import com.sun.javafx.scene.control.behavior.TextInputControlBehavior;
import com.sun.javafx.scene.control.skin.TextInputControlSkin;
import cpusim.Mediator;
import cpusim.gui.desktop.DesktopController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.fxmisc.richtext.InlineStyleTextArea;
import org.fxmisc.richtext.StyledTextArea;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class FindReplaceController implements Initializable {

    /////////////// Fields ///////////////

    @FXML
    private TextField findTF;
    @FXML
    private TextField replaceWithTF;
    @FXML
    private Label statusLabel;

    @FXML
    private CheckBox caseSensitive;
    @FXML
    private CheckBox wholeWord;
    @FXML
    private RadioButton forwardRB;
    @FXML
    private RadioButton backwardRB;
    private ToggleGroup rbGroup;

    @FXML
    private Button replaceButton;
    @FXML
    private Button replaceAllButton;
    @FXML
    private Button findNextButton;
    @FXML
    private Button closeButton;

    private DesktopController desktop;
    private boolean shiftPressed;
    private boolean useConsole;

    private boolean searchForward;
    private boolean capsSensitive;
    private boolean matchWholeWord;

    private ArrayList<int[]> instances;

    private static final char[] WORD_SEPARATORS = {' ', '\t', '\n',
            '\r', '\f', '.', ',', ':',
            '-', '(', ')', '[', ']', '{',
            '}', '<', '>', '/', '|',
            '\\', '\'', '\"'};

    /////////////// Constructor and Initializer ///////////////

    /**
     * Constructor. Requires the parent desktop controller.
     *
     * @param mediator The Mediator.
     */
    public FindReplaceController(Mediator mediator) {
        desktop = mediator.getDesktopController();
    }

    @Override
    /**
     * Initializes the dialog box.
     *
     * @param arg0 the standard URL
     * @param arg1 the standard Resource Bundle
     */
    public void initialize(URL arg0, ResourceBundle arg1) {
        initializeSearchFields();
        initializeCBsAndRBs();
    }

    /////////////// Button Actions ///////////////

    @FXML
    /**
     * Called when the Replace button is clicked.
     */
    public void onReplaceButtonClicked() {
        StyledTextArea ta = validAreaToSearch();
        if (ta == null) {
            statusLabel.setText("No text area selected.");
            return;
        }
        else if (ta.getCaretPosition() != ta.getAnchor()) {
            int pos = Math.min(ta.getCaretPosition(), ta.getAnchor());
            replaceAnchorToCaretWith(replaceWithTF.getText());
            ta.positionCaret(pos);
        }
    }

    @FXML
    /**
     * Called when the Replace All button is clicked.
     */
    public void onReplaceAllButtonClicked() {
        if (updateInstances()) {
            ArrayList<int[]> instancesWOOverlaps = removeOverlaps(instances);

            // Loop through and replace
            for (int i = instancesWOOverlaps.size() - 1; i >= 0; i--) {
                int I = instancesWOOverlaps.get(i)[0];
                int J = instancesWOOverlaps.get(i)[1];
                replaceItoJWith(I, J, replaceWithTF.getText());
            }

            // New code (December 2013) - start (by Nick, Devon, and Jon)

            //get text area of the current tab
            InlineStyleTextArea ta = (InlineStyleTextArea) desktop.getTextTabPane()
                    .getSelectionModel(
            ).getSelectedItem().getContent();
            //get the system clipboard
            Clipboard clipboard = Clipboard.getSystemClipboard();

            //get the old content on the clipboard
            DataFormat df = (DataFormat) (clipboard.getContentTypes().toArray()[0]);
            Object oldClipboardValue = (clipboard.getContent(df));

            //put the old content into a ClipboardContent Object
            ClipboardContent oldContent = new ClipboardContent();
            oldContent.put(df, oldClipboardValue);
            //copy everything in the text field
            ta.selectAll();
            ta.copy();
            //undo all the find replae changes made
            for (int i = instancesWOOverlaps.size() - 1; i >= 0; i--) {
                ta.undo();
            }
            //paste the copied (new) content to the text area
            ta.selectAll();
            ta.paste();

            //reset the content of the clipboard
            clipboard.setContent(oldContent);

            // End of new code (December 2013)
        }
        // else the status label is already updated so just return
    }

    @FXML
    /**
     * Called when the Find button is clicked.
     */
    public void onFindNextButtonClicked() {
        if (updateInstances()) {
            StyledTextArea ta = validAreaToSearch();
            if (instances.size() == 0) {
                statusLabel.setText("Text not found.");
                return;
            }
            else if (instances.size() == 1) {
                ta.selectRange(instances.get(0)[0], instances.get(0)[1]);
                return;
            }

            int caret = ta.getCaretPosition();
            int anchor = ta.getAnchor();
            int length = ta.getText().length();
            int start = Math.min(caret, anchor);
            int pos = start;
            int direction = searchForward ? 1 : -1;

            while (true) {
                pos = (pos + direction + length) % length;
                for (int i = 0; i < instances.size(); i++) {
                    if (instances.get(i)[0] == pos) {
                        if ((searchForward && pos < start) ||
                                (!searchForward && pos > start)) {
                            statusLabel.setText("Wrapped.");
                        }
                        else {
                            statusLabel.setText("");
                        }
                        ta.selectRange(instances.get(i)[0], instances.get(i)[1]);
                        return;
                    }
                }
            }
        }
    }

    @FXML
    /**
     * Called when the Close button is clicked.
     */
    public void onCloseButtonClicked() {
        ((Stage) (closeButton.getScene().getWindow())).close();
    }

    /////////////// Initialize Helpers ///////////////

    /**
     * Sets up the text fields and adds
     * appropriate handlers.
     */
    public void initializeSearchFields() {
        statusLabel.setText("");

        findTF.setOnKeyReleased(
                new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent event) {
                        if (event.getCode().equals(KeyCode.ENTER)) {
                            if (!shiftPressed) {
                                onFindNextButtonClicked();
                            }
                            else {
                                if (forwardRB.isSelected()) {
                                    backwardRB.selectedProperty().set(true);
                                    onFindNextButtonClicked();
                                    forwardRB.selectedProperty().set(true);
                                }
                                else {
                                    forwardRB.selectedProperty().set(true);
                                    onFindNextButtonClicked();
                                    backwardRB.selectedProperty().set(true);
                                }
                            }
                        }
                        else if (event.getCode().equals(KeyCode.SHIFT)) {
                            shiftPressed = false;
                        }
                    }
                });

        findTF.setOnKeyPressed(
                new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent event) {
                        if (event.getCode().equals(KeyCode.SHIFT)) {
                            shiftPressed = true;
                        }
                    }
                });
    }

    /**
     * Sets up the check boxes and the radio
     * buttons, adding the appropriate listeners.
     */
    public void initializeCBsAndRBs() {
        rbGroup = new ToggleGroup();
        forwardRB.setToggleGroup(rbGroup);
        backwardRB.setToggleGroup(rbGroup);

        backwardRB.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0,
                                Boolean oldValue, Boolean newValue) {
                searchForward = !(newValue.booleanValue());
            }
        });

        caseSensitive.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable,
                                Boolean oldValue, Boolean newValue) {
                capsSensitive = newValue.booleanValue();
            }
        });

        wholeWord.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable,
                                Boolean oldValue, Boolean newValue) {
                matchWholeWord = newValue.booleanValue();
            }
        });

        forwardRB.selectedProperty().set(true);
        backwardRB.selectedProperty().set(true);
        forwardRB.selectedProperty().set(true);

    }

    /////////////// More Helpers ///////////////

    /**
     * Updates the instances field, returning a
     * boolean telling if everything was updated
     * correctly.
     *
     * @return a boolean telling if instances was
     * updated correctly.
     */
    private boolean updateInstances() {
        String key = findTF.getText();
        if (key.length() == 0) {
            statusLabel.setText("No text entered.");
            return false;
        }
        else if (validAreaToSearch() == null) {
            statusLabel.setText("No text area selected.");
            return false;
        }
        instances = allInstances();
        return true;
    }

    /**
     * Gives the CodeArea of the current tab open, if there
     * is one. Returns null if not.
     *
     * @return the StyledTextArea of the current tab open.
     */
    public StyledTextArea validAreaToSearch() {
        if (useConsole) {
            return desktop.getIOConsole();
        }

        TabPane tp = desktop.getTextTabPane();
        Tab currTab = tp.getSelectionModel().getSelectedItem();
        if (currTab == null) {
            return null;
        }
        return (StyledTextArea) currTab.getContent();
    }

    /**
     * Replaces the currently selected text in the
     * current text tab with the specified string.
     *
     * @param s The string that will replace the currently
     *          selected text.
     */
    public void replaceAnchorToCaretWith(String s) {
        StyledTextArea ta = validAreaToSearch();
        replaceItoJWith(ta.getAnchor(), ta.getCaretPosition(), s);
    }

    /**
     * Replaces a portion of the text in the current
     * text tab with the specified string.
     *
     * @param i The starting index of selection of replacement.
     * @param j The ending index of selection of replacement.
     * @param s The string that will replace the currently
     *          selected text.
     */
    public void replaceItoJWith(int i, int j, String s) {
        StyledTextArea ta = validAreaToSearch();
        // To get the correct order
        if (i > j) {
            int a = i;
            int b = j;
            j = a;
            i = b;
        }

        // Note that the below implementation is a major hack.
        // We select the text we want to replace, paste the
        // contents in to replace, then return the Clipboard to
        // its original state. This way the actions can be un-done
        // and re-done.
        ta.selectRange(i, j);

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
        content.putString(s);
        clipboard.setContent(content);
        ta.paste();

        if (setBack) {
            ClipboardContent oldContent = new ClipboardContent();
            oldContent.put(df, oldVal);
            clipboard.setContent(oldContent);
        }

    }

    /**
     * Gives an arraylist of int[]s, where each int[] contains
     * the indices of an occurrence of the key in the content area.
     *
     * @return An arraylist of int[]s, where each int[] contains
     * the indices of an occurrence of the key in the content area.
     */
    private ArrayList<int[]> allInstances() {
        ArrayList<int[]> instances = new ArrayList<int[]>();
        StyledTextArea ta = validAreaToSearch();

        String content = ta.getText();
        String key = findTF.getText();
        key = capsSensitive ? key : key.toLowerCase();
        content = capsSensitive ? content : content.toLowerCase();

        if (content.length() < key.length()) {
            statusLabel.setText("Text not found.");
            return instances;
        }

        for (int i = 0; i < content.length() - key.length() + 1; i++) {
            if (content.substring(i, i + key.length()).equals(key)) {
                int[] a = {i, i + key.length()};
                if (!matchWholeWord) {
                    instances.add(a);
                }
                else {
                    boolean beforeIsSeparator = true;
                    boolean afterIsSeparator = true;
                    if (i > 0) {
                        beforeIsSeparator = isSeparator(content.charAt(i - 1));
                    }
                    if (i < content.length() - key.length()) {
                        afterIsSeparator = isSeparator(content.charAt(i + key.length()));
                    }
                    if (beforeIsSeparator && afterIsSeparator) {
                        instances.add(a);
                    }
                }
            }
        }
        return instances;
    }

    /**
     * Returns an arraylist equivalent to the arraylist of
     * int[] that was passed to it, after the overlapping
     * key occurrence arrays have been removed. This is used
     * by the replace all button so that we don't have problems
     * replacing all in things like "aaaa" where there are multiple
     * occurrences of the string "aa".
     *
     * @param arraylist The arraylist to remove the overlaps from.
     * @return an arraylist equivalent to the arraylist of
     * int[] that was passed to it, after the overlapping
     * key occurrence arrays have been removed.
     */
    private ArrayList<int[]> removeOverlaps(ArrayList<int[]> arraylist) {
        if (arraylist.size() == 0 || arraylist.size() == 1) {
            return arraylist;
        }
        else if (arraylist.size() == 2) {
            if (arraylist.get(0)[0] <= arraylist.get(0)[1] &&
                    arraylist.get(0)[1] <= arraylist.get(1)[0] &&
                    arraylist.get(1)[0] <= arraylist.get(1)[1]) {
                return arraylist;
            }
            else {
                ArrayList<int[]> al = new ArrayList<int[]>();
                al.add(arraylist.get(0));
                return al;
            }
        }

        ArrayList<int[]> ret = new ArrayList<int[]>();
        for (int i = 0; i < arraylist.size(); i++) {
            if (i % 2 == 1) {
                if (i == arraylist.size() - 1) {
                    if (!(arraylist.get(i - 1)[1] > arraylist.get(i)[0])) {
                        ret.add(arraylist.get(i));
                    }
                }
                else {
                    if (!(arraylist.get(i - 1)[1] > arraylist.get(i)[0] ||
                            arraylist.get(i)[1] > arraylist.get(i + 1)[0])) {
                        ret.add(arraylist.get(i));
                    }
                }
            }
            else {
                ret.add(arraylist.get(i));
            }
        }
        return ret;
    }

    /**
     * Determines whether or not the character is one
     * of the designated word separator characters.
     *
     * @param ch The specified character.
     * @return a boolean describing whether or not the
     * character is one of the designated word separator
     * characters.
     */
    private boolean isSeparator(char ch) {
        for (int k = 0; k < WORD_SEPARATORS.length; k++) {
            if (ch == WORD_SEPARATORS[k]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the stage of the current Find/Replace
     * Window.
     *
     * @return the stage of the current Find/Replace
     * Window.
     */
    public Stage getStage() {
        return ((Stage) (closeButton.getScene().getWindow()));
    }

    public void setUseConsole(boolean useIOConsole) {
        useConsole = useIOConsole;
        if (replaceButton != null && replaceAllButton != null) {
            replaceButton.setDisable(useConsole);
            replaceAllButton.setDisable(useConsole);
        }
    }

    /**
     * Opens a new find dialog and returns its FindReplaceController.
     *
     * @param m - The Mediator of the current application.
     * @param useConsole - true if you are searching the console instead of the code area
     * @return - The FindReplaceController of the new Find/Replace Dialog.
     */
    public static FindReplaceController openFindReplaceDialog(Mediator m, boolean
            useConsole) {
        final FindReplaceController frc = new FindReplaceController(m);
        frc.setUseConsole(useConsole);
        FXMLLoader fxmlLoader = new FXMLLoader(frc.getClass().getResource(
                "FindReplaceFXML.fxml"));
        fxmlLoader.setController(frc);
        final Stage dialogStage = new Stage();
        Pane dialogRoot;
        try {
            dialogRoot = fxmlLoader.load();
        } catch (IOException e) {
            System.out.println("Exception opening FindReplaceFXML.fxml: " + e);
            return null;
        }

        Scene dialogScene = new Scene(dialogRoot);
        dialogStage.setScene(dialogScene);
        dialogStage.initModality(Modality.NONE);
        dialogStage.setTitle("Find");
        dialogStage.setX(810);
        dialogStage.setY(50);
        dialogStage.show();

        dialogStage.setOnHidden(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent arg0) {
                frc.desktop.setFindReplaceController(null);
            }
        });

        dialogStage.addEventFilter(
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

        return frc;
    }

}