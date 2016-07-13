package scenarii.model;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.pegdown.PegDownProcessor;
import scenarii.controllers.ClipBoardActionsHandler;
import scenarii.dirtycallbacks.EmptyCallback;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by geoffrey on 24/05/2016.
 * Describe a step, part of a scenario.
 */
public class Step {

    // Step's position (1 based index)
    private int position;

    private HBox body;

    private Text positionText;
    private StackPane gifContainer;
    private ImageView gif;
    private ImageView cameraIcon;
    private TextArea description;

    private TextField optionalData;

    private AnchorPane up;
    private AnchorPane down;
    private ImageView trash;

    private File imageFile;

    private static Image spinner;
    private Image lastImage;

    public Step() {
        build();
        position = 0;

    }

    public Step(int position) {
        this.position = position;
        build();
    }

    private void build(){

        if(spinner == null){
            spinner = new Image(getClass().getResource("/res/spinner.gif").toString());
        }

        try {

            // extract GUI components from the DOM
            body = FXMLLoader.load(getClass().getResource("/res/step.fxml"));
            positionText = (Text) body.lookup(".step-number");
            gifContainer = (StackPane) body.lookup(".gif-container");
            gif = (ImageView) body.lookup(".gif");
            cameraIcon = (ImageView) body.lookup(".camera-icon");
            description = (TextArea) body.lookup(".step-description");

            ChoiceBox<ActionType> actions = (ChoiceBox<ActionType>) body.lookup(".action-type");
            optionalData = (TextField) body.lookup(".step-data");
            up = (AnchorPane) body.lookup(".up");
            down = (AnchorPane) body.lookup(".down");
            trash = (ImageView) body.lookup(".trash-button");

            positionText.setText(""+position);

            cameraIcon.setOpacity(1.);

            description.setOnKeyPressed(new ClipBoardActionsHandler());


            actions.setItems(FXCollections.observableArrayList(ActionType.values()));
            actions.setValue(ActionType.NOACTION);

            final ChangeListener<String> listener = (observable, oldValue, newValue) -> {

                //if(newValue.substring(1,newValue.length()).contains("`")
                //        || ! (newValue.startsWith("`") && newValue.startsWith("`"))){
                //    optionalData.setText(quote(newValue));
                //}
            };

            actions.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                switch (newValue){
                    case KEYPRESS:
                        optionalData.setEditable(true);
                        optionalData.textProperty().addListener(listener);
                        optionalData.requestFocus();
                        break;
                    case SIMPLETEXTINSERT:
                        optionalData.setEditable(true);
                        optionalData.textProperty().removeListener(listener);
                        optionalData.setText(unquote(optionalData.getText()));
                        optionalData.requestFocus();
                        break;
                    default:
                        optionalData.setEditable(false);
                        optionalData.clear();
                        optionalData.textProperty().removeListener(listener);
                        break;
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
        positionText.setText(""+position);
    }

    public void setDescription(String s){
        description.setText(s);
    }

    public HBox getBody() {
        return body;
    }

    public boolean hasGif(){
        return gif.getImage() != null;
    }

    public void setLoading(){
        lastImage = gif.getImage();
        setImage(spinner);
    }

    public void setUnLoading(){
        setImage(lastImage);
    }


    public void setImage(String path){
        if(path != null){
            imageFile = new File(path);
            if(imageFile.exists() && imageFile.isFile()){
                Image image = new Image("file:"+path);
                setImage(image);
            }
        }
    }

    public void setImage(Image image){
        gif.setImage(image);
        cameraIcon.setOpacity(0.);
        gif.setPreserveRatio(true);
        gifContainer.setMaxWidth(100);
    }

    public File getImage(){
        return this.imageFile;
    }
    public Image getGif(){ return gif.getImage(); }


    public void onShotRequest(EventHandler eventHandler){
        cameraIcon.setOnMouseClicked(eventHandler);
    }

    public void onDeleteRequest(final EmptyCallback callback){
        trash.setOnMouseClicked(event -> callback.accept());
    }

    public void onMoveUpRequest(final Consumer<Integer> callback){
        up.setOnMouseClicked(event -> callback.accept(position));
    }

    public void onMoveDownRequest(final Consumer<Integer> callback){
        down.setOnMouseClicked(event -> callback.accept(position));
    }

    Map<String,Object> toJadeModel(PegDownProcessor parser){
        Map<String,Object> model = new HashMap<>();

        model.put("position", position);
        model.put("rawDescription", description.getText().replaceAll("\\n","\\$br"));
        model.put("description", parser.markdownToHtml(description.getText()));
        if(imageFile != null)
            model.put("gif", imageFile.getName());

        return model;
    }

    private String quote(final String s){
        String newString = s.replaceAll("\\`","");
        if(!newString.startsWith("`"))
            newString = "`" + s;
        if(!newString.endsWith("`"))
            newString = s + "`";
        return newString;
    }

    private String unquote(final String s){
        int start = 0, end = s.length();
        if(s.startsWith("`"))
            start = 1;
        if(s.endsWith("`"))
            end = end-1;
        return s.substring(start,end);
    }
}
