/**
 * File:    	HighlightManager.java
 * Author:		Dale Skrien
 * Project: 	CPU Sim
 * Date:    	June, 2001
 * <p>
 * Description:
 * This file contains the class that manages the highlighting
 * of the rows of RAM and assembly code during stepping.  It is only active when the
 * debug toolbar is visible.  It stores Register/RAM pairs and
 * is a ChangeListener to the machine, which tells it when
 * to highlight the rows of RAMs and the corresponding row of the assembly program.
 */

package cpusim.util;

import cpusim.Mediator;
import cpusim.gui.desktop.DesktopController;
import cpusim.gui.desktop.RamTableController;
import cpusim.gui.desktop.editorpane.LineNumAndBreakpointFactory;
import cpusim.model.Machine;
import cpusim.model.assembler.SourceLine;
import cpusim.model.module.RAM;
import cpusim.model.module.RAMLocation;
import cpusim.model.module.Register;
import cpusim.model.module.RegisterRAMPair;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import org.fxmisc.richtext.InlineStyleTextArea;

import java.io.File;
import java.util.*;

/**
 * class to handle highlighting of rows in the table when in debug mode
 */
public class HighlightManager implements ChangeListener<Machine.StateWrapper>
{

    private DesktopController desktop;
    private Mediator mediator;
    private HashMap<RAM, Vector<RegisterRAMPair>> highlightingPairs;
    private boolean startBreak, endBreak;
    private RAM breakRAM;
    private int breakAddress;

    /**
     * Constructor
     *
     * @param mediator stores the machine and information needed
     * @param desktop  desktop that holds the register table and ram table
     */
    public HighlightManager(Mediator mediator, DesktopController desktop) {
        this.desktop = desktop;
        this.mediator = mediator;
        this.breakRAM = null;
        this.breakAddress = -1;
        this.startBreak = false;
        highlightingPairs = new HashMap<>();
        mediator.getMachine().stateProperty().addListener(this);
    }

    /**
     * replaces the existing pairs with the ones in newPairs
     *
     * @param newPairs the pairs that will be used
     */
    public void setRegisterRAMPairs(ObservableList<RegisterRAMPair> newPairs) {
        highlightingPairs.clear();
        ObservableList<RAM> rams = mediator.getMachine().getModules(RAM.class);
        for (RAM ram : rams) {
            highlightingPairs.put(ram, new Vector<>());
        }
        for (RegisterRAMPair pair : newPairs) {
            highlightingPairs.get(pair.getRam()).addElement(pair);
        }
    }

    /**
     * returns all RegisterRAMPairs in a new list
     *
     * @return all RegisterRAMPairs in a new list
     */
    public ObservableList<RegisterRAMPair> getRegisterRAMPairs() {
        ObservableList<RegisterRAMPair> result = FXCollections.observableArrayList();
        for (Vector<RegisterRAMPair> pairs : highlightingPairs.values()) {
            for (int i = 0; i < pairs.size(); i++)
                result.add(pairs.elementAt(i));
        }
        return result;
    }

    /**
     * updates highlightingPairs based on a change in the Registers
     * or RAMs of the machine through editing
     * the machine via the Modules Dialog.
     */
    public void updatePairsForNewRegistersAndRAMs() {
        ObservableList<RAM> rams = mediator.getMachine().getModules(RAM.class);

        //update pairs for new RAMs
        for (RAM ram : rams) {
            if (!highlightingPairs.containsKey(ram)) {
                highlightingPairs.put(ram, new Vector<>());
            }
        }
        //update pairs for deleted RAMs
        //the keySet is created to avoid ConcurrentModificationExceptions
        Set<RAM> keySet = new HashSet<>(highlightingPairs.keySet());
        for (RAM ram : keySet) {
            if (!rams.contains(ram)) {
                highlightingPairs.remove(ram);
            }
        }

        //update pairs for deleted Registers
        ObservableList allRegisters = mediator.getMachine().getAllRegisters();
        for (Vector<RegisterRAMPair> pairs : highlightingPairs.values()) {
            for (int i = pairs.size() - 1; i >= 0; i--) {
                RegisterRAMPair pair = pairs.elementAt(i);
                if (!allRegisters.contains(pair.getRegister())) {
                    pairs.remove(pair);
                }
            }
        }
    }

