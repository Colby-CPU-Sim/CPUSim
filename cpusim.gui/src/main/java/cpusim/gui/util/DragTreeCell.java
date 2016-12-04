/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard made the following changes on 11/25
 *
 * 1.) Changed the conditional for setting the graphics so that all instances of micros
  * will
 * have the file icon instead of the folder icon regardless of their name
 * 2.) Added the style to the microinstruction comment so that users know that it behaves
 * differently from the other microinstructions
 * 3.) Added a dialog that shows up when the user double clicks the comment
 * microinstruction
 *
 */

package cpusim.gui.util;


import cpusim.model.Machine;
import cpusim.Mediator;
import cpusim.model.Microinstruction;
import cpusim.gui.editmachineinstruction.EditMachineInstructionController;
import cpusim.gui.editmicroinstruction.EditMicroinstructionsController;
import cpusim.gui.fetchsequence.EditFetchSequenceController;
import cpusim.util.Dialogs;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * @author fabriceb
 */
public class DragTreeCell extends TreeCell<String> {

    private boolean isClass;
    private boolean isNothing;
    private Mediator mediator;
    private Stage stage;
    private TreeView treeView;
    private EditFetchSequenceController fetchSequenceController;
    private EditMachineInstructionController machineInstructionController;
    private TreeItem<String> treeItem;

    public DragTreeCell(Mediator mediator, Stage stage, TreeView treeView,
                        EditFetchSequenceController controller) {
        this.mediator = mediator;
        this.stage = stage;
        this.treeView = treeView;
        this.fetchSequenceController = controller;

        setupMouseClickEvent();
    }

    public DragTreeCell(Mediator mediator, Stage stage, TreeView treeView,
                        EditMachineInstructionController controller) {
        this.mediator = mediator;
        this.stage = stage;
        this.treeView = treeView;
        this.machineInstructionController = controller;

        setupMouseClickEvent();
    }

    public void setupMouseClickEvent() {
        this.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent
                        .getClickCount() == 2) {
                    FXMLLoader fxmlLoader = new FXMLLoader(
                            EditMicroinstructionsController.class.getResource("EditMicroinstructions.fxml"));
                    EditMicroinstructionsController controller = new
                            EditMicroinstructionsController(mediator, getClasses());
                    //controller
                    fxmlLoader.setController(controller);
                    //go to the table with this set of micro instruction
                    TreeItem item = (TreeItem) treeView.getSelectionModel()
                            .getSelectedItem();
                    String name = "";
                    if (isClass) {
                        name = item.getValue().toString().substring(0, 1).toUpperCase()
                                + item.getValue().toString().substring(1);
                    }
                    else if (!isNothing) {
                        name = item.getParent().getValue().toString().substring(0, 1)
                                .toUpperCase()
                                + item.getParent().getValue().toString().substring(1);
                    }
                    if (name.equals("Io")) {
                        name = "IO";
                    }
                    if (!name.equals("End") && !(treeItem.getValue().equals("comment") &&
                            treeItem.getParent().getValue().equals("MicroInstructions")
                    )) {
                        final Stage dialogStage = new Stage();
                        Pane dialogRoot = null;
                        try {
                            dialogRoot = fxmlLoader.load();
                        } catch (IOException e) {
                            // should never happen
                            assert false : "Unable to load file: EditMicroinstructions.fxml";
                        }
                        Scene dialogScene = new Scene(dialogRoot);
                        dialogStage.setScene(dialogScene);
                        dialogStage.initOwner(stage);
                        dialogStage.initModality(Modality.WINDOW_MODAL);
                        dialogStage.setTitle("Edit Microinstructions");

                        dialogScene.addEventFilter(
                                KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
                                    @Override
                                    public void handle(KeyEvent event) {
                                        if (event.getCode().equals(KeyCode.ESCAPE)) {
                                            if (dialogStage.isFocused()) {
                                                dialogStage.close();
                                            }
                                        }
                                    }
                                });
                        dialogStage.show();

                        controller.selectSection(name);
                        //if clicking on a microinstruction, highlight it in current
                        // the micro instruction table
                        if (!isClass && !isNothing) {
                            ObservableList l = controller.getActiveTable().getItems();
                            for (int i = 0; i < l.size(); i++) {
                                if (((Microinstruction) l.get(i)).getName().equals(item
                                        .getValue())) {
                                    controller.getActiveTable().getSelectionModel()
                                            .select(i);
                                }
                            }
                        }


                    }
                    else if (name.equals("End")) {
                        Dialogs.createInformationDialog(stage, "Non-Editable " +
                                        "Microinstruction",
                                "The microinstruction \"End\" is a " +
                                        "built-in microinstruction, and is not editable" +
                                        ".").showAndWait();
                    }
                    else {
                        Dialogs.createInformationDialog(stage, "Non-Editable " +
                                        "Microinstruction",
                                "The microinstruction \"comment\" is a " +
                                        "built-in microinstruction, and is not editable" +
                                        ".  It exists so that"
                                        + " the user can make comments within the " +
                                        "implementation"
                                        + " of an instruction").showAndWait();
                    }
                }
            }
        });
    }

    private String item;

    /**
     * updates the String in the table cell
     *
     * @param item  used for the parent method
     * @param empty used for the parent method
     */
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        this.item = item;
        String text = (item == null) ? null : item.toString();
        setText(text);

        isClass = false;
        isNothing = false;

        treeItem = this.getTreeItem();

        if (treeItem != null) {
            for (String microClass : Machine.MICRO_CLASSES) {
                if ((treeItem.getValue().equals(microClass) &&
                        treeItem.getParent().getValue().equals("MicroInstructions"))
                        || (treeItem.getValue().equals("MicroInstructions") &&
                        this.getTreeItem().getParent() == null)) {
                    isClass = true;
                    break;
                }
            }
            if (treeItem.getValue().equals("Comment") && treeItem.getParent()
                    .getValue().equals("comment")) {
                this.setStyle("-fx-text-fill:gray; -fx-font-style:italic;");
            }
        }
        else {
            isNothing = true;
        }

        if (!isNothing) {
            if (!isClass) {
                this.setOnDragDetected(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {

                        Dragboard dragBoard = startDragAndDrop(TransferMode.ANY);

                    /* Put a string on a dragboard */
                        ClipboardContent content = new ClipboardContent();
                        content.putString(treeItem.getValue() + "," + treeItem
                                .getParent().getValue());
                        dragBoard.setContent(content);

                        event.consume();
                    }
                });
            }
        }

        //set icons for the treeCells
        if (isClass) {
            setGraphic(new ImageView(this.getClass().getResource(
                    "/images/icons/FolderIcon16x16.png").toExternalForm()));
        }
        else if (!isNothing) {
            setGraphic(new ImageView(this.getClass().getResource(
                    "/images/icons/FileIcon16x16.png").toExternalForm()));
        }


    }

    public void updateDisplay() {
        if (fetchSequenceController != null) {
            fetchSequenceController.setUpMicroTableView();
            fetchSequenceController.updateMicros();
        }
        else {
            machineInstructionController.setUpMicroTreeView();
            machineInstructionController.updateMicros();
        }
    }

    private DragTreeCell getClasses() {
        return this;
    }

}