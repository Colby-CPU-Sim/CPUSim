package cpusim.gui.util;

import com.google.common.base.Strings;
import cpusim.model.Machine;
import cpusim.model.microinstruction.Microinstruction;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.*;

/**
 * This is a UI element to display a {@link Microinstruction} tree organized by
 * "type".
 *
 * @since 2016-12-01
 */
public final class MicroinstructionTreeView extends TitledPane implements MachineBound {

    private static final String FXML_FILE = "MicroinstructionTreeView.fxml";

    private final ObjectProperty<Machine> machine;

    @FXML @SuppressWarnings("unused")
    private TreeView<Microinstruction<?>> treeView;

    public MicroinstructionTreeView() {
        this.machine = new SimpleObjectProperty<>(this, "machine", null);
        this.machine.addListener((observable, oldValue, newValue) -> reloadFromMachine());

        FXMLLoader loader = FXMLLoaderFactory.fromRootController(this, FXML_FILE);
        try {
            loader.load();
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    @FXML @SuppressWarnings("unused")
    private void initialize() {
        
        // Setup the cell factory to display information properly.
        treeView.setCellFactory(new Callback<TreeView<Microinstruction<?>>, TreeCell<Microinstruction<?>>>() {
            @Override
            public TreeCell<Microinstruction<?>> call(final TreeView<Microinstruction<?>> param) {
                return new TreeCell<Microinstruction<?>>() {
                    @Override
                    protected void updateItem(final Microinstruction<?> value, final boolean empty) {
                        super.updateItem(value, empty); // MUST BE CALLED, see super docs
                        
                        MicroinstructionTreeItem item = (MicroinstructionTreeItem)this.getTreeItem();
                        
                        if (empty || item == null) {
                            // if it's empty, we need to rewrite the cell to be empty
                            // see: http://stackoverflow.com/a/23205728/1748595
                            setText("");
                            setGraphic(null);
                        } else {
                            
                            // We have an item, so add a better graphic and text
                            if (value != null) {
                                setText(value.getName());
                            } else {
                                item.getCategory().ifPresent(this::setText);
                            }

                            setGraphic(item.getGraphic());
                        }
                    }
                };
            }
        });
    }
    
    /**
     * Reloads the content of the {@link TreeView} from the {@link Machine} backing.
     */
    public void reloadFromMachine() {
        checkState(treeView != null, "Must be called AFTER #initialize()");
        checkState(machine.getValue() != null, "No machine value is bound.");
    
        treeView.setRoot(null);
        
        final MicroinstructionTreeItem root = MicroinstructionTreeItem.forCategory("Microinstructions");
        root.setExpanded(true);
        
        machine.getValue().visitMicros(new Machine.MicroinstructionVisitor() {
            private MicroinstructionTreeItem currentCategory;
        
            @Override
            public VisitResult visitCategory(final String category) {
                currentCategory = MicroinstructionTreeItem.forCategory(category);
                root.getChildren().add(currentCategory);
            
                return VisitResult.Okay;
            }
        
            @Override
            public VisitResult visitSubCategory(final String subcategory) {
                return VisitResult.Okay;
            }
        
            @Override
            public VisitResult visitMicro(final Microinstruction<?> micro) {
                checkState(currentCategory != null);
                
                currentCategory.getChildren().add(MicroinstructionTreeItem.forLeaf(micro));
            
                return VisitResult.Okay;
            }
        });
        
        treeView.setRoot(root);
    }

    @Override
    public ObjectProperty<Machine> machineProperty() {
        return this.machine;
    }

    private static class MicroinstructionTreeItem extends TreeItem<Microinstruction<?>> {

        private static ImageView getCategoryImage() {
            return new ImageView(MicroinstructionTreeItem.class.getResource(
                    "/images/icons/FolderIcon16x16.png").toExternalForm());
        }

        private static ImageView getMicroImage() {
            return new ImageView(MicroinstructionTreeItem.class.getResource(
                    "/images/icons/FileIcon16x16.png").toExternalForm());
        }
        
        private final String category;

        /**
         * Creates a {@link TreeItem} showing children of a category
         * @param category Text to display.
         * @return New TreeItem
         */
        static MicroinstructionTreeItem forCategory(final String category) {
            return new MicroinstructionTreeItem(category);
        }

        /**
         * Creates a {@link TreeItem} showing a leaf node that is a value
         * @param value The {@code Microinstruction} stored
         * @return New TreeItem
         */
        static MicroinstructionTreeItem forLeaf(final Microinstruction<?> value) {
            return new MicroinstructionTreeItem(value);
        }

        /**
         * Constructs a leaf node, showing an image from {@link #getMicroImage()}.
         * @param value Data to store
         */
        private MicroinstructionTreeItem(Microinstruction<?> value) {
            super(value);
            
            this.category = null;

            setGraphic(getMicroImage());
        }

        /**
         * Constructs an interior node, showing a folder.
         * @param category Name to display
         */
        private MicroinstructionTreeItem(final String category) {
            super(null);

            checkArgument(!Strings.isNullOrEmpty(category));
            this.category = category;
            
            setGraphic(getCategoryImage());
        }
    
        /**
         * Get the category stored if it is present.
         * @return Optional that is empty or present if it is set.
         */
        public Optional<String> getCategory() {
            return Optional.ofNullable(category);
        }
    }
    
}