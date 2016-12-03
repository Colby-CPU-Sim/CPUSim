package cpusim.gui.util;

import cpusim.model.Machine;
import cpusim.model.util.Validatable;

/**
 * Designates that a controller modifies an underlying {@link cpusim.model.Machine}.
 *
 * @since 2016-11-30
 */
public interface MachineModificationController extends MachineBound {

    /**
     * Writes the content of the controller to the {@link Machine}.
     *
     * @throws cpusim.model.util.ValidationException if there is invalid data stored.
     */
    void updateMachine();

    /**
     * Checks the validity of the content of the Controller.
     */
    void checkValidity();
}
