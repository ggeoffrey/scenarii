package scenarii.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.SwingDispatchService;
import scenarii.camera.Camera;
import scenarii.dirtycallbacks.Callback1;
import scenarii.dirtycallbacks.EmptyCallback;
import scenarii.exporters.HtmlExporter;
import scenarii.exporters.Scenario;
import scenarii.overlay.Overlay;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by geoffrey on 24/05/2016.
 */
public class MainController implements Initializable {

    @FXML
    private BorderPane root;

    @FXML
    private Button export;

    @FXML
    private Button exportTo;

    @FXML
    private Button compress;

    @FXML
    private TextField title;

    @FXML
    private TextField author;

    @FXML
    private TextArea description;

    @FXML
    private TextArea data;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox stepsContainer;

    @FXML
    private Button addStep;

    private ArrayList<Step> steps;


    private Overlay overlay;
    private Camera camera;

    private NativeEventListener listener;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        steps = new ArrayList<>();
        addStep();
        addStep.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                addStep();
            }
        });

        overlay = new Overlay();
        camera = new Camera(overlay);
        listener = new NativeEventListener(overlay, camera);

        export.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                exportToHtml();
            }
        });

    }

    private void reindex(){
        int i = 1;
        for (Step s : steps){
            s.setPosition(i);
            i++;
        }
    }

    private void addStep(){
        Step s = new Step(steps.size()+1);
        steps.add(s);
        stepsContainer.getChildren().add(s.getBody());
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                scrollPane.setVvalue(1.0d);
            }
        });

        s.onShotRequest(new EventHandler() {
            @Override
            public void handle(Event event) {
                overlay.show();
                overlay.showForDistort();
                listener.setState(State.RESIZING);
                try {
                    GlobalScreen.registerNativeHook();
                    GlobalScreen.setEventDispatcher(new SwingDispatchService());
                    GlobalScreen.addNativeMouseMotionListener(listener);
                    GlobalScreen.addNativeKeyListener(listener);
                } catch (NativeHookException e) {
                    e.printStackTrace();
                }

                listener.onGifGenerated(new Callback1<String>() {
                    @Override
                    public void call(String arg0) {
                        s.setImage(arg0);
                    }
                });

            }
        });

        s.onDeleteRequest(new EmptyCallback() {
            @Override
            public void call() {
                reindex();
                steps.remove(s);
                stepsContainer.getChildren().remove(s.getPosition()-1);
                reindex();
            }
        });
    }


    private void exportToHtml(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Scenario scenario = new Scenario(
                        title.getText(),
                        author.getText(),
                        description.getText(),
                        data.getText(),
                        steps
                );

                HtmlExporter exporter = new HtmlExporter(System.getProperty("user.home")+"/scenarii");
                exporter.export(scenario);
            }
        }).start();
    }
}
