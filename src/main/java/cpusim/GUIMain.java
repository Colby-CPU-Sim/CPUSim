/**
 * File: GUIMain
 * Last Update: August 2013
 */
package cpusim;

import java.io.File;
import java.io.IOException;
import java.util.List;
import cpusim.gui.desktop.DesktopController;

import java.net.URL;
import java.util.Optional;

import cpusim.gui.util.FXMLLoaderFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * A class to start the GUI. This is used every time 
 * the application is used unless it is run in command line mode.
 */
public class GUIMain extends Application {
	
	/**
	 * Starts the GUI.
	 */
	@Override
	public void start(Stage stage)
    {
		List<String> params = getParameters().getRaw();
		
        //netbeans has an issue with running CPUSim from the command line
        // so if you use netbeans, you may need to set the the variables
        // to empty strings instead of using params.get(0) and params.get(1).
        String machineFileName = params.get(0);
		String textFileName = params.get(1);

        // Set up the Scene with the Desktop and add it to the stage
		Mediator mediator = new Mediator(stage);
		DesktopController deskController = new DesktopController(mediator, stage);
		Pane mainPane = null;

		FXMLLoader fxmlLoader = FXMLLoaderFactory.fromController(deskController, "desktop.fxml");

        try {
            mainPane = fxmlLoader.load();
        } catch (IOException e) {
            // should never happen
           throw new IllegalStateException("Unable to load file: desktop.fxml", e);
        }
        Scene mainScene = new Scene(mainPane);
        stage.setScene(mainScene);

        // Load in the CPU Sim icon image
        URL iconURL = getClass().getResource("/images/icons/cpusim_icon.jpg");
        Image icon = new Image(iconURL.toExternalForm());
        stage.getIcons().add(icon);

        // Load the stylesheet for the line numbers in the CodePanes
        URL styleSheetURL = getClass().getResource("gui/css/LineNumbers.css");
        String stylesheet = styleSheetURL.toExternalForm();
        mainScene.getStylesheets().add(stylesheet);

        // load the machine, if provided
        if (machineFileName.equals("")) {
            mediator.newMachine(); // a new machine
        }
        else {
            mediator.openMachine(new File(machineFileName));
        }

        // Now load the text file, if provided
        if (textFileName.equals("")) {
            deskController.addDefaultTab();
        }
        else {
            deskController.open(new File(textFileName));
        }

        stage.show();
	}
}