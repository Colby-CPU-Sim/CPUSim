package cpusim.model.util.structure;

/**
 * Used to control the traversal pattern when visiting the components of a {@link cpusim.model.Machine}.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Visitor_pattern">Visitor Pattern</a>
 */
public enum VisitResult {

    /**
     * Stop the traversal
     */
    Stop,

    /**
     * Skip children, but go to siblings
     */
    SkipChildren,

    /**
     * Skip the following siblings, this implies {@link #SkipChildren}.
     */
    SkipSiblings,

    /**
     * Continue with no changes.
     */
    Continue

}
