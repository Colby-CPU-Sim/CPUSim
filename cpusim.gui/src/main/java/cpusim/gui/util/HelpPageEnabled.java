package cpusim.gui.util;

/**
 * Defines an entity that has a Help Page associated with it.
 * @since 2016-11-30
 */
public interface HelpPageEnabled {

    /**
     * returns the help page ID for a component
     * @return a string of the ID
     */
    String getHelpPageID();
}
