package scenarii.helpers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Created by geoffrey on 05/07/2016.
 */
public class PopupHelper extends Stage{


    private static PopupHelper helper;

    public static PopupHelper get(){
        if(helper == null)
            return helper = new PopupHelper();
        else return helper;
    }


    private AnchorPane root;
    private FadeTransition ft;

    private PopupHelper() {

        super(StageStyle.TRANSPARENT);

        setAlwaysOnTop(true);
        setResizable(false);


        Scene scene = null;
        try{
            root = FXMLLoader.load(getClass().getResource("/res/help.fxml"));
            scene = new Scene(root, 600, 100);
            scene.setFill(null);
            setScene(scene);
        }
        catch (IOException err){
            System.err.println("Unable to load help.fxml");
        }

        Rectangle2D primaryScreen = Screen.getPrimary().getVisualBounds();
        this.setX(primaryScreen.getWidth()/2 - root.getWidth()/2);
        this.setY((primaryScreen.getHeight()/10) * 9);

        final PopupHelper _this = this;
        if(scene != null){
            scene.setOnMouseMoved(event -> {
                if(ft!= null){
                    ft.playFromStart();
                }
            });
            scene.setOnMouseClicked(event -> {
                if(ft != null){
                    ft.stop();
                    _this.hide();
                }
            });
        }

    }


    public void display(){
        this.show();
        this.toFront();
        final PopupHelper _this = this;
        ft = new FadeTransition(Duration.millis(4000), root);
        ft.setFromValue(1.);
        ft.setToValue(0.);
        ft.play();
        ft.setOnFinished(event -> _this.hide());
    }

    public void hide(){
        super.hide();
        if(ft!=null){
            ft.stop();
        }
    }
}
