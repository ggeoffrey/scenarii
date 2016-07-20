package scenarii.listeners;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.mouse.NativeMouseEvent;
import scenarii.camera.Camera;
import scenarii.dirtycallbacks.Callback;
import scenarii.dirtycallbacks.Do;
import scenarii.model.Step;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Consumer;

/**
 * Created by geoffrey on 24/05/2016.
 */
public class SimpleShotListener extends NativeEventListener {

    private final Camera camera;

    private HashSet<Integer> shortcut1;
    private HashSet<Integer> shortcut2;
    private HashSet<Integer> currentShortcut;

    // Callbacks

    private final Consumer<Step> onShot;
    private final Callback onError;


    public SimpleShotListener(Camera camera, Consumer<Step> onShot, Callback onError) {
        this.camera = camera;
        this.onShot = onShot;
        this.onError = onError;

        HashSet<Integer> base = new HashSet<Integer>(){{
            add(29);
            add(42);
        }};
        shortcut1 = new HashSet<Integer>(){{addAll(base); add(46);}};
        shortcut2 = new HashSet<Integer>(){{addAll(base); add(25);}};

        currentShortcut = new HashSet<>();
        bind();
    }


    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        currentShortcut.add(nativeKeyEvent.getKeyCode());
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

        boolean matches = shortcut1.equals(currentShortcut)
                            || shortcut2.equals(currentShortcut);

        if(matches) { Step s = new Step();
            try{
                s.setImage(camera.singleShot());
                if(onShot != null)
                    onShot.accept(s);
            } catch (IOException e){
                System.err.println("ERROR: Unable to take a simple 1 frame shot ==>");
                System.err.println(e.getMessage());
                if(onError != null)
                    onError.accept();
            }
        }

        currentShortcut.remove(nativeKeyEvent.getKeyCode());
    }

    public void setShortcut1(HashSet<Integer> shortcut1) {
        Do.after(250, ()-> this.shortcut1 = shortcut1);
    }

    public void setShortcut2(HashSet<Integer> shortcut2) {
        Do.after(250, ()-> this.shortcut2 = shortcut2);
    }


    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {}

    @Override
    public void nativeMouseMoved(NativeMouseEvent nativeMouseEvent) {}

    @Override
    public void nativeMouseDragged(NativeMouseEvent nativeMouseEvent) {}
}
