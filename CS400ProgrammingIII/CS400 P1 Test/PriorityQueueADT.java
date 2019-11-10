

import java.util.NoSuchElementException;

// DO NOT EDIT THIS INTERFACE
public interface PriorityQueueADT<T extends Comparable<T>> {

	/** @return true if priority queue contains 0 items */
	boolean isEmpty();
	
	/** adds item to the priority queue */
	void insert(T p);	

	/** removes the max (highest value) priority value from the priority queue
	 * @return the element that was removed
	 * If no elements exists, it throws NoSuchElementException */
	T removeMax() throws NoSuchElementException;
	
	/** returns the highest priority value from the priority queue 
	 * without removing it from the priority queue.
	 * @return the element with highest priority
	 * If no elements exists, it throws NoSuchElementException */
	T getMax() throws NoSuchElementException;	
	
}

