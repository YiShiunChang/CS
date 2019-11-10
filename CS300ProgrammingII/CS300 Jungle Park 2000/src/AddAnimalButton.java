//////////////////// ALL ASSIGNMENTS INCLUDE THIS SECTION /////////////////////
//
// Title:           ASSIGNMENT 5
// Files:           this, ParkGUI, JunglePark, Animal, Tiger, Dear, 
//                  Button, ClearButton, and JungleParkTests.java.
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
 * This class extends Button class to create Buttons that are used for adding animals
 * This class is used in JunglePark object to setup the display window
 * @author Mouna Kacem, Yi-Shiun Chang
 */
public class AddAnimalButton extends Button {
  private String type; // type of the animal to add
  
  /**
   * Initialize an object of AddAnimalButton class, and 
   * set its PApplet object where the button will be displayed, and
   * set its position in the display window, and
   * set its this object's label as "Add " + type, such as Add Tiger
   * @param type Tiger, Deer
   * @param x
   * @param y
   * @param park PApplet object
   */
  public AddAnimalButton(String type, float x, float y, JunglePark park) {
      super(x, y, park); // call super Button class to initialize this object
      this.type = type.toLowerCase();
      this.label = "Add " + type;
  }
   
  /**
   * When "Add Tiger" is pressed, new a Tiger object in listGUI
   * When "Add Deer" is pressed, new a Deer object in listGUI
   */
  @Override
  public void mousePressed() {
    if (isMouseOver()) {
      switch (type) {
        case "tiger": // add a new Tiger object in listGUI
          processing.listGUI.add(new Tiger(processing));
          break;
        case "deer": // add a new Deer object in listGUI
          processing.listGUI.add(new Deer(processing));
          break;
      }
    }
  }
}
