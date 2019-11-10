//////////////////// ALL ASSIGNMENTS INCLUDE THIS SECTION /////////////////////
//
// Title:           ASSIGNMENT 9
// Files:           Badger.java,  Sett.java,  P9Tests.java
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
import java.util.List;
import java.util.NoSuchElementException;

/**
 * within the Sett class, at least two methods that test the functionality of the Badger class, 
 * and at least one methods that test each of the public methods are included. 
 * 
 * You are also required to test the constructors of either class.  
 * 
 * All of these test methods should take zero arguments and should return a boolean value: 
 * false when any defect is found, otherwise true to indicate that a test has passed.
 * 
 * @author Yi-Shiun Chang, Shuo Han
 */
public class P9Tests {
  /**
   * Unit check to validate the functionality of all the methods in
   * Badger.class, including constructor.
   * 
   * @return true when all Badger tests pass
   */
  public static boolean runAllBadgerTests() {
    // Initialize a badger object 
    Badger newBadger = new Badger(10);
    
    // Left child of newBadger should be defined as null in constructor.
    if (newBadger.getLeftLowerNeighbor() != null) {
      System.out.println("newBadger should have no left child");
      return false;
    }
    
    // Check if the size of newBadger is successfully assigned as 10
    if (newBadger.getSize() != 10) {
      System.out.println("this badger's size should be 10");
      return false;
    }
    
    // Check setLeftLowerNeighbor() using a new Badger object
    newBadger.setLeftLowerNeighbor(new Badger(8));
    
    // Now the left child of newBadger should be modified to the instance init in last step
    // If still null, setLeftLowerNeighbor() doesn't work.
    if (newBadger.getLeftLowerNeighbor() == null) {
      System.out.println("newBadger should has left child");
      return false;
    }
    
    // Right child should be null since we didn't init it
    if (newBadger.getRightLowerNeighbor() != null) {
      System.out.println("newBadger should has no right child");
      return false;
    }
    
    // Check if the newly added node has the proper size field.
    if (newBadger.getLeftLowerNeighbor().getSize() != 8) {
      System.out.println("newBadger's left child should has size 8");
      return false;
    }
    
    // Reaching this point indicates success of this unit test.
    return true;
  } 
  
  /**
   * Unit check to validate the functionality of all the methods in
   * Sett.class, including constructor.
   * 
   * @return true when all Sett tests pass
   */
  public static boolean runAllSettTests() {
    // Initialize a Sett instance
    Sett newSett = new Sett();
    
    // Add 10 nodes in Sett, will check other methods based on this inout.
    try {
      int[] badgers = {50, 30, 40, 60, 70, 20, 90, 65, 68, 62};
      for (int i = 0; i < badgers.length; i++)
        newSett.settleBadger(badgers[i]);
    } catch(IllegalArgumentException ie) {
      System.out.println("Thrown unexpected exception!");
      return false;
    } catch(Exception e) {
      System.out.println("Thrown unexpected exception!");
      return false;
    }
    
    // Badger with size 30 is in Sett, if fail to find it or the object return by
    // findBadger(30) doesn't have a size 30, they findBadger() doesn't work.
    try {
      Badger testBadger = newSett.findBadger(30);
      if (testBadger.getSize() != 30) {
        System.out.println("this badger's size should be 30");
        return false;
      }
      
      // Reaching this code segment implies testBadger has size 30. Then check if its
      // left child has the expected size.
      if (testBadger.getLeftLowerNeighbor().getSize() != 20) {
        System.out.println("the size of this badger's leftchild should be 20");
        return false;
      }
      
      // Reaching this code segment implies testBadger has size 30. Then check if its
      // right child has the expected size.
      if (testBadger.getRightLowerNeighbor().getSize() != 40) {
        System.out.println("the size of this badger's rightchild should be 40");
        return false;
      }
    } catch(NoSuchElementException ne) {
      // if badger with size 30 doesn't exist
      System.out.println("Failed to find Badgers!"); 
      return false;
    } catch(Exception e) {
      // Caught unexpected exceptions!
      System.out.println("problem with findBadger()!");
      return false;
    }
    
    // Applying countBadger(), expected return value is 10. Raise error if not.
    if (newSett.countBadger() != 10) {
      System.out.println("this sett should have 10 badgers");
      return false;
    }
    
    // Applying getAllBadgers() and keep its returned list
    List<Badger> badgersList = newSett.getAllBadgers();
    // Traverse each element of badgersList and compare it with reference dictionary
    // Any mismatch triggers error msg and returning false
    int[] checkList = {20, 30, 40, 50, 60, 62, 65, 68, 70, 90};
    for (int i = 0; i < badgersList.size(); i++) {
      if (badgersList.get(i).getSize() != checkList[i]) {
        System.out.println("the ascending order is wrong");
        return false;
      }
    }
    
    // The expected height should be 5 according to input array. Check getHeight() here.
    if (newSett.getHeight() != 5) {
      System.out.println("height shoud be 5");
      return false;
    }
    
    // The expected largest size should be 90 according to input array. 
    // Check getLargestBadger() here,
    if (newSett.getLargestBadger().getSize() != 90) {
      System.out.println("the biggest size of all badgers should be 90");
      return false;
    }
    
    // Get the root using getTopBadger(). Should be 50, the first inserted size.
    if (newSett.getTopBadger().getSize() != 50) {
      System.out.println("the top bager's size should be 50");
      return false;
    }
    
    // Clear all elements by calling clear()
    newSett.clear();
    
    // If newSett is not empty after applying clear(), return false.
    if (!newSett.isEmpty()) {
      System.out.println("newSett should be empty");
      return false;
    }
      
    // If no error happens, return true here.
    return true;
  } 
  
  /**
   * Run runAllBadgerTests() and runAllSettTests() defined above.
   * If all test passed, print "All tests passed!"
   * @param args
   */
  public static void main(String[] args) {
    // Counter keeps track of the # of failed test
    int fail = 0;
    
    // Run runAllBadgerTests(). If doesn't return true, augment counter and print error msg
    if (!runAllBadgerTests()) {
      System.out.println("fail1");
      fail++;
    }
    
    // Run runAllSettTests(). If doesn't return true, augment counter and print error msg
    if (!runAllSettTests()) {
      System.out.println("fail2");
      fail++;
    }
    
    // Print congrats msg if all tests passed.
    if(fail == 0) System.out.println("All tests passes!");
  }
}
