/**
 * File: Mediator
 * User: djskrien
 * Date: 5/29/13
 *
 * Created: 6/4/13
 */

/**
 *
 * File: Mediator
 * Author:Scott Franchi, Pratap Luitel, Stephen Webel
 * Date: 11/12/13
 *
 * Method Modified: public void clearAssembleLoadRun()
 */

/**
 * File: Mediator
 * Authors: Joseph Harwood and Jake Epstein
 * Date: 10/14/13
 *
 * Added method clearConsole which does exactly that.
 *
 * Updated method ResetEverything to also clear the console
 * to prevent violation of the principle of least astonishment.
 *
 * Updated method Run to allow for toggling of clearConsoleOnRun
 *
 */

/**
 * File: Mediator
 * Author: Pratap Luitel, Scott Franchi, Stephen Webel
 * Date: 10/27/13
 *
 * Fields added:
 *      private SimpleBooleanProperty machineDirty
 *      private File machineFile
 *      private SimpleStringProperty machineDirtyString
 *      private String currentMachineDirectory
 *
 * Methods added:
 *      public File getMachineFile()
 *      public void setMachineFile(File file)
 *      public boolean isMachineDirty()
 *      public void setMachineDirty(boolean b)
 *      public SimpleStringProperty getMachineDirtyProperty()
 *      public void setCurrentMachineDirectory(String s)
 *      public String getCurrentMachineDirectory()
 *      private void addMachineStateListeners()
 *      public void saveMachine()
 *      public void saveAsMachine()
 *      public void newMachine()
 *      public void openMachine(File fileToOpen)
 *
 * Methods modified:
 *      public boolean Assemble(String programFileName)
 *      public void AssembleLoad(String programFileName)
 *      public void AssembleLoadRun(String programFileName)
 *      public void ClearAssembleLoadRun(String programFileName)
 *      public void Run()
 *      public void Stop()
 *      public void ResetEverything()
 *      private boolean load(boolean clearing)
 *
 * Methods removed:
 *      private boolean assemble()
 *      private void run()
 *      private void stop()
 */

/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard made the following changes on 11/25
 * 
 * 1.) Changed saveAsMachine() method so that the fileChooser dialog has the .cpu file extenstion
 * filter
 * 
 */
package cpusim;

import cpusim.gui.desktop.DesktopController;
import cpusim.mif.MIFScanner;
import cpusim.model.Machine;
import cpusim.model.module.Module;
import cpusim.model.assembler.AssembledInstructionCall;
import cpusim.model.assembler.Assembler;
import cpusim.model.assembler.AssemblyException;
import cpusim.model.module.RAM;
import cpusim.model.module.RAMLocation;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterArray;
import cpusim.model.module.RegisterRAMPair;
import cpusim.model.util.Convert;
import cpusim.model.util.conversion.ConvertLongs;
import cpusim.model.util.units.ArchType;
import cpusim.util.BackupManager;
import cpusim.util.Dialogs;
import cpusim.util.LoadException;
import cpusim.util.MIFReaderException;
import cpusim.util.SourceLine;
import cpusim.xml.MachineReader;
import cpusim.xml.MachineWriter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;

/**
 * This class is the repository of global data, such as the current machine.
 */
public class Mediator {
    private static final String NEWLINE = System.lineSeparator();
    private static final String SPACES = "              ";

    private SimpleObjectProperty<Machine> machine;
    private BackupManager backupManager;
    private Stage stage;
    private Assembler assembler;
    private DesktopController desktopController;
    private SimpleBooleanProperty machineDirty;
    private File machineFile;
    private SimpleStringProperty machineDirtyString;
    private String currentMachineDirectory;

    public Mediator(Stage s) {
        this.stage = s;
        this.backupManager = new BackupManager();
        this.machine = new SimpleObjectProperty<>();
        this.machineDirty = new SimpleBooleanProperty(false);
        this.machineFile = null;
        String d = machineDirty.get() ? "*" : "";
        this.machineDirtyString = new SimpleStringProperty(d);
        machineDirty.addListener((arg0, oldVal, newVal) -> {
            String d1 = newVal ? "*" : "";
            machineDirtyString.set(d1);
        });
        setMachine(new Machine("New"));
    }

    /////////////////// Standard setters and getters ///////////////////


    /**
     * goes through all RAMs, registers, and arrays,
     * and adds the given listener as a
     * PropertyChangeListener to each of them.
     */
    public void addPropertyChangeListenerToAllModules(ChangeListener listener) {
        // FIXME Fix type safe data
        ObservableList<Register> registerList = machine.get().getModules(Register.class);
        for (Register register : registerList) {
            register.valueProperty().removeListener(listener);
            //the preceding statement does nothing if the listener
            //is not currently a listener of the register
            register.valueProperty().addListener(listener);
        }
        ObservableList<RegisterArray> arrays = machine.get().getModules(RegisterArray.class);
        for (RegisterArray array : arrays) {
            for (Register register : array) {
                register.valueProperty().removeListener(listener);
                register.valueProperty().addListener(listener);
            }
        }
        ObservableList<RAM> rams = machine.get().getModules(RAM.class);
        for (RAM ram : rams) {
            ram.dataProperty().removeListener(listener);
            ram.dataProperty().addListener(listener);
        }
    }

