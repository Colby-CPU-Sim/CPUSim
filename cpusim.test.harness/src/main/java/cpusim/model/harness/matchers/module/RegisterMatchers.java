package cpusim.model.harness.matchers.module;

import cpusim.model.Field;
import cpusim.model.Machine;
import cpusim.model.harness.matchers.NamedObjectMatchers;
import cpusim.model.module.Register;
import org.hamcrest.Matcher;

import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * Hamcrest matchers for {@link Field}
 */
public abstract class RegisterMatchers {

    private RegisterMatchers() {
        // no instantiate
    }

    public static Matcher<Register> register(Machine machine, Register expected) {
        return compose("Register",
                compose(named(expected.getName()))
                    .and(ModuleMatchers.module(machine, expected))
                    .and(SizedMatchers.properties(expected))
                    .and(initialValue(expected.getInitialValue()))
                    .and(value(expected.getValue()))
                    .and(access(expected.getAccess())));
    }

    public static Matcher<Register> named(String name) {
        return NamedObjectMatchers.named(name);
    }

    public static Matcher<Register> initialValue(long initialValue) {
        return hasFeature("initial value",
                Register::getInitialValue,
                equalTo(initialValue));
    }

    public static Matcher<Register> value(long value) {
        return hasFeature("current value",
                Register::getValue,
                equalTo(value));
    }

    public static Matcher<Register> access(Set<Register.Access> access) {
        return hasFeature("access",
                Register::getAccess,
                equalTo(access));
    }
}
