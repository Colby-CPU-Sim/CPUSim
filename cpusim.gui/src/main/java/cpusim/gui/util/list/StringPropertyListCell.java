package cpusim.gui.util.list;

import javafx.beans.property.StringProperty;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;

/**
 * {@link ListCell} implementation that is bound to a {@link StringProperty} within an {@code Item}.
 *
 * @param <Item> Underlying class that this cell supports
 * @since 2016-12-05
 */
public class StringPropertyListCell<Item> extends ListCell<Item> {

    /**
     * Property callback that will get a {@link StringProperty} from an {@code Item} value.
     */
    private final Callback<Item, StringProperty> getProperty;

    private final TextField textField;

    @SuppressWarnings("unchecked")
    public StringPropertyListCell(Callback<? extends Item, ? extends StringProperty> getPropertyCallback) {
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
                final Item item = getItem();
                getProperty.call(item).setValue(textField.getText());
                commitEdit(item);
            }
        });

        textField.setOnAction(e -> {
            getProperty.call(getItem()).setValue(textField.getText());
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        });

        setGraphic(textField);
    }

    @Override
    protected void updateItem(Item item, boolean empty) {
        super.updateItem(item, empty);

        if (isEditing() && item != null) {
            textField.setText(getProperty.call(item).getValue());
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        } else {
            setContentDisplay(ContentDisplay.TEXT_ONLY);

            textProperty().unbind();
            if (empty || item == null) {
                setText(null);
            } else {
                textProperty().bind(getProperty.call(item));
            }
        }
    }

    @Override
    public void startEdit() {
        super.startEdit();

        textField.setText(getProperty.call(getItem()).getValue());
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        textField.requestFocus();
        textField.selectAll();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        textProperty().bind(getProperty.call(getItem()));
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
