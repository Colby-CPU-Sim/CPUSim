package cpusim.gui.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import cpusim.model.Machine;
import cpusim.model.microinstruction.Microinstruction;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handles drag behaviour for {@link Microinstruction} values or {@link List} index positions.
 * @since 2016-12-04
 */
public final class MicroinstructionDragHelper implements MachineBound {

    private static final String ENCODING_CHAR = new String(Character.toChars('$'));
    private static final String ENCODING_INDEX = "index";
    private static final String ENCODING_MICROID = "micro_id";

    private final Logger logger = LoggerFactory.getLogger(MicroinstructionDragHelper.class);

    private final ObjectProperty<Machine> machine;

    public MicroinstructionDragHelper(ObjectProperty<Machine> machineProperty) {
        this.machine = checkNotNull(machineProperty);
    }

    @Override
    public ObjectProperty<Machine> machineProperty() {
        return machine;
    }

    /**
     * Inserts an index into the {@link Dragboard}
     * @param db Current {@code Dragboard}
     * @param index Index into a {@link List}
     */
    public void insertIntoDragboard(Dragboard db, int index) {
        checkNotNull(db);

        final String encoded = Joiner.on(ENCODING_CHAR)
                .join(ENCODING_INDEX, Integer.toString(index));

        ClipboardContent cc = new ClipboardContent();
        cc.putString(encoded);

        db.setContent(cc);
    }

    /**
     * Inserts an {@link Microinstruction} reference from the {@link #machineProperty()} into the {@link Dragboard}
     * @param db Current {@code Dragboard}
     * @param micro a {@code Microinstruction} to be inserted.
     */
    public void insertIntoDragboard(Dragboard db, final Microinstruction<?> micro) {
        checkNotNull(db);
        checkNotNull(micro);

        final String encoded = Joiner.on(ENCODING_CHAR)
                .join(ENCODING_MICROID,
                        micro.getClass().getName(),
                        micro.getID());

        ClipboardContent cc = new ClipboardContent();
        cc.putString(encoded);

        db.setContent(cc);
    }

    /**
     * Parses the content of a {@link Dragboard} and attempts to call the correct {@link HandleDragBehaviour} method.
     * This is a designation of the Visitor pattern.
     *
     * @param db The {@code Dragboard} to parse data from.
     * @param handler Visitor that will "visit" the right node
     */
    public void parseDragboard(Dragboard db, HandleDragBehaviour handler) {
        checkNotNull(db);
        checkNotNull(handler);

        List<String> tokens = Splitter.on(ENCODING_CHAR).omitEmptyStrings().splitToList(db.getString());
        String type = tokens.get(0);

        switch (type) {
            case ENCODING_INDEX: {
                // FIXME finish
                checkArgument(tokens.size() == 2);
                int index = Integer.parseInt(tokens.get(1));
                handler.onDragIndex(index);

            } break;

            case ENCODING_MICROID: {
                checkArgument(tokens.size() == 3);
                try {
                    @SuppressWarnings("unchecked") // if it's bad, it'll throw and be caught
                            Class<? extends Microinstruction<?>> microClass = (Class<? extends Microinstruction<?>>) Class.forName(tokens.get(1));
                    UUID id = UUID.fromString(tokens.get(2));

                    final Machine machine = this.machine.getValue();

                    List<Microinstruction<?>> micros = machine.getMicrosUnsafe(microClass);
                    Optional<Microinstruction<?>> microOpt = micros.stream()
                            .filter(m -> m.getID().equals(id))
                            .findFirst();

                    if (microOpt.isPresent()) {
                        handler.onDragMicro(microOpt.get());
                    } else {
                        logger.debug("Was passed a microinstruction that the Machine does not know about: {}@{}",
                                microClass, id);
                        handler.onOther();
                    }
                } catch (ClassNotFoundException | ClassCastException ce) {
                    logger.debug("Could not get microClass from token: " + tokens.get(1), ce);
                    handler.onOther();
                }
            } break;

            default: {
                logger.debug("Unknown dragboard sequence: {}", db.getString());
                handler.onOther();
            }
        }
    }


    /**
     * Denotes behaviour for handling when something encoded by {@link MicroinstructionDragHelper} is dropped
     * into a component. This is the "visitor" for this implementation.
     */
    public interface HandleDragBehaviour {

        void onDragIndex(int value);

        void onDragMicro(Microinstruction<?> micro);

        default void onOther() {
            // no-opt
        }

    }
}
