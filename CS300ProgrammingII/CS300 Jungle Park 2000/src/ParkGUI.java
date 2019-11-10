//////////////////// ALL ASSIGNMENTS INCLUDE THIS SECTION /////////////////////
//
// Title:           ASSIGNMENT 5
// Files:           this, JunglePark, Animal, Tiger, Deer, Button, 
//                  AddAnimalButton, ClearButton, and JungleParkTests.java.
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

/**
 * This is an interface of park graphical user interface with four methods
 * @author Mouna Kacem
 */
public interface ParkGUI {
  public void draw(); // draw a ParkGUI object (either an animal or a button) to the display window

  public void mousePressed(); // called each time the mouse is Pressed

  public void mouseReleased(); // called each time the mouse is Released

  public boolean isMouseOver(); // checks whether the mouse is over a ParkGUI object

}
