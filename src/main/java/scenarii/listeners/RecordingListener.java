package scenarii.listeners;

import javafx.application.Platform;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.mouse.NativeMouseEvent;
import scenarii.camera.Camera;
import scenarii.collections.ObservableArrayList;
import scenarii.dirtycallbacks.Callback;
import scenarii.geometry.Point;
import scenarii.model.Step;
import scenarii.overlay.Overlay;

import java.util.function.Consumer;

/**
 * Created by geoffrey on 12/07/2016.
 */
public class RecordingListener extends NativeEventListener{


    private final Overlay overlay;
    private Camera camera;

    private boolean ctrlKey;
    private boolean isResizing;
    private NativeMouseEvent mousePosition;
    private boolean batchRecord;
    private int escCount;


    // Callbacks

    private Callback onRecordEnd;
    private Consumer<String> onGifGenerated;
    private Callback cameraInstanceStopper;

    // Accumulators
    private ObservableArrayList<Step> steps;
    //-----------


    //private SynchronisedThreadResultCollector<Step> collector;


    public RecordingListener(Overlay overlay, Camera camera, ObservableArrayList<Step> steps, Callback onRecordEnd) {
        this.overlay = overlay;
        this.camera = camera;

        batchRecord = false;
        escCount = 0;
        this.steps = steps;
        this.onRecordEnd = onRecordEnd;
    }


    @Override
    public void nativeMouseMoved(final NativeMouseEvent nativeMouseEvent) {

        mousePosition = nativeMouseEvent;

        overlay.toFront();

        if(ctrlKey && !camera.isRecording() && !isResizing){
            Point overlayPosition = overlay.getPosition();
            Point center = new Point(overlay.getCenter());
            center.translate(
                    (int) overlayPosition.getX(),
                    (int) overlayPosition.getY()
            );
            Point mouse = new Point(nativeMouseEvent.getX(), nativeMouseEvent.getY());

            isResizing = true;
            final double xo = center.getX();
            final double yo = center.getY();
            final double xc = mouse.getX();
            final double yc = mouse.getY();

            Platform.runLater(() -> {
                overlay.distort(xo, yo, xc, yc);
                isResizing = false;
            });

        }
        else{
            Platform.runLater(() -> overlay.setPosition(nativeMouseEvent));
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        switch (nativeKeyEvent.getKeyCode()){
            case 29: // ctrl
                ctrlKey = true;
                break;
            case 56: // alt?
                break;
            default:
                break;
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

        switch (nativeKeyEvent.getKeyCode()){

            case 1: // ESC
                escCount++;
                if(camera.isRecording())
                    cameraInstanceStopper.accept();

                if(escCount > 1 || !batchRecord){
                    overlay.hide();
                    if(onRecordEnd != null) onRecordEnd.accept();
                    unbind();
                    batchRecord = false;
                }
                else overlay.showBorder();

                break;
            case 29:
                ctrlKey = false;
                break;
            case 56: // alt
                escCount = escCount > 0 ? escCount-1 : 0;
                overlay.hideBorder();
                if(batchRecord){
                    Step s = new Step();
                    s.setLoading();
                    steps.add(s);
                    cameraInstanceStopper = camera.record(s::setImage);
                }
                else {
                    cameraInstanceStopper = camera.record(onGifGenerated);
                    onGifGenerated = (path)->{
                        System.err.println("WARNING: Attempting to call an already consumed callback.");
                        System.err.println("         (RecordingListener::nativeKeyReleased::λ.onGifGenerated)");
                    };
                }

                break;
        }
    }


    @Override
    public void nativeMouseDragged(NativeMouseEvent nativeMouseEvent) {
        nativeMouseMoved(nativeMouseEvent);
    }

    public void shotSequence(Consumer<String> callback){
        overlay.show();
        overlay.showBorder();
        bind();
        this.onGifGenerated = callback;
    }

    public void batchRecord(){
        batchRecord = true;
        shotSequence((path)->{
            System.err.println("WARNING: Attempting to call onGifGenerated callback while recording in batch.");
            System.err.println("         A batch pushes Steps to the model and should not provide a callback.");
            System.err.println("         (RecordingListener::batchRecord::shotSequence.call(λpath.null)");
        });
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
    }
}
