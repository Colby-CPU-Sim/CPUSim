/*
 * Ben Borchard
 * 
 * Last Modified 6/4/13s
 */

/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Removed the isAbleToClose method and instead call Validate.containsDecodeMicro
 * to check if there is a decode microinstruction in the fetch sequence before closing
 * 
 * on 11/25
 * 
 * 1.) Added the comment microinstruction to the end of the microInstrTreeView
 * 2.) Added the functionality of the comment micro within the implementation format pane
 * by changing drag drop events and double clicking events on labels within the implementation
 * format pane and the implementation format pane itself
 */
package cpusim.gui.fetchsequence;

import cpusim.Machine;
import cpusim.Mediator;
import cpusim.Microinstruction;
import cpusim.gui.help.HelpController;
import cpusim.gui.util.DragTreeCell;
import cpusim.microinstruction.Comment;
import cpusim.util.CPUSimConstants;
import cpusim.util.Dialogs;
import cpusim.util.Validate;
import cpusim.util.ValidationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author Ben Borchard
 */
public class EditFetchSequenceController implements Initializable {

    @FXML AnchorPane implementationFormatPane;
    
    @FXML TreeView microInstrTreeView;

    Mediator mediator;
    
    TextField commentEditor;
    Microinstruction currentCommentMicro;
    
    ObservableList<Microinstruction> micros;
                
    Microinstruction draggingMicro;
        
