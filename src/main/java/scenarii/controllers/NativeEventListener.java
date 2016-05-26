package scenarii.controllers;

import javafx.application.Platform;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseMotionListener;
import scenarii.camera.Camera;
import scenarii.dirtycallbacks.Callback1;
import scenarii.geometry.*;
import scenarii.overlay.Overlay;

import java.awt.*;
import scenarii.geometry.Point;

/**
 * Created by geoffrey on 24/05/2016.
 */
public class NativeEventListener implements NativeMouseMotionListener, NativeKeyListener {

    private Overlay overlay;
    private Camera camera;


    private boolean ctrlKey;
    private boolean altKey;

    private boolean isResizing;

    private NativeMouseEvent mousePosition;
    private NativeMouseEvent mouseOrigin;


    private State state;

    // Callbacks

    private Callback1<String> onGifGenerated;


    //-----------


    public NativeEventListener(Overlay overlay, Camera camera) {
        this.overlay = overlay;
        this.camera = camera;
    }


    public void setState(State state){
        this.state = state;
    }


    public void onGifGenerated(Callback1<String> callback1){
        onGifGenerated = callback1;
    }

    @Override
    public void nativeMouseMoved(final NativeMouseEvent nativeMouseEvent) {

        mousePosition = nativeMouseEvent;


        if(ctrlKey){
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
                if(state != State.IDLE){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            overlay.showForDistort();
                            overlay.hide();
                        }
                    });
                }
                if(camera.isRecording()){
                    camera.stopRecord();
                    if(onGifGenerated!=null)
                        onGifGenerated.call(camera.getLastImageProduced());
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
                if(!camera.isRecording()){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            overlay.hideForDistort();
                        }
                    });
                    camera.startRecord();
                }
            default:
                for (int i = 0; i < 100; i++){
                    System.out.println(nativeKeyEvent.getKeyCode());
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

}
