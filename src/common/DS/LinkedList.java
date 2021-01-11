package common.DS;

public class LinkedList<T> {
    public int size = 0;
    public LinkedNode head;
    public LinkedNode end;
    public LinkedList() {

    }
    public void add(T obj) {
        if (end != null) {
            LinkedNode newNode = new LinkedNode(obj);
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
    public LinkedNode dequeue() {
        if (this.size > 0) {
            LinkedNode removed = head;
            remove(head);
            this.size--;
            return removed;
        }
        return null;
    }
    public boolean contains(T obj) {
        LinkedNode node = head;
        while (node != null) {
            if (node.val.equals(obj)) {
                return true;
            }
            node = node.next;
        }
        return false;
    }
    public void remove(LinkedNode node) {
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
        LinkedNode node = head;
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