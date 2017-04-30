package cpusim.model.module;

import com.google.common.collect.ImmutableSet;

/**
 * Utility methods surrounding {@link Module Modules}.
 */
public abstract class Modules {

    private Modules() {
        // no instantiate
    }

    /**
     * Get all of the supported implementing {@link Class} values for {@link Module} instances.
     * @return
     */
    public static ImmutableSet<Class<? extends Module<?>>> getModuleClasses() {
        ImmutableSet.Builder<Class<? extends Module<?>>> bld = ImmutableSet.builder();
        bld.add(Register.class);
        bld.add(RegisterArray.class);
        bld.add(RAM.class);
        bld.add(ConditionBit.class);
        return bld.build();
    }
}
