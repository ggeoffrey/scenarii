package scenarii.camera;

import scenarii.overlay.Overlay;

import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * Created by geoffrey on 23/05/2016.
 */
public class Recorder extends TimerTask {

    private Overlay overlay;
    private Robot robot;
    private Buffer<BufferedImage> buffer;

    private boolean shouldRun;

    private int shotCounter;
    private int delay;

    public Recorder(Overlay overlay, int delay) {
        this.overlay = overlay;
        this.delay = delay;
        this.shotCounter = 0;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if(shouldRun){
            shot();
        }
    }

    private void shot(){
        BufferedImage rawImage;
        rawImage = getRawImage();

        BufferedImage cropped = cropImage(rawImage, overlay);
        this.buffer.add(cropped);

        //try {
        //    ImageIO.write(cropped, "png", new File("./shots/shot-"+shotCounter+".png"));
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}


        System.out.println("SHOT: " + this.shotCounter);
    }


    private BufferedImage getRawImage(){
        return robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
    }

    private BufferedImage cropImage(BufferedImage image, Overlay overlay){
        Rectangle fov = overlay.getRect().toAWTRectangle();

        int x = fov.getX() < 0 ? 0 : (int) fov.getX();
        int y = fov.getY() < 0 ? 0 : (int) fov.getY();

        int width = (int) fov.getWidth();
        if((x + width) > image.getWidth())
            width = (x + width) - image.getWidth();

        int height = (int) fov.getHeight();
        if((y + height) > image.getHeight())
            height = (y + height) - image.getHeight();

        return image.getSubimage(x,y,width,height);
    }


    public void record(){
        buffer = new Buffer<BufferedImage>();
        shouldRun = true;
    }

    public  void stopRecording(){
        shouldRun = false;
    }

    protected void exportShot(){
        if(buffer.size() > 0){
            try {
                ImageOutputStream output =
                        new FileImageOutputStream(new File("./shots/shot-"+shotCounter+"-"+delay+".gif"));

                GifSequenceWriter writer =
                        new GifSequenceWriter(output, this.buffer.getFirst().getType(), delay*3, false);

                this.buffer.forEach(new Consumer<BufferedImage>() {
                    @Override
                    public void accept(BufferedImage bufferedImage) {
                        try {
                            writer.writeToSequence(bufferedImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });


            } catch (IOException e) {
                e.printStackTrace();
            }

            this.shotCounter++;
            this.buffer.clear();
        }
    }

}
