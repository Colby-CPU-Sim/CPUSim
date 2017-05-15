package cpusim.gui.harness;

import javafx.stage.Stage;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationAdapter;
import org.testfx.framework.junit.ApplicationFixture;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 2016-12-13.
 */
public class FXRunner extends BlockJUnit4ClassRunner {
    
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface StageSetup {
        
    }
    
    /**
     * Marks methods to run first
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Initialize {
        
    }
    
    /**
     * Marks methods to run after within {@link ApplicationAdapter#stop()}.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Stop {
        
    }
    
    private final List<Method> initMethods;
    
    private final Method stageSetupMethod;
    
    private final List<Method> stopMethods;
    
    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @param klass
     * @throws InitializationError if the test class is malformed.
     */
    public FXRunner(final Class<?> klass) throws InitializationError {
        super(klass);
    
        List<Throwable> errors = new ArrayList<>();
        List<Method> stageSetupMethods = new ArrayList<>();
        List<Method> initializeMethods = new ArrayList<>();
        List<Method> stopMethods = new ArrayList<>();
        for (Method m : klass.getMethods()) {
            if (m.isAnnotationPresent(StageSetup.class)) {
                boolean valid = isMethodPublic(m, StageSetup.class, errors);
    
                if (m.getParameterCount() != 1 || !Stage.class.isAssignableFrom(m.getParameterTypes()[0])) {
                    errors.add(new IllegalStateException(
                            String.format("Method %s is annotated with %s and does not accept a single %s parameter",
                                    m.toString(), StageSetup.class, Stage.class)));
                    valid = false;
                }
                
                if (valid) {
                    stageSetupMethods.add(m);
                }
            } else if (m.isAnnotationPresent(Initialize.class)) {
                boolean valid = isMethodPublic(m, Initialize.class, errors);
                if (m.getParameterCount() != 0) {
                    errors.add(new IllegalStateException(
                            String.format("Method %s is annotated with %s and has parameters, must be no argument method.",
                                    m.toString(), Initialize.class)));
                    valid = false;
                }
                
                if (valid) {
                    initializeMethods.add(m);
                }
            } else if (m.isAnnotationPresent(Stop.class)) {
                boolean valid = isMethodPublic(m, Stop.class, errors);
                if (m.getParameterCount() != 0) {
                    errors.add(new IllegalStateException(
                            String.format("Method %s is annotated with %s and has parameters, must be no argument method.",
                                    m.toString(), Stop.class)));
                    valid = false;
                }
    
                if (valid) {
                    stopMethods.add(m);
                }
            }
        }
    
        if (stageSetupMethods.size() > 1) {
            errors.add(new IllegalStateException(
                    String.format("Multiple methods annotated with %s, only one allowed: %s",
                            StageSetup.class, stageSetupMethods)));
        } else if (stageSetupMethods.isEmpty()) {
            errors.add(new IllegalStateException(
                    String.format("No method annotated with %s, must specify one", StageSetup.class)));
        }
        
        if (!errors.isEmpty()) {
            throw new InitializationError(errors);
        }
        
        this.initMethods = initializeMethods;
        this.stageSetupMethod = stageSetupMethods.get(0);
        this.stopMethods = stopMethods;
    }
    
    @Override
    protected Object createTest() throws Exception {
        
        Object testInst = super.createTest();
    
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupApplication(() -> new ApplicationAdapter(new ApplicationFixture() {
            @Override
            public void init() throws Exception {
                initMethods.forEach(m -> {
                    try {
                        m.invoke(testInst);
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new IllegalStateException("Failed to invoke init method", e);
                    }
                });
            }
    
            @Override
            public void start(final Stage stage) throws Exception {
                stageSetupMethod.invoke(testInst, stage);
            }
    
            @Override
            public void stop() throws Exception {
                stopMethods.forEach(m -> {
                    try {
                        m.invoke(testInst);
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new IllegalStateException("Failed to invoke stop method", e);
                    }
                });
            }
        }));
        
        return testInst;
    }
    
    private static boolean isMethodPublic(final Method m, Class<? extends Annotation> annClazz, final List<Throwable> errors) {
        if (!Modifier.isPublic(m.getModifiers())) {
            errors.add(new IllegalStateException(
                    String.format("Method %s is annotated with %s and is private, it must be public.",
                            m.toString(),
                            annClazz)));
            return false;
        }
        
        return true;
    }
    
    
}
