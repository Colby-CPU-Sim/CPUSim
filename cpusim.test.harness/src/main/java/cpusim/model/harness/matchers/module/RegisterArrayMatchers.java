package cpusim.model.harness.matchers.module;

import cpusim.model.Field;
import cpusim.model.Machine;
import cpusim.model.harness.matchers.NamedObjectMatchers;
import cpusim.model.harness.matchers.TypedMatcher;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * Hamcrest matchers for {@link Field}
 */
public abstract class RegisterArrayMatchers {

    private RegisterArrayMatchers() {
        // no instantiate
    }

    public static Matcher<RegisterArray> registerArray(Machine machine, RegisterArray expected) {
        return compose("RegisterArray",
                compose(named(expected.getName()))
                    .and(ModuleMatchers.module(machine, expected))
                    .and(SizedMatchers.properties(expected))
                    .and(initialAccess(expected.getInitialAccess()))
                    .and(initialValue(expected.getInitialValue()))
                    .and(registers(machine, expected.getRegisters())));
    }

    public static Matcher<RegisterArray> named(String name) {
        return NamedObjectMatchers.named(name);
    }


    public static Matcher<RegisterArray> length(int length) {
        return hasFeature("length",
                RegisterArray::getLength,
                equalTo(length));
    }

    public static Matcher<RegisterArray> initialAccess(EnumSet<Register.Access> access) {
        return hasFeature("access",
                RegisterArray::getInitialAccess,
                equalTo(access));
    }

    public static Matcher<RegisterArray> initialValue(long initialValue) {
        return hasFeature("initial value",
                RegisterArray::getInitialValue,
                equalTo(initialValue));
    }

    public static Matcher<RegisterArray> registers(Machine machine, Register... values) {
        return registers(machine, Arrays.asList(values));
    }

    public static Matcher<RegisterArray> registers(Machine machine, Collection<? extends Register> values) {
        return new TypedMatcher<RegisterArray>(RegisterArray.class) {
            @Override
            public boolean typedMatches(RegisterArray item) {
                return values.size() == item.getRegisters().size() &&
                        Matchers.containsInAnyOrder(values.stream()
                                .map(r -> RegisterMatchers.register(machine, r))
                                .collect(Collectors.toList()))
                                .matches(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(" has registers ")
                        .appendValue(values);
            }
        };
    }

}
