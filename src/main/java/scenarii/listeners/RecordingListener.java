package scenarii.listeners;

import javafx.application.Platform;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.mouse.NativeMouseEvent;
import scenarii.camera.Camera;
import scenarii.collections.ObservableArrayList;
import scenarii.collections.SynchronisedThreadResultCollector;
import scenarii.controllers.State;
import scenarii.dirtycallbacks.Callback1;
import scenarii.dirtycallbacks.EmptyCallback;
import scenarii.geometry.Point;
import scenarii.model.Step;
import scenarii.overlay.Overlay;

/**
 * Created by geoffrey on 12/07/2016.
 */
public class RecordingListener extends NativeEventListener{


    private Overlay overlay;
    private Camera camera;
    //private Stage window;

    private boolean ctrlKey;
    private boolean altKey;

    private boolean isResizing;

    private NativeMouseEvent mousePosition;
    private NativeMouseEvent mouseOrigin;


    private State state;
    private boolean shouldBeKeptOnFront;
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


    private SynchronisedThreadResultCollector<Step> collector;


    public RecordingListener(Overlay overlay, Camera camera) {
        this.overlay = overlay;
        this.camera = camera;

        shouldBeKeptOnFront = false;
        batchRecord = false;
        escCount = 0;
        stepsAccumulator = new ObservableArrayList<>();
        collector = new SynchronisedThreadResultCollector<>(stepsAccumulator);
    }

    public void setState(State state){
        this.state = state;
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

        //if(shouldBeKeptOnFront)
        overlay.toFront();
        //else
        shouldBeKeptOnFront = true;

        if(ctrlKey && !camera.isRecording()){
            Point overlayPostion = overlay.getPosition();
            Point center = new Point(overlay.getCenter());
            center.translate(
                    (int) overlayPostion.getX(),
                    (int) overlayPostion.getY()
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
                altKey = true;
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
                        //final CompletableFuture<Step> collect = new CompletableFuture<>();

                        RecordingListener $this = this;
                        new Thread(() -> {
                            Step s = new Step(1);
                            s.setLoading();
                            stepsAccumulator.add(s);
                            //collector.execute(s, collect, (path)-> );
                            camera.stopRecord(s::setImage);
                            // replace the camera with a clone of the old one
                            $this.camera = new Camera(camera);
                            /* thus, the old camera will do
                                it's job in the background
                                and be GCed when done.
                              */
                        }).start();

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
            case 56:
                altKey = false;
                escCount = Math.max(0, escCount-1);
                boolean cameraIsOff = !camera.isRecording();
                if(cameraIsOff){
                    Platform.runLater(() -> overlay.hideForDistort());
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
        shouldBeKeptOnFront = false;
        nativeMouseMoved(nativeMouseEvent);
    }

    public void initShot(){
        overlay.show();
        overlay.showForDistort();
        state = State.RESIZING;
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
        collector.setValuesCollector(target);
        initShot();
    }
}
