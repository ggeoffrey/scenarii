package scenarii;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.SwingDispatchService;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseMotionListener;
import scenarii.camera.Camera;
import scenarii.overlay.Overlay;

public class Main extends Application implements NativeMouseMotionListener, NativeKeyListener {

    private Stage stage;
    private TextArea area;

    private Overlay overlay;
    private Camera camera;

    // actions
    private boolean ctrlKey;
    private boolean shiftKey;
    private boolean altKey;

    private boolean isResizing;


    private NativeMouseEvent mousePosition;
    private NativeMouseEvent mouseOrigin;
    // -------

    @Override
    public void nativeMouseMoved(final NativeMouseEvent nativeMouseEvent) {

        mousePosition = nativeMouseEvent;

        if(ctrlKey){
            final double xo = mouseOrigin.getX();
            final double yo = mouseOrigin.getY();
            final double xc = nativeMouseEvent.getX();
            final double yc = nativeMouseEvent.getY();

            if(!isResizing) {
                isResizing = true;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        overlay.distort(xo, yo, xc, yc);
                        isResizing = false;
                    }
                });
            }
        }
        else{
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    overlay.setPosition(nativeMouseEvent);
                }
            });
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        switch (nativeKeyEvent.getKeyCode()){
            case 29:
                ctrlKey = true;
                mouseOrigin = mousePosition;
                break;
            case 42:
                shiftKey = true;
                break;
            case 56:
                altKey = true;
                break;
            default:
                break;
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
        switch (nativeKeyEvent.getKeyCode()){
            case 29:
                ctrlKey = false;
                mouseOrigin = null;
                break;
            case 42:
                shiftKey = false;
                break;
            case 56:
                altKey = false;
                if(camera.isRecording()) {
                    camera.stopRecord();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            overlay.showForDistort();
                        }
                    });
                }
                else{
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            overlay.hideForDistort();
                        }
                    });
                    camera.startRecord();
                }
            default:
                area.appendText(nativeKeyEvent.getKeyCode()+"\n");
                break;
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent nativeMouseEvent) {
        nativeMouseMoved(nativeMouseEvent);
    }



    @Override
    public void start(final Stage primaryStage) throws Exception {

        stage = primaryStage;
        stage.initStyle(StageStyle.TRANSPARENT);

        BorderPane root = new BorderPane();
        area = new TextArea();
        root.setCenter(area);
        Scene scene = new Scene(root, 300, 275);
        scene.setFill(null);
        primaryStage.setScene(scene);

        GlobalScreen.setEventDispatcher(new SwingDispatchService());
        GlobalScreen.addNativeMouseMotionListener(this);
        GlobalScreen.addNativeKeyListener(this);

        primaryStage.show();

        overlay = new Overlay();
        overlay.show();
        camera = new Camera(overlay, 30);
    }

    @Override
    public void stop() throws Exception {
        GlobalScreen.unregisterNativeHook();
        super.stop();
    }


    public static void main(final String[] args) {
        try {
            GlobalScreen.registerNativeHook();
            launch(args);
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }
}