    /**
     * highlights and scrolls to the cells of RAM that are specified in the
     * highlightingPairs table.  If the RAM cell has a corresponding non-null
     * SourceLine, then that line of the text file is also brought to front
     * and highlighted.  Also, if the machine stopped at a break point, the break point
     * is highlighted in both RAM and text.
     */
    public void highlightCellsAndText() {

        ObservableList<RamTableController> ramTableControllers = desktop.getRAMControllers();

        // highlight RAM cells and text for RegisterRAM pairs
        // and highlight break point if a break occurred
        for (RamTableController controller : ramTableControllers) {
            RAM ram = controller.getRam();
            int[] addresses = getAddressesToHighlight(ram);
            //now have the ram window highlight the cells
            controller.highlightRows(addresses);
            //now have the text window highlight the corresponding rows
            highlightText(ram, addresses);


            // if break occurred, highlight the cell and text at the break point
            // if ending a break, unhighlight the cell and text
            if (startBreak && ram == breakRAM) {
                // highlight the RAM cell and corresponding text line in the code
                // and change the icon in the left column to an orange block
                controller.highlightBreakInRAM(breakAddress, true);
                highlightBreakInText(breakRAM, breakAddress);
                startBreak = false;
                endBreak = true;
            }
            else if (endBreak && ram == breakRAM) {
                // unhighlight the break line in RAM
//                controller.highlightBreakInRAM(breakAddress, false);
                // and unhighlight the corresponding text line in the code
                // and change the icon in the left column back to its original form
                unhighlightBreakInText(breakRAM, breakAddress);
                endBreak = false;
            }
        }
    }


    /**
     * Checks the address of the given RAM and if it has a non-null
     * SourceLine, then bring the text window to front and highlight the
     * row.
     * Displays an error message if the text file doesn't exist or if
     * the desired line of the file does not exist.
     *
     * @param ram          the RAM with the SourceLines to be highlighted
     * @param breakAddress the address of the break point in ram
     */
    private void highlightBreakInText(RAM ram, int breakAddress) {
        SourceLine sourceLine = ram.getSourceLine(breakAddress);
        if (sourceLine != null) {
            File file = new File(sourceLine.getFileName());
            if (!file.canRead()) {
                return;
            }
            Tab newTabForFile = desktop.getTabForFile(file);
            InlineStyleTextArea text = (InlineStyleTextArea) newTabForFile.getContent();
            int line = sourceLine.getLine();
            int start = getLineStartOffset(text.getText(), line);
            int end = getLineEndOffset(text.getText(), line);
            text.selectRange(start, end);

            // now change the background of the label in left column to orange
            LineNumAndBreakpointFactory lFactory =
                    (LineNumAndBreakpointFactory) text.getParagraphGraphicFactory();
            lFactory.setCurrentBreakPointLineNumber(line);
        }
    }


    private void unhighlightBreakInText(RAM ram, int breakAddress) {
        SourceLine sourceLine = ram.getSourceLine(breakAddress);
        if (sourceLine != null) {
            File file = new File(sourceLine.getFileName());
            if (!file.canRead()) {
                return;
            }
            Tab tabForFile = desktop.getTabForFile(file);
            InlineStyleTextArea text = (InlineStyleTextArea) tabForFile.getContent();
            LineNumAndBreakpointFactory lFactory =
                    (LineNumAndBreakpointFactory) text.getParagraphGraphicFactory();
            // change the background of the label in left column back to the original color
            lFactory.setCurrentBreakPointLineNumber(-1);
        }
    }

    /**
     * Checks the addresses of the given RAM and if they have a non-null
     * SourceLine, then bring the text window to front and highlight those
     * rows.
     * Displays an error message if the text file doesn't exist or if
     * the desired line of the file does not exist.
     *
     * @param ram       the RAM with the SourceLines to be highlighted
     * @param addresses the addresses of the cells whose SourceLines are to be highlighted
     */
    private void highlightText(RAM ram, int[] addresses) {
        for (int address : addresses) {
            SourceLine sourceLine = ram.getSourceLine(address);
            if (sourceLine != null) {
                File file = new File(sourceLine.getFileName());
                if (!file.canRead()) {
                    Dialogs.createErrorDialog(desktop.getStage(), "File Not Found",
                            "CPU Sim could not find the file to open and highlight:  " +
                                    file.getAbsolutePath()).showAndWait();
                    return;
                }
                Tab newTabForFile = desktop.getTabForFile(file);
                InlineStyleTextArea text = (InlineStyleTextArea) newTabForFile.getContent();
                int line = sourceLine.getLine();
                int start = getLineStartOffset(text.getText(), line);
                int end = getLineEndOffset(text.getText(), line);
                text.selectRange(start, end);

                //window.highlightAndScrollToLine(sourceLine.getLine());

            }
        }
    }

