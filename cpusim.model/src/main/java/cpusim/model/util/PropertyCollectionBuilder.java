package cpusim.model.util;

import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Allows for easier building of a {@link ReadOnlySetProperty} instance.
 *
 * @since 2016-12-08
 */
public class PropertyCollectionBuilder<T extends IdentifiedObject> {

    private final ObservableMap<UUID, T> backingMap;
    private final ObservableSet<T> backingValues;

    public PropertyCollectionBuilder() {
        backingMap = FXCollections.observableHashMap();
        backingValues = FXCollections.observableSet(new HashSet<>());

        // bind the values to the value set from the map, unfortunately, the "values" from the ObservableMap
        // are just a collection, it's not observable, so we have to manually keep track, oh well.
        backingMap.addListener(
                (MapChangeListener<UUID, T>) c -> {
                    if (c.wasRemoved()) {
                        backingValues.remove(c.getValueRemoved());
                    }

                    if (c.wasAdded()) {
                        backingValues.add(c.getValueAdded());
                    }
                });
    }


    private <U extends T> void onValueChanged(ObservableValue<? extends U> _ignored, U oldValue, U newValue) {
        if (oldValue != null && newValue != null
                && oldValue.getID().equals(newValue.getID())) {
            // someone reset the value to the same value
            // don't trigger the changes
            return;
        }

        if (oldValue != null) {
            backingMap.remove(oldValue.getID());
        }

        if (newValue != null) {
            backingMap.put(newValue.getID(), newValue);
        }
    }

    /**
     * Adds a {@link ReadOnlyListProperty} to the backing values. This method is smarter than the
     * {@link #addAll(Collection)} as it will use {@link ReadOnlyListProperty#addListener(ChangeListener)} to back
     * the components.
     *
     * @param setComponents Non-{@code null} components to read from.
     * @return {@code this}
     */
    public PropertyCollectionBuilder<T> addAll(ReadOnlyListProperty<? extends T> setComponents) {
        setComponents.addListener((ListChangeListener<T>) c -> {
            while (c.next()) {
                if (c.wasRemoved()) {
                    c.getRemoved().stream()
                            .map(IdentifiedObject::getID)
                            .forEach(backingMap::remove);
                } else if (c.wasAdded()) {
                    backingMap.putAll(c.getAddedSubList().stream()
                                    .collect(Collectors.toMap(IdentifiedObject::getID, Function.identity())));
                }
            }
        });

        for (T value : setComponents) {
            backingMap.put(value.getID(), value);
        }

        return this;
    }

    /**
     * Adds a {@link ReadOnlySetProperty} to the backing values. This method is smarter than the
     * {@link #addAll(Collection)} as it will use {@link ReadOnlySetProperty#addListener(ChangeListener)} to back
     * the components.
     *
     * @param setComponents Non-{@code null} components to read from.
     * @return {@code this}
     */
    public PropertyCollectionBuilder<T> addAll(ReadOnlySetProperty<? extends T> setComponents) {
        setComponents.addListener((SetChangeListener<T>) c -> {
            if (c.wasRemoved()) {
                T rem = c.getElementRemoved();
                backingMap.remove(rem.getID());
            }

            if (c.wasAdded()) {
                T add = c.getElementAdded();
                backingMap.put(add.getID(), add);
            }
        });

        for (T value : setComponents) {
            backingMap.put(value.getID(), value);
        }

        return this;
    }

    /**
     * Adds all of the {@link ReadOnlyProperty} values from the colletion, individually binding them all to the backing
     * {@link Set}.
     *
     * @param components Non-{@code null} components to read from.
     * @return {@code this}
     */
    public PropertyCollectionBuilder<T> addAll(Collection<? extends ReadOnlyProperty<? extends T>> components) {
        components.forEach(c -> {
            c.addListener(this::onValueChanged);

            if (c.getValue() != null) {
                final T v = c.getValue();
                backingMap.put(v.getID(), v);
            }
        });
        return this;
    }


    /**
     * Adds a single component to the backing {@link Set}.
     *
     * @param component Component bound.
     * @return {@code this}
     */
    public PropertyCollectionBuilder<T> add(ReadOnlyProperty<? extends T> component) {
        component.addListener(this::onValueChanged);

        if (component.getValue() != null) {
            final T v = component.getValue();
            backingMap.put(v.getID(), v);
        }

        return this;
    }

    /**
     * Creates a ReadOnly property to a {@link Set} of {@link MachineComponent} values. The {@code Set} changes as
     * the properties bound via the {@link #add(ReadOnlyProperty)} method change. This uses the default values for
     * {@code name} and {@code bean} from {@link ReadOnlySetWrapper}.
     *
     * @return Set backed by properties added in {@link #add(ReadOnlyProperty)}
     *
     * @see #add(ReadOnlyProperty)
     * @see #addAll(ReadOnlySetProperty)
     * @see #addAll(Collection)
     */
    public ReadOnlySetProperty<T> buildSet() {
        return new ReadOnlySetWrapper<>(backingValues);
    }

    /**
     * Creates a ReadOnly property to a {@link Set} of {@link MachineComponent} values. The {@code Set} changes as
     * the properties bound via the {@link #add(ReadOnlyProperty)} method change.
     *
     * @param bean The bean this is bound to
     * @param name Name of the property in the {@code bean}
     *
     * @return Set backed by properties added in {@link #add(ReadOnlyProperty)}
     *
     * @see #add(ReadOnlyProperty)
     * @see #addAll(ReadOnlySetProperty)
     * @see #addAll(Collection)
     */
    public ReadOnlySetProperty<T> buildSet(Object bean, String name) {
        return new ReadOnlySetWrapper<>(bean, name, backingValues);
    }

    /**
     * Gets the {@link java.util.Map} from {@link UUID} to {@code <T>} of the elements that were added.
     * @param bean The bean this is bound to
     * @param name Name of the property in the {@code bean}
     *
     * @return Map backed by properties added in {@link #add(ReadOnlyProperty)}
     */
    public ReadOnlyMapProperty<UUID, T> buildMap(Object bean, String name) {
        return new ReadOnlyMapWrapper<>(bean, name, backingMap);
    }

}
