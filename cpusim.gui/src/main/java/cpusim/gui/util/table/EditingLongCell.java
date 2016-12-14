/**
 * author: Jinghui Yu
 * last edit data: 6/3/2013
 */

package cpusim.gui.util.table;

import cpusim.model.util.conversion.ConvertStrings;
import cpusim.util.Dialogs;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.stage.Window;


/**
 * An editable cell class that allows the user to modify the long integer in the cell.
 * It accepts any long (positive or negative).
 */

public class EditingLongCell<T> extends TableCell<T, Long> {
    private TextField textField;

    public EditingLongCell() {
    }

    /**
     * What happens when the user starts to edit the table cell.  Namely that
     * a text field is created
     */
    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            createTextField();
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }
    }


    /**
     * What happens when the user cancels while editing a table cell.  Namely the
     * graphic is set to null and the text is set to the proper value
     */
    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText(String.valueOf(getItem()));
        setGraphic(null);
    }

        /**
         * updates the Long in the table cell
         *
         * @param item  used for the parent method
         * @param empty used for the parent method
         */
    @Override
    public void updateItem(Long item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        }
        else if (isEditing()) {
            if (textField != null) {
                textField.setText(getString());
            }
            setText(null);
            setGraphic(textField);
        }
        else {
            setText(getString());
            setGraphic(null);
            setTooltip(new Tooltip("Binary: " + Long.toBinaryString(item) +
                    System.getProperty("line.separator") +
                    "Decimal: " + item +
                    System.getProperty("line.separator") +
                    "Hex: " + Long.toHexString(item)));
        }
    }


    /**
     * creates a text field with listeners so that that edits will be committed
     * at the proper time
     */
    private void createTextField() {
        textField = new TextField(getString());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            Window parentDialog = textField.getScene() == null ? null :
                                    textField.getScene().getWindow();
            if (!newValue) { // focus moved away from this field
                try {
                    long newLong = ConvertStrings.toLong(textField.getText());
                    commitEdit(newLong);
                } catch (NumberFormatException e) {
                    //didn't work because of issues with the focus
                    //textField.requestFocus();
                    //textField.setStyle("-fx-background-color:red;");
                    //textField.setTooltip(new Tooltip("You need to enter an integer"));
                    Dialogs.createErrorDialog(parentDialog, "Integer Error",
                            "This column requires integer values").showAndWait();
                    cancelEdit();
                }
            }
        });
        textField.setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                try {
                    long newLong = ConvertStrings.toLong(textField.getText());
                    commitEdit(newLong);
                } catch (NumberFormatException e) {
                    textField.setStyle("-fx-background-color:red;");
                    textField.setTooltip(new Tooltip("You need to enter an integer"));
                    //The following code crashes the program
                    //Dialogs.showErrorDialog(
                    //        (Stage)textField.getScene().getWindow(),
                    //        "This column requires integer values",
                    //        "Error Dialog", "title");
                    //cancelEdit();
                }
            }
            else if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });
    }

    /**
     * returns a string value of the item in the table cell
     *
     * @return a string value of the item in the table cell
     */
    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }
}
