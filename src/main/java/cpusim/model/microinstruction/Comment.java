/*
 * File: Comment.java
 * Author: djskrien
 * Class: CS 361
 * Project: 
 * Date: Jun 10, 2010
 */

package cpusim.model.microinstruction;

import cpusim.model.Microinstruction;
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
     * duplicate the set class and return a copy of the original Set class.
     *
     * @return a copy of the Set class
     */
    public Object clone()
    {
        assert false : "Comment.clone() was called.";
        return null; //to satisfy the compiler's need for a return value
    } // end clone()

    /**
     * copies the data from the current micro to a specific micro
     * @param newMicro the micro instruction that will be updated
     */
    public void copyDataTo(Microinstruction newMicro)
    {
        assert false : "Comment.copyDataTo() was called.";
    } // end copyDataTo()


    /**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
    public boolean uses(Module m)
    {
        return false;
    }


    public void execute()
    {
        //do nothing
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
    public String getHTMLDescription()
    {
        //not used, since Comment micros only appear in the execute
        //or fetch sequences, in which case, just the name (encoded for
        //HTML is used)
        return "";
    }

//    Use the inherited toString() method that returns the name of the Microinstruction
//    public String toString()
//    {
//        //so that it always is displayed in italics and gray.
//        return "<html><em><font color=gray>" +
//                getHTMLName() + "</font></em></html>";
//    }

    /**
     * returns the XML description
     * @return the XML description
     */
    public String getXMLDescription()
    {
        return "<Comment name=\"" + HtmlEncoder.sEncode(getName()) +
                "\" id=\"" + getID() + "\" />";
    }
} // end Comment class
