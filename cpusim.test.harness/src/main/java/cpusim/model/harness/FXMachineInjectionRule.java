package cpusim.model.harness;

import cpusim.gui.harness.FXHarness;
import cpusim.model.Machine;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.annotation.Nullable;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static org.testfx.util.WaitForAsyncUtils.*;

/**
 * Extension of {@link MachineInjectionRule} that allows the rule to be used in JavaFX tests
 */
public class FXMachineInjectionRule extends MachineInjectionRule {

    /**
     * The Test class instance.
     *
     * @param instance
     */
    public FXMachineInjectionRule(Object instance) {
        this(instance, null);
    }
    
    /**
     * The Test class instance.
     *
     * @param instance
     */
    public FXMachineInjectionRule(Object instance, @Nullable Supplier<Machine> cpuFactory) {
        super(instance, cpuFactory);

        checkArgument(FXHarness.class.isAssignableFrom(instance.getClass()),
                "Test instance must inherit from %s to be used with %s, type: %s",
                FXHarness.class, FXMachineInjectionRule.class, instance.getClass());
    }

    @Override
    protected void onEvaluateStatement(Statement base, final Description description) {
        // We inject the instructionFields on the JavaFX thread,
        // not on the main thread. The waitFor waits until
        // the Future completes
        waitFor(asyncFx(this::injectFields));
        waitForFxEvents();
    }

}
