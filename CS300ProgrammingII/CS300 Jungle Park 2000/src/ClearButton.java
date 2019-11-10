//////////////////// ALL ASSIGNMENTS INCLUDE THIS SECTION /////////////////////
//
// Title:           ASSIGNMENT 5
// Files:           this, ParkGUI, JunglePark, Animal, Tiger, Dear, Button
//                  AddAnimalButton, and JungleParkTests.java.
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
 * This class extends Button class to create Button that is used for reseting window
 * This class is used in JunglePark object to setup the display window
 * @author Mouna Kacem, Yi-Shiun Chang
 */
public class ClearButton extends Button {
  
  /**
   * Initialize an object of ClearButton class, and 
   * set its PApplet object where the button will be displayed, and
   * set its this object's label as "Clear Park"
   * @param x
   * @param y
   * @param park PApplet object
   */
  public ClearButton(float x, float y, JunglePark park) {
    super(x, y, park); // call super's constructor
    this.label = "Clear Park";
  }
  
  /**
   * Call clear() of JunglePark's object processing to clear all animals objects
   * in processing.listGUI
   */
  @Override
  public void mousePressed() {
    processing.clear(); // call JunglePark's object.clear()
  }
}
