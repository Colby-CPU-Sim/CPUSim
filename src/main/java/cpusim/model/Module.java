///////////////////////////////////////////////////////////////////////////////
// File:    	Module.java
// Type:    	java application file
// Author:		Dale Skrien
// Project: 	CPU Sim
// Date:    	June, 1999
//
// Last Modified: 6/4/13
//
// Description:
//   This file contains a common superclass for registers, register arrays, and
//   condition bits, and RAMs, corresponding to the hardware modules.
//
// To be done:


///////////////////////////////////////////////////////////////////////////////
// the package in which our file resides

package cpusim.model;


///////////////////////////////////////////////////////////////////////////////
// the libraries we need to import

import cpusim.util.NamedObject;
import cpusim.xml.HtmlEncoder;
import javafx.beans.property.SimpleStringProperty;

import java.io.Serializable;


///////////////////////////////////////////////////////////////////////////////
// the Module class

public abstract class Module
        implements Cloneable, Serializable, NamedObject
{
    private SimpleStringProperty name;	//name of the module
    private String ID; //unique ID used when saving in XML
    protected Machine machine; //machine that holds the module

    //------------------------------
    // constructor

    public Module(String name)
    {
        this.name = new SimpleStringProperty(name);

        String s = super.toString();
        int index = s.indexOf('@');
        if (index == -1)
            ID = s;
        else
            ID = s.substring(7, index) + s.substring(index + 1);
    }
    
    public Module(String name, Machine machine){
        this(name);
        this.machine = machine;
    }

    //------------------------------
    //getters and setters to make it a Bean

    public void setName(String newName)
    {
    	name.set(newName);
    }

    public String getName()
    {
        return name.get();
    }

    public String getHTMLName()
    {
        return HtmlEncoder.sEncode(getName());
    }

    public String toString()
    {
        return name.get();
    }
    
    public SimpleStringProperty getNameProperty()
    {
        return name;
    }
    

    // the ID is a unique identifier for each module.  It is
    // used in the XML machine file.
    // if the toString() method returns "cpusim.xxx.zzz@yyy",
    // then this method returns "xxx.zzzyyy".
    public String getID()
    {
        return ID;
    }

    //------------------------------
    // abstract methods
    // These methods should be overridden by all subclasses

    public abstract void copyDataTo(Module oldModule);

    public abstract Object clone();

    public abstract String getXMLDescription();

    public abstract String getHTMLDescription();

} //end of class Module
