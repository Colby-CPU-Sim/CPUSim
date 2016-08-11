package cpusim.gui.desktop;

import javafx.scene.control.TableCell;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import org.fxmisc.richtext.StyledTextArea;

/**
 * A class to hold all the data for the font and background color
 * so that it can be passed around to different regions
 */
public class FontData {
    public String font;
    public String fontSize;
    public String background;
    public String selection;

    public FontData() {
        //default values
        font = "Courier New";
        fontSize = "12";
        background = "#fff";
        selection = "lightgray";
    }

    public void setFontAndBackground(TableCell region) {
        if(region.getTableView().getSelectionModel().isSelected(region.getIndex())) {
            //if the row is selected, set the background to the selection color
            region.setBackground(new Background(new BackgroundFill(Color.web(
                                selection), null, null)));
        }
        else {
            region.setBackground(new Background(new BackgroundFill(Color.web(
                                background), null, null)));
        }
        String optQuote = font.contains(" ") ? "\"" : "";
        region.setStyle("-fx-font-size:" + fontSize + "; "
                + "-fx-font-family:" + optQuote + font + optQuote);
    }

    public void setFontAndBackground(StyledTextArea region) {
        region.setBackground(new Background(new BackgroundFill(Color.web(
                    background), null, null)));
        String optQuote = font.contains(" ") ? "\"" : "";
        region.setStyle("-fx-font-size:" + fontSize + "; "
                + "-fx-font-family:" + optQuote + font + optQuote + ";"
                + "-fx-highlight-fill:" + selection + ";");
    }
}
