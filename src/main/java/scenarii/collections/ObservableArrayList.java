package scenarii.collections;

import scenarii.dirtycallbacks.Callback2;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by geoffrey on 13/07/2016.
 */
public class ObservableArrayList<T> extends ArrayList<T> {

    private BiConsumer<Integer, T> onAdd;
    private BiConsumer<Integer, T> onRemove;

    public void onAdd(BiConsumer<Integer, T> cb){
        onAdd = cb;
    }

    public void onRemove(BiConsumer<Integer, T> cb){
        onRemove = cb;
    }

    public void unshift(int index){
        T item = super.remove(index);
        super.add(index - 1,item);
    }

    public void shift(int index){
        T item = super.remove(index);
        super.add(index + 1,item);
    }

    @Override
    public boolean add(T t) {
        boolean success = super.add(t);
        if(success) onAdd.accept(size()-1, t);
        return success;
    }

    @Override
    public void add(int index, T element) {
        super.add(index, element);
        onAdd.accept(index,element);
    }

    @Override
    public T remove(int index) {
        T t = super.remove(index);
        onRemove.accept(index,t);
        return t;
    }
}
