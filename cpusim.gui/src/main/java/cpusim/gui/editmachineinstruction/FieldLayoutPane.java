package cpusim.gui.editmachineinstruction;

import cpusim.model.Field;
import cpusim.model.MachineInstruction;
import cpusim.model.util.Colors;
import javafx.beans.NamedArg;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.fxmisc.easybind.EasyBind;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Lays out colourful fields with a simple label in the center. This control allows for rearranging and is backed by
 * a list of Field, Color pairs.
 *
 * @since 2016-12-06
 */
public class FieldLayoutPane extends HBox {

    public enum FieldType {
        Assembly,
        Instruction
    }

    private final ListProperty<Field> fields;

    private final ObjectProperty<FieldType> fieldType;

    private final ObjectProperty<MachineInstruction> currentInstruction;

    private final MapProperty<Field, Color> fieldColorMap;

    public FieldLayoutPane(@NamedArg("fieldType") FieldType fieldType) {
        this.currentInstruction = new SimpleObjectProperty<>(this, "currentInstruction", null);
        this.fields = new SimpleListProperty<>(this, "fields", FXCollections.observableArrayList());
        this.fieldType = new SimpleObjectProperty<>(this, "fieldType", fieldType);

        this.fieldColorMap = new SimpleMapProperty<>(this, "fieldColorMap", FXCollections.observableHashMap());

        ObservableList<Node> children = getChildren();

        this.fields.addListener(getFieldListChangeListener(children, FieldLabel::new));
        children.addListener(getFieldListChangeListener(this.fields, n -> ((FieldLabel)n).getField()));

        EasyBind.subscribe(currentInstruction, currentInstruction -> {
            if (currentInstruction != null) {
                switch (this.fieldType.getValue()) {
                    case Assembly:
                        this.fields.bind(currentInstruction.assemblyFieldsProperty());
                        break;

                    case Instruction:
                        this.fields.bind(currentInstruction.instructionFieldsProperty());
                        break;
                }
            }
        });
    }

    private <E1, E2> ListChangeListener<E1> getFieldListChangeListener(ObservableList<E2> other,
                                                                       Function<E1, ? extends E2> mapper) {
        return change -> {
            List<? extends E1> source = change.getList();

            while (change.next()) {

                int from = change.getFrom();
                int to = change.getTo();
                if (change.wasPermutated()) {
                    List<E2> newLabels = source.subList(from, to).stream()
                            .map(mapper)
                            .collect(Collectors.toList());

                    other.subList(from, to).clear();
                    other.addAll(from, newLabels);
                } else {
                    List<E2> newLabels = source.subList(from, from + change.getAddedSize())
                            .stream()
                            .map(mapper)
                            .collect(Collectors.toList());

                    other.subList(from, from + change.getRemovedSize()).clear();
                    other.addAll(from, newLabels);
                }
            }
        };
    }

    public ListProperty<Field> fieldsProperty() {
        return fields;
    }

    public FieldType getFieldType() {
        return fieldType.get();
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType.set(fieldType);
    }

    public MachineInstruction getCurrentInstruction() {
        return currentInstruction.get();
    }

    public ObjectProperty<MachineInstruction> currentInstructionProperty() {
        return currentInstruction;
    }

    /**
     * Used internally for representing a marker in the view
     */
    class FieldLabel extends Label {
        private final Field field;

        FieldLabel(Field field) {
            this.field = checkNotNull(field);

            this.setTextFill(fieldColorMap.computeIfAbsent(field, f2 -> Colors.generateRandomLightColor()));
        }

        public Field getField() {
            return field;
        }
    }
}
