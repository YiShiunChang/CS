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
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.lang.IllegalArgumentException;

/**
 * This class represents a Sett, where a group of Badgers live together. 
 * Each Sett is organized as a BST of Badger nodes.
 * 
 * @author Yi-Shiun Chang, Shuo Han
 */
public class Sett {
  private Badger topBadger; // the root of this BSTree
  
  /**
   * Constructs an empty Sett.
   */
  public Sett() {
    this.topBadger = null;
  }
  
  /**
   * Empties this Sett, to no longer contain any Badgers.
   */
  public void  clear() {
    this.topBadger = null;
  }
 
  /**
   * Creates a new Badger object with the specified size, and inserts them into this Sett (BST).
   * When a Badger with the specified size already exists within this Sett, 
   * thorws IllegalArgumentException. 
   * 
   * @param size of a badger
   */
  public void settleBadger(int size) throws IllegalArgumentException {
    Badger newBadger = new Badger(size); // create a badger with size
    if (topBadger == null) { // setup the root of this BSTree
      topBadger = newBadger;
    } else { // update internal nodes of this BSTree
      settleHelper(topBadger, newBadger);
    }
  }
  
  /**
   * This recursive helper method is used to help settle a new Badger within this Sett.
   * 
   * @param current is the node (Badger) that we check currently
   * @param newBadger is the node (Badger) that we want to insert to the BSTree
   */
  private void settleHelper(Badger current, Badger newBadger) throws IllegalArgumentException {
    // when size of newBadger is smaller than size of current node, get its left child
    if (newBadger.getSize() < current.getSize()) {
      // update left child when it is null; otherwise, set left child as current node for recursion
      if (current.getLeftLowerNeighbor() == null)
        current.setLeftLowerNeighbor(newBadger);
      else
        settleHelper(current.getLeftLowerNeighbor(), newBadger);
    }
    // when size of newBadger is larger than size of current node, get its right child
    else if (newBadger.getSize() > current.getSize()) {
      // update right child when it is null; otherwise, set it as current node for recursion
      if (current.getRightLowerNeighbor() == null)
        current.setRightLowerNeighbor(newBadger);
      else
        settleHelper(current.getRightLowerNeighbor(), newBadger);
    }
    // when size of newBadger is equal to size of current node, throws exception
    else {
      throw new IllegalArgumentException("WARNING: failed to settle the badger with size "
          + newBadger.getSize()
          + ", as there is already a badger with the same size in this sett");
    }
  }
  
  /**
   * Finds a Badger of a specified size in this Sett.
   * 
   * @param size
   * @return Badger of the specified size
   */
  public Badger findBadger(int size) throws NoSuchElementException {
    return findHelper(topBadger, size);
  }
  
  /**
   * This recursive helper method is used to help find a Badger within this Sett.
   * 
   * @param current is the node (Badger) that we check currently
   * @param size
   * @return Badger of the specified size
   */
  private Badger findHelper(Badger current, int size) throws NoSuchElementException {
    // when current node is null, throw exception
    if (current == null) throw new NoSuchElementException(
        "WARNING: failed to find a badger with size " + size + " in the sett");
    
    // check left child when the specified size < current size
    if (size < current.getSize())
      return findHelper(current.getLeftLowerNeighbor(), size);
    // check right child when the specified size > current size
    else if (size > current.getSize())
      return findHelper(current.getRightLowerNeighbor(), size);
    // return current Badger when its size = specified size
    else
      return current;  
  }
  
  /**
   * Counts how many Badgers live in this Sett.
   * 
   * @return int how many nodes in this BSTree
   */
  public int countBadger() {
    return countHelper(topBadger);
  }
  
  /**
   * This recursive helper method is used to help count the number of Badgers 
   * based on the given node (Badger).
   * 
   * @param current is a node that can be condised as a root of a sub BSTree
   * @return int the number of nodes in a sub BSTree
   */
  private int countHelper(Badger current) {
    // return 0 when root of a sub tree is null
    if (current == null) return 0;
    return countHelper(current.getLeftLowerNeighbor()) + countHelper(current.getRightLowerNeighbor()) + 1;
  }
  
  /**
   * Gets all Badgers living in the Sett as a list in ascending order of their size: 
   * smallest one at index zero, through the largest one at the end (at index size-1).
   * 
   * @return List that includes all Badgers
   */
  public List<Badger> getAllBadgers() {
    List<Badger> allBadgers = new ArrayList<Badger>();
    getAllHelper(topBadger, allBadgers);
    return allBadgers;
  }
  
  /**
   * This recursive helper method is used to help collect the Badgers within this Sett into a List.
   * 
   * @param current is the node (Badger) that we check currently
   * @param allBadgers 
   */
  private void getAllHelper(Badger current, List<Badger> allBadgers) {
    // use inorder traverse to check this BSTree
    if (current != null) {
      getAllHelper(current.getLeftLowerNeighbor(), allBadgers);
      allBadgers.add(current);
      getAllHelper(current.getRightLowerNeighbor(), allBadgers);
    }
  }
  
  /**
   * Computes the height of the Sett: 
   * The number of nodes from root to the deepest leaf Badger node.
   * 
   * @return int is the hegit of a this sett
   */
  public int getHeight() {
    return getHeightHelper(topBadger);
  }
  
  /**
   * This recursive helper method is used to help compute the height of this Sett.
   * 
   * @param current
   * @return int is the height of a sub tree 
   */
  private int getHeightHelper(Badger current) {
    if (current == null) return 0;
    // height should be derived from the leaf that is farest from the root
    return Math.max(getHeightHelper(current.getLeftLowerNeighbor()), 
        getHeightHelper(current.getRightLowerNeighbor())) + 1;
  }
  
  /**
   * Retrieves the largest Badger living in this Sett.
   * 
   * @return Badger with the biggest size
   */
  public Badger getLargestBadger() {
    Badger currentBadger = topBadger;
    if (topBadger == null) return null;
    // keep go to right child until there is no right child
    while (currentBadger.getRightLowerNeighbor() != null) {
      currentBadger = currentBadger.getRightLowerNeighbor();
    }
    return currentBadger;
  }
  
  /**
   * Retrieve the top Badger within this Sett (the one that was settled first).
   * 
   * @return topBadger is the root
   */
  public Badger getTopBadger() {
    return topBadger;
  }
  
  /**
   * Checks whether this Sett is empty.
   * 
   * @return true if the root is null
   */
  public boolean isEmpty() {
    return this.topBadger == null;
  }
}
