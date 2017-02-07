package cpusim.gui.editmachineinstruction;

import cpusim.gui.util.DragHelper;
import cpusim.model.Field;
import cpusim.model.Machine;
import cpusim.model.MachineInstruction;
import cpusim.model.util.Colors;
import cpusim.model.util.MachineBound;
import javafx.beans.NamedArg;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fxmisc.easybind.EasyBind;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Lays out colourful fields with a simple label in the center. This control allows for rearranging and is backed by
 * a list of Field, Color pairs.
 *
 * @since 2016-12-06
 */
public class FieldLayoutPane extends HBox implements MachineBound {

    private final Logger logger = LogManager.getLogger(FieldLayoutPane.class);

    private final ListProperty<Field> fields;

    private DoubleProperty fieldBits;
    private DoubleBinding widthPerBit;

    private final ObjectProperty<MachineInstruction.FieldType> fieldType;

    private final ObjectProperty<MachineInstruction> currentInstruction;

    private Map<Field, Color> fieldColorMap;

    private final boolean showWidth;

    private final ObjectProperty<Machine> machine;

    // Drag and Drop fields
    private int dndIndex = -1;

    public FieldLayoutPane(@NamedArg("fieldType") MachineInstruction.FieldType fieldType) {
        this.machine = new SimpleObjectProperty<>(this, "machine", null);

        this.currentInstruction = new SimpleObjectProperty<>(this, "currentInstruction", null);
        this.fields = new SimpleListProperty<>(this, "fields", FXCollections.observableArrayList());
        this.fieldType = new SimpleObjectProperty<>(this, "fieldType", fieldType);
        this.showWidth = fieldType == MachineInstruction.FieldType.Instruction;

        this.fieldColorMap = new HashMap<>();

        // Fields based properties

        this.fields.bind(EasyBind.select(currentInstruction)
                .selectObject(ins -> ins.fieldsProperty(fieldType)));

        this.fieldBits = new SimpleDoubleProperty(1);
        this.widthPerBit = widthProperty().divide(this.fieldBits);
        this.fields.addListener((ListChangeListener<Field>) c -> {
            this.fieldBits.set(c.getList().stream()
                    .mapToInt(Field::getNumBits)
                    .sum());
        });

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
                        nChildren.set(i, new FieldLabel(fields.get(i), showWidth));
                    }
                } else if (c.wasReplaced()) {
                    //update items that were replaced
                    int i = c.getFrom();
                    for ( ; i < c.getTo() && i < nChildren.size(); ++i) {
                        nChildren.set(i, new FieldLabel(fields.get(i), showWidth));
                    }
                    
                    // There's extra items, so fix them
                    for ( ; i < c.getTo(); ++i) {
                        nChildren.add(i, new FieldLabel(fields.get(i), showWidth));
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
                        toAdd.add(new FieldLabel(fields.get(i), showWidth));
                    }

                    nChildren.addAll(c.getFrom(), toAdd);
                }
            }
        });

        // Add support for drag-and-drop
        this.setOnDragEntered(ev -> {
            DragHelper helper = new DragHelper(machineProperty(), ev.getDragboard());
            helper.visit(new DragHelper.HandleDragBehaviour() {
                @Override
                public void onDragField(Field field) {
                    ev.acceptTransferModes(TransferMode.COPY_OR_MOVE);

                    logger.trace("FieldLayoutPane#onDragEntered(): ev.transferMode={}, field={}", ev.getTransferMode(), field);
                }
            });
        });

