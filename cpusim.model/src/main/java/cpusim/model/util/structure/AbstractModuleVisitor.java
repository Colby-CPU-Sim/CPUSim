package cpusim.model.util.structure;

import cpusim.model.module.*;
import javafx.collections.ObservableList;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Base implementation of {@link ModuleVisitor} that returns {@link VisitResult#Continue} for
 * all methods.
 */
@ParametersAreNonnullByDefault
public abstract class AbstractModuleVisitor implements ModuleVisitor {

    @Override
    public VisitResult startRams(final ObservableList<RAM> rams) {
        return VisitResult.Continue;
    }

    @Override
    public VisitResult visitRam(final RAM ram) {
        return VisitResult.Continue;
    }

    @Override
    public VisitResult endRams(final ObservableList<RAM> rams) {
        return VisitResult.Continue;
    }

    @Override
    public VisitResult startConditionBits(final ObservableList<ConditionBit> conditionBits) {
        return VisitResult.Continue;
    }

    @Override
    public VisitResult visitConditionBit(final ConditionBit conditionBit) {
        return VisitResult.Continue;
    }

    @Override
    public VisitResult endConditionBits(final ObservableList<ConditionBit> conditionBits) {
        return VisitResult.Continue;
    }


    @Override
    public VisitResult startRegisters(final ObservableList<Register> registers) {
        return VisitResult.Continue;
    }

    @Override
    public VisitResult visitRegister(final Register register) {
        return VisitResult.Continue;
    }

    @Override
    public VisitResult endRegisters(final ObservableList<Register> registers) {
        return VisitResult.Continue;
    }

    @Override
    public VisitResult startRegisterArrays(final ObservableList<RegisterArray> registerArrays) {
        return VisitResult.Continue;
    }

    @Override
    public VisitResult visitRegisterArray(final RegisterArray registerArray) {
        return VisitResult.Continue;
    }

    @Override
    public VisitResult endRegisterArray(final ObservableList<RegisterArray> registerArrays) {
        return VisitResult.Continue;
    }

    // Specialized modules

    @Override
    public VisitResult visitCodeStore(@Nullable final RAM codeStore) {
        return VisitResult.Continue;
    }

    @Override
    public VisitResult visitProgramCounter(@Nullable final Register pc) {
        return VisitResult.Continue;
    }

    @Override
    public VisitResult visitControlUnit(@Nullable final ControlUnit controlUnit) {
        return VisitResult.Continue;
    }
}
