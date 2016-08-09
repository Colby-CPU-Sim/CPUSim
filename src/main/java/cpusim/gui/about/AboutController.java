package cpusim.gui.about;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class AboutController implements Initializable {

    @FXML private ImageView imageView;
    @FXML private WebView webView;
    @FXML private Button okButton;

    public AboutController() {}

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        // Load in image
        URL url = getClass().getResource("/images/icons/cpusim_logo.jpg");
        imageView.setImage(new Image(url.toExternalForm()));

        // Set up Text in Web View
        WebEngine webEngine = webView.getEngine();
        url = getClass().getResource("/html/aboutCPUSim.html");
        webEngine.load(url.toExternalForm());
        webView.setZoom(javafx.stage.Screen.getPrimary().getDpi() / 96);
    }

    @FXML
    public void onOkButtonClicked() {
        ((Stage) (okButton.getScene().getWindow())).close();
    }

}