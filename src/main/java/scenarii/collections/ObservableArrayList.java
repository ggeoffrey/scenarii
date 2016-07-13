package scenarii.collections;

import scenarii.dirtycallbacks.Callback2;

import java.util.ArrayList;

/**
 * Created by geoffrey on 13/07/2016.
 */
public class ObservableArrayList<T> extends ArrayList<T> {

    private Callback2<Integer, T> onAdd;
    private Callback2<Integer, T> onRemove;

    public void onAdd(Callback2<Integer, T> cb){
        onAdd = cb;
    }

    public void onRemove(Callback2<Integer, T> cb){
        onRemove = cb;
    }

    @Override
    public boolean add(T t) {
        boolean success = super.add(t);
        if(success) onAdd.call(size()-1, t);
        return success;
    }

    @Override
    public void add(int index, T element) {
        super.add(index, element);
        onAdd.call(index,element);
    }

    @Override
    public T remove(int index) {
        T t = super.remove(index);
        onRemove.call(index,t);
        return t;
    }
}
