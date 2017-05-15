package cpusim.model.module;

import javafx.beans.property.ReadOnlyIntegerProperty;

/**
 * Denotes that a {@link Module} has a bit width.
 *
 * @param <T> Used to force this only be used on Module types. If {@link Module} was an interface, this should just
 *           inherit from that, but it's not.
 *
 * @since 2016-11-14
 */
public interface Sized<T extends Module<T>> {

    static final String PROPERTY_NAME_WIDTH = "width";

    /**
     * Get the width of the {@link Sized} component.
     * @return Width in <em>bytes</em>.
     */
    int getWidth();

    /**
     * Get the width of the {@link Sized} component as a property.
     * @return Non-{@code null} property
     */
    ReadOnlyIntegerProperty widthProperty();
}
