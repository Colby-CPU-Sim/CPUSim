package cpusim.gui.util;

import cpusim.model.Machine;
import cpusim.model.util.Validatable;

import java.util.List;

/**
 * Designates that a controller modifies an underlying {@link cpusim.model.Machine}.
 *
 * @since 2016-11-30
 */
public interface MachineModificationController<T extends Validatable> {


    /**
     * Access the stored machine.
     * @return Non-{@code null} {@code Machine} stored by the controller.
     */
    Machine getMachine();

    /**
     * Writes the content of the controller to the {@link Machine}.
     *
     * @throws cpusim.model.util.ValidationException if there is invalid data stored.
     */
    void updateMachine();

    /**
     * Checks the validity of the content of the Controller.
     * @param items the list of items that could be placed into a {@link Machine}.
     */
    default void checkValidity(final List<T> items) {
        Validatable.all(items);
    }
}
