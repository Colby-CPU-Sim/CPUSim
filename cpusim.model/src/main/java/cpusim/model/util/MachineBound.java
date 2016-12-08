package cpusim.model.util;

import cpusim.model.Machine;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;

/**
 * Denotes something bound to a non-{@link ReadOnlyObjectProperty} of {@link Machine}.
 *
 * @since 2016-12-07
 */
public interface MachineBound extends ReadOnlyMachineBound {

    @Override
    ObjectProperty<Machine> machineProperty();
}
