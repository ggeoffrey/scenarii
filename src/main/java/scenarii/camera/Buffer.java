package scenarii.camera;

import scenarii.dirtycallbacks.Callback1;


/**
 * Created by geoffrey on 23/05/2016.
 *
 * A two-way generic home-made LinkedList.
 * Behave as a list and as a stack.
 *
 * Insert is O(1)
 * Shift, unshift, push and pop are O(1)
 * Iteration is O(n)
 */
class Buffer<T> {

    // Size counter (cache)
    private int size;

    // with: left to right
    // first (left) node
    private Node head;

    // last (right) node
    private Node tail;

    Buffer() {
        this.size = 0;
    }

    /**
     * Inner description of a Node
     */
    private class Node{
        Node previous;
        Node next;
        T content;
    }

    /**
     * Add a T item at the beginning of the buffer
     * @param item
     */
    public void add(T item){

        // set new node contents
        Node n = new Node();
        n.content = item;

        // if the list is empty
        if(head == null)
            // make it the first item
            head = n;
        else{
            // insert as the new head
            n.next = head;
            head.previous = n;
            head = n;
        }

        // if no tail then the list was empty.
        // so assign it to this first node.
        if(tail == null)
            tail = n;

        // we have a new item in the list, so :
        size++;
    }

    public T getFirst(){
        return head.content;
    }

    /**
     * Return (and remove) the first element of the list (like a stack)
     * @return  First T element removed from the list.
     */
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


    /**
     * Empty this list.
     */
    public void clear() {
        // leave the dirty work to the Garbage collector.
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Iterate over the list, calling the given callback for each item.
     * @param consumer Callback to call with one T parameter.
     */
    public void forEach(Callback1<T> consumer){
        if(size > 0){
            Node n = tail;
            while (n != null){
                consumer.call(n.content);
                n = n.previous;
            }
        }
    }

}
