package scenarii.listeners;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.SwingDispatchService;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseMotionListener;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by geoffrey on 24/05/2016.
 */
public abstract class NativeEventListener implements NativeMouseMotionListener, NativeKeyListener {

    @Override
    protected void finalize() throws Throwable {
        unbind();
        unbindGlobal();
        super.finalize();
    }

    public static void bindGlobal(){
        try{
            LogManager.getLogManager().reset();
            Logger.getLogger(GlobalScreen.class.getPackage().getName())
                    .setLevel(Level.WARNING);
            GlobalScreen.registerNativeHook();
            GlobalScreen.setEventDispatcher(new SwingDispatchService());

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void unbindGlobal(){
        try {
            GlobalScreen.unregisterNativeHook();
        }
        catch (NativeHookException e){
            e.printStackTrace();
        }
    }


    protected void bind(){
        GlobalScreen.addNativeMouseMotionListener(this);
        GlobalScreen.addNativeKeyListener(this);
    }

    protected void unbind(){
        GlobalScreen.removeNativeKeyListener(this);
        GlobalScreen.removeNativeMouseMotionListener(this);
    }

}
