package cpusim.model.harness.matchers.microinstruction;

import cpusim.model.Machine;
import cpusim.model.microinstruction.Logical;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.instanceOf;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * {@link Matcher Matchers} for {@link Logical} operations.
 */
public abstract class LogicalMatchers extends ArithmeticLogicOperationMatchers {

    private LogicalMatchers() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Creates a {@link Matcher} for {@link Logical} instructions
     * @return Matcher
     *
     * @see Logical
     */
    public static Matcher<Logical> logical(Machine machine, Logical expected) {
        return compose("Logical",
                arithmeticLogicOperation(machine, expected)
                    .and(validOperation()));
    }

    /** @see Logical#operationProperty()  */
    public static Matcher<Logical> validOperation() {
        return hasFeature("operation",
                Logical::getOperation,
                instanceOf(Logical.Type.class));
    }
}
