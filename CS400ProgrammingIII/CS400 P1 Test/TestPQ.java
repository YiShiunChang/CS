/**
 * Filename:   TestPQ.java
 * Project:    p1TestPQ
 * Authors:    Debra Deppeler, Yi-Shiun Chang
 *
 * Semester:   Fall 2018
 * Course:     CS400
 * Lecture:    004 / Class Number: 46373
 * 
 * Note: Warnings are suppressed on methods that construct new instances of 
 * generic PriorityQueue types.  The exceptions that occur are caught and 
 * such types are not able to be tested with this program.
 * 
 * Due Date:   Before 10pm on September 17, 2018
 * Version:    2.0
 * 
 * Credits:    None.
 *             NOTE: this is an individual assignment, you are not allowed
 *             to view or use code written by anyone but yourself.
 * 
 * Bugs:       no known bugs, but not complete either
 */


import java.util.NoSuchElementException;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Runs black-box unit tests on the priority queue implementations
 * passed in as command-line arguments (CLA).
 * 
 * If a class with the specified class name does not exist 
 * or does not implement the PriorityQueueADT interface,
 * the class name is reported as unable to test.
 * 
 * If the class exists, but does not have a default constructor,
 * it will also be reported as unable to test.
 * 
 * If the class exists and implements PriorityQueueADT, 
 * and has a default constructor, the tests will be run.  
 * 
 * Successful tests will be reported as passed.
 * 
 * Unsuccessful tests will include:
 *     input, expected output, and actual output
 *     
 * Example Output:
 * Testing priority queue class: PQ01
 *    5 PASSED
 *    0 FAILED
 *    5 TOTAL TESTS RUN
 * Testing priority queue class: PQ02
 *    FAILED test00isEmpty: unexpectedly threw java.lang.NullPointerException
 *    FAILED test04insertRemoveMany: unexpectedly threw java.lang.ArrayIndexOutOfBoundsException
 *    3 PASSED
 *    2 FAILED
 *    5 TOTAL TESTS RUN
 * 
 *   ... more test results here
 * 
 * @author deppeler
 */
public class TestPQ {

  // set to true to see call stack trace for exceptions
  private static final boolean DEBUG = true;

  /**
   * Run tests to determine if each Priority Queue implementation
   * works as specified. User names the Priority Queue types to test.
   * If there are no command-line arguments, nothing will be tested.
   * 
   * @param args names of PriorityQueueADT implementation class types 
   * to be tested.
   */
  public static void main(String[] args) {
    for (int i=0; i < args.length; i++) 
      test(args[i]);

    if ( args.length < 1 ) 
      print("no PQs to test");
  }

  /** 
   * Run all tests on each priority queue type that is passed as a classname.
   * 
   * If constructing the priority queue in the first test causes exceptions, 
   * then no other tests are run.
   * 
   * @param className the name of the class that contains the 
   * priority queue implementation.
   */
  private static void test(String className) {
    print("Testing priority queue class: "+className);
    int passCount = 0;
    int failCount = 0;
    try {

      if (test00isEmpty(className)) passCount++; else failCount++;    
      if (test01getMaxEXCEPTION(className)) passCount++; else failCount++;

      if (test02removeMaxEXCEPTION(className)) passCount++; else failCount++;
      if (test03insertRemoveOne(className)) passCount++; else failCount++;
      if (test04insertRemoveMany(className)) passCount++; else failCount++;
      if (test05duplicatesAllowed(className)) passCount++; else failCount++;
      if (test06manyDataItems(className)) passCount++; else failCount++;

      // TODO: add calls to your additional test methods here
      if (test07negativeNumber(className)) passCount++; else failCount++;
      if (test08removeEmpty(className)) passCount++; else failCount++;
      if (test09getRemoveMax(className)) passCount++; else failCount++;
      if (test10combination(className)) passCount++; else failCount++;



      String passMsg = String.format("%4d PASSED", passCount);
      String failMsg = String.format("%4d FAILED", failCount);
      print(passMsg);
      print(failMsg);

    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      if (DEBUG) e.printStackTrace();
      print(className + " FAIL: Unable to construct instance of " + className);
    } finally {
      String msg = String.format("%4d TOTAL TESTS RUN", passCount+failCount);
      print(msg);
    }

  }

