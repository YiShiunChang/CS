//////////////////// ALL ASSIGNMENTS INCLUDE THIS SECTION /////////////////////
//
// Title:           ASSIGNMENT 10
// Files:           ProcessScheduler.java WaitingQueueADT.java 
//                  CustomProcessQueue.java ProcessSchedulerTests.java
//                  CustomProcess.java
// Course:          CS300, Fall 2018
//
// Author:          Shuo Han
// Email:           shan238@wisc.edu
// Lecturer's Name: Gary Dahl
//
//////////////////// PAIR PROGRAMMERS COMPLETE THIS SECTION ///////////////////
//
// Partner Name:    Yi-Shiun Chang
// Partner Email:   chang242@wisc.edu
// Partner Lecturer's Name: Gary Dahl
// 
// VERIFY THE FOLLOWING BY PLACING AN X NEXT TO EACH TRUE STATEMENT:
//   _X_ Write-up states that pair programming is allowed for this assignment.
//   _X_ We have both read and understand the course Pair Programming Policy.
//   _X_ We have registered our team prior to the team registration deadline.
//
///////////////////////////// CREDIT OUTSIDE HELP /////////////////////////////
//
// Students who get help from sources other than their partner must fully 
// acknowledge and credit those sources of help here.  Instructors and TAs do 
// not need to be credited here, but tutors, friends, relatives, room mates, 
// strangers, and others do.  If you received no outside help from either type
//  of source, then please explicitly indicate NONE.
//
// Persons:         NONE
// Online Sources:  NONE
//
/////////////////////////////// 80 COLUMNS WIDE ///////////////////////////////
/**
 * You can change the value of INITIAL_CAPACITY if you would like, 
 * but note that your heap MUST be an array-based implementation from the scratch. 
 * 
 * You are NOT allowed to import java.util.ArrayList class 
 * or any other ArrayList-based implementation.
 * The heap MUST array-based MIN-heap. 
 * It will be implemented such that the ROOT node is 
 * ALWAYS the entry at index 1 in the array. 
 * This means that index 0 should be unused and the process 
 * having the highest priority is always 
 * stored at index 1 of your array heap.
 * 
 * Unused indexes (0, plus any past the current size of your heap) should contain null.
 * 
 * Your heap array should expand to fit all the processes being enqueued. 
 * You can devise your dynamic heap by simply doubling its size whenever it is full. 
 * 
 * One simple way to do so is to copy all elements into a new 2x-sized array when you 
 * run out of space. 
 * We note that implementing a shadow array to expand the size of your heap array is 
 * not required by this assignment.
 * 
 * Your CustomProcessQueue class contains ONLY ONE no-argument constructor that 
 * creates an empty CustomProcessQueue.
 * enqueue() and peek() methods SHOULD return null if they are called on an 
 * empty CustomProcessQueue.
 * 
 * In addition to implementing ALL the methods defined in the WaitingQueueADT interface, 
 * your CustomProcessQueue must have the following two private methods:
 * @author baliansnow
 *
 */
public class CustomProcessQueue implements WaitingQueueADT<CustomProcess> {
  // the initial capacity of the heap
  private static final int INITIAL_CAPACITY = 20; 
  // array-based min heap storing the data, and this is an oversize array
  private CustomProcess[] heap; 
  // number of CustomProcesses present in this CustomProcessQueue
  private int size; 
  
  /**
   * Constructor
   */
  public CustomProcessQueue() {
    this.heap = new CustomProcess[INITIAL_CAPACITY];
    this.size = 0;
  }
  
