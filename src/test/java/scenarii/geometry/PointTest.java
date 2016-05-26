package scenarii.geometry;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by geoffrey on 26/05/2016.
 */
public class PointTest {
    @Test
    public void relativeTo() throws Exception {
        Point origin = new Point(10,10);
        Point target = new Point(15,15);

        target.relativeTo(origin);

        assertEquals(5, target.getX(), 0.5);
        assertEquals(5, target.getY(), 0.5);

        target = new Point(5, 5);
        target.relativeTo(origin);

        assertEquals(-5, target.getX(), 0.5);
        assertEquals(-5, target.getY(), 0.5);
    }

    @Test
    public void delta() throws Exception{
        double origin = 10, target = 15;

        assertEquals(5, Point.delta(origin,target), 0.5);
        assertEquals(-5, Point.delta(target,origin), 0.5);


    }

    @Test
    public void scalarDistance() throws Exception{
        double origin = 10, target = 15;

        assertEquals(5, Point.scalarDistance(origin,target), 0.5);
        assertEquals(5, Point.scalarDistance(target,origin), 0.5);


    }

}