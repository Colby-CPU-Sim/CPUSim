package cpusim.gui.editmachineinstruction;

import cpusim.gui.util.DragHelper;
import cpusim.gui.util.FXMLLoaderFactory;
import cpusim.gui.util.MachineBound;
import cpusim.model.Machine;
import cpusim.model.MachineInstruction;
import cpusim.model.microinstruction.Microinstruction;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Optional;

/**
 * Implements a {@link TableView} that supports drag and drop between it's own elements and a view of
 * {@link Microinstruction} values.
 *
 * @since 2016-12-01
 */
public class MachineInstructionImplTableController extends TitledPane implements MachineBound {
    
    private static final String FXML_FILE = "MachineInstructionImplTable.fxml";

    private final Logger logger = LogManager.getLogger(MachineInstructionImplTableController.class);
    
    @FXML @SuppressWarnings("unused")
    private TableView<Microinstruction<?>> executeSequenceTable;

    @FXML @SuppressWarnings("unused")
    private TableColumn<Microinstruction<?>, String> nameColumn;

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

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cycleColumn.setCellValueFactory(new PropertyValueFactory<>("cycleCount"));
    
        class WrappedInteger {
            int index = -1;
        }
    
        WrappedInteger currentIndex = new WrappedInteger();

        executeSequenceTable.setOnDragEntered(ev -> {
            DragHelper helper = new DragHelper(machineProperty(), ev.getDragboard());
            helper.visit(new DragHelper.HandleDragBehaviour() {
                @Override
                public void onDragIndex(int value) {
                    // do nothing
                }

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
                public void onDragIndex(int value) {
                    
                }

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
    
}
