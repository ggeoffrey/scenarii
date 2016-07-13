package scenarii.listeners;

import javafx.application.Platform;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.mouse.NativeMouseEvent;
import scenarii.camera.Camera;
import scenarii.collections.ObservableArrayList;
import scenarii.dirtycallbacks.Callback1;
import scenarii.dirtycallbacks.EmptyCallback;
import scenarii.geometry.Point;
import scenarii.model.Step;
import scenarii.overlay.Overlay;

/**
 * Created by geoffrey on 12/07/2016.
 */
public class RecordingListener extends NativeEventListener{


    private final Overlay overlay;
    private Camera camera;
    //private Stage window;

    private boolean ctrlKey;

    private boolean isResizing;

    private NativeMouseEvent mousePosition;
    private NativeMouseEvent mouseOrigin;


    private boolean batchRecord;

    private int escCount;

    // Callbacks

    private Callback1<String> onGifGenerated;
    private EmptyCallback onGifGenerating;
    //private Callback1<ArrayList<Step>> onBatchGenerated;
    private EmptyCallback onBatchGenerated;
    private EmptyCallback onCancel;


    // Accumulators
    private ObservableArrayList<Step> stepsAccumulator;
    //-----------


    //private SynchronisedThreadResultCollector<Step> collector;


    public RecordingListener(Overlay overlay, Camera camera) {
        this.overlay = overlay;
        this.camera = camera;

        batchRecord = false;
        escCount = 0;
        stepsAccumulator = new ObservableArrayList<>();
    }


    public void onGifGenerated(Callback1<String> callback1){
        onGifGenerated = callback1;
    }
    public void onGifGenerating(EmptyCallback callback){
        onGifGenerating = callback;
    }

    public void setOnCancel(EmptyCallback callback){
        onCancel = callback;
    }
    @Override
    public void nativeMouseMoved(final NativeMouseEvent nativeMouseEvent) {

        mousePosition = nativeMouseEvent;

        overlay.toFront();

        if(ctrlKey && !camera.isRecording()){
            Point overlayPosition = overlay.getPosition();
            Point center = new Point(overlay.getCenter());
            center.translate(
                    (int) overlayPosition.getX(),
                    (int) overlayPosition.getY()
            );
            Point mouse = new Point(nativeMouseEvent.getX(), nativeMouseEvent.getY());

            final double xo = center.getX();
            final double yo = center.getY();
            final double xc = mouse.getX();
            final double yc = mouse.getY();

            if(!isResizing) {
                isResizing = true;
                Platform.runLater(() -> {
                    overlay.distort(xo, yo, xc, yc);
                    isResizing = false;
                });
            }
        }
        else{
            Platform.runLater(() -> overlay.setPosition(nativeMouseEvent));
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        switch (nativeKeyEvent.getKeyCode()){
            case 29:
                ctrlKey = true;
                mouseOrigin = mousePosition;
                break;
            case 56:
                break;
            default:
                break;
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

        switch (nativeKeyEvent.getKeyCode()){

            case 1: // ESC
                if(!batchRecord){
                    Platform.runLater(() -> {
                        if(camera.isRecording()){

                            if(onGifGenerating!=null)
                                onGifGenerating.accept();

                            new Thread(() -> {
                                camera.stopRecord();
                                if(onGifGenerated != null)
                                    onGifGenerated.call(camera.getLastImageProduced());
                            }).start();
                        }
                        else{
                            onCancel.accept();
                        }
                        overlay.showForDistort();
                        overlay.hide();
                    });
                }
                else{ // batchRecord : true
                    if(camera.isRecording()){
                        final Camera c = camera;
                        RecordingListener $this = this;
                        new Thread(() -> {
                            Step s = new Step(1);
                            s.setLoading();
                            stepsAccumulator.add(s);
                            camera.stopRecord(s::setImage);
                            // replace the camera with a clone of the old one

                            /* thus, the old camera will do
                                it's job in the background
                                and be GCed when done.
                              */
                        }).start();
                        $this.camera = new Camera(camera);
                        overlay.showForDistort();
                        escCount++;
                    }
                    else if(escCount >= 2){
                        batchRecord = false;
                        if(onBatchGenerated != null){
                            Platform.runLater(() -> {
                                overlay.hide();
                                onBatchGenerated.accept();
                            });
                        }
                    }
                    else {
                        escCount++;
                    }
                }
                break;
            case 29:
                ctrlKey = false;
                mouseOrigin = null;
                break;
            case 56: // alt
                escCount = Math.max(0, escCount-1);
                boolean cameraIsOff = !camera.isRecording();
                if(cameraIsOff){
                    Platform.runLater(overlay::hideForDistort);
                    camera.startRecord();
                }
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

    public void initShot(){
        overlay.show();
        overlay.showForDistort();
        bind();
    }

    /*public void batchRecord(Callback1<ArrayList<Step>> onBatchGenerated){
        batchRecord = true;
        this.onBatchGenerated = onBatchGenerated;
        stepsAccumulator.clear();
        initShot();
    }*/

    public void batchRecord(EmptyCallback callback, ObservableArrayList<Step> target){
        batchRecord = true;
        onBatchGenerated = callback;
        stepsAccumulator = target;
        //collector.setValuesCollector(target);
        initShot();
    }
}
