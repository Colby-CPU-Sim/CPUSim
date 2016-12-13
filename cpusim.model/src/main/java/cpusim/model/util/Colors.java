/**
 * 
 */
package cpusim.model.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Color utility functions
 * 
 * @author Kevin Brightwell
 * @since 2016-10-11
 */
public abstract class Colors {

	private Colors() {
		// noop
	}
	
	/**
     * generates two separate lists of random colors that have the same contents
     * @param length the desired length of the two lists
     * @return an ArrayList with that contains two separate ArrayLists of random light colors
     */
    public static List<List<String>> generateTwoListsOfRandomLightColors(int length){
        final Set<String> colors = Sets.newLinkedHashSetWithExpectedSize(length);
        
        while (colors.size() != length) {
        	colors.add(generateRandomLightHtmlColor());
        }
        
        final List<String> colorsList = Lists.newArrayList(colors);
        
        final List<List<String>> twoColorLists = Lists.newArrayList();
        twoColorLists.add(colorsList);
        twoColorLists.add(Lists.newArrayList(colorsList));
        
        return twoColorLists;
    }
    
    /**
     * Generates a hexadecimal string representing an RGB value that is a random light color
     * @return hexadecimal string representation of a random light color
     */
    public static String generateRandomLightHtmlColor(){
        final Color c = generateRandomLightColor();
        return String.format("rgb(%d,%d,%d)", (int)c.getRed(), (int)c.getGreen(), (int)c.getBlue());
    }

    /**
     * Generates a hexadecimal string representing an RGB value that is a random light color
     * @return hexadecimal string representation of a random light color
     */
    public static Color generateRandomLightColor(){
        final Random rand = new Random();

        return Color.color(rand.nextInt(127)+128,
                           rand.nextInt(127)+128,
                           rand.nextInt(127)+128);
    }

    /**
     * turns an array of Strings that represent colors into one string with each color
     * separated by a space
     * @param colors an ArrayList of string representations of colors
     * @return a single string that is all the strings in the array list separated by a space
     */
    public static String toXML(Iterable<? extends CharSequence> colors){
        return Joiner.on(' ').join(colors);
    }
    
    /**
     * takes an XML representation of a bunch of strings and converts them into an 
     * ArrayList of strings
     * @param xmlString one string of colors in xml form (one space between each string)
     * @return List of Strings that were separated by a space in the string
     */
    public static List<String> xmlToColorsList(CharSequence xmlString){
        return Splitter.on(' ').splitToList(xmlString);
    }
}
