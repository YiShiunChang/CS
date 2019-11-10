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
 * This class tests the basic methods of GeometricSequenceGenerator,
 * FibonacciSequenceGenerator, and DigitalProductSequenceGenerator
 * 
 * @author Mouna Kacem, Yi-Shiun Chang
 */
public class SequenceGeneratorTests {
  
  /**
   * geometricSequenceGeneratorTest() should return false if either the constructor, 
   * hasNext(), or next() method implemented in it does not return the expected output
   * 
   * @return boolean
   */
  public static boolean geometricSequenceGeneratorTest() {
    // new a sequenceIterator from GeometricSequenceGenerator with
    // initial element 4, difference 2, and size 3
    GeometricSequenceGenerator sequenceIterator = new GeometricSequenceGenerator(4, 2, 3);
    
    // sequenceIterator should has next
    if (!sequenceIterator.hasNext()) {
      System.out.println("geometricSequenceGeneratorTest 1 fail.");
      return false;
    }
    // sequenceIterator next == 4
    if (sequenceIterator.next() != 4) {
      System.out.println("geometricSequenceGeneratorTest 2 fail.");
      return false;
    }
    
    sequenceIterator.setNext(7);
    if (sequenceIterator.next() != 7) {
      System.out.println("geometricSequenceGeneratorTest 3 fail.");
      return false;
    }
    
    // sequenceIterator should has next
    sequenceIterator.setGeneratedCount(2);
    if (!sequenceIterator.hasNext()) {
      System.out.println("geometricSequenceGeneratorTest 4 fail.");
      return false;
    }
    
    // sequenceIterator shouldn't has next
    sequenceIterator.setGeneratedCount(3);
    if (sequenceIterator.hasNext()) {
      System.out.println("geometricSequenceGeneratorTest 5 fail.");
      return false;
    }
    
    try {
      // sequenceIterator can't be created when initial is 0
      sequenceIterator = new GeometricSequenceGenerator(0, 2, 3);
      System.out.println("geometricSequenceGeneratorTest 6 fail.");
      return false;
    } catch (IllegalArgumentException e) {
      // if initial is 0, get the specific warning
      String warning = "WARNING: The starting element and the common ratio"
          + " for a geometric progression should be STRICTLY POSITIVE.";
      if (!e.getMessage().equals(warning)) return false;
    }
    
    try {
      // sequenceIterator can't be created when difference is 0
      sequenceIterator = new GeometricSequenceGenerator(4, 0, 3);
      System.out.println("geometricSequenceGeneratorTest 7 fail.");
      return false;
    } catch (IllegalArgumentException e) {
      // if difference is 0, get the specific warning
      String warning = "WARNING: The starting element and the common ratio"
          + " for a geometric progression should be STRICTLY POSITIVE.";
      if (!e.getMessage().equals(warning)) return false;
    }
    
    try {
      // sequenceIterator can't be created when size is 0
      sequenceIterator = new GeometricSequenceGenerator(4, 2, 0);
      System.out.println("geometricSequenceGeneratorTest 8 fail.");
      return false;
    } catch (IllegalArgumentException e) {
      // if size is 0, get the specific warning
      String warning = "WARNING: "
          + "CANNOT create a sequence with size <= zero.";
      if (!e.getMessage().equals(warning)) return false;
    }
    
    return true;
  }
  
