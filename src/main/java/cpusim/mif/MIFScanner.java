/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cpusim.mif;

import java.util.ArrayList;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * This class converts strings into arrays of tokens
 * under the assumption that the strings consists of lines of text
 * from an MIF file.
 * @author Ben Borchard
 */
public class MIFScanner{
    
    private String[] mifText;
    private boolean inComment;
    /** the current line number */
    private int lineNumber;

    /**
     * constructor
     * @param text the array of Strings to be converted to an array of tokens.
     */
    public MIFScanner(String[] text){
        mifText = text;
        inComment = false;
        lineNumber = 0;
    }
    
    /**
     * gives an array list of the tokens on the next line of the mif text
     * @param comments true if you want to include the end-of-line comment as another token
     * @return array list of tokens or null if there are no more lines to scan.
     */
    public ArrayList<String> getNextTokens(boolean comments){
        ArrayList<String> tokens = new ArrayList<>();
        
        char[] line;
        
        try{
            line = mifText[lineNumber].toCharArray();
        }
        catch(ArrayIndexOutOfBoundsException e){
            return null;
        }
        
        lineNumber++;
        
        boolean dashPrevious = false;
        boolean periodPrevious = false;
        
        String token = "";
        int i = 0;
        for (char c : line){
            if (inComment){
                if (c == '%'){
                    inComment = false;
                }
            }
            else{
                if (c == ' '){
                    tokens.add(token);
                    token = "";
                    dashPrevious = false;
                    periodPrevious = false;
                }
                else if (c == '=' || c == ';' || c == ':' || c == '[' || c == ']'){
                    tokens.add(token);
                    token = "";
                    tokens.add(String.valueOf(c));
                    dashPrevious = false;
                    periodPrevious = false;
                }
                else if (c == '%'){
                    tokens.add(token);
                    token = "";
                    inComment = true;
                    dashPrevious = false;
                    periodPrevious = false;
                }
                else if (c == '.'){
                    if (periodPrevious){
                        token = token.substring(0, token.length()-1);
                        tokens.add(token);
                        tokens.add("..");
                        token = "";
                        periodPrevious = false;
                    }
                    else{
                        token += c;
                        periodPrevious = true;
                    }
                    dashPrevious = false;
                }
                else if (c == '-'){
                    if (dashPrevious){
                        token = token.substring(0, token.length()-1);
                        tokens.add(token);
                        if (comments){
                            if (i != line.length-1){
                                tokens.add(mifText[lineNumber-1].substring(i+1, 
                                        mifText[lineNumber-1].length()));
                            }
                        }
                        break;
                    }
                    else{
                        token += c;
                        dashPrevious = true;
                        periodPrevious = false;
                    }
                }
                else{
                    token += c;
                    dashPrevious = false;
                    periodPrevious = false;
                }
            }
            i++;
        }
        tokens.add(token);
        
        ArrayList<String> tokensToRemove = new ArrayList<>();
        for (String t : tokens){
            if (t.equals("")){
                tokensToRemove.add(t);
            }
        }
        
        for (String t : tokensToRemove){
            tokens.remove(t);
        }
        
        return tokens;
    }
    
    public int getLineNumber(){
        return lineNumber;
    }
}
