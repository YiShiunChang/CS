//////////////////// ALL ASSIGNMENTS INCLUDE THIS SECTION /////////////////////
//
// Title:           ASSIGNMENT 5
// Files:           this, ParkGUI, JunglePark, Animal, Deer, Button, 
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
 * Tiger class extends Animal class. While inheriting animal class, this class 
 * overrides action() specially to simulates a tiger's behavior
 * @author Mouna Kacem, Yi-Shiun Chang
 */
public class Tiger extends Animal {
  //range dimension for scanning the neighborhood for food
  private static final int SCAN_RANGE = 100; 
  private static final String IMAGE_FILE_NAME = "images/tiger.png";
  private static final String TYPE = "TGR"; // A String that represents the tiger type
  //class variable that represents the identifier of the next tiger to be created
  private static int nextID = 1; 
  private final int id; // Tiger's id: positive number that represents the order of the tiger
  private int deerEatenCount; // number of Deers that the current tiger has eaten so far
  
  /**
   * Create a new Tiger object positioned at a random position of the display window,
   * and initialize its id, label, and the number of eaten deers
   * @param processing PApplet object that represents the display window
   */
  public Tiger(JunglePark processing) {
    super(processing, IMAGE_FILE_NAME); // set Tiger drawing parameters
    id = nextID; // set this tiger's identification id
    this.label = TYPE + id; // string that identifies the current tiger
    nextID++;
    this.deerEatenCount = 0;
  }

 /**
   * Tiger's behavior in the Jungle Park,
   * action() scans for food at the neighborhood of the current tiger.
   * If the Tiger founds any deer at its proximity, it hops and eats it
   */
  @Override
  public void action() {
    scanForFood(); // this tiger is searching deer
    if (deerEatenCount > 0)
      displayDeerEatenCount(); // display deerEatenCount
  }
  
  /**
   * Traverse all the objects in ArrayList listGUI<ParkGUI>. 
   * If the object is a Deer object, passes it to hop()
   */
  private void scanForFood() {
    for (int i = 0; i < processing.listGUI.size(); i++) {
      // if processing.listGUI.get(i) is a Deer object, passes it to hop()
      if (processing.listGUI.get(i) instanceof Deer) {
          hop((Deer) processing.listGUI.get(i));
        }
      }
  }
  
  /**
   * Every Deer object is considered as food, but deer will only be hopped when it is
   * near this tiger (<= SCAN_RANGE). Also, hop only happens when this tiger and deer
   * are not being dragging.
   * @param food
   */
  public void hop(Deer food) {
    // isClose is used for checking whether the distance is close enough
    // this.isDragging = tiger.isDragging and food.isDragging = deer.isDragging
    // these isDragging() let hop only happens after uses release the mouse
    if (this.isClose(food, SCAN_RANGE) && !food.isDragging() && !this.isDragging()) {
      this.setPositionX(food.getPositionX()); // tiger is on the position of deer
      this.setPositionY(food.getPositionY());
      this.deerEatenCount++; 
      processing.listGUI.remove(processing.listGUI.indexOf(food)); // deer is eaten
    }
  }
  
  /**
   * Displays the number of eaten deers if any on the top of the tiger image
   */
  public void displayDeerEatenCount() {
    this.processing.fill(0); // specify font color: black
    // display deerEatenCount on the top of the Tiger's image
    this.processing.text(deerEatenCount, 
        this.getPositionX(), this.getPositionY() - this.image.height / 2 - 4);  
  }
 
  /**
   * Get the number of deer eaten by this tiger
   * @return int
   */
  public int getDeerEatenCount() {
    return this.deerEatenCount;
  }
}