  /**
   * inserts a newObject in the priority queue
   * 
   * @param newObject CustomProcess instance to be added into the process queue
   */
  @Override
  public void enqueue(CustomProcess newObject) {
    CustomProcess[] newHeap; // array-based MIN-heap.
    
    // when the number of items exceeds heap's capacity, double the capacity
    if (size + 1 == INITIAL_CAPACITY) {
      newHeap = new CustomProcess[INITIAL_CAPACITY * 2];
      for (int i = 1; i < INITIAL_CAPACITY; i++)
        newHeap[i] = heap[i];
      heap = newHeap;
    }
    
    // p1.compareTo(p2) < 0 means that the p1 has higher priority than p2. So, 
    // p1 should be run first.
    // p1.compareTo(p2) == 0 means that p1 and p2 have exactly the same priority.
    // p1.compareTo(p2) > 0 means that p1 has lower priority than p2.
    boolean prelocalization = true;
    int insertIndex = size + 1;
    
    while (prelocalization) {
      // insertIndex != 1 means insertIndex has parent, and it should be compared
      if (insertIndex != 1) {
        // insertIndex % 2 == 0 means insertIndex is the left child
        if (insertIndex % 2 == 0) {
          // newObject's priority is lower than its parent
          if (newObject.compareTo(heap[insertIndex / 2]) >= 0) {
            heap[insertIndex] = newObject;
            prelocalization = false;
          } else {
            heap[insertIndex] = heap[insertIndex / 2];
            insertIndex = insertIndex / 2;
          }
        // insertIndex % 2 != 0 means insertIndex is the right child
        } else {
          // newObject's priority is lower than its parent
          if (newObject.compareTo(heap[(insertIndex - 1) / 2]) >= 0) {
            heap[insertIndex] = newObject;
            prelocalization = false;
          } else {
            heap[insertIndex] = heap[(insertIndex - 1) / 2];
            insertIndex = (insertIndex - 1) / 2;
          }
        }
      // insertIndex == 1 means it is the first CustomProcess to be inserted 
      } else {
        heap[insertIndex] = newObject;
        prelocalization = false;
      }
    }
    size ++;
  }

  
  /**
   * removes and returns the item with the highest priority
   * @return the root node of priority queue
   */
  @Override
  public CustomProcess dequeue() {
    CustomProcess res = heap[1]; // the first item has the highest priority
    heap[1] = heap[size]; // use the last item to replace the first item
    heap[size] = null; // change the last-th of heap to null
    
    // Call dequeueRecursive to maintain priority queue property 
    if (heap[1] != null)
      dequeueRecursive(1, heap[2], heap[3]);
    size --;
    return res;
  }
  
  /**
   * Helper to arrange priority queue organization after remove root.
   * Check the value of current parent node and its children, reshape tree
   * to maintain priority queue property
   * 
   * @param parentIndex current parent location in priority queue
   * @param left left child of current parent node
   * @param right right child of current parent node
   */
  private void dequeueRecursive(int parentIndex, CustomProcess left, CustomProcess right) {
    // if both children is not empty, then compare their value with parent node
    if (left != null && right != null) {
      // if left child is the least one, switch left with parent
      if (heap[parentIndex].compareTo(left) > 0 && heap[parentIndex].compareTo(right) <= 0 ) {
        CustomProcess temp = heap[parentIndex];
        heap[parentIndex] = left;
        heap[parentIndex * 2] = temp;
        // get position of left child, call this method again to rearrange left branch
        parentIndex = parentIndex * 2;
        dequeueRecursive(parentIndex, heap[parentIndex * 2], heap[parentIndex * 2 +1]);
      } else if (heap[parentIndex].compareTo(left)
          <= 0 && heap[parentIndex].compareTo(right) > 0) { 
        // if right child is the least one. switch it with parent 
        CustomProcess temp = heap[parentIndex];
        heap[parentIndex] = right;
        heap[parentIndex * 2 + 1] = temp;
        // get position of right child, call this method again to rearrange right branch
        parentIndex = parentIndex * 2 + 1;
        dequeueRecursive(parentIndex, heap[parentIndex * 2], heap[parentIndex * 2 +1]);
      } else if (heap[parentIndex].compareTo(left) > 0 
          && heap[parentIndex].compareTo(right) > 0) {
        // if both children are greater than parent, switch whichever larger with parent node
        if (left.compareTo(right) < 0) {
          CustomProcess temp = heap[parentIndex];
          heap[parentIndex] = left;
          heap[parentIndex * 2] = temp;
          // get position of left child, call this method again to rearrange left branch
          parentIndex = parentIndex * 2;
          dequeueRecursive(parentIndex, heap[parentIndex * 2], heap[parentIndex * 2 +1]);
        } else {
          CustomProcess temp = heap[parentIndex];
          heap[parentIndex] = right;
          heap[parentIndex * 2 + 1] = temp;
          // get position of right child, call this method again to rearrange right branch
          parentIndex = parentIndex * 2 + 1;
          dequeueRecursive(parentIndex, heap[parentIndex * 2], heap[parentIndex * 2 +1]);
        }
      } 
    } else if (left != null && right == null) {
      // if right child is null, compare parent with left child then do proper switch
      if (heap[parentIndex].compareTo(left) > 0) {
        CustomProcess temp = heap[parentIndex];
        heap[parentIndex] = left;
        heap[parentIndex * 2] = temp;
        parentIndex = parentIndex * 2;
      }
    } 
  }

  /**
   * returns without removing the item with the highest priority
   * 
   * @return current root
   */
  @Override
  public CustomProcess peek() {
    return heap[1];
  }

  /**
   * returns size of the waiting queue
   * 
   * @return size of queue as int
   */
  @Override
  public int size() {
    return size;
  }

  /**
   * checks if the waiting queue is empty
   * 
   * @return true if empty, false otherwise
   */
  @Override
  public boolean isEmpty() {
    return heap[1] == null;
  }
}
