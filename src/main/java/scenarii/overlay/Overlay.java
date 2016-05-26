package scenarii.overlay;

import org.jnativehook.mouse.NativeMouseEvent;


import java.awt.*;
import scenarii.geometry.Point;

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

}
