/**
 * author: Jinghui Yu
 * last edit data: 6/3/2013
 */

package cpusim.gui.util.table;

import cpusim.model.util.conversion.ConvertStrings;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;

import static com.google.common.base.Preconditions.checkNotNull;


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

            textField.requestFocus();
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
        } else if (isEditing()) {
            textField.setText(getItem().toString());
            setText(null);
            setGraphic(textField);
        } else {
            setText(getItem().toString());
            setGraphic(null);
            setTooltip(new Tooltip("Binary: " + Long.toBinaryString(item) +
                    System.getProperty("line.separator") +
                    "Decimal: " + item +
                    System.getProperty("line.separator") +
                    "Hex: " + Long.toHexString(item)));
        }
    }


    private void tryCommitEdit() {
        checkNotNull(textField);

        try {
            long newLong = ConvertStrings.toLong(textField.getText());
            commitEdit(newLong);
        } catch (NumberFormatException e) {
            // Show an error
            textField.requestFocus();
            textField.setStyle("-fx-background-color:red;");
            textField.setTooltip(new Tooltip("You need to enter an integer"));
        }
    }

    /**
     * creates a text field with listeners so that that edits will be committed
     * at the proper time
     */
    private void createTextField() {
        textField = new TextField(getItem().toString());

        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { // focus moved away from this field
                tryCommitEdit();
            }
        });
        textField.setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                tryCommitEdit();
                t.consume();
            } else if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
                t.consume();
            }
        });
    }
}
