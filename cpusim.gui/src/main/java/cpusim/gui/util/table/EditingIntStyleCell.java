/**
 * auther: Jinghui Yu
 * last edit data: 6/3/2013
 */

package cpusim.gui.util.table;

import cpusim.gui.desktop.FontData;
import cpusim.model.util.Convert;
import cpusim.util.Dialogs;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * An editable cell class that allows the user to modify the integer in the cell.
 */

public class EditingIntStyleCell<T> extends TableCell<T, Integer> {

    private TextField textField;
    protected FontData styleInfo;

    public EditingIntStyleCell(FontData style) {
        this.styleInfo = style;
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
     * updates the Integer in the table cell
     *
     * @param item  used for the parent method
     * @param empty used for the parent method
     */
    @Override
    public void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);

        styleInfo.setFontAndBackground(this);

        if (empty) {
            setText(null);
            setGraphic(null);
        }
        else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                }
                setText(null);
                setGraphic(textField);
            }
            else {
                setText(getString());
                setGraphic(null);
            }
        }
    }

    /**
     * creates a text field with listeners so that that edits will be committed
     * at the proper time
     */
    private void createTextField() {
        textField = new TextField(getString());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0,
                                Boolean arg1, Boolean arg2) {
                if (!arg2) {
                    try {
                        int newInt = (int) Convert.fromAnyBaseStringToLong(textField
                                .getText());
                        if (newInt >= 0) {
                            try {
                                commitEdit(newInt);
                                textField.setTooltip(new Tooltip("Binary: " +
                                        Integer.toBinaryString(newInt) +
                                        System.getProperty("line.separator") + "Hex: " +
                                        Integer.toHexString(newInt)));
                            } catch (AssertionError e) {
                                if (textField.getScene() != null) {
                                    Dialogs.createErrorDialog(textField.getScene().getWindow(),
                                            "Integer Error", "Entered value out of range").showAndWait();
                                    cancelEdit();
                                }
                            }
                        }
                        else {
                            if (textField.getScene() != null) {
                                Dialogs.createErrorDialog(textField.getScene().getWindow(),
                                        "Integer Error", "This column requires "
                                                + "positive integer values").showAndWait();
                                cancelEdit();
                            }
                        }
                    } catch (NumberFormatException e) {
                        //didn't work because of issues with the focus
                        //textField.requestFocus();
                        //textField.setStyle("-fx-background-color:red;");
                        //textField.setTooltip(new Tooltip("You need to enter an
                        // integer"));
                        if (textField.getScene() != null) {
                            Dialogs.createErrorDialog(textField.getScene().getWindow(),
                                    "Integer Error", "This column requires "
                                            + "positive integer values").showAndWait();
                            cancelEdit();
                        }
                    }
                }
            }
        });
        textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {

                if (t.getCode() == KeyCode.ENTER) {

                    try {
                        int newInt = (int) Convert.fromAnyBaseStringToLong(textField
                                .getText());
                        if (newInt >= 0) {
                            try {
                                commitEdit(newInt);
                                textField.setTooltip(new Tooltip("Binary: " +
                                        Integer.toBinaryString(newInt) +
                                        System.getProperty("line.separator") + "Hex: " +
                                        Integer.toHexString(newInt)));
                            } catch (AssertionError e) {
                                textField.setStyle("-fx-background-color:red;");
                                textField.setTooltip(new Tooltip("This value is"
                                        + "out of range"));
                            }
                        }
                        else {
                            textField.setStyle("-fx-background-color:red;");
                            textField.setTooltip(new Tooltip("You need to"
                                    + " enter a non-negative integer"));
                        }
                    } catch (NumberFormatException e) {
                        textField.setStyle("-fx-background-color:red;");
                        textField.setTooltip(new Tooltip("You need to enter a "
                                + "positive integer"));
                        //The following code crashes the program
                        //Dialogs.showErrorDialog(
                        //            (Stage)textField.getScene().getWindow(),
                        //            "This column requires integer values",
                        //            "Error Dialog", "title");
                        //cancelEdit();
                    }
                }
                else if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
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
