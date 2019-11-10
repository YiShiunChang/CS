//////////////////// ALL ASSIGNMENTS INCLUDE THIS SECTION /////////////////////
//
// Title:           ASSIGNMENT 4
// Files:           this, Account.java, TransactionGroup.java
// Course:          CS300, Fall 2018
//
// Author:          Yi-Shiun Chang
// Email:           chang242@wisc.edu
// Lecturer's Name: Gary Dahl
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
import java.io.File;
import java.io.FileNotFoundException;
import java.util.zip.DataFormatException;

public class ExceptionalBankingTests {
  
  /**
   * Test eight different units in Account and TransactionGroup
   * if all test are passed,print eight trues 
   * @param args
   */
  public static void main(String[] args) {
    System.out.println(testAccountBalance());
    System.out.println(testOverdraftCount());
    System.out.println(testTransactionGroupEmpty());
    System.out.println(testTransactionGroupInvalidEncoding());
    System.out.println(testAccountAddNegativeQuickWithdraw());
    System.out.println(testAccountBadTransactionGroup());
    System.out.println(testAccountIndexOutOfBounds());
    System.out.println(testTransactionGroupIndexOutOfBounds());
    System.out.println(testAccountMissingFile());
  }
  
  /**
   * Add three types of transaction groups and test whether getCurrentBalance() works
   * @return true if passed the test
   * @throws DataFormatException 
   */
  public static boolean testAccountBalance() { 
    Account newAccount = new Account("account1");
    try {
      // binary type with 6 transactions
      newAccount.addTransactionGroup("0 0 0 0 0 1 1 1 1 0 0 0 1 1 1 0 0 1 1"); 
      // integer type with 7 transactions
      newAccount.addTransactionGroup("1 1 2 -4 3 -5 3 100"); 
      // quick withdraw type with 9 transactions
      newAccount.addTransactionGroup("2 1 1 3 4");
    } catch (OutOfMemoryError e) {
      e.printStackTrace();
    } catch (DataFormatException e) {
      e.printStackTrace();
    } 
    
    if (newAccount.getCurrentBalance() == -600) {
      return true;
    }
    return false;
  }
  
  /**
   * Add three types of transaction groups and test whether getNumberOfOverdrafts() works
   * @return true if passed the test
   * @throws DataFormatException
   */
  public static boolean testOverdraftCount() { 
    Account newAccount = new Account("account1");
    try {
      // 3 overdrafts
      newAccount.addTransactionGroup("0 0 0 0 0 1 1 1 1 0 0 0 1 1 1 0 0 1 1");
      // 2 overdrafts
      newAccount.addTransactionGroup("1 1 2 -4 3 -5 3 100");
      // 7 overdrafts
      newAccount.addTransactionGroup("2 1 1 3 4");
    } catch (OutOfMemoryError e) {
      e.printStackTrace();
    } catch (DataFormatException e) {
      e.printStackTrace();
    }
  
    if (newAccount.getNumberOfOverdrafts() == 12) {
      return true;
    }
    return false;
  }
  
  /**
   * This method checks whether the TransactionGroup constructor throws an exception with an
   * appropriate message, when it is passed an empty int[].
   * @return true when test verifies correct functionality, and false otherwise.
   * @throws DataFormatException 
   */
  public static boolean testTransactionGroupEmpty() { 
    Account newAccount = new Account("account1");
    try {
      newAccount.addTransactionGroup("");
    } catch (DataFormatException e) {
      if (e.toString().equals("java.util.zip.DataFormatException: "
          + "addTransactionGroup requires string commands that "
          + "contain only space separated integer values"))
        return true;
    }
    return false; 
  }
   
