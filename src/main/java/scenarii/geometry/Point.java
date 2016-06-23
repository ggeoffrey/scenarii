package scenarii.geometry;

import scenarii.Main;

/**
 * Created by geoffrey on 26/05/2016.
 * A point (x,y)
 */
public class Point extends java.awt.Point {

    public Point(int x, int y) {
        super(x, y);
    }

    public Point(java.awt.Point p) {
        super(p);
    }

    /**
     * Translate this point to make it's coordinates relative to another
     * point rather than to (0,0)
     * @param p
     */
    public void relativeTo(Point p){
        this.setLocation(
                (int) delta(p.getX(), this.getX()),
                (int) delta(p.getY(), this.getY())
        );
    }


    /**
     * Δ beetween a & b.
     * @param a
     * @param b
     * @return
     */
    public static double delta(double a, double b){
        return b - a;
    }

    /**
     * Absolute distance between a & b.
     * @param a
     * @param b
     * @return a positive number.
     */
    public static double scalarDistance(double a, double b){
        return Math.abs(delta(a,b));
    }
}
