package simpleplayer1_subm;

public class LinkedNode<T> {
    public LinkedNode<T> next;
    public LinkedNode<T> prev;
    public T val;
    public LinkedNode(T obj) {
        val = obj;
    }
}