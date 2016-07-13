package scenarii.listeners;

import javafx.application.Platform;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.mouse.NativeMouseEvent;
import scenarii.camera.Camera;
import scenarii.collections.SynchronisedThreadResultCollector;
import scenarii.controllers.State;
import scenarii.dirtycallbacks.Callback1;
import scenarii.dirtycallbacks.EmptyCallback;
import scenarii.geometry.Point;
import scenarii.model.Step;
import scenarii.overlay.Overlay;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

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
    private ArrayList<Step> stepsAccumulator;
    //-----------


    private SynchronisedThreadResultCollector<Step> collector;


    public RecordingListener(Overlay overlay, Camera camera) {
        this.overlay = overlay;
        this.camera = camera;

        shouldBeKeptOnFront = false;
        batchRecord = false;
        escCount = 0;
        stepsAccumulator = new ArrayList<Step>();
        collector = new SynchronisedThreadResultCollector<Step>(stepsAccumulator);
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
            //mouse.relativeTo(center);

            final double xo = center.getX();
            final double yo = center.getY();
            final double xc = mouse.getX();
            final double yc = mouse.getY();

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
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if(camera.isRecording()){

                                if(onGifGenerating!=null)
                                    onGifGenerating.call();

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        camera.stopRecord();
                                        if(onGifGenerated != null)
                                            onGifGenerated.call(camera.getLastImageProduced());
                                    }
                                }).start();
                            }
                            else{
                                onCancel.call();
                            }
                            overlay.showForDistort();
                            overlay.hide();
                        }
                    });
                }
                else{ // batchRecord : true
                    if(camera.isRecording()){
                        final CompletableFuture<Step> collect = new CompletableFuture<>();
                        collector.execute(collect);
                        RecordingListener $this = this;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                camera.stopRecord(new Callback1<String>() {
                                    @Override
                                    public void call(String path) {
                                        Step s = new Step(1);
                                        s.setImage(path);
                                        collect.complete(s);
                                    }
                                });
                                // replace the camera with a clone of the old one
                                $this.camera = new Camera(camera);
                                /* thus, the old camera will do
                                    it's job in the background
                                    and be GCed when done.
                                  */
                            }
                        }).start();

                        overlay.showForDistort();
                        escCount++;
                    }
                    else if(escCount >= 2){
                        batchRecord = false;
                        if(onBatchGenerated != null){
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    overlay.hide();
                                    onBatchGenerated.call();
                                }
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
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            overlay.hideForDistort();
                        }
                    });
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

    public void batchRecord(EmptyCallback callback, ArrayList<Step> target){
        batchRecord = true;
        onBatchGenerated = callback;
        stepsAccumulator = target;
        initShot();
    }
}
