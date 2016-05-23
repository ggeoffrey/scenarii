package scenarii.camera;

import scenarii.overlay.Overlay;

import java.awt.*;
import java.util.Timer;

/**
 * Created by geoffrey on 23/05/2016.
 */
public class Camera extends Thread {

    private Overlay overlay;
    private int fps;
    private Robot robot;
    private Timer timer;
    private Recorder recorder;

    private int shotCounter;

    private Runnable asyncTask;

    public Camera(Overlay overlay){
        build(overlay, 1);
    }

    public Camera(Overlay overlay, int fps) {
        build(overlay, fps);
    }

    private void build(Overlay overlay, int fps){
        this.overlay = overlay;
        this.shotCounter = 0;
        this.fps = fps;

        this.timer = new Timer(true);
        this.recorder = new Recorder(overlay, fpsToDelay(fps));
        timer.scheduleAtFixedRate(recorder, 0, fpsToDelay(fps));

    }

    public void startRecord(){
        recorder.record();
    }
    public void stopRecord(){
        recorder.stopRecording();
        recorder.exportShot();
    }



    private int fpsToDelay(int fps){
        return (int) ((1./fps) * 1000);
    }


}
