/**
 * auther: Jinghui Yu
 * last edit date: 6/3/2013
 */

package cpusim.gui.util.table;

import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An editable cell class that allows the user to modify the string in the cell.
 */
public class EditingStrCell<T> extends TableCell<T, String> {

    private TextField textField;
    protected boolean valid;

    public EditingStrCell() {

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

        setText(getItem());
        setGraphic(null);
    }

    /**
     * updates the String in the table cell
     * @param item used for the parent method
     * @param empty used for the parent method
     */
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else if (isEditing()) {
            textField.setText(getItem());

            setText(getItem());
            setGraphic(textField);
        } else {
            setText(getItem());
            setGraphic(null);
        }
    }

    /**
     * creates a text field with listeners so that that edits will be committed 
     * at the proper time
     */
    private void createTextField() {
        checkNotNull(getItem());

        textField = new TextField(getItem());

        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                commitEdit(textField.getText());
            }
        });

        textField.setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                commitEdit(textField.getText());
                t.consume();
            } else if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
                t.consume();
            }
        });
    }
}
