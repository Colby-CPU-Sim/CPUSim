package cpusim.gui.util.list;

import javafx.beans.property.StringProperty;
import javafx.scene.control.ListCell;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;

/**
 * {@link ListCell} implementation that is bound to a {@link StringProperty} within an {@code Item}.
 *
 * @param <Item> Underlying class that this cell supports
 * @since 2016-12-05
 */
public class StringPropertyListCell<Item> extends PropertyListCell<Item, String> {

    @SuppressWarnings("unchecked")
    public StringPropertyListCell(Callback<? extends Item, ? extends StringProperty> getPropertyCallback) {
        super(getPropertyCallback, new DefaultStringConverter());
    }
}
