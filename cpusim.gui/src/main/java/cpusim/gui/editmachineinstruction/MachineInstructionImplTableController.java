package cpusim.gui.editmachineinstruction;

import com.google.common.collect.ImmutableMap;
import cpusim.gui.util.DragHelper;
import cpusim.gui.util.FXMLLoaderFactory;
import cpusim.gui.util.table.EditingStrCell;
import cpusim.model.Machine;
import cpusim.model.MachineInstruction;
import cpusim.model.microinstruction.Comment;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.util.MachineBound;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Implements a {@link TableView} that supports drag and drop between it's own elements and a view of
 * {@link Microinstruction} values.
 *
 * @since 2016-12-01
 */
@ParametersAreNonnullByDefault
public class MachineInstructionImplTableController extends TitledPane implements MachineBound {
    
    private static final String FXML_FILE = "MachineInstructionImplTable.fxml";

    private final Logger logger = LogManager.getLogger(MachineInstructionImplTableController.class);
    
    @FXML @SuppressWarnings("unused")
    private TableView<Microinstruction<?>> executeSequenceTable;

    @FXML @SuppressWarnings("unused")
    private TableColumn<Microinstruction<?>, String> typeColumn;

    @FXML @SuppressWarnings("unused")
    private TableColumn<Microinstruction<?>, String> descColumn;

    @FXML @SuppressWarnings("unused")
    private TableColumn<Microinstruction<?>, Integer> cycleColumn;

    private ObjectProperty<MachineInstruction> currentInstruction;
    
    private final ObjectProperty<Machine> machine;

