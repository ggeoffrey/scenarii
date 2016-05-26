package scenarii.controllers;

import javafx.event.EventHandler;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.text.Text;

/**
 * Created by geoffrey on 25/05/2016.
 */
public class ClipBoardActionsHandler implements EventHandler<KeyEvent>{
    private final static KeyCodeCombination copy = new KeyCodeCombination(
            KeyCode.C,
            KeyCombination.CONTROL_DOWN
    );

    private final static KeyCodeCombination copyMac = new KeyCodeCombination(
            KeyCode.C,
            KeyCombination.META_DOWN
    );

    private final static KeyCodeCombination past = new KeyCodeCombination(
            KeyCode.V,
            KeyCombination.CONTROL_DOWN
    );

    private final static KeyCodeCombination pastMac = new KeyCodeCombination(
            KeyCode.V,
            KeyCombination.META_DOWN
    );


    @Override
    public void handle(KeyEvent event) {
        if(event.getSource() instanceof TextInputControl){
            if(copy.match(event) || copyMac.match(event)){
                ClipboardContent content = new ClipboardContent();
                String text = ((TextInputControl) event.getSource()).getText();
                content.putString(text);
                Clipboard.getSystemClipboard().setContent(content);
                event.consume();
            }
            else if(past.match(event) || pastMac.match(event)){
                String toPast = Clipboard.getSystemClipboard().getString();
                TextInputControl control = (TextInputControl) event.getSource();

                int caretPosition = control.getCaretPosition();
                if(caretPosition<0)
                    caretPosition = 0;

                control.insertText(caretPosition, toPast);
                event.consume();
            }
        }

    }
}
