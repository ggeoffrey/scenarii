package scenarii.overlay;

import org.jnativehook.mouse.NativeMouseEvent;




import java.awt.*;

import javafx.application.Platform;
import javafx.stage.Stage;
import scenarii.geometry.Point;

/**
 * Created by geoffrey on 23/05/2016.
 */
public class Overlay {
	
	private static boolean alwaysOnTopSupported;
	
    private final OverlaySection top;
    private final OverlaySection bottom;

    private boolean displayCenterCircle;

    public Overlay() {
        this.top = new OverlaySection(SectionOrientation.TOP);
        this.bottom = new OverlaySection(SectionOrientation.BOTTOM);
        alwaysOnTopSupported = supportAlwaysOnTop();
        displayCenterCircle = true;
    }

    public void setPosition(NativeMouseEvent event){
        setPosition(event.getX(), event.getY());
    }

    private void setPosition(double x, double y){
        top.setPosition(x,y);
        bottom.setPosition(x,y);
    }

    public void show(){
        Platform.runLater(()->{
            top.show();
            bottom.show();
        });
    }



    public void hide(){
        Platform.runLater(()->{
            top.hide();
            bottom.hide();
        });
    }

    /*public void hideBorder(){
        top.hideBorder();
        bottom.hideBorder();
        if (!displayCenterCircle) {
            top.hideCircle();
            bottom.hideCircle();
        }
    }*/

    public void showBorder(){
        top.showBorder();
        bottom.showBorder();
        if (!displayCenterCircle) {
            top.showCircle();
            bottom.showCircle();
        }
    }

    public void hideBorder(){
    }



    public void distort(double xOrigin, double yOrigin, double xCurrent, double yCurrent){

        double deltaX = Math.max(30., Point.scalarDistance(xOrigin, xCurrent)*2);
        double deltaY = Math.max(30., Point.scalarDistance(yOrigin, yCurrent)*2);

        top.setWidth(deltaX);
        bottom.setWidth(deltaX);

        top.setHeight(deltaY);
        bottom.setHeight(deltaY);

        setPosition(xOrigin,yOrigin);
    }

    public void close(){
        top.close();
        bottom.close();
    }


    public Rectangle getRect(){
        return new Rectangle(
                (int) top.getX(),
                (int) top.getY(),
                (int) top.getWidth(),
                (int) top.getHeight() * 2 + 4);
    }

    public Point getCenter(){
        return new Point((int) top.getWidth()/2, (int) (top.getHeight()*2+4)/2);
    }

    public Point getPosition(){
        return top.getPosition();
    }
    
    
    public void toFront(){
    	if(!alwaysOnTopSupported){
    		Platform.runLater(() -> {
                top.toFront();
                bottom.toFront();
            });
    	}
    }
    
    
    private boolean supportAlwaysOnTop(){
    	boolean supported = true;
    	try{
    		Stage.class.getMethod("setAlwaysOnTop", boolean.class);
    	}
    	catch(Exception e){
    		supported = false;
    	}
    	return supported;
    }


    public void setDisplayCenterCircle(boolean displayCenterCircle) {
        this.displayCenterCircle = displayCenterCircle;
    }
}
