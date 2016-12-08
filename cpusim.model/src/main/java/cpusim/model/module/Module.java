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

package cpusim.model.module;


import cpusim.model.util.MachineComponent;
import cpusim.model.Machine;
import cpusim.model.util.*;
import cpusim.xml.HTMLEncodable;
import cpusim.xml.HtmlEncoder;
import javafx.beans.property.*;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;


///////////////////////////////////////////////////////////////////////////////
// the Module class

public abstract class Module<T extends Module<T>>
        implements IdentifiedObject,
                NamedObject,
                LegacyXMLSupported,
                HTMLEncodable,
                Validatable,
                MachineComponent,
                Copyable<T>
{
    private StringProperty name;	//name of the module
    private ReadOnlyObjectProperty<UUID> id; //unique ID used when saving in XML
    private final Machine machine; //machine that holds the module

    //------------------------------
    // constructor
    
    protected Module(String name, UUID id, Machine machine) {
        this.name = new SimpleStringProperty(this, "name", checkNotNull(name));
        this.id = new ReadOnlyObjectWrapper<>(this, "id", checkNotNull(id));

        this.machine = machine;
    }

    @Override
    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public ReadOnlyProperty<UUID> idProperty() {
        return id;
    }

    @Override
    public ReadOnlyObjectProperty<Machine> machineProperty() {
        return new ReadOnlyObjectWrapper<>(this, "machine", checkNotNull(machine));
    }

    public String getHTMLName()
    {
        return HtmlEncoder.sEncode(getName());
    }

    @Override
    public void validate() {
        NamedObject.super.validate();
    }

} //end of class Module
