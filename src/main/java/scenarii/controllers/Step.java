package scenarii.controllers;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.function.Consumer;

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

            positionText.setText(""+position);

            cameraIcon.setOpacity(1.);

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

    public HBox getBody() {
        return body;
    }

    public boolean hasGif(){
        return gif.getImage() != null;
    }

    public void setImage(String path){
        Image image = new Image(path);
        gif.setImage(image);
        cameraIcon.setOpacity(0.);
        gif.fitWidthProperty().bind(gifContainer.widthProperty());
        gif.fitHeightProperty().bind(gifContainer.heightProperty());
    }


    public void onShotRequest(EventHandler eventHandler){
        cameraIcon.setOnMouseClicked(eventHandler);
    }
}
