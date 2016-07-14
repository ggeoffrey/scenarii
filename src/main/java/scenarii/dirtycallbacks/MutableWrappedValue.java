package scenarii.dirtycallbacks;

/**
 * Created by geoffrey on 14/07/2016.
 */
public class MutableWrappedValue<T> {
    T value;


    public MutableWrappedValue(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
