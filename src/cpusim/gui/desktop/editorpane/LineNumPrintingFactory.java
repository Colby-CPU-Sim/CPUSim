/**
 * File: LinNumPrintingFactory
 * User: djskrien
 * Date: 2/22/15
 */
package cpusim.gui.desktop.editorpane;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.fxmisc.richtext.Paragraph;
import org.fxmisc.richtext.StyledTextArea;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;

import java.util.HashSet;
import java.util.Set;
import java.util.function.IntFunction;

/**
 * This class is for displaying line numbers when printing a file only.
 */
public class LineNumPrintingFactory implements IntFunction<Node>
{
    private final String stylesheet;
    private SimpleObjectProperty<IntFunction<String>> format;
    private StyledTextArea<?> area;
    private int startingIndex;
    private int numDigits;

    private static final String STYLESHEET = LineNumAndBreakpointFactory.class.getResource
            ("../../css/LineNumbers.css").toExternalForm();

    /** factory method for generating the IntFunction */
    public static IntFunction<Node> get(StyledTextArea<?> area, int startingIndex, int
            totalNumLines, IntFunction<String> format)
    {
        return new LineNumPrintingFactory(area, startingIndex, totalNumLines, format,
                STYLESHEET);
    }

    /** Private constructor */
    private LineNumPrintingFactory(StyledTextArea<?> area, int startingIndex,
                      int totalNumLines, IntFunction<String> format, String stylesheet) {
        this.area = area;
        this.format = new SimpleObjectProperty<>(format);
        this.stylesheet = stylesheet;
        this.startingIndex = startingIndex;
        this.numDigits = (int) Math.floor(Math.log10(totalNumLines)) + 1;
    }

    public void setFormat(IntFunction<String> format) {
        this.format.set(format);
    }

    public SimpleObjectProperty<IntFunction<String>> formatProperty() {
        return this.format;
    }

    @Override
    public Node apply(int idx) {
        Label lineNo = new Label();
        lineNo.getStyleClass().add("lineno");
        lineNo.getStylesheets().add(stylesheet);
        lineNo.setText(formatTheLineNumber(idx + 1 + startingIndex));
        return lineNo;
    }

    private String formatTheLineNumber(int x) {
        return String.format(format.get().apply(numDigits), x);
    }
}