import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * Implementation of a BPTree with efficient access to many different indexes of a large data set:
 * 1. BPTree objects are created for each type of index needed by the program.  
 * 2. BPTrees provide an efficient range search as compared to other types of data structures due 
 *    to the ability to perform log_m N lookups and linear in-order traversals of the data items.
 * 
 * @author YiShiun, Chang
 *
 * @param <K> key - expect a generic that can represent each item
 * @param <V> value - expect a user-defined type that stores all data for a food item
 */
public class BPTree<K extends Comparable<K>, V> implements BPTreeADT<K, V> {
  // root of the tree
  private Node root;
  // branching factor is the max number of child-nodes that a nodes in the tree can have
  private int branchingFactor;
    
  /**
   * Public constructor
   * 
   * @param branchingFactor 
   */
  public BPTree(int branchingFactor) {
    // 2-3 tree is the basic example of B+ tree, 3 represents the max number of child-nodes
    // if branchingFactor < 3, this tree is not a BPTree, it is just a binary tree
    if (branchingFactor < 3)
      throw new IllegalArgumentException("Illegal branching factor: " + branchingFactor);
    this.branchingFactor = branchingFactor;
    root = new LeafNode(); // root is also a leafNode when initializing a BPTree
  }
    
  /**
   * Insert new node into this tree
   */
  @Override
  public void insert(K key, V value) {
    root.insert(key, value);
  }
    
  /**
   * Search nodes with a given key and comparator. In this project, key is value for a 
   * specific nutrient, such as protein. As a result, we may search a FoodItem its with protein
   * is >= 100, or == 100, or <= 100.
   */
  @Override
  public List<V> rangeSearch(K key, String comparator) {
    if (!comparator.contentEquals(">=") && 
        !comparator.contentEquals("==") && 
        !comparator.contentEquals("<=") )
      return null; 
    return root.rangeSearch(key, comparator);
  }
    
  /**
   * Print out this BPTree, it is presented in a hierarchic way with each item's key
   */
  @Override
  public String toString() {
    Queue<List<Node>> queue = new LinkedList<List<Node>>();
    queue.add(Arrays.asList(root));
    StringBuilder sb = new StringBuilder();
    while (!queue.isEmpty()) {
      Queue<List<Node>> nextQueue = new LinkedList<List<Node>>();
      while (!queue.isEmpty()) {
        List<Node> nodes = queue.remove();
        sb.append('{');
        Iterator<Node> it = nodes.iterator();
        while (it.hasNext()) {
          Node node = it.next();
          sb.append(node.toString());
          if (it.hasNext())
              sb.append(", ");
          if (node instanceof BPTree.InternalNode)
              nextQueue.add(((InternalNode) node).children);
        }
        sb.append('}');
        if (!queue.isEmpty())
          sb.append(", ");
        else {
          sb.append('\n');
        }
      }
      queue = nextQueue;
    }
    return sb.toString();
  } 
    
  /**
   * This abstract class represents any type of node in the tree
   * This class is a super class of the LeafNode and InternalNode types.
   * 
   * Each node in a BPTree can contain multiple key-value pairs.
   * The maximum amount of pairs in a node is branchFacotr-1.
   * 
   * The root in a BPTree is a leafNode at the beginning, and changes into an internalNode later.
   */
  private abstract class Node {
    List<K> keys; // list of keys
    
    Node() {
      this.keys = new ArrayList<K>();
    }

    abstract void insert(K key, V value);

    abstract K getFirstLeafKey();
      
    abstract Node split();
      
    abstract List<V> rangeSearch(K key, String comparator);

    abstract boolean isOverflow();
      
    public String toString() {
      return keys.toString();
    }
  }
  
  /**
   * InternalNode is a corresponding class to LeafNode. Thanks to the BPTree structure we choose,
   * nodes in this BPTree have different functions, so check the structure to understand why we
   * need internalNodes and leafNodes.
   * 
   * An internalNode only stores keys for key-value pairs within it, while a leafNode stores both
   * keys and values for for key-value pairs within it.
   * In this project, key is a number for a given foodItem, which is the corresponding value.
   * For example, this BPTree may represents fat, so every foodItems use themselves as values
   * while their fat-values as keys.
   */
  private class InternalNode extends Node {
    List<Node> children; // list of children nodes
      
    InternalNode() {
      super(); // each node needs its keys (a list) to store keys for key-value pairs () in it
      this.children = new ArrayList<Node>(); // an internalNode has its child-nodes
    }
      
    /**
     * If children.get(0) is an InternalNode, it will call InternalNode.getFirstKey()
     * If children.get(0) is a LeafNode, then it will call LeafNode.getFirstLeafKey(), 
     * then return key of the first key-value pair 
     */
    K getFirstLeafKey() {
      return children.get(0).getFirstLeafKey();
    }
      
    /**
     * An InternalNode can only contain branchingFactor children at most
     */
    boolean isOverflow() {
      return children.size() > branchingFactor;
    }
     
