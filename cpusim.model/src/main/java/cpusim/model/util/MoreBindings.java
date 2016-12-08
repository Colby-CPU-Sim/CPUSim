package cpusim.model.util;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.fxmisc.easybind.EasyBind;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Binding utilities
 *
 * @since 2016-12-07
 */
public abstract class MoreBindings {

    private MoreBindings() {
        // stop construction
    }

    /**
     * Short-cut method to create a {@link ReadOnlyObjectProperty} that is immediately bound to an
     * {@link ObjectBinding}.
     *
     * @param bean Java bean the property belongs to
     * @param name Name of the property in the bean
     * @param toBindTo Non-{@code null} value to bind to
     * @param <T> Type for the Property
     *
     * @return Read-Only proeprty that is immediately bound to the passed {@link ObjectBinding}
     *
     * @see #createReadOnlyBoundProperty(Object, String, ObservableValue)
     * @see ObjectBinding
     */
    public static <T> ReadOnlyObjectProperty<T> createReadOnlyBoundProperty(Object bean,
                                                                            String name,
                                                                            ObservableValue<? extends T> toBindTo) {
        checkNotNull(toBindTo);
        ObjectProperty<T> out = new SimpleObjectProperty<>(bean, name, null);
        out.bind(toBindTo);

        return out;
    }

    /**
     * Delegate to {@link #createReadOnlyBoundProperty(Object, String, ObservableValue)} setting the {@code bean} to
     * {@code null} and {@code name} to {@code ""}.
     *
     * @param toBindTo Non-{@code null} value to bind to
     * @param <T> Type for the Property
     * @return Read-Only proeprty that is immediately bound to the passed {@link ObservableValue}
     *
     * @see #createReadOnlyBoundProperty(Object, String, ObservableValue)
     */
    public static <T> ReadOnlyObjectProperty<T> createReadOnlyBoundProperty(ObservableValue<? extends T> toBindTo) {
        return createReadOnlyBoundProperty(null, "", toBindTo);
    }

    public static <T, U> ObservableList<U> flatMapProperties(
            Predicate<ReadOnlyObjectProperty<T>> filter,
            Function<T, ? extends U> mapper,
            ObservableList<ReadOnlyObjectProperty<T>> properties) {
        ObservableList<T> values = EasyBind.map(properties.filtered(filter), ReadOnlyObjectProperty::getValue);
        return EasyBind.map(values, mapper);
    }

    public static <T, U> ObservableList<U> flatMapProperties(
            Function<T, ? extends U> mapper,
            ObservableList<ReadOnlyObjectProperty<T>> properties) {
        return flatMapProperties(o -> o.getValue() != null, mapper, properties);
    }

    public static <T> ObservableList<? extends ObservableValue<T>> nonNullPropertyList(ObservableList<? extends ObservableValue<T>> list) {
        return list.filtered(o -> o.getValue() != null);
    }
}
