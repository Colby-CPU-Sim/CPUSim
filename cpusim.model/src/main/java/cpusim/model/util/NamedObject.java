package cpusim.model.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines that an object has a `name` and `ID` property.
 * @author Josh Ladieu
 * @since 2000-11-01
 */
public interface NamedObject extends Cloneable
{
	/**
     * Get the name of a component
     */
	@JsonProperty
    public String getName();
    
    /**
     * Set the name of a component
     */
    public void setName(String name);
    
    /**
     * Unique Identifier for a {@link NamedObject}
     * @return
     * 
     * @author Kevin Brightwell
     * @since 2016-09-20
     */
    @JsonProperty("id")
    public default String getID() {
	    final String s = this.toString();
	    final int index = s.indexOf('@');
	            
        if(index == -1) { 
            return s; 
        } else  {
            return s.substring(7, index) + s.substring(index + 1);
        }
    }
    
    /**
     * Converts a collection of {@link NamedObject} instances into a {@link Map} from {@link #getName()} to the values.
     * 
     * @param in non-<code>null</code> collection
     * @return Mapping between {@link #getName()} to value. 
     */
    public static <T extends NamedObject> Map<String, T> toNamedMap(final 	Iterable<? extends T> in) {
    	return StreamSupport.stream(checkNotNull(in).spliterator(), true)
    			.collect(Collectors.toMap(NamedObject::getName, Function.identity()));
    }
}