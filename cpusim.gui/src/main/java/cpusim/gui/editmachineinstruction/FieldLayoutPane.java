package cpusim.gui.editmachineinstruction;

import cpusim.model.Field;
import cpusim.model.MachineInstruction;
import cpusim.model.util.Colors;
import cpusim.util.MoreBindings;
import javafx.beans.NamedArg;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.Subscription;

import javax.annotation.Nullable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Lays out colourful fields with a simple label in the center. This control allows for rearranging and is backed by
 * a list of Field, Color pairs.
 *
 * @since 2016-12-06
 */
public class FieldLayoutPane extends HBox {

    private final ListProperty<Field> fields;

    private final ObjectProperty<MachineInstruction.FieldType> fieldType;

    private final ObjectProperty<MachineInstruction> currentInstruction;

    private final MapProperty<Field, Color> fieldColorMap;


    public FieldLayoutPane(@NamedArg("fieldType") MachineInstruction.FieldType fieldType) {
        this.currentInstruction = new SimpleObjectProperty<>(this, "currentInstruction", null);
        this.fields = new SimpleListProperty<>(this, "fields", FXCollections.observableArrayList());
        this.fieldType = new SimpleObjectProperty<>(this, "fieldType", fieldType);

        this.fieldColorMap = new SimpleMapProperty<>(this, "fieldColorMap", FXCollections.observableHashMap());

        EasyBind.subscribe(currentInstruction, this::changeInstruction);
    }


    private NumberBinding widthPerBit;
    private Subscription childBinding;

    // This class solves a nasty bug: So, EasyBind.map() calls it's mapping function
    // against the value using mapper.apply(element) which, when using `FieldLabel::new` as
    // the mapping function, it creates a new Label and thus can't actually delete the previous one
    // which means that when the removal events were happening, it was trying to remove a NEW label
    // instead of the one that should have been removed. Using a small cache solves this by trying to
    // get a value from the cache instead of just spontaneously creating them.
    // I feel like EasyBind.map() should use the same mechanism by default.. it leads to some odd behaviours
    // and I can not fathom a reason to not have it


    private void changeInstruction(@Nullable MachineInstruction instruction) {
        ObservableList<Node> children = getChildren();
        if (childBinding != null) {
            childBinding.unsubscribe();
        }

        fields.unbind();
        fields.clear();
        children.clear();

        if (instruction != null) {
            widthPerBit = Bindings.divide(widthProperty(),
                    Bindings.createDoubleBinding(() -> instruction.numBitsProperty().doubleValue(), instruction.numBitsProperty()));

            // bind fields first, that way the children get bound properly
            childBinding = EasyBind.listBind(children, MoreBindings.orderedMap(fields, FieldLabel::new));

            fields.bind(instruction.fieldsProperty(this.fieldType.get()));
       } else {
            // make sure to unbind if the instruction isn't valid

        }
    }

    public ListProperty<Field> fieldsProperty() {
        return fields;
    }

    public MachineInstruction.FieldType getFieldType() {
        return fieldType.get();
    }

    public void setFieldType(MachineInstruction.FieldType fieldType) {
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

            Color color = fieldColorMap.computeIfAbsent(field, ignore -> Colors.generateRandomLightColor());
            this.setBackground(new Background(new BackgroundFill(color, null, null)));
            this.setTextFill(color.darker().darker());

            NumberBinding width = widthPerBit.multiply(field.numBitsProperty());
            this.prefWidthProperty().bind(width);
            this.maxWidthProperty().bind(width);

            HBox.setHgrow(this, Priority.ALWAYS);

            this.textProperty().bind(field.nameProperty());
            this.tooltipProperty().bind(Bindings.createObjectBinding(() -> {
                Tooltip tip = new Tooltip();
                tip.textProperty()
                        .bind(Bindings.format("%s\nWidth: %d", field.nameProperty(), field.numBitsProperty()));

                return tip;
            }));

            this.setAlignment(Pos.CENTER);
        }

        public Field getField() {
            return field;
        }

        // Due to using the Field as the "discriminant" within the labels, we use it
        // as the equals and hashcode operations.

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof FieldLabel)) return false;

            return super.equals(obj) && ((FieldLabel) obj).getField().equals(field);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), this.field.hashCode());
        }
    }
}
