package cpusim.model.microinstruction;

import cpusim.model.Machine;
import cpusim.model.module.ConditionBit;
import cpusim.model.module.Register;

import java.math.BigInteger;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * The logical microinstructions perform the bit operations of AND, OR, NOT, NAND,
 * NOR, or XOR on the specified registers.
 */
public class Logical extends ArithmeticLogicOperation<Logical> {

    public enum Type implements ALUOperator {
        AND(BigInteger::and),

        OR(BigInteger::or),

        XOR(BigInteger::xor),

        NAND((lhs, rhs) -> AND.apply(lhs, rhs).not()),

        NOR((lhs, rhs) -> OR.apply(lhs, rhs).not()),

        NOT((lhs, _rhs) -> lhs.not());

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
     * creates a new {@link Logical} object with input values.
     *  @param name name of the microinstruction.
     * @param id Unique ID of instruction
     * @param machine the machine that the microinstruction belongs to.
     * @param type type of logical microinstruction.
     * @param lhs the source1 register.
     * @param rhs the source2 register.
     * @param destination the destination register.
     */
    public Logical(String name,
                   UUID id,
                   Machine machine,
                   Type type,
                   Register destination,
                   Register lhs,
                   Register rhs,
                   ConditionBit zeroBit) {
        super(name, id, machine, type, destination, lhs, rhs, null, null, null, zeroBit);
    }

    @Override
    public Set<Type> validOperations() {
        return EnumSet.allOf(Type.class);
    }

    @Override
    public Logical cloneFor(IdentifierMap oldToNew) {
       return new Logical(getName(), UUID.randomUUID(), oldToNew.getNewMachine(),
               (Type) getOperation(),
               oldToNew.get(getDestination().orElse(null)),
               oldToNew.get(getLhs().orElse(null)),
               oldToNew.get(getRhs().orElse(null)),
               oldToNew.copyOrNull(getZeroBit()));
    }

}