  /////////////////////////
  // TODO: ADD YOUR TEST METHODS HERE
  // Must test each operation of the PriorityQueueADT
  // Find and report cases that cause problems.
  // Do not try to fix or debug implementations.
  /////////////////////////


  /** 
   * Confirm that insert(), getMax(), and removeMax() work well together, 
   *
   * @param className name of the priority queue implementation to test.
   * @return true if the insert(), getMax(), and removeMax() return correct value when
   *         they work in different orders
   * @throws ClassNotFoundException 
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   */
  private static boolean test10combination(String className) 
      throws InstantiationException, IllegalAccessException, ClassNotFoundException {
      PriorityQueueADT<Integer> pq = newIntegerPQ(className);
      Integer one = 100;
      Integer two = 200;
      Integer thr = 300;
      Integer fou = 400;
      
      pq.insert(one);
      pq.insert(two);
      Integer check1 = pq.removeMax();
      Integer check2 =pq.getMax();
      pq.insert(thr);
      pq.insert(fou);
      Integer check3 =pq.getMax();
      Integer check4 =pq.removeMax();
      
      if (check1.equals(200) && check2.equals(100) && check3.equals(400) && check4.equals(400)) {
        return true;
      } 
      else {
        print("FAILED testg10combinatoin");
        return false;
      }
      
  }
  
  /**
   * Confirm that getMax and removeMax actually get the same value, 
   * and insert() and removeMax() work well
   *
   * @param className name of the priority queue implementation to test.
   * @return true if the max value is the same by using getMax and removeMax
   * @throws ClassNotFoundException 
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   */
  private static boolean test09getRemoveMax(String className) 
      throws InstantiationException, IllegalAccessException, ClassNotFoundException {
      PriorityQueueADT<Integer> pq = newIntegerPQ(className);
      Integer one = new Integer(-1);
      Integer get;
      Integer remove;
      
      try {
        pq.insert(one);
      } catch (Exception e) {
        print("FAILED test09getRemoveMax: insert fail"+e);
        return false;
      }
      
      try {
        get = pq.getMax();
      } catch (NoSuchElementException e) {
        return true;      
      } catch (Exception e) {
        if (DEBUG) e.printStackTrace();
        print("FAILED test09getRemoveMaxEXCEPTION: get unexpectedly threw " + e.getClass().getName());
        return false;
      }
      
      try {
        remove = pq.removeMax();
      } catch (NoSuchElementException e) {
        print("FAILED test09getRemoveMax: NoSuchElementException " + e.getClass().getName());
        return false;      
      } catch (Exception e) {
        if (DEBUG) e.printStackTrace();
        print("FAILED test09getRemoveMax: remove unexpectedly threw " + e.getClass().getName());
        return false;
      }
      
      if (get.equals(remove)) {
        return true;
      } else {
        print("FAILED test09getRemoveMax: getMax and removeMax do not have the same value");
        return false;
      }
  }
  
  /** 
   * Confirm that removeMax actually remove an element from priority queue, 
   * and insert() and removeMax() work well
   *
   * @param className name of the priority queue implementation to test.
   * @return true if the max value is the max inserted value
   * @throws ClassNotFoundException 
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   */
  private static boolean test08removeEmpty(String className) 
      throws InstantiationException, IllegalAccessException, ClassNotFoundException {
      PriorityQueueADT<Integer> pq = newIntegerPQ(className);
      Integer one = new Integer(-1);
      
      try {
        pq.insert(one);
      } catch (Exception e) {
        print("FAILED test08removeEmpty: insert fail"+e);
        return false;
      }
      
      try {
        pq.removeMax();
      } catch (NoSuchElementException e) {
        print("FAILED test08removeEmpty: NoSuchElementException " + e.getClass().getName());
        return false;      
      } catch (Exception e) {
        if (DEBUG) e.printStackTrace();
        print("FAILED test08removeEmpty: unexpectedly threw " + e.getClass().getName());
        return false;
      }
      
      try {
        if (pq.isEmpty()) {
          return true;
        } else {
          print("FAILED test08removeEmpty: removeMax doesn't work");
          return false;
        }
      } catch (Exception e) {
        print("FAILED test08removeEmpty: unexpectedly threw when handling isEmpty, and "
            + "removeMax doesn't clean pq completely.");
        return false;
      }
  }
  
