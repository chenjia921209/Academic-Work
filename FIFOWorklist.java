package edu.uwm.cs351.util;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public class FIFOWorklist<T> implements Worklist<T> { 
	//FIFO:The item that has been in the list longest is the highest priority item. = Queue

    // Queue符合 FIFO的規則（先進先出）
    private Queue<T> queue = new LinkedList<>();

    @Override
    public boolean hasNext() {
        return !queue.isEmpty();
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return queue.remove();
    }

    @Override
    public void add(T item) {
        queue.add(item);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is not supported.");
    }
}