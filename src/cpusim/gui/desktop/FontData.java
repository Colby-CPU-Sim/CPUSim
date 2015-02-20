package cpusim.gui.desktop;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * Just a class to hold all the data for the font and background color
 * so that it can be passed around to different regions
 */
public class FontData {
    public String font;
    public String fontSize;
    public String background;

    public FontData() {
        font = "Courier New";
        fontSize = "12";
        background = "#fff";
    }

    public void setFontAndBackground(Region region) {
        region.setBackground(new Background(new BackgroundFill(Color.web(
                background), null, null)));
        String optQuote = font.contains(" ") ? "\"" : "";
        region.setStyle("-fx-font-size:" + fontSize + "; "
                + "-fx-font-family:" + optQuote + font + optQuote);
    }
}
