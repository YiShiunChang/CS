//////////////////// ALL ASSIGNMENTS INCLUDE THIS SECTION /////////////////////
//
// Title:           ASSIGNMENT 5
// Files:           this, ParkGUI, JunglePark, Tiger, Deer, Button, 
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
import java.util.Random;

/**
 * This class represents an animal in the Jungle Park application
 * It implements the interface ParkGUI
 * @author Mouna Kacem, Yi-Shiun Chang
 */
public class Animal implements ParkGUI {
  private static Random randGen = new Random(); // generator of random numbers for 
                                                // setting animals' positions
  protected String label; // represents an animal's identifier
  protected JunglePark processing; // PApplet object that represents the display window
  protected PImage image; // an animal's image
  private float[] position; // an animal's position in the display window
                            // Usage: position[0: x-coordinate, or 1: y-coordinate]
  private boolean isDragging; // indicates whether the animal is being dragged or not


  /**
   * Create a new Animal object positioned at a given position of the display window
   * @param processing PApplet object that represents the display window
   * @param positionX x-coordinate of the animal's image in the display window
   * @param positionY y-coordinate of the animal's image in the display window
   * @param imageFileName filename of the animal image
   */
  public Animal(JunglePark processing, float positionX, float positionY, String imageFileName) {
    this.processing = processing; // set the PApplet Object where the animal will be drawn
    this.position = new float[] {positionX, positionY}; // set the position of this animal object
    this.image = processing.loadImage(imageFileName); // set the image of this animal object
    this.isDragging = false; // initialize the animal as not dragging
  }

  /**
   * Create a new Animal object positioned at a random position of the display window
   * @param processing PApplet object that represents the display window
   * @param imageFileName filename of the animal image
   */
  public Animal(JunglePark processing, String imageFileName) {
    // call another constructor
    this(processing, (float) randGen.nextInt(processing.width),
        Math.max((float) randGen.nextInt(processing.height), 100), imageFileName);
  }

  /**
   * Draw the animal to the display window. It also sets its position to the mouse position if 
   * it is being dragged (i.e. if an animals's isDragging field is set to true).
   */
  @Override
  public void draw() {
    // if an animal is being dragged, set its position to the mouse position 
    // with respect to the display window (processing) dimension
    if (this.isDragging) {
      // for x-axis
      if (this.processing.mouseX < 0) // mouse outside the screen
        this.position[0] = 0;
      else if (this.processing.mouseX > this.processing.width) // mouse outside the screen
        this.position[0] = this.processing.width;
      else
        this.position[0] = this.processing.mouseX;
      // for y-axis
      if (this.processing.mouseY < 0) // mouse outside the screen
        this.position[1] = 0;
      else if (this.processing.mouseY > this.processing.height) // mouse outside the screen
        this.position[1] = this.processing.height;
      else
        this.position[1] = this.processing.mouseY;
    }
    // before drawing a image on the window, let each animal does its action if there is any 
    this.action();
    // draw the tiger at its current position
    this.processing.image(this.image, this.position[0], position[1]);
    // display label
    displayLabel();
  }


  /**
   * Display an animal object label on the application window screen
   */
  private void displayLabel() {
    this.processing.fill(0); // specify font color: black
    this.processing.text(label, 
        this.position[0], this.position[1] + this.image.height / 2 + 4); // display label text
  }

  /**
   * Check if the mouse is over the image of this given animal object
   * @return true if the mouse is over a given animal object, false otherwise
   */
  @Override
  public boolean isMouseOver() {
    int animalWidth = image.width; // image width
    int animalHeight = image.height; // image height

    // checks if the mouse is over the tiger
    if (processing.mouseX > position[0] - animalWidth / 2
        && processing.mouseX < position[0] + animalWidth / 2
        && processing.mouseY > position[1] - animalHeight / 2
        && processing.mouseY < position[1] + animalHeight / 2) {
      return true;
    }
    return false;
  }
  
  /**
   * When mouse is pressed and it is on this objects's image, set isDragging true
   */
  @Override
  public void mousePressed() {
    if (isMouseOver())
      isDragging = true;
  }

  /**
   * When mouse is released, set this object's isDragging false
   */
  @Override
  public void mouseReleased() {
    isDragging = false;
  }

  /**
   * this object's identifier means the name of the animal that this object is 
   * representing, such as tiger and deer 
   * @return the label that represents this object's identifier
   */
  public String getLabel() {
    return label;
  }


  /**
   * @return the image of type PImage of the tiger object
   */
  public PImage getImage() {
    return image;
  }


  /**
   * @return the X coordinate of the animal position
   */
  public float getPositionX() {
    return position[0];
  }

  /**
   * @return the Y coordinate of the animal position
   */
  public float getPositionY() {
    return position[1];
  }


  /**
   * @param position the XPosition to set
   */
  public void setPositionX(float position) {
    this.position[0] = position;
  }

  /**
   * @param position the YPosition to set
   */
  public void setPositionY(float position) {
    this.position[1] = position;
  }

  /**
   * @return true if the animal is being dragged, false otherwise
   */
  public boolean isDragging() {
    return isDragging;
  }

  /**
   * Check whether two animals are closer enough
   * @param otherAnimal
   * @param range
   * @return true if the distance otherAnimal and this animal is smaller than range
   */
  public boolean isClose(Animal otherAnimal, int range) { 
    if (this.distance(otherAnimal) <= range)
      return true;
    return false;      
  }
  
  /**
   * Compute the Euclidean distance between the current animal and another one
   * @param otherAnimal reference to another animal
   * @return distance between the current animal and otherAnimal
   */
  public double distance(Animal otherAnimal) {
    return Math.sqrt(Math.pow(this.getPositionX() - otherAnimal.getPositionX(), 2)
        + Math.pow(this.getPositionY() - otherAnimal.getPositionY(), 2));
  }
  
  /**
   * Define the behavior of the current animal in the jungle park
   */
  public void action() {
    // This method should be overriden by a subclass
  }
  

}
