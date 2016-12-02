package cpusim.gui.editmachineinstruction;

import cpusim.gui.util.FXMLLoaderFactory;
import cpusim.model.Machine;
import cpusim.model.microinstruction.Microinstruction;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.io.IOException;

import static com.google.common.base.Preconditions.*;

/**
 * Implements a {@link TableView} that supports drag and drop between it's own elements and a view of
 * {@link Microinstruction} values.
 *
 * @since 2016-12-01
 */
class MachineInstructionImplTableController extends TitledPane {
    
    private static final String FXML_FILE = "MachineInstructionImplTable.fxml";
    
    @FXML
    private TableView<Microinstruction<?>> instructionTable;
    
    private final Machine machine;
    
    private Dragboard dragboard;
    
    MachineInstructionImplTableController(Machine machine) {
        this.machine = machine;
        
        this.dragboard = null;
        
        try {
            FXMLLoaderFactory.fromRootController(this, FXML_FILE).load();
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }
    
    private interface HandleDragBehaviour {
        
        void onDragInteger(int value);
        
        void onDragClass(Class<?> clazz);
    
        default void onOther() {
            
        }
        
    }
    
    private <T> void parseDragboard(Dragboard db, Class<T> checkClass, HandleDragBehaviour handler) {
        checkNotNull(db);
        checkNotNull(handler);
        
        try {
            // First try to see if it's just reordering
            final int otherIdx = Integer.parseInt(db.getString());
            
            handler.onDragInteger(otherIdx);
        } catch (NumberFormatException nfe) {
            // it wasn't a string.. try to get a class then
            try {
                @SuppressWarnings("unchecked")
                final Class<?> clazz = Class.forName(db.getString());
                
                checkArgument(checkClass.isAssignableFrom(clazz));
                
                handler.onDragClass(clazz);
            } catch (ClassNotFoundException cnfe) {
                // can't handle this..
                // but it wasn't meant for us, so just don't consume it!
            }
        }
    }
    
    @FXML
    private void initialize() {
        
        instructionTable.setRowFactory(view -> {
            TableRow<Microinstruction<?>> row = new TableRow<>();
            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    final int idx = row.getIndex();
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(row.snapshot(null, null));
    
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(Integer.toString(idx));
                    
                    db.setContent(cc);
                    event.consume();
                }
            });
            
            row.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db != null && db.hasString()) {
                    parseDragboard(db, Microinstruction.class, new HandleDragBehaviour() {
                        @Override
                        public void onDragInteger(final int otherIdx) {
                            // Check if the current index matches the index in question
                            if (otherIdx != row.getIndex()) {
                                event.acceptTransferModes(TransferMode.MOVE);
                                event.consume();
                            }
                        }
    
                        @Override
                        public void onDragClass(final Class<?> clazz) {
                            
                            // need to be smarter here..
                            
                            event.acceptTransferModes(TransferMode.COPY);
                            event.consume();
                        }
                    });
                }
            });
            
            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                
                if (db != null && db.hasString()) {
                    final ObservableList<Microinstruction<?>> items = view.getItems();
                    
                    parseDragboard(db, Microinstruction.class, new HandleDragBehaviour() {
                        @Override
                        public void onDragInteger(final int otherIdx) {
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
                        public void onDragClass(final Class<?> clazz) {
                            
                            
                            /* data dropped */
                            /* if there is a string data on dragboard, read it and use it */
//                            if (currentInstr != null) {
//                                Dragboard db = event.getDragboard();
//                                int lastComma = db.getString().lastIndexOf(",");
//                                String microName = db.getString().substring(0, lastComma);
//                                String className = db.getString().substring(lastComma + 1);
//                                Microinstruction<?> micro = null;
//
//                                Outer: for (Class<? extends Microinstruction<?>> mc : Machine.getMicroClasses()) {
//                                    if (mc.getSimpleName().toLowerCase().equals(className)) {
//                                        for (Microinstruction<?> instr : mediator.getMachine().getMicrosUnsafe(mc)) {
//                                            if (instr.getName().equals(microName)) {
//                                                micro = instr;
//                                                break Outer;
//                                            }
//                                        }
//                                    }
//                                }
//
//                                if (className.equals("comment")) {
//                                    // we want Comment micros used in only one place so create a new one
//                                    micro = new Comment();
//                                    micro.setName(microName);
//                                }
//                                double localY = implementationFormatPane.sceneToLocal(event.getSceneX(), event.getSceneY()).getY();
//                                int index = getMicroinstrIndex(localY);
//
//                                currentInstr.getMicros().add(index, micro);
//
//                            }
                            
                            event.setDropCompleted(true);
                            event.consume();
                        }
                    });
                }
            });
            
            return row;
        });
    }
    
}
