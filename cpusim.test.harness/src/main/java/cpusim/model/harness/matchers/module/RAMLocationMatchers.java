package cpusim.model.harness.matchers.module;

import cpusim.model.Field;
import cpusim.model.assembler.SourceLine;
import cpusim.model.module.RAM;
import cpusim.model.module.RAMLocation;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 * Hamcrest matchers for {@link Field}
 */
public abstract class RAMLocationMatchers {

    private RAMLocationMatchers() {
        // no instantiate
    }

    /**
     * Creates a {@link Matcher} for {@link RAMLocation} parts.
     *
     * @return Matcher
     * @see RAMLocation
     */
    public static Matcher<RAMLocation> properties(RAM ram, RAMLocation expected) {
        return compose("RAM Location",
                compose(address(expected.getAddress()))
                        .and(comment(expected.getComment()))
                        .and(ram(sameInstance(ram)))
                        .and(shouldBreak(expected.getBreak()))
                        .and(sourceLine(expected.getSourceLine()))
                        .and(value(expected.getValue())));
    }

    /** @see RAMLocation#addressProperty() */
    public static Matcher<RAMLocation> address(long address) {
        return hasFeature("address",
                RAMLocation::getAddress,
                equalTo(address));
    }

    /** @see RAMLocation#breakProperty() */
    public static Matcher<RAMLocation> shouldBreak(boolean shouldBreak) {
        return hasFeature("is break point",
                RAMLocation::getBreak,
                equalTo(shouldBreak));
    }

    /** @see RAMLocation#commentProperty() */
    public static Matcher<RAMLocation> comment(String comment) {
        return hasFeature("comment",
                RAMLocation::getComment,
                equalTo(comment));
    }

    /** @see RAMLocation#sourceLineProperty() */
    public static Matcher<RAMLocation> sourceLine(SourceLine line) {
        return hasFeature("source line",
                RAMLocation::getSourceLine,
                equalTo(line));
    }

    /** @see RAMLocation#valueProperty() */
    public static Matcher<RAMLocation> value(long value) {
        return hasFeature("value",
                RAMLocation::getValue,
                equalTo(value));
    }

    /** @see RAMLocation#getRam() */
    public static Matcher<RAMLocation> ram(Matcher<RAM> ram) {
        return hasFeature("ram", RAMLocation::getRam, ram);
    }

}
