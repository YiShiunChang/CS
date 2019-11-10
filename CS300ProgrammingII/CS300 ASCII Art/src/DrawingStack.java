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
 * This DrawingStack uses the Node class to implement a stack by using a 
 * chain-of-linked-nodes approach. Also, DrawingStack only contains DrawingChanges, 
 * which means this stack is only composed of Node<DrawingChange>, so it must not be generic.
 * 
 * @author Yi-Shiun Chang, Shuo Han
 * @param <DrawingChange>
 */
@SuppressWarnings("hiding")
public class DrawingStack<DrawingChange> implements StackADT<DrawingChange>, Iterable<DrawingChange> {
  private Node<DrawingChange> root; // first node of this stack
  private int size; // how many nodes in this stack
  
  /**
   * Constructor that take a DrawingChange object to initialize stack
   * 
   * @param element
   */
  public DrawingStack(DrawingChange element) {
    this.size = 0;
    // Add the dirst element into stack
    push(element);
  }
  
  /**
   * Constructor that initializes a null stack
   */
  public DrawingStack() {
    this.root = null;
    this.size = 0;
  }
  
  /**
   * Get the root of this stack
   * 
   * @return root
   */
  public Node<DrawingChange> getRoot() {
    return root;
  }
  
  /**
   * This method is used to reset the stack
   */
  public void setRoot() {
    this.root = null;
    this.size = 0;
  }
  
  /**
   * Add an element to this stack
   * 
   * @param element to be added
   * @throws IllegalArgumentException if the input element is null
   */
  @Override
  public void push(DrawingChange element) throws IllegalArgumentException {
    // null can't be added as a Node<DrawingChange>
    if (element == null) throw new IllegalArgumentException();
    
    // if this stack is null, create the first Node<DrawingChange>
    if (root == null) 
      root = new Node<DrawingChange>(element, null);
    // if this stack is not null, insert element as the first Node<DrawingChange>
    // in this stack
    else {
      Node<DrawingChange> temp = root;
      root = new Node<DrawingChange>(element, temp);
    }
    // increase the recorded size of this stack 
    size ++;
  }
  
  /**
   * Remove the element on the stack top and return it
   * 
   * @return the element removed from the stack top
   */
  @Override
  public DrawingChange pop() {
    // get the first Node in this stack
    Node<DrawingChange> temp = root;
    // set the first Node by the second node in this stack
    root = root.getNext();
    // decrease the recorded size of this stack 
    size --;
    return temp.getData();
  }

  /**
   * Get the element on the stack top
   * 
   * @return the element on the stack top
   */
  @Override
  public DrawingChange peek() {
    return root.getData();
  }

  /**
   * Returns true if this stack contains no elements
   * 
   * @return true if this stack contains no elements, otherwise false
   */
  @Override
  public boolean isEmpty() {
    if (root != null)
      return false;
    return true;
  }

  /**
   * Get the number of elements in the stack
   * 
   * @return the size of the stack
   */
  @Override
  public int size() {
    return size;
  }

  /**
   * DrawingStack’s iterator() method should return a new DrawingStackIterator 
   * that starts at the top of the stack of DrawingChanges
   * 
   * @return iterator of the stack
   */
  @Override
  public Iterator<DrawingChange> iterator() {
    // iterator starts from root element
    return new DrawingStackIterator<DrawingChange>(root);
  }

}
