/**
 * Jinghui Yu
 * Created on 3/3/2015
 *
 * Dialogs class is used to create different kinds dialogs including error, information, confirmation and choices.
 */

package cpusim.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Window;

import java.util.List;

public class Dialogs {

    /**
     * Creates error dialog for saving, closing, new actions.
     *
     * @param window
     * @param header  head text of the confirmation dialog
     * @param content content of the confirmation dialog
     * @return an dialog object
     */
    public static Alert createErrorDialog(Window window, String header, String content) {
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        if (window != null)
            dialog.initOwner(window);
        dialog.setTitle("CPU Sim");
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        return dialog;
    }

    /**
     * Creates information dialog for saving, closing, new actions.
     *
     * @param window
     * @param header  head text of the confirmation dialog
     * @param content content of the confirmation dialog
     * @return an dialog object
     */
    public static Alert createInformationDialog(Window window, String header, String content) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        if (window != null)
            dialog.initOwner(window);
        dialog.setTitle("CPU Sim");
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        return dialog;
    }

    /**
     * Creates confirmation dialog.
     *
     * @param window
     * @param header  head text of the confirmation dialog
     * @param content content of the confirmation dialog
     * @return an dialog object
     */
    public static Alert createConfirmationDialog(Window window, String header, String content) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        if (window != null)
            dialog.initOwner(window);
        dialog.setTitle("CPU Sim");
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        return dialog;
    }

    /**
     * Creates customized confirmation dialog for saving, closing, new actions.
     *
     * @param window
     * @param header  head text of the confirmation dialog
     * @param content content of the confirmation dialog
     * @return an dialog object
     */
    public static Alert createCustomizedConfirmationDialog(Window window, String header, String content) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        if (window != null)
            dialog.initOwner(window);
        dialog.setTitle("CPU Sim");
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        return dialog;
    }

    /**
     * Creates choice dialog for saving, closing, new actions.
     *
     * @param window
     * @param header  head text of the confirmation dialog
     * @param content content of the confirmation dialog
     * @return an dialog object
     */
    public static ChoiceDialog<String> createChoiceDialog(Window window, String header, String content,
                                                          String initC, List<String> choices) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(initC, choices);
        if (window != null)
            dialog.initOwner(window);
        dialog.setTitle("CPU Sim");
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        return dialog;
    }

    /**
     * Creates text input dialog.
     *
     * @param window
     * @param header  head text of the confirmation dialog
     * @param content content of the confirmation dialog
     * @return an dialog object
     */
    public static TextInputDialog createTextInputDialog(Window window, String header, String content) {
        TextInputDialog dialog = new TextInputDialog();
        if (window != null)
            dialog.initOwner(window);
        dialog.setTitle("CPU Sim");
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        return dialog;
    }

    /**
     * Creates warning dialog.
     *
     * @param window
     * @param header  head text of the confirmation dialog
     * @param content content of the confirmation dialog
     * @return an dialog object
     */
    public static Alert createWariningDialog(Window window, String header, String content) {
        Alert dialog = new Alert(Alert.AlertType.WARNING);
        if (window != null)
            dialog.initOwner(window);
        dialog.setTitle("CPU Sim");
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        return dialog;
    }
}
