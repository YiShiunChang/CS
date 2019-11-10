//////////////////// ALL ASSIGNMENTS INCLUDE THIS SECTION /////////////////////
//
// Title:           ASSIGNMENT 7
// Files:           Sequence, ArithmeticSequenceGenerator, GeometricSequenceGenerator,
//                  FibonacciSequenceGenerator, DigitProductSequenceGenerator, 
//                  and SequenceGeneratorTests.java
// Course:          CS300, Fall 2018
//
// Author:          Mouna Kacem, Yi-Shiun Chang
// Email:           chang242@wisc.edu
// Lecturer's Name: Gary Dahl
//
///////////////////////////// CREDIT OUTSIDE HELP /////////////////////////////
//
// Students who get help from sources other than their partner must fully 
// acknowledge and credit those sources of help here.  Instructors and TAs do 
// not need to be credited here, but tutors, friends, relatives, room mates, 
// strangers, and others do.  If you received no outside help from either type 
// of source, then please explicitly indicate NONE.
//
// Persons:         NONE
// Online Sources:  NONE
//
/////////////////////////////// 80 COLUMNS WIDE ///////////////////////////////
import java.util.Iterator;
import java.util.Scanner;

/**
 * This class represents a sequence generator for a numerical progression
 * This sequence generator can generate arithmetic, geometric, Fibonacci, 
 * and digit product sequences
 * 
 * Note. When using this class:
 * 1. We first run main() method since we don't new Sequence as an object
 * 2. In the main(), we create an Object of Sequence called sequence
 * 3. This sequence object has all methods of Interface Iterable, since it implements Iterable
 * 4. This sequence object has a field sequenceIterator, which is a Iterator<Integer>
 * 5. By calling the constructor of Sequence class, we new the field sequenceIterator
 * 6. In the constructor, we call different generator to build sequenceIterator, 
 *    such as ArithmeticSequenceGenerator
 * 7. Since ArithmeticSequenceGenerator implements Iterator<Integer>, 
 *    we can use it to new an Iterator
 * 8. In the main() again, we use System.out.println(sequence); 
 *    to call our sequence object's toStrin() method
 * 9. When using our sequence object's toStrin(), toString() uses a enhanced for loop,
 *    and enhanced for loop would call Iterator and use Iterator's next() and hasNest() 
 * 
 * @author Mouna & YourName
 */
public class Sequence implements Iterable<Integer> {
  // set of constants that represent the sequence types supported by this sequence generator
  // ex. sequenceType = SequenceType.values()[0] = ARITHMETIC;
  public enum SequenceType {ARITHMETIC, GEOMETRIC, FIBONACCI, DIGIT_PRODUCT};
  private Iterator<Integer> sequenceIterator; // iterator to iterate over this sequence
  private SequenceType sequenceType;  // type of this sequence


  /**
   * Creates a Sequence with respect to a user command line
   * Note. This constructor does not catch any exception
   * 
   * @param command array of integers that represents the user command line to generate a sequence
   */
  public Sequence(int[] command) {
    sequenceType = SequenceType.values()[command[0]]; // set the sequence type
    switch (sequenceType) {
      case ARITHMETIC: 
        // call ArithmeticSequenceGenerator class to create an iterator over an arithmetic sequence
        // with initial element command[1], difference command[2], and size stored at command[3]
        sequenceIterator = new ArithmeticSequenceGenerator(command[1], command[2], command[3]);
        break;
      case GEOMETRIC:
        // call GeometricSequenceGenerator class to create an iterator over an geometric sequence 
        // with initial element at command[1], ratio at command[2], and size at command[3]
        sequenceIterator = new GeometricSequenceGenerator(command[1], command[2], command[3]);
        break;
      case FIBONACCI:
        // call FibonacciSequenceGenerator class to create an iterator over an Fibonacci sequence 
        // with size at command[1]
        sequenceIterator = new FibonacciSequenceGenerator(command[1]);
        break;
      case DIGIT_PRODUCT:
        // call DigitProductSequenceGenerator class to create an iterator 
        // over an DigitProduct sequence with init at command[1], and size at command[2]
        DigitProductSequenceGenerator digitSequence; 
        digitSequence = new DigitProductSequenceGenerator(command[1], command[2]);
        sequenceIterator =  digitSequence.getIterator();
        break;
    }
  }
  
  /**
   * Returns a String that includes the sequence name and the different numbers of the sequence 
   * separated by a single space
   * 
   * @return String representation of the generated sequence
   */
  @Override
  public String toString() {
    // TODO time Complexity: O(N)
    // initialize the String seq by sequenceType.name()
    String seq = sequenceType.name() + " sequence: "; 
    // use a for-each loop to traverse the sequence and add the different numbers of the sequence 
    // to its string representation, separated by a single space
    for (Integer i : this)
      seq += i + " ";
    return seq;
    
    // Note. Enhanced for loop would use iterator's next and hasNext as example demonstrated below:
    // LinkedList is a class that implements Iterable, and list is an object of LinkedList
    // Iterator<String> iterator = list.iterator(); // iterator() method is from Iterable interface
    // while( iterator.hasNext() ) { // .hasNext() method from Iterator interface
    //  String s = iterator.next(); // .next() method from Iterator interface
    //  System.out.println(s);
    // }    
  }