    public void setDesktopController(DesktopController d) {
        this.desktopController = d;
    }

    public DesktopController getDesktopController() {
        return this.desktopController;
    }

    public <U extends Module<U>> ObservableList<U> getModule(Class<U> moduleType) {
        final Machine machineObj = machine.get();
        return machineObj.getModules(moduleType);
    }

    /**
     * Sets the assembler's machine field
     * and sets up a new assembler.
     *
     * @param m - the new Machine for this mediator.
     */
    public void setMachine(Machine m) {
        machine.set(m);

        // Bind title to machine name
        if (stage.titleProperty().isBound()) {
            stage.titleProperty().unbind();
        }
        if (desktopController != null) {
            stage.titleProperty().bind(machineDirtyString.
                    concat(machine.get().nameProperty()));
        } else {
            stage.titleProperty().bind(machine.get().nameProperty());
        }

        // In case name ends with .cpu
        String newName = machine.get().getName();
        if (newName.toLowerCase().endsWith(".cpu")) {
            machine.get().nameProperty().set(newName.substring(0, newName.length() - 4));
        }

        machine.get().stateProperty().removeListener(backupManager);
        machine.get().stateProperty().addListener(backupManager);
        addPropertyChangeListenerToAllModules(backupManager);
        this.assembler = new Assembler(machine.get());
    }

    /**
     * get the backup Manager object.
     *
     * @return backupManager object.
     */
    public BackupManager getBackupManager() {
        return backupManager;
    }

    /**
     * gets the Mediator's current Machine object.
     *
     * @return the Mediator's current Machine object.
     */
    public Machine getMachine() {
        return machine.get();
    }

    /**
     * gets the Mediator's current Machine Property.
     *
     * @return the Mediator's current Machine Property.
     */
    public ObjectProperty<Machine> machineProperty() {
        return machine;
    }

    /**
     * gets the Mediator's current assembler object.
     *
     * @return the Mediator's current assembler object.
     */
    public Assembler getAssembler() {
        return assembler;
    }

    public Stage getStage() {
        return stage;
    }

    //--------------------------------------------------------------------------
    //ADDED METHODS

    /**
     * gets the Mediator's current machineFile.
     *
     * @return the Mediator's current machineFile.
     */
    public File getMachineFile() {
        return this.machineFile;
    }

    /**
     * sets the Mediator's machineFile field.
     */
    public void setMachineFile(File file) {
        this.machineFile = file;
    }

    /**
     * gets the Dirtiness of the machine.
     *
     * @return the Mediator's current machineDirty value.
     */
    public boolean isMachineDirty() {
        return this.machineDirty.get();
    }

    /**
     * sets the Mediator's machineDirty field.
     */
    public void setMachineDirty(boolean b) {
        this.machineDirty.set(b);
    }

    /**
     * Gives the simple string property that is "*"
     * if machine is dirty, "" if not.
     *
     * @return the simple string property that is "*"
     * if machine is dirty, "" if not.
     */
    public SimpleStringProperty getMachineDirtyProperty() {
        return this.machineDirtyString;
    }

    /**
     * Set the currentMachineDirectory field to a given string.
     *
     * @param s the string to set the field to.
     */
    public void setCurrentMachineDirectory(String s) {
        currentMachineDirectory = s;
    }

    /**
     * Get the currentMachineDirectory.
     */
    public String getCurrentMachineDirectory() {
        return currentMachineDirectory;
    }

    /**
     * Removes old listeners and adds new ones.
     * Used when opening or creating a new machine from the DesktopController
     */
    private void addMachineStateListeners() {
        machine.get().stateProperty().removeListener(this.desktopController.getHighlightManager());
        machine.get().stateProperty().addListener(this.desktopController.getHighlightManager());
        machine.get().stateProperty().removeListener(this.desktopController.getUpdateDisplayManager());
        machine.get().stateProperty().addListener(this.desktopController.getUpdateDisplayManager());
        machine.get().stateProperty().removeListener(this.desktopController.getConsoleManager());
        machine.get().stateProperty().addListener(this.desktopController.getConsoleManager());
        machine.get().stateProperty().removeListener(this.desktopController.getDebugToolBarController());
        machine.get().stateProperty().addListener(this.desktopController.getDebugToolBarController());
    }

