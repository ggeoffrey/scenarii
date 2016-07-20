package scenarii.listeners;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.mouse.NativeMouseEvent;
import scenarii.dirtycallbacks.Do;

import java.util.HashSet;

/**
 * Created by geoffrey on 24/05/2016.
 */
public class ShortcutListener extends NativeEventListener {

    private HashSet<Integer> codes;

    public ShortcutListener(){
        codes = new HashSet<>();
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        codes.add(nativeKeyEvent.getKeyCode());
        System.out.println(nativeKeyEvent.getKeyCode());
        System.out.println(codes);
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
        Do.after(200, ()-> codes.remove(nativeKeyEvent.getKeyCode()));
    }

    public HashSet<Integer> getCodes() {
        return new HashSet<>(codes);
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
