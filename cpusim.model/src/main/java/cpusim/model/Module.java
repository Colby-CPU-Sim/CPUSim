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
import cpusim.model.util.IdentifiedObject;
import cpusim.model.util.LegacyXMLSupported;
import cpusim.model.util.NamedObject;
import cpusim.model.util.Validatable;
import cpusim.xml.HTMLEncodable;
import cpusim.xml.HtmlEncoder;
import javafx.beans.property.*;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;


///////////////////////////////////////////////////////////////////////////////
// the Module class

public abstract class Module<T extends Module<T>>
        implements IdentifiedObject, NamedObject, LegacyXMLSupported, HTMLEncodable, Copyable<T>, Validatable
{
    private SimpleStringProperty name;	//name of the module
    private ObjectProperty<UUID> id; //unique ID used when saving in XML
    protected Machine machine; //machine that holds the module

    //------------------------------
    // constructor
    
    protected Module(String name, UUID id, Machine machine) {
        this.name = new SimpleStringProperty(name);
        this.id = new SimpleObjectProperty<>(checkNotNull(id));

        this.machine = machine;
    }

    //------------------------------
    //getters and setters to make it a Bean

    @Override
    public void setName(String newName)
    {
    	name.set(newName);
    }

    @Override
    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public ReadOnlyProperty<UUID> idProperty() {
        return id;
    }

    public String getHTMLName()
    {
        return HtmlEncoder.sEncode(getName());
    }

    public String toString()
    {
        return name.get();
    }
    
    @Override
    public void validate() {
        NamedObject.super.validate();
    }

} //end of class Module