    /**
     * saves the current machine to its file
     * if it has no file it calls the saveAsMachine method
     */
    public void saveMachine() {
        if (machineFile == null) {
            this.saveAsMachine();
            return;
        }

        MachineWriter writer = new MachineWriter();
        try {
            writer.writeMachine(machine.get(), machineFile.getName(),
                    getRegisterRAMPairs(),
                    new PrintWriter(new FileWriter(machineFile), true));
            setMachineDirty(false);
            this.desktopController.updateReopenMachineFiles(machineFile);
        } catch (IOException IOe) {
            Dialogs.createErrorDialog(stage, "Save file error",
                    "Could not save file for unknown reason").showAndWait();
        }
    }

    /**
     * saves the current machine to a user specified file
     */
    public void saveAsMachine() {
        FileChooser fileChooser = new FileChooser();
        this.desktopController.initFileChooser(fileChooser, "Save Machine", false);

        //extension filters removed by Ben Borchard on 2/27/14
        /*fileChooser.getExtensionFilters().add(new ExtensionFilter("Machine File (.cpu)", "*.cpu"));*/

        File fileToSave = fileChooser.showSaveDialog(stage);
        if (fileToSave == null) {
            return;
        }
        
        /*extension filters removed by Ben Borchard on 2/27/14
        if (fileToSave.getAbsolutePath().lastIndexOf(".cpu") != 
                fileToSave.getAbsolutePath().length() - 4) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".cpu");
        }*/


        MachineWriter writer = new MachineWriter();
        try {
            writer.writeMachine(machine.get(), fileToSave.getName(),
                    getRegisterRAMPairs(),
                    new PrintWriter(new FileWriter(fileToSave), true));
            setCurrentMachineDirectory(fileToSave.getParent());
            setMachineFile(fileToSave);
            setMachineDirty(false);
            this.desktopController.updateReopenMachineFiles(fileToSave);

            // to get name without .cpu
            String newName = fileToSave.getName();
            if (newName.toLowerCase().endsWith(".cpu")) {
                machine.get().setName(newName.substring(0, newName.length() - 4));
            } else {
                machine.get().setName(newName);
            }
        } catch (IOException e) {
            Dialogs.createErrorDialog(stage, "Save file error",
                    "Could not save file for unknown reason").showAndWait();
        }


    }

    public void newMachine() {
        Machine machine = new Machine("New");
        setMachineFile(null);
        setMachine(machine);
        ObservableList<RegisterRAMPair> newPairs = FXCollections.observableArrayList();
        this.desktopController.getHighlightManager().setRegisterRAMPairs(newPairs);
        setMachineDirty(true);

        addMachineStateListeners();

        this.desktopController.clearTables();
        this.desktopController.setUpTables();
    }

    /**
     * Attempts to load a machine from the given file.
     * If the file is null or not properly formatted, an error message
     * appears and a new empty machine is loaded instead.
     *
     * @param fileToOpen the File containing the machine to load.
     */
    public void openMachine(File fileToOpen) {
        String errorMessage;
        MachineReader reader = new MachineReader();
        Machine machine;

        try {
            reader.parseDataFromFile(fileToOpen);
        } catch (Exception ex) {
            ex.printStackTrace();
            errorMessage = ex.getMessage();
            if (errorMessage == null) {
                errorMessage = "The error type is unknown.";
            }
            String messagePrefix = "Error when reading the machine file \"" +
                    fileToOpen.getName() + "\"";
            if (ex instanceof SAXParseException) {
                messagePrefix += " at line " +
                        ((SAXParseException) ex).getLineNumber();
            }
            errorMessage = messagePrefix + "." + NEWLINE + errorMessage;
            Dialogs.createErrorDialog(stage, "Error reading machine file",
                    errorMessage).showAndWait();

            // remove the file from the Reopen machine... menu item
            //if (this.desktopController.getReopenMachineFiles().contains(
            //       fileToOpen.getAbsolutePath())) {
            //    this.desktopController.getReopenMachineFiles().remove(
            //        fileToOpen.getAbsolutePath());
            //}
            //this.desktopController.updateReopenMachineMenu();

            return;
        }

        this.desktopController.updateReopenMachineFiles(fileToOpen);
        setCurrentMachineDirectory(fileToOpen.getParent());
        setMachineFile(fileToOpen);
        setMachineDirty(false);

        machine = reader.getMachine().orElseThrow(() -> new IllegalStateException("Could not load Machine from reader."));
        setMachine(machine);
        this.desktopController.getHighlightManager().setRegisterRAMPairs(
                FXCollections.observableList(reader.getRegisterRAMPairs()));

        addMachineStateListeners();

        this.desktopController.clearTables();
        this.desktopController.setUpTables();
        this.desktopController.refreshTopTabPane();
    }

    //--------------------------------------------------------------------------


    /////////////////// Execute Menu ///////////////////

    //--------------------------------------------------------------------------

