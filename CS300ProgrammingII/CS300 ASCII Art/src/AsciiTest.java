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

/**
 * This class includes tests that check other classes in this project.
 * 
 * @author Yi-Shiun Chang, Shuo Han
 */
public class AsciiTest {
  /**
   * Check toString and undo methods of Canvas object
   * 
   * @return true if pass
   */
  public static boolean testStackDrawToStringUndo(){
    // create a canvas with 9 area
    Canvas canvas = new Canvas(3,3);
    
    // draw a, b, and c on canvas
    canvas.draw(0, 0, 'a');
    canvas.draw(1, 1, 'b');
    canvas.draw(2, 2, 'c');
    
    // the canvas should look like
    String actual = "a  " + System.lineSeparator() + 
        " b " + System.lineSeparator() + "  c"+System.lineSeparator();
    
    // check the output of toString with what is should look like
    if (!canvas.toString().equals(actual)) return false;
    
    // undo the canvas by undoing its undoStack
    if (!canvas.undo()) return false;
    if (!canvas.undo()) return false;
    if (!canvas.undo()) return false;
    // the fourth time canvas.undo() should return false
    if (canvas.undo()) return false;
    
    return true;
  } 
  
  /**
   * This test should create a stack, push a DrawingChange onto the stack,
   * and then use peek to verify that the correct item is at the top of the stack. 
   * 
   * @return true if the correct item is at the top of the stack
   */
  public static boolean testStackPushPeek(){
    // create a stack and a DrawingChange object that needs to be pushed into the stack
    DrawingStack<DrawingChange> stack = new DrawingStack<DrawingChange>();
    DrawingChange draw1 = new DrawingChange(2, 2, 'c', 'd');
    DrawingChange draw2 = new DrawingChange(3, 3, 'e', 'f');
    
    // push draw in to stack
    stack.push(draw1);
    stack.push(draw2);
    
    // check whether the first node is what we expected 
    if (stack.peek().equals(draw2))
      return true;
    return false;
  }
  
  /**
   * This test method runs multiple other test methods. 
   * 
   * @return return false if any of its component tests fail
   */
  public static boolean runStackTestSuite() {
    int passCount = 0; // Keep track of tests passed
    int failCount = 0; // Keep track of tests failed
    
    // increase the failCount if tests failed, increase passeCount is passed
    if (testStackPushPeek()) passCount++; else failCount++;
    if (testStackDrawToStringUndo()) passCount++; else failCount++;
    
    // Print error summary if any test failed.
    if(failCount != 0)
      System.out.format("%d out of %d test failed.\n", failCount, failCount+passCount);
    
    if (failCount == 0)
      return true;
    return false;
  }
  
  /**
   * Run runStackTestSuite()
   * 
   * @param args
   */
  public static void main(String[] args) {
    if (runStackTestSuite()) 
      System.out.println("All tests passed!");
  }
}
