package cpusim.model.util;

import static com.google.common.base.Preconditions.*;

/**
 * Describes a class that has internal state that can be validated.
 *
 * @since 2016-11-21
 */
public interface Validatable {
    
    /**
     * Validates the internal state of an instance.
     *
     * @since 2016-11-21
     */
    void validate();
    
    
    /**
     * Runs the {@link #validate()} call on all candidates.
     * @param list List of {@link Validatable} instances.
     *
     * @see #validate()
     *
     * @since 2016-11-21
     */
    public static void all(Iterable<? extends Validatable> list) {
        checkNotNull(list).forEach(Validatable::validate);
    }
}
