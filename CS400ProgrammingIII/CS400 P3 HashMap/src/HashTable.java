/**
 * Filename:   HashTable.java
 * Project:    p3
 * Authors:    Yi-Shiun, Chang (004 / Class Number: 46373)
 *
 * Semester:   Fall 2018
 * Course:     CS400
 * 
 * Due Date:   Before 23:59pm on October 23, 2018
 * Version:    1.0
 * 
 * Credits:    None
 * 
 * Bugs:       None
 */
import java.util.ArrayList;
import java.util.NoSuchElementException;
    
/**
 * This is a HashTable class built for practicing.
 * The collision resolution is "Chained Buckets", and the data structure of each bucket is
 * a series of nodes that are linked one by one. When put(), get(), and remove() a hash node
 * with key, this hash table first finds the index-th bucket, and then checks the linked-nodes
 * of the index-th bucket, until reaching the end of the chain or finding the node with key. 
 * Each bucket (linked-nodes) could have different length, but the worst case of time complexity
 * for put(), get(), and remove() are the same: they all have O(n) where n = hashArraySize.
 * 
 * Since every Object in Java has its own hash code, I get the hash code by K.hashCode() and
 * modulo it by the number of buckets to get the index for this hash table. 
 *  
 * @author Yi-Shiun, Chang
 * @param <K>
 * @param <V>
 */
public class HashTable<K extends Comparable<K>, V> implements HashTableADT<K, V> {
  private ArrayList<HashNode<K, V>> hashArray; // a hashArray with chained buckets
  private int chainedBuckets; // number of chainedBuckets
  private int hashArraySize; // number of HashNodes in this hash array
  private double loadFactor; // threshold of expanding and rehashing
  
  /**
   * Inner class of HashTable for building each hash node
   *
   * @param <K> key of a hash node
   * @param <V> value of a hash node
   */
  private class HashNode<K, V> { 
    K key; 
    V value; 
    // each hash node needs a next field to chain with another node
    HashNode<K, V> next; 
    
    /**
     * Constructor for K key and V value
     * 
     * @param key
     * @param value
     */
    public HashNode(K key, V value) { 
      this.key = key; 
      this.value = value; 
    } 
  }
  
  /**
   * Constructor of HashTable class, initialize chainedBuckets = 11 and null 
   * for each chained bucket, and initialize loadFactor = 0.75, and
   * initialize this.hashArraySize = 0
   */
	public HashTable() {
	  this.hashArray = new ArrayList<>();
	  this.chainedBuckets = 11;
	  this.loadFactor = 0.75; // loadFactor is usually between 0.7 and 0.8
	  this.hashArraySize = 0;
	  
	  for (int i=0; i<this.chainedBuckets; i++) {
	    hashArray.add(null);
	  }
	}
	
	/**
	 * Constructor of HashTable class, 
	 * initialize chainedBuckets = initialCapacity and null for each chained bucket, and
	 * initialize this.loadFactor = loadFactor, and
	 * initialize this.hashArraySize = 0
	 * 
	 * @param initialCapacity
	 * @param loadFactor
	 */
	public HashTable(int initialCapacity, double loadFactor) {
	  this.hashArray = new ArrayList<>();
    this.chainedBuckets = initialCapacity;
    this.loadFactor = loadFactor;
    this.hashArraySize = 0;
    
    for (int i=0; i<this.chainedBuckets; i++) {
      hashArray.add(null);
    }
	}
	
	/**
	 * Get how many hash nodes are in this hash table 
	 * 
	 * @return int number of keys in this hash table 
	 */
  @Override
  public int size() {
    return hashArraySize;
  }
  
  /**
   * This bucketSize() is used to check whether chainedBuckets has increased
   * after load-factor exceeded threshold
   * 
   * @return int 
   */
  public int bucketSize() {
    return chainedBuckets;
  }
	
	/**
	 * getIndex() is a hash function that finds index for a key 
	 * 
	 * @param key K of a hash node
	 * @return int index of this hash array
	 */
  private int getIndex(K key) { 
    int hashCode = key.hashCode(); // every Object in Java has its own hash code
    int index = hashCode % chainedBuckets; // hash code modulo by chainedBuckets
    return index; 
  } 
	