    /**
     * Insert a key-value pair into this BPTree. 
     * Key is value of a specific nutrient for a foodItem.
     * Value is that foodItem.
     */
    void insert(K key, V value) {
      // get the index of the given key within this internalNode, if it is contained in the list; 
      // otherwise, return (-(insertion point) - 1)
      // the insertion point is where the key would be inserted into keys
      int index = Collections.binarySearch(keys, key);
      // if index >= 0, it means that this BPTree already has a pair that contains the given key,
      // in this case, we search the index+1th child node of the ArrayList children
      // if index < 0, it means that this BPTree may not have a pair that contains the given key,
      // let's check the -index-1th child, because it may contains the pair we wanted
      int childIndex = index >= 0 ? index + 1 : -index - 1;
      Node child = children.get(childIndex);
      // this is like a recursive route, note that both InternalNode and LeafNode have insert()
      // when the child is a InternalNode, we use InternalNode.insert() and repeat the process,
      // so eventually, we will reach a LeafNode and update the LeafNode
      // when we get the child as a LeafNode, we use LeafNode.insert() to update a key-value pair
      child.insert(key, value);
      // if the child is LeafNode and overflow: create a sibling as a leafNode by LeafNode.split()
      // the first key of this sibling will be added to the keys of its parentNode (internalNode),
      // so we should get the index for this first key in order to add it to the internalNode's keys
      // note that the index is a parentIndex, which is used in sibling's parentNode (internalNode)
      
      // for the first key 
      // if parentIndex < 0, it means "keys" of InternalNode doesn't contain the key
      // we then add key and child to InternalNode's keys and children
      // if parentIndex >= 0, it means "keys" of InternalNode contains the key
      // we just update the existed child, which is within the children
      
      // when the child is LeafNode, it may update the keys of its parentNode (internalNode)
      // and this internalNode may hence become overflow
      // in this case, let's check the parentNode of this InternalNode
      // this parentNode is like an internalNode's internalNode, parent's parent
      // therefore, the child is InternalNode now:
      // if it is overflow: create a sibling as a InternalNode by InternalNode.split()
      // then do a similar process in the case of child is LeafNode
      if (child.isOverflow()) {
        Node sibling = child.split();
        int parentIndex = Collections.binarySearch(keys, sibling.getFirstLeafKey());
        if (parentIndex < 0) {
          keys.add(-parentIndex - 1, sibling.getFirstLeafKey());
          children.add(-parentIndex - 1 + 1, sibling);
        } else {
          children.set(parentIndex, sibling);
        }
      }
      // when we insert a pair, BPTree always starts from root, and during the process of inserts,
      // we first update the specific LeafNode that we insert to, and then
      // we update InternalNodes that on the path to the specific LeafNode
      // as a result, the root may have been updated too, and we check whether it overflow
      if (root.isOverflow()) {
        Node sibling = split();
        InternalNode newRoot = new InternalNode();
        newRoot.keys.add(sibling.getFirstLeafKey());
        newRoot.children.add(this);
        newRoot.children.add(sibling);
        root = newRoot;
      }
    }
    
    /**
     * Split an internalNode when it is overflow, please be careful about the splitFrom/To
     */
    Node split() {
      InternalNode sibling = new InternalNode();
      int splitFrom = keys.size() / 2 + 1;
      int splitTo = keys.size();
      sibling.keys.addAll(keys.subList(splitFrom, splitTo));
      sibling.children.addAll(children.subList(splitFrom, splitTo+1));
      keys.subList(splitFrom - 1, splitTo).clear();
      children.subList(splitFrom, splitTo+1).clear();
      
      return sibling;
    }
    
    /**
     * For a internalNode's rangeSearch, its purpose is to find the index of a given key,
     * then searches the child-node of that index
     */
    List<V> rangeSearch(K key, String comparator) {
      int index = Collections.binarySearch(keys, key);
      int childIndex = index >= 0 ? index + 1 : -index - 1;
      return children.get(childIndex).rangeSearch(key, comparator);
    }
  } 
    
  /**
   * In this BPTree structure, LeafNodes store all the key-value pairs. 
   * InternalNodes and the root do not store values, they only store keys for searching purpose.
   * This structure is different from an initiative way, please check structure picture. 
   */
  private class LeafNode extends Node {
    List<ArrayList<V>> values; // list of values
    LeafNode next; // reference to the next leaf node
    LeafNode previous; // reference to the previous leaf node
    
    /**
     * Constructor of a new LeafNode
     */
    LeafNode() {
      super(); // create a list of keys
      this.values = new ArrayList<ArrayList<V>>();
      this.next = null;
      this.previous = null;
    }
    
    /**
     * @return k the first key of this leafNode
     */
    K getFirstLeafKey() {
      return keys.get(0);
    }
      
    /**
     * @return true if the number of key-value pairs in this node exceeds/equals branchingFactor
     */
    boolean isOverflow() {
      return values.size() >= branchingFactor;
    }
        
