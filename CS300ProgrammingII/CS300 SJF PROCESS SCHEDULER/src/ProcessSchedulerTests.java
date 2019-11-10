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
 * Implement at least 4 public test methods to check the 
 * functionality (including the next step).
 * Make sure to run all the tests in main method.
 * 
 * @author YiShiun Chang, Shuo Han
 *
 */
public class ProcessSchedulerTests {
  /**
   * Check the correctness of the enqueue operation implemented in the CustomProcessQueue class
   * 
   * @return true if all tests passed
   */
  public static boolean testEnqueueCustomProcessQueue(){
    // Create an hard coded queue
    CustomProcessQueue SJF = new CustomProcessQueue();
    int[] burstTime = {5, 4, 3, 2, 1, 6, 7, 8, 9, 10, 15, 14, 13, 12, 11, 5, 5, 9, 8, 20, 21};
        
    // Iteratively add 21 process into queue to check if enqueue method works
    try {
      for (int i = 0; i < 21; i++) 
        SJF.enqueue(new CustomProcess(burstTime[i]));
    } catch (Exception e) {
      System.out.println("testEnqueueCustomProcessQueue fail");
      e.printStackTrace();
      return false;
    }
    return true;
  } 
  
  /**
   * Check the correctness of the dequeue operation implemented in the CustomProcessQueue class
   * 
   * @return true if all tests passed, false o.w.
   */
  public static boolean testDequeueCustomProcessQueue(){
    CustomProcessQueue SJF = new CustomProcessQueue();
    // unordered time list to be added into queue
    int[] burstTime = {5, 4, 3, 2, 1, 6, 7, 8, 9, 10, 15, 14, 13, 12, 11, 5, 5, 9, 8, 20, 21};
    // sorted reference time list to check the functionality of dequeue
    int[] dequeTime = {1, 2, 3, 4, 5, 5, 5, 6, 7, 8, 8, 9, 9, 10, 11, 12, 13, 14, 15, 20, 21};
    
    try {
      // Iteratively insert new objects to process queue
      for (int i = 0; i < 21; i++) 
        SJF.enqueue(new CustomProcess(burstTime[i]));
      // Remove objects one by one, if the order of time values removed don't match reference,
      // return false
      for (int i = 0; i < 21; i++) {
        if (SJF.dequeue().getBurstTime() != dequeTime[i]) return false;
      }
      
    } catch (Exception e) { // handle unexpected error
      System.out.println("testDequeueCustomProcessQueue fail");
      e.printStackTrace();
      return false;
    }
    return true;
  } 
  
  /**
   * Check the functionality of the dequeue operation 
   * implemented in the CustomProcessQueue class
   * 
   * @return true if all tests passed, false o.w.
   */
  public static boolean testPeekCustomProcessQueue() {
    // unordered time list to be added into queue
    CustomProcessQueue SJF = new CustomProcessQueue();
    int[] burstTime = {5, 4, 3, 2, 1, 6, 7, 8, 9, 10};
    
    try {
      // fill up queue and get current peek. Expected value should be the least
      // time 1, return false if not 1.
      for (int i = 0; i < 5; i++)
        SJF.enqueue(new CustomProcess(burstTime[i]));
      if (SJF.peek().getBurstTime() != 1) return false;
      
      // fill up queue by remaining objects, new peek value should be 1 as well
      for (int i = 5; i < 10; i++)
        SJF.enqueue(new CustomProcess(burstTime[i]));
      if (SJF.peek().getBurstTime() != 1) return false;
      
      // Do one step dequeue, 1 should have been removed after this step
      // Current peek should be 2.
      SJF.dequeue();
      if (SJF.peek().getBurstTime() != 2) return false;
    } catch (Exception e) {
      // take care of unwanted error
      System.out.println("testPeekCustomProcessQueue fail");
      e.printStackTrace();
      return false;
    }
    return true;
  }
  
  /**
   * Check the functionality of the Size and isEmpty methods implemented 
   * in the CustomProcessQueue class
   * 
   * @return true if all tests passed, false o.w.
   */
  public static boolean testSizeEmptyCustomProcessQueue() {
    // unordered time list to be added into queue
    CustomProcessQueue SJF = new CustomProcessQueue();
    int[] burstTime = {5, 4, 3, 2, 1, 6, 7, 8, 9, 10};
    
    try {
      // Before adding any objects to queue, check if empty, should be both true.
      if (SJF.isEmpty() != true) return false;
      if (SJF.size() != 0) return false;
      
      // Add one objects, now size should return 1 and isEmpty should return false.
      for (int i = 0; i < 1; i++)
        SJF.enqueue(new CustomProcess(burstTime[i]));
      if (SJF.isEmpty() != false) return false;
      if (SJF.size() != 1) return false;
      
      // Remove the only element currently in queue. Size should go back to 0 and
      // isEmpty should return true if removal succeeded. 
      SJF.dequeue();
      if (SJF.isEmpty() != true) return false;
      if (SJF.size() != 0) return false;      
      
      // Now add 10 objects iteratively, after addition, isEmpty should return false
      // current size is 10. If either return value is wrong, return false.
      for (int i = 0; i < 10; i++)
        SJF.enqueue(new CustomProcess(burstTime[i]));
      if (SJF.isEmpty() != false) return false;
      if (SJF.size() != 10) return false;
    } catch (Exception e) {
      // handle unexpected exception and print error msg
      System.out.println("testSizeEmptyCustomProcessQueue fail");
      e.printStackTrace();
      return false;
    }
    return true;
  }
  
  /**
   * Driver to run four tests defined above. Check the functionality of methods in
   * CustomProcessQueue class. Print out results after all the runs.
   * @param args
   */
  public static void main(String[] args) {
    // counting the # if successful and failed tests
    int success = 0;
    int fail = 0;
    // Sequentially run all tests defined above. 
    if (testEnqueueCustomProcessQueue()) success ++; else fail ++;
    if (testDequeueCustomProcessQueue()) success ++; else fail ++;
    if (testPeekCustomProcessQueue()) success ++; else fail ++;
    if (testSizeEmptyCustomProcessQueue()) success ++; else fail ++;
    
    // print out test log.
    System.out.printf("%d success while %d fail", success, fail);
  }
}
