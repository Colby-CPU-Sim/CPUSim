package cpusim.gui.options;

import javafx.beans.property.SimpleObjectProperty;
import cpusim.IOChannel;
import cpusim.microinstruction.IO;

public class IOOptionsData {
	private SimpleObjectProperty<IO> io;
	private SimpleObjectProperty<IOChannel> channel;
	
	/**
	 * Constructor for a new set of IOOptions Data.
	 * 
	 * @param i - The IO microinstruction.
	 * @param cb - The corresponding IOChannel.
	 */
	public IOOptionsData(IO i, IOChannel cb) {
		io = new SimpleObjectProperty<IO>();
		channel = new SimpleObjectProperty<IOChannel>();
		io.set(i);
		channel.set(cb);
	}
	
	////////////////// Getters //////////////////
	
	/**
	 * Gives the IO SimpleObjectProperty.
	 * 
	 * @return the IO SimpleObjectProperty.
	 */
	public SimpleObjectProperty<IO> ioProperty() {
		return io;
	}
	
	/**
	 * Gives the IOChannel SimpleObjectProperty.
	 * 
	 * @return the IOChannel SimpleObjectProperty.
	 */
	public SimpleObjectProperty<IOChannel> channelProperty() {
		return channel;
	}
	
	/**
	 * Gives the current IO in this data set.
	 * 
	 * @return the current IO in this data set.
	 */
    public IO getIo() { 
    	return io.get(); 
    }
    
    /**
	 * Gives the current IOChannel in this data set.
	 * 
	 * @return the current IOChannel in this data set.
	 */
    public IOChannel getChannel() { 
    	return channel.get(); 
    }

	////////////////// Setters //////////////////
    
    /**
     * Sets the current IO microinstruction.
     * 
     * @param n - the new IO microinstruction.
     */
	public void setIo(IO n) {
		io.set(n);
	}
	
	/**
     * Sets the current IOChannel.
     * 
     * @param n - the new IOChannel.
     */
	public void setChannel(IOChannel cb) {
		channel.set(cb);
	}
	
	/**
	 * Gives a clone of this IOOptionsData.
	 */
	public Object clone() {
		return new IOOptionsData(io.get(), channel.get());
	}
	
}