package scenarii.overlay;

import org.jnativehook.mouse.NativeMouseEvent;


import java.awt.*;

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

    public void showForDistort(){
        top.showForDistort();
        bottom.showForDistort();
    }

    public void hideForDistort(){
        top.hideForDistort();
        bottom.hideForDistort();
    }


    public void distort(double xOrigin, double yOrigin, double xCurrent, double yCurrent){

        double deltaX = computeDelta(xOrigin, xCurrent);
        double deltaY = computeDelta(yOrigin, yCurrent);

        if(deltaX > 2.5 || deltaX < -2.5){
            top.alterWidth(deltaX);
            bottom.alterWidth(deltaX);
        }
        if(deltaY > 2.5 || deltaY < -2.5){
            top.alterHeight(deltaY);
            bottom.alterHeight(deltaY);
        }
        setPosition(xOrigin,yOrigin);
    }


    private double computeDelta(double origin, double current){
        if(current < origin)
            return - Math.log(origin - current);
        else
            return Math.log(current - origin);
    }

    public Rectangle getRect(){
        return new Rectangle(
                (int) top.getX(),
                (int) top.getY(),
                (int) top.getWidth(),
                (int) top.getHeight() * 2 + 1);
    }

}
