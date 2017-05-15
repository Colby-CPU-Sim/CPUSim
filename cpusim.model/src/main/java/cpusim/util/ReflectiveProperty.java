package cpusim.util;

import com.google.common.base.Strings;
import javafx.beans.property.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Reflectively finds a property by a name on a class. This is useful for different
 * property-based tasks. For example, utilization in JFX tables.
 */
// TODO replace with jfxproperties later
@ParametersAreNonnullByDefault
public final class ReflectiveProperty<T, P>  {

    private final static Logger logger = LogManager.getLogger(ReflectiveProperty.class);

    private final Class<T> baseClazz;
    private final String propertyName;

    private Method propMethod = null;

    public ReflectiveProperty(Class<T> baseClass, String propertyName) {
        checkNotNull(baseClass, "baseClass == null");
        checkArgument(!Strings.isNullOrEmpty(propertyName), "propertyName must not be null or empty");

        this.baseClazz = baseClass;
        this.propertyName = propertyName;
    }

    static final String PROPERTY_SUFFIX = "Property";

    String getPropertyMethodName() {
        return Character.toLowerCase(propertyName.charAt(0))
                + propertyName.substring(1, propertyName.length())
                + PROPERTY_SUFFIX;
    }

    Method getPropMethod() {
        logger.traceEntry("Reflective search for property: {}", propertyName);

        final String propertyMethodName = getPropertyMethodName();

        Method propMethod = null;
        Class<?> clazz = baseClazz;

        while (clazz != Object.class && propMethod == null) {

            try {
                Method m = clazz.getMethod(propertyMethodName);
                if (m != null) {
                    propMethod = m;
                    logger.trace("Found method '{}' on class {}", propertyMethodName, clazz.getName());
                }
            } catch (NoSuchMethodException e) {
                // just keep going
            }

            clazz = baseClazz.getSuperclass();
        }

        if (propMethod == null) {
            throw new IllegalStateException("No property method (" + propertyMethodName + ") exists on " + baseClazz.getName());
        }

        this.propMethod = propMethod;
        return logger.traceExit(propMethod);
    }

    public Property<P> getProperty(T base) {
        checkNotNull(base, "base == null");

        if (propMethod == null) {
            getPropMethod();
        }

        try {
            @SuppressWarnings("unchecked")
            final Property<P> out = (Property<P>)propMethod.invoke(base);
            return out;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

}
