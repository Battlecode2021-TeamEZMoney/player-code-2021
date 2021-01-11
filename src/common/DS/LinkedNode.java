package common.DS;

public class LinkedNode<T> {
    public LinkedNode next;
    public LinkedNode prev;
    public T val;
    public LinkedNode(T obj) {
        val = obj;
    }
}