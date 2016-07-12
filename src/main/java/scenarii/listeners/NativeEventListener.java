package scenarii.listeners;

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
import scenarii.controllers.State;
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
public abstract class NativeEventListener implements NativeMouseMotionListener, NativeKeyListener {

    @Override
    protected void finalize() throws Throwable {
        unbind();
        super.finalize();
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
