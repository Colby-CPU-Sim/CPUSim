package cpusim.model.util;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Created by kevin on 2016-12-01.
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
    UUID getID();
}
