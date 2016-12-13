package cpusim.gui.harness;

import cpusim.model.Machine;
import cpusim.model.harness.CPUSimRunner;
import cpusim.model.util.MachineBound;
import javafx.beans.property.ObjectProperty;
import org.junit.runner.RunWith;
import org.testfx.api.FxRobot;

/**
 * @since 2016-12-12
 */
@RunWith(CPUSimRunner.class)
public abstract class FXHarness extends FxRobot implements MachineBound {

    private ObjectProperty<Machine> machineProperty;

    @CPUSimRunner.MachineSetup
    public void setupMachineBinding(ObjectProperty<Machine> machineProperty) {
        this.machineProperty = machineProperty;
    }

    @Override
    public ObjectProperty<Machine> machineProperty() {
        return machineProperty;
    }
}
