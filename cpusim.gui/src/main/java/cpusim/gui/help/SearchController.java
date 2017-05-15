/**
 * File: SearchController
 * Author: Scott Franchi, Pratap Luitel, Stephen Webel
 * Date: 11/12/13
 *
 * Date Modified: 12/6/2013
 *
 * Fields added:
 *  private SimpleStringProperty searchMode;
 *     private RegularExpressionGenerator generator;
 *     private List<String> occurrenceRegExList;
 *
 * Methods modified:
 *   public void addHighlightTagsToBody(org.w3c.dom.Document originalDoc)
 *      private int countOccurrences(BufferedReader fileIn, String occurrenceRegEx)
 *      private int countOccurrences(BufferedReader fileIn, String occurrenceRegEx)
 *
 * Methods added:
 *      public void initializeSearch(Document doc, String searchFeildText)
 *      public void setDocumentCopy(Document originalDoc)
 *      private addHighlightNodes(Document originalDoc, NodeList nodeList, String searchRegEx)
 *      private void nodeBuilder(Document originalDoc, Node node, String searchRegEx)
 *      private Node generateNewChildNode(Document originalDoc,
 *                                        String nodeText, int occurenceIndex, int occurenceEndIndex)
 *      public void buildOccurrenceMap()
 *      private String tagStripper(String fileText)
 *
 *
 */

package cpusim.gui.help;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.property.SimpleStringProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;



public class SearchController {
    private String[][] nameURLPairs;
    private Map<String, Integer> nameOccurrenceMap;
    private ArrayList<Node> copyNodeList;
    private String htmlHighlightColor;
    private List<String> occurrenceRegExList;
    private RegularExpressionGenerator generator;
    private SimpleStringProperty searchMode;
    
    
    /**
     * constructor method takes namedUrlPairs
     * 
     * @param namedUrlPairs contains file name and its url
     */
    public SearchController(String[][] namedUrlPairs, SimpleStringProperty mode){
        nameURLPairs = namedUrlPairs;
        nameOccurrenceMap = new HashMap<>();
        htmlHighlightColor = "background-color:yellow";
        occurrenceRegExList = new ArrayList();
        copyNodeList = new ArrayList();
        searchMode = mode;
    }
        
    /**
     * called from HelpController
     *
     * @param searchFieldText string to be searched
     * @param doc             Document generated from webEngine which is
     *                        based on the webView
     */
    public void initializeSearch(Document doc, String searchFieldText){
        generator = new RegularExpressionGenerator(searchFieldText, searchMode);
        generator.searchRegExGenerator();
        occurrenceRegExList = generator.getOccurrencetRegEx();
        //TODO change input
        addHighlightTagsToBody(doc);
        //Handle the search list and occurences
        buildOccurrenceMap();
    }
    
    /**
     * copies a Document(generated from webView here) by
     * recursively cloning the nodes of the document
     *
     * @param originalDoc the document to be copied
     *
     */
    public void setDocumentCopy(Document originalDoc){
        copyNodeList = new ArrayList<>();
        NodeList nl = originalDoc.getChildNodes();
        for(int i=0; i<nl.getLength(); i++){
            copyNodeList.add(nl.item(i).cloneNode(true));
        }
    }
    

    /**
     * returns a map with the file name(key) and the number of
     * occurrences of the searched word in the file(value)
     *
     * @return the map with the file name and the number
     *      of occurrences of the string we want to search
     */
    public Map<String,Integer> getSortedNameOccurrenceMap(){
        return nameOccurrenceMap;
    }



    /**
     * initializes nameOccurenceMap to a new map 
     */
    private void clearMap(){
        nameOccurrenceMap = new HashMap<>();
    }

    
    /** 
     * returns a html version of the body with codes to highlight the searched text
     * 
     * @param originalDoc   the document to which a string is to be searched and highlighted
     */
    public void addHighlightTagsToBody(org.w3c.dom.Document originalDoc) {

        htmlHighlightColor = "background-color:yellow";
        NodeList originalDocNodeList = originalDoc.getChildNodes();
        for(int i=0; i<copyNodeList.size(); i++){
            originalDoc.replaceChild(copyNodeList.get(i).cloneNode(true), originalDocNodeList.item(i));
        }
        String fileText = originalDoc.getLastChild().getTextContent();
        fileText = tagStripper(fileText);
        
        if(!isAnd(fileText)){ //file doesn't match all ANDed strings.
            return;
        }
        
        for(String searchRegEx : occurrenceRegExList){
            addHighlightNodes(originalDoc,originalDocNodeList,searchRegEx);
        }
    }
    
