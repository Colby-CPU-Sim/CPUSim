/**
 * 
 */
package cpusim.model.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Denotes that an instance can be copied to
 *
 * @since 2016-09-20
 */
public interface Copyable<T extends Copyable<T>> {

	/**
     * Provides a field-by-field copy into <code>other</code>.
     * 
     * @param other Instance to copy into
     *
     * @throws NullPointerException if `other` is null
     */
    public <U extends T> void copyTo(final U other);
    
    /**
     * Instantiates a clone of the underlying type. This is <strong>very unsafe</strong>, but it supports some legacy
     * code that needs a proper clone method.
     * @return Non-{@code null} instance of T.
     */
    @SuppressWarnings("unchecked")
    public default T cloneOf() {
        try {
            final Constructor<T> ctor = (Constructor<T>)this.getClass().getConstructor(getClass());
            final T newT = ctor.newInstance(this);
            return newT;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new IllegalStateException(e);
        }
    }
	
}
