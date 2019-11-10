//////////////////// ALL ASSIGNMENTS INCLUDE THIS SECTION /////////////////////
//
// Title:           ASSIGNMENT 8
// Files:           AsciiArt, Canvas, DrawingStack, DrawingStackIterator,
//                  DrawingChange, AsciiTest
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
import java.util.Iterator;

/**
 * The DrawingStackIterator must implement Iterator<DrawingChange> and is a direct iterator 
 * over Drawingstack. 
 * 
 * @author Yi-Shiun Chang, Shuo Han
 */
@SuppressWarnings("hiding")
public class DrawingStackIterator<DrawingChange> implements Iterator<DrawingChange> {
  Node<DrawingChange> root;
  
  /**
   * This constructor should take a Node<DrawingChange> as a parameter
   * 
   * @param node
   */
  public DrawingStackIterator(Node<DrawingChange> node) {
    this.root = node;
  }
  
  /**
   * Check whether current root has a next node
   * 
   * @return true if there is a next node
   */
  @Override
  public boolean hasNext() {
    if (root == null)
      return false;
    return true;
  }

  /**
   * Return current root's data and update root with its next node
   * 
   * @return root's data
   */
  @Override
  public DrawingChange next() {
    // if already reach the last element in this stack, just return null
    if(root == null) 
      return null;
    Node<DrawingChange> temp = root;
    root = root.getNext();
    return temp.getData();
  }
}
