/**
 * Jinghui Yu
 * Created on 3/3/2015
 */

package cpusim.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Region;
import javafx.stage.Window;

import java.util.List;

/**
 * A factory class for generating standard dialogs, such as error dialogs.
 */
public class Dialogs {

    /**
     * Creates error dialog for saving, closing, new actions.
     *
     * @param window owning window or null if there is none
     * @param header  head text of the confirmation dialog
     * @param content content of the confirmation dialog
     * @return an dialog object
     */
    public static Alert createErrorDialog(Window window, String header, String content) {
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        initializeDialog(dialog, window, header, content);

        return dialog;
    }

    /**
     * Creates information dialog for saving, closing, new actions.
     *
     * @param window owning window or null if there is none
     * @param header  head text of the confirmation dialog
     * @param content content of the confirmation dialog
     * @return an dialog object
     */
    public static Alert createInformationDialog(Window window, String header, String content) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        initializeDialog(dialog, window, header, content);

        return dialog;
    }

    /**
     * Creates confirmation dialog.
     *
     * @param window owning window or null if there is none
     * @param header  head text of the confirmation dialog
     * @param content content of the confirmation dialog
     * @return an dialog object
     */
    public static Alert createConfirmationDialog(Window window, String header, String content) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        initializeDialog(dialog, window, header, content);

        return dialog;
    }

    /**
     * Creates choice dialog for saving, closing, new actions.
     *
     * @param window owning window or null if there is none
     * @param header  head text of the confirmation dialog
     * @param content content of the confirmation dialog
     * @return an dialog object
     */
    public static ChoiceDialog<String> createChoiceDialog(Window window, String header, String content,
                                                          String initC, List<String> choices) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(initC, choices);
        initializeDialog(dialog, window, header, content);

        return dialog;
    }

    /**
     * Creates text input dialog.
     *
     * @param window owning window or null if there is none
     * @param header  head text of the confirmation dialog
     * @param content content of the confirmation dialog
     * @return an dialog object
     */
    public static TextInputDialog createTextInputDialog(Window window, String header, String content) {
        TextInputDialog dialog = new TextInputDialog();
        initializeDialog(dialog, window, header, content);

        return dialog;
    }

    /**
     * Creates warning dialog.
     *
     * @param window owning window or null if there is none
     * @param header  head text of the confirmation dialog
     * @param content content of the confirmation dialog
     * @return an dialog object
     */
    public static Alert createWariningDialog(Window window, String header, String content) {
        Alert dialog = new Alert(Alert.AlertType.WARNING);
        initializeDialog(dialog, window, header, content);

        return dialog;
    }

    /**
     * Initialized a dialog with the given owner window, and String header and content
     * @param dialog dialog box to initialize
     * @param window owning window or null if there is none
     * @param header dialog header
     * @param content dialog content
     */
    public static void initializeDialog(Dialog dialog, Window window, String header, String content){
        if (window != null)
            dialog.initOwner(window);
        dialog.setTitle("CPU Sim");
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        // Allow dialogs to resize for Linux
        // https://bugs.openjdk.java.net/browse/JDK-8087981
        dialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

    }
}
