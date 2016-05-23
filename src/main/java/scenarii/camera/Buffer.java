package scenarii.camera;

import javafx.scene.Node;

import java.util.function.Consumer;

/**
 * Created by geoffrey on 23/05/2016.
 */
public class Buffer<T> {

    private int size;

    private Node head;
    private Node tail;

    public Buffer() {
        this.size = 0;
    }

    private class Node{
        Node previous;
        Node next;
        T content;
    }

    public void add(T item){

        Node n = new Node();
        n.content = item;

        if(head == null)
            head = n;
        else{
            n.next = head;
            head.previous = n;
            head = n;
        }

        if(tail == null)
            tail = n;

        size++;
    }

    public T getFirst(){
        return head.content;
    }

    public T pop(){
        if(head != null){
            T item = getFirst();
            head = head.next;
            head.previous = null;
            size--;
            return item;
        }
        return null;
    }


    public int size(){
        return size;
    }


    public void clear(){
        head = null;
        tail = null;
        size = 0;
    }

    public void forEach(Consumer<T> consumer){
        if(size > 0){
            Node n = tail;
            while (n != null){
                consumer.accept(n.content);
                n = n.previous;
            }
        }
    }

}
