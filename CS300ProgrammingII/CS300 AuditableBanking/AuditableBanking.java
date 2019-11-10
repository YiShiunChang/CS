import java.util.Arrays;
import java.util.Scanner;

public class AuditableBanking {
  
  public static void main(String[] args) {
    System.out.println("========== Welcome to the Auditable Banking App ==========");
    int[][] allTransactions = new int[100][];
    int allTransactionsCount = 0;
    Scanner input = new Scanneclr(System.in);
      
    while (true) {
      printContext();
      if (input.hasNextLine()) {
        String command = input.nextLine();
        if (command.toUpperCase().charAt(0) == 'Q') {break;}
        allTransactionsCount = processCommand(command, allTransactions, allTransactionsCount);
      }
    }
    
    input.close();
    System.out.println("============ Thank you for using this App!!!! ============");
    System.out.println(Arrays.deepToString(allTransactions));
    }
  
  public static void printContext() {
    System.out.println("COMMAND MENU:\n"
        + "Submit a Transaction (enter sequence of integers separated by spaces)\n"
        + "Show Current [B]alance\n"
        + "Show Number of [O]verdrafts\n"
        + "[Q]uit Program");
    System.out.print("ENTER COMMAND: ");
  }
  
  public static int submitTransactions(int[] newTransactions, int[][] allTransactions, int allTransactionsCount) {
    if (allTransactions.length > allTransactionsCount) {
      allTransactions[allTransactionsCount] = newTransactions;
      return allTransactionsCount+1;
    } else {
      System.out.println("allTransactions' arrays are full");
      return allTransactionsCount;
    }
  }
  
  public static int processCommand(String command, int[][] allTransactions, int allTransactionsCount) {
    String[] commandSList = command.split(" ");
    
    if (commandSList[0].toUpperCase().equals("B")) {
      int balance = calculateCurrentBalance(allTransactions, allTransactionsCount);
      System.out.println("Current Balance: "+ balance + "\n");
    } else if (commandSList[0].toUpperCase().equals("O")) {
      int overdraft = calculateNumberOfOverdrafts(allTransactions, allTransactionsCount);
      System.out.println("Number of Overdrafts: "+ overdraft + "\n");
    } else {
      int[] commandNList = new int[commandSList.length];
      
      for (int i = 0; i < commandSList.length; i++) {
        commandNList[i] = Integer.parseInt(commandSList[i]);
      }
      
      if (allTransactions.length > allTransactionsCount && 2 >= commandNList[0] && commandNList[0] >= 0 ) {
        allTransactionsCount = submitTransactions(commandNList, allTransactions, allTransactionsCount);
        System.out.println("");
      }
    }
    return allTransactionsCount;
  }
  
  public static int calculateCurrentBalance(int[][] allTransactions, int allTransactionsCount) {
    int subBalance = 0;
    for (int i = 0; i < allTransactionsCount; i++) {
      int[] newArray = Arrays.copyOfRange(allTransactions[i], 1, allTransactions[i].length);
      int newArrayLen = newArray.length;
      if (allTransactions[i][0] == 0) {
        for (int j = 0; j < newArrayLen; j++) {
          if (newArray[j] == 1) {
            subBalance += 1;
          } else {
            subBalance -= 1;
          }
        }
      } else if (allTransactions[i][0] == 1) {
        for (int j = 0; j < newArrayLen; j++) {
          subBalance += newArray[j];
        }
        
      } else if (allTransactions[i][0] == 2) {
        for (int j = 0; j < newArrayLen; j++) {
          switch (j) {
          case 0:
            subBalance -= (20*newArray[j]);
            break;
          case 1:
            subBalance -= (40*newArray[j]);
            break;
          case 2:
            subBalance -= (80*newArray[j]);
            break;
          case 3:
            subBalance -= (100*newArray[j]);
            break;
          }
        }
      } else {
        System.out.println("Worong format of transaction");
      }
    }
    return subBalance;
  }

  public static int calculateNumberOfOverdrafts(int[][] allTransactions, int allTransactionsCount) {
    int balance = 0;
    int overdraft = 0;
    
    for (int i = 0; i < allTransactionsCount; i++) {
      int[] newArray = Arrays.copyOfRange(allTransactions[i], 1, allTransactions[i].length);
      int newArrayLen = newArray.length;
      if (allTransactions[i][0] == 0) {
        for (int j = 0; j < newArrayLen; j++) {
          if (newArray[j] == 1) {
            balance += 1;
          } else {
            balance -= 1;
          }
          if (balance < 0 && newArray[j] != 1) {
            overdraft ++;
          }
        }
      } else if (allTransactions[i][0] == 1) {
        for (int j = 0; j < newArrayLen; j++) {
          balance += newArray[j];
          if (balance < 0 && newArray[j] < 0) {
            overdraft ++;
          }
        }
      } else if (allTransactions[i][0] == 2) {
        for (int j = 0; j < newArrayLen; j++) {
          switch (j) {
          case 0:
            balance -= (20*newArray[j]);
            break;
          case 1:
            balance -= (40*newArray[j]);
            break;
          case 2:
            balance -= (80*newArray[j]);
            break;
          case 3:
            balance -= (100*newArray[j]);
            break;
          }
          if (balance < 0 && newArray[j] > 0) {
            overdraft ++;
          }
        }
      } else {
        System.out.println("Worong format of transaction");
      }
    }
    return overdraft;
  }

}
