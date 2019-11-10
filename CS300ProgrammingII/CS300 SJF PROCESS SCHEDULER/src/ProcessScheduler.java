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

import java.util.Scanner;

/**
 * The ProcessScheduler class represents the data type for the main scheduler 
 * for our processes. It drives the scheduler and arrange user interface and
 * console massages.
 * 
 * @author baliansnow
 *
 */
public class ProcessScheduler {
  private int currentTime; // stores the current time after the last run
  private int numProcessesRun; // stores the number of processes run so far
  private CustomProcessQueue queue; // this processing unit's custom process queue
  
  /**
   * As illustrated in the demo presented at the top of this write-up, 
   * 
   * first a set of processes are scheduled according to the SJF policy. 
   * Then, they will be run one by one according to the same policy. This process 
   * may be repeated many times.
   */
  
  public ProcessScheduler() {
    this.queue = new CustomProcessQueue();
    this.currentTime = 0;
    this.numProcessesRun = 0;
  }
  
  /**
   * enqueue the given process in the CustomProcessQueue queue
   * 
   * @param process CustomProcess to be enqueued
   */
  public void scheduleProcess(CustomProcess process) {
    queue.enqueue(process);
  }
  
  /**
   * The scheduleProcess() method enqueue the given process in the CustomProcessQueue queue.
   * This method returns when all the scheduled processes are run and the queue is empty. 
   * It returns a String that represents the log of one run operation. The format of this 
   * log is as follows.
   * @return return log massages as string
   */
  public String run() {
    StringBuilder sb = new StringBuilder();
    // The log returned by the run() method should begin with one the following messages, 
    // depending on the <size of the queue>, if it contains only one process, or zero 
    // or many processes:
    if (queue.size() <= 1)
      sb.append("Starting " + queue.size() + " process\n\n");
    else
      sb.append("Starting " + queue.size() + " processes\n\n");
    
    // Each time a ready process is dequeued to be run, the following message must 
    // be added to this log:
    while (!queue.isEmpty()) {
      sb.append("Time " + currentTime + " : Process ID " 
          + queue.peek().getProcessId() + " Starting.\n");
      currentTime += queue.peek().getBurstTime();
      numProcessesRun ++;
      sb.append("Time " + currentTime + ": Process ID " 
          + queue.dequeue().getProcessId() + " Completed.\n");
    }
    
    // When ALL processes are run, the following message will be added to the log:
    sb.append("\nTime " + currentTime + ": All scheduled processes completed.\n");
      
    //Do not forget to update the number of processes run so far.
    return sb.toString();
  }
  
  /**
   * The final step in this assignment is to implement the main method of the 
   * ProcessScheduler class. 
   * This method serves as the driver of the application. 
   * 
   * NOTE that this method creates and uses only one instance of the Scanner class, 
   * and also only one instance of the ProcessScheduler class. 
   * 
   * @param args
   */
  public static void main(String[] args) {
    // Initialize driver 
    ProcessScheduler ps = new ProcessScheduler();
    // When 'q' keyword detected, quit will be switched to true
    boolean quit = false;
    Integer burstTime;
    Scanner sc = new Scanner(System.in);
    
    // Generate console display massage
    System.out.println("==========   Welcome to the SJF Process Scheduler App   ========");
    while (!quit) {
      System.out.println("\nEnter command:\n"
          + "[schedule <burstTime>] or [s <burstTime>]\n"
          + "[run] or [r]\n"
          + "[quit] or [q]\n");
      // When taking input from the user, you have to check the validity of the 
      // input and print a warning if it is invalid.
      // If the user enters a wrong command or if the format of the command is 
      // not valid, the exact following message should be printed out to the console:
      try {
        String input = sc.nextLine();
        String[] inputArray = input.split(" ");
        
        // Check input eligibility, throw exception if not
        if (inputArray.length == 0 || inputArray.length >= 3) {
          throw new IllegalArgumentException("WARNING: Please enter a valid command!\n");
        } 
        
        // Continue sanity check, throw exception to trigger error msg
        if (!(inputArray[0].equals("schedule") || inputArray[0].equals("s") || 
            inputArray[0].equals("run") || inputArray[0].equals("r") ||
            inputArray[0].equals("quit") || inputArray[0].equals("q"))) {
          throw new IllegalArgumentException("WARNING: Please enter a valid command!\n");
        }
        
        // if user command is scheduling new process, add new process to queue
        if (inputArray[0].equals("schedule") || inputArray[0].equals("s")) {
          // time field cannot be negative
          burstTime = Integer.parseInt(inputArray[1]);
          if (burstTime <= 0)
            throw new IllegalArgumentException("WARNING: burst time MUST be greater than 0!\n");
          
          // Create CustomProcess instance with user-specified time
          CustomProcess newProcess = new CustomProcess(burstTime);
          
          // Add newly created object into schedule queue and print out proper console msg
          ps.scheduleProcess(newProcess);
          System.out.println("Process ID " + newProcess.getProcessId() 
            + " scheduled. Burst Time = " + burstTime + "\n");
        }
        
        // if user input run command, call run method to executed existing processes in queue 
        if (inputArray[0].equals("run") || inputArray[0].equals("r")) {
          System.out.println(ps.run());
        }
        
        // when detect quit command, change boolean variable to true and print out proper msg 
        if (inputArray[0].equals("quit") || inputArray[0].equals("q")) {
          quit = true;
          System.out.println(ps.numProcessesRun + " processes run in " + ps.currentTime + 
              " units of time!\n" + "Thank you for using our scheduler!\n" + "Goodbye!\n");
        }

      } catch (IllegalArgumentException e) {
        // print out error msg 
        System.out.println(e.getMessage());
      } 
    }
    
    // close scanner to save memory and avoid error.
    sc.close();
  }
}
