/**
 * Filename:   TestAVLTree.java
 * Project:    p2
 * Authors:    Debra Deppeler, Yi-Shiun Chang
 *
 * Semester:   Fall 2018
 * Course:     CS400
 * Lecture:    004 / Class Number: 46373
 * 
 * Due Date:   as specified in Canvas
 * Version:    1.0
 * 
 * Credits:    none
 * 
 * Bugs:       no known bugs, but not complete either
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import org.junit.Test;

/** TODO: add class header comments here*/
public class TestAVLTree {

  /**
   * Tests that an AVLTree is empty upon initialization.
   */
  @Test
  public void test01isEmpty() {
    AVLTree<Integer> tree = new AVLTree<Integer>();
    assertTrue(tree.isEmpty());
  }

  /**
   * Tests that an AVLTree is not empty after adding a node.
   */
  @Test
  public void test02isNotEmpty() {
    AVLTree<Integer> tree = new AVLTree<Integer>();
    tree.insert(1);
    assertFalse(tree.isEmpty());
  }
  
  /**
   * Tests functionality of a single delete following several inserts.
   */
  @Test
  public void test03insertManyDeleteOne() {
    AVLTree<Integer> tree = new AVLTree<Integer>();
    ArrayList<Integer> numbers = new ArrayList<Integer>();
    ArrayList<Integer> inorderTraverse;
    Integer[] numbersArray = {50, 60, 30, 70, 55, 65};
    
    // update ArrayList "numbers" with Integer[] "numbersArray"
    for (int i=0; i<numbersArray.length; i++) {
      numbers.add(numbersArray[i]);
    }
    
    // insert nodes into tree by ArrayList "numbers"
    for (int i=0; i<numbers.size(); i++) {
      tree.insert(numbers.get(i));
    }
    
    // remove numbers[2], which is 30
    numbers.remove(2);
    // delete node, which value is 30 
    tree.delete(30);
    
    // check the values of nodes in the tree are in the ArrayList "numbers"
    // and check the size of tree and "numbers" are the same
    inorderTraverse = tree.inOrdorTraverseBST();
    for (int i = 0; i < inorderTraverse.size(); i++) {
      boolean pass = false;
      for (Integer number: numbers) {
        if (inorderTraverse.get(i) == number)
          pass = true;
      }
      assertTrue(pass);
    }
    assertEquals(inorderTraverse.size(), numbers.size());
  }
  
  /**
   * Tests functionality of many deletes following several inserts.
   */
  @Test
  public void test04insertManyDeleteMany() {
    AVLTree<Integer> tree = new AVLTree<Integer>();
    ArrayList<Integer> numbers = new ArrayList<Integer>();
    ArrayList<Integer> inorderTraverse;
    
    // make an ArrayList<Integer> numbers with values 0-499
    for (int i = 0; i < 500; i++) {
      numbers.add(i);
    }
    
    // insert 0-499 into tree
    for (int i = 0; i<numbers.size(); i++) {
      tree.insert(numbers.get(i));
    }
    assertFalse(tree.isEmpty());
    
    // delete nodes with values 300-499
    for (int i = 300; i < 1000000; i++) {
      tree.delete(i);
    }
    
    inorderTraverse = tree.inOrdorTraverseBST();
    
    // check whether tree is empty, balanced, BST, 
    // and whether its size is equal to 300 
    assertFalse(tree.isEmpty());
    assertTrue(tree.checkForBalancedTree());
    assertTrue(tree.checkForBinarySearchTree());
    assertEquals(inorderTraverse.size(), 300);
  }

  /**
   * Tests functionality of operations on an emptyTree
   */
  @Test
  public void test05correctOperationsOnAnEmptyTree() {
    AVLTree<Integer> tree = new AVLTree<Integer>();
    tree.delete(3);
    
    assertFalse(tree.search(3));
    assertTrue(tree.isEmpty());
    assertTrue(tree.checkForBalancedTree());
    assertTrue(tree.checkForBinarySearchTree());
  }
  
  /**
   * Tests functionality of operations on a tree with one item
   */
  @Test
  public void test06correctOperationsOnTreesWithOneItem () {
    AVLTree<Integer> tree = new AVLTree<Integer>();
    tree.insert(100);
    assertFalse(tree.isEmpty());
    assertTrue(tree.checkForBalancedTree());
    assertTrue(tree.checkForBinarySearchTree());
    assertTrue(tree.search(100));
    tree.delete(100);
    assertTrue(tree.isEmpty());
  }
  
