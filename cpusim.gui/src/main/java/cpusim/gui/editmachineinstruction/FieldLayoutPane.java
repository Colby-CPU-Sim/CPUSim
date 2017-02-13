package cpusim.gui.editmachineinstruction;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import cpusim.gui.util.DragHelper;
import cpusim.gui.util.FXMLLoaderFactory;
import cpusim.model.Field;
import cpusim.model.Machine;
import cpusim.model.MachineInstruction;
import cpusim.model.util.Colors;
import cpusim.model.util.MachineBound;
import javafx.beans.NamedArg;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.PropertyBinding;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.*;

/**
 * Lays out colourful fields with a simple label in the center. This control allows for rearranging and is backed by
 * a list of Field, Color pairs.
 *
 * @since 2016-12-06
 */
public class FieldLayoutPane extends VBox implements MachineBound {

    /**
     * Defines how much time between allowing a swap of two fields when
     * dragging and dropping.
     */
    private static final Duration DND_SWAP_REFUSAL_TIME = Duration.ofMillis(400);
    
    
    private static final String FXML_FILE = "FieldsLayoutPane.fxml";

    private final Logger logger = LogManager.getLogger(FieldLayoutPane.class);
    private static final Marker DND_MARKER = MarkerManager.getMarker("DND");
    private static final Marker THIS_DND_MARKER =
            MarkerManager.getMarker("DND_" + FieldLayoutPane.class.getSimpleName()).setParents(DND_MARKER);

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
    private AtomicReference<ImmutableSet<Integer>> dndPrevSwap = new AtomicReference<>();
    private Timer dndTimer = new Timer(this.toString() + "#dndTimer", true);
    
    @FXML
    private HBox fieldsBox;
    
    @FXML
    private Label deleteLabel;

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
        this.widthPerBit = Bindings.createDoubleBinding(() ->
            getWidth() / fieldBits.get(), widthProperty(), fieldBits);
        this.fields.addListener((ListChangeListener<Field>) c -> {
            this.fieldBits.set(c.getList().stream()
                    .mapToInt(Field::getNumBits)
                    .sum());
        });
    
        try {
            FXMLLoaderFactory.fromController(this, FXML_FILE).load();
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }
    
