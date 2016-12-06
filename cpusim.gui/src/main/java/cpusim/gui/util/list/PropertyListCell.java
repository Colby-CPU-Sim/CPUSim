package cpusim.gui.util.list;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * {@link ListCell} implementation that is bound to a {@link StringProperty} within an {@code Item}.
 *
 * @param <Item> Underlying class that this cell supports
 * @since 2016-12-05
 */
public class PropertyListCell<Item, Type> extends ListCell<Item> {

    /**
     * Property callback that will get a {@link StringProperty} from an {@code Item} value.
     */
    private final Callback<Item, Property<Type>> getProperty;

    private final StringConverter<Type> converter;

    private final TextField textField;

    @SuppressWarnings("unchecked")
    public PropertyListCell(Callback<? extends Item, ? extends Property<Type>> getPropertyCallback,
                            StringConverter<? extends Type> converter) {
        this.getProperty = (Callback<Item, Property<Type>>)getPropertyCallback;
        this.converter = (StringConverter<Type>) converter;

        textField = new TextField();

        textField.setOnKeyPressed(e -> {
            if (isEditing() && e.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
                e.consume();
            }
        });

        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                getProperty.call(getItem()).setValue(getConvertedText());
                commitEdit(getItem());
            }
        });

        textField.setOnAction(e -> {
            getProperty.call(getItem()).setValue(getConvertedText());
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        });

        setGraphic(textField);
    }

    private String getConvertedType() {
        return converter.toString(getProperty.call(getItem()).getValue());
    }

    private StringBinding getConvertedTypeBinding() {
        return Bindings.createStringBinding(this::getConvertedType, getProperty.call(getItem()));
    }

    private Type getConvertedText() {
        return converter.fromString(textField.getText());
    }

    @Override
    protected void updateItem(Item value, boolean empty) {
        super.updateItem(value, empty);

        if (isEditing() && value != null) {
            textField.setText(getConvertedType());
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        } else {
            setContentDisplay(ContentDisplay.TEXT_ONLY);

            textProperty().unbind();
            if (empty || value == null) {
                setText(null);
            } else {
                textProperty().bind(getConvertedTypeBinding());
            }
        }
    }

    @Override
    public void startEdit() {
        super.startEdit();

        textField.setText(getConvertedType());
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        textField.requestFocus();
        textField.selectAll();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        textProperty().bind(getConvertedTypeBinding());
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
