///////////////////////////////////////////////////////////////////////////////
// File:    	HighlightManager.java
// Author:		Dale Skrien
// Project: 	CPU Sim
// Date:    	June, 2001
//
// Description:
//   This file contains the class that manages the highlighting
//   of the rows of RAM during stepping.  It is only active when the
//   debug toolbar is visible.  It stores Register/RAM pairs and
//   is a PropertyChangeListener to the machine, which tells it when
//   to highlight the rows of RAMs.


///////////////////////////////////////////////////////////////////////////////
// the package in which our file resides

package cpusim.util;

import cpusim.BreakException;
import cpusim.Machine;
import cpusim.Mediator;
import cpusim.gui.desktop.DesktopController;
import cpusim.gui.desktop.RamTableController;
import cpusim.module.RAM;
import cpusim.module.Register;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.InlineStyleTextArea;

import java.io.File;
import java.util.*;

/**
 * class to highlight rows in the table when in debug mode
 */
public class HighlightManager
        implements ChangeListener<Machine.StateWrapper>
{

    //instance variables
    private DesktopController desktop;
    private Mediator mediator;
    private int breakAddress;  //the address to be highlighted in red
    private RAM breakRAM;      //the ram in which the break address resides
    private HashMap<RAM, Vector<RegisterRAMPair>> highlightingPairs;

    /**
     * Constructor
     * @param mediator stores the machine and information needed
     * @param desktop desktop that holds the register table and ram table
     */
    public HighlightManager(Mediator mediator,
                            DesktopController desktop)
    {
        this.desktop = desktop;
        this.mediator = mediator;
        highlightingPairs = new HashMap<>();
        breakAddress = -1; //no break address
        mediator.getMachine().stateProperty().addListener(this);
    }

    /**
     * replaces the existing pairs with the ones in newPairs
     * @param newPairs the pairs that will be used
     */
    public void setRegisterRAMPairs(ObservableList<RegisterRAMPair> newPairs)
    {
        highlightingPairs.clear();
        ObservableList rams = mediator.getMachine().getModule("rams");
        for (Object ram : rams) {
            highlightingPairs.put((RAM) ram,
                    new Vector<RegisterRAMPair>());
        }
        for (RegisterRAMPair pair : newPairs) {
            highlightingPairs.get(pair.getRam()).addElement(pair);
        }
    }

    /**
     * returns all RegisterRAMPairs in a new list
     * @return all RegisterRAMPairs in a new list
     */
    public ObservableList<RegisterRAMPair> getRegisterRAMPairs()
    {
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
    public void updatePairsForNewRegistersAndRAMs()
    {
        ObservableList rams = mediator.getMachine().getModule("rams");

        //update pairs for new RAMs
        for (Object ram : rams) {
            if (! highlightingPairs.containsKey(ram))
                highlightingPairs.put((RAM) ram, new Vector<RegisterRAMPair>());
        }
        //update pairs for deleted RAMs
        //the keySet is created to avoid ConcurrentModificationExceptions
        Set<RAM> keySet = new HashSet<>(highlightingPairs.keySet());
        for (RAM ram : keySet) {
            if (!rams.contains(ram))
                highlightingPairs.remove(ram);
        }

        //update pairs for deleted Registers
        ObservableList allRegisters = mediator.getMachine().getAllRegisters();
        for (Vector<RegisterRAMPair> pairs : highlightingPairs.values()) {
            for (int i = pairs.size()-1; i >= 0; i--) {
                RegisterRAMPair pair = pairs.elementAt(i);
                if (!allRegisters.contains(pair.getRegister()))
                    pairs.remove(pair);
            }
        }
    }

    /**
     * highlights and scrolls to the cells of RAM that are specified in the
     * highlightingPairs table.  If the RAM cell has a corresponding non-null
     * SourceLine, then that line of the text file is also brought to front
     * and highlighted.
     */
    public void highlightCellsAndText()
    {
        ObservableList ramTables = desktop.getRAMController();

        for (Object key : ramTables) {
            RamTableController table = (RamTableController) key;
            RAM ram = table.getRam();
            int[] addresses = getAddressesToHighlight(ram);

            //now have the window highlight the cells
            table.highlightRows(addresses);
            highlightText(ram, addresses);
            if (ram == breakRAM) {
                table.highlightBreak(breakAddress);
                breakRAM = null; //turn off highlight when execution resumes
            }
        }
    }


    /**
     * Checks the addresses of the given RAM and if they have a non-null
     * SourceLine, then bring the text window to front and highlight those
     * rows.
     * Displays an error message if the text file doesn't exist or if
     * the desired line of the file does not exist.
     *
     * @param ram the RAM with the SourceLines to be highlighted
     * @param addresses the addresses of the cells whose SourceLines are to be highlighted
     */
    private void highlightText(RAM ram, int[] addresses)
    {
        for (int address : addresses) {
            SourceLine sourceLine = ram.getSourceLine(address);
            if (sourceLine != null) {
                File file = new File(sourceLine.getFileName());
                if (!file.canRead()) {
                    CPUSimConstants.dialog.
                            owner(desktop.getStage()).
                            masthead("File Not Found").
                            message("CPU Sim could not find the file to open and highlight:  " +
                                    file.getAbsolutePath()).
                            showError();
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
     * @param line line index
     * @return the offset
     */
    private int getLineStartOffset(String content, int line){
        int count = 0;
        int lineNum = 0;
        while ( count < content.length() ){
             if (content.substring(count,count+1).equals("\n")){
                 lineNum++;
                 count ++;
             }else {
                 count++;
             }
             if (lineNum == line) {
                 return count;
             }
        }
        return content.length()-1;
    }

    /**
     * Determines the offset of the end of the given line.
     *
     * @param content the text string that content the line
     * @param line line index
     * @return the offset
     */
    private int getLineEndOffset(String content, int line){
        int count = getLineStartOffset(content, line);
        while ( count < content.length() ){
            if (content.substring(count,count+1).equals("\n")){
                count ++;
                if (count >= content.length())
                    count = content.length() - 1;
                return count;
            }else {
                count++;
            }
        }
        return content.length()-1;
    }

    //--------------------------------
    // returns an array of addresses to be highlighted for the given RAM
    public int[] getAddressesToHighlight(RAM ram)
    {
        Vector registerRAMPairs = highlightingPairs.get(ram);
        int[] addresses = new int[registerRAMPairs.size()];
        for (int i = 0; i < addresses.length; i++) {
            RegisterRAMPair info = (RegisterRAMPair)
                    registerRAMPairs.elementAt(i);
            Register register = info.getRegister();
            if (info.isDynamic())
                addresses[i] = (int) register.getValue();
            else
                addresses[i] = info.getAddressAtStart();
            //if address register has 1 in the leftmost bit, its value is stored
            //as a 2's complement negative, but we want to treat it as an
            //unsigned decimal
            if( addresses[i] < 0 && register.getWidth() < 32) {
                addresses[i] += 1 << register.getWidth();
            }
        }
        return addresses;
    }

    /**
     * save the new value of registers at the start of a cycle.
     */
    public void saveStartOfCycleValues()
    {
        Collection c = highlightingPairs.values();
        for (Object aC : c) {
            Vector infos = (Vector) aC;
            for (int i = 0; i < infos.size(); i++) {
                RegisterRAMPair info = (RegisterRAMPair) infos.elementAt(i);
                info.setAddressAtStart((int) info.getRegister().getValue());
            }
        }
    }

    public void clearHighlights(){
        ObservableList ramTables = desktop.getRAMController();
        for (Object key : ramTables) {
            RamTableController table = (RamTableController) key;
            table.getTable().getSelectionModel().clearSelection();
        }

    }

    //--------------------------------------
    // --- PropertyChangeListener Methods---
    //--------------------------------------

    //------------------------------------------
    //
    // Receive notifications that a module
    // has modified a property.  This method is called in the
    // SwingWorker's thread in Machine.execute() and so no GUI stuff is
    // allowed here.

    /**
     * Receive notifications that a module
     * has modified a property.  This method is called in
     * Machine.execute() and so no GUI stuff is
     * allowed here.
     * @param stateWrapper the variable that is being listened
     * @param oldStateWrapper the value of the state before changed
     * @param newStateWrapper the new state object
     */
    public void changed(ObservableValue<? extends Machine.StateWrapper> stateWrapper,
                        Machine.StateWrapper oldStateWrapper,
                        Machine.StateWrapper newStateWrapper)
    {
        if (newStateWrapper.getState() == Machine.State.START_OF_MACHINE_CYCLE) {
            //save values of all the registers
            saveStartOfCycleValues();
        }
        else if( newStateWrapper.getState() == Machine.State.BREAK) {
            breakAddress = ((BreakException) newStateWrapper.getValue()).breakAddress;
            breakRAM = ((BreakException) newStateWrapper.getValue()).breakRAM;
        }
        //else it was a something that we don't care about
    }

} //end of class HighlightManager
