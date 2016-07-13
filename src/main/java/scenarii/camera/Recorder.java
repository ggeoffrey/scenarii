package scenarii.camera;

import scenarii.overlay.Overlay;

import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * Created by geoffrey on 23/05/2016.
 * Managed by a camera to record shots, accumulate them and export them as a .gif .
 */
@Deprecated
class Recorder extends TimerTask {

    // The target field of view FOV
    private Overlay overlay;

    // Robot that allows screen shots
    private Robot robot;

    // Accumulator
    private Buffer<BufferedImage> buffer;

    // Idle?
    private boolean shouldRun;

    // How many shots? used to generate a unique name for each export.
    private static int shotCounter = 0;

    // Time between two frames
    private int delay;


    // A folder unique name (current clock time) to avoid files conflicts
    private long uniqueName;

    // Path where temporary shots should be stored.
    private String folderPath;

    // Last image's path (as a String).
    private String lastImageProduced;


    public Recorder(Overlay overlay, int delay){
        build(overlay, delay, new Date().getTime());
    }


    public Recorder(Overlay overlay, int delay, long uniqueName) {
        build(overlay, delay, uniqueName);
    }

    private void build(Overlay overlay, int delay, long uniqueName){
        this.uniqueName = uniqueName;

        // ensure folders are ready to store shots
        mkdir();

        this.overlay = overlay;
        this.delay = delay;
        Recorder.shotCounter = Math.max(0, shotCounter);
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if(shouldRun){
            // shot only if not idle.
            shot();
        }
    }

    /**
     * Create all necessary temporary folders at the right place.
     */
    private void mkdir(){
        String path = System.getProperty("user.home")+"/scenarii-snaps/";
        File folder = new File(path);

        if(!folder.exists())
            folder.mkdir();

        this.folderPath = path + this.uniqueName;

        File snapsFolder = new File(this.folderPath);

        if(!snapsFolder.exists())
            snapsFolder.mkdir();
    }

    /**
     * Take a screen shot, crop it according to the FOV and buffer it.
     */
    private void shot(){
        BufferedImage rawImage = getRawImage();
        BufferedImage cropped = cropImage(rawImage, overlay);
        this.buffer.add(cropped);
    }

    public String shotFullScreen(){
        BufferedImage rawImage = getRawImage();
        Buffer<BufferedImage> buffer = new Buffer<>();
        buffer.add(rawImage);
        exportShot(buffer, null);
        return lastImageProduced;
    }

    /**
     * Take a proper screen capture.
     * @return A bufferedImage of the screen area.
     */
    private BufferedImage getRawImage(){
        return robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
    }

    /**
     * Crop an image according to the FOV.
     * @param image Image to crop.
     * @param overlay FOV to match.
     * @return a smaller or equal image.
     */
    private BufferedImage cropImage(BufferedImage image, Overlay overlay){

        // Get the fov.
        Rectangle fov = overlay.getRect();

        // Lower bound is 0
        int x = fov.getX() < 0 ? 0 : (int) fov.getX();
        int y = fov.getY() < 0 ? 0 : (int) fov.getY();

        // Upper bound is x defined by the screen size

        // Compute the width while avoiding out-of-screen areas.
        int width = (int) fov.getWidth();
        if((x + width) > image.getWidth())
            width = image.getWidth() - x;

        // Same for the height
        int height = (int) fov.getHeight();
        if((y + height) > image.getHeight())
            height = image.getHeight() - y;

        // (3 . 4) px padding to avoid red rectangle blinks
        x += 3;
        y += 3;
        width -= 4;
        height -= 4;

        // Crop it
        return image.getSubimage(x,y,width,height);
    }


    /**
     * Start to record the screen.
     * You must call stopRecording to stop it.
     */
    public void record(){
        buffer = new Buffer<>();
        shouldRun = true;
    }

    /**
     * Stop the recording.
     */
    public void stopRecording(){
        shouldRun = false;
    }

    /**
     * Export buffer's content into a .gif file, in a temporary folder.
     */


    void exportShot(Consumer<String> callback){
        exportShot(this.buffer, callback);
    }

    private void exportShot(Buffer<BufferedImage> buffer, Consumer<String> callback){
        if(buffer != null && buffer.size() > 0){
            try {
                // ensure folder exists.
                mkdir();

                // make a name for the current image.
                // store it for the rest of the application.
                String lastImage = this.folderPath+"/shot-"+shotCounter+".gif";
                lastImageProduced = lastImage;

                ImageOutputStream output =
                        new FileImageOutputStream(new File(lastImageProduced));

                // Make a GifWriter with a bigger delay (by 4) seems appropriate.
                final GifSequenceWriter writer =
                        new GifSequenceWriter(output, buffer.getFirst().getType(), delay*4, true);

                buffer.forEach((bufferedImage)->{
                    try {
                        writer.writeToSequence(bufferedImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
				});

                if(callback != null) callback.accept(lastImage);

            } catch (IOException e) {
                e.printStackTrace();
            }

            shotCounter++;
            buffer = new Buffer<>();

            Runtime.getRuntime().gc();
        }
    }

    /**
     * Give mouse position on the screen.
     * @return mouse coordinates.
     */
    private Point getGlobalMousePosition(){
        return MouseInfo.getPointerInfo().getLocation();
    }

    private Point getLocalMousePosition(Overlay overlay){
        Point globalPos = getGlobalMousePosition();
        Point corner = overlay.getRect().getLocation();

        int xLocal = (int) (globalPos.getX() - corner.getX());
        int yLocal = (int) (globalPos.getY() - corner.getY());

        return new Point(xLocal, yLocal);
    }

    public boolean isRecording(){
        return shouldRun;
    }

    public String getLastImageProduced(){
        return lastImageProduced;
    }

    public long getUniqueName() {
        return uniqueName;
    }
}
