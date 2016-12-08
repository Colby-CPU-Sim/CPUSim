package cpusim.model;

import cpusim.model.util.MachineComponent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Clones {@link Machine} values via the {@link #cloneMachine(Machine)} method.
 *
 * @see #cloneMachine(Machine)
 *
 * @since 2016-12-07
 */
public class MachineCloner {


    public MachineCloner() {
    }

    /**
     * Clones the machine specified as {@code originalMachine}.
     *
     * @return Clone of original {@link Machine}
     */
    public Machine cloneMachine(Machine originalMachine) {
        checkNotNull(originalMachine);

        MachineComponent.IdentifierMap oldToNewMapping = new MachineComponent.IdentifierMap();

        return originalMachine.cloneFor(oldToNewMapping);
    }
}
