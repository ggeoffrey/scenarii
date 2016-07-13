package scenarii.collections;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Created by geoffrey on 13/07/2016.
 */
public class SynchronisedThreadResultCollector<T> {

    private ObservableArrayList<T> valuesCollector;

    private int targetIndex;

    public SynchronisedThreadResultCollector(ObservableArrayList<T> valuesCollector) {
        this.valuesCollector = valuesCollector;
        this.targetIndex = valuesCollector.size();
    }

    public void rebaseIndex(ObservableArrayList o){
        targetIndex = o.size();
    }

    public void execute(T item, CompletableFuture<String> future, Consumer<String> callback){
        final int index = targetIndex;
        targetIndex++;

        valuesCollector.add(index, item);
        future.thenAccept(value -> callback.accept(value));
    }

    public ArrayList<T> getValuesCollector() {
        return valuesCollector;
    }


    public void setValuesCollector(ObservableArrayList<T> valuesCollector) {
        this.valuesCollector = valuesCollector;
    }
}
