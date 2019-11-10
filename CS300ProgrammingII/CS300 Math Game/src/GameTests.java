//////////////////// ALL ASSIGNMENTS INCLUDE THIS SECTION /////////////////////
//
// Title:           ASSIGNMENT 6
// Files:           GameList.java, GameNode.java, GameApplication.java, 
//                  and GameTests.java
// Course:          CS300, Fall 2018
//
// Author:          Yi-Shiun Chang
// Email:           chang242@wisc.edu
// Lecturer's Name: Gary Dahl
//
//////////////////// PAIR PROGRAMMERS COMPLETE THIS SECTION ///////////////////
//
// Partner Name:    Shuo Han
// Partner Email:   shan238@wisc.edu
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
import java.util.Random;

/**
 * There are two static test methods that take no arguments 
 * and return true when they detect the correct behavior of a working 
 * GameNode implementation (and false otherwise).  
 * The rest of those methods is to test the GameList implementation.  
 * All five of these test methods test the operators defined in GameOperator
 * class. 
 * All the tests will be run in main method and print proper msg according to 
 * test results.
 * @author chang & han
 *
 */
public class GameTests {
  public static void main (String[] args) {
    int passCount = 0; // Keep track of tests passed
    int failCount = 0; // Keep track of tests failed
    
    // increase the failCount if tests failed, increase passeCount is passed
    if (test01GameNodeInitialize()) passCount++; else failCount++;
    if (test02GameNodeSetNext()) passCount++; else failCount++;
    if (test03applyOperatorToNumberMul()) passCount++; else failCount++;
    if (test04applyOperatorToNumberAdd()) passCount++; else failCount++;

    
    if(failCount == 0) 
      System.out.println("All tests passed!");
    else
      System.out.format("%d out of %d test failed.", failCount, failCount+passCount);
  }
  
  /**
   * This class is to test GameNode class. Check if the number initialized lies
   * in 0-9. This processes will be repeated for 100 times and a warning will be
   * printed if anything wrong.
   * @return true if passed
   */
  private static boolean test01GameNodeInitialize() {
    GameNode newNode; // Declare a uninitialized variable which will be updated later
    Random ran = new Random();
    for (int i = 0; i < 100; i++) { // Repeat GameNode initialization for 100 times
      newNode = new GameNode(ran); // Pass random number generator to create new GameNode instance
      // Return false adn print out error msg when the number of newly created object has a number
      // beyond legal range
      if (!(newNode.getNext() == null && 1 <= newNode.getNumber() && newNode.getNumber() <= 9)) {
        System.out.println("test01GameNodeInitialize fail.");
        return false;
      }
    }
    // This line will be reached only when all 100 tests are passed. 
    return true;
  }
  
  /**
   * This test check the functionality of setNext() method inside of GameNode. 
   * First create two instances of GameNode and set one as the next of another
   * using setNext. 
   * @return true if passed
   */
  private static boolean test02GameNodeSetNext() {
    Random ran = new Random();
    // First node. We will later set its next  
    GameNode newNode1 = new GameNode(ran); 
    // Second node. We will set it as the next of the first one
    GameNode newNode2 = new GameNode(ran); 
    // Call setNext()
    newNode1.setNext(newNode2);
    
    // The initial value of newNode1.getNext() is null. But after updating it with setNext()
    // It should be newNode2 instead.
    if (newNode1.getNext() == null || !newNode1.getNext().equals(newNode2)) {
      System.out.println("test02GameNodeSetNext fail.");
      return false;
    }
    return true;
  }
  
  /**
   * This method tests the functionality of test04applyOperatorToNumberMul() 
   * method in GameList by multiplication operator. And check if the number field  
   * of current node is correctly updated and if the node in which the second 
   * operand is properly removed.
   * @return true if passed
   */
  private static boolean test03applyOperatorToNumberMul() {
    Random ran = new Random();
    // Create a GameList with zero element
    GameList myList = new GameList();
    // Create two GameNodes and add them to myList
    GameNode newNode1 = new GameNode(ran);
    GameNode newNode2 = new GameNode(ran);
    myList.addNode(newNode1);
    myList.addNode(newNode2);

    // Extract their numbers and to be used as reference of followed test process 
    int num1 = newNode1.getNumber();
    int num2 = newNode2.getNumber();
    // Call applyOperatorToNumber() to multiply matched number and the following together
    myList.applyOperatorToNumber(num1, GameOperator.getFromChar('x'));

    // Correct result should return the product of two numbers and the 
    // second node should be removed.
    if (newNode1.getNumber() != num1 * num2 || newNode1.getNext() != null) {
      System.out.println("test03ApplyOperatorAdd fail.");
      return false;
    }
    return true;
  }
  
  /**
   * This method tests the functionality of test04applyOperatorToNumberAdd() 
   * method in GameList by addition operator. And check if the number field  
   * of current node is correctly updated and if the node in which the second 
   * operand is properly removed.
   * @return true if passed
   */
  private static boolean test04applyOperatorToNumberAdd() {
    Random ran = new Random();
    // Create a GameList with zero element
    GameList myList = new GameList();
    // Create two GameNodes and add them to myList
    GameNode newNode1 = new GameNode(ran);
    GameNode newNode2 = new GameNode(ran);
    myList.addNode(newNode1);
    myList.addNode(newNode2);

    // Extract their numbers and to be used as reference of followed test process 
    int num1 = newNode1.getNumber();
    int num2 = newNode2.getNumber();
    // Call applyOperatorToNumber() to add matched number and the following together
    myList.applyOperatorToNumber(num1, GameOperator.getFromChar('+'));

    // Correct result should return the sum of two numbers and the 
    // second node should be removed.
    if (newNode1.getNumber() != num1 + num2 || newNode1.getNext() != null) {
      System.out.println("test03ApplyOperatorAdd fail.");
      return false;
    }
    return true;
  }
}
