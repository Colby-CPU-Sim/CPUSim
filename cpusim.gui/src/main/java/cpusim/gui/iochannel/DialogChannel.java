/**
 * File: DialogChannel
 * Last update: December 2013
 * Authors: Stephen Morse, Ian Tibbits, Terrence Tan
 * Class: CS 361
 * Project 7
 *
 * change implemented interface from IOChannel to StringChannel
 * remove result field & modify constructor to remove field instantiation
 * remove writeLong, writeAscii, writeUnicode, readLong, readAscii, 
 * readUnicode, and reset methods add writeString(String s):void method
 * that displays string in dialog 
 *
 * add readString(String prompt):String method that writes prompt to an input 
 * dialog and waits for user input, then returns input.
 *
 * Throws ExecutionException if the input is cancelled by the user
 */
package cpusim.gui.iochannel;

import com.google.common.base.MoreObjects;
import cpusim.model.ExecutionException;
import cpusim.model.iochannel.IOChannel;
import cpusim.model.util.units.ArchType;
import cpusim.util.Dialogs;
import cpusim.util.FXUtilities;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.Optional;

/**
 * This class implements IOChannel using Dialog Boxes.
 */
public class DialogChannel extends AbstractStringChannel {

    // Stage to bring up dialogs in front of
    private Stage stage;
    // Name of this channel
    private String name;
    // The input string returned from user
    private Optional<String> input;

    /**
     * Creates a new dialog channel. There is only
     * one DialogChannel that is used, {@link cpusim.util.GUIChannels#DIALOG}
     *
     * @param name - The name given to the console channel.
     */
    public DialogChannel(String name) {
        this.name = name;
        this.stage = null;
    }

    /**
     * Sets the Stage.
     *
     * @param stage the Stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    /**
     * displays an output to the user
     *
     * @param s - the output displayed to the user
     */
    @Override
    public void writeString(final String s) {
        try {
            FXUtilities.runAndWait(() -> Dialogs.createConfirmationDialog(stage, "Write", s).showAndWait());
        } catch (Exception e) {
            throw new ExecutionException("An Exception was thrown" +
                    " when we attempted to read from the dialog.");
        }
    }

    /**
     * reads and returns the String input of the user
     *
     * @param prompt - the prompt to the user for input
     * @return the String input by the user
     */
    @Override
    public String readString(final String prompt) {
        try {
            FXUtilities.runAndWait(() -> input = Dialogs.createTextInputDialog(stage,"Read Dialog",prompt).showAndWait());
        } catch (Exception e) {
            throw new ExecutionException("An Exception was thrown" +
                    " when we attempted to read from the dialog.");
        }

        if (input == null || !input.isPresent()) {
            // The user chose "Cancel" from the input dialog.
            // I don't think the input will ever be null, but I'm leaving the test
            // here anyway.
            throw new ExecutionException("Read cancelled.");
        }
        return input.get();
    }
    
    
    /**
     * Gives a string representation of the object.
     * In this case, its name field.
     */
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .addValue(this.name)
                .add("stage", this.stage)
                .toString();
    }

}