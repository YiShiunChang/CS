//////////////////// ALL ASSIGNMENTS INCLUDE THIS SECTION /////////////////////
//
// Title:           ASSIGNMENT 8
// Files:           AsciiArt, Canvas, DrawingStack, DrawingStackIterator,
//                  DrawingChange, AsciiTest
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
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * This class is used to interact with user to draw chars on canvas.
 * 
 * Here are some specific requirements for how this interactive session should proceed: 
 * 1. Launching the AsciiArt class’s main method should immediately begin a driver loop 
 * and present the user with a menu.
 *  
 * 2. From the user’s perspective, the top left corner of the Canvas is located at 
 * row 0, column 0. When prompting the user for drawing coordinates, 
 * ask for the row and then the column on separate lines. 
 *  
 * 3. The menu should be stable. Bad user input should result in a message indicating 
 * the bad input and should prompt the user again. Meet this requirement using any combination 
 * of careful coding and try/catch blocks (However, do not catch Exception or RuntimeException).
 *  
 * @author Yi-Shiun Chang, Shuo Han
 */
public class AsciiArt {
  /**
   * Execute main driver loop.
   * 
   * @param args
   */
  public static void main(String[] args) {
    Canvas sheet = null; // initialize the canvas object
    boolean quitGame = false; // if true, stop driver loop
    Scanner sc = new Scanner(System.in);
    int input; // record the user intention for the canvas
    
    // driver loop
    System.out.println("Welcome!!");
    while(!quitGame) {
      printMenu();
      // check whether the user input is int
      input = userIntention(sc);
      // execute different function for different input
      switch(input) {
      case 1: // create canvas
        System.out.print("Input canvas width: ");
        int width = canvasInput(sc);
        System.out.print("Input canvas height: ");
        int height = canvasInput(sc);
        sheet = new Canvas(width, height);
        break;
      case 2: // draw a char
        boolean valid = false;
        while (!valid) {
          try {
            System.out.print("Input row: ");
            int row = positionInput(sc);
            System.out.print("Input column: ");
            int col = positionInput(sc);
            System.out.print("Input character: ");
            char c = sc.next().charAt(0);
            sheet.draw(row, col, c);
            valid = true;
          } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
          } catch (NullPointerException e) {
            System.out.println("Please create a canvase first.");
            valid = true;
          }
        }
        break;
      case 3: // delete a drawn char
        sheet.undo();
        break;
      case 4: // roll back to recover a deletion
        sheet.redo();
        break;
      case 5: // draw the canvas
        System.out.println(sheet.toString());
        break;
      case 6: // draw history move
        sheet.printHistory();
        break;
      case 7: // leave game
        quitGame = true;
        break;
      }
    }
    System.out.println("Bye");
  }
  
  /**
   * Menu description
   */
  private static void printMenu() {
    String menu = "\n======== MENU ========\n" +
        "[1] Create a new canvas\n" + 
        "[2] Draw a character\n" + 
        "[3] Undo drawing\n" + 
        "[4] Redo drawing\n" + 
        "[5] Show current canvas\n" + 
        "[6] Show drawing history\n" + 
        "[7] Exit";
    System.out.println(menu);     
  }
  
  /**
   * Check the input of a user to ensure AsciiArt function
   * 
   * @param sc
   * @return int is user's intention
   */
  private static int userIntention(Scanner sc) {    
    do {
      try {
        // read the input
        int input = sc.nextInt();
        
        // if input is out of the range, throw exception
        if (input <= 0 || input > 7)
          throw new IllegalArgumentException();
        
        // if there is no exception being thrown, return user's input
        return input;
      } catch (InputMismatchException e) {
        System.out.println("Please input a number from 1 to 7.");
        sc.next();
      } catch (IllegalArgumentException e) {
        System.out.println("Please input a number from 1 to 7.");
      }
    } while (true);
  }
  
  /**
   * When user creates a canvas, check the input of canvas' width and height is reasonable.
   * 
   * @param sc
   * @return int canvas' height or width
   */
  private static int canvasInput(Scanner sc) {
    do {
      try {
        // read the input
        int input = sc.nextInt();
        
        // if input less than 0, throw exception
        if (input <= 0)
          throw new IllegalArgumentException();
        
        // if there is no exception being thrown, return user's input
        return input;
      } catch (InputMismatchException e) {
        System.out.println("Please input a number.");
        sc.next();
      } catch (IllegalArgumentException e) {
        System.out.println("Please input a number greater than 0.");
      }
    } while (true);
  }
  
  /**
   * Check whether user's input of char is within the range of the canvas
   * 
   * @param sc
   * @return int char's position on col or row
   */
  private static int positionInput(Scanner sc) {
    do {
      try {
        // read the input
        int input = sc.nextInt();
        // if there is no exception being thrown, return user's input
        return input;
      } catch (InputMismatchException e) {
        System.out.println("Please input a number.");
        sc.next();
      } 
    } while (true);
  }
}
