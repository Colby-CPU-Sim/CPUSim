package cpusim.gui.desktop.editorpane;

import cpusim.MachineInstruction;
import cpusim.Mediator;
import cpusim.assembler.PunctChar;
import cpusim.module.RAM;
import cpusim.module.RAMLocation;
import org.fxmisc.richtext.StyleSpans;
import org.fxmisc.richtext.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * File: CodePaneHelper
 * User: djskrien
 * Date: 2/8/15
 */
public class CodePaneController
{
    private Mediator mediator;

    /** the style info for the various parts of an assm language program */
    private StyleInfo defaultStyleInfo, instrStyleInfo, equStyleInfo, labelStyleInfo,
                      literalStyleInfo, dataStyleInfo, macroStyleInfo, symbolStyleInfo,
                      asciiStyleInfo, includeStyleInfo, stringStyleInfo, commentStyleInfo;

    public CodePaneController(Mediator m) {
        this.mediator = m;

        // the default styles for each part.
        this.defaultStyleInfo = new StyleInfo().updateFontSize(12).updateFontFamily
                ("monospace");
        this.instrStyleInfo = defaultStyleInfo.updateTextColor("#5a3").updateBold(true);
        this.equStyleInfo = defaultStyleInfo.updateTextColor("#404");
        this.labelStyleInfo = defaultStyleInfo.updateTextColor("#f00");
        this.literalStyleInfo = defaultStyleInfo.updateTextColor("orange");
        this.dataStyleInfo = defaultStyleInfo.updateTextColor("#044");
        this.macroStyleInfo = defaultStyleInfo.updateTextColor("#a04");
        this.symbolStyleInfo = defaultStyleInfo.updateTextColor("#000");
        this.asciiStyleInfo = defaultStyleInfo.updateTextColor("#4aa");
        this.includeStyleInfo = defaultStyleInfo.updateTextColor("#a44");
        this.stringStyleInfo = defaultStyleInfo.updateTextColor("#44a").updateItalic(true);
        this.commentStyleInfo = defaultStyleInfo.updateTextColor("#880").updateItalic(true);
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
        String wholeRegExpr = "(?<INSTR>" + instrNameRegExpr + ")"
                + "|(?<EQU>" + equRegExpr + ")"
                + "|(?<MACRO>" + macroRegExpr + ")"
                + "|(?<LABEL>" + labelRegExpr + ")"
                + "|(?<LITERAL>" + literalRegExpr + ")"
                + "|(?<SYMBOL>" + symbolsRegExpr + ")"
                + "|(?<DATA>" + dataRegExpr + ")"
                + "|(?<ASCII>" + asciiRegExpr + ")"
                + "|(?<INCLUDE>" + includeRegExpr + ")"
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
                matcher.group("INSTR") != null ? instrStyleInfo :
                matcher.group("EQU") != null ? equStyleInfo :
                matcher.group("MACRO") != null ? macroStyleInfo :
                matcher.group("LABEL") != null ? labelStyleInfo :
                matcher.group("SYMBOL") != null ? symbolStyleInfo :
                matcher.group("DATA") != null ? dataStyleInfo :
                matcher.group("LITERAL") != null ? literalStyleInfo :
                matcher.group("ASCII") != null ? asciiStyleInfo :
                matcher.group("INCLUDE") != null ? includeStyleInfo :
                matcher.group("STRING") != null ? stringStyleInfo :
                matcher.group("COMMENT") != null ? commentStyleInfo :
                null; /* never happens */
            assert styleInfo != null;
            spansBuilder.add(defaultStyleInfo, matcher.start() - lastKwEnd);
            spansBuilder.add(styleInfo, matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(defaultStyleInfo, text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    public static void clearAllBreakPointsInRam(RAM ram) {
        if (ram == null) {
            return;
        }
        for (RAMLocation rLoc : ram.data()) {
            rLoc.setBreak(false);
        }
    }

    /**
     * Gives an array of each line of a string.
     * Assumes there is no wrapping.
     *
     * @param s The string to split into lines.
     * @return An Array of Strings, which contains
     * the lines as they would appear on a text editor.
     */
    public static String[] getLines(String s) {
        int num = numberOfLinesInString(s);
        String[] sa = s.split("\n");
        if (sa.length != num) {
            String[] newSa = new String[num];
            for (int i = 0; i < sa.length; i++) {
                newSa[i] = sa[i];
            }
            newSa[num-1] = "";
            sa = newSa;
        }
        return sa;
    }

    /**
     * Given a string, this returns the number of lines
     * it would take a text editor to display the string.
     *
     * @param s - The string in question.
     * @return The number of lines it would take a text
     * editor to display that string.
     */
    public static int numberOfLinesInString(String s) {
        int numLines = 1;
        // System.getProperty("line.separator") doesn't work
        // on PCs, TextArea may only use "\n" char.
        final String LINE_SEPARATOR = "\n";
        while(!s.equals("")) {
            if(s.startsWith(LINE_SEPARATOR)) {
                numLines++;
            }
            s = s.substring(1);
        }
        return numLines;
    }

}
