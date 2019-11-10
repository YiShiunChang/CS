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
 * Canvas class uses width and height to define the area of a canvas, and uses 2D array to
 * represents the canvas. Furthermore, it uses drawing stacks to record the steps of drawing 
 * on this canvas.
 * 
 * @author Yi-Shiun Chang, Shuo Han
 */
public class Canvas {
  private final int width;  // width of the canvas, columns of the drawingArray
  private final int height; // height of the canvas, rows of the drawingArray
  private char[][] drawingArray; // 2D character array to store the drawing
  private final DrawingStack<DrawingChange> undoStack; // store previous changes for undo
  private final DrawingStack<DrawingChange> redoStack; // store undone changes for redo
  
  /**
   * A Canvas is initially blank (use the space ' ' character). 
   * This canvas is a 2D array, and it let us draw char on it. Also, it records our steps of draw
   * by using DrawingStack object.
   * 
   * @param width of the canvas
   * @param height of the canvas
   */
  public Canvas(int width, int height) {
    // throws IllegalArgumentException if width or height is 0 or negative
    if (width <= 0 || height <= 0) throw new IllegalArgumentException("width or height can't be 0 or negative");
    this.width = width;
    this.height = height;
    
    // initialize the 2D drawingArray
    this.drawingArray = new char[height][width];
    for (int i = 0; i < drawingArray.length; i++) {
      for (int j = 0; j < drawingArray[i].length; j++) {
        drawingArray[i][j] = ' ';
      }
    }
    
    // initialize the drawing stacks
    this.undoStack = new DrawingStack<DrawingChange>();
    this.redoStack = new DrawingStack<DrawingChange>();
  } 
      
  /**
   * Draw a character at the given position
   * 
   * @param row
   * @param col
   * @param c
   */
  public void draw(int row, int col, char c) {
    // throw an IllegalArgumentException if the drawing position is outside the canvas
    if (row < 0 || col < 0 || row > width-1 || col > height-1)
      throw new IllegalArgumentException("Drawing position cna't be outside the canvas");
    
    // create a matching DrawingChange, which matches canvas to the node that records the 
    // DrawingChange object that represents this step of drawing 
    // col and row represent the position that we draw on the canvas
    // drawingArray[col][row] represents the original char on the canvas
    // c represents the new char that we draw on the canvas
    DrawingChange node = new DrawingChange(col, row, drawingArray[row][col], c);
    
    // if that position is already marked with a different character, overwrite it
    drawingArray[row][col] = c;
    // after making a new change, add the matching DrawingChange to the undoStack 
    undoStack.push(node);
    // after making a new change, the redoStack should be empty
    // redoStack.setRoot();
    for (int i = 0; i < redoStack.size(); i++)
      redoStack.pop();
  } 
      
  /**
   * Undo the most recent drawing change. 
   * 
   * @return true if successful or false otherwise
   */
  public boolean undo() {
    try {
      // pop the latest Node<DrawingChange> from the nodes that we draw
      DrawingChange node = undoStack.pop(); 
      // set the canvas to the original char the latest DrawingChange
      drawingArray[node.y][node.x] = node.prevChar; 
      // an undone DrawingChange should be added to the redoStack, so that we can redo if needed
      redoStack.push(node);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    } catch (NullPointerException e) {
      return false;
    }
  } 
   
  /**
   * Redo the most recent undo drawing change. 
   *
   * @return true if successful or false otherwise
   */
  public boolean redo() {
    try {
      // pop the latest Node<DrawingChange> that we undone 
      DrawingChange node = redoStack.pop();
      // redo the most recent undone drawing change
      drawingArray[node.y][node.x] = node.newChar;
      // a redone DrawingChange should be added (back) to the undoStack
      undoStack.push(node);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    } catch (NullPointerException e) {
      return false;
    }
  } 
  
  /**
   * Return a printable string version of the Canvas.
   * Format example ( _ is blank):
   * X___X
   * _X_X_
   * __X__
   * _X_X_
   * X___X
   * 
   * @return string that presents a canvas
   */
  public String toString() {
    StringBuilder output = new StringBuilder();
    // traverse through the 2D array
    for (int i = 0; i < drawingArray.length; i++) {
      for (int j = 0; j < drawingArray[i].length; j++) {
        output.append(drawingArray[i][j]);
      }
      //  use System.lineSeparator() to put a newline character between rows
      output.append(System.lineSeparator());
    }
    return output.toString();
  } 
  
  /**
   * Prints a record of the changes that are stored on the undoStack
   */
  public void printHistory() {
    int size = undoStack.size();
    // iterate through the undoStack
    for (DrawingChange node: undoStack) {
      System.out.println(size+". Draw '"+node.newChar+"' on ("+node.x+","+node.y+")");
      size --;
    }
  }
}
