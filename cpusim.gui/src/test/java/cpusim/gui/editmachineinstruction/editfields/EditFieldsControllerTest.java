package cpusim.gui.editmachineinstruction.editfields;

import cpusim.gui.harness.FXHarness;
import cpusim.model.Field;
import cpusim.model.Machine;
import cpusim.model.harness.CPUSimRunner;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.Test;

import java.util.UUID;

/**
 *
 * @since 2016-12-12
 */
public class EditFieldsControllerTest extends FXHarness {

    private Field f1;
    private Field f2;

    @CPUSimRunner.MachineSetup
    private EditFieldsController underTest;

    @CPUSimRunner.MachineSetup
    public void setupFields(ObjectProperty<Machine> machineProperty) {
        Machine machine = machineProperty.get();

        f1 = new Field("f1", UUID.randomUUID(), machine, 8,
                Field.Relativity.absolute, null, 0x00,
                Field.SignedType.Unsigned, Field.Type.required);
        f2 = new Field("f2", UUID.randomUUID(), machine, 4,
                Field.Relativity.absolute, null, 0x00,
                Field.SignedType.Signed, Field.Type.required);
    }

    @Override
    public void start(Stage stage) throws Exception {
        underTest = new EditFieldsController();

        Scene dialogScene = new Scene(underTest);
        stage.setScene(dialogScene);
        stage.show();
    }

    @Test
    public void verifyBaseState() {

    }

    @Test
    public void test() {
    }

}