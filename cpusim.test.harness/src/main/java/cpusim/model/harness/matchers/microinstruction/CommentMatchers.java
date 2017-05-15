package cpusim.model.harness.matchers.microinstruction;

import cpusim.model.Machine;
import cpusim.model.microinstruction.Comment;
import org.hamcrest.Matcher;

import static cpusim.model.harness.matchers.microinstruction.MicroinstructionMatchers.microinstruction;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.compose;

/**
 * {@link Matcher Matchers} for the {@link Comment} {@link cpusim.model.microinstruction.Microinstruction}
 */
public abstract class CommentMatchers {

    private CommentMatchers() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a {@link Matcher} for a {@link Comment} micro.
     *
     * @return Matcher
     * @see Comment
     */
    public static Matcher<Comment> comment(Machine machine, Comment expected) {
        return compose("Comment", microinstruction(machine, expected));
    }
}
