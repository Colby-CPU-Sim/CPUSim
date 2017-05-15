package cpusim.model.harness.matchers.module;

import com.google.common.collect.Lists;
import cpusim.model.Field;
import cpusim.model.Machine;
import cpusim.model.harness.matchers.NamedObjectMatchers;
import cpusim.model.module.RAM;
import cpusim.model.module.RAMLocation;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * Hamcrest matchers for {@link Field}
 */
public abstract class RAMMatchers {

    private RAMMatchers() {
        // no instantiate
    }

    public static Matcher<RAM> ram(Machine machine, RAM expected) {
        return compose("RAM",
                compose(named(expected.getName()))
                    .and(ModuleMatchers.module(machine, expected))
                        .and(SizedMatchers.sized(expected))
                    .and(cellMask(expected.getCellMask()))
                    .and(cellSize(expected.getCellSize()))
                    .and(data(expected.getDataIterator(0)))
                    .and(length(expected.getLength()))
                    .and(haltOnBreaks(expected.haltAtBreaks())));
    }

    public static Matcher<RAM> cellMask(long mask) {
        return hasFeature("cell mask", RAM::getCellMask, equalTo(mask));
    }

    public static Matcher<RAM> cellSize(int cellSize) {
        return hasFeature("cell size",
                RAM::getCellSize,
                equalTo(cellSize));
    }

    public static Matcher<RAM> data(RAMLocation... values) {
        return data(Arrays.asList(values));
    }

    public static Matcher<RAM> data(Iterator<RAMLocation> values) {
        return data(Lists.newArrayList(values));
    }

    public static Matcher<RAM> data(Collection<? extends RAMLocation> values) {
        return new TypeSafeMatcher<RAM>(RAM.class) {
            @Override
            public boolean matchesSafely(RAM item) {
                return values.size() == item.getLength() &&
                        Matchers.contains(values.stream()
                                .map(rl -> RAMLocationMatchers.properties(item, rl))
                                .collect(Collectors.toList()))
                                .matches(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(" has data ")
                        .appendValue(values);
            }
        };
    }

    public static Matcher<RAM> length(int length) {
        return hasFeature("length",
                RAM::getLength,
                equalTo(length));
    }

    public static Matcher<RAM> named(String name) {
        return NamedObjectMatchers.named(name);
    }

    public static Matcher<RAM> haltOnBreaks(boolean halt) {
        return hasFeature("should halt",
                RAM::haltAtBreaks,
                equalTo(halt));
    }


}