    /**
     * Determines the offset of the start of the given line.
     *
     * @param content the text string that content the line
     * @param line    line index
     * @return the offset
     */
    private int getLineStartOffset(String content, int line) {
        if (line == 0) {
            return 0;
        }
        int count = 0;
        int lineNum = 0;
        while (count < content.length()) {
            if (content.substring(count, count + 1).equals("\n")) {
                lineNum++;
                count++;
            }
            else {
                count++;
            }
            if (lineNum == line) {
                return count;
            }
        }
        return content.length() - 1;
    }

    /**
     * Determines the offset of the end of the given line.
     *
     * @param content the text string that content the line
     * @param line    line index
     * @return the offset
     */
    private int getLineEndOffset(String content, int line) {
        int count = getLineStartOffset(content, line);
        while (count < content.length()) {
            if (content.substring(count, count + 1).equals("\n")) {
                count++;
                if (count >= content.length()) {
                    count = content.length() - 1;
                }
                return count;
            }
            else {
                count++;
            }
        }
        return content.length() - 1;
    }

    //--------------------------------
    // returns an array of addresses to be highlighted for the given RAM
    private int[] getAddressesToHighlight(RAM ram) {
        Vector<RegisterRAMPair> registerRAMPairs = highlightingPairs.get(ram);
        int[] addresses = new int[registerRAMPairs.size()];
        for (int i = 0; i < addresses.length; i++) {
            RegisterRAMPair pair = registerRAMPairs.elementAt(i);
            Register register = pair.getRegister();
            if (pair.isDynamic()) {  // get the current value of the register
                addresses[i] = (int) register.getValue();
            }
            else { // get the value of the register at the start of the machine cycle.
                addresses[i] = pair.getAddressAtStart();
            }
            //if address register has 1 in the leftmost bit, its value is stored
            //as a 2's complement negative, but we want to treat it as an
            //unsigned decimal
            if (addresses[i] < 0 && register.getWidth() < 32) {
                addresses[i] += 1 << register.getWidth();
            }
        }
        return addresses;
    }

    /**
     * save in each RegisterRAMPair the value of register at the start of a cycle.
     */
    public void saveStartOfCycleValues() {
        Collection<Vector<RegisterRAMPair>> c = highlightingPairs.values();
        for (Vector<RegisterRAMPair> pairs : c) {
            for (int i = 0; i < pairs.size(); i++) {
                RegisterRAMPair info = pairs.elementAt(i);
                info.setAddressAtStart((int) info.getRegister().getValue());
            }
        }
    }

    /**
     * Receive notifications that a module
     * has modified a property.  This method is called in
     * Machine.execute() and so no GUI stuff is
     * allowed here.
     *
     * @param stateWrapper    the variable that is being listened
     * @param oldStateWrapper the value of the state before changed
     * @param newStateWrapper the new state object
     */
    public void changed(ObservableValue<? extends Machine.StateWrapper> stateWrapper,
                Machine.StateWrapper oldStateWrapper, Machine.StateWrapper newStateWrapper) {

        if (newStateWrapper.getState() == Machine.State.START_OF_MACHINE_CYCLE) {
            //save values of all the registers
            saveStartOfCycleValues();
        }

        // update the break state
        if ( ! this.startBreak && newStateWrapper.getState() == Machine.State.BREAK) {
            saveStartOfCycleValues();
            // in the next update of the display, turn on the break highlighting
            this.breakRAM = ((RAMLocation) newStateWrapper.getValue()).getRam();
            this.breakAddress = (int) ((RAMLocation) newStateWrapper.getValue()).getAddress();
            this.startBreak = true;
            this.endBreak = false;
        }
//        else if ( ! this.startBreak && newStateWrapper.getState() != Machine.State.BREAK) {
//            this.startBreak = false;
//            this.endBreak = false;
//        }
//        else if (this.startBreak && newStateWrapper.getState() != Machine.State.BREAK) {
//            // in the next update of the display, turn off the break highlighting
//            this.startBreak = false;
//            this.endBreak = true;
//        }
        // do nothing if startBreak && state == BREAK
    }
}