    /**
     * Insert a key-value pair into this LeafNode
     */
    void insert(K key, V value) {
      // get the index of the given key within this LeafNode 
      // if it is contained in the list; otherwise, return (-(insertion point) - 1). 
      // the insertion point is where the key would be inserted into keys
      int index = Collections.binarySearch(keys, key);
      // index < 0 means key doesn't exist and let's set it up
      if (index < 0) {
        keys.add(-index-1, key);
        ArrayList<V> newValue = new ArrayList<V>();
        newValue.add(value);
        values.add(-index-1, newValue);
      // index >= 0 means key exists in this leafNode
      // usually, we only update values when duplicate keys are not allowed
      // but in this project, we should allow duplicate keys for foodItems that have the same 
      // value of a specific nutrient. For example, fish and chicken may both have fat = 100
      } else {
        values.get(index).add(value);
      }
      
      // root can either be a LeafNode or an InternalNode, 
      // if root is a LeafNode and it is overflow, update the root as an InternalNode while
      // updating its keys and children
      if (root.isOverflow()) {
        Node sibling = split();
        // create an InternalNode (parent node) for two LeafNodes
        InternalNode newRoot = new InternalNode();        
        // InternalNode (parent node) only stores keys for searching purpose
        newRoot.keys.add(sibling.getFirstLeafKey());
        newRoot.children.add(this);
        newRoot.children.add(sibling);
        root = newRoot;
      }
    }
      
    /**
     * Split a LeafNode into two when it is overflow
     */
    Node split() {
      LeafNode sibling = new LeafNode();
      int splitFrom = (keys.size()+1) / 2;
      int splitTo = keys.size();
      // the new LeafNode contains half of the key-value pairs in the old LeafNode
      sibling.keys.addAll(keys.subList(splitFrom, splitTo));
      sibling.values.addAll(values.subList(splitFrom, splitTo));
      this.next = sibling;
      // the old LeafNode only contains the first part of the original key-value pairs
      keys.subList(splitFrom, splitTo).clear();
      values.subList(splitFrom, splitTo).clear();
      sibling.previous = this;
      
      return sibling;
    }
    
    /**
     * Before calling the leafNode's rangeSearch, there could be several calls of internalNode's 
     * rangeSearch. However, once reaching the leafNode's rangeSearch, a List that contains all
     * values of the pairs that qualify the given key and comparator is returned. 
     */
    List<V> rangeSearch(K key, String comparator) {
      List<V> res = new ArrayList<V>(); // the result
      LeafNode node = this; // this leafNode contains pairs that qualify the given key and comparator
      
      // we switch to its neighbor after finishing checking this node, so the while loop ends 
      // when node is null
      while (node != null) {
        Iterator<K> kIter = node.keys.iterator(); // prepare for iterating this node's keys
        Iterator<ArrayList<V>> vIter = node.values.iterator(); // prepare for iterating this node's values
        while (kIter.hasNext()) {
          K currentKey = kIter.next();
          // a key may have multiple values because a given value of a nutirent may correspond to 
          // several foodItems
          ArrayList<V> currentValue = vIter.next(); 
          int compare = currentKey.compareTo(key);
          if (comparator.equals("<=") && (compare == -1 || compare == 0)) {
            for (V value: currentValue)
              res.add(value);
          }            
          else if (comparator.equals("==") && compare == 0) {
            for (V value: currentValue)
              res.add(value);
          }
          else if (comparator.equals(">=") && (compare == 0 || compare == 1)) {
            for (V value: currentValue)
              res.add(value);
          }
        }

        // when this node's kIter has no more keys, switch to this node's neighbor
        if (comparator.equals("<="))
          node = node.previous;
        else if (comparator.equals(">="))
          node = node.next;
        else
          node = null;
      }
      return res;
    }
  } 
    
    
  /**
   * Contains a basic test scenario for a BPTree instance.
   * It shows a simple example of the use of this class
   * and its related types.
   * 
   * @param args
   */
  public static void main(String[] args) {
      // create empty BPTree with branching factor of 3
      BPTree<Double, Double> bpTree = new BPTree<>(3);

      // create a pseudo random number generator
      Random rnd1 = new Random();

      // some value to add to the BPTree
      Double[] dd = {0.0d, 0.5d, 0.2d, 0.8d};

      // build an ArrayList of those value and add to BPTree also
      // allows for comparing the contents of the ArrayList 
      // against the contents and functionality of the BPTree
      // does not ensure BPTree is implemented correctly
      // just that it functions as a data structure with
      // insert, rangeSearch, and toString() working.
      List<Double> list = new ArrayList<>();
      for (int i = 0; i < 400; i++) {
          Double j = dd[rnd1.nextInt(4)];
          list.add(j);
          bpTree.insert(j, j);
          System.out.println("\n\nTree structure:\n" + bpTree.toString());
      }
      List<Double> filteredValues = bpTree.rangeSearch(0.2d, "<=");//
      System.out.println("Filtered values: " + filteredValues.toString());
  }
} // End of class BPTree
