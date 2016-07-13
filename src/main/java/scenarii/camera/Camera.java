package scenarii.camera;

import scenarii.dirtycallbacks.Callback1;
import scenarii.overlay.Overlay;

import java.awt.*;
import java.util.Timer;
import java.util.function.Consumer;

/**
 * Created by geoffrey on 23/05/2016.
 *
 * Describe the camera, drawing the rectangle,
 * managing the FOV and the recorder.
 */
public class Camera extends Thread {

    // JavaFx panels
    private Overlay overlay;

    // Default Frames/second
    private int fps;


    // A timer object for scheduling shots
    private Timer timer;

    // The recorder itself
    private Recorder recorder;


    public Camera(Overlay overlay){
        build(overlay, 30, null);
    }

    public Camera(Camera camera){
        build(camera.overlay, 30, camera.recorder.getUniqueName());
    }


    /**
     * Generic builder
     * @param overlay
     * @param fps
     */
    private void build(Overlay overlay, int fps, Long uniqueName){
        this.overlay = overlay;
        this.fps = fps;

        this.timer = new Timer(true);

        if(uniqueName == null)
            this.recorder = new Recorder(overlay, fpsToDelay(fps));
        else
            this.recorder = new Recorder(overlay, fpsToDelay(fps), uniqueName);

        timer.scheduleAtFixedRate(recorder, 0, fpsToDelay(fps));
    }

    /**
     * Make the camera record a shot sequence.
     * Camera.stopRecord must be called to stop it.
     */
    public void startRecord(){
        recorder.record();
    }

    /**
     * Stop the shots sequence recording and export the result
     * to a temporary file.
     */
    public void stopRecord(){
        stopRecord(null);
    }

    public void stopRecord(Consumer<String> callback){
        recorder.stopRecording();
        recorder.exportShot(callback);
    }






    /**
     * Transform fps to millisec interval.
     * @param fps
     * @return time between two frames
     */
    private int fpsToDelay(int fps){
        return (int) ((1./fps) * 1000);
    }


    public boolean isRecording(){
        return recorder.isRecording();
    }



    public void reset(){
        timer.cancel();
        recorder = new Recorder(overlay, fpsToDelay(fps));
        timer.scheduleAtFixedRate(recorder, 0, fpsToDelay(fps));
    }

    /**
     * Get the last camera exported image.
     * @return
     */
    public String getLastImageProduced(){
        return recorder.getLastImageProduced();
    }

    /**
     * Take a fullscreen capture.
     * @return Path to the resulting image.
     */
    public String shot(){
       return recorder.shotFullScreen();
    }

}
