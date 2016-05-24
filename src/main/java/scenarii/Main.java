package scenarii;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.SwingDispatchService;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseMotionListener;
import scenarii.camera.Camera;
import scenarii.overlay.Overlay;

public class Main extends Application {

    @Override
    public void start(final Stage primaryStage) throws Exception {

        //stage.initStyle(StageStyle.TRANSPARENT);

        BorderPane root = FXMLLoader.load(getClass().getResource("/res/main.fxml"));
        primaryStage.setScene(new Scene(root, 800, 600));

        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        GlobalScreen.unregisterNativeHook();
        super.stop();
    }


    public static void main(final String[] args) {
        launch(args);
    }
}
