package cpusim.gui.util.table;

import cpusim.model.util.NamedObject;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Contains classes for implementing {@link javafx.scene.control.TableCell} factories with
 * {@link cpusim.model.util.NamedObject} bases.
 */
@ParametersAreNonnullByDefault
public abstract class NamedObjectCellFactories {

    private NamedObjectCellFactories() {

    }

    public static class ComboBox<S, T extends NamedObject> implements Callback<TableColumn<S, T>, TableCell<S, T>> {

        private final ObservableValue<ObservableList<T>> itemsProperty;

        public ComboBox(ObservableValue<ObservableList<T>> itemsProperty) {
            this.itemsProperty = itemsProperty;
        }

        public ComboBox() {
            this.itemsProperty = null;
        }

        @Override
        public TableCell<S, T> call(TableColumn<S, T> param) {
            ComboBoxTableCell<S, T> cb = new ComboBoxTableCell<>();
            cb.setConverter(new NamedObject.NameStringConverter<>());

            if (itemsProperty != null) {
                cb.getItems().addAll(itemsProperty.getValue());
            }

            return cb;
        }
    }

    public static class TextField<S, T extends NamedObject> implements Callback<TableColumn<S, T>, TableCell<S, T>> {

        @Override
        public TableCell<S, T> call(TableColumn<S, T> param) {
            TextFieldTableCell<S, T> cb = new TextFieldTableCell<>();
            cb.setConverter(new NamedObject.NameStringConverter<>());
            return cb;
        }
    }

}

