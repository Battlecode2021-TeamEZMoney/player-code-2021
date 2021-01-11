package common.DS;

public class LinkedList<T> {
    public int size = 0;
    public LinkedNode<T> head;
    public LinkedNode<T> end;
    public LinkedList() {

    }
    public void add(T obj) {
        if (end != null) {
            LinkedNode<T> newNode = new LinkedNode(obj);
            newNode.prev = end;
            end.next = newNode;
            end = newNode;
        }
        else {
            head = new LinkedNode(obj);
            end = head;
        }
        size++;
    }
    public LinkedNode<T> dequeue() {
        if (this.size > 0) {
            LinkedNode<T> removed = head;
            remove(head);
            this.size--;
            return removed;
        }
        return null;
    }
    public boolean contains(T obj) {
        LinkedNode<T> node = head;
        while (node != null) {
            if (node.val.equals(obj)) {
                return true;
            }
            node = node.next;
        }
        return false;
    }
    public void remove(LinkedNode<T> node) {
        if (node.prev != null) {
            node.prev.next = node.next;
        }
        else {
            // deal with head
            head = node.next;
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        }
        else {
            end = node.prev;
        }
        node = null;
        size--;
    }
    public boolean remove(T obj) {
        LinkedNode<T> node = head;
        while (node != null) {
            if (node.val.equals(obj)) {
                remove(node);
                return true;
            }
            node = node.next;
        }
        return false;
    }

}