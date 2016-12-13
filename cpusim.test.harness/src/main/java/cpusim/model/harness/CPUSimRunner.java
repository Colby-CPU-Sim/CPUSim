package cpusim.model.harness;

import javafx.beans.property.ObjectProperty;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @since 2016-12-12
 */
public class CPUSimRunner extends BlockJUnit4ClassRunner {

    @Target({ElementType.METHOD, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MachineSetup {
    }

    private static final Class<?>[] INJECT_METHOD_PARAMETERS = new Class<?>[] { ObjectProperty.class };

    private final List<Method> machineSetupMethods;

    public CPUSimRunner(Class<?> klass) throws InitializationError {
        super(klass);

        // Load up all Machine "bound" methods
        machineSetupMethods = new ArrayList<>();

        // find them all, we need to reverse the List after to call super classes first
        while (klass != Object.class) {
            for (Method m : klass.getDeclaredMethods()) {
                if (m.isAnnotationPresent(MachineSetup.class) &&
                        Arrays.equals(INJECT_METHOD_PARAMETERS, m.getParameterTypes())) {
                    m.setAccessible(true);
                    machineSetupMethods.add(m);
                }
            }

            klass = klass.getSuperclass();
        }

        Collections.reverse(machineSetupMethods);
    }

    @Override
    protected Object createTest() throws Exception {
        Class<?> testClazz = getTestClass().getJavaClass();

        Object testInst = super.createTest();

//        Machine machine = new Machine(testClazz.getCanonicalName());
//        ObjectProperty<Machine> machineProperty = new SimpleObjectProperty<>(testInst, "machine", machine);
//        machineSetupMethods.forEach(m -> {
//            try {
//                m.invoke(testInst, machineProperty);
//            } catch (InvocationTargetException ite) {
//                throw new IllegalStateException(ite);
//            } catch (IllegalAccessException iae) {
//                throw new IllegalStateException("This should never happen, Method#setAccessible(boolean) " +
//                        "is already called.", iae);
//            }
//        });

        return testInst;
    }
}