    /**
     * Assembles the current program. Called
     * from the Execute menu.
     *
     * @param programFileName the name of the current file.
     */
    //CHANGE: returns boolean value
    public boolean Assemble(String programFileName) {
        try {
            assembler.assemble(programFileName,
                    (machine.get()).getStartingAddressForLoading());
            return true;
        } catch (AssemblyException ae) {
            //TODO:  Use the catch block from Desktop.assembleCurrentProgram
            //       in version 3.8.3.  In the meanwhile, just display
            //      the error message from the Exception.
            desktopController.highlightToken(ae.token);
            Dialogs.createErrorDialog(stage, "Assembly error",
                    ae.getMessage() + System.lineSeparator() + "Error is at line " + (ae.token.lineNumber + 1) +
                            " and column " + ae.token.columnNumber + ", in file " +
                            ae.token.filename).showAndWait();
            return false;
        }
    }

    /**
     * Assembles and Loads the current program. Called
     * from the Execute menu.
     *
     * @param programFileName the name of the current file.
     */
    public void AssembleLoad(String programFileName) {
        // Try assembling
        //CHANGE: Assemble called rather than assemble
        boolean success = Assemble(programFileName);
        if (!success) {
            return;
        }

        // Try Loading
        load(false);
    }

    /**
     * Assembles, Loads, and Runs the current program.
     * Called from the Execute menu.
     *
     * @param programFileName the name of the current file.
     */
    public void AssembleLoadRun(String programFileName) {
        // Try assembling
        //CHANGE: Assemble called rather than assemble
        boolean success = Assemble(programFileName);
        if (!success) {
            return;
        }

        // Try Loading
        success = load(false);
        if (!success) {
            return;
        }

        // Try running
        //CHANGE: Run called rather than run
        Run();
    }

    /**
     * Clears, Assembles, Loads, and Runs the current program.
     * Called from the Execute menu.
     *
     * @param programFileName the name of the current file.
     */
    public void ClearAssembleLoadRun(String programFileName) {

        // Try assembling
        boolean success = Assemble(programFileName);
        if (!success) {
            return;
        }

        // clear the registers and arrays and RAMs
        // note:  Run() resets the control unit and io channels
        clearRAMs();
        clearRegisters();
        clearRegisterArrays();

        // Try loading
        success = load(true);
        if (!success) {
            return;
        }

        // Try running
        Run();
    }

    /**
     * Runs the current program.
     * Called from the Execute menu.
     */
    //CHANGE: run() code replaced body
    public void Run() {
        // Try running
        machine.get().getControlUnit().reset();

        if (desktopController.getOtherSettings().clearConsoleOnRun) {
            machine.get().resetAllChannels();
        } else {
            machine.get().resetAllChannelsButConsole();
        }

        machine.get().execute(Machine.RunModes.RUN);
    }

    /**
     * Stops the currently running program.
     * Called from the Execute menu.
     */
    public void Stop() {
        machine.get().setRunMode(Machine.RunModes.ABORT);
    }

    /**
     * Clears all registers and Arrays and Rams and Console.
     * Called from the Execute menu.
     */
    public void ResetEverything() {
        clearRegisters();
        clearRegisterArrays();
        clearRAMs();
        ClearConsole();
    }

    /**
     * Clears the console.
     * Called from the Execute menu.
     */
    public void ClearConsole() {
        machine.get().resetAllChannels();
    }

    /////////////////////// Private helper Methods ///////////////////////

    /**
     * To avoid repetitive coding, the clear method
     * is implemented here. It clears the Back-Ups
     * in the Debug menu, clears the registers in the
     * machine, and updates the displays to show that
     * everything has been cleared.
     */
    public void clearRegisters() {
        machine.get().clearAllRegisters();
    }

    public void clearRegisterArrays() {
        machine.get().clearAllRegisterArrays();
    }

    public void clearRAMs() {
        machine.get().clearAllRAMs();
    }

    /**
     * To avoid repetitive coding, the load method
     * is implemented here. It loads the binary decoded
     * instructions into RAM.
     *
     * @param clearing if the clearing all Registers and RAMs should be done
     * @return boolean true if everything went smoothly,
     * false if there was an error
     */
    private boolean load(boolean clearing) {

        if (!machine.get().getCodeStore().isPresent()) {
            Dialogs.createErrorDialog(stage, "Error finding RAM",
                    "The machine has no RAM's and so there is " +
                    "nowhere to load the assembled code.").showAndWait();
            return false;
        }
        List<AssembledInstructionCall> instrs =
                assembler.getAssembledInstructions();

        try {
            if (clearing) {
                clearRegisters();
                clearRegisterArrays();
                clearRAMs();
            }
            RAM codeStore = machine.get().getCodeStore().get();
            
            codeStore.loadAssembledInstructions(instrs, machine.get().getStartingAddressForLoading());

            // remove the old breakpoints and add the new ones
            codeStore.clearAllBreakpoints();
            setBreakPointsInRam();
            return true;
        } catch (LoadException ex) {
            Dialogs.createErrorDialog(stage, "RAM Load Error",
                    ex.getMessage()).showAndWait();
            return false;
        }
    }

