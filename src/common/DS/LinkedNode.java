package common.DS;

public class LinkedNode<T> {
    public LinkedNode<T> next;
    public LinkedNode<T> prev;
    public T val;
    public LinkedNode(T obj) {
        val = obj;
    }
}