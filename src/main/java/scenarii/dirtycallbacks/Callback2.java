package scenarii.dirtycallbacks;

/**
 * Created by geoffrey on 24/05/2016.
 * Simulate a one-argument λ-expression
 */
public abstract class Callback2<A,B> {
    abstract public void call(A arg0, B arg1);
}
