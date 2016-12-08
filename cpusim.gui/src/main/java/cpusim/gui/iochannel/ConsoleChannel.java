/**
 * File: ConsoleChannel
 * Last update: August 2013
 */
/**
 * File: ConsoleChannel
 * Authors: Joseph Harwood and Jake Epstein
 * Date: 11/15/13
 *
 * Implemented the missing body of the reset() method;
 */
/**
 * File: ConsoleChannel
 * Last update: December 2013
 * Authors: Stephen Morse, Ian Tibbits, Terrence Tan
 * Class: CS 361
 * Project 7
 * 
 * change implemented interface from IOChannel to StringChannel
 * remove readingType field, Type enum and numBits field, channel 
 * no longer validates remove writeLong, writeAscii, writeUnicode,
 * readLong, readAscii, readUnicode, reset and getPrompt methods
 * add writeString(String s):void method that writes strings to console
 * add readString(String prompt):String method that writes prompt to
 * the console and waits for user input, then returns input.
 * Throws ExecutionException saying input cancelled if ABORT mode.
 * add resetNecessaryFields private convenience method
 * modify setMediator method�s KeyEvent handler for �Enter�
 * to remove validation of user input
 */
package cpusim.gui.iochannel;

import cpusim.Mediator;
import cpusim.model.ExecutionException;
import cpusim.model.Machine;
import cpusim.model.iochannel.IOChannel;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import org.fxmisc.richtext.StyledTextArea;

/**
 * This class implements IOChannel using a console that appears as a
 * panel along the bottom edge of the main CPU Sim desktop.
 */
public class ConsoleChannel extends AbstractStringChannel {
	/** Name of this Channel */
	private String name; 
	/** TextArea this channel uses for IO */
	private StyledTextArea ioConsole;

	/** Whether input has started in ioConsole or not */
	private boolean inputStarted;
	/** Sometimes need to delete enter after user hits enter */
	private boolean needToDeleteEnter;
	/** Used for determining if user halted machine */
	private boolean inputCancelled;
	/** Used to tell if user finished giving input */
	private boolean doneInput;
    
    /** To keep track of where user input starts in console */
	private int startCaret;
	/** reference to the mediator */
	private Mediator mediator;

	/** Line separator */
	private String LINE_SEPARATOR = System.getProperty("line.separator");
	
	/** added user input field */
	private String userInput;

	/**
	 * Constructor for new Console Channel. There is only
	 * one Console channel that is used, {@link cpusim.util.GUIChannels#CONSOLE}.
	 * 
	 * @param name - The name given to the console channel.
	 */
	public ConsoleChannel(String name) {
		this.name = name;
		this.ioConsole = null;
		inputCancelled = false;
	}
        
        /**
         * Returns the console text field in the desktop display.  Needed so that
         * it can be properly cleared upon run.
         * @return 
         */
        public StyledTextArea getIOConsole(){
            return ioConsole;
        }

	/**
	 * Sets the mediator and sets up ioConsole.
	 * 
	 * @param med - The Mediator.
	 */
	public void setMediator(Mediator med) {
		this.mediator = med;
		this.ioConsole = med.getDesktopController().getIOConsole();

		ioConsole.setEditable(false);
		ioConsole.setOnKeyPressed(event -> {
			if (ioConsole.isEditable()) {
				String content = ioConsole.getText();
				if (!inputStarted) {
					startCaret = content.length();
				}

				if (ioConsole.getCaretPosition() < startCaret) {
					ioConsole.positionCaret(content.length());
				}

				if (event.getCode().equals(KeyCode.BACK_SPACE)) {
					if (ioConsole.getCaretPosition() == startCaret) {
						ioConsole.insertText(startCaret, " ");
					}
				}
				else if (event.getCode().equals(KeyCode.ENTER)) {
					inputStarted = false;
					userInput = (ioConsole.getText(startCaret, ioConsole.getText()
							.length()));
					ioConsole.appendText(LINE_SEPARATOR);

					// reset
					ioConsole.setEditable(false);
					doneInput = true;
					return;
				}
				inputStarted = true;
			}
		});

		// the following code is useless since it is the only place needToDeleteEnter
		// is every accessed, and so it is always false.
//		ioConsole.setOnKeyReleased(
//				new EventHandler<KeyEvent>() {
//					@Override
//					public void handle(KeyEvent event) {
//						if (needToDeleteEnter) {
//							if (event.getCode().equals(KeyCode.ENTER)) {
//								String content = ioConsole.getText();
////                              TODO: Do I need to fix the next line?  I commented it
// out
////                                because ioConsole used to be a TextArea with a
// setText()
////                                method but now it's a StyledTextArea with no
// setText() method
////								ioConsole.setText(ioConsole.getText(0, content
// .length()-1));
//								ioConsole.positionCaret(content.length());
//								needToDeleteEnter = false;
//							}
//						}
//					}
//				});
	}

	/**
	 * Gives a string representation of the object.
	 * In this case, its name field.
	 */
	public String toString() {
		return name;
	}

	/**
	 * displays an output to the user
	 * @param s - the output displayed to the user
	 */
	@Override
	public void writeString(final String s) {
		if (s.isEmpty()){
			return;
		}
		try {
			Platform.runLater(new Runnable() {
				public void run() {
					ioConsole.appendText(s);
				}
			});
		} catch (Exception e) {
			throw new ExecutionException("An Exception was thrown" +
					" when we attempted to read a value from the console.");
		}
	}

	/**
	 * reads and returns the String input of the user
	 * @param prompt - the prompt to the user for input
	 * @return the String input by the user
	 */
	@Override
	public String readString(final String prompt) {
		try {
			Platform.runLater(new Runnable() {
				public void run() {
					ioConsole.appendText(prompt);
					ioConsole.setEditable(true);

				}
			});
		} catch (Exception e) {
			throw new ExecutionException("An Exception was thrown" +
					" when we attempted to read a value from the console.");
		}
		
		while (!doneInput && !inputCancelled) {
			try {
				inputCancelled = mediator.getMachine().getRunMode() == Machine.RunModes.ABORT;
				Thread.sleep(10);
			}
			catch (InterruptedException e) {
				System.out.println("Error while sleeping thread");
			}
		}
		
		if (inputCancelled) {
			resetNecessaryFields();
			Platform.runLater(new Runnable() {
				public void run() {
					ioConsole.appendText(LINE_SEPARATOR);
					ioConsole.setEditable(false);
				}
			});
			throw new ExecutionException("Read cancelled.");
			
		}
		
		// Reset Necessary Fields
		resetNecessaryFields();
		
		return userInput;
	}
	
	@Override
	public void reset() {
		super.reset();
	}

	/**
	 * Resets necessary fields at the end of 
	 * reading a string from user.
	 */
	private void resetNecessaryFields() {
		doneInput = false;
		inputCancelled = false;
		inputStarted = false;
	}

}