  /**
   * This helper method checks for the correctness of the syntax of the user command 
   * with respect to the sequence type
   * 
   * @param userCommand command entered by the user
   * @return false if the syntax of userCommand is correct 
   *         with respect to the program specification
   */
  private static boolean checkCommandSyntax(String[] userCommand) {
    // TODO add the time complexity of this method, where the problem size N is the length
    // of the userCommand array provided as input argument parameter
    // TODO time complexity: O(1)
    
    boolean syntaxError = false;
    switch (userCommand[0].trim()) {
      case "0": // Arithmetic progression
        if (userCommand.length != 4)
          syntaxError = true;
        break;
      case "1": // Geometric progression
        if (userCommand.length != 4)
          syntaxError = true;
        break;
      case "2": // Fibonacci progression
        if (userCommand.length != 2)
          syntaxError = true;
        break;
      case "3": // Digit Product progression
        if (userCommand.length != 3) 
          syntaxError = true;
        break;
      default:
        syntaxError = true;
    }
    return syntaxError;
  }

  /**
   * This main method represents the driver application: starts the program, 
   * displays the command menu, prompt the user for input, and displays the expected output
   * 
   * @param args
   */
  public static void main(String[] args) {
    String welcome = "   Welcome to the Sequence Generator App   ";
    System.out.println("================="+welcome+"=================\r\n");
    
    // Set the menu, prompt the user command, error messages, and goodbye strings
    final String menu = "COMMAND MENU: \r\n" + " [Sequence_Code] [Sequence_Parameters]\r\n"
        + "   [0 (for ARITHMETIC)  ] [First_Number Common_Difference Sequence_Size]\r\n"
        + "   [1 (for GEOMETRIC)   ] [First_Number Common_Ratio Sequence_Size]\r\n"
        + "   [2 (for FIBONACCI)   ] [Sequence_Size]\r\n"
        + "   [3 (for DIGIT PRODUCT SEQUENCE)] [First_Number Sequence_Size]\r\n " + " \r\n"
        + " [Q]uit Program\r\n";
    final String promptUser = "\nENTER COMMAND: ";
    final String errorMsg = "SYNTAX ERROR. Please refer to the above COMMAND MENU for details.";
    final String formatErrMsg = "ERROR: COMMAND must contain ONLY space separated integer values";
    final String goodBye = "   Thank you for using this App!!!!   ";
    
    // boolean variable that tells if the syntax of the user command is correct
    boolean syntaxError = false; 
    // sequence object
    Sequence sequence; 
    // Scanner object to read the user input from the keyboard
    Scanner input = new Scanner(System.in); 
    // array of integers that represents the command line parts
    int[] seqCommand; 

    // Display menu and prompt the user
    System.out.println(menu);
    System.out.print(promptUser);

    // read the user command line and get the first character in the user command
    String line = input.nextLine().trim();
    char firstChar = line.charAt(0);
    
    // process the user command line
    while (Character.toUpperCase(firstChar) != 'Q') {
      // Array of Strings representing the command
      String[] userCommand = line.trim().split(" "); 
      // Checks for the correctness of the userCommand Syntax
      syntaxError = checkCommandSyntax(userCommand);

      if (!syntaxError) { 
        // process the user command
        seqCommand = new int[userCommand.length];
        try {
          // convert the user command to integers
          for (int i = 0; i < userCommand.length; i++)
            seqCommand[i] = Integer.parseInt(userCommand[i]);
          // generate the sequence with respect to the processed user command line
          sequence = new Sequence(seqCommand);
          // display the sequence
          System.out.println(sequence);
          
        } catch (NumberFormatException e) {
          System.out.println(formatErrMsg);
        } catch (IllegalArgumentException bug) {
          System.out.println(bug.getMessage());
        }
      } else {// Syntax error
        System.out.println(errorMsg);
      }
      // prompt the user
      System.out.print(promptUser);
      line = input.nextLine().trim();
      firstChar = line.charAt(0);

    }
    input.close(); // free the Scanner resource
    
    // display goodbye message
    System.out.println("==================="+goodBye+"==================="); 
  }

  /**
   * Override Interface Iterable<T> 
   * Original iterator(): Returns an iterator over elements of type T.
   * 
   * @return an iterator over elements of type Integer
   */
  @Override
  public Iterator<Integer> iterator() {
    return sequenceIterator;
  }

}