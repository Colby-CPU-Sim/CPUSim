/**
 * File: LineNumAndBreakpointFactory
 * User: djskrien
 * Date: 2/14/15
 */
package cpusim.gui.desktop.editorpane;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
 * generates labels for the front of each line of code text areas that optionally
 * include line numbers and/or breakpoint indicators (red circles).
 */
public class LineNumAndBreakpointFactory implements IntFunction<Node>
{
    private final EventStream<Integer> nParagraphs;
    private final String stylesheet;
    private SimpleObjectProperty<IntFunction<String>> format;
    private StyledTextArea<?> area;
    private Set<Paragraph> breakPoints;

    private static final String STYLESHEET = LineNumAndBreakpointFactory.class.getResource
            ("../../css/LineNumbers.css").toExternalForm();
    private static final IntFunction<String> DEFAULT_FORMAT = (digits -> "%0" + digits + "d");

    /** factory methods for generating IntFunctions */
    public static IntFunction<Node> get(StyledTextArea<?> area, String customStylesheet) {
        return new LineNumAndBreakpointFactory(area, DEFAULT_FORMAT, customStylesheet);
    }

    public static IntFunction<Node> get(StyledTextArea<?> area) {
        return new LineNumAndBreakpointFactory(area, DEFAULT_FORMAT, STYLESHEET);
    }

    public static IntFunction<Node> get(StyledTextArea<?> area, IntFunction<String>
            format) {
        return new LineNumAndBreakpointFactory(area, format, STYLESHEET);
    }

    public static IntFunction<Node> get(StyledTextArea<?> area, IntFunction<String>
            format, String customStylesheet) {
        return new LineNumAndBreakpointFactory(area, format, customStylesheet);
    }

    /** Private constructor */
    private LineNumAndBreakpointFactory(StyledTextArea<?> area, IntFunction<String>
            format, String stylesheet) {
        nParagraphs = EventStreams.sizeOf(area.getParagraphs());
        this.area = area;
        this.format = new SimpleObjectProperty<>(format);
        this.stylesheet = stylesheet;
        this.breakPoints = new HashSet<>();
    }

    public void setFormat(IntFunction<String> format) {
        this.format.set(format);
    }

    public SimpleObjectProperty<IntFunction<String>> formatProperty() {
        return this.format;
    }

    public Set<Integer> getBreakPointLines() {
        Set<Integer> breakPointLineNumbers = new HashSet<>();
        for(Paragraph p : breakPoints)
            breakPointLineNumbers.add(area.getParagraphs().indexOf(p));
        return breakPointLineNumbers;
    }

    @Override
    public Node apply(int idx) {
        Label lineNo = new Label();
        Paragraph paragraph = area.getParagraphs().get(idx);
        Circle icon = new Circle(4, breakPoints.contains(paragraph) ? Color.RED :
                        Color.web("#eee")); // same color as background and so invisible
        lineNo.setGraphic(icon);
        lineNo.getStyleClass().add("lineno");
        lineNo.getStylesheets().add(stylesheet);

        // add a listener to the Label so that clicks in it cause the breakpoint
        // circle to toggle on and off
        lineNo.setOnMouseClicked(event -> {
            Circle circle = (Circle) lineNo.getGraphic();
            if (breakPoints.contains(paragraph)) {
                breakPoints.remove(paragraph);
                circle.setFill(Color.web("#eee"));
            }
            else {
                breakPoints.add(paragraph);
                circle.setFill(Color.RED);
            }
        });

        // When the format changes, for example when line numbers are shown or hidden,
        // redraw the label's text
        format.addListener((observable, oldValue, newValue) -> {
            lineNo.setText(formatTheLineNumber(idx + 1, area.getParagraphs().size()));
        });

        // when removed from the scene, be sure the paragraph is removed from
        // the set of breakpoints
        area.getParagraphs().addListener((ListChangeListener<Paragraph<?>>) c -> {
            if( ! area.getParagraphs().contains(paragraph))
                breakPoints.remove(paragraph);
        });

        // When removed from the scene, stay subscribed to never(), which is
        // a fake subscription that consumes no resources, instead of staying
        // subscribed to area's paragraphs.
        EventStreams.valuesOf(lineNo.sceneProperty())
                .flatMap(scene -> scene != null
                        ? nParagraphs.map(n -> formatTheLineNumber(idx + 1, n))
                        : EventStreams.<String>never())
                .feedTo(lineNo.textProperty());
        return lineNo;
    }

    private String formatTheLineNumber(int x, int max) {
        int digits = (int) Math.floor(Math.log10(max)) + 1;
        return String.format(format.get().apply(digits), x);
    }
}
