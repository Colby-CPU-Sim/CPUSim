package cpusim.gui.editmachineinstruction;

import cpusim.gui.util.ControlButtonController;
import cpusim.model.MachineInstruction;

/**
 *
 * @since 2016-12-11
 */
public class MachineInstructionButtonController extends ControlButtonController<MachineInstruction> {

    public MachineInstructionButtonController() {
        super(false, true);

        loadFXML();
    }

    @Override
    protected boolean checkDelete(MachineInstruction toDelete) {
        return true;
    }
}
