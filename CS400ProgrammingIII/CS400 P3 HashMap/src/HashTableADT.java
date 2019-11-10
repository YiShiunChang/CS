/**
 * Filename:   HashTableADT.java
 * Project:    p3
 * Course:     cs400
 * Authors:    Debra Deppeler
 *
 * DO NOT EDIT THIS INTERFACE
 */

import java.util.NoSuchElementException;

//DO NOT EDIT THIS INTERFACE
public interface HashTableADT<K extends Comparable<K>, V> {
	
	/** @return  the number of keys in the hash table */
	public int size();
	
	/** insert a <key,value> pair entry into the hash table 
	 * if the key already exists in the table, 
	 * replace existing value for that key with the 
	 * value specified in this call to put.
	 * 
	 * permits null values but not null keys and permits the same value 
	 * to be paired with different key
	 * 
	 * throw IllegalArgumentException when key is null
	 */
	public void put(K key, V value) throws IllegalArgumentException;
	
	/** return the value associated with the given key.
	 * throw IllegalArgumentException if key is null 
	 * throw NoSuchElementException if key does not exist 
	 */
	public V get(K key) throws IllegalArgumentException, NoSuchElementException;
	
	/** remove the (key,value) entry for the specified key
	 * throw IllegalArgumentException if key is null 
	 * throw NoSuchElementException if key does not exist in the tree 
	 */
	public void remove(K key) throws IllegalArgumentException, NoSuchElementException;

}