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
import java.util.NoSuchElementException; // expect to need
import static org.junit.Assert.*; 
import org.junit.Before;  // setUp method
import org.junit.After;   // tearDown method
import org.junit.Test;   

/**
 * 
 * 
 * @author Yi-Shiun, Chang
 */
public class TestHashTable{

	// TODO: add other fields that will be used by multiple tests
	
	// Allows us to create a new hash table before each test
	static HashTableADT<Integer, Object> ht;
	
	// TODO: add code that runs before each test method
	@Before
	public void setUp() throws Exception {
		ht = new HashTable<Integer, Object>();  
	}

	// TODO: add code that runs after each test method
	@After
	public void tearDown() throws Exception {
		ht = null;
	}
		
	/** 
	 * Tests that a HashTable is empty upon initialization
	 */
	@Test
	public void test000_IsEmpty() {
		assertEquals("size with 0 entries:", 0, ht.size());
	}
	
	/** 
	 * Tests that a HashTable is not empty after adding one (K, V) pair
	 */
	@Test
	public void test001_IsNotEmpty() {
		ht.put(1,"0001");
		int expected = 1;
		int actual = ht.size();
		assertEquals("size with one entry:", expected, actual);
	}

	/** 
	 * Other tests assume <int, Object> pairs,
	 * this test checks that <Long, Object> pair also works.
	 */
	@Test
	public void test002_Long_Object() {
		Long key = 9876543210L;
		Object expected = "" + key;		
		HashTableADT<Long,Object> table = new HashTable<Long,Object>();
		table.put(key, expected);
		Object actual = table.get(key);
		assertTrue("put-get of (Long, Object) pair", expected.equals(actual));
	}
	
	/**
	 * Test that the value for a key is updated after insert again.
	 */
	@Test
	public void test003_Update() {
		String key = "here";
		Object actual1 = 123;
		Object actual2 = 456;
		HashTableADT<String, Object> table = new HashTable<String, Object>();
		table.put(key, actual1); // insert hash node
		table.put(key, actual2); // update hash node
		Object expected = table.get(key); // get hash node
		assertTrue("put-put-get of (String, Object) pairs", expected.equals(actual2));
	}
	
	/**
	 * Tests that inserting many and removing one entry from the hash table works
	 */
	@Test(timeout = 1000 * 10)
	public void test004_InsertManyRemoveOne() {
	  // insert 1000 hash nodes
	  for (int i = 0; i < 1000; i++)
      ht.put(i, i);    
	  
	  int actual = 100;
	  Object expected = ht.get(100);
	  assertTrue("putmany-get of (Integer, Object) pairs", expected.equals(actual));
	}
	
	/**
	 * Tests ability to insert many entries and and remove many entries from the hash table
	 */
	@Test(timeout=1000 * 10)
	public void test005_InsertRemoveMany() {
	  // insert 1000 hash nodes
	  for (int i = 0; i < 1000; i++) 
      ht.put(i, i);  
	  
	  // remove 200 hash nodes
	  for (int i = 500; i < 700; i++)
	    ht.remove(i);
    
	  // check size
	  assertTrue("putmany-removemany of (Integer, Object) pairs", 800 == ht.size());
	  
	  // check nodes are removed correctly
	  for (int i = 0; i < 500; i++)
	    assertTrue("putmany-removemany of (Integer, Object) pairs", ht.get(i).equals(i));
	  for (int i = 700; i < 1000; i++)
	    assertTrue("putmany-removemany of (Integer, Object) pairs", ht.get(i).equals(i));
	}
	
	/**
	 * An IllegalArgumentException should be throw when inserting null
	 * @throws IllegalArgumentException
	 */
	@Test (expected = IllegalArgumentException.class)
	public void test006_IllegalArgumentException() throws IllegalArgumentException {
	    ht.put(null, 100);
	}
	
  /**
   * An IllegalArgumentException should be throw when getting null
   * @throws IllegalArgumentException
   */
  @Test (expected = IllegalArgumentException.class)
  public void test007_IllegalArgumentException() throws IllegalArgumentException {
      ht.get(null); 
  }
  
  /**
   * An IllegalArgumentException should be throw when removing null
   * @throws IllegalArgumentException
   */
   @Test (expected = IllegalArgumentException.class)
   public void test008_IllegalArgumentException() throws IllegalArgumentException {
       ht.remove(null);
   }
 
   /**
   * A NoSuchElementException should be throw when getting a node that doesn't exist
   * @throws NoSuchElementException
   */
  @Test (expected = NoSuchElementException.class)
  public void test009_NoSuchElementException() throws NoSuchElementException {
      ht.get(1);
  }
  
  /**
   * A NoSuchElementException should be throw when removing a node that doesn't exist
   * @throws NoSuchElementException
   */
   @Test (expected = NoSuchElementException.class)
   public void test010_NoSuchElementException() throws NoSuchElementException {
       ht.remove(1);
   }
   
   /**
    * Test constructor with parameters
    */
   @Test 
   public void test011_ConstructorWithPara() {
       HashTable<Integer, Integer> ht1 = new HashTable<Integer, Integer> (101, 0.7);
       int actual = 1;
       ht1.put(1, actual);
       int expected = ht1.get(1);
       assertEquals(expected, actual);
       assertEquals(ht1.bucketSize(), 101);
       assertEquals(ht1.size(), 1);
   }
   
   @Test 
   public void test012_buchetsAfterLoadFactorExceedsThreshold() {
     for (int i = 0; i < 9; i++) 
       ht.put(i, i);

     assertEquals(((HashTable) ht).bucketSize(), 23);
   }
   
   @Test (expected = NoSuchElementException.class)
   public void test013_buchetsAfterLoadFactorExceedsThreshold() {
     for (int i = 0; i < 8; i++) 
       ht.put(i, i);     
     

     ht.put(7, 10);
     assertEquals(((HashTable) ht).bucketSize(), 11);
     
     ht.put(8, 9);
     assertEquals(((HashTable) ht).bucketSize(), 23);
     
     ht.remove(6);
     assertEquals(((HashTable) ht).bucketSize(), 23);
     
     ht.get(9);
   }
   
   /**
    * Test constructor with parameters
    */
   @Test 
   public void test014_ConstructorWithParaAndThreshold() {
       HashTable<Integer, Integer> ht1 = new HashTable<Integer, Integer> (7, 2);
       for (int i = 0; i < 13; i++) 
         ht1.put(i, i);  
       
       for (int i = 0; i < 13; i++) 
         assertTrue(ht1.get(i).equals(i));
       
       assertEquals(ht1.size(), 13);
       assertEquals(ht1.bucketSize(), 7);
       
       for (int i = 13; i < 29; i++) 
         ht1.put(i, i); 
       
       assertEquals(ht1.size(), 29);
       assertEquals(ht1.bucketSize(), 15);
       
       for (int i = 0; i < 15; i++) 
         ht1.remove(i);
       
       assertEquals(ht1.size(), 14);
       assertEquals(ht1.bucketSize(), 15);
   }
}
