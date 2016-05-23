package scenarii.geometry;

import java.awt.*;

/**
 * Created by geoffrey on 23/05/2016.
 */
public class Rect {

    private Point origin;
    private double width;
    private double height;

    public Rect(Point origin, double width, double height) {
        this.origin = origin;
        this.width = width;
        this.height = height;
    }

    public Rect(double x, double y, double width, double height) {
        this.origin = new Point(x,y);
        this.width = width;
        this.height = height;
    }

    public Point getOrigin() {
        return origin;
    }

    public void setOrigin(Point origin) {
        this.origin = origin;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public Rectangle toAWTRectangle(){
        return new Rectangle(
                (int) origin.getX(),
                (int) origin.getY(),
                (int) width,
                (int) height);
    }


    @Override
    public String toString() {
        return (int) origin.getX()+":"+ (int) origin.getY()+" x "+ (int) width+":"+ (int) height;
    }
}
