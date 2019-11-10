//////////////////// ALL ASSIGNMENTS INCLUDE THIS SECTION /////////////////////
//
// Title:           ASSIGNMENT 7
// Files:           Sequence, ArithmeticSequenceGenerator, GeometricSequenceGenerator,
//                  FibonacciSequenceGenerator, DigitProductSequenceGenerator, 
//                  and SequenceGeneratorTests.java
// Course:          CS300, Fall 2018
//
// Author:          Mouna Kacem, Yi-Shiun Chang
// Email:           chang242@wisc.edu
// Lecturer's Name: Gary Dahl
//
///////////////////////////// CREDIT OUTSIDE HELP /////////////////////////////
//
// Students who get help from sources other than their partner must fully 
// acknowledge and credit those sources of help here.  Instructors and TAs do 
// not need to be credited here, but tutors, friends, relatives, room mates, 
// strangers, and others do.  If you received no outside help from either type 
// of source, then please explicitly indicate NONE.
//
// Persons:         NONE
// Online Sources:  NONE
//
/////////////////////////////// 80 COLUMNS WIDE ///////////////////////////////
import java.util.Iterator;
 
/**
 * This class represents a generator for a Fibonacci progression
 * This class implements the Iterator<Integer> interface
 * 
 * @author Mouna Kacem, Yi-Shiun Chang
 */
public class FibonacciSequenceGenerator implements Iterator<Integer> {
  private final int SIZE; // the number of elements in this sequence
  private int prev; // the previous item in the sequence with respect to the current iteration
  private int next; // the next item in the sequence with respect to the current iteration
  private int generatedCount; // the number of items generated so far
  
  /**
   * Setters for the convenience of test
   * 
   * @param prev
   */
  public void setPrev(int prev) {
    this.prev = prev;
  }
  
  /**
   * Setters for the convenience of test
   * 
   * @param next
   */
  public void setNext(int next) {
    this.next = next;
  }
  
  /**
   * Setters for the convenience of test
   * 
   * @param generatedCount
   */
  public void setGeneratedCount(int generatedCount) {
    this.generatedCount = generatedCount;
  }
  
  /**
   * Initialize all the defined instance fields: size, prev, next, and generatedCount
   * 
   * @param size the number of elements in this sequence
   */
  public FibonacciSequenceGenerator(int size) {
    // check for the precondition: size > 0, 
    // throws an IllegalArgumentException if this precondition is not satisfied
    if (size <= 0) 
      throw new IllegalArgumentException("WARNING: "
          + "CANNOT create a sequence with size <= zero.");
    // set the instance fields
    this.SIZE = size;
    this.prev = 0;
    this.next = 1;
    generatedCount = 0;
  }
 
  /**
   * Override Interface Iterator<E> 
   * Original hasNext(): Returns true if the iteration has more elements
   * 
   * @return true if the iteration has a next element in the sequence, and false otherwise. 
   */
  @Override
  public boolean hasNext() {
    // time complexity: O(1)
    return generatedCount < this.SIZE;
  }
  
  /**
   * Override Interface Iterator<E> 
   * Original next(): Returns the next element in the iteration
   * Generates the next element in the iteration and returns the current one in the same iteration
   * 
   * @return Integer the current one in the this iteration
   */
  @Override
  public Integer next() {
    // time complexity: O(1)
    // check if the current element has a next element in this sequence
    if (!hasNext()) return null;
    
    if (generatedCount == 0) { // the first iteration returns prev
      generatedCount++; // increment the number of generated elements so far
      return prev;
    }
    else if (generatedCount == 1) { // the second iteration returns next
      generatedCount++; // increment the number of generated elements so far
      return next;
    }
    else {
      generatedCount++; // increment the number of generated elements so far
      int current = next+prev; // set the current element to next+prev
      prev = next; // set the prev to the next for the future usage
      next = current; // set the next to the current for the future usage
      return current; // return the current number as the current one
    }
  }
}