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
 * This node class is built for a math game, and it contains its own number and
 * a next to linked the next node 
 * 
 * @author Yi-Shiun Chang, Shuo Han
 */
public class GameNode {
  private int number;    // the number held within this node
  private GameNode next; // the next GameNode in the list, or null for the last node

  /**
   * initializes number to random 1-9 value, and next to null
   * @param rng random number generator which should generate number randomly from  1-9
   */
  public GameNode(Random rng) {
    this.number = rng.nextInt(9) + 1;
    this.next = null;
  }
  
  /**
   * Accessor for the number field
   * @return the number field as an int
   */
  public int getNumber() {
    return this.number;
  }
  
  /**
   * Accessor for the next field
   * @return The object of next node. Null of this is last node
   */
  public GameNode getNext() {
    return this.next;
  } 
  
  /**
   * Mutator for the next field
   * @param next Instance of GameNode class as new next node.
   */
  public void setNext(GameNode next) {
    this.next = next;
  }
  
  /**
   * The new number for this node is calculated by applying the provided operator 
   * to this node's number (the first operand), and the next node's number (the 
   * second operand).
   * @param operator The operator instance that contains the operator to apply
   */
  public void applyOperator(GameOperator operator) {
    try {
      this.number = operator.apply(this.number, this.next.getNumber());
    } catch (ArithmeticException e) {
      // if denominator = 0, set this.number = 0
      if (this.next.getNumber() == 0 && operator.toString().equals("/"))
        this.number = 0;
    }
    
    this.next = this.next.next;
  }
}
