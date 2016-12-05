package cpusim.util;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import javafx.util.Callback;

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple class that will clean up "enhancement" modifications, for example Mockito or Guice.
 *
 * @since 2016-12-05
 */
public abstract class ClassCleaner {

    private ClassCleaner() {
        // no-op
    }

    private static final String MOCKITO_ENHANCER_TAG = "$$EnhancerByMockitoWithCGLIB$$";

    /**
     * Removes {@value #MOCKITO_ENHANCER_TAG} classes from a mocked class, it will also try to get the proper interface
     * as required.
     *
     * @param dirtyClazz Class to clean
     * @return Clean class
     */
    private static Class<?> cleanMockito(Class<?> dirtyClazz) {
        // Scheme taken from: http://stackoverflow.com/a/33629924
        Class<?> out = dirtyClazz;

        ClimbLoop: while (out != Object.class && out.getSimpleName().contains(MOCKITO_ENHANCER_TAG)) {
            Class<?> superClazz = out.getSuperclass();
            if (superClazz == Object.class) {
                // then it means we may have a raw interface
                for (Class<?> iface : out.getInterfaces()) {
                    if (!iface.getName().equals("org.mockito.cglib.proxy.Factory")) {
                        out = iface;
                        continue ClimbLoop;
                    }
                }
            }

            out = superClazz;
        }

        return out;
    }

    private final static ImmutableSet<Function<Class<?>, Class<?>>> cleaners = ImmutableSet.of(
            // Mockito
            ClassCleaner::cleanMockito
    );

    /**
     * Get a {@link Class} value for the name specified. This will try to clean off any "enhanced" classes from Java
     * ByteCode manipulation.
     *
     * @param name Fully qualified name for a class, e.g. from {@link Class#getName()}.
     * @return Class represented by the fully qualified name.
     * @throws ClassNotFoundException if no valid Class is found.
     * @throws IllegalArgumentException if the name is {@code null} or empty.
     *
     * @see #cleanClass(Class)
     */
    public static Class<?> forName(final String name) throws ClassNotFoundException {
        checkArgument(!Strings.isNullOrEmpty(name), "Class name must be specified.");
        Class<?> clazz = Class.forName(name);
        return cleanClass(clazz);
    }

    /**
     * Cleans a {@link Class} of any enhancements.
     *
     * @param clazz class to clean.
     * @return Cleaned-up class.
     */
    public static Class<?> cleanClass(final Class<?> clazz) {
        Class<?> checkClazz = checkNotNull(clazz);
        for (Function<Class<?>, Class<?>> check: cleaners) {
            checkClazz = check.apply(clazz);
        }

        return checkClazz;
    }
}
