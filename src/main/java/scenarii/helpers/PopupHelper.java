package scenarii.helpers;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.*;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.awt.*;
import java.io.IOException;

/**
 * Created by geoffrey on 05/07/2016.
 */
public class PopupHelper extends Stage{



    /*public static void displayMessage(String title, String subtitle, String message){




            //ProcessBuilder pb = new ProcessBuilder();
            //Process p = pb.start();
            //int exitCode = p.waitFor();
            //System.out.println(exitCode);
            //
            //Thread.sleep(1000);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    java.lang.Runtime.getRuntime().exec(new String[]{"osascript", "-e", "display notification \"Hello\""});
                    Thread.sleep(1000);
                }
                catch (Exception e){
                    System.err.println("Unable to publish notifications...");
                    System.err.println(e.getMessage());
                }
            }
        });

        //Thread.sleep(1000);

        //if(SystemUtils.IS_OS_WINDOWS) {
        //    final java.awt.Image iconImage = Toolkit.getDefaultToolkit().getImage("/res/logo/logo-icon.gif");
        //    final TrayIcon icon = new TrayIcon(iconImage, "Scenarii");
        //    icon.setImageAutoSize(true);
        //    SystemTray tray = SystemTray.getSystemTray();
        //    try {
        //        tray.add(icon);
        //        //icon.displayMessage("Test", "Hello", TrayIcon.MessageType.INFO);
        //    } catch (AWTException e) {
        //        System.err.println("Unable to load the system tray icon...");
        //    }
        //}
        //else if(SystemUtils.IS_OS_MAC){
        //    Platform.runLater(new Runnable() {
        //        @Override
        //        public void run() {
        //        }
        //    });
        //}

    }*/

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
            scene.setOnMouseMoved(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if(ft!= null){
                        ft.playFromStart();
                    }
                }
            });
            scene.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if(ft != null){
                        ft.stop();
                        _this.hide();
                    }
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
        ft.onFinishedProperty().addListener(new ChangeListener<EventHandler<ActionEvent>>() {
            @Override
            public void changed(ObservableValue<? extends EventHandler<ActionEvent>> observable, EventHandler<ActionEvent> oldValue, EventHandler<ActionEvent> newValue) {
                _this.hide();
            }
        });
    }

    public void hide(){
        if(ft!=null){
            ft.stop();
        }
        super.hide();
    }
}
