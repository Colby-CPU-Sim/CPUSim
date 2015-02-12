/**
 * auther: Jinghui Yu
 * last edit date: 6/3/2013
 */

package cpusim.gui.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * An editable cell class that allows the user to modify the string in the cell.
 */
public class EditingStrStyleCell<T> extends TableCell<T, String> {

    protected TextField textField;
    protected int cellSize;
    protected boolean valid;
    protected String errorMessage;
    protected String color;

    public EditingStrStyleCell(String color) {
        this.color = color;
    }

    public EditingStrStyleCell() {

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

        setStyle(color);
        
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
        textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0,
                                Boolean arg1, Boolean arg2) {
                if (!arg2) {
                    commitEdit(textField.getText());
                }
            }
        });
        textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (t.getCode() == KeyCode.ENTER) {
                    commitEdit(textField.getText());
                } else if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
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
