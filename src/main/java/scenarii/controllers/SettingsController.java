package scenarii.controllers;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import scenarii.collections.CollectionUtils;
import scenarii.listeners.ShortcutListener;

import java.awt.event.KeyAdapter;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.function.Consumer;

/**
 * Created by geoffrey on 18/07/2016.
 */
public class SettingsController implements Initializable {

    private MainController mainControllerRef;

    @FXML private BorderPane root;
    @FXML private Button doneButton;
    @FXML private TextField codeSnap;
    @FXML private TextField codeSnapAlt;


    private Stage thisStage;

    private boolean listening;
    private ShortcutListener listener;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listening = false;
        listener = new ShortcutListener();

        final TreeSet<String> combo = new TreeSet<>();
        Consumer<KeyEvent> handlerPressed = (e)->{
            combo.add(e.getCode().getName());
            String s = CollectionUtils.join(combo, "+");
            ((TextField) e.getTarget()).setText(s);
        };

        codeSnap.setOnKeyPressed(handlerPressed::accept);
        codeSnapAlt.setOnKeyPressed(handlerPressed::accept);

        Consumer<KeyEvent> handlerReleased = (e)->{
            String codes = CollectionUtils.join(listener.getCodes(),"-");
            System.out.println(CollectionUtils.join(combo,"+"));
            System.out.println("|" + codes + "|");
            combo.clear();
        };

        codeSnap.setOnKeyReleased(handlerReleased::accept);
        codeSnapAlt.setOnKeyReleased(handlerReleased::accept);

        codeSnap.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                listener.bind();
            }
            else{
                listener.unbind();
            }
        });
    }

    void setMainControllerRef(MainController mainControllerRef) {
        this.mainControllerRef = mainControllerRef;
    }
    void setCorrespondingStage(Stage s){
        thisStage = s;
    }
}
