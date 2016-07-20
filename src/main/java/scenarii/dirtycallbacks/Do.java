package scenarii.dirtycallbacks;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by geoffrey on 20/07/2016.
 */
public class Do {

    public static void after(int delay, Callback callback){
        new Thread(()->{
            try {
                Thread.sleep(delay);
                callback.accept();
            } catch (InterruptedException e) {
                System.err.println("ERROR: for some strange reason Thread.sleep was interrupted.");
                System.err.println("       (Do::after) ==>");
                System.err.println(e.getMessage());
            }
        }).start();
    }

    public static Timer every(int delay, int period, Callback callback){
        final Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                callback.accept();
            }
        }, delay, period);
        return timer;
    }

    public static Timer every(int delay, Callback callback){
        return every(0, delay, callback);
    }
}
