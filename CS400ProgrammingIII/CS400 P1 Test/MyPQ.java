/**
 * Filename:   MyPQ.java
 * Project:    p1TestPQ
 * Version:    1.0
 * User:       deppeler
 * Date:       Aug 29, 2018
 * Authors:    Debra Deppeler
 *
 * Semester:   Fall 2018
 * Course:     CS400
 * Instructor: Deppeler (deppeler@cs.wisc.edu)
 * Credits:    n/a
 * Bugs:       no known bugs, but not complete either
 *
 * Due Date:   before 10:00 pm on September 17th
 */


import java.util.NoSuchElementException;

/** TODO: add class header comments here
 * @param <T>
 *
 */
public class MyPQ<T extends Comparable<T>> implements PriorityQueueADT<T> {

	/* (non-Javadoc)
	 * @see p1.PriorityQueueADT#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see p1.PriorityQueueADT#insert(java.lang.Comparable)
	 */
	@Override
	public void insert(T p) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see p1.PriorityQueueADT#removeMax()
	 */
	@Override
	public T removeMax() throws NoSuchElementException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see p1.PriorityQueueADT#getMax()
	 */
	@Override
	public T getMax() throws NoSuchElementException {
		// TODO Auto-generated method stub
		return null;
	}

}