  /**
   * Tests functionality of Operations On a Tree 
   * With A Few To Several Items Added And Deleted
   */
  @Test
  public void test07correctOperationsOnTreesWithAFewToSeveralItemsAddedAndDeleted () {
    AVLTree<Integer> tree = new AVLTree<Integer>();
    ArrayList<Integer> inorderTraverse;
    
    // add nodes with values from 0-499
    for (int i = 0; i < 500; i++) {
      tree.insert(i);
    }
    
    // delete nodes with values from 300-699
    for (int i = 300; i < 700; i++) {
      tree.delete(i);
    }
    
    // add nodes with values from 250-599
    for (int i = 250; i < 600; i++) {
      tree.insert(i);
    }
    
    // delete nodes with values from 550-699
    for (int i = 550; i < 700; i++) {
      tree.delete(i);
    }
    assertFalse(tree.isEmpty());
    assertTrue(tree.checkForBalancedTree());
    assertTrue(tree.checkForBinarySearchTree());
    inorderTraverse = tree.inOrdorTraverseBST();
    assertEquals(inorderTraverse.size(), 550);    
  }
  
  /**
   * Tests functionality of Operations On LeftLeft Insert
   */
  @Test
  public void test08leftLeftInsert() {
    AVLTree<Integer> tree = new AVLTree<Integer>();
    
    Integer[] numbers = {50, 60, 30, 40, 10, 20};
    for (int i=0; i<numbers.length; i++) {
      tree.insert(numbers[i]);
    }

    assertTrue(tree.checkForBalancedTree());
    assertTrue(tree.checkForBinarySearchTree());
  }
  
  /**
   * Tests functionality of Operations On RightRight Insert
   */
  @Test
  public void test09rightRightInsert() {
    AVLTree<Integer> tree = new AVLTree<Integer>();
    
    Integer[] numbers = {50, 60, 30, 70, 55, 65};
    for (int i=0; i<numbers.length; i++) {
      tree.insert(numbers[i]);
    }
    
    assertTrue(tree.checkForBalancedTree());
    assertTrue(tree.checkForBinarySearchTree());
  }
  
  /**
   * Tests functionality of Operations On LefttRight Insert
   */
  @Test
  public void test10leftRightInsert() {
    AVLTree<Integer> tree = new AVLTree<Integer>();
    
    Integer[] numbers = {50, 60, 30, 10, 40, 35};
    for (int i=0; i<numbers.length; i++) {
      tree.insert(numbers[i]);
    }
    
    assertTrue(tree.checkForBalancedTree());
    assertTrue(tree.checkForBinarySearchTree());
  }
  
  /**
   * Tests functionality of Operations On RightLeft Insert
   */
  @Test
  public void test11rightLeftInsert() {
    AVLTree<Integer> tree = new AVLTree<Integer>();
    
    Integer[] numbers = {50, 60, 30, 70, 55, 57};
    for (int i=0; i<numbers.length; i++) {
      tree.insert(numbers[i]);
    }
    
    assertTrue(tree.checkForBalancedTree());
    assertTrue(tree.checkForBinarySearchTree());
  }
  
  /**
   * Tests functionality of Operations On LeftLeft Delete
   */
  @Test
  public void test12leftLeftDelete() {
    AVLTree<Integer> tree = new AVLTree<Integer>();
    
    Integer[] numbers = {60, 50, 70, 30, 65, 55, 20, 40};
    for (int i=0; i<numbers.length; i++) {
      tree.insert(numbers[i]);
    }
    
    tree.delete(65);
    assertTrue(tree.checkForBalancedTree());
    assertTrue(tree.checkForBinarySearchTree());
  }
  
  /**
   * Tests functionality of Operations On RightRight Delete
   */
  @Test
  public void test13rightRightDelete() {
    AVLTree<Integer> tree = new AVLTree<Integer>();
    
    Integer[] numbers = {50, 30, 60, 40, 70, 55, 65};
    for (int i=0; i<numbers.length; i++) {
      tree.insert(numbers[i]);
    }
    
    tree.delete(40);
    assertTrue(tree.checkForBalancedTree());
    assertTrue(tree.checkForBinarySearchTree());
  }
  
  /**
   * Tests functionality of Operations On LeftRight Delete
   */
  @Test
  public void test14leftRightDelete() {
    AVLTree<Integer> tree = new AVLTree<Integer>();
    
    Integer[] numbers = {50, 30, 60, 40, 10, 70, 35};
    for (int i=0; i<numbers.length; i++) {
      tree.insert(numbers[i]);
    }
    
    tree.delete(70);
    assertTrue(tree.checkForBalancedTree());
    assertTrue(tree.checkForBinarySearchTree());
  }
  
  /**
   * Tests functionality of Operations On RightLeft Delete
   */
  @Test
  public void test15rightLeftDelete() {
    AVLTree<Integer> tree = new AVLTree<Integer>();
    
    Integer[] numbers = {50, 30, 60, 40, 55, 70, 57};
    for (int i=0; i<numbers.length; i++) {
      tree.insert(numbers[i]);
    }
    
    tree.delete(40);
    assertTrue(tree.checkForBalancedTree());
    assertTrue(tree.checkForBinarySearchTree());
  }
}