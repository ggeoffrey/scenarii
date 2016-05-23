package scenarii.overlay;

import org.jnativehook.mouse.NativeMouseEvent;
import scenarii.geometry.Point;
import scenarii.geometry.Rect;

/**
 * Created by geoffrey on 23/05/2016.
 */
public class Overlay {
    private OverlaySection top;
    private OverlaySection bottom;

    public Overlay() {
        this.top = new OverlaySection(SectionOrientation.TOP);
        this.bottom = new OverlaySection(SectionOrientation.BOTTOM);
    }

    public void setPosition(NativeMouseEvent event){
        setPosition(event.getX(), event.getY());
    }

    public void setPosition(double x, double y){
        top.setPosition(x,y);
        bottom.setPosition(x,y);
    }

    public void show(){
        top.show();
        bottom.show();
    }

    public void hide(){
        top.hide();
        bottom.hide();
    }


    public void distort(double xOrigin, double yOrigin, double xCurrent, double yCurrent){

        double deltaX = computeDelta(xOrigin, xCurrent);
        double deltaY = computeDelta(yOrigin, yCurrent);


        top.alterWidth(deltaX);
        bottom.alterWidth(deltaX);
        top.alterHeight(deltaY);
        bottom.alterHeight(deltaY);

        setPosition(xOrigin,yOrigin);
    }


    private double computeDelta(double origin, double current){
        if(current < origin)
            return - Math.log(origin - current);
        else
            return Math.log(current - origin);
    }

    public Rect getRect(){
        return new Rect(top.getX(),top.getY(), top.getWidth(), top.getHeight() * 2 + 1);
    }

}
