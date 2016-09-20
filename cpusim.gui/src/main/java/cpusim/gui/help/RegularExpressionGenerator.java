/**
 * File: RegularExpressionGenerator
 * Authors: Scott Franchi, Pratap Luitel, Stephen Webel
 * Date: 12/3/13
 */

/**
 * File: DesktopController
 * Author: Pratap Luitel, Scott Franchi, Stephen Webel
 * Date: 12/6/13
 *
 * This File is new as of 12/6/13
 *
 * 
 * Fields Added:
 *      Added All
 *
 * Methods added:
 *      Added All
 *      
 *
 */
package cpusim.gui.help;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.property.SimpleStringProperty;



public class RegularExpressionGenerator {
    private List<String> occurrenceRegExList;
    private String searchString;
    private SimpleStringProperty searchMode;
    //This exists to replace characters that are recognized
    //in regular expression with their characterEntities
    //when in Normal mode. Due to DOM tree problems, this
    //works great for occurrence counting but is buggy when highlihgting
    public static final String[][] characterEntity = {  {"|","&verbar"},
                                                        {"[","&lsqb"},
                                                        {"]","&rsqb"},
                                                        {"(","&lpar"},
                                                        {")","&rpar"},
                                                        {"^","&Hat"},
                                                        {"{","&rcub"},
                                                        {"}","&lcub"},
                                                        {"'","&apos"},
                                                        {"?","&quest"},
                                                        {"=","&equals"},
                                                        {"%","percnt"},
                                                        {"#","&num"},
                                                        {"@","&commat"},
                                                        {"_","&lowbar"},
                                                        {":","&colon"},
                                                        {";","&semi"},
                                                        {"$","&dollar"},
                                                        {"!","&excl"},
                                                        {"*","&ast"},
                                                        {".","&period"},
                                                        {"+","&plus"},
                                                        {"\\","&bsol"},
                                                        {"/","&sol"},
                                                        {"<","&lt"},
                                                        {">","&gt"}};
    
    
    
    /**
     * Constructor for the class that takes a search string and a SimpleStringProperty
     * mode which tells the search mode.
     *
     * @param  srchString  the string to be searched
     * @param  mode          a SingleStringProperty that tells the selected
     *
     */
    public RegularExpressionGenerator(String srchString, SimpleStringProperty mode){
        occurrenceRegExList = new ArrayList<>();
        searchString = srchString;
        searchMode = mode;
    }

    
    /**
     * @return a list containing the regular expressions to be searched
     */
    public List<String> getOccurrencetRegEx(){
        return this.occurrenceRegExList;
    }
    
    /**
     * calls auxiliary methods to parse the search string based on
     * the current search mode. 
     * 
     * Sets occurenceRegExList to the result of the parse
     */
    public void searchRegExGenerator(){
        if(searchString.equals("")){
            occurrenceRegExList.clear();
            return;
        }
        switch(searchMode.get()){
           case "Normal":
                occurrenceRegExList = normalParse(searchString);
                removeEmptyMatchString();
                break;
            case "Regular Expressions":
                occurrenceRegExList = regExParse();
                removeEmptyMatchString();
                break;
            case "Whole Words":
                wholeWordParse();
                break;
            case "Match Case":
                matchCaseParse();
                break;
            case "Whole Words Match Case":
                wholeWordMatchCaseParse();
                break;
        }
        
    }
    
    /**
     * Strips all regular expressions that match the empty string
     *
     */
    private void removeEmptyMatchString(){
        //concurrent modification unavoidable
        for(int i=0;i<occurrenceRegExList.size();i++){
            Pattern p = Pattern.compile(occurrenceRegExList.get(i));
            Matcher m = p.matcher("");
            if(m.matches()){
                occurrenceRegExList.remove(occurrenceRegExList.get(i));
                i--;//to deal with concurrent modification
            }
        }
    }
    
    
//--------------------------Private Parser Methods---------------------------//
    
    /**
     * Handles the search case for Normal Parsing mode. Splits the string on AND first,
     * then on OR and generates corresponding regular expression. Syntax for case
     * insensitivity is added to the new expression/s.
     *
     * @return a List with regular expression/s
     *
     */
    private List<String> normalParse(String searchString){
        for(String[] key : characterEntity){
            if(searchString.contains(key[0])){
                while(searchString.contains(key[0])){
                    searchString = searchString.replace(key[0], key[1]);  
                }   
            }
        }
        //strip AND
        List<String> tempRegExList = new ArrayList<>(Arrays.asList(searchString.split(" AND ")));
        
        //Strip OR and add case insensitive
        for(String regEx : tempRegExList){
            int i = tempRegExList.indexOf(regEx);
            tempRegExList.set(i, "(?i)" + regEx.replaceAll(" OR ", "|"));
        }
        return tempRegExList;
    }
    
    
    /**
     * Handles the case when the string to be searched is in Regular Expression mode.
     * The regular expression for a search string is exactly the search string in this mode.
     *
     * @return a List with regular expressions
     *
     */
    private List<String> regExParse(){
        List<String> tempRegExList = new ArrayList<>();
        //Check that a regular expression is complete 
        try{
            Pattern p = Pattern.compile(searchString); //if fails, regEx is not complete
            tempRegExList.add(searchString);
        }
        catch(java.util.regex.PatternSyntaxException ex){
            //TODO: Highligh text red if an incomplete regex is entered
            System.out.println("Please Provide a full regular expression ");
        }
        return tempRegExList;

    }
    
    /**
     * Handles the case when the search string needs to match an exact word.
     * Search String parsed normally first(to split string on AND and OR).
     *
     *
     */
    private void wholeWordParse(){
        occurrenceRegExList = normalParse(searchString);
        removeEmptyMatchString();
        //slightly round about to add case insensitive whole word tags
        for(String regEx : occurrenceRegExList){
            int i = occurrenceRegExList.indexOf(regEx);
            
            occurrenceRegExList.set(i,"(?i)\\b" + occurrenceRegExList.get(i).substring(4) + "\\b");
        }
        

    }
    /**
     * Handles the case when the search is case sensitive.
     * Syntax for case insensitivity is added in normal parsing mode which
     * is removed here.
     *
     */
    private void matchCaseParse(){
        occurrenceRegExList = normalParse(searchString);
        removeEmptyMatchString();
        //remove case insensitivity
        for(String regEx : occurrenceRegExList){
            int i = occurrenceRegExList.indexOf(regEx);
            occurrenceRegExList.set(i, regEx.substring(4));
        }
        
    }
    
    /**
     * Handles the case when the search should match whole words
     * and be case sensitive. Regular Expression syntax added in normal mode for case
     * insensitivity removed first, then syntax for whole word added.
     *
     * @return a List with regular expression/s
     */
    private void wholeWordMatchCaseParse(){
        occurrenceRegExList = normalParse(searchString);
        removeEmptyMatchString();
        //add match case whole word tags
        for(String regEx : occurrenceRegExList){
            int i = occurrenceRegExList.indexOf(regEx);
            occurrenceRegExList.set(i,"\\b" + occurrenceRegExList.get(i).substring(4) + "\\b");
        }
    }
    
}