    /**
     * helper method for addHighlightTagsToBody
     *
     * @param originalDoc   a Document generated from webEngine based on webView
     * @param nodeList      a NodeList that has children nodes of original Doc
     * @param searchRegEx   a regular expression to be searched
     */
    private void addHighlightNodes(Document originalDoc, NodeList nodeList, String searchRegEx) {
        for(int i=0; i<nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            if(node.getNodeType() == Node.TEXT_NODE){
                nodeBuilder(originalDoc,node,searchRegEx);
            }else{
                addHighlightNodes(originalDoc,node.getChildNodes(),searchRegEx);
            }  
        }
    }
    /**
     * checks if the text node contains the search string to call further method to highlight
     *
     * @param originalDoc   a Document generated from webEngine based on webView
     * @param node          a node that contains text to be searched
     * @param searchRegEx   a regular expression to be searched
     */
    private void nodeBuilder(Document originalDoc, Node node, String searchRegEx) {
        String nodeText = node.getTextContent();
        
        Pattern p = Pattern.compile(searchRegEx);
        Matcher m = p.matcher(nodeText);

        if(m.find()){
            int occurenceStartIndex = m.start();
            int occurenceEndIndex = m.end();
            Node newChild = generateNewChildNode(originalDoc, nodeText,occurenceStartIndex,occurenceEndIndex);
 
            Node parentNode = node.getParentNode();
            parentNode.replaceChild(newChild, node);
            
            if(newChild.getLastChild().getNodeType() == Node.TEXT_NODE){
                nodeBuilder(originalDoc, newChild.getLastChild(), searchRegEx);
            }
        }
    }
    /**
     * called when a node contains the search string.Adds tags for highlighting the node.
     *
     * @param originalDoc      a Document generated from webEngine based on webView
     * @param nodeText         a string containing the text from the node which calls it
     * @param occurrenceIndex  an int specifying the starting index of the occurring search string
     * @param occurenceEndIndex  an int specifying the starting index of the occurring search string
     *
     * @return                 a node with the text containing highlight tags corresponding to the
     *                         given occurrenceIndex and occurrenceEndIndex
     */
    private Node generateNewChildNode(Document originalDoc, String nodeText, int occurrenceIndex, int occurenceEndIndex) {
        Element newChild = originalDoc.createElement("span");
        
        String priorText = nodeText.substring(0, occurrenceIndex);
        if(!priorText.equals("")){
            Text checkedText = originalDoc.createTextNode(priorText);
            newChild.appendChild(checkedText);
        }
        
        String highlightText = nodeText.substring(occurrenceIndex,occurenceEndIndex);
        Element highlightedText = originalDoc.createElement("span");
        highlightedText.setTextContent(highlightText);
        highlightedText.setAttribute("style",htmlHighlightColor);
        newChild.appendChild(highlightedText);
        
        
        String textToBeChecked = nodeText.substring(occurenceEndIndex);
        if(!textToBeChecked.equals("")){
            Text toBeCheckedText = originalDoc.createTextNode(textToBeChecked);
            newChild.appendChild(toBeCheckedText);
        }
        
        return newChild;
    }
    
    /**
     * Auxiliary method to check all regular expressions in the list 
     * to see if they have a match in the file text. 
     * 
     * @param fileText full text to be checked
     * @return a boolean if the given text should be occurrence counted and highlighted
     */
    private boolean isAnd(String fileText){
        boolean hasAll = true;
        Pattern p;
        Matcher m;
        for(String regEx : occurrenceRegExList){
            p = Pattern.compile(regEx);
            m = p.matcher(fileText);
            if(!m.find()){
                hasAll = false;
            }
        }
        return hasAll;
    }
    
