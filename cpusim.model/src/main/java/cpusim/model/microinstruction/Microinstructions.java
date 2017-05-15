package cpusim.model.microinstruction;

import com.google.common.collect.ImmutableSet;

/**
 * Utility class for methods surrounding {@link Microinstruction}.
 */
public abstract class Microinstructions {

    private Microinstructions() {
        // no instantiate
    }
    /**
     * Get all of the supported implementing {@link Class} values for the {@link Microinstruction}.
     * @return Immutable set of all the classes
     */
    // TODO replace with injection hopefully.. :(
    public static ImmutableSet<Class<? extends Microinstruction<?>>> getMicroClasses() {
        ImmutableSet.Builder<Class<? extends Microinstruction<?>>> bld = ImmutableSet.builder();
        bld.add(Arithmetic.class);
        bld.add(Branch.class);
        bld.add(Decode.class);
        bld.add(End.class);
        bld.add(Comment.class);
        bld.add(Increment.class);
        bld.add(IO.class);
        bld.add(Logical.class);
        bld.add(MemoryAccess.class);
        bld.add(SetBits.class);
        bld.add(SetCondBit.class);
        bld.add(Shift.class);
        bld.add(Test.class);
        bld.add(TransferRtoR.class);
        bld.add(TransferRtoA.class);
        bld.add(TransferAtoR.class);
        return bld.build();
    }
}
