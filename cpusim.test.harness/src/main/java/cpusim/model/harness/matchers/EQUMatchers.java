package cpusim.model.harness.matchers;

import cpusim.model.assembler.EQU;
import org.hamcrest.Matcher;

import static cpusim.model.harness.matchers.NamedObjectMatchers.named;
import static org.hamcrest.CoreMatchers.is;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * Matchers for {@link EQU}
 */
public abstract class EQUMatchers {

    private EQUMatchers() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a {@link Matcher} for an {@link EQU}.
     *
     * @return Matcher
     * @see EQU
     */
    public static Matcher<EQU> equ(EQU expected) {
        return compose("EQU",
                compose(value(expected.getValue()))
                        .and(named(expected.getName())));
    }
    
    /**
     * @see cpusim.model.assembler.EQU#getValue()
     */
    public static Matcher<EQU> value(long value) {
        return hasFeature("value", EQU::getValue, is(value));
    }
    
}
