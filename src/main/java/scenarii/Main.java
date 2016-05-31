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
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

public class Main extends Application {

    @Override
    public void start(final Stage primaryStage) throws Exception {

        //stage.initStyle(StageStyle.TRANSPARENT);

        int w = 800;
        int h = 600;

        BorderPane root = FXMLLoader.load(getClass().getResource("/res/main.fxml"));
        primaryStage.setScene(new Scene(root, w, h));
        primaryStage.setResizable(false);
        primaryStage.setMaxWidth(w);

        primaryStage.setTitle("Scenarii");
        primaryStage.getIcons().add(new Image(getClass().getResource("/res/logo/logo-48.png").toString()));
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                try {
                    GlobalScreen.unregisterNativeHook();
                } catch (NativeHookException e) {
                    e.printStackTrace();
                }
                finally {
                    Platform.exit();
                }
            }
        });
    }


    public static void main(final String[] args) {
        launch(args);
    }
}
