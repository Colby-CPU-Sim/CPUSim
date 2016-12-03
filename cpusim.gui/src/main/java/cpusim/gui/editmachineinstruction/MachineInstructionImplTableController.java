package cpusim.gui.editmachineinstruction;

import cpusim.gui.util.FXMLLoaderFactory;
import cpusim.gui.util.MachineBound;
import cpusim.gui.util.MicroinstructionDragHelper;
import cpusim.model.Machine;
import cpusim.model.MachineInstruction;
import cpusim.model.microinstruction.Comment;
import cpusim.model.microinstruction.Microinstruction;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Logger logger = LoggerFactory.getLogger(MachineInstructionImplTableController.class);
    
    @FXML @SuppressWarnings("unused")
    private TableView<Microinstruction<?>> executeSequenceTable;

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
        executeSequenceTable.setRowFactory(view -> {
            TableRow<Microinstruction<?>> row = new TableRow<>();
            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    final int idx = row.getIndex();
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(row.snapshot(null, null));

                    MicroinstructionDragHelper helper = new MicroinstructionDragHelper(machineProperty());
                    helper.insertIntoDragboard(db, idx);

                    event.consume();
                }
            });

            // FIXME is this necessary?
//            row.setOnDragOver(event -> {
//                Dragboard db = event.getDragboard();
//                if (db != null && db.hasString()) {
//
//                    parseDragboard(db, new MicroinstructionDragHelper.HandleDragBehaviour() {
//                        @Override
//                        public void onDragIndex(final int otherIdx) {
//                            // Check if the current index matches the index in question
//                            if (otherIdx != row.getIndex()) {
//                                event.acceptTransferModes(TransferMode.MOVE);
//                                event.consume();
//                            }
//                        }
//
//                        @Override
//                        public void onDragMicro(final Microinstruction<?> micro) {
//
//                            // need to be smarter here..
//
//                            event.acceptTransferModes(TransferMode.COPY);
//                            event.consume();
//                        }
//                    });
//                }
//            });
            
            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                
                if (db != null && db.hasString()) {
                    final ObservableList<Microinstruction<?>> items = view.getItems();

                    MicroinstructionDragHelper helper = new MicroinstructionDragHelper(machineProperty());
                    helper.parseDragboard(db, new MicroinstructionDragHelper.HandleDragBehaviour() {
                        @Override
                        public void onDragIndex(final int otherIdx) {
                            // Check if the current index matches the index in question
                            if (otherIdx != row.getIndex()) {
                                // Check if the current index matches the index in question
                                Microinstruction<?> instruction = items.get(otherIdx);
    
                                int insertIdx = row.getIndex();
                                if (row.isEmpty()) {
                                    // just insert it at the end then
                                    insertIdx = items.size();
                                }
                                
                                items.remove(otherIdx);
                                items.add((otherIdx < insertIdx ? insertIdx : insertIdx - 1), instruction);
                                view.getSelectionModel().select(insertIdx);
    
                                event.setDropCompleted(true);
                                event.consume();
                            }
                        }
        
                        @Override
                        public void onDragMicro(final Microinstruction<?> micro) {

                            Microinstruction<?> realMicro = micro;
                            if (micro instanceof Comment) {
                                // we want Comment micros used in only one place so create a new one
                                realMicro = new Comment((Comment)micro);
                            }

                            items.add(row.getIndex(), realMicro);
                            view.getSelectionModel().select(row.getIndex());
                            
                            event.setDropCompleted(true);
                            event.consume();
                        }

                        @Override
                        public void onOther() {
                            logger.debug("Received unknown value dropped.");
                        }
                    });
                }
            });
            
            return row;
        });

        // bind the table of execution micros to be bound to the current instructions' sequence
        currentInstruction.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                executeSequenceTable.setItems(newValue.getMicros());
            }
        });

        // If there is no value bound to the currentInstruction, disable the sequence table
        executeSequenceTable.disableProperty().bind(currentInstruction.isNull());
    }
    
}
