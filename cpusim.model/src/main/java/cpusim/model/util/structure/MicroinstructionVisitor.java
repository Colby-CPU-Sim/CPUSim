package cpusim.model.util.structure;

import cpusim.model.microinstruction.Microinstruction;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Visits the {@link Microinstruction Microinstructions} components of a {@link cpusim.model.Machine}.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Visitor_pattern">Visitor Pattern</a>
 */
@ParametersAreNonnullByDefault
public interface MicroinstructionVisitor {

    VisitResult visitCategory(final String category);

    VisitResult visitType(final Class<? extends Microinstruction<?>> clazz);

    VisitResult visitMicro(final Microinstruction<?> micro);

}
