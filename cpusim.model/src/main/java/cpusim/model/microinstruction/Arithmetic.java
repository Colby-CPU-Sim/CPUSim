package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.Register;
import cpusim.model.util.MachineComponent;
import javafx.beans.property.ListProperty;

import java.math.BigInteger;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The arithmetic microinstruction use three registers and optionally two condition
 * bits performing arithmetic operations (e.g. add, subtract).
 *
 * @since 2013-06-06
 */
public class Arithmetic extends ArithmeticLogicOperation<Arithmetic> {

    public enum Type implements ALUOperator {

        ADD(BigInteger::add),
        SUBTRACT(BigInteger::subtract),
        MULTIPLY(BigInteger::multiply),
        DIVIDE(BigInteger::divide);

        private final ALUOperator operation;

        Type(ALUOperator operation) {
            this.operation = checkNotNull(operation);
        }

        @Override
        public BigInteger apply(BigInteger lhs, BigInteger rhs) {
            return operation.apply(lhs, rhs);
        }
    }
    
    /**
     * Constructor
     * creates a new Increment object with input values.
     *
     * @param name name of the microinstruction.
     * @param id UUID for the microinstruction
     * @param machine machine that the microinstruction belongs to.
     * @param type type of logical microinstruction.
     * @param destination the destination register.
     * @param lhs the source1 register.
     * @param rhs the source2 register.
     */
    public Arithmetic(String name,
                      UUID id,
                      Machine machine,
                      Type type,
                      Register destination,
                      Register lhs,
                      Register rhs,
                      ConditionBit carryBit,
                      ConditionBit overflowBit,
                      ConditionBit zeroBit) {
        super(name, id, machine, type, destination, lhs, rhs, carryBit, null, overflowBit, zeroBit);
    }
    
    /**
     * Copy constructor.
     *
     * @param other Copied from.
     */
    public Arithmetic(Arithmetic other) {
        this(other.getName(),
                UUID.randomUUID(),
                other.getMachine(),
                (Type) other.getOperation(),
                other.getDestination(),
                other.getLhs(),
                other.getRhs(),
                other.getCarryBit().orElse(null),
                other.getOverflowBit().orElse(null),
                other.getZeroBit().orElse(null));
    }

    @Override
    public Set<Type> validOperations() {
        return EnumSet.allOf(Type.class);
    }

    /**
     * execute the micro instruction from machine
     */
    @Override
    public void execute() {
        super.execute();

        getCarryBit().ifPresent(carryBit -> {

            long lhs = getLhs().getValue();
            long rhs = getRhs().getValue();

            carryBit.set(0);

            //set the carry bit if necessary
            if (getOperation().equals(Type.ADD) &&
                    ((lhs < 0 && rhs < 0) ||
                            (lhs < 0 && rhs >= -lhs) ||
                            (lhs < 0 && rhs >= -lhs))) {
                carryBit.set(1);
            }
        });
    }
    
    @Override
    public Arithmetic cloneFor(MachineComponent.IdentifierMap oldToNew) {
        checkNotNull(oldToNew);

        return new Arithmetic(this.getName(), UUID.randomUUID(), oldToNew.getNewMachine(),
                (Type)getOperation(), oldToNew.get(getDestination()),
                oldToNew.get(getLhs()), oldToNew.get(getRhs()),
                oldToNew.copyOrNull(getCarryBit()), oldToNew.copyOrNull(getOverflowBit()),
                oldToNew.copyOrNull(getZeroBit()));
    }
}