    /**
     * Removes HTML tags from full text representations of the files.
     * 
     * used by the occurrence counter and highlighter 
     * 
     * @param fileText
     * @return 
     */
    private String tagStripper(String fileText){
        Pattern p = Pattern.compile("<[^>]*>");
        Matcher m = p.matcher(fileText);
        fileText = m.replaceAll("");  
                
        p = Pattern.compile("\\+");
        m = p.matcher(fileText);
        fileText = m.replaceAll(" ");
        return fileText;
    }
    
     /**
     * constructs a map with number of occurrence as key, and corresponding file url as value
     *
     */
    public void buildOccurrenceMap(){
        clearMap();
        String occurrenceRegEx = "";
        
        //count occurences as if OR'ed, skips counting later if file does not match AND
        for(String searchRegEx : occurrenceRegExList){
            occurrenceRegEx += "|" + searchRegEx;
        }
        
        //remove first | added in loop for conveniance
        if(!occurrenceRegEx.isEmpty()){
            occurrenceRegEx = occurrenceRegEx.substring(1);
        }
        
        for(String[] filePair : nameURLPairs){
            URL fileUrl = getClass().getResource(filePair[1]);
            BufferedReader in;
            Integer occurrences = 0;
            //forced catch block for file reading. will politely exit
            try {
                in = new BufferedReader(new InputStreamReader(fileUrl.openStream()));
                if(!occurrenceRegEx.isEmpty()){ //keeps empty map form occurence counting
                    occurrences = countOccurrences(in, occurrenceRegEx);
                }
                in.close();
            } catch (IOException ex) {
                System.out.println("IO Exception caught in Builing Maps, Please check files");
            }
            if(occurrences < 1){ //nothing there, nothing to do
                continue;
            }
            nameOccurrenceMap.put(occurrences.toString()+" "+filePair[0], occurrences);
        }
        if(!nameOccurrenceMap.isEmpty()){
            sortOccurrenceMap();
        }
    }
    
    /** 
     * Auxiliary method which returns the number of occurrences of a 
     * regular expression in the text of a Document. 
     * 
     * @param  doc 			the document where we search for the occurrence of a particular String
     * @param  searchString the string we will be searching in the doc
     * @return 				the number of occurrence of searchString in doc
     */
    private int countOccurrences(BufferedReader fileIn, String occurrenceRegEx) throws IOException {
        String lines;
        int count = 0;
        String fileText = "";
        while ((lines= fileIn.readLine()) != null){
            fileText += lines;
        }
        fileText = tagStripper(fileText);
        if(!isAnd(fileText)){ //file doesn't match all anded strings.
            return 0;
        }
        
        Pattern p = Pattern.compile(occurrenceRegEx);
        Matcher m = p.matcher(fileText);

        while(m.find()){
            count++;
        }
        return count;
    }
    
    /** 
     * sorts the occurrenceMap
     */
    private void sortOccurrenceMap(){
        SearchControllerComparator scc = new SearchControllerComparator(nameOccurrenceMap);
        TreeMap<String,Integer> tm = new TreeMap(scc);
        tm.putAll(nameOccurrenceMap);
        nameOccurrenceMap = tm;
    }


    /** 
     * inner class to compare the number of occurrence of 
     * the searched string in each file to sort the file.
     * Used by sortOccurrenceMap method. 
     */
    private static class SearchControllerComparator implements Comparator<String> {

        Map<String, Integer> base;
        /**
         * constructor method takes a map
         * 
         * @param base is a map with the file name and the number of occurrence 
         * 			   as key and value respectively
         */
        SearchControllerComparator(Map<String, Integer> base) {
            this.base = base;
        }
        
        /**
         * returns 1 if the second parameter(file) has higher occurrence of 
         * the searched string; returns 1 otherwise. 
         * 
         * @param a first file name Key to be compared
         * @param b second file name Key to be compared
         */
        @Override
        public int compare(String a, String b) {
            if (base.get(a) <= base.get(b)) {
                return 1;
            } else {
                return -1;
            }
        }
    }
    
}

