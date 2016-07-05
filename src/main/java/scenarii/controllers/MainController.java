package scenarii.controllers;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.zeroturnaround.zip.ZipUtil;

import scenarii.camera.Camera;
import scenarii.dirtycallbacks.Callback1;
import scenarii.dirtycallbacks.EmptyCallback;
import scenarii.exporters.FileUtils;
import scenarii.exporters.HtmlExporter;
import scenarii.helpers.PopupHelper;
import scenarii.model.Scenario;
import scenarii.importers.HtmlImporter;
import scenarii.model.Step;
import scenarii.overlay.Overlay;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.zip.Deflater;

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
    private Button batchRecord;

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

    @FXML
    private ProgressIndicator progress;


    private Stage primaryStage;


    private ArrayList<Step> steps;


    private Overlay overlay;
    private Camera camera;

    private File targetFolder;

    private NativeEventListener listener;


    private BooleanProperty invalidName;
    private BooleanProperty targetFolderPresent;
    private BooleanBinding exportAvailable;
    private BooleanBinding canExportOrCompress;

    private PopupHelper helper;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        root.getStylesheets().add("/res/main.css");

        invalidName = new SimpleBooleanProperty(false);
        targetFolderPresent = new SimpleBooleanProperty(false);

        exportAvailable = title.textProperty()
                .isEqualTo("")
                .or(author.textProperty().isEqualTo(""))
                .or(invalidName);

        canExportOrCompress = exportAvailable.or(targetFolderPresent.not());

        title.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                String text = title.getText();
                if (!FileUtils.isFilenameValid(text)) {
                    invalidName.setValue(true);
                    title.setStyle("-fx-text-fill: #ff4b41;");
                } else {
                    invalidName.setValue(false);
                    title.setStyle("-fx-text-fill: white;");
                }

            }
        });


        export.disableProperty().bind(canExportOrCompress);
        exportTo.disableProperty().bind(exportAvailable);
        compress.disableProperty().bind(canExportOrCompress);

        steps = new ArrayList<Step>();
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
                retreivePrimaryStage();
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Importer...");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML file", "*.html"));
                File toImport = fileChooser.showOpenDialog(primaryStage);
                Scenario sc = HtmlImporter.load(toImport);

                title.setText(sc.getTitle());
                author.setText(sc.getAuthor());
                description.setText(sc.getDescription());
                data.setText(sc.getData());

                steps.clear();
                stepsContainer.getChildren().clear();
                for (Step s : sc.getSteps()) {
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

        exportTo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                retreivePrimaryStage();
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("Export to folder...");
                File folder = directoryChooser.showDialog(primaryStage);

                if (folder != null) {
                    if (!folder.exists()) {
                        folder.mkdir();
                    }
                    targetFolder = folder;
                    targetFolderPresent.setValue(true);
                    exportToHtml();
                }
            }
        });

        compress.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                compress();
            }
        });


        batchRecord.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                primaryStage.toBack();
                listener.batchRecord(new Callback1<ArrayList<Step>>() {
                    @Override
                    public void call(ArrayList<Step> steps) {
                        listener.unbind();
                        primaryStage.toFront();
                        for (Step s : steps){
                            addStep(s);
                        }
                        reindex();
                    }
                });
            }
        });


        ClipBoardActionsHandler clipBoardListener = new ClipBoardActionsHandler();
        title.setOnKeyPressed(clipBoardListener);
        author.setOnKeyPressed(clipBoardListener);
        description.setOnKeyPressed(clipBoardListener);
        data.setOnKeyPressed(clipBoardListener);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                retreivePrimaryStage();
            }
        });

        helper = PopupHelper.get();
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

    private void addStep(final Step s){
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

                helper.display();
                retreivePrimaryStage();
            	primaryStage.toBack();

                listener.initShot();

                listener.setOnCancel(new EmptyCallback() {
                    @Override
                    public void call() {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                helper.hide();
                            }
                        });
                    }
                });

                listener.onGifGenerated(new Callback1<String>() {
                    @Override
                    public void call(String arg0) {
                        s.setImage(arg0);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                primaryStage.toFront();
                                helper.hide();
                            }
                        });

                        listener.unbind();
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


    private void compress(){
        if(canExportOrCompress.not().get()){
            progress.setProgress(-1.);
            invalidName.setValue(true);

            String path = targetFolder+"/"+title.getText();
            ZipUtil.pack(new File(path), new File(path+".zip"), Deflater.BEST_COMPRESSION);

            progress.setProgress(1.);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    invalidName.setValue(false);
                    canExportOrCompress.invalidate();
                }
            });

        }
    }

    private void exportToHtml(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(targetFolder != null && targetFolder.exists()){

                    Scenario scenario = new Scenario(
                            title.getText(),
                            author.getText(),
                            description.getText(),
                            data.getText(),
                            steps
                    );
                    System.out.println(targetFolder.getAbsolutePath());
                    HtmlExporter exporter = new HtmlExporter(targetFolder.getAbsolutePath());

                    invalidName.setValue(true);

                    exporter.export(scenario, progress); // <++++++++

                    invalidName.setValue(false);
                }
            }
        }).start();
    }

    private Stage retreivePrimaryStage(){
        primaryStage = root.getScene() != null
                ? (Stage) root.getScene().getWindow()
                : null;
        return primaryStage;
    }
}