  /** 
   * Confirm that negative values can be inserted into priority queue, 
   * and insert() and removeMax() work well
   *
   * @param className name of the priority queue implementation to test.
   * @return true if the max value is the max inserted value
   * @throws ClassNotFoundException 
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   */
  private static boolean test07negativeNumber(String className) 
      throws InstantiationException, IllegalAccessException, ClassNotFoundException {
      PriorityQueueADT<Integer> pq = newIntegerPQ(className);
      Integer one = new Integer(-1);
      Integer two = new Integer(-100);
      Integer three = new Integer(100);
      Integer max1;
      Integer max2;
      Integer max3;
      
      try {
        pq.insert(one);
        pq.insert(two);
        pq.insert(three);
      } catch (Exception e) {
        print("FAILED test07negativeNumber: insert fail"+e);
        return false;
      }
      
      try {
        max1 = pq.removeMax();
        max2 = pq.removeMax();
        max3 = pq.removeMax();
      } catch (NoSuchElementException e) {
        print("FAILED test07negativeNumber: NoSuchElementException " + e.getClass().getName());
        return false;      
      } catch (Exception e) {
        if (DEBUG) e.printStackTrace();
        print("FAILED test07negativeNumber: unexpectedly threw " + e.getClass().getName());
        return false;
      }
      
      if (max1.equals(100) && max2.equals(-1)) {
        return true;
      } 
      else {
        print("FAILED test07negativeNumber: removeMax1 and removeMax2 are not 100 and -1");
        return false;
      }
    }
  
  /** 
   * Confirm that many items can be inserted into priority queue, 
   * the amount of many items is 1 million,
   * and insert() and removeMax() work well
   *
   * @param className name of the priority queue implementation to test.
   * @return true if 1 million data items can be inserted into priority queue
   * @throws ClassNotFoundException 
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   */
  private static boolean test06manyDataItems(String className) 
    throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    PriorityQueueADT<Double> pq = newDoublePQ(className);
    Double max;
    
    try {
      for(Double i=1.0; i<=1000000.0; i++) {
        pq.insert(i);
      }
    } catch (Exception e) {
      print("FAILED test06manyDataItems: Double insert fail"+e);
      return false;
    }
    
