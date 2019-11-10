//////////////////// ALL ASSIGNMENTS INCLUDE THIS SECTION /////////////////////
//
// Title:           ASSIGNMENT 5
// Files:           this, ParkGUI, JunglePark, Animal, Tiger, Button, 
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
 * Deer class extends Animal class. While inheriting animal class, this class 
 * overrides action() specially to simulates a deer's behavior
 * @author Mouna Kacem, Yi-Shiun Chang
 */
public class Deer extends Animal {
  //range dimension for scanning the neighborhood for threat
  private static final int SCAN_RANGE = 175; 
  private static final String IMAGE_FILE_NAME = "images/deer.png";
  private static final String TYPE = "DR"; // a String that represents the deer type
  // class variable that represents the identifier of the next deer to be created
  private static int nextID = 1; 
  private final int id; // Deer's id: positive number that represents the order of the deer
    
  /**
   * Create a new Deer object positioned at a random position of the display window,
   * and initialize its id and label
   * @param processing
   */
  public Deer(JunglePark processing) { 
    super(processing, IMAGE_FILE_NAME); // set Deer drawing parameters
    id = nextID; // set this deer's identification id
    this.label = TYPE + id; // String that identifies the current deer
    nextID++;
  }
  
  /**
   * Deer's behavior in the Jungle Park,
   * action() scans for threat at the neighborhood of the current deer.
   * If the deer founds any tiger at its proximity, prints a String THREAT on it 
   */
  @Override
  public void action() {  
    if (scanForThreat(SCAN_RANGE)) {
      this.processing.fill(0); // specify font color: black
      this.processing.text("THREAT!", this.getPositionX(), 
          this.getPositionY() - this.image.height / 2 - 4);
    }
  }
  
  /**
   * Traverse all the objects in ArrayList listGUI<ParkGUI>. 
   * If the object is a Tiger object, and it is close enough to this deer,
   * return true
   * @param scanRange
   * @return true if a tiger is close enough to this deer
   */
  public boolean scanForThreat(int scanRange) {
    for (int i = 0; i < processing.listGUI.size(); i++) {
      // isClose is used for checking whether the distance is close enough
      // (Tiger) processing.listGUI.get(i) cast a ParkGUI object into Tiger object
      if (processing.listGUI.get(i) instanceof Tiger) {
        if (this.isClose((Tiger) processing.listGUI.get(i), scanRange))
          return true;
      }
    }
    return false;
  }
   
  
}