  /**
   * fibonacciSequenceGeneratorTest() should return false if either the constructor, 
   * hasNext(), or next() method implemented in it does not return the expected output
   * 
   * @param boolean
   */
  public static boolean fibonacciSequenceGeneratorTest() {
    // new a sequenceIterator from FibonacciSequenceGenerator with size 4
    FibonacciSequenceGenerator sequenceIterator = new FibonacciSequenceGenerator(4);
    
    // sequenceIterator should has next
    if (!sequenceIterator.hasNext()) {
      System.out.println("fibonacciSequenceGeneratorTest 1 fail.");
      return false;
    }
    // sequenceIterator next == 0
    if (sequenceIterator.next() != 0) {
      System.out.println("fibonacciSequenceGeneratorTest 2 fail.");
      return false;
    }
    
    // sequenceIterator.setGeneratedCount(2) is important, 
    // since generatedCount is critical when calling .next()
    sequenceIterator.setPrev(2);
    sequenceIterator.setNext(7);
    sequenceIterator.setGeneratedCount(2);
    if (sequenceIterator.next() != 9) {
      System.out.println("fibonacciSequenceGeneratorTest 3 fail.");
      return false;
    }
    
    // generatedCount < 4, hasNext() return true 
    sequenceIterator.setGeneratedCount(3);
    if (!sequenceIterator.hasNext()) {
      System.out.println("fibonacciSequenceGeneratorTest 4 fail.");
      return false;
    }
    
    // generatedCount == 4, hasNext() return false
    sequenceIterator.setGeneratedCount(4);
    if (sequenceIterator.hasNext()) {
      System.out.println("fibonacciSequenceGeneratorTest 5 fail.");
      return false;
    }
    
    try {
      // sequenceIterator can't be created when size is 0
      sequenceIterator = new FibonacciSequenceGenerator(0);
      System.out.println("fibonacciSequenceGeneratorTest 6 fail.");
      return false;
    } catch (IllegalArgumentException e) {
      // if size is 0, get the specific warning
      String warning = "WARNING: "
          + "CANNOT create a sequence with size <= zero.";
      if (!e.getMessage().equals(warning)) return false;
    }
    
    return true;
  }
  
  /**
   * digitProductSequenceGeneratorTest() should return false if
   * 1. User can't create a digit-product sequence with the provided initial number and size
   * 2. The sequence is not stored in the sequence instance field
   * 3. getIterator() method doesn't return an Iterator over the generated sequence
   * 4. constructor doesn't throw the appropriate exceptions with the appropriate error messages 
   *    if the provided parameters to generate this sequence are NOT valid.
   * 
   * @param boolean
   */
  public static boolean digitProductSequenceGeneratorTest() {
    // new a sequenceIterator from DigitProductSequenceGenerator with initial 79 and size 4
    DigitProductSequenceGenerator sequenceIterator = new DigitProductSequenceGenerator(79, 10);
    // create the Arraylist sequence in the sequenceIterator
    sequenceIterator.generateSequence();
    
    // Arraylist sequence should not be null
    if (sequenceIterator.getSequence() == null) {
      System.out.println("digitProductSequenceGeneratorTest 1 fail.");
      return false;
    }
    // Arraylist sequence is type Arraylist
    if (!(sequenceIterator.getSequence() instanceof ArrayList)) {
      System.out.println("digitProductSequenceGeneratorTest 2 fail.");
      return false;
    }
    // when call getIterator() from sequenceIterator, get an Iterator
    if (!(sequenceIterator.getIterator() instanceof Iterator)) {
      System.out.println("digitProductSequenceGeneratorTest 3 fail.");
      return false;
    }
    
    try {
      // sequenceIterator can't be created when initial is 0
      sequenceIterator = new DigitProductSequenceGenerator(0, 10);
      System.out.println("digitProductSequenceGeneratorTest 4 fail.");
      return false;
    } catch (IllegalArgumentException e) {
      // if initial is 0, get the specific warning
      String warning = "WARNING: The starting element for digit product sequence "
          + "cannot be less than or equal to zero.";
      if (!e.getMessage().equals(warning)) return false;
    }
    
    
    try {
      // sequenceIterator can't be created when size is 0
      sequenceIterator = new DigitProductSequenceGenerator(10, 0);
      System.out.println("digitProductSequenceGeneratorTest 5 fail.");
      return false;
    } catch (IllegalArgumentException e) {
      // if size is 0, get the specific warning
      String warning = "WARNING: CANNOT create a sequence with size <= zero.";
      if (!e.getMessage().equals(warning)) return false;
    }
    
    return true;
  }
  
  /**
   * Call all testing methods
   * 
   * @param args
   */
  public static void main(String[] args) {
    int passCount = 0; // Keep track of tests passed
    int failCount = 0; // Keep track of tests failed
    
    // increase the failCount if tests failed, increase passeCount is passed
    if (!geometricSequenceGeneratorTest()) failCount++; else passCount++;
    if (!fibonacciSequenceGeneratorTest()) failCount++; else passCount++;
    if (!digitProductSequenceGeneratorTest()) failCount++; else passCount++;
    
    if(failCount == 0) 
      System.out.println("All tests passed!");
    else
      System.out.format("%d out of %d test failed.", failCount, failCount+passCount);
  }
  
}
