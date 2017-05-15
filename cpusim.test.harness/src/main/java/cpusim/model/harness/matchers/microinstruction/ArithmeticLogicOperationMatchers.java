package cpusim.model.harness.matchers.microinstruction;

import cpusim.model.Machine;
import cpusim.model.harness.matchers.MachineBoundMatchers;
import cpusim.model.harness.matchers.module.ConditionBitMatchers;
import cpusim.model.harness.matchers.module.RegisterMatchers;
import cpusim.model.microinstruction.ArithmeticLogicOperation;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.Register;
import org.hamcrest.Matcher;
import org.hobsoft.hamcrest.compose.ConjunctionMatcher;

import java.util.Optional;
import java.util.function.Function;

import static cpusim.model.harness.matchers.microinstruction.MicroinstructionMatchers.microinstruction;
import static org.hamcrest.CoreMatchers.is;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

/**
 *  Base {@link Matcher Matchers} for {@link ArithmeticLogicOperation}s
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "WeakerAccess"})
abstract class ArithmeticLogicOperationMatchers {

    ArithmeticLogicOperationMatchers() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a {@link Matcher} for the base properties of an {@link ArithmeticLogicOperation}
     * instruction.
     *
     * @return Matcher
     * @see ArithmeticLogicOperation
     */
    static <T extends ArithmeticLogicOperation<T>>
    ConjunctionMatcher<T> arithmeticLogicOperation(Machine machine, T expected) {
        return microinstruction(machine, expected)
                    .and(destination(machine, expected.getDestination()))
                    .and(lhs(machine, expected.getLhs()))
                    .and(rhs(machine, expected.getRhs()))
                    .and(operator(expected.getOperation()))
                    .and(carryBit(machine, expected.getCarryBit()))
                    .and(negativeBit(machine, expected.getNegativeBit()))
                    .and(overflowBit(machine, expected.getOverflowBit()))
                    .and(zeroBit(machine, expected.getZeroBit()));
    }

    /**
     * @see ArithmeticLogicOperation#operationProperty()
     */
    public static <T extends ArithmeticLogicOperation<T>>
    Matcher<T> operator(ArithmeticLogicOperation.ALUOperator operator) {
        return hasFeature("operator", ArithmeticLogicOperation::getOperation, is(operator));
    }

    private static <T extends ArithmeticLogicOperation<T>>
    Matcher<T> register(String desc, Function<T, Optional<Register>> accessor,
                        Machine machine, Optional<Register> register) {
        return MachineBoundMatchers.optionalMachineValue(desc, accessor,
                RegisterMatchers::register,
                machine, register);
    }

    /** @see ArithmeticLogicOperation#destinationProperty() */
    public static <T extends ArithmeticLogicOperation<T>>
    Matcher<T> destination(Machine machine, Register destination) {
        return destination(machine, Optional.ofNullable(destination));
    }

    /** @see ArithmeticLogicOperation#destinationProperty() */
    public static <T extends ArithmeticLogicOperation<T>>
    Matcher<T> destination(Machine machine, Optional<Register> destination) {
        return register("destination",
                ArithmeticLogicOperation::getDestination,
                machine,
                destination);
    }

    /** @see ArithmeticLogicOperation#lhsProperty() */
    public static <T extends ArithmeticLogicOperation<T>>
    Matcher<T> lhs(Machine machine, Register lhs) {
        return lhs(machine, Optional.ofNullable(lhs));
    }

    /** @see ArithmeticLogicOperation#lhsProperty() */
    public static <T extends ArithmeticLogicOperation<T>>
    Matcher<T> lhs(Machine machine, Optional<Register> lhs) {
        return register("left-hand side",
                ArithmeticLogicOperation::getLhs,
                machine,
                lhs);
    }

    /** @see ArithmeticLogicOperation#rhsProperty() */
    public static <T extends ArithmeticLogicOperation<T>>
    Matcher<T> rhs(Machine machine, Register rhs) {
        return rhs(machine, Optional.ofNullable(rhs));
    }

    /** @see ArithmeticLogicOperation#rhsProperty() */
    public static <T extends ArithmeticLogicOperation<T>>
    Matcher<T> rhs(Machine machine, Optional<Register> rhs) {
        return register("right-hand side",
                ArithmeticLogicOperation::getRhs,
                machine,
                rhs);
    }

    // Check a condition bit
    private static <T extends ArithmeticLogicOperation<T>>
    Matcher<T> conditionBitProp(String desc,
                                Function<T, Optional<ConditionBit>> accessor,
                                Machine machine,
                                Optional<ConditionBit> bit) {
        return MachineBoundMatchers.optionalMachineValue(desc, accessor,
                ConditionBitMatchers::conditionBit,
                machine, bit);
    }

    /** @see ArithmeticLogicOperation#carryBitProperty() */
    public static <T extends ArithmeticLogicOperation<T>>
    Matcher<T> carryBit(Machine machine, Optional<ConditionBit> carryBit) {
        return conditionBitProp("carryBit", ArithmeticLogicOperation::getCarryBit, machine,
                carryBit
        );
    }

    /** @see ArithmeticLogicOperation#negativeBitProperty() */
    public static <T extends ArithmeticLogicOperation<T>>
    Matcher<T> negativeBit(Machine machine, Optional<ConditionBit> negativeBit) {
        return conditionBitProp("negativeBit", ArithmeticLogicOperation::getNegativeBit, machine,
                negativeBit
        );
    }

    /** @see ArithmeticLogicOperation#overflowBitProperty() */
    public static <T extends ArithmeticLogicOperation<T>>
    Matcher<T> overflowBit(Machine machine, Optional<ConditionBit>  overflowBit) {
        return conditionBitProp("overflowBit", ArithmeticLogicOperation::getOverflowBit, machine,
                overflowBit
        );
    }

    /** @see ArithmeticLogicOperation#zeroBitProperty() */
    public static <T extends ArithmeticLogicOperation<T>>
    Matcher<T> zeroBit(Machine machine, Optional<ConditionBit> zeroBit) {
        return conditionBitProp("zeroBit", ArithmeticLogicOperation::getZeroBit, machine,
                zeroBit
        );
    }

}
