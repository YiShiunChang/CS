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
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class represents a generator for a DigitProduct progression
 * This class implements the Iterator<Integer> interface
 * 
 * @author Mouna Kacem, Yi-Shiun Chang
 */
public class DigitProductSequenceGenerator {
  private final int INIT; // initial number
  private final int SIZE; // size of sequence
  // ArrayList<E> class defined in the java.util package implements already the Iterable<E> interface.
  private ArrayList<Integer> sequence; // ArrayList object storing the sequence
  
  /**
   * Getters for the convenience of test
   * 
   * @return ArrayList<Integer> of a DigitProductSequence
   */
  public ArrayList<Integer> getSequence(){
    return sequence;
  }
  
  /**
   * Initialize all the defined instance fields: init, size, and sequence
   * 
   * @param init the initial number
   * @param size the size of this sequence
   */
  public DigitProductSequenceGenerator(int init, int size) {
    // check for the precondition: size > 0, 
    // throws an IllegalArgumentException if this precondition is not satisfied
    if (size <= 0) 
      throw new IllegalArgumentException("WARNING: "
          + "CANNOT create a sequence with size <= zero.");
    // check for the validity of init > 0
    // throw an IllegalArgumentExceptio if it is not valid 
    if(init <= 0)
      throw new IllegalArgumentException("WARNING: The starting element for digit product sequence"
          + " cannot be less than or equal to zero.");
    // set the instance fields
    this.INIT = init;
    this.SIZE = size;
    sequence = new ArrayList<Integer> ();
    // build the sequence
    generateSequence();
  }
  
  /**
   * This method generates the digit sequence with first element init and size using loop(s)
   * 
   * Note. generateSequence() method has to clear the sequence Arraylist content 
   *       before adding new contents
   */
  public void generateSequence() {
    sequence.clear(); // clean the ArrayList
    
    // building the sequence
    for (int i = 0; i < SIZE; i++) {
      if (i == 0) sequence.add(INIT); // add the first element
      else {
        // get the digits of the previous element
        String prev = Integer.toString(sequence.get(i-1));
        String[] digits = prev.split(""); 
        // multiply the digits with each other unless it is 0
        int product = 1;
        for (int j = 0; j < digits.length; j++) {
          if (Integer.parseInt(digits[j]) != 0)
            product *= Integer.parseInt(digits[j]);
        }
        // add the non-first element
        sequence.add(sequence.get(i-1)+product);
      }
    }
  }
  
  /**
   * Returns an iterator over the elements in this list in proper sequence.
   * 
   * @return Iterator which iterate over the ArrayList sequence field
   */
  public Iterator<Integer> getIterator() {
    return sequence.iterator();
  }
}
