package scenarii.controllers;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.SwingDispatchService;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseMotionListener;

import de.neuland.jade4j.parser.node.Node;
import scenarii.camera.Camera;
import scenarii.dirtycallbacks.Callback1;
import scenarii.dirtycallbacks.EmptyCallback;
import scenarii.geometry.*;
import scenarii.model.Step;
import scenarii.overlay.Overlay;


import scenarii.geometry.Point;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by geoffrey on 24/05/2016.
 */
public class NativeEventListener implements NativeMouseMotionListener, NativeKeyListener {

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
    private Callback1<ArrayList<Step>> onBatchGenerated;
    private EmptyCallback onCancel;


    // Accumulators
    private ArrayList<Step> stepsAccumulator;
    //-----------


    public NativeEventListener(Overlay overlay, Camera camera) {
        this.overlay = overlay;
        this.camera = camera;
        //this.window = stage;
        
        shouldBeKeptOnFront = false;
        batchRecord = false;
        escCount = 0;
        stepsAccumulator = new ArrayList<Step>();
    }


	public void setState(State state){
        this.state = state;
    }


    public void onGifGenerated(Callback1<String> callback1){
        onGifGenerated = callback1;
    }

    public void setOnCancel(EmptyCallback callback){
        onCancel = callback;
    }
    @Override
    public void nativeMouseMoved(final NativeMouseEvent nativeMouseEvent) {

        mousePosition = nativeMouseEvent;

        if(shouldBeKeptOnFront)
        	overlay.toFront();
        else
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
            /*
            case 42:
                shiftKey = true;
                break;
            */
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
                                camera.stopRecord();
                                if(onGifGenerated != null)
                                    onGifGenerated.call(camera.getLastImageProduced());
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
                        camera.stopRecord();
                        Step s = new Step(1);
                        s.setImage(camera.getLastImageProduced());
                        stepsAccumulator.add(s);
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
                                    onBatchGenerated.call(stepsAccumulator);
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
            /*
            case 42:
                shiftKey = false;
                break;
            */
            case 56:
                altKey = false;
                escCount = Math.max(0, escCount-1);
                if(!camera.isRecording()){
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

    public void batchRecord(Callback1<ArrayList<Step>> onBatchGenerated){
        batchRecord = true;
        this.onBatchGenerated = onBatchGenerated;
        stepsAccumulator.clear();
        initShot();
    }

    public void bind(){
        try{
            LogManager.getLogManager().reset();
            Logger.getLogger(GlobalScreen.class.getPackage().getName())
                    .setLevel(Level.WARNING);
            GlobalScreen.registerNativeHook();
            GlobalScreen.setEventDispatcher(new SwingDispatchService());
            GlobalScreen.addNativeMouseMotionListener(this);
            GlobalScreen.addNativeKeyListener(this);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void unbind(){
        try {
            GlobalScreen.removeNativeKeyListener(this);
            GlobalScreen.removeNativeMouseMotionListener(this);
            GlobalScreen.unregisterNativeHook();
        }
        catch (NativeHookException e){
            e.printStackTrace();
        }
    }
}
