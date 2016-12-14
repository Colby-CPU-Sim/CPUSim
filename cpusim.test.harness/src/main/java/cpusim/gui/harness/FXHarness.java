package cpusim.gui.harness;

import cpusim.model.Machine;
import cpusim.model.harness.MachineInjectionRule;
import cpusim.model.util.MachineBound;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.junit.runner.RunWith;
import org.testfx.api.FxRobot;

/**
 * @since 2016-12-12
 */
@RunWith(FXRunner.class)
public abstract class FXHarness extends FxRobot implements MachineBound {
    
    @MachineInjectionRule.BindMachine
    private ObjectProperty<Machine> machineProperty = new SimpleObjectProperty<>(this, "machine", null);

    @Override
    public ObjectProperty<Machine> machineProperty() {
        return machineProperty;
    }
}
