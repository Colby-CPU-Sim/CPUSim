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


import cpusim.model.util.Copyable;
import cpusim.model.util.LegacyXMLSupported;
import cpusim.model.util.NamedObject;
import cpusim.model.util.Validatable;
import cpusim.xml.HTMLEncodable;
import cpusim.xml.HtmlEncoder;
import javafx.beans.property.SimpleStringProperty;

import java.util.Comparator;

import static com.google.common.base.Preconditions.checkNotNull;


///////////////////////////////////////////////////////////////////////////////
// the Module class

public abstract class Module<T extends Module<T>>
        implements NamedObject, LegacyXMLSupported, HTMLEncodable, Copyable<T>, Validatable
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
    
    /**
     * Validate the internal state of a subclass.
     */
    protected abstract void validateState();
    
    @Override
    public final void validate() {
        NamedObject.super.validate();
        
        validateState();
    }

} //end of class Module
