package scenarii.listeners;

import javafx.application.Platform;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.SwingDispatchService;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseMotionListener;
import scenarii.camera.Camera;
import scenarii.dirtycallbacks.Callback1;
import scenarii.dirtycallbacks.EmptyCallback;
import scenarii.geometry.Point;
import scenarii.model.Step;
import scenarii.overlay.Overlay;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by geoffrey on 24/05/2016.
 */
public class SimpleShotListener extends NativeEventListener {

    private Camera camera;

    private boolean ctrlKey;
    private boolean shiftKey;


    // Callbacks

    private Callback1<Step> onShot;



    public SimpleShotListener(Camera camera, Callback1<Step> onShot) {
        this.camera = camera;
        this.onShot = onShot;
        bind();
    }


    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        switch (nativeKeyEvent.getKeyCode()){
            case 29:
                ctrlKey = true;
                break;
            case 42:
                shiftKey = true;
                break;
            default:
                break;
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

        switch (nativeKeyEvent.getKeyCode()){
            case 25: // P or p
            case 46: // C or c
                if(ctrlKey && shiftKey){
                    Step s = new Step(1);
                    s.setImage(camera.shot());
                    if(onShot != null){
                        onShot.call(s);
                    }
                }
                break;
        }
    }


    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
    }


    @Override
    public void nativeMouseMoved(NativeMouseEvent nativeMouseEvent) {

    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent nativeMouseEvent) {

    }
}
