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
 * This class represents a generator for a geometric progression
 * This class implements the Iterator<Integer> interface
 * 
 * @author Mouna Kacem, Yi-Shiun Chang
 */
public class GeometricSequenceGenerator implements Iterator<Integer> {
  private final int SIZE; // the number of elements in this sequence
  private final int INIT; // the first term in this sequence
  private final int RATIO; // the common ratio for this sequence
  private int next; // the next term in the sequence
  private int generatedCount; // the number of elements within the sequence generated so far
  
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
   * Initialize all the defined instance fields: init, ratio, size, next, and generatedCount
   * 
   * @param init the first term in this sequence 
   * @param ratio the common ratio using as a multiplier
   * @param size the number of elements in this sequence
   */
  public GeometricSequenceGenerator(int init, int ratio, int size) {
    // check for the precondition: size > 0, 
    // throws an IllegalArgumentException if this precondition is not satisfied
    if (size <= 0) 
      throw new IllegalArgumentException("WARNING: "
          + "CANNOT create a sequence with size <= zero.");
    // check for the validity of init >0 and ratio >0, 
    // throw an IllegalArgumentExceptio if these two parameters are not valid 
    if(init <= 0 || ratio <=0 )
      throw new IllegalArgumentException("WARNING: The starting element and the common ratio"
          + " for a geometric progression should be STRICTLY POSITIVE.");
    // set the instance fields
    this.SIZE = size;
    this.INIT = init;
    this.RATIO = ratio;
    next = init; // initializes next to the first element in this geometric progression
    generatedCount = 0;
  }
 
  /**
   * Override Interface Iterator<E> 
   * Original hasNext(): Returns true if the iteration has more elements
   * 
   * @return true if the iteration has a next element in the sequence, and false otherwise
   */
  @Override
  public boolean hasNext() {
    // time complexity: O(1)
    return generatedCount < this.SIZE;
  }
  
  /**
   * Override Interface Iterator<E> and generates the next element in the iteration
   * Original next(): Returns the next element in the iteration
   * 
   * @return Integer the current one in the this iteration
   */
  @Override
  public Integer next() {
    // time complexity: O(1)
    // check if the current element has a next element in this sequence
    if (!hasNext()) return null;
    generatedCount++; // increment the number of generated elements so far
    int current = next; // set the current element to next
    next *= this.RATIO; // set the next element (multiply the ratio to the current number) 
    return current; // return the current number as the current one
  }
}