  /**
   * put() inserts a new hash node into this hashArray, and rehashes this hashArray
   * when load factor goes beyond threshold
   * 
   * @throws IllegalArgumentException when inserting a node with key = null 
   */
	@Override
	public void put(K key, V value) throws IllegalArgumentException {
	  if (key == null)
	    throw new IllegalArgumentException("key can't be null for a hash node.");
	  
    int index = getIndex(key); // get index of a given key 
    HashNode<K, V> head = hashArray.get(index); // get the first hash node of the index-th bucket

    // traverse the chain of the index-th bucket
    // check whether a node with key is already existed, update its value if it existed
    while (head != null) { 
      if (head.key.equals(key)) { 
          head.value = value; 
          break; 
      } 
      head = head.next; 
    } 
    
    // if there is no hash node with key, create a new hash node with key and insert in chain 
    // the new hash node is inserted in the first place of the index-th bucket
    if (head == null) {
      head = hashArray.get(index); 
      HashNode<K, V> newNode = new HashNode<K, V>(key, value); 
      newNode.next = head; 
      hashArray.set(index, newNode); 
      hashArraySize ++;
    }

    // if load factor goes beyond threshold, then new chainedBucket = 2*chainedBucket+1
    if ((double) hashArraySize/chainedBuckets >= loadFactor) { 
      ArrayList<HashNode<K, V>> temp = hashArray; // create a temp array to store nodes
      hashArray = new ArrayList<>(); // reset hashArray
      this.chainedBuckets = 2*this.chainedBuckets+1; // new chainedBucket = 2*chainedBucket+1
      hashArraySize = 0; // hashArraySize will be updated later
      
      // set new hashArray to default
      for (int i = 0; i < chainedBuckets; i++) 
        hashArray.add(null); 
      
      // put each node of temp array into new hashArray
      for (HashNode<K, V> headNode : temp) { 
        while (headNode != null) { 
          put(headNode.key, headNode.value); // index would be updated when recalling put()
          headNode = headNode.next; 
        } 
      } 
    } 
	}
	
	/**
	 * get() searches a node with node.key = key, and returns its value
	 * 
	 * @return V value of a node with node.key = key
	 * @throws IllegalArgumentException when trying to get a node with key = null 
	 * @throws NoSuchElementException when a node with key doesn't exist
	 */
	@Override
	public V get(K key) throws IllegalArgumentException, NoSuchElementException {
	  if (key == null)
      throw new IllegalArgumentException("key can't be null for a hash node.");
    
    int index = getIndex(key); // get index of a given key 
    HashNode<K, V> head = hashArray.get(index); // get the first hash node of the index-th bucket

    // traverse the chain of the index-th bucket
    // check whether a node with key is already existed, return its value if it exists
    while (head != null) { 
      if (head.key.equals(key)) 
        return head.value; 
      head = head.next; 
    } 
    
    // if a hash node with key is not found 
    throw new NoSuchElementException("An hash node with key "+key+" is not existed."); 
	}
	
	/**
	 * remove() searches a node with node.key = key, and removes it
	 * 
	 * @throws IllegalArgumentException when trying to get a node with key = null 
   * @throws NoSuchElementException when a node with key doesn't exist
	 */
	@Override
	public void remove(K key) throws IllegalArgumentException, NoSuchElementException {
	  if (key == null)
      throw new IllegalArgumentException("key can't be null for a hash node.");
	  
    int index = getIndex(key); // get index of a given key 
    HashNode<K, V> head = hashArray.get(index); // get the first hash node of the index-th bucket

    // traverse the chain of the index-th bucket
    // check whether a node with key is already existed, break while loop if it exists
    HashNode<K, V> prev = null; 
    while (head != null) { 
      if (head.key.equals(key)) {
        hashArraySize--; // reduce size  
        break;
      }
      prev = head; 
      head = head.next;  
    } 

    // if the node with key is existed, head is that node and head != null
    if (head == null) 
      throw new NoSuchElementException("An hash node with key "+key+" is not existed."); 

    // remove the node with key, and set the next of its prev node to its next
    if (prev != null) 
      prev.next = head.next;
    // if prev = null, it means the node with key is the first node in this bucket
    else
      hashArray.set(index, head.next); 
	}
}
