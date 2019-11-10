//////////////////// ALL ASSIGNMENTS INCLUDE THIS SECTION /////////////////////
//
// Title:           ASSIGNMENT 4
// Files:           this, Account.java, ExceptionalBankingTests.java
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
import java.util.zip.DataFormatException;

public class TransactionGroup {
  
  // encodingType includes three constant types: binary, integer, quick_withdraw
  private enum EncodingType { BINARY_AMOUNT, INTEGER_AMOUNT, QUICK_WITHDRAW };
  private EncodingType type; // the type of a transaction group
  private int[] values; // the transactions' values in a transaction group 
  
  /**
   * groupEncoding is an array of transactions' values
   * groupEncoding[0] represents the type of all transactions in a transaction group
   * groupEncoding[1::] represent all transactions in a transaction group 
   * but they have different meaning in different types
   * @param groupEncoding is an int array
   * @throws DataFormatException 
   */
  public TransactionGroup(int[] groupEncoding) throws DataFormatException {
    // let encodingType type = binary, integer, or quick_withdraw based on groupEncoding[0]
    // if groupEncoding[0] is not 0, 1, or 2, throw a DataFormatException
    if (groupEncoding[0] < 0 || groupEncoding[0] > 2)
      throw new DataFormatException("the first element within a transaction group "
          + "must be 0, 1, or 2");
    
    this.type = EncodingType.values()[groupEncoding[0]];
    this.values = new int[groupEncoding.length-1];
    
    switch (this.type) {
    // an example of BINARY: 0 0 0 0 0 1 1 1 1 0 0 0 1 1 1 0 0 1 1
    // the first 0 means BINARY type, throw a DataFormatException when other values are not 0 or 1
    case BINARY_AMOUNT:
      for(int i=0;i<values.length;i++) {
        if (groupEncoding[i+1] != 0 && groupEncoding[i+1] != 1)
          throw new DataFormatException("binary amount transaction groups "
              + "may only contain 0s and 1s.");
        this.values[i] = groupEncoding[i+1];
      }
      break;
    // an example of INTEGER: 1 1 2 -4 3 -5 3 100
    // the first 1 means INTEGER type, throw a DataFormatException when values contain 0
    case INTEGER_AMOUNT:
      for(int i=0;i<values.length;i++) {
        if (groupEncoding[i+1] == 0)
          throw new DataFormatException("integer amount transaction groups may not contain 0s.");
        this.values[i] = groupEncoding[i+1];
      }
      break;
    // an example of QUICK WITHDRAW: 2 1 1 3 4
    // the first 2 means QUICK WITHDRAW type, throw a DataFormatException when a value is negative
    // or the length of groupEncoding exceeds 5
    case QUICK_WITHDRAW:
      if (values.length != 4)
        throw new DataFormatException("quick withdraw transaction groups "
            + "must contain 5 elements.");
      for(int i=0;i<values.length;i++) {
        if (groupEncoding[i+1] < 0 )
          throw new DataFormatException("quick withdraw transaction groups "
              + "may not contain negative numbers.");
        this.values[i] = groupEncoding[i+1];
      }
      break;
    }    
  }
  
  /**
   * Compute the amount of transactions in a transaction group
   * @return int the amount of transactions in a transaction group
   */
  public int getTransactionCount() {
    int transactionCount = 0;
    switch(this.type) {
      // for binary type, add 1 to transactionCount 
      // if value is not -1(at the beginning), and not equals to the lastAmount
      case BINARY_AMOUNT: 
        int lastAmount = -1;
        for(int i=0;i<this.values.length;i++) {
          if(this.values[i] != lastAmount) {
            transactionCount++; 
            lastAmount = this.values[i];            
          }
        }
        break;
      // for integer type, set transactionCount = values.length
      case INTEGER_AMOUNT: 
        transactionCount = values.length;
        break;
      // for quick withdraw type, each value in values represents the amount of transaction
      // so transactionCount is a accumulated number of all values[i]
      case QUICK_WITHDRAW:
        for(int i=0;i<this.values.length;i++)
          transactionCount += this.values[i];        
    }
    return transactionCount;
  }
  
  /**
   * Based on the type of this transaction group, using different ways to compute the
   * transaction amount of each transaction in this group, and return the amount of
   * the #th transaction, # = transactionIndex
   * @param transactionIndex represents the #th transaction in a transaction group
   * @return -1 if nothing can be returned, 
   *         or return the transaction amount of the #th transaction in this group, 
   */
  public int getTransactionAmount(int transactionIndex) throws IndexOutOfBoundsException {
    if (getTransactionCount() - 1 < transactionIndex)
      throw new IndexOutOfBoundsException("this transactions amount "+getTransactionCount()+
          " is lower than index "+transactionIndex);
    
    int transactionCount = 0;
    switch(this.type) {
      // for binary type, a possible transaction group could be:
      // [1, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0] has four transactions
      // which means deposition $3, withdraw $4, deposition $2, withdraw $2
      case BINARY_AMOUNT:
        int lastAmount = -1; 
        int amountCount = 0; 
        for (int i=0;i<=this.values.length;i++) {
          if (i == this.values.length || this.values[i] != lastAmount) { 
            if(transactionCount-1 == transactionIndex) {
              if(lastAmount == 0)
                return -1 * amountCount;
              else
                return +1 * amountCount;
            }
            transactionCount++;       
            lastAmount = this.values[i]; //seems can be took off
            amountCount = 1;
          } else 
            amountCount++;
          lastAmount = this.values[i];
        } 
        break;
      // for integer type, return values[transactionIndex]
      // this reveals that transactionIndex could be 0, and each values[i] is a transactionAmount
      // but based on the information in Account.getTransactionAmount
      // transactionIndex will never be 0
      case INTEGER_AMOUNT:
        return this.values[transactionIndex];
      // for quick withdraw type, the values.length is 4
      // if values[0] = 3, it means -20 has been done for 3 times
      case QUICK_WITHDRAW:
        final int[] QW_AMOUNTS = new int[] {-20, -40, -80, -100};
        for(int i=0;i<this.values.length;i++) 
          for(int j=0;j<this.values[i];j++)
            if(transactionCount == transactionIndex) 
              return QW_AMOUNTS[i]; 
            else 
              transactionCount++;
    }
    return -1;
  }  
}