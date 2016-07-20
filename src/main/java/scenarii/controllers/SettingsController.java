package scenarii.controllers;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import scenarii.collections.CollectionUtils;
import scenarii.listeners.ShortcutListener;
import scenarii.listeners.SimpleShotListener;

import java.net.URL;
import java.util.HashSet;
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
    private ShortcutListener listener;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listener = new ShortcutListener();

        final TreeSet<String> combo = new TreeSet<>();
        Consumer<KeyEvent> handlerPressed = (e)->{
            combo.add(e.getCode().getName());
            String s = CollectionUtils.join(combo, "+");
            ((TextField) e.getTarget()).setText(s);
        };

        codeSnap.setOnKeyPressed(handlerPressed::accept);
        codeSnapAlt.setOnKeyPressed(handlerPressed::accept);

        codeSnap.setOnKeyReleased((e)->{
            HashSet<Integer> codes = listener.getCodes();
            SimpleShotListener listener = mainControllerRef.getSimpleShotListner();
            listener.setShortcut1(codes);
            combo.clear();
        });
        codeSnapAlt.setOnKeyReleased((e)->{
            HashSet<Integer> codes = listener.getCodes();
            SimpleShotListener listener = mainControllerRef.getSimpleShotListner();
            listener.setShortcut2(codes);
            combo.clear();
        });

         ChangeListener<Boolean> listenerToggleState = (observable, oldValue, newValue) -> {
            if (newValue)
                listener.bind();
            else
                listener.unbind();
        };

        codeSnap.focusedProperty().addListener(listenerToggleState);
        codeSnapAlt.focusedProperty().addListener(listenerToggleState);


        doneButton.setOnAction(event -> thisStage.hide());
    }

    void setMainControllerRef(MainController mainControllerRef) {
        this.mainControllerRef = mainControllerRef;
    }
    void setCorrespondingStage(Stage s){
        thisStage = s;
    }
}