    //--------------------------------------------------------------------------

    /////////////////////// Other Getters, Setters and Parsers ///////////////////////

    /**
     * takes the ram information and puts it into mif format
     *
     * @param ram The RAM whose data is to be converted to MIF
     * @return the textual representation of the ram according to mif formatting
     */
    public String ramToMIF(RAM ram) {
        String ramInMIF = "";

        ramInMIF += "DEPTH = " + ram.getLength() + ";" + NEWLINE;
        ramInMIF += "WIDTH = " + ram.getCellSize() + ";" + NEWLINE;
        ramInMIF += "ADDRESS_RADIX = HEX;" + NEWLINE;
        ramInMIF += "DATA_RADIX = BIN;" + NEWLINE;
        ramInMIF += "CONTENT" + NEWLINE;
        ramInMIF += "BEGIN" + NEWLINE + NEWLINE;
        int i = 0;
        int dataChunkBeginAddress = -1;
        for (RAMLocation ramLoc : ram.data()) {
            if (i != ram.data().size() - 1) {
                if (ramLoc.getValue() == ram.data().get(i + 1).getValue()) {
                    if (dataChunkBeginAddress == -1) {
                        dataChunkBeginAddress = i;
                    }
                    i++;
                    continue;
                } else {
                    if (dataChunkBeginAddress != -1) {
                        ramInMIF += "[" + ConvertLongs.toHexString(dataChunkBeginAddress, ram.getCellSize()) + ".." +
                                ConvertLongs.toHexString(i, ram.getNumAddrBits()) + "]:  " +
                                ConvertLongs.to2sComplementString(ramLoc.getValue(), ram.getCellSize())
                                + ";" + SPACES + "-- " + ramLoc.getComment() + NEWLINE;
                        dataChunkBeginAddress = -1;
                    } else {
                        ramInMIF += ConvertLongs.fromLongToHexadecimalString(i, ram.getNumAddrBits())
                                + "        :  " + ConvertLongs.fromLongToTwosComplementString(ramLoc.getValue(),
                                ram.getCellSize()) + ";" + SPACES + "-- " + ramLoc.getComment() + NEWLINE;
                    }
                }
            } else {
                if (dataChunkBeginAddress != -1) {
                    ramInMIF += "[" + ConvertLongs.fromLongToHexadecimalString(
                            dataChunkBeginAddress, ram.getCellSize()) + ".." +
                            ConvertLongs.fromLongToHexadecimalString(i, ram.getNumAddrBits()) + "]:  " +
                            ConvertLongs.fromLongToTwosComplementString(ramLoc.getValue(),
                                    ram.getCellSize()) + ";" + SPACES + "-- " + ramLoc.getComment() + NEWLINE;
                    dataChunkBeginAddress = -1;
                } else {
                    ramInMIF += ConvertLongs.fromLongToHexadecimalString(i, ram.getNumAddrBits())
                            + "        :  " + ConvertLongs.fromLongToTwosComplementString(ramLoc.getValue(),
                            ram.getCellSize()) + ";" + SPACES + "-- " + ramLoc.getComment() + NEWLINE;
                }
            }
            i++;
        }

        ramInMIF += "END;" + NEWLINE;

        return ramInMIF;
    }

    public String ramToIntelHex(RAM ram) {
        final StringBuilder ramInIntelHex = new StringBuilder();

        int bytesNeeded = (ram.getCellSize() + 7) / 8;


        for (RAMLocation ramLoc : ram.data()) {
            ramInIntelHex.append(":");
            ramInIntelHex.append(ConvertLongs.fromLongToHexadecimalString(bytesNeeded, 8));
            ramInIntelHex.append(ConvertLongs.fromLongToHexadecimalString(ramLoc.getAddress(), 16));
            ramInIntelHex.append("00");
            ramInIntelHex.append(ConvertLongs.fromLongToHexadecimalString(ramLoc.getValue(), bytesNeeded * 8));
            ramInIntelHex.append(getCheckSumString(bytesNeeded, (int) ramLoc.getAddress(), ramLoc.getValue()));
            ramInIntelHex.append(NEWLINE);
        }
    
        ramInIntelHex.append(":00000001FF");

        return ramInIntelHex.toString();
    }

