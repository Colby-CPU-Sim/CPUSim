/**
 * File: ConsoleManager
 * User: djskrien
 * Date: 8/10/13
 */
package cpusim.util;

import cpusim.model.Machine;
import cpusim.model.microinstruction.Microinstruction;
import cpusim.model.microinstruction.IO;
import cpusim.model.module.ControlUnit;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.fxmisc.richtext.StyledTextArea;

/**
 * This class manages the messages and input and output sent to and from
 * the console TextArea at the bottom of the Desktop window.
 */
public class ConsoleManager implements ChangeListener<Machine.StateWrapper>
{
    private StyledTextArea ioConsole;

    public ConsoleManager(StyledTextArea text) {
        ioConsole = text;
    }

    /**
     * Receive notifications that a module
     * has modified a property. This method is called within the
     * run thread and so any GUI events it generates need to be called
     * using invokeLater
     * @param stateWrapper the variable that is being listened
     * @param oldStateWrapper the value of the state before changed
     * @param newStateWrapper the new state object
     */
    @Override
    public void changed(ObservableValue<? extends Machine.StateWrapper> stateWrapper,
                        Machine.StateWrapper oldStateWrapper, Machine.StateWrapper newStateWrapper) {

        if (newStateWrapper.getState() == Machine.State.START_OF_EXECUTE_THREAD) {
            if (((Boolean) newStateWrapper.getValue()))  // machine is in RUN mode
                printlnToConsole("EXECUTING...");
        }
        else if (newStateWrapper.getState() == Machine.State.EXCEPTION_THROWN) {
            String msg = (String) newStateWrapper.getValue();
            printlnToConsole("EXECUTION HALTED DUE TO AN EXCEPTION: " + msg);
        }
        else if (newStateWrapper.getState() == Machine.State.EXECUTION_ABORTED) {
            printlnToConsole("EXECUTION HALTED BY THE USER");
        }
        else if (newStateWrapper.getState() == Machine.State.START_OF_MICROINSTRUCTION) {
            // if it is an input IO micro using the console, make the console yellow
            ControlUnit.State state = (ControlUnit.State) newStateWrapper.getValue();
            Microinstruction currentMicro = state.getInstr().getMicros().get(state.getIndex());
            if(currentMicro instanceof IO) {
                IO micro = (IO) currentMicro;
                if(micro.getDirection().equals("input") &&
                        micro.getConnection().equals(GUIChannels.CONSOLE)) {
                    printToConsole("","yellow");
                    Platform.runLater(() -> ioConsole.requestFocus());
                }
            }
        }
        else if (newStateWrapper.getState() == Machine.State.EXECUTION_HALTED ||
                newStateWrapper.getState() == Machine.State.HALTED_STEP_BY_MICRO) {
            Platform.runLater(() -> ioConsole.setStyle("-fx-background-color: white"));
            if (((boolean) newStateWrapper.getValue())) {
                // halt bits are set so the halt is normal
                printlnToConsole("EXECUTION HALTED NORMALLY due to the setting of the bit(s): " +
                        newStateWrapper.getMachine().haltBitsThatAreSet());
            }
        }
        // ignore all other possible states:
        //   "never run" --initial state when machine is loaded
        //	 "start of machine cycle"
        //   "break"
    }


    public void printToConsole(final String message, final String backgroundColor) {
        Platform.runLater(() -> {
            ioConsole.appendText(message);
            ioConsole.setStyle("-fx-background-color: " + backgroundColor);
        });
    }

    public void printToConsole(final String message) {
        printToConsole(message, "white");
    }

    public void printlnToConsole(final String message) {
        printToConsole(message + "\n", "white");
    }

    public void printlnToConsole(final String message, final String backgroundColor) {
        printToConsole(message + "\n", backgroundColor);
    }

}
