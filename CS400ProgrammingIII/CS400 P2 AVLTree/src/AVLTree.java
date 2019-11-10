/**
 * Filename:   AVLTree.java
 * Project:    p2
 * Authors:    Debra Deppeler, Yi-Shiun Chang
 *
 * Semester:   Fall 2018
 * Course:     CS400
 * Lecture:    004 / Class Number: 46373
 * 
 * Due Date:   Before 10pm on September 24, 2018
 * Version:    1.0
 * 
 * Credits:    None
 * 
 * Bugs:       no known bugs, but not complete either
 */

import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.LinkedList;

/** 
 * Build a AVLTree object with AVLTree class
 * @param <K>
 */
public class AVLTree<K extends Comparable<K>> implements AVLTreeADT<K> {
  AVLTree<K> that = this;
  public BSTNode<K> rootNode = null;          // a tree's first node
  
  /** 
   * Represents a tree node
   * Build a tree node with constructor, accessors, and mutators 
   * @param <K>
   */
  public class BSTNode<K> {
    private K key;                  // value of each node
    private int height;             // height-position of each node in a tree
    private BSTNode<K> left, right; // left-child and right-child of each node
    
    /**
     * Constructor for a BST node.
     * @param key
     */
    public BSTNode(K key) {
      this.key = key;
      this.left = null;
      this.right = null;
      this.height = 0;
    }

    /* accessors */
    public K getId() {
      return this.key;
    }
    
    public BSTNode<K> getLeftChild() {
      return this.left;
    }
    
    public BSTNode<K> getRightChild() {
      return this.right;
    }
    
    // height is counted from root to leafs, as from 0 to n
    public int getHeight() {
      return this.height;
    }

    /* mutators */
    public void setId(K key) {
      this.key = key;
    }
    
    public void setLeftChild(BSTNode<K> node) {
      this.left = node;
    }
    
    public void setRightChild(BSTNode<K> node) {
      this.right = node;
    }
    
    public void setHeight(int height) {
      this.height = height;
    }
  }
  
  /**
   * Test all the functions in AVLTree
   * @param arg
   */
  public static void main(String[] arg) {
    leftLeftInsert();
    rightRightInsert();
    leftRightInsert();
    rightLeftInsert();
    leftLeftDelete();   
    rightRightDelete();
    leftRightDelete();
    rightLeftDelete();
    System.out.println("\nEnd");
  }
  
  
  /**
   * Check whether a tree is empty or not
   * @return boolean true if the rootNode is exist
   */
  @Override
  public boolean isEmpty() {
    return (that.rootNode == null);
  }
  
