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
 * This abstract data type represents the pattern for a ready processes waiting list.
 * 
 * @author YiShiun Chang, Shuo Han
 * @param <T> is bounded by Comparable 
 */
public interface WaitingQueueADT<T extends Comparable<T>> {
  
  /**
   * Inserts a newObject in the priority queue
   */
  public void enqueue(T newObject); 
  
  /**
   * Removes and returns the item with the highest priority,
   * the highest priority is the one with minimum value
   */
  public T dequeue(); 
 
  /**
   * Returns without removing the item with the highest priority
   * 
   * @return item that has the minimum value
   */
  public T peek(); 
  
  /**
   * Returns size of the waiting queue
   */
  public int size(); 
 
  /**
   * Checks if the waiting queue is empty
   */
  public boolean isEmpty(); // 
}