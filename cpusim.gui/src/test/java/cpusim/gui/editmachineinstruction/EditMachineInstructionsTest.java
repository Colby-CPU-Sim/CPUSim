package cpusim.gui.editmachineinstruction;

import cpusim.Mediator;
import cpusim.gui.util.DragHelper;
import cpusim.gui.util.MicroinstructionTreeView;
import cpusim.model.harness.BindMachine;
import cpusim.model.harness.MachineInjectionRule;
import cpusim.model.microinstruction.Comment;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

/**
 * Test the interaction between the {@link MachineInstructionImplTableController} and a
 * {@link MicroinstructionTreeView}
 *
 * @since 2016-12-05
 */
public class EditMachineInstructionsTest extends ApplicationTest {
    
    
    @BindMachine
    private EditMachineInstructionController underTest;
    
    @Rule
    public MachineInjectionRule machineProperty = new MachineInjectionRule(this);
    
    @Override
    public void start(Stage stage) throws Exception {
        StackPane pane = new StackPane();

        HBox layout = new HBox();
        pane.getChildren().add(layout);
        ObservableList<Node> children = layout.getChildren();
        
        underTest = new EditMachineInstructionController(new Mediator(stage));
        
        

        microinstructionTreeView = new MicroinstructionTreeView();
        microinstructionTreeView.setId("instTree");
        children.add(microinstructionTreeView);
        microinstructionTreeView.machineProperty().bind(machineProperty);

        double width = machineInstructionImplTableController.getPrefWidth() + microinstructionTreeView.getPrefWidth();
        double height = Math.max(machineInstructionImplTableController.getPrefHeight(),
                microinstructionTreeView.getPrefHeight());

        Scene scene = new Scene(pane, width, height);
        stage.setScene(scene);
        stage.show();
    }

    @Before
    public void setupMock() {
        final Comment commentMicro = machineProperty.getValue().getMicros(Comment.class).get(0);
    }

    @Test
    public void dragCommentIntoList() throws Exception {

        clickOn("Comment", MouseButton.PRIMARY);
//        clickOn("Comment[2]", MouseButton.PRIMARY);


    }
}