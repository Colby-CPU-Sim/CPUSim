package cpusim.gui.util;

import cpusim.model.Machine;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;

import java.util.Optional;

/**
 * Defines a type that requires a {@link Machine} and links to it via an {@link ObjectProperty}.
 *
 * @since 2016-12-02
 */
public interface MachineBound {

    /**
     * Get the {@link Machine} property that this object is bound to.
     *
     * @return Read-only property for the {@link Machine}, this may be empty, but never {@code null}.
     */
    ObjectProperty<Machine> machineProperty();

    /**
     * Get the stored {@link Machine}, it will be {@link Optional#empty()} if no instance is present.
     * @return Non-{@code null}, but possibly empty, {@code Optional} value.
     */
    default Optional<Machine> getMachine() {
        return Optional.ofNullable(machineProperty().getValue());
    }
}
