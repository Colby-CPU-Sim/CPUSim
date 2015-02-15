/**
 * auther: Jinghui Yu
 * last edit date: 6/3/2013
 */

/**
 * File: PreferencesController
 * Authors: Joseph Harwood and Jake Epstein
 * Date: 11/15/13
 *
 * Added field clearConsoleOnRun as a CheckBox from an fxml source.
 * (Additionally added the separator and checkbox in Preferences.fxml)
 *
 * Edited method saveOtherTab to save the value of the clearConsoleOnRun CheckBox
 * to the DesktopController's OtherSettings object.
 *
 * Edited method setValues to load the CheckBox with the appropriate state as
 * signified by the DesktopController's OtherSettings object.
 */

package cpusim.gui.preferences;

import cpusim.Mediator;
import cpusim.gui.desktop.DesktopController;
import cpusim.gui.help.HelpController;
import cpusim.util.CPUSimConstants;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;

/**
 * The controller for the Preferences Dialog.
 */
public class PreferencesController implements Initializable {
	@FXML ComboBox<String> assemblyWindowFont;
	@FXML ChoiceBox<String> assemblyWindowSize;
	@FXML ChoiceBox<String> assemblyWindowStyle;
	@FXML ColorPicker assemblyForground;
	@FXML ColorPicker assemblyBackground;
	@FXML ComboBox<String> registerWindowFont;
	@FXML ChoiceBox<String> registerWindowSize;
	@FXML ChoiceBox<String> registerWindowStyle;
	@FXML ColorPicker registerForground;
	@FXML ChoiceBox registerBackground;
	@FXML ColorPicker registerBorder;

	@FXML CheckBox autoSave;
	@FXML CheckBox showLineNumbers;
	@FXML CheckBox lineWrap;
	@FXML CheckBox clearConsoleOnRun;

	@FXML Button applyButton;
	@FXML Button okayButton;
	@FXML Button closeButton;

	@FXML AnchorPane keyBindingsPane;
	@FXML AnchorPane menuItemsPane;

	@FXML ScrollPane kbScrollPane;

	@FXML BorderPane mainPane;

	@FXML TabPane tabPane;

	@FXML Tab keyBindingsTab;

	private final Mediator mediator;
	private final DesktopController desktopController;

	private ObservableList<String> keyBindings;

	private boolean listening;

	private Label currLabel;

	private String currBinding;


	public PreferencesController(Mediator mediator, DesktopController desktopController)
	{
		this.mediator = mediator;
		this.desktopController = desktopController;
	}

	/**
	 * initializes the dialog window after its root element has been processed.
	 * set the number of visible rows of combo boxes to be 8.
	 *
	 * @param url the location used to resolve relative paths for the root
	 *            object, or null if the location is not known.
	 * @param rb  the resources used to localize the root object, or null if the root
	 *            object was not localized.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		assemblyWindowFont.setVisibleRowCount(8);
		registerWindowFont.setVisibleRowCount(8);

		listening = false;

		//find the screen width
		double screenwidth = Screen.getPrimary().getBounds().getWidth();
		double screenheight = Screen.getPrimary().getBounds().getHeight();

		//fit main pane to the screen (roughly)
		if (mainPane.getPrefWidth() > screenwidth){
			mainPane.setPrefWidth(screenwidth-75);
		}

		if (mainPane.getPrefHeight() > screenheight){
			mainPane.setPrefHeight(screenheight-40);
		}

		kbScrollPane.setStyle("-fx-background-color:white;");

		tabPane.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>(){
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
				if (t.intValue() == 1 && listening){
					currLabel.setStyle("-fx-background-color:white;-fx-border-color:white;");
					keyBindings.set(keyBindingsPane.getChildren().indexOf(currLabel), currBinding);
					updateKeyBindingDisplay();
					stopListening();
				}
			}
		});

		initKeyBindings();

		updateKeyBindingDisplay();

		currLabel = (Label)keyBindingsPane.getChildren().get(0);

		setValues();
	}

	@FXML protected void onApplyButtonClick(ActionEvent e) {
		if (tabPane.getTabs().get(0).isSelected()) {
			saveFontTab();
		} else if (tabPane.getTabs().get(1).isSelected()) {
			saveKeyBindingsTab();
		} else {
			saveOtherTab();
		}
		desktopController.updateStyleOfTabs();
		desktopController.updateStyleOfTables();
	}

	/**
	 * save the changes after clicking the ok button.
	 *
	 * @param e a type of action when a button is clicked.
	 */
	@FXML protected void onOKButtonClick(ActionEvent e) {
		saveFontTab();
		saveKeyBindingsTab();
		saveOtherTab();

		desktopController.updateStyleOfTabs();
		desktopController.updateStyleOfTables();
		
		onCloseButtonClick(null);
	}

