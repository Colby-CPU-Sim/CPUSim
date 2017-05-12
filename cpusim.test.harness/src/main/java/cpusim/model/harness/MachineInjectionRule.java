package cpusim.model.harness;

import cpusim.model.Machine;
import cpusim.model.util.MachineBound;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * JUnit4 {@link org.junit.Rule} that will automatically bind anything annotated with {@link BindMachine} to an internal
 * {@link Machine} instance build with the name of the Test class.
 *
 * @see FXMachineInjectionRule for working with JavaFX applications -- it requires special considerations for threads
 */
public class MachineInjectionRule extends SimpleObjectProperty<Machine> implements TestRule {
    private final Object testInstance;
    private final List<Field> fieldsToBind;
    private final Supplier<Machine> machineFactory;

    public MachineInjectionRule(Object instance) {
        this(instance, null);
    }

    public MachineInjectionRule(Object instance, @Nullable Supplier<Machine> cpuFactory) {
        testInstance = checkNotNull(instance, "instance == null");

        ArrayList<Field> fieldsToBind = new ArrayList<>();

        // collect the instructionFields
        Class<?> iterClazz = testInstance.getClass();
        while (iterClazz != Object.class) {
            for (Field f : iterClazz.getDeclaredFields()) {
                if (f.isAnnotationPresent(BindMachine.class) &&
                        (MachineBound.class.isAssignableFrom(f.getType())
                                || ObjectProperty.class.isAssignableFrom(f.getType()))) {
                    f.setAccessible(true);
                    fieldsToBind.add(f);
                }
            }

            iterClazz = iterClazz.getSuperclass();
        }

        fieldsToBind.trimToSize();

        this.fieldsToBind = fieldsToBind;

        if (cpuFactory != null) {
            this.machineFactory = cpuFactory;
        } else {
            this.machineFactory = () -> new Machine(testInstance.getClass().getCanonicalName());
        }
    }

    private Machine createMachine() {
        return this.machineFactory.get();
    }

    @Override
    public final Statement apply(final Statement base, final Description description) {
        this.set(createMachine());

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                onEvaluateStatement(base, description);

                base.evaluate();
            }
        };
    }

    protected void onEvaluateStatement(Statement base, final Description description) {
        // Simply inject the fields, all injection, no waiting. :)
        injectFields();
    }

    protected void injectFields() {
        // find them all, we need to reverse the List after to call super classes first
        fieldsToBind.forEach(f -> {
            try {
                f.setAccessible(true);
                Object instance = f.get(testInstance);
                if (MachineBound.class.isAssignableFrom(f.getType())) {
                    MachineBound bound = (MachineBound) instance;
                    checkState(bound != null,
                            "Found field %s that is null, did you initialize it in an @Before method? " +
                                    "To use the @BindMachine annotation, must initialize during construction or declaration.",
                            f);
                    bound.machineProperty().bind(this);
                } else {
                    @SuppressWarnings("unchecked") // this is safe because of how the instructionFields are collected
                    ObjectProperty<Machine> property = (ObjectProperty<Machine>) instance;
                    property.bind(this);
                }
            } catch (IllegalAccessException iae) {
                throw new IllegalStateException(iae);
            }
        });
    }
}
