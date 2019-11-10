import java.util.Arrays;

public class AuditableBankingTests {
  
  public static void main(String[] args) {
    testAuditableBanking();
    
    boolean test = false;
    if (test) {
      testProcessCommand();
      testCalculateCurrentBalance();
      testCalculateNumberOfOverdrafts();
    }
  }
  
  public static boolean testProcessCommand() {
    int[][] allTransactions = new int[4][8];
    String command = "0 0 1 1 0 1 1";
    int allTransactionsCount = AuditableBanking.processCommand(command, allTransactions, 2);
    
    if (allTransactions[2][2] != 1) {
      System.out.println("Wrong format of array inputs");
      return false;
    } else if (allTransactionsCount != 3) {
      System.out.println("Wrong count for allTransactions count");
      return false;
    }
    return true;
  }
  
  public static boolean testCalculateCurrentBalance() {
    boolean foundProblem = false;
    int[][] transactions = new int[][] {
      {1,10,-20,+30,-20,-20}, // +2 overdrafts (ending balance:  -20)
      {0,1,1,1,0,0,1,1,1,1},  // +2 overdrafts (ending balance:  -15)
      {1,115},                // +0 overdrafts (ending balance: +100)
      {2,3,1,0,1},            // +1 overdrafts (ending balance: -100)
    };
    // test with a single transaction of the Integer Amount encoding
    int transactionCount = 2;    
    int balance = AuditableBanking.calculateCurrentBalance(transactions,transactionCount);
    if(balance != -15) {
      System.out.println("FAILURE: calculateCurrentBalance did not return -15 when transactionCount = 2, "
          + "and transactions contained: "+ Arrays.deepToString(transactions));
      foundProblem = true;
      } else
      System.out.println("PASSED TEST 1/2 of calculateCurrentBalance!!!");
   
    // test with four transactions: including one of each encoding
    transactionCount = 3;
    balance = AuditableBanking.calculateCurrentBalance(transactions,transactionCount);
    if(balance != 100) {
      System.out.println("FAILURE: calculateCurrentBalance did not return 100 when transactionCount = 3, "
          + "and transactions contained: "+Arrays.deepToString(transactions));
      foundProblem = true;
    } else
      System.out.println("PASSED TESTS 2/2 of calculateCurrentBalance!!!");
    
    return !foundProblem;
  }
  
  public static boolean testCalculateNumberOfOverdrafts() {
    
    boolean foundProblem = false;
    int[][] transactions = new int[][] {
      {1,10,-20,+30,-20,-20}, // +2 overdrafts (ending balance:  -20)
      {0,1,1,1,0,0,1,1,1,1},  // +2 overdrafts (ending balance:  -15)
      {1,115},                // +0 overdrafts (ending balance: +100)
      {2,3,1,0,1},            // +1 overdrafts (ending balance: -100)
    };
    
    // test with a single transaction of the Integer Amount encoding
    int transactionCount = 1;    
    int overdrafts = AuditableBanking.calculateNumberOfOverdrafts(transactions,transactionCount);
    if(overdrafts != 2) {
      System.out.println("FAILURE: calculateNumberOfOverdrafts did not return 2 when transactionCount = 1, "
          + "and transactions contained: "+ Arrays.deepToString(transactions));
      foundProblem = true;
      } else
      System.out.println("PASSED TEST 1/2 of TestCalculateNumberOfOverdrafts!!!");
   
    // test with four transactions: including one of each encoding
    transactionCount = 4;
    overdrafts = AuditableBanking.calculateNumberOfOverdrafts(transactions,transactionCount);
    if(overdrafts != 5) {
      System.out.println("FAILURE: calculateNumberOfOverdrafts did not return 5 when transactionCount = 4, "
          + "and transactions contained: "+Arrays.deepToString(transactions));
      foundProblem = true;
    } else
      System.out.println("PASSED TESTS 2/2 of TestCalculateNumberOfOverdrafts!!!");
    
    return !foundProblem;
  }

  public static void testAuditableBanking() {
    String[] command = {"0 1 1 0 0 0"};
    AuditableBanking.main(command);
  }
  
}