	private void saveFontTab() {
		DesktopController.FontData textFD = desktopController.getTextFontData();
		DesktopController.FontData tableFD = desktopController.getTableFontData();

		textFD.font = "\""+assemblyWindowFont.getValue()+"\"";
		textFD.fontSize = assemblyWindowSize.getValue();
		switch (assemblyWindowStyle.getValue()) {
		case "normal":
			textFD.bold = false;
			textFD.italic = false;
			break;
		case "bold":
			textFD.bold = true;
			textFD.italic = false;
			break;
		case "italic":
			textFD.bold = false;
			textFD.italic = true;
			break;
		default:
			textFD.bold = true;
			textFD.italic = true;
			break;
		}
		textFD.foreground = "#"+assemblyForground.getValue().toString().substring(2, 8);
		textFD.background = "#"+assemblyBackground.getValue().toString().substring(2, 8);

		tableFD.font = "\""+registerWindowFont.getValue()+"\"";
		tableFD.fontSize = registerWindowSize.getValue();
		switch (registerWindowStyle.getValue()) {
		case "normal":
			tableFD.bold = false;
			tableFD.italic = false;
			break;
		case "bold":
			tableFD.bold = true;
			tableFD.italic = false;
			break;
		case "italic":
			tableFD.bold = false;
			tableFD.italic = true;
			break;
		default:
			tableFD.bold = true;
			tableFD.italic = true;
			break;
		}

		if (!tableFD.background.equals(registerBackground.getValue().toString()) ){
                        CPUSimConstants.dialog.
                                owner((Stage) applyButton.getScene().getWindow()).
                                masthead("Table Color Change Warning").
                                message("The change of background color in register and RAM tables " +
                                        "will not be applied until you restart the application.").
                                showWarning();
		}

		tableFD.foreground = "#"+registerForground.getValue().toString().substring(2, 8);
		tableFD.background = registerBackground.getValue().toString();
		tableFD.border = "#"+registerBorder.getValue().toString().substring(2, 8);
	}

	private void saveKeyBindingsTab() {
		desktopController.setKeyBindings(keyBindings);
	}

	private void saveOtherTab() {
		desktopController.getOtherSettings().autoSave = this.autoSave.isSelected();
		desktopController.getOtherSettings().showLineNumbers.set(this.showLineNumbers.isSelected());
		desktopController.getOtherSettings().lineWrap.set(this.lineWrap.isSelected());
		desktopController.getOtherSettings().clearConsoleOnRun = this.clearConsoleOnRun.isSelected();
	}


	/**
	 * close the window without saving the changes.
	 *
	 * @param e  a type of action when a button is clicked.
	 */
	@FXML protected void onCloseButtonClick(ActionEvent e){
		//get a handle to the stage.
		Stage stage = (Stage) closeButton.getScene().getWindow();

		//reset values
		setValues();

		//close window.
		stage.close();
	}

	@FXML protected void handleDefault(ActionEvent e){
		keyBindings.clear();
		for (String kb : desktopController.DEFAULT_KEY_BINDINGS){
			keyBindings.add(kb);
		}
		updateKeyBindingDisplay();
	}

	@FXML protected void handleHelp(ActionEvent e){
		String startString = "Preferences Dialog";
		if (mediator.getDesktopController().getHelpController() == null) {
			HelpController helpController = HelpController.openHelpDialog(
					mediator.getDesktopController(), startString);
			mediator.getDesktopController().setHelpController(helpController);
		}
		else {
			HelpController hc = mediator.getDesktopController().getHelpController();
			hc.getStage().toFront();
			hc.selectTreeItem(startString);
		}
	}

