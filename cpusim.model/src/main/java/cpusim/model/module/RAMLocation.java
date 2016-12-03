/**
 * RAMData.java
 * Description: a simple class that just has the two fields address and value that
 * 
 * Last Modified: 6/4/13
 * 
 * @author: Ben Borchard
 */

package cpusim.model.module;

import cpusim.util.SourceLine;
import javafx.beans.property.*;

/**
 * A kind of address and data in the RAM
 */
public class RAMLocation {

    private LongProperty address;
    private LongProperty value;
    private RAM ram;
    private BooleanProperty breakPoint;
    private StringProperty comment;
    private SourceLine sourceLine;

    /**
     * Constructor
     * @param addr address of data
     * @param val value store in that line
     * @param ram the ram that stores the data
     * @param breakPoint tell if it's a breakPoint
     * @param comment comment from assmebled instruction
     * @param sourceLine sourceline from the assembled instruction
     */
    public RAMLocation(int addr, long val, RAM ram, boolean breakPoint, String comment,
            SourceLine sourceLine){
        this.address = new SimpleLongProperty(addr);
        this.ram = ram;
        this.value = new SimpleLongProperty(val);
        this.breakPoint = new SimpleBooleanProperty(breakPoint);
        this.comment = new SimpleStringProperty(comment);
        this.sourceLine = sourceLine;
    }
    
    /**
     * accessor method for value field
     * @return the value
     */
    public long getValue(){
        return value.get();
    }
    
    /**
     * sets the value of a ram location
     * @param val new value
     */
    public void setValue(long val){
        value.set(val);
    }

    /**
     * getter for the value property object
     * @return the value property
     */
    public LongProperty valueProperty() {
        return value;
    }
    
    /**
     * accessor method for address field
     * @return the address
     */
    public long getAddress(){
        return address.get();
    }

    /**
     * getter the address property
     * @return the address property object
     */
    public LongProperty addressProperty() {
    	return address;
    }

    /**
     * getter of the breakPoint value
     * @return the breakPoint value
     */
    public boolean getBreak(){
        return breakPoint.get();
    }

    /**
     * set the breakPoint property
     * @param breakPoint the new value of breakPoint
     */
    public void setBreak(boolean breakPoint){
        this.breakPoint.set(breakPoint);
    }

    /**
     * get the breakPoint property
     * @return the breakPoint property object
     */
    public BooleanProperty breakProperty() {
        return breakPoint;
    }

    /**
     * get the comment as a string
     * @return the string of the comment
     */
    public String getComment(){
        return comment.get();
    }

    /**
     * set the comment the new value
     * @param comment new string of comment
     */
    public void setComment(String comment){
        this.comment.set(comment);
    }

    /**
     * get the sourceline
     * @return the source line
     */
    public SourceLine getSourceLine(){
        return sourceLine;
    }

    /**
     * set the source line
     * @param sourceLine the new sourceline to replace
     */
    public void setSourceLine(SourceLine sourceLine){
        this.sourceLine = sourceLine;
    }

    /**
     * get the ram object that stores the object
     * @return a ram object
     */
    public RAM getRam(){
        return ram;
    }
}