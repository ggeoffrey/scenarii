package scenarii.camera;

import scenarii.dirtycallbacks.Callback1;
import scenarii.overlay.Overlay;

import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.TimerTask;

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

    private long uniqueName;
    private String folderPath;

    private String lastImageProduced;

    public Recorder(Overlay overlay, int delay) {
        this.uniqueName = new Date().getTime();

        mkdir();

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

    private void mkdir(){
        String path = System.getProperty("user.home")+"/scenarii-snaps/";
        File folder = new File(path);
        if(!folder.exists()){
            folder.mkdir();
        }
        this.folderPath = path + this.uniqueName;

        File snapsFolder = new File(this.folderPath);

        if(!snapsFolder.exists())
            snapsFolder.mkdir();

    }

    private void shot(){
        BufferedImage rawImage;
        rawImage = getRawImage();
        BufferedImage cropped = cropImage(rawImage, overlay);
        this.buffer.add(cropped);
    }


    private BufferedImage getRawImage(){
        return robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
    }

    private BufferedImage cropImage(BufferedImage image, Overlay overlay){
        Rectangle fov = overlay.getRect();

        int x = fov.getX() < 0 ? 0 : (int) fov.getX();
        int y = fov.getY() < 0 ? 0 : (int) fov.getY();

        int width = (int) fov.getWidth();
        if((x + width) > image.getWidth())
            width = image.getWidth() - x;

        int height = (int) fov.getHeight();
        if((y + height) > image.getHeight())
            height = image.getHeight() - y;

        // 1 px padding to avoid red rectangle blinks
        x += 3;
        y += 3;
        width -= 4;
        height -= 4;

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
        if(buffer != null && buffer.size() > 0){
            try {

                mkdir();

                lastImageProduced = this.folderPath+"/shot-"+shotCounter+".gif";
                ImageOutputStream output =
                        new FileImageOutputStream(new File(lastImageProduced));

                final GifSequenceWriter writer =
                        new GifSequenceWriter(output, this.buffer.getFirst().getType(), delay*4, true);

                this.buffer.forEach(new Callback1<BufferedImage>() {
					@Override
					public void call(BufferedImage bufferedImage) {
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

}