    try {
      max = pq.removeMax();
    } catch (NoSuchElementException e) {
      print("FAILED test06manyDataItems: Double NoSuchElementException " + e.getClass().getName());
      return false;      
    } catch (Exception e) {
      if (DEBUG) e.printStackTrace();
      print("FAILED test06manyDataItems: Double unexpectedly threw " + e.getClass().getName());
      return false;
    }
 
    
    if (max.equals(1000000.0)) {
      return true;
    } 
    else {
      print("FAILED test06manyDataItems: max is not 1000000.0");
      return false;
    }
    
  } 
  
  /** 
   * Confirm that duplicate values can be inserted into priority queue, 
   * and insert() and removeMax() work well
   *
   * @param className name of the priority queue implementation to test.
   * @return true if duplicate values can be inserted (and then removed)
   * @throws ClassNotFoundException 
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   */
  private static boolean test05duplicatesAllowed(String className) 
    throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    PriorityQueueADT<Integer> pq = newIntegerPQ(className);
    Integer[] inputList = {0, 1, 100}; 
    Integer max1;
    Integer max2;
    
    try {
      for (Integer element: inputList) {
        for (int i=0; i<inputList.length; i++) {
          pq.insert(element);
        }
      }
    } catch (Exception e) {
      print("FAILED test05duplicatesAllowed: insert fail"+e);
      return false;
    }
    
    try {
      max1 = pq.removeMax();
      max2 = pq.removeMax();
    } catch (NoSuchElementException e) {
      print("FAILED test05duplicatesAllowed: NoSuchElementException " + e.getClass().getName());
      return false;      
    } catch (Exception e) {
      if (DEBUG) e.printStackTrace();
      print("FAILED test05duplicatesAllowed: unexpectedly threw " + e.getClass().getName());
      return false;
    }
    
    if (max1.equals(100) && max2.equals(100)) {
      return true;
    } 
    else {
      print("FAILED test05duplicatesAllowed: removeMax1 and removeMax2 is not 100");
      return false;
    }
  }

  /** 
   * Confirm that priority queue returns the max value in the priority order 
   * when insert many elements, and insert() and removeMax() work well
   *
   * @param className name of the priority queue implementation to test.
   * @return true if removeMax returns the max values in the priority order, which is 200
   * @throws ClassNotFoundException 
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   */
  private static boolean test04insertRemoveMany(String className) 
    throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    PriorityQueueADT<Integer> pq = newIntegerPQ(className);
    ArrayList<Integer> removeManyArray = new ArrayList<>();
    ArrayList<Integer> checkManyArray = new ArrayList<>();
    Integer max;
    
    for (int i=0; i<100; i++) {
      removeManyArray.add(i);
    }

    try {
      for(int element: removeManyArray) {
        pq.insert(element);
      }
    } catch (Exception e) {
      print("FAILED test04insertRemoveMany: insert fail"+e);
      return false;
    }
    
    try {
      for (int i=0; i<100; i++) {
        checkManyArray.add(pq.removeMax());
      }
    } catch (NoSuchElementException e) {
      print("FAILED test04insertRemoveMany: NoSuchElementException " + e.getClass().getName());
      return false;      
    } catch (Exception e) {
      if (DEBUG) e.printStackTrace();
      print("FAILED test04insertRemoveMany: unexpectedly threw " + e.getClass().getName());
      return false;
    }
    
    for (int i=99; i>-1; i--) {
      max = Collections.max(removeManyArray);
      removeManyArray.remove(max);
      if (!max.equals(i)) {
        print("FAILED test04insertRemoveOne: removeMax is not in descending order");
        return false;
      }
    }
    return true;
  }

  /** 
   * Confirm that priority queue returns the max value when insert one element, 
   * and insert() and removeMax() work well
   * 
   * @param className name of the priority queue implementation to test.
   * @return true if removeMax is the same value as what was inserted, which is 100
   * @throws ClassNotFoundException 
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   */
  private static boolean test03insertRemoveOne(String className) 
    throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    PriorityQueueADT<Integer> pq = newIntegerPQ(className);
    Random r = new Random();
    Integer one = r.nextInt(100);
    Integer max;
    
    try {
      pq.insert(one);
    } catch (Exception e) {
      print("FAILED test03insertRemoveOne: insert fail"+e);
      return false;
    }
    
    try {
      max = pq.removeMax(); 
    } catch (NoSuchElementException e) {
      print("FAILED test03insertRemoveOne: NoSuchElementException " + e.getClass().getName());
      return false;      
    } catch (Exception e) {
      if (DEBUG) e.printStackTrace();
      print("FAILED test03insertRemoveOne: unexpectedly threw " + e.getClass().getName());
      return false;
    }
    
    if (max.equals(one)) {
      return true;
    }
    else {
    print("FAILED test03insertRemoveOne: removeMax is not "+one);
    return false;
    }

  }

  /** 
   * Confirm that removeMax throws NoSuchElementException if called on 
   * an empty priority queue. Any other exception indicates a fail.
   * 
   * @param className name of the priority queue implementation to test.
   * @return true if removeMax on empty priority queue throws NoSuchElementException
   * @throws ClassNotFoundException 
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   */
  private static boolean test02removeMaxEXCEPTION(String className) 
    throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    PriorityQueueADT<Integer> pq = newIntegerPQ(className);
    try {
      pq.removeMax();
    } catch (NoSuchElementException e) {
      return true;
    } catch (Exception e) {
      if (DEBUG) e.printStackTrace();
      print("FAILED test02getMaxEXCEPTION: unexpectedly threw " + e.getClass().getName());
      return false;
    }
    print("FAILED test02removeMaxEXCEPTION: removeMax did not throw NoSuchElement exception on newly constructed PQ");
    return false;
  }

  /** DO NOT EDIT -- provided as an example
   * Confirm that getMax throws NoSuchElementException if called on 
   * an empty priority queue. Any other exception indicates a fail.
   * 
   * @param className name of the priority queue implementation to test.
   * @return true if getMax on empty priority queue throws NoSuchElementException
   * @throws ClassNotFoundException 
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   */
  private static boolean test01getMaxEXCEPTION(String className) 
    throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    PriorityQueueADT<Integer> pq = newIntegerPQ(className);
    try {
      pq.getMax();
    } catch (NoSuchElementException e) {
      return true;      
    } catch (Exception e) {
      if (DEBUG) e.printStackTrace();
      print("FAILED test01getMaxEXCEPTION: unexpectedly threw " + e.getClass().getName());
      return false;
    }
    print("FAILED test01getMaxEXCEPTION: getMax did not throw NoSuchElement exception on newly constructed PQ");
    return false;
  }

  /** DO NOT EDIT THIS METHOD
   * @return true if able to construct Integer priority queue and 
   * the instance isEmpty.
   * 
   * @throws ClassNotFoundException 
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   */
  private static boolean test00isEmpty(String className) 
    throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    PriorityQueueADT<Integer> pq = newIntegerPQ(className);
    try {
    if (pq.isEmpty()) 
      return true;
    } catch (Exception e) {
      if (DEBUG) e.printStackTrace();
      print("FAILED test00isEmpty: unexpectedly threw " + e.getClass().getName());
      return false;
    }
    print("FAILED test00isEmpty: isEmpty returned false on newly constructed PQ");
    return false;
  }

  /** DO NOT EDIT THIS METHOD
   * Constructs a max Priority Queue of Integer using the class that is name.
   * @param className The specific Priority Queue to construct.
   * @return a PriorityQueue
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   * @throws ClassNotFoundException 
   */
  @SuppressWarnings({ "unchecked" })
  public static final PriorityQueueADT<Integer> newIntegerPQ(String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    Class<?> pqClass = Class.forName(className);
    Object obj = pqClass.newInstance(); 
    if (obj instanceof PriorityQueueADT) {
      return (PriorityQueueADT<Integer>) obj;
    }
    return null;
  }

  /** DO NOT EDIT THIS METHOD
   * Constructs a max Priority Queue of Double using the class that is named.
   * @param className The specific Priority Queue to construct.
   * @return a PriorityQueue
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   * @throws ClassNotFoundException 
   */
  @SuppressWarnings({ "unchecked" })
  public static final PriorityQueueADT<Double> newDoublePQ(final String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    Class<?> pqClass = Class.forName(className);
    Object obj = pqClass.newInstance(); 
    if (obj instanceof PriorityQueueADT) {
      return (PriorityQueueADT<Double>) obj;
    }
    return null;
  }

  /** DO NOT EDIT THIS METHOD
   * Constructs a max Priority Queue of String using the class that is named.
   * @param className The specific Priority Queue to construct.
   * @return a PriorityQueue
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   * @throws ClassNotFoundException 
   */
  @SuppressWarnings({ "unchecked" })
  public static final PriorityQueueADT<String> newStringPQ(final String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    Class<?> pqClass = Class.forName(className);
    Object obj = pqClass.newInstance(); 
    if (obj instanceof PriorityQueueADT) {
      return (PriorityQueueADT<String>) obj;
    }
    return null;
  }


  /** DO NOT EDIT THIS METHOD
   * Write the message to the standard output stream.
   * Always adds a new line to ensure each message is on its own line.
   * @param message Text string to be output to screen or other.
   */
  private static void print(String message) {
    System.out.println(message);
  }

}
