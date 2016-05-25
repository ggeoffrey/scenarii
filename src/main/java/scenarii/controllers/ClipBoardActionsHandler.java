package scenarii.controllers;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * Created by geoffrey on 25/05/2016.
 */
public class ClipBoardActionsHandler extends EventHandler<KeyEvent>{
    private final static KeyCodeCombination copy = new KeyCodeCombination(
            KeyCode.C,
            KeyCombination.CONTROL_ANY
    );

    private final static KeyCodeCombination past = new KeyCodeCombination(
            KeyCode.V,
            KeyCombination.CONTROL_ANY
    );

}
