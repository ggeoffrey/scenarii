package scenarii.controllers;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.zeroturnaround.zip.ZipUtil;
import scenarii.camera.Camera;
import scenarii.collections.ObservableArrayList;
import scenarii.dirtycallbacks.Callback1;
import scenarii.exporters.FileUtils;
import scenarii.exporters.HtmlExporter;
import scenarii.helpers.PopupHelper;
import scenarii.importers.HtmlImporter;
import scenarii.listeners.NativeEventListener;
import scenarii.listeners.RecordingListener;
import scenarii.listeners.SimpleShotListener;
import scenarii.model.Scenario;
import scenarii.model.Step;
import scenarii.overlay.Overlay;

import java.io.File;
import java.net.URL;
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


    private ObservableArrayList<Step> steps;


    private File targetFolder;

    private RecordingListener listener;


    private BooleanProperty invalidName;
    private BooleanProperty targetFolderPresent;
    private BooleanBinding canExportOrCompress;

    private PopupHelper helper;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        root.getStylesheets().add("/res/main.css");

        invalidName = new SimpleBooleanProperty(false);
        targetFolderPresent = new SimpleBooleanProperty(false);

        BooleanBinding exportAvailable = title.textProperty()
                .isEqualTo("")
                //.or(author.textProperty().isEqualTo(""))
                .or(invalidName);

        canExportOrCompress = exportAvailable.or(targetFolderPresent.not());

        title.setOnKeyReleased(event -> {
            String text = title.getText();
            if (!FileUtils.isFilenameValid(text)) {
                invalidName.setValue(true);
                title.setStyle("-fx-text-fill: #ff4b41;");
            } else {
                invalidName.setValue(false);
                title.setStyle("-fx-text-fill: white;");
            }

        });

        export.disableProperty().bind(canExportOrCompress);
        exportTo.disableProperty().bind(exportAvailable);
        compress.disableProperty().bind(canExportOrCompress);

        steps = new ObservableArrayList<>();
        steps.onAdd((index, step) -> {
            //stepsContainer.getChildren().add(index, s.getBody());
            if(step.getPosition() == 0)
                step.setPosition(index + 1);
            addStep(index,step);
        });

        steps.onRemove((index, step)->  stepsContainer.getChildren().remove((int)index));

        addStep();
        addStep.setOnAction(event -> steps.add(new Step()));

        Overlay overlay = new Overlay();
        Camera camera = new Camera(overlay);

        NativeEventListener.bindGlobal();

        listener = new RecordingListener(overlay, camera);

        new SimpleShotListener(camera, new Callback1<Step>() {
            @Override
            public void call(final Step s) {
                Platform.runLater(() -> {
                    steps.add(s);
                    //reindex();
                    scrollPane.setVvalue(1.0d);
                });
            }
        });


        open.setOnAction(event -> {
            retrievePrimaryStage();
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
            sc.getSteps().forEach(this::addStep);
        });


        export.setOnAction(event -> exportToHtml());

        exportTo.setOnAction(event -> {
            retrievePrimaryStage();
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
        });

        compress.setOnAction(event -> compress());


        batchRecord.setOnAction(event -> {
            primaryStage.toBack();
            listener.batchRecord(() -> {
                listener.unbind();
                //simpleShotListener.bind();
                primaryStage.toFront();
                /*for (Step s : steps){
                    addStep(s);
                }*/
                //stepsContainer.getChildren().clear();
                //reindex();
            }, steps);
        });


        ClipBoardActionsHandler clipBoardListener = new ClipBoardActionsHandler();
        title.setOnKeyPressed(clipBoardListener);
        author.setOnKeyPressed(clipBoardListener);
        description.setOnKeyPressed(clipBoardListener);
        data.setOnKeyPressed(clipBoardListener);

        Platform.runLater(this::retrievePrimaryStage);

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

    private void addStep(Step s){
        addStep(steps.size(), s);
    }

    private void addStep(int index, final Step s){
        if(steps.size() == 1 && !steps.get(0).hasGif() && s.hasGif()){
            steps.get(0).setImage(s.getGif());
            //stepsContainer.getChildren().remove(0);
        }
        else {
            //steps.add(index,s);

            reindex();

            Platform.runLater(() -> {
                stepsContainer.getChildren().add(index, s.getBody());
                scrollPane.setVvalue(1.0d);
                reindex();
            });

            s.onMoveUpRequest(position -> {
                if (position > 1) {
                    Step target = steps.get(position - 1);
                    stepsContainer.getChildren().remove(position - 1);
                    stepsContainer.getChildren().add(position - 2, target.getBody());
                    //steps.remove(position-1);
                    //steps.add(position-2,target);
                    steps.unshift(position - 1);
                    reindex();
                }
            });

            s.onMoveDownRequest(position -> {
                if (position < steps.size()) {
                    Step target = steps.get(position - 1);
                    stepsContainer.getChildren().remove(position - 1);
                    stepsContainer.getChildren().add(position, target.getBody());
                    //steps.remove(position-1);
                    //steps.add(position,target);
                    steps.shift(position - 1);
                    reindex();
                }
            });

            s.onShotRequest(event -> {

                if (s.getPosition() < 3) {
                    helper.display();
                }
                retrievePrimaryStage();
                primaryStage.toBack();

                listener.initShot();

                s.setLoading();

                listener.setOnCancel(() -> Platform.runLater(() -> {
                    helper.hide();
                    s.setUnLoading();
                }));

                listener.onGifGenerating(s::setLoading);

                listener.onGifGenerated(new Callback1<String>() {
                    @Override
                    public void call(String arg0) {
                        s.setImage(arg0);
                        Platform.runLater(() -> {
                            primaryStage.toFront();
                            helper.hide();
                        });

                        listener.unbind();
                    }
                });
            });

            s.onDeleteRequest(() -> {
                reindex();
                steps.remove(s);
                stepsContainer.getChildren().remove(s.getPosition() - 1);
                reindex();
            });
        }
    }


    private void compress(){
        if(canExportOrCompress.not().get()){
            progress.setProgress(-1.);
            invalidName.setValue(true);

            String path = targetFolder+"/"+title.getText();
            ZipUtil.pack(new File(path), new File(path+".zip"), Deflater.BEST_COMPRESSION);

            progress.setProgress(1.);
            Platform.runLater(() -> {
                invalidName.setValue(false);
                canExportOrCompress.invalidate();
            });

        }
    }

    private void exportToHtml(){
        new Thread(() -> {
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

                exporter.export(scenario, progress); // <++++++++++

                invalidName.setValue(false);
            }
        }).start();
    }

    private Stage retrievePrimaryStage(){
        primaryStage = root.getScene() != null
                ? (Stage) root.getScene().getWindow()
                : null;
        return primaryStage;
    }
}