    /**
     * parses text from an MIF file, putting information in the proper places
     *
     * @param fileText text from the MIF file to parse
     * @param ram      the RAM to load the info from the file into
     * @param pathName the pathname of the file being parsed
     */
    public void parseMIFFile(String fileText, RAM ram, String pathName) {
        String[] lines = fileText.split("/\r\n|\n|\r/");

        MIFScanner scanner = new MIFScanner(lines);

        ObservableList<RAMLocation> ramToBe = FXCollections.observableArrayList();

        String addressRadix = "";
        String dataRadix = "";
        int depth = -1;
        int width = -1;
        int startAddr;
        int endAddr;
        int dataBeginIndex;
        ArrayList<Long> data = new ArrayList<>();
        String comment;

        //search for the preliminary information
        while (true) {
            ArrayList<String> tokens = scanner.getNextTokens(false);
            if (tokens == null) {
                throw (new MIFReaderException("There is no content indicator"));
            }


            if (tokens.size() == 4) {
                if (!tokens.get(1).equals("=")) {
                    throw (new MIFReaderException("Equals sign missing or in incorrect "
                            + "position on line " + scanner.getLineNumber()));
                }
                if (!tokens.get(3).equals(";")) {
                    throw (new MIFReaderException("Semicolon missing or in incorrect "
                            + "position on line " + scanner.getLineNumber()));
                }
                if (tokens.get(0).toLowerCase().equals("depth")) {
                    try {
                        depth = Integer.parseInt(tokens.get(2));
                    } catch (NumberFormatException nfe) {
                        throw (new MIFReaderException("The value for depth assigned on line "
                                + scanner.getLineNumber() + " should be a valid integer"));
                    }
                }
                if (tokens.get(0).toLowerCase().equals("width")) {
                    try {
                        width = Integer.parseInt(tokens.get(2));
                    } catch (NumberFormatException nfe) {
                        throw (new MIFReaderException("The value for width assigned on line "
                                + scanner.getLineNumber() + " should be a valid integer"));
                    }
                }
                if (tokens.get(0).toLowerCase().equals("address_radix")) {
                    addressRadix = tokens.get(2).toLowerCase();
                    if (addressRadix.equals("hexadecimal")) {
                        addressRadix = "hex";
                    }
                    if (addressRadix.equals("decimal")) {
                        addressRadix = "dec";
                    }
                    if (addressRadix.equals("binary")) {
                        addressRadix = "bin";
                    }
                    if (!addressRadix.equals("hex") && !addressRadix.equals("dec") &&
                            !addressRadix.equals("bin")) {
                        throw (new MIFReaderException("unknown radix '" + addressRadix +
                                "' found on line " + scanner.getLineNumber()));
                    }
                }
                if (tokens.get(0).toLowerCase().equals("data_radix")) {
                    dataRadix = tokens.get(2).toLowerCase();
                    if (dataRadix.equals("hexadecimal")) {
                        dataRadix = "hex";
                    }
                    if (dataRadix.equals("decimal")) {
                        dataRadix = "dec";
                    }
                    if (dataRadix.equals("binary")) {
                        dataRadix = "bin";
                    }
                    if (!dataRadix.equals("hex") && !dataRadix.equals("dec") &&
                            !dataRadix.equals("bin")) {
                        throw (new MIFReaderException("unknown radix " + dataRadix +
                                " found on line " + scanner.getLineNumber()));
                    }
                }

            } else if (tokens.size() == 1) {
                if (tokens.get(0).toLowerCase().equals("content")) {
                    break;
                } else {
                    throw (new MIFReaderException("Unknown indicator " + tokens.get(0) + " on"
                            + "line " + scanner.getLineNumber()));
                }
            } else if (tokens.size() != 0) {
                throw (new MIFReaderException("Parse error on line " + scanner.getLineNumber()));
            }

        }

        if (depth > ram.getLength()) {
            Dialogs.createWarningDialog(stage, "Warning",
                    "There is more data being loaded into the ram"
                            + "than the ram has room for.  This means that not all of the information"
                            + "in the file will be loaded into ram").showAndWait();
        }

        if (width != ram.getCellSize()) {
            throw (new MIFReaderException("The specified width does not match the "
                    + "cell size of the ram and therefor the data from the file"
                    + " cannot be loaded"));
        }

        //look for the begin indicator
        while (true) {
            ArrayList<String> tokens = scanner.getNextTokens(false);
            if (tokens == null) {
                throw (new MIFReaderException("There is no begin indicator"));
            }

            if (tokens.size() == 1 && tokens.get(0).toLowerCase().equals("begin")) {
                break;
            }
        }

        //parse the data
        while (true) {
            endAddr = -1;
            data.clear();
            comment = "";
            ArrayList<String> tokens = scanner.getNextTokens(true);
            if (tokens == null) {
                throw (new MIFReaderException("There is no end indicator"));
            }

            if (tokens.size() == 0) {
                continue;
            }

            if (tokens.size() < 4) {
                if (tokens.size() == 2) {
                    if (tokens.get(0).toLowerCase().equals("end") && tokens.get(1).equals(";")) {
                        break;
                    } else {
                        throw (new MIFReaderException("Unknown indicator " + tokens.get(0) + " on"
                                + "line " + scanner.getLineNumber()));
                    }
                }
                throw (new MIFReaderException("Parse error on line " + scanner.getLineNumber()));
            }
            if (tokens.get(1).equals(":")) {
                try {
                    if (addressRadix.equals("hex")) {
                        startAddr = Integer.parseInt(tokens.get(0), 16);
                    } else if (addressRadix.equals("dec")) {
                        startAddr = Integer.parseInt(tokens.get(0));
                    } else {
                        startAddr = Integer.parseInt(tokens.get(0), 2);
                    }
                } catch (NumberFormatException e) {
                    throw (new MIFReaderException("Invalid address value" + tokens.get(0) +
                            "on line " + scanner.getLineNumber()));
                }
                dataBeginIndex = 2;
            } else if (tokens.get(0).equals("[")) {
                if (!tokens.get(2).equals("..")) {
                    throw (new MIFReaderException("Parse error on line " + scanner.getLineNumber()));
                }
                if (!tokens.get(4).equals("]")) {
                    throw (new MIFReaderException("Closed bracket is missing or misplaced "
                            + "on line " + scanner.getLineNumber()));
                }
                if (!tokens.get(5).equals(":")) {
                    throw (new MIFReaderException("Colon is missing or misplaced "
                            + "on line " + scanner.getLineNumber()));
                }
                try {
                    if (addressRadix.equals("hex")) {
                        startAddr = Integer.parseInt(tokens.get(1), 16);
                    } else if (addressRadix.equals("dec")) {
                        startAddr = Integer.parseInt(tokens.get(1));
                    } else {
                        startAddr = Integer.parseInt(tokens.get(1), 2);
                    }
                } catch (NumberFormatException e) {
                    throw (new MIFReaderException("Invalid address value" + tokens.get(1) +
                            "on line " + scanner.getLineNumber()));
                }
                try {
                    if (addressRadix.equals("hex")) {
                        endAddr = Integer.parseInt(tokens.get(3), 16);
                    } else if (addressRadix.equals("dec")) {
                        endAddr = Integer.parseInt(tokens.get(3));
                    } else {
                        endAddr = Integer.parseInt(tokens.get(3), 2);
                    }
                } catch (NumberFormatException e) {
                    throw (new MIFReaderException("Invalid address value" + tokens.get(3) +
                            "on line " + scanner.getLineNumber()));
                }
                dataBeginIndex = 6;
            } else {
                throw (new MIFReaderException("Parse error on line " + scanner.getLineNumber()));
            }
            while (true) {
                if (tokens.get(dataBeginIndex).equals(";")) {
                    if (dataBeginIndex != tokens.size() - 1) {
                        comment = tokens.get(dataBeginIndex + 1);
                    }
                    break;
                }
                try {
                    if (dataRadix.equals("hex")) {
                        data.add(Long.parseLong(tokens.get(dataBeginIndex), 16));
                    } else if (addressRadix.equals("dec")) {
                        data.add(Long.parseLong(tokens.get(dataBeginIndex)));
                    } else {
                        data.add(Long.parseLong(tokens.get(dataBeginIndex), 2));
                    }
                } catch (NumberFormatException e) {
                    throw (new MIFReaderException("Invalid data value" + tokens.get(dataBeginIndex) +
                            "on line " + scanner.getLineNumber()));
                }
                if (dataBeginIndex == tokens.size() - 1) {
                    break;
                }
                dataBeginIndex++;
            }

            if (ram.data().size() == ramToBe.size()) {
                break;
            }


            int k = 0;
            if (endAddr == -1) {
                for (long value : data) {
                    if (ram.data().size() == ramToBe.size()) {
                        break;
                    }
                    ramToBe.add(new RAMLocation(startAddr + k, value, ram, false, comment,
                            new SourceLine(scanner.getLineNumber(), pathName)));
                    k++;
                }
            } else {
                while (startAddr + k != endAddr + 1) {
                    if (ram.data().size() == ramToBe.size()) {
                        break;
                    }
                    ramToBe.add(new RAMLocation(startAddr + k, data.get(k % data.size()), ram,
                            false, comment, new SourceLine(scanner.getLineNumber(), pathName)));
                    k++;
                }
            }
        }

        int i = 0;
        for (RAMLocation ramLoc : ram.data()) {
            ramLoc.setValue(ramToBe.get(i).getValue());
            ramLoc.setComment(ramToBe.get(i).getComment());
            ramLoc.setSourceLine(ramToBe.get(i).getSourceLine());
            i++;
        }
    }

