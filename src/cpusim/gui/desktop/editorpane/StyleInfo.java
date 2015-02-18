/**
 * File: StyleInfo
 * Author: Thomas Mikula and Dale Skrien
 * Date: 2/11/15
 */
package cpusim.gui.desktop.editorpane;

import java.util.Optional;

/**
 * This class stores the bold, italic, and color data needed to appropriately draw
 * each Text section of the code areas.
 */
public class StyleInfo {
    public static final StyleInfo EMPTY = new StyleInfo();

    public final Optional<Boolean> bold;
    public final Optional<Boolean> italic;
    public final Optional<String> textColor; // of form #xxyyzz for web colors in hex

    public StyleInfo() {
        bold = Optional.empty();
        italic = Optional.empty();
        textColor = Optional.empty();
    }

    public StyleInfo(
            Optional<Boolean> bold,
            Optional<Boolean> italic,
            Optional<String> textColor) {
        this.bold = bold;
        this.italic = italic;
        this.textColor = textColor;
    }

    /**
     * converts it all to CSS
     * @return a String with the CSS form of the data
     */
    public String toCss() {
        StringBuilder sb = new StringBuilder();

        if (bold.isPresent()) {
            if (bold.get()) {
                sb.append("-fx-font-weight: bold;");
            }
            else {
                sb.append("-fx-font-weight: normal;");
            }
        }

        if (italic.isPresent()) {
            if (italic.get()) {
                sb.append("-fx-font-style: italic;");
            }
            else {
                sb.append("-fx-font-style: normal;");
            }
        }

        if (textColor.isPresent()) {
            String color = textColor.get();
            sb.append("-fx-fill: ").append(color).append(";");
        }
            // use the following lines instead of the preceding two
            // if I decide to store textColor as a Color instead of as a string
            // Color color = textColor.get()
            // int red = (int) (color.getRed() * 255);
            // int green = (int) (color.getGreen() * 255);
            // int blue = (int) (color.getBlue() * 255);
            //sb.append("-fx-fill: rgb(" + red + ", " + green + ", " + blue + ")");

        return sb.toString();
    }

    public StyleInfo updateWith(StyleInfo mixin) {
        return new StyleInfo(
                mixin.bold.isPresent() ? mixin.bold : this.bold,
                mixin.italic.isPresent() ? mixin.italic : this.italic,
                mixin.textColor.isPresent() ? mixin.textColor : this.textColor);
    }

    public StyleInfo updateBold(boolean bold) {
        return new StyleInfo(Optional.of(bold), italic, textColor);
    }

    public StyleInfo updateItalic(boolean italic) {
        return new StyleInfo(bold, Optional.of(italic), textColor);
    }

    public StyleInfo updateTextColor(String textColor) {
        return new StyleInfo(bold, italic, Optional.of(textColor));
    }
}