    public EditFetchSequenceController(Mediator mediator) {
        this.mediator = mediator;
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        micros = FXCollections.observableArrayList();
        for (Microinstruction micro : mediator.getMachine().getFetchSequence().getMicros()){
            micros.add(micro);
        }
        
        setUpMicroTableView();
        
        updateMicros();
        
        microInstrTreeView.setOnMousePressed(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent t) {
                if (commentEditor != null){
                    commitCommentEdit();
                }
            }
        });
        
        implementationFormatPane.setOnMousePressed(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent t) {
                if (commentEditor != null){
                    commitCommentEdit();
                }
            }
        });

        implementationFormatPane.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                event.acceptTransferModes(TransferMode.COPY);
                double localY = implementationFormatPane.sceneToLocal(event.getSceneX(), event.getSceneY()).getY();
                int index = getMicroinstrIndex(localY);
                insertMicroinstr(index);
            }
        });
        implementationFormatPane.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                /* data dropped */
                /* if there is a string data on dragboard, read it and use it */
                Dragboard db = event.getDragboard();
                String microName = db.getString().split(",")[0];
                String className = db.getString().split(",")[1];
                Microinstruction micro = null;
                
                for (String string : Machine.MICRO_CLASSES){
                    for (Microinstruction instr : mediator.getMachine().getMicros(string)){
                        if (instr.getName().equals(microName) && instr.getMicroClass().equals(className)){
                            micro = instr;
                        }
                    }
                }
                if (className.equals("comment")){
                    micro = new Comment();
                    micro.setName(microName);
                }
                double localY = implementationFormatPane.sceneToLocal(event.getSceneX(), event.getSceneY()).getY();
                int index = getMicroinstrIndex(localY);
                micros.add(index, micro);
             }
        });
        implementationFormatPane.setOnDragExited(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                updateMicros();
             }
        });
        
    }
    
    @FXML
    protected void handleCancel(ActionEvent ae){
        ((Stage)implementationFormatPane.getScene().getWindow()).close();
    }
    
    @FXML
    protected void handleOkay(ActionEvent ae){
        try{
            Validate.containsDecodeMicro(micros);
            if (commentEditor != null){
                commitCommentEdit();
            }
            mediator.getMachine().getFetchSequence().setMicros(micros);
            mediator.setMachineDirty(true);
            ((Stage)implementationFormatPane.getScene().getWindow()).close();
        }
        catch(ValidationException ex){
            Dialogs.createErrorDialog(microInstrTreeView.getScene().getWindow(),
                    "Fetch Sequence Error", ex.getMessage()).showAndWait();
        }
    }

    @FXML
    protected void handleHelp(ActionEvent ae){
    	String startString = "Fetch Sequence Dialog";
    	if (mediator.getDesktopController().getHelpController() == null) {
			HelpController helpController = HelpController.openHelpDialog(
					mediator.getDesktopController(), startString);
			mediator.getDesktopController().setHelpController(helpController);
		}
		else {
			HelpController hc = mediator.getDesktopController().getHelpController();
			hc.getStage().toFront();
			hc.selectTreeItem(startString);
		}
    }
    
    public void updateMicros() {
        implementationFormatPane.getChildren().clear();
        int i = 0;
        for (final Microinstruction micro : micros){
            final Label microLabel = new Label(micro.getName());
            boolean commentLabel = false;
            if (micro instanceof Comment){
                microLabel.setStyle("-fx-text-fill:gray; -fx-font-style:italic;");
                commentLabel = true;
            }
            microLabel.setPrefWidth(implementationFormatPane.getPrefWidth());
            microLabel.setPrefHeight(20);
            microLabel.setLayoutY(i);
            microLabel.setTooltip(new Tooltip(micro.getMicroClass()));

            //makes the labels movable
            microLabel.setOnDragDetected(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    
                    //This is to make sure no error is thrown when the drag is detected
                    //while still editing the text of a comment
                    if(implementationFormatPane.getChildren().contains(microLabel)){
                    
                        /* drag was detected, start a drag-and-drop gesture*/
                        /* allow any transfer mode */
                        Dragboard db = microLabel.startDragAndDrop(TransferMode.ANY);

                        micros.remove(micro);
                        updateMicros();

                        ClipboardContent content = new ClipboardContent();
                        content.putString(microLabel.getText()+","+microLabel.getTooltip().getText());
                        db.setContent(content);

                        event.consume();
                    }
                }
            });

            //determines what happens when the label is doubleClicked
            if (!commentLabel){
                microLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)
                                && mouseEvent.getClickCount() == 2 ){
                            ObservableList<TreeItem> list = microInstrTreeView.getRoot().getChildren();

                            for (TreeItem<String> t : list){
                                if (t.getValue().equals(micro.getMicroClass())){
                                    t.setExpanded(true);
                                    microInstrTreeView.scrollTo(list.indexOf(t));
                                    ObservableList<TreeItem<String>> nodes = t.getChildren();

                                    for (TreeItem<String> tt : nodes){
                                        if (tt.getValue().equals(micro.getName())){
                                            microInstrTreeView.getSelectionModel().select(
                                                    list.indexOf(t)+nodes.indexOf(tt)+2);
                                        }

                                    }
                                }
                                else {
                                    t.setExpanded(false);
                                }
                            }
                        }
                    }
                });
            }
            else{
                microLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)
                                && mouseEvent.getClickCount() == 2 ){
                            commentEditor = new TextField(microLabel.getText());
                            commentEditor.setPrefWidth(implementationFormatPane.getWidth());
                            commentEditor.setOnKeyPressed(new EventHandler<KeyEvent>(){
                                @Override
                                public void handle(KeyEvent t) {
                                    if (t.getCode() == KeyCode.ENTER) {
                                        commitCommentEdit();
                                    } 
                                }
                            });
                            int index = implementationFormatPane.getChildren().indexOf(microLabel);
                            microLabel.setVisible(false);
                            implementationFormatPane.getChildren().add(index, commentEditor);
                            commentEditor.setPrefHeight(20);
                            commentEditor.setLayoutY(index*20);
                            currentCommentMicro = micro;
                        }
                    }
                });
            }


            i += 20;
            implementationFormatPane.getChildren().add(microLabel);
        }
    }

    public void setUpMicroTableView() {

        TreeItem<String> rootNode = new TreeItem<>("MicroInstructions");

        microInstrTreeView.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
            @Override
            public TreeCell<String> call(TreeView<String> param) {
                return new DragTreeCell(mediator,
                        (Stage)implementationFormatPane.getScene().getWindow(),
                        microInstrTreeView, getClasses() );
            }
        });

        rootNode.setExpanded(true);

        for(String microClass : Machine.MICRO_CLASSES){
            TreeItem<String> classNode = new TreeItem<>(microClass);
            for (final Microinstruction micro : mediator.getMachine().getMicros(microClass)){
                final TreeItem<String> microNode = new TreeItem<>(micro.getName());
                classNode.getChildren().add(microNode);
            }
            rootNode.getChildren().add(classNode);
        }

        microInstrTreeView.setRoot(rootNode);
    }
    
    private int getMicroinstrIndex(double localY) {
        List<Double> cutOffLocs = new ArrayList<>();
        cutOffLocs.add(0.0);
        for (Node instr : implementationFormatPane.getChildren()){
            Label label = (Label) instr;
            cutOffLocs.add(label.getLayoutY()+.5*label.getPrefHeight());
        }
        cutOffLocs.add(implementationFormatPane.getPrefHeight());
        int index = 0;
        for (int i=0; i<cutOffLocs.size()-1; i++){
            if (localY >= cutOffLocs.get(i) && localY < cutOffLocs.get(i+1)){
                index = i;
            }
        }
        return index;
    }
    
    private void insertMicroinstr(int index) {
        int i = 0;
//        currentInstr.getMicros().clear();
//        currentInstr.getMicros().add(index, new Branch("",0,new ControlUnit("", mediator.getMachine())));
        updateMicros();
        for(Node instr : implementationFormatPane.getChildren()){
            Label label = (Label)instr;
            if (i >= index){
                label.setPrefHeight(label.getPrefHeight()+20);
            }
            i++;
        }
    }
    
    /**
     * commits the edit made to a comment microinstructions
     */
    public void commitCommentEdit(){
        currentCommentMicro.setName(commentEditor.getText());
        updateMicros();
        commentEditor = null;
    }

    private EditFetchSequenceController getClasses(){
        return this;
    }

}

