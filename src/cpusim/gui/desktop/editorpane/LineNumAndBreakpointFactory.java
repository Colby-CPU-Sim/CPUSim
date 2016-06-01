/**
 * File: LineNumAndBreakpointFactory
 * User: djskrien
 * Date: 2/14/15
 */
package cpusim.gui.desktop.editorpane;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableSet;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
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
    private ObservableSet<Paragraph> breakPoints;
    /** set when program stops at a break point */
    private SimpleIntegerProperty currentBreakPointLineNumber;

    private static final String STYLESHEET = LineNumAndBreakpointFactory.class.getResource
            ("/cpusim/gui/css/LineNumbers.css").toExternalForm();
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
        this.nParagraphs = EventStreams.sizeOf(area.getParagraphs());
        this.area = area;
        this.format = new SimpleObjectProperty<>(format);
        this.stylesheet = stylesheet;
        this.breakPoints = FXCollections.observableSet();
        this.currentBreakPointLineNumber = new SimpleIntegerProperty(-1);
    }

    public void setFormat(IntFunction<String> format) {
        this.format.set(format);
    }

    public SimpleObjectProperty<IntFunction<String>> formatProperty() {
        return this.format;
    }

    public void setCurrentBreakPointLineNumber(int n) { this.currentBreakPointLineNumber.set(n); }

    public SimpleIntegerProperty currentBreakPointLineNumberProperty() {
        return this.currentBreakPointLineNumber;
    }

    /**
     * @return the set of Paragraphs with breakpoints set so that changes to the set can
     * be observed.
     */
    public ObservableSet<Paragraph> getBreakPoints() {
        return breakPoints;
    }

    /**
     * @return the Set of line numbers with break points so that corresponding RAM breakpoints
     * can be set when the code is loaded.
     */
    public Set<Integer> getAllBreakPointLineNumbers() {
        Set<Integer> breakPointLineNumbers = new HashSet<>();
        for(Paragraph p : breakPoints)
            breakPointLineNumbers.add(area.getParagraphs().indexOf(p));
        return breakPointLineNumbers;
    }

    @Override
    public Node apply(int idx) {
        Label label = new Label();
        Paragraph paragraph = area.getParagraphs().get(idx);
        boolean breakPoint = getAllBreakPointLineNumbers().contains(idx);
        Circle icon = new Circle(4, breakPoint ? Color.RED :
                        Color.web("#eee")); // same color as background and so invisible
        label.setGraphic(icon);
        label.getStyleClass().add("lineno");
        label.getStylesheets().add(stylesheet);

        // add a listener to the Label so that clicks in it cause the breakpoint
        // circle to toggle on and off
        label.setOnMouseClicked(event -> {
            Circle circle = (Circle) label.getGraphic();
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
            label.setText(formatTheLineNumber(idx + 1, area.getParagraphs().size()));
        });

        // When code stops due to a break point, change the background to orange
        // instead of light grey
        currentBreakPointLineNumber.addListener((observable,oldValue,newValue) -> {
            if ((int) newValue == idx) { // break at given line
                label.setBackground(new Background(new BackgroundFill(Color.ORANGE,
                        CornerRadii.EMPTY, Insets.EMPTY)));
            }
            else if ((int) oldValue == idx) { // resumed after breaking at given line
                label.setBackground(new Background(new BackgroundFill(Color.web("#eee"),
                        CornerRadii.EMPTY, Insets.EMPTY)));
            }
        });

        // when removed from the scene, be sure the paragraph is removed from
        // the set of breakpoints
        area.getParagraphs().addListener((ListChangeListener<Paragraph<?>>) c -> {
            if( ! area.getParagraphs().contains(paragraph))
                breakPoints.remove(paragraph);
        });

        // reformat the line numbers when the number of lines changes.
        // When removed from the scene, stay subscribed to never(), which is
        // a fake subscription that consumes no resources, instead of staying
        // subscribed to area's paragraphs.
        EventStreams.valuesOf(label.sceneProperty())
                .flatMap(scene -> scene != null
                        ? nParagraphs.map(n -> formatTheLineNumber(idx + 1, n))
                        : EventStreams.<String>never())
                .feedTo(label.textProperty());
        return label;
    }

    private String formatTheLineNumber(int x, int max) {
        int digits = (int) Math.floor(Math.log10(max)) + 1;
        return String.format(format.get().apply(digits), x);
    }

    public void setAllBreakPoints(Set<Integer> allBreakPointLineNumbers) {
        for (int idx : allBreakPointLineNumbers) {
            breakPoints.add(area.getParagraph(idx));
        }
    }
}