    public MachineInstructionImplTableController() {
        this.currentInstruction = new SimpleObjectProperty<>(this, "currentInstruction", null);
        this.machine = new SimpleObjectProperty<>(this, "machine", null);

        // If the machine changes, make sure we don't have anything showing because it doesn't make sense anymore :)

        try {
            FXMLLoaderFactory.fromRootController(this, FXML_FILE).load();
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    @Override
    public ObjectProperty<Machine> machineProperty() {
        return machine;
    }

    public Optional<MachineInstruction> getCurrentInstruction() {
        return Optional.ofNullable(currentInstruction.getValue());
    }

    public ObjectProperty<MachineInstruction> currentInstructionProperty() {
        return currentInstruction;
    }

    @FXML @SuppressWarnings("unused")
    private void initialize() {

        typeColumn.setCellValueFactory(param -> Bindings.createObjectBinding(() -> {
            Microinstruction<?> ins = param.getValue();
            if (ins != null) {
                return ins.getClass().getSimpleName();
            } else {
                return "";
            }
        }));
        descColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cycleColumn.setCellValueFactory(new PropertyValueFactory<>("cycleCount"));

        typeColumn.setCellFactory((TableColumn<Microinstruction<?>, String> column) -> new MicroStyledCell<>());
        descColumn.setCellFactory((TableColumn<Microinstruction<?>, String> column) -> new MicroDescTableCell());
        cycleColumn.setCellFactory((TableColumn<Microinstruction<?>, Integer> column) -> new MicroStyledCell<>());

        class WrappedInteger {
            int index = -1;
        }
    
        WrappedInteger currentIndex = new WrappedInteger();

        executeSequenceTable.setOnDragEntered(ev -> {
            DragHelper helper = new DragHelper(machineProperty(), ev.getDragboard());
            helper.visit(new DragHelper.HandleDragBehaviour() {
                @Override
                public void onDragMicro(Microinstruction<?> micro) {
                    if (executeSequenceTable.getItems().isEmpty()) {
                        // set the index to zero, this is because it will not get set by the row onDragOver
                        // due to the way that the rows work.
                        currentIndex.index = 0;
                    }
                }
            });

            ev.consume();
        });
    
        final EventHandler<DragEvent> checkDragOver = ev -> {
            // we can just draw it
            DragHelper helper = new DragHelper(machineProperty(), ev.getDragboard());
            helper.visit(new DragHelper.HandleDragBehaviour() {
                @Override
                public void onDragIndex(int value) {
                    ev.acceptTransferModes(TransferMode.MOVE);
                }
            
                @Override
                public void onDragMicro(Microinstruction<?> micro) {
                    ev.acceptTransferModes(TransferMode.COPY);
//                  logger.debug("executeSequenceTable#setOnDragOver(): micro -> {}", micro);
                }
            });
        
            ev.consume();
        };
        executeSequenceTable.setOnDragOver(checkDragOver);

        executeSequenceTable.setOnDragExited(ev -> {
            // we can just draw it
            ObservableList<Microinstruction<?>> items = executeSequenceTable.getItems();
            DragHelper helper = new DragHelper(machineProperty(), ev.getDragboard());
            helper.visit(new DragHelper.HandleDragBehaviour() {
                @Override
                public void onDragMicro(Microinstruction<?> micro) {
                    int index = items.indexOf(micro);
                    if (index != -1) {
                        items.remove(index);

                        logger.trace("Removed marker {}", micro);
                    }
                }
            });

            ev.consume();
        });

        executeSequenceTable.setOnDragDropped(ev -> {
            ObservableList<Microinstruction<?>> items = executeSequenceTable.getItems();
            DragHelper helper = new DragHelper(machineProperty(), ev.getDragboard());
            helper.visit(new DragHelper.HandleDragBehaviour() {
                @Override
                public void onDragIndex(int value) {
                    // Happens when removing an instruction
                    if (currentIndex.index != value) {
                        Microinstruction<?> item = items.remove(value);
                        items.add(Math.min(items.size(), Math.max(0, currentIndex.index - 1)), item);
                        
                        ev.setDropCompleted(true);
                    } else {
                        
                        ev.setDropCompleted(false);
                    }
                }

                @Override
                public void onDragMicro(Microinstruction<?> micro) {
                    logger.trace("Dropped {}", micro);
                    int index = items.indexOf(micro);
                    
                    if (index != -1) {
                        items.remove(index);
                    }
                    
                    items.add(Math.min(currentIndex.index, items.size()), micro.cloneOf());

                    ev.setDropCompleted(true);
                    logger.debug("executeSequenceTable#setOnDragDropped({}): micro -> {}", micro, index);
                }
            });

            currentIndex.index = -1;
            ev.consume();
        });

        executeSequenceTable.setRowFactory(view -> {
            TableRow<Microinstruction<?>> row = new TableRow<Microinstruction<?>>() {
                @Override
                protected void updateItem(Microinstruction<?> item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        if (Comment.class.isAssignableFrom(item.getClass())) {
                            // Make the comments look different
                            this.getChildren().forEach(n -> {

                            });

                        }
                    }
                }
            };
            
            // @Nava2 - After *hours* of work, I figured out that TableRow values do NOT receive the
            // onDragDropped() event. This is silly, but it means that the executeSequenceTable must do this itself.
            // We do this by using the "currentIndex" variable to maintain the current row being dragged across.
            
            // TODO Add some highlighting to show that there is more feedback than just the "hovering row"

            // Starting a drag
            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    final int idx = row.getIndex();
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(row.snapshot(null, null));

                    DragHelper helper = new DragHelper(machineProperty(), db);
                    helper.setIndexContent(idx);

                    event.consume();
                    logger.trace("row.setOnDragDetected() started MOVE, idx = {}", idx);
                }
            });

            // A drag entered the row
            row.setOnDragEntered(ev -> {
                DragHelper helper = new DragHelper(machineProperty(), ev.getDragboard());
                helper.visit(new DragHelper.HandleDragBehaviour() {
                    @Override
                    public void onDragIndex(int value) {
                        logger.trace("Over micro ({}) drag, row.getIndex() = {}, mode = {}",
                                value, row.getIndex(), ev.getTransferMode());
                    }

                    @Override
                    public void onDragMicro(Microinstruction<?> micro) {
                        logger.trace("Over micro ({}) drag, row.getIndex() = {}, mode = {}",
                                micro, row.getIndex(), ev.getTransferMode());
                    }
                });
    
                currentIndex.index = row.getIndex();

                ev.consume();
            });

            row.setOnDragOver(checkDragOver);

            // Drag left the row
//            row.setOnDragExited(ev -> {
//                DragHelper helper = new DragHelper(machineProperty(), ev.getDragboard());
//                helper.visit(new DragHelper.HandleDragBehaviour() {
//                    @Override
//                    public void onDragIndex(int value) {
//                        logger.trace("Exited index ({}) drag, row.getIndex() = {}", value, row.getIndex());
//                    }
//
//                    @Override
//                    public void onDragMicro(Microinstruction<?> micro) {
//                        logger.trace("Exited micro ({}) drag, row.getIndex() = {}", micro, row.getIndex());
//                    }
//                });
//
//                ev.consume();
//            });
            

            // This row started a drag, and is now done
            row.setOnDragDone(ev -> {
                if (ev.getTransferMode() == TransferMode.MOVE) {
                    logger.trace("Row Drag successfully completed: row.getIndex() = {}", row.getIndex());
                } else {
                    int index = row.getIndex();
                    Microinstruction<?> item = executeSequenceTable.getItems().remove(row.getIndex());
                    logger.trace("Removed microinstruction: {}@{}", item, row.getIndex());
                }

                ev.consume();
            });
            
            return row;
        });

        // bind the table of execution micros to be bound to the current instructions' sequence
        currentInstruction.addListener((observable, oldValue, newValue) -> {
            ObservableList<Microinstruction<?>> tableItems = executeSequenceTable.getItems();
            
            if (oldValue != null) {
                ObservableList<Microinstruction<?>> oldItems = oldValue.getMicros();
                oldItems.clear();
                oldItems.addAll(tableItems);
            }
            
            if (newValue != null) {
                tableItems.clear();
                tableItems.addAll(newValue.getMicros());
            }
        });
    }

    private static final Consumer<TableCell<Microinstruction<?>, ?>> DEFAULT_STYLE_HANDLER = cell -> {

        TableRow<?> row = cell.getTableRow();

//        cell.setTextFill(row.getTextFill());
//        Font rowFont = row.getFont();
//        cell.setFont(Font.font(rowFont.getFamily(), rowFont.getSize()));
    };

    // Add any special formatting in here
    private static final ImmutableMap<Class<? extends Microinstruction<?>>, Consumer<? super TableCell<Microinstruction<?>, ?>>> STYLE_HANDLERS =
            ImmutableMap.<Class<? extends Microinstruction<?>>, Consumer<? super TableCell<Microinstruction<?>, ?>>>builder()
            .put(Comment.class, cell -> {
                cell.setTextFill(Color.LIGHTGREEN);
                Font thisFont = cell.getFont();
                cell.setFont(Font.font(thisFont.getFamily(), FontPosture.ITALIC, thisFont.getSize()));
            })
            .build();

    private static void styleCellForMicro(TableCell<Microinstruction<?>, ?> cell) {
        checkNotNull(cell, "cell == null");

        @SuppressWarnings("unchecked") // this is safe, can't have an improper row in the table
        TableRow<Microinstruction<?>> row = (TableRow<Microinstruction<?>>)cell.getTableRow();
        checkState(row != null, "Calling set style before inserted into a row");

        Microinstruction<?> rowValue = row.getItem();

        final Consumer<? super TableCell<Microinstruction<?>, ?>> styler;
        if (rowValue == null) {
            styler = DEFAULT_STYLE_HANDLER;
        } else {
            styler = STYLE_HANDLERS.getOrDefault(rowValue.getClass(), DEFAULT_STYLE_HANDLER);
        }

        styler.accept(cell);
    }

    /**
     * Wrapper class that just updates the style based on the MicroInstruction type
     * @param <T>
     */
    private static class MicroStyledCell<T> extends TableCell<Microinstruction<?>, T> {
        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);

            styleCellForMicro(this);
        }
    }

    /**
     * Simple extension of {@link EditingStrCell} that only allows for editing if the Micro is a
     * {@link Comment}.
     */
    private static class MicroDescTableCell extends EditingStrCell<Microinstruction<?>> {

        @Override
        public void startEdit() {
            @SuppressWarnings("unchecked") // this is safe, can't have an improper row in the table
            TableRow<Microinstruction<?>> row = (TableRow<Microinstruction<?>>)getTableRow();
            if (!row.isEmpty()) {
                Microinstruction<?> value = row.getItem();

                if (Comment.class.isAssignableFrom(value.getClass())) {
                    // we have a comment
                    super.startEdit();
                }
            }
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            styleCellForMicro(this);
        }
    }
    
}