	/**
	 * sets the initial values for the combo boxes based on what the values are
	 * in the desktopController
	 */
	private void setValues() {
		// Text
		DesktopController.FontData textFD = desktopController.getTextFontData();
		DesktopController.FontData tableFD = desktopController.getTableFontData();

		String newTextFont = textFD.font;
		if(newTextFont.contains("\"")){
			String[] text = newTextFont.split("\"");
			newTextFont = ""; 
			for (String part : text) {
				newTextFont += part;
			}
		}
		assemblyWindowFont.setValue(newTextFont);
		assemblyWindowSize.setValue(textFD.fontSize);

		if (textFD.bold && textFD.italic){
			assemblyWindowStyle.setValue("bold and italic");
		}
		else if(textFD.bold){
			assemblyWindowStyle.setValue("bold");
		}
		else if (textFD.italic){
			assemblyWindowStyle.setValue("italic");
		}
		else{
			assemblyWindowStyle.setValue("normal");
		}

		assemblyForground.setValue(Color.web(textFD.foreground));
		assemblyBackground.setValue(Color.web(textFD.background));

		// Table
		String newTableFont = tableFD.font;
		if(newTableFont.contains("\"")){
			String[] text = newTableFont.split("\"");
			newTableFont = "";
			for (String part : text){
				newTableFont += part;
			}
		}
		registerWindowFont.setValue(newTableFont);
		registerWindowSize.setValue(tableFD.fontSize);

		if (tableFD.bold && tableFD.italic){
			registerWindowStyle.setValue("bold and italic");
		}
		else if(tableFD.bold){
			registerWindowStyle.setValue("bold");
		}
		else if (tableFD.italic){
			registerWindowStyle.setValue("italic");
		}
		else{
			registerWindowStyle.setValue("normal");
		}

		registerForground.setValue(Color.web(tableFD.foreground));
		registerBackground.setValue(tableFD.background);
		registerBorder.setValue(Color.web(tableFD.border));

		autoSave.selectedProperty().set(desktopController.getOtherSettings().autoSave);
        showLineNumbers.selectedProperty().set(desktopController.getOtherSettings().showLineNumbers.get());
        lineWrap.selectedProperty().set(desktopController.getOtherSettings().lineWrap.get());
		clearConsoleOnRun.selectedProperty().set(desktopController.getOtherSettings().clearConsoleOnRun);
	}

	/**
	 * initializes the keyBindings variable based on what the key bindings according
	 * to the desktop controller
	 */
	private void initKeyBindings(){
		keyBindings = FXCollections.observableArrayList();

		ObservableList<String> realKeyBindings = desktopController.getKeyBindings();

		for (String binding : realKeyBindings){
			keyBindings.add(binding);
		}
	}

