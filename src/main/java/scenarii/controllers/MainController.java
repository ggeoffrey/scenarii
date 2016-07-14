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

    @FXML private BorderPane root;
    @FXML private Button open;
    @FXML private Button export;
    @FXML private Button exportTo;
    @FXML private Button compress;
    @FXML private Button batchRecord;
    @FXML private TextField title;
    @FXML private TextField author;
    @FXML private TextArea description;
    @FXML private TextArea data;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox stepsContainer;
    @FXML private Button addStep;
    @FXML private ProgressIndicator progress;


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
        steps.onAdd(this::addStep);

        steps.onRemove((index, step)->  stepsContainer.getChildren().remove((int)index));

        steps.add(new Step());
        addStep.setOnAction(event -> steps.add(new Step()));

        Overlay overlay = new Overlay();
        Camera camera = new Camera(overlay);

        NativeEventListener.bindGlobal();

        listener = new RecordingListener(overlay, camera, steps, ()-> Platform.runLater(()->{
            helper.hide();
            retrievePrimaryStage();
            primaryStage.toFront();
            scrollDown();
        }));

        new SimpleShotListener(camera, (step) -> Platform.runLater(() -> {
            steps.add(step);
            scrollDown();
        }), ()->{
            System.err.println("ERROR: <TODO> should display a popup or something else.");
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
            sc.getSteps().forEach((step -> steps.add(step)));
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
            listener.batchRecord();
        });

        ClipBoardActionsHandler clipBoardListener = new ClipBoardActionsHandler();
        title.setOnKeyPressed(clipBoardListener);
        author.setOnKeyPressed(clipBoardListener);
        description.setOnKeyPressed(clipBoardListener);
        data.setOnKeyPressed(clipBoardListener);

        Platform.runLater(this::retrievePrimaryStage);

        helper = PopupHelper.get();
    }


    private void addStep(Step s){
        addStep(steps.size(), s);
    }

    private void addStep(int index, final Step s){
        if(steps.size() == 1 && !steps.get(0).hasGif() && s.hasGif()){
            steps.get(0).setImage(s.getGif());
        }
        else {
            if(s.getPosition() == 0)
                s.setPosition(index + 1);

            Platform.runLater(() -> {
                stepsContainer.getChildren().add(index, s.getBody());
                scrollDown();
            });

            s.onMoveUpRequest(this::swapSteps);
            s.onMoveDownRequest(position -> swapSteps(position + 1));

            s.onShotRequest(event -> {
                s.setLoading();
                if (s.getPosition() < 3) {
                    helper.display();
                }
                retrievePrimaryStage();
                primaryStage.toBack();
                listener.shotSequence(s::setImage);
                scrollDown();
            });

            s.onDeleteRequest((position) -> {
                steps.remove(position - 1);
                for(int i = position - 1; i < steps.size(); i++){
                    steps.get(i).setPosition(i + 1);
                }
            });
        }
    }

    private void swapSteps(int position){
        if(position > 1 && position <= steps.size()){
            Step self = steps.get(position - 1);
            stepsContainer.getChildren().remove(position - 1);
            stepsContainer.getChildren().add(position - 2, self.getBody());

            steps.unshift(position - 1);
            steps.get(position - 1).setPosition(position);
            self.setPosition(position - 1);
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

    private boolean scrolling = false;
    private void scrollDown(){
        if (!scrolling) {
            scrolling = true;
            new Thread(()->{
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("done");
                Platform.runLater(() -> {
                    scrollPane.setVvalue(scrollPane.getVmax());
                    scrolling = false;
                });
            }).start();
        }
    }
}
