package cpusim.gui.desktop.editorpane;

import java.util.Optional;

/**
 * File: StyleInfo
 * Author: Thomas Mikula and Dale Skrien
 * Date: 2/11/15
 */
public class StyleInfo {
    public static final StyleInfo EMPTY = new StyleInfo();

    /**
     * return a new StyleInfo that is empty except for the given font size
     * @param fontSize the font size
     * @return the StyleInfo with the font size
     */
    public static StyleInfo fontSize(int fontSize) {
        return EMPTY.updateFontSize(fontSize);
    }

    public static StyleInfo fontFamily(String family) {
        return EMPTY.updateFontFamily(family);
    }

    public static StyleInfo textColor(String color) {
        return EMPTY.updateTextColor(color);
    }

    public final Optional<Boolean> bold;
    public final Optional<Boolean> italic;
    public final Optional<Integer> fontSize;
    public final Optional<String> fontFamily;
    public final Optional<String> textColor; // of form #xxyyzz for web colors in hex
    public final Optional<String> backgroundColor; // of form #xxyyzz for web colors in hex

    public StyleInfo() {
        bold = Optional.empty();
        italic = Optional.empty();
        fontSize = Optional.empty();
        fontFamily = Optional.empty();
        textColor = Optional.empty();
        backgroundColor = Optional.empty();
    }

    public StyleInfo(
            Optional<Boolean> bold,
            Optional<Boolean> italic,
            Optional<Integer> fontSize,
            Optional<String> fontFamily,
            Optional<String> textColor,
            Optional<String> backgroundColor) {
        this.bold = bold;
        this.italic = italic;
        this.fontSize = fontSize;
        this.fontFamily = fontFamily;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
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

        if (fontSize.isPresent()) {
            sb.append("-fx-font-size: " + fontSize.get() + "pt;");
        }

        if (fontFamily.isPresent()) {
            sb.append("-fx-font-family: " + fontFamily.get() + ";");
        }

        if (textColor.isPresent()) {
            String color = textColor.get();
            sb.append("-fx-fill: " + color + ";");
        }

        if (backgroundColor.isPresent()) {
            String color = backgroundColor.get();
            sb.append("-fx-background-color: " + color + ";");
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
                mixin.fontSize.isPresent() ? mixin.fontSize : this.fontSize,
                mixin.fontFamily.isPresent() ? mixin.fontFamily : this.fontFamily,
                mixin.textColor.isPresent() ? mixin.textColor : this.textColor,
                mixin.backgroundColor.isPresent() ? mixin.backgroundColor : this.backgroundColor);
    }

    public StyleInfo updateBold(boolean bold) {
        return new StyleInfo(Optional.of(bold), italic,
                fontSize, fontFamily, textColor, backgroundColor);
    }

    public StyleInfo updateItalic(boolean italic) {
        return new StyleInfo(bold, Optional.of(italic),
                fontSize, fontFamily, textColor, backgroundColor);
    }

    public StyleInfo updateFontSize(int fontSize) {
        return new StyleInfo(bold, italic, Optional.of
                (fontSize), fontFamily, textColor, backgroundColor);
    }

    public StyleInfo updateFontFamily(String fontFamily) {
        return new StyleInfo(bold, italic, fontSize, Optional
                .of(fontFamily), textColor, backgroundColor);
    }

    public StyleInfo updateTextColor(String textColor) {
        return new StyleInfo(bold, italic, fontSize, fontFamily, Optional.of(textColor),
                backgroundColor);
    }

    public StyleInfo updateBackgroundColor(String backgroundColor) {
        return new StyleInfo(bold, italic, fontSize, fontFamily, textColor,
                Optional.of(backgroundColor));
    }
}
