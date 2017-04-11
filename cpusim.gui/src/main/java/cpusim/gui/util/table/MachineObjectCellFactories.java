package cpusim.gui.util.table;

import cpusim.model.Machine;
import cpusim.model.module.Module;
import javafx.beans.property.ReadOnlyObjectProperty;
import org.fxmisc.easybind.EasyBind;

/**
 * Created by kevin on 10/04/2017.
 */
public abstract class MachineObjectCellFactories {

    private MachineObjectCellFactories() {

    }

    public static <S, T extends Module<T>>
    NamedObjectCellFactories.ComboBox<S, T> modulesProperty(ReadOnlyObjectProperty<Machine> machineProperty, Class<T> moduleClazz) {
        return new NamedObjectCellFactories.ComboBox<>(EasyBind.select(machineProperty)
                .selectObject(m -> m.getModules(moduleClazz)));
    }
}
