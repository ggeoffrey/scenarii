package scenarii;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.apache.commons.io.FileUtils;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import com.sun.javafx.application.PlatformImpl;

import java.io.File;
import java.io.IOException;


/**
 * Application entry point
 */
public class Main extends Application {

    @Override
    public void start(final Stage primaryStage) throws Exception {

        // window's width
        int w = 800;
        // and height
        int h = 600;

        
        BorderPane root = FXMLLoader.load(getClass().getResource("/res/main.fxml"));
        primaryStage.setScene(new Scene(root, w, h));
        primaryStage.setResizable(false);
        primaryStage.setMaxWidth(w);

        primaryStage.setTitle("Scenarii");
        primaryStage.getIcons().add(new Image(getClass().getResource("/res/logo/logo-48.png").toString()));
        primaryStage.show();

        // Close properly
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                try {
                    // ensure native listener are closed
                    GlobalScreen.unregisterNativeHook();

                    // Clean temp files
                    String path = System.getProperty("user.home")+"/scenarii-snaps/";
                    FileUtils.deleteDirectory(new File(path));
                } catch (NativeHookException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    public static void main(final String[] args) {
        launch(args);
    }
}
