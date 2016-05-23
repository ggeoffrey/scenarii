package scenarii.camera;

import static org.junit.Assert.*;

/**
 * Created by geoffrey on 23/05/2016.
 */
public class BufferTest {


    private Buffer<Integer> getBuffer(){
        Buffer<Integer> sb = new Buffer<>();
        sb.add(1);
        sb.add(2);
        sb.add(3);
        sb.add(4);
        return sb;
    }

    @org.junit.Test
    public void add() throws Exception {
        Buffer<Integer> sb = getBuffer();
        assertEquals(3, sb.size());
    }

    @org.junit.Test
    public void getFirst() throws Exception {
        Buffer<Integer> sb = getBuffer();
        assertEquals(4, (int) sb.getFirst());
    }

    @org.junit.Test
    public void pop() throws Exception {
        Buffer<Integer> sb = getBuffer();
        assertEquals((int) sb.pop(), 4);
        assertEquals(2, sb.size());
    }


}