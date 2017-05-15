package cpusim.model.util.structure;

import cpusim.model.module.*;
import javafx.collections.ObservableList;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Used to visit all of the {@link Module Modules} of a {@link cpusim.model.Machine}.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Visitor_pattern">Visitor Pattern</a>
 */
@ParametersAreNonnullByDefault
public interface ModuleVisitor {

    VisitResult startRams(ObservableList<RAM> rams);

    VisitResult visitRam(RAM ram);

    VisitResult endRams(ObservableList<RAM> rams);

    VisitResult startConditionBits(ObservableList<ConditionBit> conditionBits);

    VisitResult visitConditionBit(ConditionBit conditionBit);

    VisitResult endConditionBits(ObservableList<ConditionBit> conditionBits);

    VisitResult startRegisters(ObservableList<Register> registers);

    VisitResult visitRegister(Register register);

    VisitResult endRegisters(ObservableList<Register> registers);

    VisitResult startRegisterArrays(ObservableList<RegisterArray> registerArrays);

    VisitResult visitRegisterArray(RegisterArray registerArray);

    VisitResult endRegisterArray(ObservableList<RegisterArray> registerArrays);

    VisitResult visitCodeStore(@Nullable RAM codeStore);

    VisitResult visitProgramCounter(@Nullable Register pc);

    VisitResult visitControlUnit(@Nullable ControlUnit controlUnit);
}
