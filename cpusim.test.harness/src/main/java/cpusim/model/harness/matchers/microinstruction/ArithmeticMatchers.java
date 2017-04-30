package cpusim.model.harness.matchers.microinstruction;

import cpusim.model.Machine;
import cpusim.model.microinstruction.Arithmetic;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.instanceOf;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * {@link Matcher Matchers} for the {@link Arithmetic} {@link cpusim.model.microinstruction.Microinstruction}
 */
public abstract class ArithmeticMatchers extends ArithmeticLogicOperationMatchers {

    private ArithmeticMatchers() {
        // no instantiation
    }

    public static Matcher<Arithmetic> arithmetic(Machine machine, Arithmetic expected) {
        return compose("Arithmetic",
                arithmeticLogicOperation(machine, expected))
                    .and(validOperation());
    }

    /** @see Arithmetic#operationProperty() */
    public static Matcher<Arithmetic> validOperation() {
        return hasFeature("operation",
                Arithmetic::getOperation,
                instanceOf(Arithmetic.Type.class));
    }

}
