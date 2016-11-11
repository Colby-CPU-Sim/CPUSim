package cpusim.model.module;

import cpusim.model.Module;

/**
 * Denotes that a {@link Module} has a bit width.
 *
 * @author Kevin Brightwell (Nava2)
 * @since 2016-11-14
 */
public interface Sized<T extends Module<T>> {
    
    /**
     * Get the width of the {@link Sized} component.
     * @return Width in <em>bytes</em>.
     */
    int getWidth();
}
