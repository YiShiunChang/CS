//////////////////// ALL ASSIGNMENTS INCLUDE THIS SECTION /////////////////////
//
// Title:           ASSIGNMENT 5
// Files:           this, ParkGUI, JunglePark, Animal, Tiger, Dear, Button
//                  AddAnimalButton, and ClearButton.java.
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
 * This class extends JunglePark class to test functions that are related to JunglePark
 * @author Mouna Kacem, Yi-Shiun Chang
 */
public class JungleParkTests extends JunglePark {
  //PApplet object that represents the display window of this program
  private static JunglePark park; 
                                 
  /**
   * This method checks whether isClose() called by a Deer returns true if a tiger is within its
   * scanRange area and false if called with another tiger as input parameter located outside 
   * the scanRange area
   * @return true when test verifies correct functionality, and false otherwise.
   */
  public static boolean test1isCloseMethod() {
    boolean passed = true;
    // create a deer and two tigers
    Deer d = new Deer(park);
    Tiger t1 = new Tiger(park);
    Tiger t2 = new Tiger(park);
    // set deer at position(200,200)
    d.setPositionX(200);
    d.setPositionY(200);
    // set first tiger at position(400,200)
    t1.setPositionX(400); // tiger is 200px away from deer
    t1.setPositionY(200);
    // set second tiger at position(300,200)
    t2.setPositionX(300); // tiger is 100px away from deer
    t2.setPositionY(200);
    
    // isClose() should return false here, and 175 is deers' detecting range
    if (d.isClose(t1, 175)) { 
      System.out.println("Deer's isClose is returning true when it should return false.");
      passed = false;
    }
    //isClose() should return true here
    if (!d.isClose(t2, 175)) { 
      System.out.println("Deer's isClose is returning false when it should return true.");
      passed = false;
    }
    
    return passed;
  }

  /**
   * This method checks whether isClose() called by a Tiger returns false if another tiger is
   * located outside its scanRange area
   * @return true when test verifies correct functionality, and false otherwise.
   */
  public static boolean test2isCloseMethod() {
    boolean passed = true;
    Tiger t1 = new Tiger(park);
    Tiger t2 = new Tiger(park);
    Tiger t3 = new Tiger(park);
    // set first tiger at position(400,200)
    t1.setPositionX(400); // tiger1 is 50px away from tiger2
    t1.setPositionY(200);
    // set second tiger at position(300,200)
    t2.setPositionX(300); // tiger2 is 50px away from tiger1
    t2.setPositionY(200);
    // set third tiger at position(200,400)
    t3.setPositionX(200); // tiger3 is >100 away from other tigers
    t3.setPositionY(400);
    
    // isClose() should return true here, and 100 is tigers' detecting range
    if (!t1.isClose(t2, 100)) {
      System.out.println("Tiger1's isClose is returning false when it should return true.");
      passed = false;
    }
    // isClose() should return true false,
    if (t2.isClose(t3, 100)) {
      System.out.println("Tiger3's isClose is returning true when it should return false.");
      passed = false;
    }

    return passed;
  }

  /**
   * This method checks whether the deer detects a Tiger present at its proximity
   * @return true when test verifies correct functionality, and false otherwise.
   */
  public static boolean test1DeerScanForThreatMethod() {
    boolean passed = true;
    Deer d1 = new Deer(park);
    park.listGUI.add(d1);
    Tiger t1 = new Tiger(park);
    park.listGUI.add(t1);
    
    // Set first deer at position(200,200)
    d1.setPositionX(200);
    d1.setPositionY(200);
    // Set first tiger at position(25,200)
    t1.setPositionX(25); 
    t1.setPositionY(200);
    
    // scanForThreat() should return true
    if (!d1.scanForThreat(175)) {
      System.out.println("Deer's scanForThreat is returning false when it should return true.");
      passed = false;
    }

    park.listGUI.clear(); // clear all the content of listGUI to get ready for a next scenario

    return passed;
  }

  /**
   * This method checks whether your scanForThreat() method returns false if no Tiger is present
   * within a specific range distance from it
   * @return true when test verifies correct functionality, and false otherwise.
   */
  public static boolean test2DeerScanForThreatMethod() {
    boolean passed = true;
    Deer d1 = new Deer(park);
    park.listGUI.add(d1);
    Tiger t1 = new Tiger(park);
    park.listGUI.add(t1);
    
    // Set first deer at position(400,100)
    d1.setPositionX(400);
    d1.setPositionY(100);
    // Set first tiger at position(50,200)
    t1.setPositionX(50); 
    t1.setPositionY(200);
    
    // scanForThreat() should return false
    if (d1.scanForThreat(175)) {
      System.out.println("Deer's scanForThreat is returning true when it should return false.");
      passed = false;
    }

    park.listGUI.clear(); // clear all the content of listGUI to get ready for a next scenario

    return passed;
  }

  /**
   * This method checks whether the tiger hops on the deer provided to the hop() method as input
   * argument. (1) The tiger should take the position of the deer. (2) The unfortunate deer should
   * be removed from the JunglePark listGUI. (3) The eatenDeerCount should be incremented.
   * 
   * @return true when test verifies correct functionality, and false otherwise.
   */
  public static boolean testTigerHopMethod() {
    boolean passed = true;
    Deer d = new Deer(park);
    Tiger t = new Tiger(park);
    // Set the deer at position(250,250)
    d.setPositionX(250);
    d.setPositionY(250);
    // Set the tiger at position(300,300) tiger is 70.71px away from deer d1
    t.setPositionX(300);
    t.setPositionY(300);
    // add the tiger and the deer to the JunglePark (i.e. to listGUI)
    park.listGUI.add(d);
    park.listGUI.add(t);
    
    t.hop(d); // tiger hops on the deer
    if (t.getPositionX() != d.getPositionX() && t.getPositionY() != d.getPositionY()) {
      // tiger should move to the position of the deer
      System.out.println("Tiger did not move correctly when hopping.");
      passed = false;
    }
    if (park.listGUI.contains(d)) {
      // deer should be removed from the park
      System.out.println("Deer was not removed after being hopped on.");
      passed = false;
    }
    if (t.getDeerEatenCount() != 1) {
      // deerEatenCount should be incremented. It was 0
      System.out.println("deerEatenCount should be incremented after the tiger hopped on a deer.");
      passed = false;
    }

    park.listGUI.clear(); // clear all the content of listGUI to get ready for a next scenario

    return passed;
  }

  /**
   * Run JungleParkTests program as a PApplet client and setup it as a test case
   * @param args
   */
  public static void main(String[] args) {
    // Call PApplet.main(String className) to start this program as a PApplet client application
    PApplet.main("JungleParkTests");
  }

  /**
   * This is a callback method automatically called only one time when the PApplet application
   * starts as a result of calling PApplet.main("PAppletClassName"); Defines the initial environment
   * properties of this class/program As setup() is run only one time when this program starts, all
   * your test methods should be called in this method
   */
  @Override
  public void setup() {
    super.setup(); // calls the setup() method defined
    park = this; // set the park to the current instance of Jungle
    
    System.out.println("test1isCloseMethod(): " + test1isCloseMethod());
    System.out.println("test2isCloseMethod(): " + test2isCloseMethod());
    System.out.println("test1DeerScanForThreatMethod(): "+test1DeerScanForThreatMethod());
    System.out.println("test2DeerScanForThreatMethod(): "+test2DeerScanForThreatMethod());
    System.out.println("testTigerHopMethod(): " + testTigerHopMethod());

    // close PApplet display window (No need for the graphic mode for these tests)
    park.exit();
  }
}