    public void setRegisterRamPairs(ObservableList<RegisterRAMPair> registerRAMPairs) {
        desktopController.getHighlightManager().setRegisterRAMPairs(registerRAMPairs);
    }

    public ObservableList<RegisterRAMPair> getRegisterRAMPairs() {
        return desktopController.getHighlightManager().getRegisterRAMPairs();
    }

    private String getCheckSumString(int byteCount, int address, long value) {
        int sum = byteCount + address % 256 + address / 256;
        byte[] bytes = BigInteger.valueOf(value & ArchType.Byte.getMask(byteCount)).toByteArray();
        for (byte b : bytes)
            sum += b;
        sum &= 255;  // to get just the lowest 8 bits
        int checkSum = (256 - sum) % 256; // only want the lowest 8 bits
        return ConvertLongs.fromLongToHexadecimalString(checkSum, 8);
    }

    public void parseIntelHexFile(String fileText, RAM ram, String pathName) {
        String[] lines = fileText.split("/\r\n|\n|\r/");

        ObservableList<RAMLocation> ramToBe = FXCollections.observableArrayList();

        int lineNumber = 1;
        for (String line : lines) {
            if (line.length() % 2 != 1) {
                Dialogs.createErrorDialog(stage, "Intel Hex Parse Error",
                        "There is an even number of characters on"
                                + "line " + lineNumber + ", which is not allowed in"
                                + " intel hex format").showAndWait();
            }
            if (line.charAt(0) != ':') {
                Dialogs.createErrorDialog(stage, "Intel Hex Parse Error",
                        "line " + lineNumber + " does not start with"
                                + "a colon as it should").showAndWait();
            }
            int bytesOfData = (int) Convert.fromHexadecimalStringToLong(line.substring(1, 3), 8);
            if (line.length() != 11 + 2 * bytesOfData) {
                Dialogs.createErrorDialog(stage, "Intel Hex Parse Error",
                        "Line " + lineNumber +
                                " specifies " + bytesOfData + " bytes of data but contains " +
                                (line.length() - 11) + " bytes of data.").showAndWait();
            }
            if (line.substring(7, 9).equals("01")) {
                int i = 0;
                for (RAMLocation ramLoc : ram.data()) {
                    ramLoc.setValue(ramToBe.get(i).getValue());
                    ramLoc.setComment(ramToBe.get(i).getComment());
                    ramLoc.setSourceLine(ramToBe.get(i).getSourceLine());
                    i++;
                }
                return;
            }

            int address = Integer.parseInt(line.substring(3, 7), 16);

            int recordType = Integer.parseInt(line.substring(7, 9), 16);
            if (!(recordType == 0 || recordType == 1)) {
                Dialogs.createErrorDialog(stage, "Intel Hex Parse Error",
                        "Line " + lineNumber +
                                " has a record type other than data record (00)" +
                                " or end of file record (01), which CPUSim means CPUSim cannot"
                                + "parse this file.").showAndWait();
            }
            // check the checksum for the line
            // first get all the chars after the colon into a byte array
            BigInteger bline = new BigInteger(line.substring(1), 16);
            byte[] byteArray = bline.toByteArray();
            // The toByteArray() method might add an extra byte of 0
            // at the beginning of the array, but it won't affect the
            // check sum.
            int sum = 0;
            for (byte b : byteArray)
                sum += b;
            if ((sum & 255) != 0) {
                Dialogs.createErrorDialog(stage, "Intel Hex Parse Error",
                        "Line " + lineNumber +
                                " has an incorrect checksum value.").showAndWait();
            }
            long data = Convert.fromHexadecimalStringToLong(line.substring(9, 9 + bytesOfData * 2),
                    bytesOfData * 8);

            ramToBe.add(new RAMLocation(address, data, ram, false, "", new SourceLine(lineNumber,
                    pathName)));

            lineNumber++;
        }
        // if we get here, there must not have been an end-of-line
        // record type, so throw an exception
        Dialogs.createErrorDialog(stage, "Intel Hex Parse Error",
                "The file is missing an end-of-line record.").showAndWait();
    }

