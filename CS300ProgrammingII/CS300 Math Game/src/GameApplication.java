//////////////////// ALL ASSIGNMENTS INCLUDE THIS SECTION /////////////////////
//
// Title:           ASSIGNMENT 6
// Files:           GameList.java, GameNode.java, GameApplication.java, 
//                  and GameTests.java
// Course:          CS300, Fall 2018
//
// Author:          Yi-Shiun Chang
// Email:           chang242@wisc.edu
// Lecturer's Name: Gary Dahl
//
//////////////////// PAIR PROGRAMMERS COMPLETE THIS SECTION ///////////////////
//
// Partner Name:    Shuo Han
// Partner Email:   shan238@wisc.edu
// Partner Lecturer's Name: Gary Dahl
// 
// VERIFY THE FOLLOWING BY PLACING AN X NEXT TO EACH TRUE STATEMENT:
//   _X_ Write-up states that pair programming is allowed for this assignment.
//   _X_ We have both read and understand the course Pair Programming Policy.
//   _X_ We have registered our team prior to the team registration deadline.
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
import java.util.Random;
import java.util.Scanner;

/**
 * This class is a math game in which the player is presented with a list of numbers and
 * a target goal number. The player will choose one of these numbers along with an operator: 
 * addition (+), subtraction (-), multiplication (x), division (/), or concatenation (&). 
 * The chosen number will then be combined with the number following it in the list using 
 * the specified operator, so that two list elements become one. Before these nodes are 
 * combined, a new randomly chosen value is added to the end of the list, so that the final 
 * length of the list remains the same. The player makes repeated choices like this, 
 * until the target number appears as one of the values within the list. 
 * 
 * @author Yi-Shiun Chang, Shuo Han
 */
public class GameApplication {
  private Random ran = new Random(); // a generator for building new GameNode
  private Scanner scan = new Scanner(System.in); // used for reading user's input
  private int target = ran.nextInt(90)+10; // build a target between 10 and 99  
  private int move = 0; // how many move does user take to finish this game
  private GameList list = makeGameList(ran); // a game list consists of game nodes
  private boolean quitGame = false; // ends the game or not
  
  /**
   * Start the main loop of this math game
   * @param args null
   */
  public static void main(String[] args) {
    GameApplication newGame = new GameApplication(); // initialize this game
    
    System.out.println("Welcome to this game.");
    while (!newGame.quitGame) { // stop while loop when quitGame = true
      // print the basic information of the game and get the user's input
      String userInput = gameProcess1(newGame.target, newGame.move, newGame.list, newGame.scan);
      
      try {
        // if input is quit, then end this game
        if (userInput.toLowerCase().equals("quit")) {
          System.out.println("Goodbye");
          break;
        }
        
        // get the operation that user wants
        char operation = userInput.charAt(userInput.length()-1);
        // get the number that user wants to operate
        int num = Integer.parseInt(userInput.substring(0, userInput.length()-1));
        // warn user if the number is not in the list
        if (!newGame.list.contains(num)) 
          System.out.println("WARNING: "+num+" is not in this game list");
        // warn user if the operation is not existed
        else if (GameOperator.getFromChar(operation) == null) 
          System.out.println("WARNING: "+operation+" is not a valid operation");
        else {
          newGame.move ++; // follow instruction to add move first
          newGame.list.addNode(new GameNode(newGame.ran)); // add new node second
          // do operation on the specific number and a number after it
          newGame.list.applyOperatorToNumber(num, GameOperator.getFromChar(operation));
        }
        
        // if a number in this list is equal to target, user win
        if (newGame.list.contains(newGame.target)) {
          System.out.println("\nCongratulations, you won in "+newGame.move+" moves.");
          System.out.print("Solution: "+newGame.list.toString());
          newGame.quitGame = true; // set quitGame = true to end while loop
          }
      } catch (IndexOutOfBoundsException ie) {
        System.out.println("WARNING: your input is empty!");
      } catch (NumberFormatException ne) {
        System.out.println("WARNING: your input doesn't follow format");
      } catch (Exception e) {
        System.out.println("WARNING: unknown exception! Please check input!");
      }
    }
  }
  
  /**
   * Add 7 game nodes into this game list
   * 
   * @param ran
   * @return GameList consists of 7 game nodes
   */
  static private GameList makeGameList(Random ran) {
    GameList list = new GameList();;
    for (int i = 0; i < 7; i++) {
      list.addNode(new GameNode(ran));
    }
    return list;
  }
  
  /**
   * Print the basic information of the game, for example:
   * Goal: 50 Moves Taken: 0
   * Puzzle: 8 -> 2 -> 2 -> 1 -> 8 -> 4 -> 3 -> END
   * Ask user to input instruction, for example:
   * Number and Operation [+, -, x, /, &] to Apply:
   * Trim the user's input
   * 
   * @param target
   * @param move
   * @param list
   * @param scan
   * @return String user's input after trimmed
   */
  static private String gameProcess1(int target, int move, GameList list, Scanner scan) {
    System.out.println("\nGoal: "+target+" Moves Taken: "+move);
    System.out.println("Puzzle: "+list.toString());
    String operations = GameOperator.ALL_OPERATORS.toString(); // get operations in GameOperator
    System.out.print("Number and Operation " + operations +  " to Apply: ");
    String userInput = scan.nextLine().trim(); // remove leading and trailing whitespace
    return userInput;
  }
}
