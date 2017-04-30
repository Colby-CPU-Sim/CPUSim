package cpusim.model.util.structure;

import cpusim.model.Field;
import cpusim.model.Machine;
import javafx.collections.ObservableList;

import java.util.Optional;

/**
 * Accepted by {@link Machine#acceptVisitor(MachineVisitor)} to allow easier traversal
 * of a specified {@link Machine}.
 *
 * The traversal will visit all of the following properties in <em>some ordering</em>:
 * <ul>
 *     <li>
 *         {@link cpusim.model.module.Module Modules} via {@link #startModules()} if {@link #getModuleVisitor()}
 *         is present
 *     </li>
 *     <li>
 *         {@link cpusim.model.microinstruction.Microinstruction Microinstructions} via {@link #startMicros()}
 *         if {@link #getMicrosVisitor()} is present</li>
 *     <li>
 *         The indexing property
 *     </li>
 * </ul></il></li>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Visitor_pattern">Visitor Pattern</a>
 */
public interface MachineVisitor {

    Optional<ModuleVisitor> getModuleVisitor();

    VisitResult startModules();

    VisitResult endModules();

    /**
     * Returns a {@link MicroinstructionVisitor} to use in traversing the
     * {@link cpusim.model.microinstruction.Microinstruction Microinstructions}. If this value is not
     * {@link Optional#isPresent() present}, then the traversal will not visit the children ({@link #startMicros()} is
     * still called.
     *
     * @return
     */
    Optional<MicroinstructionVisitor> getMicrosVisitor();

    VisitResult startMicros();

    VisitResult endMicros();

    VisitResult startFields(ObservableList<Field> fields);

    VisitResult visitField(Field field);

    VisitResult endFields(ObservableList<Field> fields);

    /**
     * States whether the machine indexes from the right ({@code true}) or left.
     * @param indexFromRight See {@link Machine#isIndexFromRight()}
     * @return State of traversal
     */
    VisitResult visitIndexFromRight(boolean indexFromRight);

    /**
     *
     * @param loadingAddress
     * @return
     */
    VisitResult visitStartingAddressForLoading(int loadingAddress);

    /**
     * Gets the {@link Machine#getName() name} of the {@link Machine}.
     * @param name Name of the Machine
     * @return State of the traversal
     *
     * @see Machine#getName()
     */
    VisitResult visitName(String name);
}
