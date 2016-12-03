package cpusim.model.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;

import java.util.UUID;

/**
 * Denotes an instance that has an {@link UUID} associated with it.
 * @since 2016-12-01
 */
public interface IdentifiedObject {
    /**
     * Generates a random {@link UUID} using {@link UUID#randomUUID()} and returns the result.
     * @return a random sequence of characters as per the {@link UUID#toString()} spec.
     */
    static UUID generateRandomID() {
        return UUID.randomUUID();
    }
    
    /**
     * Unique Identifier for a {@link NamedObject}
     * @return An identifier for the Object
     *
     * @since 2016-09-20
     */
    @JsonProperty("id")
    default UUID getID() {
        return idProperty().getValue();
    }

    /**
     * Property for the UUID of an object.
     * @return ObjectProperty allocated to the unique ID
     */
    ReadOnlyProperty<UUID> idProperty();
}
