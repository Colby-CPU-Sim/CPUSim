package cpusim.gui.editmachineinstruction;

import cpusim.model.Field;
import cpusim.model.MachineInstruction;
import cpusim.model.util.Colors;
import javafx.beans.NamedArg;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fxmisc.easybind.EasyBind;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private final Logger logger = LogManager.getLogger(FieldLayoutPane.class);


    public FieldLayoutPane(@NamedArg("fieldType") MachineInstruction.FieldType fieldType) {
        this.currentInstruction = new SimpleObjectProperty<>(this, "currentInstruction", null);
        this.fields = new SimpleListProperty<>(this, "fields", FXCollections.observableArrayList());
        this.fieldType = new SimpleObjectProperty<>(this, "fieldType", fieldType);

        this.fieldColorMap = new SimpleMapProperty<>(this, "fieldColorMap", FXCollections.observableHashMap());

        EasyBind.subscribe(currentInstruction, this::changeInstruction);

        this.fields.addListener((ListChangeListener<Field>) c -> {

            // This class change listener was required because in order to map the fields property to
            // the children, I manually had to make sure the indexes lined up. Unfortunately, using
            // EasyBind.map() doesn't work because it counts on the mapping function returning the same value
            // for a map when removing elements. This means some form of uniqueness. Additionally, if caching, it
            // causes the damned thing to add duplicate labels. Thus, I had to manually put in a ListChangeListener,
            // however, this is probably much more efficient anyway.

            ObservableList<Node> nChildren = getChildren();
            List<FieldLabel> children = nChildren.stream()
                    .map(v -> (FieldLabel)v)
                    .collect(Collectors.toList());

            while (c.next()) {
                if (c.wasPermutated()) {
                    for (int i = c.getFrom(); i < c.getTo(); ++i) {
                        nChildren.set(c.getPermutation(i), children.get(i));
                    }
                } else if (c.wasUpdated()) {
                    //update or replace item, either way, need to change the Label
                    for (int i = c.getFrom(); i < c.getTo(); ++i) {
                        nChildren.set(i, new FieldLabel(fields.get(i)));
                    }
                } else if (c.wasReplaced()) {
                    //update or replace item, either way, need to change the Label
                    int i = c.getFrom();
                    for ( ; i < c.getTo() && i < nChildren.size(); ++i) {
                        nChildren.set(i, new FieldLabel(fields.get(i)));
                    }
                    
                    for ( ; i < c.getTo(); ++i) {
                        nChildren.add(i, new FieldLabel(fields.get(i)));
                    }
                } else if (c.wasRemoved()) {
                    // https://docs.oracle.com/javase/8/javafx/api/javafx/collections/ListChangeListener.Change.html#getFrom--
                    // If removing, then it returns the same index and the number to remove.
                    for (int i = c.getFrom() + c.getRemovedSize() - 1; i >= c.getFrom() ; --i) {
                        nChildren.remove(i);
                    }
                } else if (c.wasAdded()) {
                    // add them all at once, lets the underlying list do better..

                    List<FieldLabel> toAdd = new ArrayList<>(c.getTo() - c.getFrom());
                    for (int i = c.getFrom(); i < c.getTo(); ++i) {
                        toAdd.add(new FieldLabel(fields.get(i)));
                    }

                    nChildren.addAll(c.getFrom(), toAdd);
                }
            }
        });
    }


    private NumberBinding widthPerBit;




    private void changeInstruction(@Nullable MachineInstruction instruction) {
        fields.unbind();

        if (instruction != null) {
            widthPerBit = Bindings.divide(widthProperty(),
                    Bindings.createDoubleBinding(() -> {
                        double width = 0.0;
                        for (Field f: this.fields) {
                            width += f.getNumBits();
                        }

                        return width;
                    }, this.fields));

            // bind fields first, that way the children get bound properly
            fields.bind(instruction.fieldsProperty(this.fieldType.get()));

//            children.addAll(fields.stream().map(FieldLabel::new).collect(Collectors.toList()));
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
    }
}
