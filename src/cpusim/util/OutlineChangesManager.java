package cpusim.util;

import cpusim.gui.desktop.DesktopController;
import cpusim.gui.desktop.RamTableController;
import cpusim.gui.desktop.RegisterTableController;
import cpusim.module.RAM;
import cpusim.module.Register;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class to keep track of the outline changes for both register table and ram table.
 * Values are outlined when you are stepping through execution by microinstruction
 * and the value is changed.
 */
public class OutlineChangesManager {
    private Map<RAM,HashMap<Integer,Long>> ramMap;
    private Set<Register> registerSet;
    private DesktopController desktop;
    private BackupManager backups;

    /**
     * Constructs a new OutlineChangesManager for a given environment.
     * @param backups a BackupManager which allows us to access the most
     *                                               recent RAM and Reg changes
     * @param desktop a Desktop object which we use to access the different
     *                                                 windows where we outline
     */
    public OutlineChangesManager(BackupManager backups, DesktopController desktop) {
        this.backups = backups;
        this.desktop = desktop;
        registerSet = new HashSet<>();
        ramMap = new HashMap<>();
    }

    /**
     * Removes all old outlines and then outlines most recent RAM and Reg
     * changes.
     *	If there are no changes, it will just clear all outlining.
     */
    public void updateOutlines()
    {
        ramMap.clear();
        registerSet.clear();
        getRamAndRegChangesFromBackupManager();
        updateRamAndRegOutlines();
    }

    /**
     * Removes all old RAM and Register outlines from all relevant desktop
     * windows.
     */
    public void clearAllOutlines()
    {
        ramMap.clear();
        registerSet.clear();
        this.updateRamAndRegOutlines();
    }

    /**
     * get latest changes from backupmanager and store them in ramMap and
     * registerSet
     */
    private void getRamAndRegChangesFromBackupManager()
    {
        HashMap latestBackupTable = backups.getLatestBackup();

        for (Object module : latestBackupTable.keySet()) {
            if (module instanceof Register) {
                registerSet.add(((Register) module));
            }
            else if (module instanceof RAM) {
                RAM ram = (RAM) module;
                ramMap.put(ram, (HashMap<Integer, Long>)
                        latestBackupTable.get(ram));
            }
        }
    }

    /**
     * finds the relevant desktop windows and tells them to outline RAM and
     * Registers listed in ramMap and registerSet
     */
    private void updateRamAndRegOutlines()
    {
        for (Object c : desktop.getRegisterController()){
            RegisterTableController controller = (RegisterTableController) c;
            controller.outlineRows(registerSet);
        }

        for (Object c : desktop.getRAMController()){
            RamTableController controller = (RamTableController) c;
            RAM ram = controller.getRam();
            HashMap<Integer,Long> tempRam = ramMap.get(ram);

            //now have the window outline the cells
            if (tempRam != null) { //if there are cells to outline
                controller.outlineRows(tempRam.keySet());
            }
            else {  //give it an empty set of rows to outline
                controller.outlineRows(new HashSet<Integer>());
            }
        }
    }
}
