package cpusim.gui.util;

import cpusim.model.util.Copyable;
import cpusim.model.util.NamedObject;
import javafx.beans.NamedArg;

/**
 *
 * @since 2016-12-11
 */
public class DefaultControlButtonController<T extends NamedObject & Copyable<T>> extends ControlButtonController<T> {

    public DefaultControlButtonController(@NamedArg("prefIconOnly") boolean prefIconOnly) {
        super(false, prefIconOnly);

        loadFXML();
    }
}
