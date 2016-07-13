package scenarii.collections;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * Created by geoffrey on 13/07/2016.
 */
public class SynchronisedThreadResultCollector<T> {

    private ArrayList<T> valuesCollector;

    private int targetIndex;

    public SynchronisedThreadResultCollector(ArrayList<T> valuesCollector) {
        this.valuesCollector = valuesCollector;
    }

    public void execute(CompletableFuture<T> future){
        final int index = targetIndex;
        targetIndex++;

        future.thenAccept((value)->{
            valuesCollector.add(index,value);
        });
    }


    public ArrayList<T> getValuesCollector() {
        return valuesCollector;
    }
}