  /**
   * Insert a node into a tree, and this node's value is key
   * If tree doesn't have a rootNode, build the first node
   * @param key
   */
  @Override
  public void insert(K key) {
    try {
      this.rootNode = insertNode(this.rootNode, key);
    } catch (DuplicateKeyException e) {
      System.out.println(e.getMessage());
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }
  
  /**
   * Start searching the AVLtree from root
   * First, check each node's value and compare with key until find the suitable position for key
   * Second, use a bottom-up method, from the new node, which has value key, trace back to its
   * ancestor and do rotate to update AVLTree if necessary
   * @param currentNode a node that needs to be modified
   * @param key
   * @return BSTNode<K> a node that has been modified
   * @throws DuplicateKeyException if a node with value key is already existed in AVLTree
   * @throws IllegalArgumentException if key == null
   */
  private BSTNode<K> insertNode(BSTNode<K> currentNode, K key) 
      throws DuplicateKeyException, IllegalArgumentException {
    // if key is null, throw an IllegalArgumentException
    if (key == null) {
      throw new IllegalArgumentException("Please input a valid key");
    }
    // if currentNode is null, create a new node, because of reaching a leaf
    if (currentNode == null) {
      BSTNode<K> newNode = new BSTNode<K>(key);
      return newNode;
    }
    // otherwise, keep searching through the tree until meet a leaf, or find a duplicated node 
    else {
      switch(key.compareTo(currentNode.getId())){
      case -1:
        currentNode.setLeftChild(insertNode(currentNode.getLeftChild(), key));
        break;
      case 1:
        currentNode.setRightChild(insertNode(currentNode.getRightChild(), key));
        break;
      default:
        throw new DuplicateKeyException("A node with the same value is already existed");
      }
      // after update currentNode's left and right childNote, let's check currentNode's balanceValue
      // ancestor two levels away from a lead node, its absolute balanceValue may exceeds 1,
      // so codes below has meaning for it 
      int balanceValue = getNodeBalance(currentNode); 
      if (balanceValue < -1) { // balanceValue < -1 means sublefttree is longer than subrighttree
        switch (key.compareTo(currentNode.getLeftChild().getId())) {
        case -1: // after Left Left Case, balance is updated, so sent currentNode to its ancestor 
          return rotateRight(currentNode); 
        case 1:  // after Left Right Case, balance is updated, so sent currentNode to its ancestor 
          currentNode.setLeftChild(rotateLeft(currentNode.getLeftChild()));
          return rotateRight(currentNode);
        }
      }
      else if (balanceValue > 1) { // balanceValue < -1 means subrighttree is longer than sublefttree
        switch (key.compareTo(currentNode.getRightChild().getId())){
        case 1:  // after Right Right Case, balance is updated, so sent currentNode to its ancestor  
          return rotateLeft(currentNode);
        case -1: // after Right Left Case, balance is updated, so sent currentNode to its ancestor  
          currentNode.setRightChild(rotateRight(currentNode.getRightChild()));
          return rotateLeft(currentNode);
        }   
      }
    }
    // for leaf node's balanceValue == 0, and for -1 <= leaf node's first ancestor's balanceValue <= 1,
    // codes above(from balanceValue) has no meaning for it, return currentNode straight forward
    return currentNode;
  }
  
  /**
   * Given a node with value key
   * If it exists in a tree, delete it 
   * If it doesn't, do nothing 
   * @param key
   */
  @Override
  public void delete(K key){
    try {
      this.rootNode = deleteNode(this.rootNode, key);
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }
  
  /**
   * Start searching the AVLtree from root
   * First, check each node's value and compare with key until find the node with value key
   * Second, 
   * @param currentNode
   * @param key
   * @return
   * @throws IllegalArgumentException
   */
  private BSTNode<K> deleteNode(BSTNode<K> currentNode, K key) throws IllegalArgumentException{  
    // if key is null, throw an IllegalArgumentException
    if (key == null) {
      throw new IllegalArgumentException("Please input a valid key");
    }
    // if currentNode is null, return null
    if (currentNode == null)  
        return currentNode;  
    // otherwise, keep searching through the tree until meet the node with value key
    switch(key.compareTo(currentNode.getId())){
    case -1:
      currentNode.setLeftChild(deleteNode(currentNode.getLeftChild(), key));
      break;
    case 1:
      currentNode.setRightChild(deleteNode(currentNode.getRightChild(), key));
      break;
    case 0:
      // build a temporary node when finding the node that need to be deleted
      BSTNode<K> tempNode = null;
      // when the node doesn't have two childNodes
      // has one childNode: currentNode = tempNode = a childNode
      // has no childNode: currentNode = null, tempNode = currentNode
      if ((currentNode.getLeftChild() == null) || (currentNode.getRightChild() == null)) {
        if (currentNode.getLeftChild() == null)  
          tempNode = currentNode.getRightChild(); 
        else 
          tempNode = currentNode.getLeftChild(); 
        
        if (tempNode == null) {
          //tempNode = currentNode;  
          currentNode = null; 
        }
        else
          currentNode = tempNode;
      }
      // when the node has two childNodes, 
      // use in-order way to find the minimum node in its subrighttree, called rightMinNode
      // set tempNode = rightMinNode, and currentNode's ID = tempNode.ID
      // do recursion to update the subrighttree with currentNode's rightChild and tempNode's Id
      else {
        BSTNode<K> rightMinNode = currentNode.getRightChild();
        while (rightMinNode.getLeftChild() != null)
          rightMinNode = rightMinNode.getLeftChild();
        
        tempNode = rightMinNode;
        currentNode.setId(tempNode.getId());
        
        currentNode.setRightChild(deleteNode(currentNode.getRightChild(), tempNode.getId()));
      }
    }
    // since currentNode == null means currentNode has no childNode, return null to its ancestor
    if (currentNode == null)  
      return currentNode; 
    // since currentNode != null, we have to update its balance
    int balanceValue = getNodeBalance(currentNode);
    if (balanceValue < -1) { // balanceValue < -1 means sublefttree is longer than subrighttree
      if (getNodeBalance(currentNode.getLeftChild()) < 0) { // Left Left Case 
        return rotateRight(currentNode);
      }
      else if (getNodeBalance(currentNode.getLeftChild()) >= 0) { // Left Right Case 
        currentNode.setLeftChild(rotateLeft(currentNode.getLeftChild()));
        return rotateRight(currentNode);
      }
    }
    else if (balanceValue > 1) { // balanceValue < -1 means subrighttree is longer than sublefttree
      if ((getNodeBalance(currentNode.getRightChild()) > 0)) { // Right Right Case  
        return rotateLeft(currentNode);
      }
      else if ((getNodeBalance(currentNode.getRightChild()) <= 0)) {// Right Left Case 
        currentNode.setRightChild(rotateRight(currentNode.getRightChild()));
        return rotateLeft(currentNode);
      }
    }
    return currentNode;
  }
  
  /**
   * AVLTree rotate left
   * @param root an imbalance node
   * @return BSTNode<K> the node that its balance has been modified 
   */
  public BSTNode<K> rotateLeft(BSTNode<K> root) {
    BSTNode<K> temp = root.getRightChild();
    root.setRightChild(temp.getLeftChild());
    temp.setLeftChild(root);
    return temp;
  }
  
  /**
   * AVLTree rotate right
   * @param root an imbalance node
   * @return the node that its balance has been modified 
   */
  public BSTNode<K> rotateRight(BSTNode<K> root) {
    BSTNode<K> temp = root.getLeftChild();
    root.setLeftChild(temp.getRightChild());
    temp.setRightChild(root);
    return temp;
  }
  
  /**
   * Call exist() to search node with value key in a tree
   * @return boolean true if node with value key is exist
   */
  @Override
  public boolean search(K key) {
    if (rootNode == null)
      return false;
    try {
      return exist(key, rootNode);
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
      return false;
    }
  }

  /**
   * Search node with value key in a tree
   * @param key
   * @param currentNode
   * @return boolean true if node with value key is exist
   */
  private boolean exist(K key, BSTNode<K> currentNode) throws IllegalArgumentException {
    // if key is null, throw an IllegalArgumentException
    if (key == null) {
      throw new IllegalArgumentException("Please input a valid key");
    }
    
    switch (key.compareTo(currentNode.getId())) {
    case -1:
      if (currentNode.getLeftChild() != null)
        return exist(key, currentNode.getLeftChild());
      else
        return false;
    case 1:
      if (currentNode.getRightChild() != null)
        return exist(key, currentNode.getRightChild());
      else
        return false;
    case 0:
      return true;
    }
    
    return false;
  }  
  
  /**
   * Call print() to print out all nodes in a tree in an in-order way
   * @return String ""
   */
  @Override
  public String print() {
    ArrayList<K> inorderTraverse = inOrdorTraverseBST();
    
    System.out.print("In ascending order: ");
    for (int i=0; i<inorderTraverse.size(); i++) {
      System.out.print(inorderTraverse.get(i)+" ");
    }
    System.out.println("");
    return "";
  }

  /**
   * Call print() to print out a tree
   * @return String ""
   */
  public String printTree() {
    printSideways();
    return "";
  }
 
  /**
   * Print a tree
   */
  private void printSideways() {
    System.out.println("------------------------------------------");
    recursivePrintSideways(rootNode, "");
    System.out.println("------------------------------------------");
  }

  /**
   * Print nodes in a tree
   * @param current
   * @param indent
   */
  private void recursivePrintSideways(BSTNode<K> current, String indent) {
    if (current != null) {
      recursivePrintSideways(current.getRightChild(), indent + "    ");
      System.out.println(indent + current.getId());
      recursivePrintSideways(current.getLeftChild(), indent + "    ");
    }
  } 
  
  /**
   * Assumed tree is balanced and check whether each node in a tree is balanced or not
   * @return boolean true if all nodes in a tree are balanced
   */
  @Override
  public boolean checkForBalancedTree() {
    BSTNode<K> currentNode = this.rootNode;
    boolean balancedTree = true;
    
    return checkLoop(currentNode, null, balancedTree, null);
  }
  
  /**
   * Traverse a tree in pre-order way, if all nodes are balanced then return true
   * return false when meeting the first node that is not balanced
   * @param currentNode
   * @param balancedTree
   * @return boolean false after finding a node is not balanced
   */
  public boolean checkLoop(BSTNode<K> currentNode, BSTNode<K> preNode, boolean balancedTree, String direction) {
    if (currentNode == null)
      return true;
    if (checkNodeBalance(currentNode) == true) {
      balancedTree = checkLoop(currentNode.getLeftChild(), currentNode, balancedTree, "left") && 
          checkLoop(currentNode.getRightChild(), currentNode, balancedTree, "right");
    }
    else {
      balancedTree = false;
    }
    return balancedTree;
  }
  
  /**
   * Check whether subRightTreeDeep minus subLeftTreeDeep is between -1 and 1
   * @param currentNode
   * @return boolean true if this node's subRightTreeDeep minus subLeftTreeDeep is between -1 and 1
   */
  public boolean checkNodeBalance(BSTNode<K> currentNode) {
    if (currentNode == null)
      return true;
    if (subTreeDiff(currentNode) < -1 || subTreeDiff(currentNode) > 1) 
      return false;
    return true;
  }
  
  /**
   * Get the height difference of subrighttree and sublefttree
   * @param currentNode
   * @return int the height difference of subrighttree and sublefttree
   */
  public int getNodeBalance(BSTNode<K> currentNode) {
    if (currentNode == null)
      return 0;
    return subTreeDiff(currentNode);
  }
  
  /**
   * Compute the difference between currentNode's subRightTreeHeight and subLeftTreeHeight
   * @param currentNode
   * @return int difference of subTrees' Heights
   */
  public int subTreeDiff(BSTNode<K> currentNode) {
    int diff = nodeHeight(currentNode.getRightChild()) - nodeHeight(currentNode.getLeftChild());
    return diff;
  }
  
  /**
   * Compute how high a node is
   * @param currentNode
   * @return int height
   */
  public int nodeHeight(BSTNode<K> currentNode) {
    if (currentNode == null)
      return 0;
    return Math.max(nodeHeight(currentNode.getLeftChild()), nodeHeight(currentNode.getRightChild()))+1;
  }

  /**
   * Check if values of all nodes are retrieved in ascending order
   * @return boolean true if tree is a BST
   */
  @Override
  public boolean checkForBinarySearchTree() {
    if (this.rootNode == null)
      return true;
    
    ArrayList<K> inorderTraverse = inOrdorTraverseBST();
    for (int i=1; i<inorderTraverse.size(); i++) {
      if(inorderTraverse.get(i-1).compareTo(inorderTraverse.get(i)) == 1)
        return false;
    }
    
    return true;
  }
  
  /**
   * Traverse a tree in in-order way and record it
   * @return ArrayList<K> a list of nodes that retrieved from a traverse
   */
  public ArrayList<K> inOrdorTraverseBST(){
    BSTNode<K> currentNode = this.rootNode;
    LinkedList<BSTNode<K>> nodeStack = new LinkedList<BSTNode<K>>();
    ArrayList<K> result = new ArrayList<K>();
    
    if (currentNode == null)
      return null;
    
    while (currentNode != null || nodeStack.size() > 0) {
      while (currentNode != null) {
        nodeStack.add(currentNode);
        currentNode = currentNode.getLeftChild();
      }
      currentNode = nodeStack.pollLast();
      result.add(currentNode.getId());
      currentNode = currentNode.getRightChild();
    }
    
    return result;
  }
  
  /**
   * A test case for leftLeftInsert
   */
  private static void leftLeftInsert() {
    AVLTree<Integer> tree1 = new AVLTree<Integer>();
    System.out.println("LeftLeft Tree Insert Case");
    System.out.println("Before insert nodes, tree is empty: "+tree1.isEmpty());
    
    Integer[] numbers = {50, 60, 30, 40, 10, 20};
    for (int i=0; i<numbers.length; i++) {
      tree1.insert(numbers[i]);
    }
    
    System.out.println("After insert nodes, tree is empty: "+tree1.isEmpty());
    System.out.println("Tree looks like: ");
    tree1.printTree();
    tree1.print();
    System.out.println("Tree is balanced: "+tree1.checkForBalancedTree());
    System.out.println("Tree is a BST: "+tree1.checkForBinarySearchTree()+"\n");
  }
  
  /**
   * A test case for rightRightInsert
   */
  private static void rightRightInsert() {
    System.out.println("RightRight Tree Insert Case");
    AVLTree<Integer> tree1 = new AVLTree<Integer>();
    System.out.println("Before insert nodes, tree is empty: "+tree1.isEmpty());
    
    Integer[] numbers = {50, 60, 30, 70, 55, 65};
    for (int i=0; i<numbers.length; i++) {
      tree1.insert(numbers[i]);
    }
    
    System.out.println("After insert nodes, tree is empty: "+tree1.isEmpty());
    System.out.println("Tree looks like: ");
    tree1.printTree();
    tree1.print();
    System.out.println("Tree is balanced: "+tree1.checkForBalancedTree());
    System.out.println("Tree is a BST: "+tree1.checkForBinarySearchTree()+"\n");
  }
  
  /**
   * A test case for leftRightInsert
   */
  private static void leftRightInsert() {
    System.out.println("LeftRight Tree Insert Case");
    AVLTree<Integer> tree1 = new AVLTree<Integer>();
    System.out.println("Before insert nodes, tree is empty: "+tree1.isEmpty());
    
    Integer[] numbers = {50, 60, 30, 10, 40, 35};
    for (int i=0; i<numbers.length; i++) {
      tree1.insert(numbers[i]);
    }
    
    System.out.println("After insert nodes, tree is empty: "+tree1.isEmpty());
    System.out.println("Tree looks like: ");
    tree1.printTree();
    tree1.print();
    System.out.println("Tree is balanced: "+tree1.checkForBalancedTree());
    System.out.println("Tree is a BST: "+tree1.checkForBinarySearchTree()+"\n");
  }
  
  /**
   * A test case for rightLeftInsert
   */
  private static void rightLeftInsert() {
    System.out.println("RightLeft Tree Insert Case");
    AVLTree<Integer> tree1 = new AVLTree<Integer>();
    System.out.println("Before insert nodes, tree is empty: "+tree1.isEmpty());
    
    Integer[] numbers = {50, 60, 30, 70, 55, 57};
    for (int i=0; i<numbers.length; i++) {
      tree1.insert(numbers[i]);
    }
    
    System.out.println("After insert nodes, tree is empty: "+tree1.isEmpty());
    System.out.println("Tree looks like: ");
    tree1.printTree();
    tree1.print();
    System.out.println("Tree is balanced: "+tree1.checkForBalancedTree());
    System.out.println("Tree is a BST: "+tree1.checkForBinarySearchTree()+"\n");
  }
  
  /**
   * A test case for leftLeftDelete
   */
  private static void leftLeftDelete() {
    System.out.println("LeftLeft Tree Delete Case");
    AVLTree<Integer> tree1 = new AVLTree<Integer>();
    System.out.println("Before insert nodes, tree is empty: "+tree1.isEmpty());
    
    Integer[] numbers = {60, 50, 70, 30, 65, 55, 20, 40};
    for (int i=0; i<numbers.length; i++) {
      tree1.insert(numbers[i]);
    }
    
    System.out.println("After insert nodes, tree is empty: "+tree1.isEmpty());
    System.out.println("Tree looks like: ");
    tree1.printTree();
    tree1.print();
    
    tree1.delete(65);
    System.out.println("After delete nodes 65, Tree looks like: "); 
    tree1.printTree();
    tree1.print();
    
    System.out.println("Tree is balanced: "+tree1.checkForBalancedTree());
    System.out.println("Tree is a BST: "+tree1.checkForBinarySearchTree()+"\n");
  }
  
  /**
   * A test case for rightRightDelete
   */
  private static void rightRightDelete() {
    System.out.println("LeftLeft Tree Delete Case");
    AVLTree<Integer> tree1 = new AVLTree<Integer>();
    System.out.println("Before insert nodes, tree is empty: "+tree1.isEmpty());
    
    Integer[] numbers = {50, 30, 60, 40, 70, 55, 65};
    for (int i=0; i<numbers.length; i++) {
      tree1.insert(numbers[i]);
    }
    
    System.out.println("After insert nodes, tree is empty: "+tree1.isEmpty());
    System.out.println("Tree looks like: ");
    tree1.printTree();
    tree1.print();
    
    tree1.delete(40);
    System.out.println("After delete nodes 40, Tree looks like: "); 
    tree1.printTree();
    tree1.print();
    
    System.out.println("Tree is balanced: "+tree1.checkForBalancedTree());
    System.out.println("Tree is a BST: "+tree1.checkForBinarySearchTree()+"\n");
  }
  
  /**
   * A test case for leftRightDelete
   */
  private static void leftRightDelete() {
    System.out.println("LeftRight Tree Delete Case");
    AVLTree<Integer> tree1 = new AVLTree<Integer>();
    System.out.println("Before insert nodes, tree is empty: "+tree1.isEmpty());
    
    Integer[] numbers = {50, 30, 60, 40, 10, 70, 35};
    for (int i=0; i<numbers.length; i++) {
      tree1.insert(numbers[i]);
    }
    
    System.out.println("After insert nodes, tree is empty: "+tree1.isEmpty());
    System.out.println("Tree looks like: ");
    tree1.printTree();
    tree1.print();
    
    tree1.delete(70);
    System.out.println("After delete nodes 70, Tree looks like: "); 
    tree1.printTree();
    tree1.print();
    
    System.out.println("Tree is balanced: "+tree1.checkForBalancedTree());
    System.out.println("Tree is a BST: "+tree1.checkForBinarySearchTree()+"\n");
  }
  
  /**
   * A test case for rightLeftDelete, search()
   */
  private static void rightLeftDelete() {
    System.out.println("RightLeft Tree Delete Case");
    AVLTree<Integer> tree1 = new AVLTree<Integer>();
    System.out.println("Before insert nodes, tree is empty: "+tree1.isEmpty());
    
    Integer[] numbers = {50, 30, 60, 40, 55, 70, 57};
    for (int i=0; i<numbers.length; i++) {
      tree1.insert(numbers[i]);
    }
    
    System.out.println("After insert nodes, tree is empty: "+tree1.isEmpty());
    System.out.println("Tree looks like: ");
    tree1.printTree();
    tree1.print();
    
    tree1.delete(40);
    System.out.println("After delete nodes 40, Tree looks like: "); 
    tree1.printTree();
    tree1.print();
    System.out.println("Tree is balanced: "+tree1.checkForBalancedTree());
    System.out.println("Tree is a BST: "+tree1.checkForBinarySearchTree()+"\n");
    
    System.out.println("Does 40 exist in tree? "+tree1.search(40));
    System.out.println("Does 50 exist in tree? "+tree1.search(50));
    System.out.print("Does null exist in tree? ");
    System.out.println(tree1.search(null));
    System.out.println("Try to insert 55 again: ");
    tree1.insert(55);
    System.out.println("Try to insert null: ");
    tree1.insert(null);
    System.out.println("Try to delete null: ");
    tree1.delete(null);
    System.out.println("Try to delete 100: nothing happen!");
    tree1.delete(100);
    tree1.print();
  }
}