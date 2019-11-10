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

/**
 * This class represents a Badger which is designed to live in a Sett. 
 * Each Badger object represents a single node within a BST (known as a Sett).
 * 
 * @author Yi-Shiun Chang, Shuo Han
 */
public class Badger {
  private Badger leftLowerNeighbor;
  private Badger rightLowerNeighbor;
  private int size;
  
  /**
   * Creates a new Badger with specified size.
   * 
   * @param size of the newly constructed Badger object.
   */
  public Badger(int size) {
    this.size = size;
    this.leftLowerNeighbor = null;
    this.rightLowerNeighbor = null;
  }
  
  /**
   * Retrieves neighboring badger that is smaller than this one.
   * 
   * @return badger
   */
  public Badger getLeftLowerNeighbor() {
    return leftLowerNeighbor;
  }
  
  /**
   * Retrieves neighboring badger that is larger than this one.
   * 
   * @return badger
   */
  public Badger getRightLowerNeighbor() {
    return rightLowerNeighbor;
  }
  
  /**
   * Retrieves the size of this badger
   * 
   * @return size
   */
  public int getSize() {
    return size;
  }
  
  /**
   * Changes this badger's lower left neighbor.
   * 
   * @param badger
   */
  public void setLeftLowerNeighbor(Badger badger) {
    this.leftLowerNeighbor = badger;
  }
  
  /**
   * Changes this badger's lower right neighbor.
   * 
   * @param badger
   */
  public void setRightLowerNeighbor(Badger badger) {
    this.rightLowerNeighbor = badger;
  }
}
