package scenarii.listeners;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.mouse.NativeMouseEvent;
import scenarii.camera.Camera;
import scenarii.model.Step;

import java.util.function.Consumer;

/**
 * Created by geoffrey on 24/05/2016.
 */
public class SimpleShotListener extends NativeEventListener {

    private final Camera camera;

    private boolean ctrlKey;
    private boolean shiftKey;


    // Callbacks

    private final Consumer<Step> onShot;



    public SimpleShotListener(Camera camera, Consumer<Step> onShot) {
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
                        onShot.accept(s);
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
