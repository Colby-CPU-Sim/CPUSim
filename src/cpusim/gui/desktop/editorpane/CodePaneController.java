package cpusim.gui.desktop.editorpane;

import cpusim.MachineInstruction;
import cpusim.Mediator;
import cpusim.assembler.PunctChar;
import cpusim.module.RAM;
import cpusim.module.RAMLocation;
import cpusim.util.SourceLine;
import org.fxmisc.richtext.StyleSpans;
import org.fxmisc.richtext.StyleSpansBuilder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * File: CodePaneController
 * User: djskrien
 * Date: 2/8/15
 */
public class CodePaneController
{
    private Mediator mediator;

    /** the style info for the various parts of an assm language program */
    private Map<String,StyleInfo> styles;

    public CodePaneController(Mediator m) {
        this.mediator = m;
        this.styles = new HashMap<>();

        // the default styles for each part.
        StyleInfo base = new StyleInfo().
                updateBold(false).
                updateItalic(false).
                updateTextColor("#000");
        this.styles.put("default",base);
        this.styles.put("instr", base.updateTextColor("#5a3").updateBold(true));
        this.styles.put("keyword", base.updateTextColor("#00b"));
        this.styles.put("label", base.updateTextColor("#c00"));
        this.styles.put("literal", base.updateTextColor("#f80"));
        this.styles.put("symbol", base.updateTextColor("#000"));
        this.styles.put("string", base.updateTextColor("#44a").updateItalic(true));
        this.styles.put("comment", base.updateTextColor("#880").updateItalic(true));
    }

    /**
     * generate the regular expression pattern for color-coding the program in the CodeArea.
     * Note: The pattern varies depending on the machine being simulated, so this pattern is
     * regenerated every time computeHighlighting() is called.  If this turns out to be
     * too slow, we can compute it once every time a machine is loaded and again every
     * time a machine instruction or assembly punctuation changes.
     * @return  the Pattern for the CodeArea to use when highlighting its contents.
     */
    private Pattern computePatternForMachine() {
        // get the regExpr for the instruction names
        List<MachineInstruction> instrs = mediator.getMachine().getInstructions();
        String[] instrNames = instrs.stream().map(MachineInstruction::getName).toArray
                                                                          (String[]::new);
        String instrNameRegExpr = "\\b(" + String.join("|", instrNames) + ")\\b";

        // get the regExpr for keywords: .data, .ascii, .include, equ, MACRO, ENDM
        String dataRegExpr = "\\.data";
        String asciiRegExpr = "\\.ascii";
        String includeRegExpr = "\\.include";
        String equRegExpr = "equ";
        String macroRegExpr = "MACRO|ENDM";
        String keywordRegExpr = dataRegExpr+"|"+asciiRegExpr+"|"+includeRegExpr+"|"+
                equRegExpr+"|"+macroRegExpr;

        // get the regExpr for "tokens" (special symbols consisting of exactly one char)
        PunctChar[] punctChars = mediator.getMachine().getPunctChars();
        String tokensRegExpr = "";
        for(PunctChar chr: punctChars)
           if(chr.getUse()==PunctChar.Use.token)
               tokensRegExpr += "\\" + chr.getChar() + "|";
        // want something like this: "\\!|\\#|\\$|\\%|\\&|\\^|\\_|\\`|\\*|\\?|\\@|\\~"
        // so remove the last "|";
        tokensRegExpr = tokensRegExpr.substring(0,tokensRegExpr.length()-1);

        // get the regExpr for symbols of one or more of a set of characters
        String individualSymbolsRegExpr = "[";
        for(PunctChar chr: punctChars)
            if(chr.getUse()==PunctChar.Use.symbol)
                individualSymbolsRegExpr += "\\" + chr.getChar();
        individualSymbolsRegExpr += "]";
        String symbolsRegExpr = "(" + individualSymbolsRegExpr + "|[a-zA-Z])" +
                                "(" + individualSymbolsRegExpr + "|\\+|\\-|[a-zA-Z0-9])*";

        // get the regExpr for labels, strings, comments, and literals
        char labelChar = mediator.getMachine().getLabelChar();
        String labelRegExpr = symbolsRegExpr + "\\" + labelChar;
        char commentChar = mediator.getMachine().getCommentChar();
        String commentRegExpr = "\\" + commentChar + "[^\n]*\n?";
        String stringRegExpr = "\".*?\"";
        String decimalLiteralRegExpr = "(\\+|\\-)?[0-9]+";
        String charLiteralRegExpr = "\\'.\\'";
        String binaryIntLiteralRegExpr = "(\\+|\\-)?0b[0-1]+";
        String hexIntLiteralRegExpr = "(\\+|\\-)?0x[0-9a-fA-F]+";
        String literalRegExpr = binaryIntLiteralRegExpr + "|" +
                                hexIntLiteralRegExpr + "|" +
                                charLiteralRegExpr + "|" +
                                decimalLiteralRegExpr;

        // combine them all and compile it
        String wholeRegExpr =
                   "(?<KEYWORD>" + keywordRegExpr + ")"
                + "|(?<LABEL>" + labelRegExpr + ")"
                + "|(?<INSTR>" + instrNameRegExpr + ")"
                + "|(?<LITERAL>" + literalRegExpr + ")"
                + "|(?<SYMBOL>" + symbolsRegExpr + ")"
                + "|(?<STRING>" + stringRegExpr + ")"
                + "|(?<COMMENT>" + commentRegExpr + ")";
        return Pattern.compile(wholeRegExpr);
    }

    public StyleSpans<StyleInfo> computeHighlighting(String text) {
        Pattern codePattern = computePatternForMachine();
        Matcher matcher = codePattern.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<StyleInfo> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {
            StyleInfo styleInfo =
                matcher.group("INSTR") != null ? styles.get("instr") :
                matcher.group("KEYWORD") != null ? styles.get("keyword") :
                matcher.group("LABEL") != null ? styles.get("label") :
                matcher.group("SYMBOL") != null ? styles.get("symbol") :
                matcher.group("LITERAL") != null ? styles.get("literal") :
                matcher.group("STRING") != null ? styles.get("string") :
                matcher.group("COMMENT") != null ? styles.get("comment") :
                null; /* never happens */
            assert styleInfo != null;
            spansBuilder.add(styles.get("default"), matcher.start() - lastKwEnd);
            spansBuilder.add(styleInfo, matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(styles.get("default"), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    /**
     * returns the StyleInfo for the given group
     * @param group one of the strings "instr", "keyword", "label", "symbol", "literal",
     *              "string", "comment"
     * @return its StyleInfo or null if the group is not one of the specified strings
     */
    public StyleInfo getStyleInfo(String group) {
        return styles.get(group);
    }

    /**
     * sets the style info for the given group.
     * @param group one of the strings "instr", "keyword", "label", "symbol", "literal",
     *              "string", "comment"
     * @param style the new StyleInfo to be assigned to the group
     */
    public void setStyleInfo(String group, StyleInfo style) {
        styles.put(group, style);
    }
}
