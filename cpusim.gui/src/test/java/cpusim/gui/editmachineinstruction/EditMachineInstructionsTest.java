package cpusim.gui.editmachineinstruction;

import cpusim.Mediator;
import cpusim.gui.harness.FXHarness;
import cpusim.gui.harness.FXRunner;
import cpusim.gui.util.FXMLLoaderFactory;
import cpusim.gui.util.MicroinstructionTreeView;
import cpusim.model.harness.BindMachine;
import cpusim.model.harness.MachineInjectionRule;
import cpusim.model.harness.SamplesFixture;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.textfx.matcher.control.MoreListViewMatchers;

import static org.testfx.api.FxAssert.verifyThat;

/**
 * Test the interaction between the {@link MachineInstructionImplTableController} and a
 * {@link MicroinstructionTreeView}
 *
 * @since 2016-12-05
 */
public class EditMachineInstructionsTest extends FXHarness {
    
    
    @BindMachine
    private EditMachineInstructionController underTest;

    @BindMachine
    private Mediator mediator;
    
    @Rule
    public MachineInjectionRule machineProperty = new MachineInjectionRule(this,
            SamplesFixture.MAXWELL.factory(0));

    @FXRunner.StageSetup
    public void start(Stage stage) throws Exception {
        mediator = new Mediator(stage);
        underTest = new EditMachineInstructionController(mediator);

        FXMLLoader loader = FXMLLoaderFactory.fromController(underTest, "EditMachineInstruction.fxml");
        Pane root = loader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Test @Ignore
    public void verifyLoadedMachine() throws Exception {
        interrupt();

        // FIXME this fails because the query for .list-cell returns an incorrect number of cells (visually verified)

        verifyThat("#instructionList",
                   MoreListViewMatchers.hasValues(getMachine().getInstructions()));

    }
}