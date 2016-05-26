package scenarii.geometry;

import scenarii.Main;

/**
 * Created by geoffrey on 26/05/2016.
 */
public class Point extends java.awt.Point {

    public Point(int x, int y) {
        super(x, y);
    }

    public Point(java.awt.Point p) {
        super(p);
    }

    public void relativeTo(Point p){
        this.setLocation(
                (int) delta(p.getX(), this.getX()),
                (int) delta(p.getY(), this.getY())
        );
    }


    public static double delta(double a, double b){
        return b - a;
    }

    public static double scalarDistance(double a, double b){
        return Math.abs(delta(a,b));
    }
}
