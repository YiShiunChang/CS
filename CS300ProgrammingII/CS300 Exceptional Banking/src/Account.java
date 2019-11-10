//////////////////// ALL ASSIGNMENTS INCLUDE THIS SECTION /////////////////////
//
// Title:           ASSIGNMENT 4
// Files:           this, TransactionGroup.java, ExceptionalBankingTests.java
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
import java.util.Scanner;
import java.util.zip.DataFormatException;

public class Account {
  private static final int MAX_GROUPS = 10000; // maximum 10000 transaction groups
  private static int nextUniqueId = 1000; 
  
  // each account has its name, ID, transactionGroups array, and transactionGroupsCount
  // transactionGroups is an array of up to 10000 TransactionGroup
  private String name;
  private final int UNIQUE_ID; 
  private TransactionGroup[] transactionGroups; // consists of many transaction groups, 
    // and each transaction group consists of many transactions
  private int transactionGroupsCount; // how many transaction groups are in the transactionGroups
  
  /**
   * Initialize an account with given name, nextUniqueId, transactionGroups, and count
   * @param name
   */
  public Account(String name) {
   this.name = name;
   this.UNIQUE_ID = Account.nextUniqueId;
   Account.nextUniqueId++;
   this.transactionGroups = new TransactionGroup[MAX_GROUPS];
   this.transactionGroupsCount = 0;
  }
  
  /**
   * Read a file, and get account's name and UNIQUE_ID in first two lines
   * then update the transactionGroup by addTransactionGroup() if there are more lines
   * @param file
   * @throws DataFormatException 
   */
  public Account(File file) throws DataFormatException, FileNotFoundException {
    Scanner in = null;
    
    try {
     in = new Scanner(file);
    } 
    catch (FileNotFoundException e) {
      throw new FileNotFoundException(file.getPath());
    }
    
    this.name = in.nextLine();
    this.UNIQUE_ID = Integer.parseInt(in.nextLine());
    Account.nextUniqueId = this.UNIQUE_ID + 1;
    this.transactionGroups = new TransactionGroup[MAX_GROUPS];
    this.transactionGroupsCount = 0;    
    String nextLine=""; // has no function
    
    while (in.hasNextLine()) 
      this.addTransactionGroup(in.nextLine());
    
    in.close();
  }

  /**
   * Get an account's unique ID
   * @return int UNIQUE_ID
   */
  public int getId() { 
    return this.UNIQUE_ID;
  }
  
  /**
   * Build a new transaction group based on the command string 
   * and add it into the array transactionGroups  
   * @param command
   * @throws DataFormatException 
   * @throws OutOfMemoryError
   */
  public void addTransactionGroup(String command) 
      throws DataFormatException, OutOfMemoryError {
    
    // a transcationGroups can only contains 10000 transaction groups
    if (this.transactionGroupsCount == 10000) 
      throw new OutOfMemoryError("one acount can only contains 10000 transaction groups");
    
    // a transaction can not be null
    if (command == null)
      throw new DataFormatException("transaction group encoding cannot be null or empty");
    
    // if command doesn't contain space, it is definitely not separated by space
    // separate the command into a String array by space, and build an int array on String array
    if (command.indexOf(' ') == -1)
      throw new DataFormatException("addTransactionGroup requires string commands "
        + "that contain only space separated integer values");
    String[] parts = command.split(" ");
    
    // build an int array newTransactions, which is a transaction group, from parts
    int[] newTransactions = new int[parts.length];
    try {
      for (int i = 0; i < parts.length; i++) {
        newTransactions[i] = Integer.parseInt(parts[i]);
      }
    } catch (NumberFormatException e) {
        throw new DataFormatException("addTransactionGroup requires string commands "
            + "that contain only space separated integer values");
    }
    
    // build a transaction group based on int array newTransactions
    // put the new built transaction group into this account's transactionGroups by count
    TransactionGroup t = new TransactionGroup(newTransactions);
    this.transactionGroups[this.transactionGroupsCount] = t;
    this.transactionGroupsCount++;
  }
  
  /**
   * For all transaction groups in this account's transactionGroups, get their transactionCount
   * and sum them all. Therefore, get the total transactionCount of the entire account, or say
   * the entire transactionGroups 
   * @return int the total transactionCount of the entire account
   */
  public int getTransactionCount() {
    int transactionCount = 0;
    for(int i=0;i<this.transactionGroupsCount;i++)
      transactionCount += this.transactionGroups[i].getTransactionCount();
    return transactionCount;
  }

  /**
   * Get the amount of a specific transaction based on the index,
   * if index is larger than the amount of transactions made in this account,
   * throw exception
   * @param index the #th transaction a user did in an account
   * @return int the amount of a specific transaction 
   * @throws IndexOutOfBoundsException
   */
  public int getTransactionAmount(int index) throws IndexOutOfBoundsException {    
    int transactionCount = 0;
    // 1. start from the first transactionGroup in the transactionGroups
    // 2. prevTransactionCount records the accumulated amount of transactions,
    //    which were made in all previous transaction groups
    // 3. transactionCount updates the accumulated amount of transactions,
    //    which add an additional transaction group's count to the prevTransactionCount
    // 4. if the accumulated amount of transactionCount is higher than index,
    //    let index minus prevTransactionCount, so we get the #th transaction in 
    //    the current transaction group
    // 5. get into the current transaction group by this.transactionGroups[i],
    //    and get the amount of a specific transaction
    // otherwise, retrun -1
    for(int i=0;i<this.transactionGroupsCount;i++) {
      int prevTransactionCount = transactionCount;
      transactionCount += this.transactionGroups[i].getTransactionCount();
      
      if(transactionCount > index) {
        index -= prevTransactionCount;
        return this.transactionGroups[i].getTransactionAmount(index);
      }
    }
    
    if (transactionCount - 1 < index)
      throw new IndexOutOfBoundsException("this transactions amount "+transactionCount+
          " is lower than index "+index);
    
    return -1;
  }
  
  /**
   * Set size = the number of all transactions in this account by getTransactionCount()
   * by going through 0 to size, we get each transaction's amount and sum them all
   * @return int balance of this account
   */
  public int getCurrentBalance() {
    int balance = 0;
    int size = this.getTransactionCount();
    for(int i=0;i<size; i++)
      balance += this.getTransactionAmount(i);
    return balance;
  }
  
  /**
   * By going through all the transactions in an account,
   * once balance and a transaction's amount are both smaller than 0
   * overdraftCount increases 1
   * @return int overdraftCount
   */
  public int getNumberOfOverdrafts() {
    int balance = 0;
    int overdraftCount = 0;
    int size = this.getTransactionCount();
    for(int i=0;i<size; i++) {
      int amount = this.getTransactionAmount(i); 
      balance += amount;
      if(balance < 0 && amount < 0)
        overdraftCount++;
    }
    return overdraftCount;
  }
    
}