    /**
     * sets or clears a break point in the given ram at the line corresponding
     * to the given SourceLine.
     * @param sourceLine the file and line number where the break is to be set/cleared
     * @param set true if setting the breakpoint, false if clearing the breakpoint
     */
    public void setBreakPointInRAM(SourceLine sourceLine, boolean set) {
        checkState(machine.get().getCodeStore().isPresent());
        RAM ram = machine.get().getCodeStore().get();
        for (RAMLocation rLoc : ram.data()) {
            if (sourceLine.equals(rLoc.getSourceLine())) {
                rLoc.setBreak(set);
            }
        }
    }

    /**
     * sets the break point status of all RAMLocations so that they match the break
     * point status of the corresponding lines of the currently loaded assembly program.
     */
    private void setBreakPointsInRam() {
        checkState(machine.get().getCodeStore().isPresent());
        RAM ram = machine.get().getCodeStore().get();
        for (RAMLocation rLoc : ram.data()) {
            if (rLoc.getSourceLine() != null) {
                SourceLine sourceLine = rLoc.getSourceLine();
                Set<Integer> breakPoints = desktopController.getAllBreakPointLineNumbersForFile(
                        sourceLine.getFileName());
                rLoc.setBreak(breakPoints.contains(sourceLine.getLine()));
            }
        }
    }

}
