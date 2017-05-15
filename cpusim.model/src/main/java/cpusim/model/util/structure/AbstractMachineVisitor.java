package cpusim.model.util.structure;

import cpusim.model.Field;
import javafx.collections.ObservableList;

import java.util.Optional;

/**
 * Provides a base implementation of {@link MachineVisitor} that returns
 * {@link VisitResult#Continue} for all components and {@link Optional#empty()} for
 * the two child Visitors.
 */
public abstract class AbstractMachineVisitor implements MachineVisitor {

    @Override
    public Optional<ModuleVisitor> getModuleVisitor() {
        return Optional.empty();
    }

    @Override
    public VisitResult startModules() {
        return VisitResult.Continue;
    }

    @Override
    public VisitResult endModules() {
        return VisitResult.Continue;
    }

    @Override
    public Optional<MicroinstructionVisitor> getMicrosVisitor() {
        return Optional.empty();
    }

    @Override
    public VisitResult startMicros() {
        return VisitResult.Continue;
    }

    @Override
    public VisitResult endMicros() {
        return VisitResult.Continue;
    }

    @Override
    public VisitResult visitName(String name) {
        return VisitResult.Continue;
    }

    @Override
    public VisitResult visitIndexFromRight(boolean indexFromRight) {
        return VisitResult.Continue;
    }

    @Override
    public VisitResult visitStartingAddressForLoading(int loadingAddress) {
        return VisitResult.Continue;
    }

    @Override
    public VisitResult startFields(ObservableList<Field> fields) {
        return VisitResult.Continue;
    }

    @Override
    public VisitResult visitField(Field field) {
        return VisitResult.Continue;
    }

    @Override
    public VisitResult endFields(ObservableList<Field> fields) {
        return VisitResult.Continue;
    }
}
