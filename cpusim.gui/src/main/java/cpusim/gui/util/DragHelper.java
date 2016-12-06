package cpusim.gui.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import cpusim.model.Field;
import cpusim.model.Machine;
import cpusim.model.Module;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.util.ClassCleaner;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handles drag behaviour for {@link Microinstruction} values or {@link List} index positions.
 * @since 2016-12-04
 */
public final class DragHelper implements MachineBound {

    // Note: Can't use $ because Java classes use it :)
    private static final String ENCODING_CHAR = new String(Character.toChars('#'));

    private static final DataFormat MODULE_FORMAT = new DataFormat(Module.class.getName());

    private static final DataFormat MICRO_FORMAT = new DataFormat(Microinstruction.class.getName());

    private static final DataFormat FIELD_FORMAT = new DataFormat(Field.class.getName());

    private static final DataFormat INDEX_FORMAT = new DataFormat("list.index");


    private final Logger logger = LoggerFactory.getLogger(DragHelper.class);

    private final ObjectProperty<Machine> machine;

    private final Dragboard dragboard;

    private ClipboardContent clipboardContent = new ClipboardContent();

    public DragHelper(ObjectProperty<Machine> machineProperty, final Dragboard dragboard) {
        this.machine = new SimpleObjectProperty<>(this, "machine", null);
        this.machine.bind(checkNotNull(machineProperty));

        this.dragboard = checkNotNull(dragboard);
    }

    @Override
    public ObjectProperty<Machine> machineProperty() {
        return machine;
    }

    private boolean setContent(ClipboardContent cc) {
        dragboard.clear();
        return dragboard.setContent(cc);
    }

    /**
     * Inserts an index into the {@link Dragboard}
     * @param index Index into a {@link List}
     * @return See {@link Dragboard#setContent(Map)}
     */
    public boolean setIndexContent(int index) {
        clipboardContent.put(INDEX_FORMAT, index);

        return setContent(clipboardContent);
    }

    /**
     * Inserts an {@link Microinstruction} reference from the {@link #machineProperty()} into the {@link Dragboard}
     * @param micro a {@code Microinstruction} to be inserted.
     *
     * @return See {@link Dragboard#setContent(Map)}
     */
    public boolean setMicroContent(final Microinstruction<?> micro) {
        checkNotNull(micro);

        clipboardContent.put(MICRO_FORMAT,
                Joiner.on(ENCODING_CHAR).join(micro.getClass().getName(), micro.getID()));

        return setContent(clipboardContent);
    }

    /**
     * Parses the content of a {@link Dragboard} and attempts to call the correct {@link HandleDragBehaviour} method.
     * This is a designation of the Visitor pattern.
     *
     * @param handler Visitor that will "visit" the right node
     */
    public void visit(HandleDragBehaviour handler) {
        checkNotNull(handler);

        for (DataFormat format: dragboard.getContentTypes()) {
            if (format == INDEX_FORMAT) {
                handler.onDragIndex((Integer)dragboard.getContent(format));
            } else if (format == MICRO_FORMAT) {
                // We have MICRO_FORMAT content
                List<String> tokens = Splitter.on(ENCODING_CHAR)
                        .omitEmptyStrings()
                        .splitToList((String)dragboard.getContent(format));
                checkArgument(tokens.size() == 2);

                try {
                    @SuppressWarnings("unchecked") // if it's bad, it'll throw and be caught
                    Class<? extends Microinstruction<?>> microClass
                            = (Class<? extends Microinstruction<?>>) ClassCleaner.forName(tokens.get(0));
                    final UUID id = UUID.fromString(tokens.get(1));

                    final Machine machine = this.machine.getValue();

                    List<Microinstruction<?>> micros = machine.getMicrosUnsafe(microClass);
                    Optional<Microinstruction<?>> microOpt = micros.stream()
                            .filter(m -> m.getID().equals(id))
                            .findFirst();

                    if (microOpt.isPresent()) {
                        handler.onDragMicro(microOpt.get());
                    } else {
                        logger.warn("Was passed a microinstruction that the Machine does not know about: {}@{}",
                                microClass, id);
                        handler.onOther(format, dragboard.getContent(format));
                    }
                } catch (ClassNotFoundException | ClassCastException ce) {
                    logger.error("Could not get microClass from token: {}", tokens.get(0), ce);
                    handler.onOther(format, dragboard.getContent(format));
                }

            } else if (format == MODULE_FORMAT) {
                // FIXME actually handle this :)
                handler.onOther(format, dragboard.getContent(format));
            } else {
                handler.onOther(format, dragboard.getContent(format));
            }
        }
    }


    /**
     * Denotes behaviour for handling when something encoded by {@link DragHelper} is dropped
     * into a component. This is the "visitor" for this implementation.
     */
    public interface HandleDragBehaviour {

        void onDragIndex(int value);

        void onDragMicro(Microinstruction<?> micro);

        default void onOther(DataFormat format, Object value) {
            // no-opt
        }

    }
}
