/**
 * auther: Jinghui Yu
 * last edit date: 6/3/2013
 */

package cpusim.gui.util.table;

import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

/**
 * An editable cell class that allows the user to modify the string in the cell.
 */
public class EditingStrCell<T> extends TableCell<T, String> {

    protected TextField textField;
    protected int cellSize;
    protected boolean valid;
    protected String errorMessage;

    public EditingStrCell(int cellSize) {
        this.cellSize = cellSize;
    }

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

        setText((String) getItem());
        setGraphic(null);
    }

    /**
     * updates the String in the table cell
     * @param item used for the parent method
     * @param empty used for the parent method
     */
    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(getString());
                setGraphic(null);
            }
        }
    }

    /**
     * creates a text field with listeners so that that edits will be committed 
     * at the proper time
     */
    protected void createTextField() {
        textField = new TextField(getString());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textField.focusedProperty().addListener((arg0, arg1, arg2) -> {
            if (!arg2) {
                commitEdit(textField.getText());
            }
        });
        textField.setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                commitEdit(textField.getText());
            } else if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });
    }

    /**
     * returns a string value of the item in the table cell
     * @return a string value of the item in the table cell
     */
    protected String getString() {
        return getItem() == null ? "" : getItem().toString();
    }
}
