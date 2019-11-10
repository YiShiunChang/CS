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
/**
 * This class is built for a math game, and it consists of nodes to form a list
 * 
 * @author Yi-Shiun Chang, Shuo Han
 */
public class GameList {
  private GameNode list; // reference to the first GameNode in this list

  /**
   * Initializes list to start out empty and thus initialize list as null.
   */
  public GameList() {
    this.list = null;
  }
  
  /**
   * Adds the new node to the end of this list. If current list is empty, then add it to
   * this.list as the first element. Otherwise ass it to the last element.
   * @param newNode the GameNode object to be added
   */
  public void addNode(GameNode newNode) {
    if(this.list == null) {
      this.list = newNode; // If current list is empty, set newNode as the first element
    } else {
      GameNode currentNode = this.list; 
      // If currentNode isn't last element, update currentNode by its next until reach the
      // end of the whole list
      while(currentNode.getNext() != null) {
        currentNode = currentNode.getNext();
      }
      // Set the next field of current last element as newNode.
      currentNode.setNext(newNode);
    }
  } 
  
  /**
   * Only returns true when this list contains a node with the specified number
   * @param number the int value to find
   * @return true if target number is found.
   */
  public boolean contains(int number) {
    GameNode currentNode = this.list; // Start search from the first element
    // Iterate if not reach the end of list
    while(currentNode != null) {
      if(currentNode.getNumber() == number)
        return true; // Find match, return true immediately.
      // if current number doesn't match target value, move to next GameNode.
      currentNode = currentNode.getNext();
    }
    return false;
  } 
  
  /**
   * Returns a string with each number in the list separated by " -> "s, and ending with " -> END"
   */
  public String toString() {
    String result = ""; // Initialize an empty string, add 
    GameNode currentNode = this.list; // Start from the first element
    // Traverse the list and add proper works to result String. 
    while(currentNode != null) {
        result = result + currentNode.getNumber() + " -> "; 
        currentNode = currentNode.getNext(); // Move to next node 
    }
    return result + "END"; // Add "END" in the last to let Strig ending with it
  } 
  
  /**
   * Scan through this list searching for the first occurrence of a node with the specified 
   * number.  After finding a node with that number, it calls the applyOperator method on 
   * that GameNode, passing along the specified operator object reference. 
   * @param number the value to search
   * @param operator operator to apply on the GameNode instance.
   */
  public void applyOperatorToNumber(int number, GameOperator operator) {
    GameNode currentNode = this.list; // Start from the first element
    while(currentNode != null) {
      if(currentNode.getNumber() == number) {
        // Apply operator to matched and following Node, change the number in matched
        // Node to the result and remove the following Node from the list
        currentNode.applyOperator(operator);
        return; // No more actions to do once find the target. 
      }
      currentNode = currentNode.getNext(); // move to next Node
    }
  }
}
