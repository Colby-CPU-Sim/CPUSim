package cpusim.model.harness;

import cpusim.model.util.MachineBound;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

/**
 * Denotes a {@link Field} to bind to via {@link MachineBound#machineProperty()}.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BindMachine {

}
