package scenarii.controllers;

import javafx.application.Platform;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.SwingDispatchService;
import scenarii.camera.Camera;
import scenarii.dirtycallbacks.Callback1;
import scenarii.dirtycallbacks.EmptyCallback;
import scenarii.exporters.FileUtils;
import scenarii.exporters.HtmlExporter;
import scenarii.exporters.Scenario;
import scenarii.importers.HtmlImporter;
import scenarii.overlay.Overlay;

import java.io.File;
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
    private Button open;

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

        BooleanProperty validNames = new SimpleBooleanProperty();

        BooleanBinding binding = title.textProperty()
                .isEmpty()
                .or(author.textProperty().isEmpty())
                .or(validNames);

        title.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                String text = title.getText();
                if(!FileUtils.isFilenameValid(text)){
                    validNames.setValue(true);
                    title.setStyle("-fx-text-fill: #ff4b41;");
                }
                else{
                    validNames.setValue(false);
                    title.setStyle("-fx-text-fill: white;");
                }

            }
        });


        export.disableProperty().bind(binding);
        exportTo.disableProperty().bind(binding);
        compress.disableProperty().bind(binding);

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


        open.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Import…");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML file","*.html"));
                File toImport = fileChooser.showOpenDialog(root.getScene().getWindow());
                Scenario sc = HtmlImporter.load(toImport);

                title.setText(sc.getTitle());
                author.setText(sc.getAuthor());
                description.setText(sc.getAuthor());
                data.setText(sc.getData());

                steps.clear();
                stepsContainer.getChildren().clear();
                for(Step s : sc.getSteps()){
                    addStep(s);
                }
            }
        });


        export.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                exportToHtml();
            }
        });


        ClipBoardActionsHandler clipBoardListener = new ClipBoardActionsHandler();
        title.setOnKeyPressed(clipBoardListener);
        author.setOnKeyPressed(clipBoardListener);
        description.setOnKeyPressed(clipBoardListener);
        data.setOnKeyPressed(clipBoardListener);
    }

    private void reindex(){
        int i = 1;
        for (Step s : steps){
            s.setPosition(i);
            i++;
        }
    }

    private void addStep(){
        addStep(new Step(steps.size()+1));
    }

    private void addStep(Step s){
        steps.add(s);
        stepsContainer.getChildren().add(s.getBody());
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                scrollPane.setVvalue(1.0d);
            }
        });

        s.onMoveUpRequest(new Callback1<Integer>() {
            @Override
            public void call(Integer position) {
                if(position > 1){
                    Step target = steps.get(position-1);
                    stepsContainer.getChildren().remove(position-1);
                    stepsContainer.getChildren().add(position-2,target.getBody());
                    steps.remove(position-1);
                    steps.add(position-2,target);
                    reindex();
                }
            }
        });

        s.onMoveDownRequest(new Callback1<Integer>() {
            @Override
            public void call(Integer position) {
                if(position < steps.size()){
                    Step target = steps.get(position-1);
                    stepsContainer.getChildren().remove(position-1);
                    stepsContainer.getChildren().add(position,target.getBody());
                    steps.remove(position-1);
                    steps.add(position,target);
                    reindex();
                }
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

                        try {
                            GlobalScreen.removeNativeKeyListener(listener);
                            GlobalScreen.removeNativeMouseMotionListener(listener);
                            GlobalScreen.unregisterNativeHook();
                        }
                        catch (NativeHookException e){
                            e.printStackTrace();
                        }
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
