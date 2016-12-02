package cpusim.model.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.*;

/**
 * Defines that an object has a `name` and `ID` property.
 * @since 2000-11-01
 */
public interface NamedObject extends Validatable {
	/**
     * Get the name of a component
     */
	@JsonProperty
    String getName();
    
    /**
     * Set the name of a component
     */
    void setName(String name);
    
    @Override
    default void validate() {
        NamedObject.validateName(getName());
    }
    
    /**
     * Converts a collection of {@link NamedObject} instances into a {@link Map} from {@link #getName()} to the values.
     * 
     * @param in non-<code>null</code> collection
     * @return Mapping between {@link #getName()} to value. 
     */
    static <T extends NamedObject> Map<String, T> toNamedMap(final Iterable<? extends T> in) {
    	return StreamSupport.stream(checkNotNull(in).spliterator(), true)
    			.collect(Collectors.toMap(NamedObject::getName, Function.identity()));
    }
    
    /**
     * returns a String that is different from all names of
     * existing objects in the given list.  It checks whether proposedName
     * is unique and if so, it returns it.  Otherwise, it
     * proposes a new name of proposedName + "copy" and tries again.
     *
     * @param list         list of existing objects
     * @param object The {@link NamedObject} being cloned.
     * @return the unique name
     */
    static String createUniqueDuplicatedName(ObservableList<? extends NamedObject> list,
                                             NamedObject object) {
        
        int i = 1;
        final String base = object.getName() + "_copy";
    
        final Set<String> allNames = list.stream().map(NamedObject::getName).collect(Collectors.toSet());
    
        // iterate until we get a unique name
        for (;;) {
            final String current = base + Integer.toString(i);
            
            if (!allNames.contains(current)) {
                return current;
            }
            
            ++i;
        }
    }
    
    /**
     * returns a String that is different from all names of
     * existing objects in the given list.  It checks whether proposedName
     * is unique and if so, it returns it.  Otherwise, it
     * proposes a new name of proposedName + "?" and tries again.
     *
     * @param list         list of existing objects
     * @param modChar Character used to make the name "unique", defaults to '?'
     * @return the unique name
     */
    static String createUniqueName(ObservableList<? extends NamedObject> list, int modChar) {
        final char[] chars = Character.toChars(modChar);
        return createUniqueName(list, new String(chars));
    }

    /**
     * returns a String that is different from all names of
     * existing objects in the given list.  It checks whether proposedName
     * is unique and if so, it returns it.  Otherwise, it
     * proposes a new name of proposedName + "?" and tries again.
     *
     * @param list         list of existing objects
     * @param mod Character used to make the name "unique", defaults to '?'
     * @return the unique name
     */
    static String createUniqueName(ObservableList<? extends NamedObject> list, final String mod) {

        final StringBuilder current = new StringBuilder(mod);
        final Set<String> allNames = list.stream().map(NamedObject::getName).collect(Collectors.toSet());

        // iterate until we get a unique name
        while (allNames.contains(current.toString())) {
            current.append(mod);
        }

        return current.toString();
    }
    
    /**
     * Delegates to {@link #createUniqueName(ObservableList, int)} with {@code '?'} as the default character.
     *
     * @param list         list of existing objects
     * @return the unique name
     *
     * @see #createUniqueName(ObservableList, int)
     */
    static String createUniqueName(ObservableList<? extends NamedObject> list) {
        return createUniqueName(list, '?');
    }
    
    /**
     * checks if all the names in the array of machine instrs are unique.
     * Will throw a classCastException if the array of objects passed in are not
     * instances of NamedObject
     * @param list array of namedObjects
     *
     * @since 2016-11-14
     */
    static void validateNamesUnique(List<? extends NamedObject> list)
    {
        final Set<String> names = Sets.newHashSetWithExpectedSize(list.size());
        for (NamedObject obj: list) {
            if (names.contains(obj.getName())) {
                throw new ValidationException("The name \"" + obj.getName() +
                        "\" is used more than once.\n" +
                        "All names must be unique.");
            }
            
            names.add(obj.getName());
        }
    }
    
    /**
     * checks if the name is a nonempty string
     * @param name string to check
     */
    static void validateName(String name) {
        if (Strings.isNullOrEmpty(name)) {
            throw new ValidationException("A name must have at least one character.");
        }
    
        if (name.trim().equals("<none>")) {
            throw new ValidationException("A ConditionBit has been given the " +
                    "name \"<none>\".\nThat name is reserved to indicate" +
                    " that no condition bit is desired.");
        }
    }
    
    /**
     * Validates the namedObjects whose names are not used in assembly code.
     * Will throw a classCastException if the array of objects passed in are not
     * instances of NamedObject
     * @param objects the nameable objects to be checked
     */
    static void validateUniqueAndNonempty(List<? extends NamedObject> objects) {
        validateNamesUnique(objects);
        objects.stream().map(NamedObject::getName).forEach(NamedObject::validateName);
    }
}