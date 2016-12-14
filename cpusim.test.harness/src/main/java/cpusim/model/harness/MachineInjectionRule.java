package cpusim.model.harness;

import cpusim.model.Machine;
import cpusim.model.util.MachineBound;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * JUnit4 {@link org.junit.Rule} that will automatically bind anything annotated with {@link BindMachine} to an internal
 * {@link Machine} instance build with the name of the Test class.
 *
 * @since 2016-12-13.
 */
public class MachineInjectionRule extends SimpleObjectProperty<Machine> implements TestRule {

    private final Object testInstance;
    
    private final List<Field> fieldsToBind;
    
    /**
     * The Test class instance.
     *
     * @param instance
     */
    public MachineInjectionRule(Object instance) {
        checkNotNull(instance);
    
        testInstance = instance;
        
        ArrayList<Field> fieldsToBind = new ArrayList<>();
    
        // collect the fields
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
    }
    
    private Machine createMachine() {
        return new Machine(testInstance.getClass().getCanonicalName());
    }
    
    @Override
    public Statement apply(final Statement base, final Description description) {
        MachineInjectionRule.this.set(createMachine());

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                injectFields();
                base.evaluate();
            }
        };
    }
    
    private void injectFields() {
        // find them all, we need to reverse the List after to call super classes first
        fieldsToBind.forEach(f -> {
            try {
                f.setAccessible(true);
                Object instance = f.get(testInstance);
                if (MachineBound.class.isAssignableFrom(f.getType())) {
                    MachineBound bound = (MachineBound) instance;
                    bound.machineProperty().bind(this);
                } else {
                    @SuppressWarnings("unchecked") // this is safe because of how the fields are collected
                    ObjectProperty<Machine> property = (ObjectProperty<Machine>) instance;
                    property.bind(this);
                }
            } catch (IllegalAccessException iae) {
                throw new IllegalStateException(iae);
            }
        });
    }
}
