/**
 * Filename:   Profile.java
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
import java.util.Random;
import java.util.TreeMap;

public class Profile<K extends Comparable<K>, V> {
  
	HashTableADT<K, V> hashtable;
	TreeMap<K, V> treemap;
	 
	public Profile() {
	  hashtable = new HashTable<K, V>();
	  treemap = new TreeMap<K, V>();
	}
	
	public Profile(int capacity, double loadFactor) {
    hashtable = new HashTable<K, V>(capacity, loadFactor);
    treemap = new TreeMap<K, V>();
  }
	
	public void insert(K key, V value) {
		hashtable.put(key, value);
		treemap.put(key, value);
	}
	
	public void retrieve(K key) {
		System.out.println(hashtable.get(key));
		System.out.println(treemap.get(key));
	}
	
	public static void main(String[] args) {
	  Random ran = new Random();
	  
		if (args.length < 1) {
			System.out.println("Expected 1 argument: <num_elements>");
			System.exit(1);
		}
		
		// get the first item from argument and parse it into integer
		Integer numElements = Integer.parseInt(args[0]);
		
		// create a profile object
    Profile<Integer, Integer> profile1 = new Profile<Integer, Integer>();

    // execute the insert method of profile as many times as numElements
    for (int i = 0; i < numElements; i++) {
      profile1.insert(i, i);    
    }
    
    // execute the retrieve method of profile as many times as numElements
    for (int i = 0; i < numElements; i++) {
      profile1.retrieve(i);    
    }
    
    // create a profile object
    Profile<Integer, Integer> profile2 = new Profile<Integer, Integer>(11, 0.75);
    
    // execute the insert method of profile as many times as numElements
    for (int i = 0; i < numElements; i++) {
      profile1.insert(i, i+2);    
    }
    
    // execute the retrieve method of profile as many times as numElements
    for (int i = 0; i < numElements; i++) {
      profile1.retrieve(i);    
    }

		String msg = String.format("Successfully inserted and retreived %d elements into the hash"
		    + " table and treemap for 2 differecnt cases", numElements*2);
		System.out.println(msg);
	}
}
