package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.Module;
import cpusim.model.module.Register;
import cpusim.model.util.MachineComponent;
import cpusim.model.util.ObservableCollectionBuilder;
import cpusim.model.util.Validate;
import cpusim.model.util.ValidationException;
import javafx.beans.property.*;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.math.BigInteger;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Common {@link Microinstruction} between {@link Arithmetic} and {@link Logical}
 * @since 2016-12-07
 */
@ParametersAreNonnullByDefault
public abstract class ArithmeticLogicOperation<T extends ArithmeticLogicOperation<T>>
        extends Microinstruction<T> {

    /**
     * A Binary operation performed, it is just an alias of {@link BiFunction} with {@link BigInteger}
     * arguments.
     */
    public interface ALUOperator extends BiFunction<BigInteger, BigInteger, BigInteger> {
        // empty, just for convenience
    }

    private final ObjectProperty<ALUOperator> operation;

    @DependantComponent
    private final ObjectProperty<Register> lhs;

    @DependantComponent
    private final ObjectProperty<Register> rhs;

    @DependantComponent
    private final ObjectProperty<Register> destination;

    @DependantComponent
    private final ObjectProperty<ConditionBit> carryBit;
    @DependantComponent
    private final ObjectProperty<ConditionBit> negativeBit;
    @DependantComponent
    private final ObjectProperty<ConditionBit> overflowBit;
    @DependantComponent
    private final ObjectProperty<ConditionBit> zeroBit;

    @DependantComponent
    private final ReadOnlySetProperty<ConditionBit> conditionBits;

    private final ReadOnlySetProperty<MachineComponent> dependantComponents;

    ArithmeticLogicOperation(String name, UUID id, Machine machine,
                             ALUOperator operation,
                             Register destination, Register lhs, Register rhs,
                             ConditionBit carryBit, ConditionBit negativeBit,
                             ConditionBit overflowBit, ConditionBit zeroBit) {
        super(name, id, machine);
        this.destination = new SimpleObjectProperty<>(this, "destination", destination);
        this.lhs = new SimpleObjectProperty<>(this, "lhs", lhs);
        this.rhs = new SimpleObjectProperty<>(this, "rhs", rhs);

        this.carryBit = new SimpleObjectProperty<>(this, "carryBit", carryBit);
        this.negativeBit = new SimpleObjectProperty<>(this, "negativeBit", negativeBit);
        this.overflowBit = new SimpleObjectProperty<>(this, "overflowBit", overflowBit);
        this.zeroBit = new SimpleObjectProperty<>(this, "zeroBit", zeroBit);

        ObservableCollectionBuilder<ConditionBit> conditionBits = new ObservableCollectionBuilder<>();
        conditionBits.add(this.carryBit)
                .add(this.negativeBit)
                .add(this.overflowBit)
                .add(this.zeroBit);

        this.conditionBits = conditionBits.buildSet(this, "conditionBits");

        this.operation = new SimpleObjectProperty<ALUOperator>(this, "operation", operation) {
            // Do some special checking, make sure its not some arbitrary operation
            private void checkOperator(ALUOperator newValue) {
                checkNotNull(newValue);

                if (!validOperations().contains(newValue)) {
                    throw new IllegalArgumentException("Attempted to set invalid operation: " + newValue);
                }
            }

            @Override
            public void set(ALUOperator v) {
                checkOperator(v);

                super.set(v);
            }

            @Override
            public void setValue(ALUOperator v) {
                checkOperator(v);

                super.setValue(v);
            }
        };

        dependantComponents = MachineComponent.collectDependancies(this)
                .buildSet(this, "dependantComponents");
    }

    public abstract Set<? extends ALUOperator> validOperations();

    @Override
    public ReadOnlySetProperty<MachineComponent> getDependantComponents() {
        return dependantComponents;
    }

    /**
     * returns the register to be calculated.
     * @return the name of the register.
     */
    public Optional<Register> getLhs(){
        return Optional.ofNullable(lhs.get());
    }

    /**
     * updates the register used by the microinstruction.
     * @param lhs the new source register for the logical microinstruction.
     */
    public void setLhs(@Nullable Register lhs){
        this.lhs.set(lhs);
    }

    public ObjectProperty<Register> lhsProperty() {
        return lhs;
    }

    /**
     * returns the register to be calculated.
     * @return the name of the register.
     */
    public Optional<Register> getRhs(){
        return Optional.ofNullable(rhs.get());
    }

    /**
     * updates the register used by the microinstruction.
     * @param rhs the new source register for the logical microinstruction.
     */
    public void setRhs(@Nullable Register rhs){
        this.rhs.set(rhs);
    }


    public ObjectProperty<Register> rhsProperty() {
        return rhs;
    }

    /**
     * returns the register to put result.
     * @return the name of the register.
     */
    public Optional<Register> getDestination(){
        return Optional.ofNullable(destination.get());
    }

    /**
     * updates the register used by the microinstruction.
     * @param newDestination the new destination for the logical microinstruction.
     */
    public void setDestination(@Nullable Register newDestination){
        destination.set(newDestination);
    }

    public ObjectProperty<Register> destinationProperty() {
        return destination;
    }

    public ALUOperator getOperation() {
        return operation.get();
    }

    public ObjectProperty<ALUOperator> operationProperty() {
        return operation;
    }

    public void setOperation(ALUOperator operation) {
        this.operation.set(operation);
    }

    // Condition bits:

    public Optional<ConditionBit> getCarryBit() {
        return Optional.ofNullable(carryBit.get());
    }

    public ObjectProperty<ConditionBit> carryBitProperty() {
        return carryBit;
    }

    public void setCarryBit(@Nullable ConditionBit carryBit) {
        this.carryBit.set(carryBit);
    }

    public Optional<ConditionBit> getNegativeBit() {
        return Optional.ofNullable(negativeBit.get());
    }

    public ObjectProperty<ConditionBit> negativeBitProperty() {
        return negativeBit;
    }

    public void setNegativeBit(@Nullable ConditionBit negativeBit) {
        this.negativeBit.set(negativeBit);
    }

    public Optional<ConditionBit> getOverflowBit() {
        return Optional.ofNullable(overflowBit.get());
    }

    public ObjectProperty<ConditionBit> overflowBitProperty() {
        return overflowBit;
    }

    public void setOverflowBit(@Nullable ConditionBit overflowBit) {
        this.overflowBit.set(overflowBit);
    }

    public Optional<ConditionBit> getZeroBit() {
        return Optional.ofNullable(zeroBit.get());
    }

    public ObjectProperty<ConditionBit> zeroBitProperty() {
        return zeroBit;
    }

    public void setZeroBit(@Nullable ConditionBit zeroBit) {
        this.zeroBit.set(zeroBit);
    }


    /**
     * execute the micro instruction from machine
     */
    @Override
    public void execute() {
        BigInteger lhs = BigInteger.valueOf(this.lhs.get().getValue());
        BigInteger rhs = BigInteger.valueOf(this.rhs.get().getValue());
        BigInteger result = operation.get().apply(lhs, rhs);

        // Handle the any set condition bits:

        getZeroBit().ifPresent(zeroBit -> {
            if (result.equals(BigInteger.ZERO)) {
                zeroBit.set(1);
            }
        });

        getCarryBit().ifPresent(carryBit -> carryBit.set(0));

        getNegativeBit().ifPresent(negativeBit -> negativeBit.set(result.longValue() < 0));

        getOverflowBit().ifPresent(overflowBit -> {
            int width = destination.get().getWidth();
            BigInteger twoToWidthMinusOne = BigInteger.valueOf(2).pow(width-1);

            if (result.compareTo(twoToWidthMinusOne) >= 0 ||
                    result.compareTo(twoToWidthMinusOne.negate()) < 0) {
                overflowBit.set(1);
            } else {
                overflowBit.set(0);
            }
        });

        this.destination.get().setValue(result.longValue());
    }

    /**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
    @Override
    public boolean uses(Module<?> m){
        return (m == lhs.get() || m == rhs.get() || m == destination.get());
    }

    @Override
    public void validate() {
        super.validate();

        // get width of the lhs, rhs, and destination
        // registers, if they are different, then the validity
        // test fails
        Register lhs = Validate.getOptionalProperty(this, ArithmeticLogicOperation::lhsProperty);
        Register rhs = Validate.getOptionalProperty(this, ArithmeticLogicOperation::rhsProperty);
        Register dest = Validate.getOptionalProperty(this, ArithmeticLogicOperation::destinationProperty);

        if (!(lhs.getWidth() == rhs.getWidth() &&
                rhs.getWidth() == dest.getWidth())) {
            throw new ValidationException("At least one of the registers in the " +
                    "microinstruction \"" + getName() +
                    "\" has\na bit width that is different than one " +
                    "or more of the others.\nAll registers must have " +
                    "the same number of bits.");
        }
    }

    @Override
    public <U extends T> void copyTo(U other) {
        checkNotNull(other);

        other.setName(getName());

        other.setDestination(getDestination().orElse(null));
        other.setLhs(getLhs().orElse(null));
        other.setRhs(getRhs().orElse(null));

        other.setOperation(getOperation());

        other.setCarryBit(getCarryBit().orElse(null));
        other.setNegativeBit(getNegativeBit().orElse(null));
        other.setOverflowBit(getOverflowBit().orElse(null));
        other.setZeroBit(getZeroBit().orElse(null));
    }

    /**
     * returns the XML description
     * @return the XML description
     */
    @Override
    public String getXMLDescription(String indent) {
        StringBuilder bld = new StringBuilder();

        bld.append(indent);
        bld.append("<");
        bld.append(getClass().getSimpleName());
        bld.append(" name=\"");
        bld.append(getHTMLName());
        bld.append("\" id=\"");
        bld.append(getID());
        bld.append("\" type=\"");
        bld.append(getOperation());
        bld.append("\" source1=\"");
        bld.append(getLhs());
        bld.append("\" source2=\"");
        bld.append(getRhs());
        bld.append("\" destination=\"");
        bld.append(getDestination());
        bld.append("\" ");

        getCarryBit().ifPresent(carryBit -> {
            bld.append("carryBit=\"");
            bld.append(carryBit.getID());
            bld.append("\" ");
        });

        getNegativeBit().ifPresent(negativeBit -> {
            bld.append("negativeBit=\"");
            bld.append(negativeBit.getID());
            bld.append("\" ");
        });

        getOverflowBit().ifPresent(overflowBit -> {
            bld.append("overflowBit=\"");
            bld.append(overflowBit.getID());
            bld.append("\" ");
        });

        getZeroBit().ifPresent(zeroBit -> {
            bld.append("zeroBit=\"");
            bld.append(zeroBit.getID());
            bld.append("\" ");
        });

        bld.append(" />");

        return bld.toString();
    }

    @Override
    public String getHTMLDescription(String indent){
        StringBuilder bld = new StringBuilder();

        bld.append(indent);
        bld.append("<TR><TD>");
        bld.append(getHTMLName());
        bld.append("</TD><TD>");
        bld.append(getOperation());
        bld.append("</TD><TD>");
        bld.append(getLhs());
        bld.append("</TD><TD>");
        bld.append(getRhs());
        bld.append("</TD><TD>");
        bld.append(getDestination());
        bld.append("</TD>");

        getCarryBit().ifPresent(carryBit -> {
            bld.append("<TD>");
            bld.append(carryBit.getName());
            bld.append("</TD>");
        });

        getNegativeBit().ifPresent(negativeBit -> {
            bld.append("<TD>");
            bld.append(negativeBit.getName());
            bld.append("</TD>");
        });

        getOverflowBit().ifPresent(overflowBit -> {
            bld.append("<TD>");
            bld.append(overflowBit.getName());
            bld.append("</TD>");
        });

        getZeroBit().ifPresent(zeroBit -> {
            bld.append("<TD>");
            bld.append(zeroBit.getName());
            bld.append("</TD>");
        });

        bld.append("</TR>");

        return bld.toString();
    }
}
