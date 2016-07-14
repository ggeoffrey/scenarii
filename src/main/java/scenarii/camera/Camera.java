package scenarii.camera;

import scenarii.dirtycallbacks.Callback;
import scenarii.dirtycallbacks.MutableWrappedValue;
import scenarii.overlay.Overlay;

import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * Created by geoffrey on 14/07/2016.
 */
public class Camera {

    private Overlay overlay;
    private long uniqueName;
    private String folderPath;
    private static int shotCounter = 0;
    private boolean isRecording;
    private int delay;

    private Consumer<String> onGifGenerated;
    private Robot robot;


    public Camera(Overlay overlay){
        this(overlay, new Date().getTime(), 30);
    }

    public Camera(Camera camera){
        this(camera.overlay, camera.uniqueName, 30);
    }

    public Camera(Overlay overlay, long uniqueName, int fps) {
        this.overlay = overlay;
        this.uniqueName = uniqueName;
        this.folderPath = System.getProperty("user.home")+"/scenarii-snaps/"+uniqueName;
        shotCounter = Math.max(0, shotCounter);
        delay = fpsToDelay(fps);

        isRecording = false;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public Callback record(Consumer<String> callback){

        Callback stopper = ()->{};

        if(isRecording)
            System.err.println("WARNING: Tried to record more than one sequence at a time (Camera::record).");
        else{
            isRecording = true;
            String path = getGifName();
            GifSequenceWriter writer = initGifWrite(delay, path).orElse(null);
            if(writer != null){
                final Timer timer = new Timer(true);
                boolean shouldStop = false;
                MutableWrappedValue<Integer> seqencesCount = new MutableWrappedValue<>(0);
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        BufferedImage cropped = cropImage(getRawImage(), overlay);
                        try {
                            writer.writeToSequence(cropped);
                            seqencesCount.set(seqencesCount.get()+1);
                        } catch (IOException e) {
                            System.err.println("ERROR: unable to write frame to current gif file");
                            System.err.println("       at (Camera::record::timertask.run)  ==>");
                            System.err.println(e.getMessage());
                        }
                    }
                }, 0, delay);


                stopper = ()->{
                    timer.cancel();
                    new Thread(()->{
                        try {
                            Thread.sleep(250);
                            writer.close();
                        } catch (IOException e) {
                            System.err.println("ERROR: unable to close the writer after writing a frame sequence");
                            System.err.println("       of "+ seqencesCount.get() +" frames. ");
                            System.err.println("       (Camera::record::λ.stopper) ==>");
                            System.err.println(e.getMessage());
                        }
                        catch (InterruptedException e){
                            System.err.println("ERROR: for some strange reason Thread.sleep was interrupted.");
                            System.err.println("       (Camera::record::λ.stopper) ==>");
                            System.err.println(e.getMessage());
                        }
                    }).start();

                    isRecording = false;
                    callback.accept(path);
                };
            }
        }
        return stopper;
    }

    public String singleShot() throws IOException {
        String imagePath = getGifName();
        GifSequenceWriter writer = initGifWrite(delay, imagePath).orElse(null);
        if(writer != null){
            writer.writeToSequence(getRawImage());
            writer.close();
        }
        return imagePath;
    }


    /**
     * Transform fps to millisecond interval.
     * @param fps
     * @return time between two frames
     */
    private int fpsToDelay(int fps){
        return (int) ((1./fps) * 1000);
    }

    public boolean isRecording() {
        return isRecording;
    }



    // ------ TOOLS ------


    private String getGifName(){
        // make a name for the current image.
        String path = this.folderPath+"/shot-"+shotCounter+".gif";
        shotCounter++;
        return path;
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


    private Optional<GifSequenceWriter> initGifWrite(int delay, String path){
        ImageOutputStream output = null;
        GifSequenceWriter writer = null;
        Optional<GifSequenceWriter> returnValue = null;
        boolean somethingBadHappend = false;
        try{
            // ensure folder exists.
            mkdir();
            output = new FileImageOutputStream(new File(path));

            // Make a GifWriter with a bigger delay (by 4) seems appropriate.
            writer = new GifSequenceWriter(output, BufferedImage.TYPE_INT_RGB, delay*4, true);

            returnValue = Optional.of(writer);

        }catch (IOException e){
            System.err.println("ERROR: unable to init gif writing (Camera::initGifWrite) ==> ");
            System.err.println(e.getMessage());
            returnValue = Optional.empty();
            somethingBadHappend = true;
        }
        finally {
            if(somethingBadHappend) {
                try {
                    if (output != null)
                        output.close();
                    if (writer != null)
                        writer.close();
                } catch (IOException e) {
                    System.err.println("ERROR: unable to close streams on aborted (Camera::initGifWrite) ==>");
                    System.err.println(e.getMessage());
                }
            }
        }
        return returnValue;
    }
}