  /**
   * This method checks whether the TransactionGroup constructor throws an exception with an
   * appropriate message, when then first int in the provided array is not 0, 1, or 2.
   * @return true when test verifies correct functionality, and false otherwise.
   */
  public static boolean testTransactionGroupInvalidEncoding() { 
    Account newAccount = new Account("account1");
    try {
      newAccount.addTransactionGroup("4 3 3 3 2 1");
    } catch (DataFormatException e) {
      if (e.toString().equals("java.util.zip.DataFormatException: "
          + "the first element within a transaction group must be 0, 1, or 2"))
        return true;
    }
    return false; 
  }
   
  /**
   * This method checks whether the Account.addTransactionGroup method throws an exception with an
   * appropriate message, when it is passed a quick withdraw encoded group that contains negative
   * numbers of withdraws.
   * @return true when test verifies correct functionality, and false otherwise.
   */
  public static boolean testAccountAddNegativeQuickWithdraw() {
    Account newAccount = new Account("account1");
    try {
      newAccount.addTransactionGroup("2 -3 3 3 1");
    } catch (DataFormatException e) {
      if (e.toString().equals("java.util.zip.DataFormatException: "
          + "quick withdraw transaction groups may not contain negative numbers."))
        return true;
    }
    return false; 
  }
   
  /**
   * This method checks whether the Account.addTransactionGroup method throws an exception with an
   * appropriate message, when it is passed a string with space separated English words (non-int).
   * @return true when test verifies correct functionality, and false otherwise.
   */
  public static boolean testAccountBadTransactionGroup() {
    Account newAccount = new Account("account1");
    try {
      newAccount.addTransactionGroup("1 a p p l e");
    } catch (DataFormatException e) {
      if (e.toString().equals("java.util.zip.DataFormatException: "
          + "addTransactionGroup requires string commands that "
          + "contain only space separated integer values"))
        return true;
    }
    return false; 
  }
   
  /**
   * This method checks whether the Account.getTransactionAmount method throws an exception with an
   * appropriate message, when it is passed an index that is out of bounds.
   * @return true when test verifies correct functionality, and false otherwise.
   */
  public static boolean testAccountIndexOutOfBounds() {
    Account newAccount = new Account("account1");
    try {
      newAccount.addTransactionGroup("2 1 1 1 0");
      newAccount.addTransactionGroup("1 1");
      newAccount.addTransactionGroup("0 1");
      newAccount.getTransactionAmount(6);
    } catch (IndexOutOfBoundsException e) {
      // System.out.println(e.getMessage());
      if (e.toString().equals("java.lang.IndexOutOfBoundsException: "
          + "this transactions amount 5 is lower than index 6"))
        return true;
    } catch (DataFormatException e) {
      e.printStackTrace();
    }
    return false; 
  }
  
  /**
   * This method checks whether the TransactionGroup.getTransactionAmount method 
   * throws an exception with an appropriate message, 
   * when it is passed an index that is out of bounds.
   * @return true when test verifies correct functionality, and false otherwise.
   */
  public static boolean testTransactionGroupIndexOutOfBounds() {
    try {
      int[] numbers = {0, 0, 0, 0, 1, 1};
      TransactionGroup newGroup = new TransactionGroup(numbers);
      newGroup.getTransactionAmount(2);
    } catch (IndexOutOfBoundsException e) {
      System.out.println(e.getMessage());
      if (e.toString().equals("java.lang.IndexOutOfBoundsException: "
          + "this transactions amount 5 is lower than index 6"))
        return true;
    } catch (DataFormatException e) {
      e.printStackTrace();
    }
    return false; 
  }
   
  /**
   * This method checks whether the Account constructor that 
   * takes a File parameter throws an exception with an appropriate message, 
   * when it is passed a File object that 
   * does not correspond to an actual file within the file system.
   * @return true when test verifies correct functionality, and false otherwise.
   */
  public static boolean testAccountMissingFile() {
    File file = new File("10_Random");
    try {
      Account newAccount = new Account(file);
    } catch (FileNotFoundException e) {
      if (e.toString().equals("java.io.FileNotFoundException: 10_Random"))
        return true;
    } catch (DataFormatException e) {
      e.printStackTrace();
    }
    return false;
  }
}

