package scenarii.listeners;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.mouse.NativeMouseEvent;

import java.util.TreeSet;
import java.util.function.Consumer;

/**
 * Created by geoffrey on 24/05/2016.
 */
public class ShortcutListener extends NativeEventListener {

    private TreeSet<Integer> codes;

    public ShortcutListener(){
        codes = new TreeSet<>();
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        codes.add(nativeKeyEvent.getKeyCode());
        System.out.println(nativeKeyEvent.getKeyCode());
        System.out.println(codes);
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
        try {
            // wait some time to allow the java listener to
            // getCodes() before it's removed from the set
            Thread.sleep(50);
            codes.remove(nativeKeyEvent.getKeyCode());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public TreeSet<Integer> getCodes() {
        return codes;
    }

    public void bind(){
        super.bind();
    }

    public void unbind(){
        super.unbind();
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {}

    @Override
    public void nativeMouseMoved(NativeMouseEvent nativeMouseEvent) {}

    @Override
    public void nativeMouseDragged(NativeMouseEvent nativeMouseEvent) {}
}
