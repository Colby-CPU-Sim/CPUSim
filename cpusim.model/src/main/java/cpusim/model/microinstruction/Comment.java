/*
 * File: Comment.java
 * Author: djskrien
 * Class: CS 361
 * Project: 
 * Date: Jun 10, 2010
 */

package cpusim.model.microinstruction;

import cpusim.model.Module;
import cpusim.xml.HtmlEncoder;

/**
 * Comment microinstructions do nothing.  They are included just so that their
 * names can be used as comments in the fetch or execute sequences.
 * Their text can be edited directly from the fetch or execute sequence lists
 * in the dialog boxes by double-clicking on them or selecting them and then
 * pressing F2.  When you are done editing, just press Enter or Return.
 */
public class Comment extends Microinstruction
{
    /**
     * Constructor
     */
    public Comment()
    {
        super("Comment", null);
    } // end constructor

    
    /**
     * returns the class of the microinstruction
     * @return the class of the microinstruction
     */
    @Override
    public String getMicroClass(){
        return "comment";
    }

    /**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
    @Override
    public boolean uses(Module<?> m)
    {
        return false;
    }

    @Override
    public void execute()
    {
        //do nothing
    }
    
    @Override
    protected void validateState() {
        // nothing to validate
    }
    
    /**
     * returns the HTML description
     * @return the HTML description
     */
    @Override
    public String getHTMLDescription(String indent)
    {
        //not used, since Comment micros only appear in the execute
        //or fetch sequences, in which case, just the name (encoded for
        //HTML is used)
        return "";
    }

    /**
     * returns the XML description
     * @return the XML description
     */
    @Override
    public String getXMLDescription(String indent)
    {
        return indent + "<Comment name=\"" + HtmlEncoder.sEncode(getName()) +
                "\" id=\"" + getID() + "\" />";
    }
} // end Comment class
