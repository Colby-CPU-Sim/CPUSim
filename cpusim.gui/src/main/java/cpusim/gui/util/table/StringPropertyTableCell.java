package cpusim.gui.util.table;

import javafx.beans.property.StringProperty;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;

/**
 * {@link ListCell} implementation that is bound to a {@link StringProperty} within an {@code Item}.
 *
 * @param <Item> Underlying class that this cell supports
 * @since 2016-12-05
 */
public class StringPropertyTableCell<Item> extends TableCell<Item, String> {

    /**
     * Property callback that will get a {@link StringProperty} from an {@code Item} value.
     */
    private final Callback<Item, StringProperty> getProperty;

    private final TextField textField;

    @SuppressWarnings("unchecked")
    public StringPropertyTableCell(Callback<? extends Item, ? extends StringProperty> getPropertyCallback) {
        this.getProperty = (Callback<Item, StringProperty>)getPropertyCallback;

        textField = new TextField();

        textField.setOnKeyPressed(e -> {
            if (isEditing() && e.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
                e.consume();
            }
        });

        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                final String text = textField.getText();
                getProperty.call(getRowItem()).setValue(text);
                commitEdit(text);
            }
        });

        textField.setOnAction(e -> {
            getProperty.call(getRowItem()).setValue(textField.getText());
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        });

        setGraphic(textField);
    }

    /**
     * Gets the current row's item.
     * @return
     */
    private Item getRowItem() {
        return getTableView().getItems().get(getTableRow().getIndex());
    }

    @Override
    protected void updateItem(String value, boolean empty) {
        super.updateItem(value, empty);

        if (isEditing() && value != null) {
            textField.setText(getProperty.call(getRowItem()).getValue());
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        } else {
            setContentDisplay(ContentDisplay.TEXT_ONLY);

            textProperty().unbind();
            if (empty || value == null) {
                setText(null);
            } else {
                textProperty().bind(getProperty.call(getRowItem()));
            }
        }
    }

    @Override
    public void startEdit() {
        super.startEdit();

        textField.setText(getProperty.call(getRowItem()).getValue());
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        textField.requestFocus();
        textField.selectAll();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        textProperty().bind(getProperty.call(getRowItem()));
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    /**
     * Get the backing {@link TextField} used when editing.
     * @return Non-{@code null} text field used to edit the content.
     */
    public TextField getTextField() {
        return textField;
    }
}
