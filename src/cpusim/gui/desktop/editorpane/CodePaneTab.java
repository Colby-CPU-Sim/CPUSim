package cpusim.gui.desktop.editorpane;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Tab;

import java.io.File;

/**
 * File: CodePaneTab
 * User: djskrien
 * Date: 2/8/15
 */
public class CodePaneTab extends Tab {
    /** the associated file containing the code (null if not yet saved to a file) */
    private SimpleObjectProperty<File> file;
    /** indicates whether the code area has been modified since last saved */
    private SimpleBooleanProperty dirty;

    public CodePaneTab() {
        this.file = new SimpleObjectProperty<>();
        this.dirty = new SimpleBooleanProperty(false);

        // if dirty is set, add an "*" in front of tab text
        this.dirty.addListener((observable, oldValue, newValue) -> {
            if (newValue)
                CodePaneTab.this.setText("*" + CodePaneTab.this.getText());
            else
                CodePaneTab.this.setText(CodePaneTab.this.getText().substring(1));
        });

        // if the associated file is changed, update the tab text and the dirty field
        this.file.addListener((observable, oldValue, newValue) -> {
            CodePaneTab.this.setDirty(false);  // in case it was dirty for the old file.
            CodePaneTab.this.setText(newValue.getName());
            CodePaneTab.this.setDirty(false);
        });
    }

    public boolean getDirty() {
        return dirty.get();
    }

    public SimpleBooleanProperty dirtyProperty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty.set(dirty);
    }

    public File getFile() {
        return file.get();
    }

    public void setFile(File file) {
        this.file.set(file);
    }

    public SimpleObjectProperty<File> fileProperty() {
        return file;
    }
}