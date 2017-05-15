package cpusim.util;


import cpusim.gui.iochannel.ConsoleChannel;
import cpusim.gui.iochannel.DialogChannel;
import cpusim.model.iochannel.BufferedChannel;
import cpusim.model.iochannel.IOChannel;

/**
 * Utility class that holds singleton instances for GUIChannels.
 */
public abstract class GUIChannels {
    
    private GUIChannels() {
        // no-op
    }
    
    /**
     * Holds unbuffered shared references to channels.
     */
    public static abstract class Unbuffered {
        private Unbuffered() {
            // no-op
        }
        
        public static final ConsoleChannel CONSOLE = new ConsoleChannel("[Console]");
        
        public static final DialogChannel DIALOG = new DialogChannel("[Dialog]");
    }
    
    // the three standard options available for io channels
    public static final BufferedChannel CONSOLE = new BufferedChannel(Unbuffered.CONSOLE);
    
    public static final BufferedChannel DIALOG = new BufferedChannel(Unbuffered.DIALOG);
    
    public static final BufferedChannel FILE = new BufferedChannel("File...");
}
