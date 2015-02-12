package cpusim.gui.desktop.editorpane;

import javafx.scene.paint.Color;

import java.util.Optional;

/**
 * File: StyleInfo
 * Author: Thomas Mikula and Dale Skrien
 * Date: 2/11/15
 */
public class StyleInfo {
    public static final StyleInfo EMPTY = new StyleInfo();

    public static StyleInfo fontSize(int fontSize) {
        return EMPTY.updateFontSize(fontSize);
    }

    public static StyleInfo fontFamily(String family) {
        return EMPTY.updateFontFamily(family);
    }

    public static StyleInfo textColor(String color) {
        return EMPTY.updateTextColor(color);
    }

    final Optional<Boolean> bold;
    final Optional<Boolean> italic;
    final Optional<Integer> fontSize;
    final Optional<String> fontFamily;
    final Optional<String> textColor;

    public StyleInfo() {
        bold = Optional.empty();
        italic = Optional.empty();
        fontSize = Optional.empty();
        fontFamily = Optional.empty();
        textColor = Optional.empty();
    }

    public StyleInfo(
            Optional<Boolean> bold,
            Optional<Boolean> italic,
            Optional<Integer> fontSize,
            Optional<String> fontFamily,
            Optional<String> textColor) {
        this.bold = bold;
        this.italic = italic;
        this.fontSize = fontSize;
        this.fontFamily = fontFamily;
        this.textColor = textColor;
    }

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

            // use the following lines instead of the preceding two
            // if I decide to store textColor as a Color instead of as a string
            // Color color = textColor.get()
            // int red = (int) (color.getRed() * 255);
            // int green = (int) (color.getGreen() * 255);
            // int blue = (int) (color.getBlue() * 255);
            //sb.append("-fx-fill: rgb(" + red + ", " + green + ", " + blue + ")");
        }

        return sb.toString();
    }

    public StyleInfo updateWith(StyleInfo mixin) {
        return new StyleInfo(
                mixin.bold.isPresent() ? mixin.bold : bold,
                mixin.italic.isPresent() ? mixin.italic : italic,
                mixin.fontSize.isPresent() ? mixin.fontSize : fontSize,
                mixin.fontFamily.isPresent() ? mixin.fontFamily : fontFamily,
                mixin.textColor.isPresent() ? mixin.textColor : textColor);
    }

    public StyleInfo updateBold(boolean bold) {
        return new StyleInfo(Optional.of(bold), italic,
                fontSize, fontFamily, textColor);
    }

    public StyleInfo updateItalic(boolean italic) {
        return new StyleInfo(bold, Optional.of(italic),
                fontSize, fontFamily, textColor);
    }

    public StyleInfo updateFontSize(int fontSize) {
        return new StyleInfo(bold, italic, Optional.of
                (fontSize), fontFamily, textColor);
    }

    public StyleInfo updateFontFamily(String fontFamily) {
        return new StyleInfo(bold, italic, fontSize, Optional
                .of(fontFamily), textColor);
    }

    public StyleInfo updateTextColor(String textColor) {
        return new StyleInfo(bold, italic, fontSize, fontFamily, Optional.of(textColor));
    }
}
