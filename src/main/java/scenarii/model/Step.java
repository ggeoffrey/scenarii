package scenarii.model;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.neuland.jade4j.lexer.token.Call;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.pegdown.PegDownProcessor;
import scenarii.controllers.ClipBoardActionsHandler;
import scenarii.dirtycallbacks.Callback1;
import scenarii.dirtycallbacks.EmptyCallback;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by geoffrey on 24/05/2016.
 */
public class Step {

    private int position;

    private HBox body;

    private Text positionText;
    private StackPane gifContainer;
    private ImageView gif;
    private ImageView cameraIcon;
    private TextArea description;

    private ChoiceBox<ActionType> actions;
    private TextField optionalData;

    private FontAwesomeIconView up;
    private FontAwesomeIconView down;
    private FontAwesomeIconView trash;

    private File imageFile;


    public Step() {
        build();
        position = 0;
    }

    public Step(int position) {
        this.position = position;
        build();
    }

    private void build(){

        try {
            body = (HBox) FXMLLoader.load(getClass().getResource("/res/step.fxml"));
            positionText = (Text) body.lookup(".step-number");
            gifContainer = (StackPane) body.lookup(".gif-container");
            gif = (ImageView) body.lookup(".gif");
            cameraIcon = (ImageView) body.lookup(".camera-icon");
            description = (TextArea) body.lookup(".step-description");

            actions = (ChoiceBox<ActionType>) body.lookup(".action-type");
            optionalData = (TextField) body.lookup(".step-data");
            up = (FontAwesomeIconView) body.lookup(".up");
            down = (FontAwesomeIconView) body.lookup(".down");
            trash = (FontAwesomeIconView) body.lookup(".trash-button");

            positionText.setText(""+position);

            cameraIcon.setOpacity(1.);

            description.setOnKeyPressed(new ClipBoardActionsHandler());



            actions.setItems(FXCollections.observableArrayList(ActionType.values()));
            actions.setValue(ActionType.NOACTION);

            ChangeListener<String> listener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                    //if(newValue.substring(1,newValue.length()).contains("`")
                    //        || ! (newValue.startsWith("`") && newValue.startsWith("`"))){
                    //    optionalData.setText(quote(newValue));
                    //}
                }
            };

            actions.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ActionType>() {
                @Override
                public void changed(ObservableValue<? extends ActionType> observable, ActionType oldValue, ActionType newValue) {
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

    public void setImage(String path){
        imageFile = new File(path);
        if(imageFile.exists() && imageFile.isFile()){
            Image image = new Image("file:"+path);
            gif.setImage(image);
            cameraIcon.setOpacity(0.);
            gif.fitWidthProperty().bind(gifContainer.widthProperty());
            gif.fitHeightProperty().bind(gifContainer.heightProperty());
        }
    }

    public File getImage(){
        return this.imageFile;
    }


    public void onShotRequest(EventHandler eventHandler){
        cameraIcon.setOnMouseClicked(eventHandler);
    }

    public void onDeleteRequest(EmptyCallback callback){
        trash.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                callback.call();
            }
        });
    }

    public void onMoveUpRequest(Callback1<Integer> callback){
        up.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                callback.call(position);
            }
        });
    }

    public void onMoveDownRequest(Callback1<Integer> callback){
        down.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                callback.call(position);
            }
        });
    }

    public Map<String,Object> toJadeModel(PegDownProcessor parser){
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
