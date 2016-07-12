package scenarii.camera;

import scenarii.overlay.Overlay;

import java.awt.*;
import java.util.Timer;

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

    // Robot instance for screen captures
    private Robot robot;

    // A timer object for scheduling shots
    private Timer timer;

    // The recorder itself
    private Recorder recorder;

    // Allows unique shot names.
    private int shotCounter;

    // Camera's own thread
    private Runnable asyncTask;

    public Camera(Overlay overlay){
        build(overlay, 30);
    }

    public Camera(Overlay overlay, int fps) {
        build(overlay, fps);
    }


    /**
     * Generic builder
     * @param overlay
     * @param fps
     */
    private void build(Overlay overlay, int fps){
        this.overlay = overlay;
        this.shotCounter = 0;
        this.fps = fps;

        this.timer = new Timer(true);
        this.recorder = new Recorder(overlay, fpsToDelay(fps));
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
        recorder.stopRecording();
        recorder.exportShot();
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
