//////////////////// ALL ASSIGNMENTS INCLUDE THIS SECTION /////////////////////
//
// Title:           ASSIGNMENT 7
// Files:           
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
 * This class represents a generator for an arithmetic progression
 * This class implements the Iterator<Integer> interface
 * 
 * @author Mouna
 */
public class ArithmeticSequenceGenerator implements Iterator<Integer> {
  private final int INIT; // initial element in the sequence progression
  private final int DIFF; // common difference
  private final int SIZE; // number of elements in the sequence
  private int next; // next element in this sequence
  private int generatedCount; // number of elements within the sequence generated so far


  /**
   * Generates an arithmetic progression
   * 
   * @param init initial value
   * @param diff common difference
   * @param size number of elements in the sequence
   */
  public ArithmeticSequenceGenerator(int init, int diff, int size) {
    // check for the precondition: size > 0, 
    // throws an IllegalArgumentException if this precondition is not satisfied
    if (size <= 0) 
      throw new IllegalArgumentException("WARNING: "
          + "CANNOT create a sequence with size <= zero.");
    // check for the validity of init (>=0) and diff (>0), 
    // throw an IllegalArgumentExceptio if these two parameters are not valid 
    if(init < 0 || diff <=0 )
      throw new IllegalArgumentException("WARNING: "
          + "The starting element and the common difference for an arithmetic progression "
          + "should be POSITIVE!");
    // set the instance fields
    this.SIZE = size;
    this.INIT = init;
    this.DIFF = diff;
    next = init; // initializes next to the first element in this arithmetic progression
    generatedCount = 0;
  }


  /** 
   * Checks if the iteration has a next element in this sequence
   * 
   * @return true if the current element in the iteration has a next element in this sequence
   */
  @Override
  public boolean hasNext() {
    return generatedCount < SIZE;
  }

  /** 
   * Returns the next element in this arithmetic sequence iteration with respect to the numbers
   * generated so far
   * 
   * @return the next element in this iteration
   */
  @Override
  public Integer next() {
    // check if the current element has a next element in this sequence
    if (!hasNext()) return null;
    int current = next; // set the current element to next
    generatedCount++; // increment the number of generated elements so far
    next += DIFF; // set the next element (adds the common difference to the current number) 
    return current; // return the current number as the generated one
  }
  
}