    @FXML
    void initialize() {
        this.fields.addListener((ListChangeListener<Field>) c -> {
        
            // This class change listener was required because in order to map the fields property to
            // the children, I manually had to make sure the indexes lined up. Unfortunately, using
            // EasyBind.map() doesn't work because it counts on the mapping function returning the same value
            // for a map when removing elements. This means some form of uniqueness. Additionally, if caching, it
            // causes the damned thing to add duplicate labels. Thus, I had to manually put in a ListChangeListener,
            // however, this is probably much more efficient anyway.
        
            ObservableList<Node> nChildren = fieldsBox.getChildren();
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
    
        this.setOnDragExited(ev -> {
            DragHelper helper = new DragHelper(machineProperty(), ev.getDragboard());
            helper.visit(new DragHelper.HandleDragBehaviour() {
                @Override
                public void onDragField(Field field) {
                    ev.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                
                    if (dndIndex != -1) {
                        fields.remove(dndIndex);
                        dndIndex = -1;
                    
                        logger.trace("FieldLayoutPane#onDragExited(): ev.transferMode={}, field={}", ev.getTransferMode(), field);
                    
                        ev.consume();
                    }
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
        private static final String STYLE_TEMPLATE = "-fx-border-color: %s;" +
                "-fx-background-color: %s;";

        private final ObjectProperty<Field> field;

        private final Label name;
        private final Label width;
        
        private final Logger logger = LogManager.getLogger(FieldLabel.class);
        private final Marker THIS_DND_MARKER = MarkerManager.getMarker("DND_" + FieldLabel.class.getSimpleName())
                .setParents(FieldLayoutPane.THIS_DND_MARKER);

        FieldLabel(Field field, boolean showWidth) {
            this.field = new SimpleObjectProperty<>(this, "field", checkNotNull(field));

            Color color = fieldColorMap.computeIfAbsent(field, ignore -> Colors.generateRandomLightColor());
            Color borderColor = color.darker();
            Color textColor = borderColor.darker();

            // Create a tooltip with the name and width of a field
            Tooltip tooltip = new Tooltip();
            tooltip.textProperty().bind(Bindings.format("%s\nWidth: %d bits",
                    EasyBind.select(this.field).selectObject(Field::nameProperty),
                    EasyBind.select(this.field).selectObject(Field::numBitsProperty)));

            this.setStyle(String.format(STYLE_TEMPLATE, Colors.toWeb(borderColor), Colors.toWeb(color)));

            DoubleBinding halfHeight = this.maxHeightProperty().divide(2);

            name = new Label();
            //this.setBackground(new Background(new BackgroundFill(color, null, null)));

            name.setTextFill(textColor);
            name.textProperty().bind(field.nameProperty());
            name.setAlignment(Pos.BOTTOM_CENTER);
            name.prefHeightProperty().bind(halfHeight);
            name.minHeightProperty().bind(halfHeight);

            VBox.setVgrow(name, Priority.ALWAYS);
            name.setTooltip(tooltip);

            getChildren().add(name);

            PropertyBinding<Number> numBitsProperty = EasyBind.selectProperty(this.field, Field::numBitsProperty);

            if (showWidth) {
                width = new Label();
                width.setTextFill(textColor);
                width.textProperty().bind(Bindings.convert(numBitsProperty));
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

            NumberBinding widthBinding = Bindings.createDoubleBinding(() ->
                    Math.floor(widthPerBit.get() * numBitsProperty.getValue().doubleValue()),
                    widthPerBit, numBitsProperty);

            Consumer<Region> bindWidth = node -> {
              if (node != null) {
//                  node.minWidthProperty().bind(widthBinding);
                  node.prefWidthProperty().bind(widthBinding);
//                  node.maxWidthProperty().bind(widthBinding);
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

        private int getIndexInParent() {
            return fieldsBox.getChildren().indexOf(this);
        }
        
        private boolean validMoveEventSource(DragEvent ev) {
            checkNotNull(ev);
            checkArgument(ev.getTransferMode() == TransferMode.MOVE,
                    "Tried to validate non-MOVE event, {}", ev.getTransferMode());
            
            Object source = ev.getGestureSource();
            if (source instanceof Node) {
                logger.trace(THIS_DND_MARKER,
                        "validMoveEventSource() - getOwner() = {}, ev.getOwner()={}",
                        this.getParent(), ((Node)source).getParent());
                // should be exact same reference
                return ((Node)source).getParent() == getParent();
            }
            
            return false;
        }

        private void initializeDragAndDrop() {
            
            this.setOnDragDetected(ev -> {
                logger.traceEntry();
                Dragboard db = this.startDragAndDrop(TransferMode.MOVE);
                db.setDragView(this.snapshot(null, null));
    
                DragHelper helper = new DragHelper(machineProperty(), db);
                helper.setFieldContent(this.getField());
                
                dndIndex = getIndexInParent();
    
                ev.setDragDetect(true);
                ev.consume();
                
                logger.trace(THIS_DND_MARKER,"setOnDragDetected() started MOVE, field={}", field);
                
                logger.traceExit();
            });

            this.setOnDragEntered(ev -> {
    
                if (ev.getTransferMode() == TransferMode.MOVE) {
                    if (!validMoveEventSource(ev)) {
                        return;
                    }
                }
                
                DragHelper helper = new DragHelper(machineProperty(), ev.getDragboard());

                helper.visit(new DragHelper.HandleDragBehaviour() {
                    @Override
                    public void onDragField(Field field) {
                        ev.acceptTransferModes(TransferMode.COPY_OR_MOVE);
    
                        logger.trace(THIS_DND_MARKER,"onDragEntered() " +
                                "ENTERED - {}", field);
                        
                        ev.consume();
                    }
                });
            });

            this.setOnDragOver(ev -> {
                logger.traceEntry("onDragOver() - {}", ev);
    
                if (ev.getTransferMode() == TransferMode.MOVE) {
                    if (!validMoveEventSource(ev)) {
                        return;
                    }
                }
                
                final int idx = getIndexInParent();
                
                if (dndIndex != -1 && idx == dndIndex) {
                    ev.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    logger.trace(THIS_DND_MARKER,"onDragOver() ignored due to dndIndex == idx ({})", idx);
                    return;
                }

                DragHelper helper = new DragHelper(machineProperty(), ev.getDragboard());

                helper.visit(new DragHelper.HandleDragBehaviour() {
                    @Override
                    public void onDragField(Field field) {
                        ev.acceptTransferModes(TransferMode.COPY_OR_MOVE);

                        if (ev.getTransferMode() == TransferMode.COPY || ev.getTransferMode() == TransferMode.MOVE) {
                            // moving from FieldList -> location
                            if (idx != dndIndex) {
                                int cidx = idx;

                                List<Node> children = fieldsBox.getChildren();
                                if (cidx == children.size() - 1) {
                                    cidx = children.size();
                                }

                                if (dndIndex == -1) {
                                    fields.add(cidx, field);
                                    dndIndex = cidx;

                                    logger.trace("onDragOver() COPY - added {} {}", cidx, field);
                                } else {
                                    // already inserted, so swap them

                                    final ImmutableSet<Integer> toSwap = ImmutableSortedSet.of(dndIndex, cidx);
                                    if (!toSwap.equals(dndPrevSwap.get())) {
                                        logger.trace("FieldLabel#onDragOver() " +
                                                "COPY - moving field f:{} -> t:{}", dndIndex, cidx);

                                        Field current = fields.get(dndIndex); // index is dragging
                                        fields.remove(dndIndex);
                                        dndIndex = Math.min(cidx, fields.size());
                                        fields.add(dndIndex, current);

                                        // Set the timer to not let a swap happen for awhile
                                        dndPrevSwap.set(toSwap);
                                        dndTimer.schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                dndPrevSwap.lazySet(null);
                                            }
                                        }, DND_SWAP_REFUSAL_TIME.toMillis());
                                    } else {
                                        logger.trace("onDragOver() " +
                                                "COPY - NOT swapping due to previous swap", cidx, dndIndex);
                                    }
                                }

                            }

                            ev.consume();
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
//                            logger.trace("FieldLabel#onDragExited() found COPY, thus removed {}", field);
//
//                            //ev.consume();
//                        } else if (ev.getTransferMode() == TransferMode.MOVE) {
//                            // rearranging
//                        }
//                    }
//                });
//            });

            this.setOnDragDropped(ev -> {
                Dragboard db = ev.getDragboard();
                DragHelper helper = new DragHelper(machineProperty(), db);

                helper.visit(new DragHelper.HandleDragBehaviour() {
                    @Override
                    public void onDragField(Field field) {
                        ev.acceptTransferModes(TransferMode.COPY_OR_MOVE);

                        if (ev.getTransferMode() == TransferMode.COPY
                                || ev.getTransferMode() == TransferMode.MOVE) {
                            // moving from FieldList -> location
                            dndIndex = -1;

                            logger.trace("onDragDropped() COPY - dropped {}", field);

                            ev.consume();
                        }
                    }
                });
            });
            
            this.setOnDragDone(ev -> {
                ev.acceptTransferModes(TransferMode.MOVE);
                
                ev.setDropCompleted(true);
                logger.trace("onDragDone() MOVE completed");
                
                ev.consume();
            });
        }
        
        private FieldLayoutPane getOwner() {
            return FieldLayoutPane.this;
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