	/**
	 * updates the display of the key bindings on the left side of the key bindings
	 * tab
	 */
	private void updateKeyBindingDisplay(){
		keyBindingsPane.getChildren().clear();
		int currY = 2;
		ContextMenu contextMenu = new ContextMenu();
		MenuItem setToDefault = new MenuItem("setToDefault");
		MenuItem setToNoKeyBinding = new MenuItem("setToNoKeyBinding");
		for (String binding : keyBindings){

			final Label kbLabel = new Label(binding);
			kbLabel.setLayoutY(currY);
			kbLabel.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>(){
				public void handle(ContextMenuEvent t){
					currLabel = kbLabel;
				}
			});
			kbLabel.setOnMouseClicked(new EventHandler<MouseEvent>(){
				@Override
				public void handle(MouseEvent t) {
					if (t.getButton() == MouseButton.PRIMARY){
						currLabel.setStyle("-fx-background-color:white;-fx-border-color:white;");
						kbLabel.setStyle("-fx-background-color:lightblue;-fx-border-style:solid;"
								+ "-fx-border-color:black;");
						currBinding = kbLabel.getText();
						currLabel = kbLabel;
						listenForKeyEvents();
					}
				}
			});
			setToDefault.setOnAction(new EventHandler<ActionEvent>(){
				public void handle(ActionEvent t){
					String kbString = desktopController.DEFAULT_KEY_BINDINGS[keyBindingsPane.getChildren().indexOf(currLabel)];
					boolean conflict = false;
					for (Node label : keyBindingsPane.getChildren()){
						if (((Label)label).getText().equals(kbString)
								&& !((Label)label).equals(currLabel)){
							conflict = true;
							String conflictMenuItem = ((Label)menuItemsPane.getChildren().get(
									keyBindingsPane.getChildren().indexOf(label))).getText();
                                                        Action response = CPUSimConstants.dialog.
                                                                owner(((Stage)keyBindingsPane.getScene().getWindow())).
                                                                masthead("Key Binding Conflict").
                                                                message("The key binding of the menu item '"+conflictMenuItem
									+"' already has the key binding '"+kbString+"'.  Would you"
									+ "like to reassign the key binding of '"+
									conflictMenuItem+"'?").
                                                                showConfirm();
							if (response == Dialog.ACTION_YES){
								keyBindings.set(keyBindingsPane.getChildren().indexOf(label), "          ");
								currBinding = ((Label)label).getText();
								currLabel.setText(kbString);
								currLabel.setStyle("-fx-background-color:white;-fx-border-color:white;");
								keyBindings.set(keyBindingsPane.getChildren().indexOf(currLabel), kbString);
								currLabel = (Label)label;
								currLabel.setText("         ");
								currLabel.setStyle("-fx-background-color:lightblue;-fx-border-style:solid;"
										+ "-fx-border-color:black;");
								listenForKeyEvents();

							}
							else if (response == Dialog.ACTION_CANCEL){
								return;
							}
                                                        else /* response == Actions.NO */{
								keyBindings.set(keyBindingsPane.getChildren().indexOf(label), "          ");
								currLabel.setText(kbString);
								keyBindings.set(keyBindingsPane.getChildren().indexOf(currLabel), kbString);
								currLabel = (Label)label;
								currLabel.setText("         ");
							}
							break;
						}
					}
					if (!conflict){
						currLabel.setText(kbString);
						keyBindings.set(keyBindingsPane.getChildren().indexOf(currLabel), kbString);
					}
				}
			});
			setToNoKeyBinding.setOnAction(new EventHandler<ActionEvent>(){
				public void handle(ActionEvent t){
					currLabel.setText("         ");
					keyBindings.set(keyBindingsPane.getChildren().indexOf(currLabel), "       ");
					stopListening();
					currLabel.setStyle("-fx-background-color:white;-fx-border-color:white;");
				}
			});
			contextMenu.getItems().clear();
			contextMenu.getItems().addAll(setToDefault, setToNoKeyBinding);
			kbLabel.setContextMenu(contextMenu);
			keyBindingsPane.getChildren().add(kbLabel);
			currY += 16;
		}
	}

	/**
	 * sets a key listener to active so that you the user can properly set the key
	 * binding to whatever key they would like
	 */
	private void listenForKeyEvents(){
		listening = true;

		keyBindingsPane.getScene().setOnKeyPressed(new EventHandler<KeyEvent>(){
			@Override
			public void handle(KeyEvent t) {
				//System.out.println(t.getCode());

				if (t.getCode().isModifierKey()){
					return;
				}

				String keyString = String.valueOf(t.getCode());
				if (keyString.indexOf("DIGIT") != -1){
					keyString = keyString.replaceAll("DIGIT", "");
				}



				if (keyString.length() > 1){

					String[] words = keyString.split("_");
					keyString = "";
					for (String word : words){
						keyString += word.substring(0,1)+word.substring(1).toLowerCase()+"_";
					}
					keyString = keyString.substring(0, keyString.length()-1);
				}



				String kbString;
				if(t.isShiftDown() && t.isAltDown() && t.isControlDown()){
					if (System.getProperty("os.name").startsWith("Windows")){
						kbString = desktopController.SHORTCUT+"-Shift-Alt-"+keyString;
					}
					else{
						kbString = desktopController.SHORTCUT+"-Shift-Alt-Ctrl-"+keyString;
					}
				}
				else if(t.isAltDown() && t.isControlDown()){
					if (System.getProperty("os.name").startsWith("Windows")){
						kbString = desktopController.SHORTCUT+"-Alt-"+keyString;
					}
					else{
						kbString = desktopController.SHORTCUT+"-Alt-Ctrl-"+keyString;
					}
				}
				else if(t.isShiftDown() && t.isControlDown()){
					if (System.getProperty("os.name").startsWith("Windows")){
						kbString = desktopController.SHORTCUT+"-Shift-"+keyString;
					}
					else{
						kbString = desktopController.SHORTCUT+"-Shift-Ctrl-"+keyString;
					}
				}
				else if(t.isControlDown()){
					if (System.getProperty("os.name").startsWith("Windows")){
						kbString = desktopController.SHORTCUT+"-"+keyString;
					}
					else{
						kbString = desktopController.SHORTCUT+"-Shift-Ctrl-"+keyString;
					}
				}
				else if(t.isShiftDown() && t.isAltDown()){
					kbString = desktopController.SHORTCUT+"-Shift-Alt-"+keyString;
				}
				else if (t.isShiftDown()){
					kbString = desktopController.SHORTCUT+"-Shift-"+keyString;
				}
				else if (t.isAltDown()){
					kbString = desktopController.SHORTCUT+"-Alt-"+keyString;
				}
				else{
					kbString = desktopController.SHORTCUT+"-"+keyString;
				}            
				boolean stillListening = false;
				int conflictIndex = 0;
				for (Node label : keyBindingsPane.getChildren()){
					if (((Label)label).getText().equals(kbString)
							&& !((Label)label).equals(currLabel)){

						String conflictMenuItem = ((Label)menuItemsPane.getChildren().get(
								keyBindingsPane.getChildren().indexOf(label))).getText();
                                                Action response = CPUSimConstants.dialog.
                                                        owner(((Stage)keyBindingsPane.getScene().getWindow())).
                                                        masthead("Key Binding Conflict").
                                                        message("The key binding of the menu item '"+conflictMenuItem
								+"' already has the key binding '"+kbString+"'.  Would you"
								+ "like to reassign the key binding of '"+
								conflictMenuItem+"'?").
                                                        showConfirm();

						if (response == Dialog.ACTION_YES){
							keyBindings.set(keyBindingsPane.getChildren().indexOf(label), "          ");
							currBinding = ((Label)label).getText();
							stillListening = true;

						}
						else if (response == Dialog.ACTION_CANCEL){
							return;
						}
						else{
							keyBindings.set(keyBindingsPane.getChildren().indexOf(label), "          ");

						}
						break;
					}
					conflictIndex++;
				}

				//don't let the user assign key bindings that cannot be changed
				if (kbString.equals("Ctrl-X") || kbString.equals("Ctrl-V") || kbString.equals("Ctrl-A")
						|| kbString.equals("Ctrl-C") || kbString.equals("Ctrl-Z") || kbString.equals("Ctrl-X")
						|| kbString.equals("Ctrl-Shift-Z") || kbString.equals("Cmd-X") 
						|| kbString.equals("Cmd-V") || kbString.equals("Cmd-A")
						|| kbString.equals("Cmd-C") || kbString.equals("Cmd-Z") || kbString.equals("Cmd-X")
						|| kbString.equals("Cmd-Shift-Z")){
                                        CPUSimConstants.dialog.
                                                owner((Stage)keyBindingsPane.getScene().getWindow()).
                                                masthead("Reserved Key Binding").
                                                message("The key binding "+kbString+" cannot be assigned"
									+ " to a menu item").
                                                showError();
					return;

				}



				keyBindings.set(keyBindingsPane.getChildren().indexOf(currLabel), kbString);
				updateKeyBindingDisplay();
				stopListening();
				if (stillListening){
					currLabel = (Label)keyBindingsPane.getChildren().get(conflictIndex);
					currLabel.setStyle("-fx-background-color:lightblue;-fx-border-style:solid;"
							+ "-fx-border-color:black;");
					listenForKeyEvents();
				}


			}
		});
	}

	/**
	 * sets the key event listener to inactive
	 */
	private void stopListening(){
		listening = false;
		keyBindingsPane.getScene().setOnKeyPressed(null);
	}

}
