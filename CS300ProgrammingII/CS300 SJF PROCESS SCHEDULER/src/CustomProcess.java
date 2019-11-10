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
 * Define the object class used in process scheduler. Contains information 
 * describing a process. This class implements Comparable<CustomProcess>
 * interface.
 * 
 * @author YiShiun Chang, Shuo Han
 *
 */
public class CustomProcess implements Comparable<CustomProcess> {
  //stores the id to be assigned to the next process to be created
  private static int nextProcessId = 1; 
  //unique identifier for this process
  private final int PROCESS_ID; 
  //time required by this process for CPU execution
  private int burstTime; 
  
  /**
   * Constructor setup 3 fields of this CustimProcess
   * 
   * @param burstTime of a CustimProcess
   */
  public CustomProcess(int burstTime) { 
    this.PROCESS_ID = CustomProcess.nextProcessId;
    // Augment static field, make process ID being unique 
    CustomProcess.nextProcessId ++;
    this.burstTime = burstTime;
  }
  
  /**
   * Return this CustomProcess's PROCESS_ID
   * @return PROCESS_ID field
   */
  public int getProcessId() {
    return PROCESS_ID;
  }
  
  /**
   * CustomProcess's burstTime accesser
   * @return burstTime field
   */
  public int getBurstTime() {
    return burstTime;
  }
  
  /**
   * compareTo() method is used to compare this CustomProcess to another one (other). 
   * Suppose that we have two instances of CustomProcess classes referred by p1 and p2. 
   * The process that has higher priority than the other should be run first.
   * 
   * p1.compareTo(p2) < 0 means that the p1 has higher priority than p2. So, 
   * p1 should be run first.
   * p1.compareTo(p2) == 0 means that p1 and p2 have exactly the same priority.
   * p1.compareTo(p2) > 0 means that p1 has lower priority than p2.
   * 
   * @param other CustomProcess object to be compared with this object
   * @return -1 if this is less than other; 0 if equals; 1 if grater than other
   */
  @Override
  public int compareTo(CustomProcess other) {
    // first compare BurstTime
    if (burstTime < other.getBurstTime())
      return -1;
    else if (burstTime == other.getBurstTime()) {
      // If BurstTime is equal, compare secondary key PROCESS_ID
      if (this.PROCESS_ID < other.PROCESS_ID)
        return -1;
      else if (this.PROCESS_ID > other.PROCESS_ID)
        return 1;
      else
        return 0;
    } else 
      return 1;
  }
}
