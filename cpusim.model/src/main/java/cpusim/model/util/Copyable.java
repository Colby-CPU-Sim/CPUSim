/**
 * 
 */
package cpusim.model.util;

import javafx.beans.property.Property;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

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
    <U extends T> void copyTo(final U other);
    
    /**
     * Instantiates a clone of the underlying type. This is <strong>very unsafe</strong>, but it supports some legacy
     * code that needs a proper clone method.
     * @return Non-{@code null} instance of T.
     */
    @SuppressWarnings("unchecked")
    default T cloneOf() {
        try {
            Class<T> clazz = (Class<T>)getClass();

            try {
                final Constructor<T> ctor = clazz.getDeclaredConstructor(clazz);
                ctor.setAccessible(true);

                return ctor.newInstance(this);
            } catch (NoSuchMethodException nsme) {
                // try the other option
                final Constructor<T> ctor = clazz.getDeclaredConstructor();
                ctor.setAccessible(true);

                final T instance = ctor.newInstance(this);
                this.copyTo(instance);

                return instance;
            }
        } catch (NoSuchMethodException nsme) {
            throw new IllegalStateException("Could not execute constructor, " +
                    "likely no copy-constructor or default constructor specified", nsme);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e){
            throw new IllegalStateException("Invalid access to constructor", e);
        }
    }

    @SuppressWarnings("unchecked")
    static <T extends Copyable<T>> void copyProperties(final T from, final T to, List<Function<T, Property<?>>> propertyAccessors) {
        checkNotNull(to);
        checkNotNull(from);

        for (Function<T, Property<?>> accessor: propertyAccessors) {
            checkNotNull(accessor);

            Property pTo = accessor.apply(to);
            Property pFrom = accessor.apply(from);

            pTo.setValue(pFrom.getValue());
        }
    }
	
}
