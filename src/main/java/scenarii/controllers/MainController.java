package scenarii.controllers;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.zeroturnaround.zip.ZipUtil;
import scenarii.camera.Camera;
import scenarii.collections.ObservableArrayList;
import scenarii.dirtycallbacks.Do;
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

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
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
    @FXML private Button reset;
    @FXML private TextField title;
    @FXML private TextField author;
    @FXML private TextArea description;
    @FXML private TextArea data;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox stepsContainer;
    @FXML private Button addStep;
    @FXML private ProgressIndicator progress;

    @FXML private CheckBox circleCheckBox;
    @FXML private ComboBox<Integer> fpsSelector;
    @FXML private Button configureButton;


    private Stage primaryStage;
    private ObservableArrayList<Step> steps;
    private File targetFolder;
    private RecordingListener listener;
    private BooleanProperty invalidName;
    private BooleanProperty targetFolderPresent;
    private BooleanBinding canExportOrCompress;
    private PopupHelper helper;

    private SettingsController settingsController;
    private Stage settingsStage;
    private SimpleShotListener simpleShotListner;

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

        root.setOnKeyReleased(event -> {
            if(event.isControlDown() || event.isMetaDown()){
                String s = event.getCode().getName();
                if(s == "S" || s == "K"){
                    if(canExportOrCompress.not().get())
                        export.fire();
                    else exportTo.fire();
                }
            }
        });

        final int defaultFps = 30;
        fpsSelector.getItems().addAll(3,6,12,18,24,30);


        steps = new ObservableArrayList<>();
        steps.onAdd(this::addStep);

        steps.onRemove((index, step)->  stepsContainer.getChildren().remove((int)index));

        steps.add(new Step());
        addStep.setOnAction(event -> steps.add(new Step()));

        Overlay overlay = new Overlay();
        Camera camera = new Camera(overlay, defaultFps);

        fpsSelector.setOnAction(event -> {
            int value = fpsSelector.getSelectionModel().getSelectedItem();
            camera.setFps(value);
        });
        fpsSelector.setValue(defaultFps);

        circleCheckBox.setOnAction(event -> {
            overlay.setDisplayCenterCircle(circleCheckBox.isSelected());
        });

        NativeEventListener.bindGlobal();

        listener = new RecordingListener(overlay, camera, steps, ()-> Platform.runLater(()->{
            helper.hide();
            retrievePrimaryStage();
            primaryStage.toFront();
            scrollDown();
            steps.stream()
                    //.filter(Step::isLoading)  // TODO
                    .forEach(Step::setUnLoading);
        }));

        simpleShotListner = new SimpleShotListener(camera, (step) -> Platform.runLater(() -> {
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

        reset.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Reset warning");
            alert.setHeaderText("You are going to loose all current steps and restart from scratch.");
            alert.setContentText("Are you ok with this?");

            Optional<ButtonType> result = alert.showAndWait();
            if(result.get() == ButtonType.NO || result.get() == ButtonType.CANCEL){
                alert.close();
            }
            else{
                steps.clear();
                stepsContainer.getChildren().clear();
                title.setText("");
                targetFolder = null;
                targetFolderPresent.set(false);
            }
        });

        ClipBoardActionsHandler clipBoardListener = new ClipBoardActionsHandler();
        title.setOnKeyPressed(clipBoardListener);
        author.setOnKeyPressed(clipBoardListener);
        description.setOnKeyPressed(clipBoardListener);
        data.setOnKeyPressed(clipBoardListener);

        Platform.runLater(this::retrievePrimaryStage);

        helper = PopupHelper.get();

        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);


        try {
            URL loc = getClass().getResource("/res/settings.fxml");
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(loc);
            loader.setBuilderFactory(new JavaFXBuilderFactory());

            BorderPane config = loader.load(loc.openStream());
            settingsController = loader.getController();
            settingsController.setMainControllerRef(this);

            settingsStage = new Stage();
            settingsController.setCorrespondingStage(settingsStage);

            settingsStage.setScene(new Scene(config,450,200));
            settingsStage.setResizable(false);
            settingsStage.setTitle("Settings");
        } catch (IOException e) {
            System.err.println("ERROR: Unable to load settings view.");
            System.err.println("       (MainController::configureButton::λ.onAction) ==>");
            System.err.println(e.getMessage());
        }
        configureButton.setOnAction(event -> {
            settingsStage.show();
            settingsStage.toFront();
        });
    }


    private void addStep(int index, final Step s){
        if(steps.size() == 2 && !steps.get(0).hasGif() && s.hasGif()){
            steps.remove(0);
            addStep(0,s);
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

            s.onViewRequest(()->{
                if(s.hasGif()){
                    try {
                        Desktop.getDesktop().browse(s.getImage().toURI());
                    } catch (IOException e) {
                        System.err.println("ERROR: Unable to open step's image in default desktop viewer");
                        System.err.println("       (Step::onViewRequest::λ.callback) ==>");
                        System.err.println(e.getMessage());
                    }
                }
            });

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

            String path = targetFolder+"/"+FileUtils.exportableFolderName(title.getText());
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
            Do.after(10, ()-> Platform.runLater(() -> {
                scrollPane.setVvalue(scrollPane.getVmax());
                scrolling = false;
            }));
        }
    }


    SimpleShotListener getSimpleShotListner() {
        return simpleShotListner;
    }
}
