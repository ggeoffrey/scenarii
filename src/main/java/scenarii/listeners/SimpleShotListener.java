package scenarii.listeners;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.mouse.NativeMouseEvent;
import scenarii.camera.Camera;
import scenarii.dirtycallbacks.Callback;
import scenarii.model.Step;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Created by geoffrey on 24/05/2016.
 */
public class SimpleShotListener extends NativeEventListener {

    private final Camera cameraOld;

    private boolean ctrlKey;
    private boolean shiftKey;


    // Callbacks

    private final Consumer<Step> onShot;
    private final Callback onError;



    public SimpleShotListener(Camera cameraOld, Consumer<Step> onShot, Callback onError) {
        this.cameraOld = cameraOld;
        this.onShot = onShot;
        this.onError = onError;
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
                    Step s = new Step();
                    try{
                        s.setImage(cameraOld.singleShot());
                        if(onShot != null)
                            onShot.accept(s);
                    } catch (IOException e){
                        System.err.println("ERROR: Unable to take a simple 1 frame shot ==>");
                        System.err.println(e.getMessage());
                        if(onError != null)
                            onError.accept();
                    }
                }
                break;
            case 29:
                ctrlKey = false;
                break;
            case 42:
                shiftKey = false;
                break;
        }
    }


    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {}

    @Override
    public void nativeMouseMoved(NativeMouseEvent nativeMouseEvent) {}

    @Override
    public void nativeMouseDragged(NativeMouseEvent nativeMouseEvent) {}
}
