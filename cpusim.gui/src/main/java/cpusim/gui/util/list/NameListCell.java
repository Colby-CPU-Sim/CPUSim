package cpusim.gui.util.list;

import cpusim.model.util.NamedObject;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

/**
 * {@link javafx.scene.control.ListCell} extension that modifies the {@link NamedObject#nameProperty()} and sets it.
 *
 * @since 2016-12-04
 */
public class NameListCell<T extends NamedObject> extends ListCell<T> {

    private final TextField textField;

    public NameListCell() {
        textField = new TextField();

        textField.setOnKeyPressed(e -> {
            if (isEditing() && e.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
                e.consume();
            }
        });

        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                final T item = getItem();
                item.setName(textField.getText());
                commitEdit(item);
            }
        });

        textField.setOnAction(e -> {
            getItem().setName(textField.getText());
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        });

        setGraphic(textField);
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (isEditing()) {
            textField.setText(item.getName());
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        } else {
            setContentDisplay(ContentDisplay.TEXT_ONLY);

            textProperty().unbind();
            if (empty) {
                setText(null);
            } else {
                textProperty().bind(item.nameProperty());
            }
        }
    }

    @Override
    public void startEdit() {
        super.startEdit();

        textField.setText(getItem().getName());
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        textField.requestFocus();
        textField.selectAll();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        textProperty().bind(getItem().nameProperty());
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }
}
