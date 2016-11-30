/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cpusim.gui.util;

import com.google.common.base.Strings;
import cpusim.model.Machine;
import cpusim.model.microinstruction.Microinstruction;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * This is a UI element to display a {@link Microinstruction} tree organized by
 * "type".
 *
 * @since 2016-12-01
 */
public class MicroinstructionTreeView extends TitledPane {

    private static final String FXML_FILE = "MicroinstructionTreeView.fxml";

    private final Machine machine;

    @FXML
    private TreeView<Microinstruction> treeView;

    public MicroinstructionTreeView(Machine machine) {
        this.machine = machine;

        FXMLLoader loader = FXMLLoaderFactory.fromRootController(this, FXML_FILE);
        try {
            loader.load();
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    @FXML
    private void initialize() {

        // FIXME Make these names nicer or have a better hierarchy..
        // FIXME an ideal scenario would be to have the Machine accept a visitor that would visit
        // FIXME "Category" -> "sub cat" -> "instruction"



    }

    private static class MicroinstructionTreeItem extends TreeItem<Microinstruction> {

        private static ImageView getCategoryImage() {
            return new ImageView(MicroinstructionTreeItem.class.getResource(
                    "/images/icons/FolderIcon16x16.png").toExternalForm());
        }

        private static ImageView getMicroImage() {
            return new ImageView(MicroinstructionTreeItem.class.getResource(
                    "/images/icons/FileIcon16x16.png").toExternalForm());
        }

        private final Microinstruction instruction;

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
        static MicroinstructionTreeItem forLeaf(final Microinstruction value) {
            return new MicroinstructionTreeItem(value);
        }

        /**
         * Constructs a leaf node, showing an image from {@link #getMicroImage()}.
         * @param value Data to store
         */
        private MicroinstructionTreeItem(Microinstruction value) {
            super(value);

            this.instruction = value;

            setGraphic(getMicroImage());
        }

        /**
         * Constructs an interior node, showing a folder.
         * @param category Name to display
         */
        private MicroinstructionTreeItem(final String category) {
            super(null);

            checkArgument(!Strings.isNullOrEmpty(category));

            this.instruction = null;


            setGraphic(getCategoryImage());
        }

        /**
         * Get the stored {@link Microinstruction}
         * @return Present optional if {@link #isLeaf()} is {@code true}.
         */
        public Optional<Microinstruction> getInstruction() {
            return Optional.ofNullable(instruction);
        }
    }
    
}