//        this.setOnDragExited(ev -> {
//            DragHelper helper = new DragHelper(machineProperty(), ev.getDragboard());
//            helper.visit(new DragHelper.HandleDragBehaviour() {
//                @Override
//                public void onDragField(Field field) {
//                    ev.acceptTransferModes(TransferMode.COPY, TransferMode.MOVE);
//
//                    if (dndIndex != -1) {
//                        //fields.remove(dndIndex);
//                        //dndIndex = -1;
//
//
//                        ev.consume();
//                    }
//                }
//            });
//        });

        this.setOnDragDropped(ev -> {
            if (dndIndex == -1) {
                throw new IllegalStateException("Attempted to drop Field, but was not properly initialized");
            }

            DragHelper helper = new DragHelper(machineProperty(), ev.getDragboard());
            helper.visit(new DragHelper.HandleDragBehaviour() {
                @Override
                public void onDragField(Field field) {
                    ev.acceptTransferModes(TransferMode.COPY, TransferMode.MOVE);

                    dndIndex = -1;
                    ev.setDropCompleted(true);
                    ev.consume();
                }
            });
        });
    }


    @Override
    public ObjectProperty<Machine> machineProperty() {
        return machine;
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

    ObjectProperty<MachineInstruction> currentInstructionProperty() {
        return currentInstruction;
    }

    Map<Field, Color> getFieldColorMap() {
        return fieldColorMap;
    }

    void setFieldColorMap(Map<Field, Color> fieldColorMap) {
        this.fieldColorMap = checkNotNull(fieldColorMap);
    }

    /**
     * Used internally for representing a marker in the view
     */
    class FieldLabel extends VBox {

        private static final double DEFAULT_MAX_LABEL_HEIGHT = 50.;

        private final ObjectProperty<Field> field;

        private final Label name;
        private final Label width;

        FieldLabel(Field field, boolean showWidth) {
            this.field = new SimpleObjectProperty<>(this, "field", checkNotNull(field));

            Color color = fieldColorMap.computeIfAbsent(field, ignore -> Colors.generateRandomLightColor());
            Color textColor = color.darker().darker();

            // Create a tooltip with the name and width of a field
            Tooltip tooltip = new Tooltip();
            tooltip.textProperty().bind(Bindings.format("%s\nWidth: %d bits",
                    EasyBind.select(this.field).selectObject(Field::nameProperty),
                    EasyBind.select(this.field).selectObject(Field::numBitsProperty)));

            DoubleBinding halfHeight = this.maxHeightProperty().divide(2);

            name = new Label();
            this.setBackground(new Background(new BackgroundFill(color, null, null)));

            name.setTextFill(textColor);
            name.textProperty().bind(field.nameProperty());
            name.setAlignment(Pos.BOTTOM_CENTER);
            name.prefHeightProperty().bind(halfHeight);
            name.minHeightProperty().bind(halfHeight);

            VBox.setVgrow(name, Priority.ALWAYS);
            name.setTooltip(tooltip);

            getChildren().add(name);

            if (showWidth) {
                width = new Label();
                width.setTextFill(textColor);
                width.textProperty().bind(Bindings.convert(field.numBitsProperty()));
                width.setAlignment(Pos.TOP_CENTER);
                width.prefHeightProperty().bind(halfHeight);
                width.minHeightProperty().bind(halfHeight);

                VBox.setVgrow(width, Priority.ALWAYS);
                width.setTooltip(tooltip);

                getChildren().add(width);
            } else {
                width = null;
                name.prefHeightProperty().bind(this.maxHeightProperty());
                name.minHeightProperty().bind(this.maxHeightProperty());
                name.setAlignment(Pos.CENTER);
            }

            NumberBinding widthBinding = widthPerBit.multiply(field.numBitsProperty());

            Consumer<Region> bindWidth = node -> {
              if (node != null) {
                  node.minWidthProperty().bind(widthBinding);
                  node.prefWidthProperty().bind(widthBinding);
                  node.maxWidthProperty().bind(widthBinding);
              }
            };

            bindWidth.accept(this);
            bindWidth.accept(this.name);
            bindWidth.accept(this.width);

            this.setMinHeight(50);
            this.setMaxHeight(DEFAULT_MAX_LABEL_HEIGHT);

            HBox.setHgrow(this, Priority.ALWAYS);
            VBox.setVgrow(this, Priority.ALWAYS);

            this.setAlignment(Pos.CENTER);

            this.initializeDragAndDrop();
        }

        private void initializeDragAndDrop() {

            this.setOnDragOver(ev -> {
                DragHelper helper = new DragHelper(machineProperty(), ev.getDragboard());

                helper.visit(new DragHelper.HandleDragBehaviour() {
                    @Override
                    public void onDragField(Field field) {
                        ev.acceptTransferModes(TransferMode.COPY_OR_MOVE);

                        ev.consume();
                    }
                });
            });

            this.setOnDragEntered(ev -> {
                DragHelper helper = new DragHelper(machineProperty(), ev.getDragboard());

                helper.visit(new DragHelper.HandleDragBehaviour() {
                    @Override
                    public void onDragField(Field field) {
                        ev.acceptTransferModes(TransferMode.COPY_OR_MOVE);

                        int idx = FieldLayoutPane.this.getChildren().indexOf(FieldLabel.this);

                        if (ev.getTransferMode() == TransferMode.COPY) {
                            // moving from FieldList -> location
                            if (idx != dndIndex) {
                                if (dndIndex == -1) {
                                    fields.add(idx, field);

                                    logger.trace("FieldLabel#onDragEntered() found COPY, thus adding new Field");
                                } else {
                                    // already inserted, so swap them
                                    Field current = fields.get(dndIndex); // index is dragging
                                    fields.set(dndIndex, fields.get(idx));
                                    fields.set(idx, current);

                                    logger.trace("FieldLabel#onDragEntered() found COPY, swapping fields " + idx + " <-> " + dndIndex);
                                }

                                dndIndex = idx;
                            }

                            ev.consume();
                        } else if (ev.getTransferMode() == TransferMode.MOVE) {
                            // rearranging
                        }
                    }
                });
            });

//            this.setOnDragExited(ev -> {
//                Dragboard db = ev.getDragboard();
//                DragHelper helper = new DragHelper(machineProperty(), db);
//
//                if (dndIndex == -1) {
//                    return;
//                }
//
//                helper.visit(new DragHelper.HandleDragBehaviour() {
//                    @Override
//                    public void onDragField(Field field) {
//                        ev.acceptTransferModes(TransferMode.COPY, TransferMode.MOVE);
//
//                        if (ev.getTransferMode() == TransferMode.COPY) {
//                            // moving from FieldList -> location
////                            fields.remove(dndIndex);
////                            dndIndex = -1;
//
//                            logger.trace("FieldLabel#onDragExited() found COPY, thus removed Field");
//
//                            //ev.consume();
//                        } else if (ev.getTransferMode() == TransferMode.MOVE) {
//                            // rearranging
//                        }
//                    }
//                });
//            });
//
//            this.setOnDragDropped(ev -> {
//                Dragboard db = ev.getDragboard();
//                DragHelper helper = new DragHelper(machineProperty(), db);
//
//                helper.visit(new DragHelper.HandleDragBehaviour() {
//                    @Override
//                    public void onDragField(Field field) {
//                        ev.acceptTransferModes(TransferMode.COPY, TransferMode.MOVE);
//
//                        if (ev.getTransferMode() == TransferMode.COPY) {
//                            // moving from FieldList -> location
//                            dndIndex = -1;
//
//                            logger.trace("FieldLabel#onDragDropped() found COPY, dropping Field");
//
//                            ev.consume();
//                        } else if (ev.getTransferMode() == TransferMode.MOVE) {
//                            // rearranging
//                        }
//                    }
//                });
//            });
        }

        Field getField() {
            return field.get();
        }

        ObjectProperty<Field> fieldProperty() {
            return field;
        }

        Label getNameLabel() {
            return name;
        }

        Optional<Label> getWidthLabel() {
            return Optional.ofNullable(width);
        }
    }
}
