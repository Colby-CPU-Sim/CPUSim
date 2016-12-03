/**
 * File: KeyCodeInfo
 * User: djskrien
 * Date: 2/23/15
 */
package cpusim.gui.desktop;

import cpusim.util.Convert;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

/**
 * This class is a tuple storing a keyboard shortcut as a String (as stored in Preferences)
 * and as a KeyCodeCombination object.
 * When the String is changed, the KeyCodeCombination is changed to match.
 */
public class KeyCodeInfo {
    private String keycodeString;
    private ObjectProperty<KeyCodeCombination> keycodeCombo;

    public KeyCodeInfo(String keycode) {
        keycodeString = keycode;
        keycodeCombo = new SimpleObjectProperty<>(convert(keycode));
    }

    private KeyCodeCombination convert(String keyBinding) {
        KeyCodeCombination.ModifierValue shift = KeyCodeCombination.ModifierValue.UP;
        KeyCodeCombination.ModifierValue ctrl = KeyCodeCombination.ModifierValue.UP;
        KeyCodeCombination.ModifierValue alt = KeyCodeCombination.ModifierValue.UP;
        KeyCodeCombination.ModifierValue meta = KeyCodeCombination.ModifierValue.UP;
        KeyCodeCombination.ModifierValue shortcut = KeyCodeCombination
                .ModifierValue.UP;
        String[] keys = keyBinding.split("-");
        KeyCode key = KeyCode.getKeyCode(keys[keys.length - 1]);
        if (key == null) {
            key = Convert.charToKeyCode(keys[keys.length - 1]);
        }
        keys[keys.length - 1] = null;
        if (keys.length > 1) {
            for (String mod : keys) {
                if (mod != null) {
                    switch (mod) {
                        case "Shift":
                            shift = KeyCodeCombination.ModifierValue.DOWN;
                            break;
                        case "Ctrl":
                            if (!System.getProperty("os.name").toLowerCase().contains("mac")) {
                                shortcut = KeyCodeCombination.ModifierValue.DOWN;
                            } else {
                                ctrl = KeyCodeCombination.ModifierValue.DOWN;
                            }
                            break;
                        case "Alt":
                            alt = KeyCodeCombination.ModifierValue.DOWN;
                            break;
                        case "Meta":
                            meta = KeyCodeCombination.ModifierValue.DOWN;
                            break;
                        case "Cmd":
                            shortcut = KeyCodeCombination.ModifierValue.DOWN;
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        if (key != null)
            return new KeyCodeCombination(key, shift, ctrl, alt, meta, shortcut);
        else
            return null;
    }

    public void bindToMenuItem(MenuItem menuItem) {
        String menuText = menuItem.getText();
        menuItem.acceleratorProperty().bind(keycodeCombo);
    }

    public void setKeyCode(String newKeyCode) {
        keycodeString = newKeyCode;
        keycodeCombo.set(convert(newKeyCode));
    }

    public String getKeyCode() {
        return keycodeString;
    }
}
