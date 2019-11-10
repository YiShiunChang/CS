//////////////////// ALL ASSIGNMENTS INCLUDE THIS SECTION /////////////////////
//
// Title:           ASSIGNMENT 2
// Files:           this
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
import java.util.Random;

public class JunglePark {
  private static PApplet processing; 
  private static PImage backgroundImage;  
  private static Tiger[] tigers = new Tiger[8];  
  private static Random randGen = new Random();
  
  /**
   * Use Utility.startApplication(), a Java GUI, until use closes it. 
   * @param args
   */
  public static void main(String[] args) {
    Utility.startApplication();
  }
  
  /**
   * Visualize the background of GUI processingObj, JUNGLE PARK AREA. 
   * @param processingObj represents a reference to the graphical interface of the application
   */
  public static void setup(PApplet processingObj) {
    processing = processingObj; 
    processing.background(245, 255, 250);
    backgroundImage = processing.loadImage("images/background.png");
    processing.image(backgroundImage, processing.width/2, processing.height/2);

  }
  
  /**
   * Update and draw tigers on the JUNGLE PARK AREA. 
   */
  public static void update() {
    processing.background(245, 255, 250);
    processing.image(backgroundImage, processing.width/2, processing.height/2);
    
    for (int i = 0; i < tigers.length; i++) {
      if (tigers[i] != null) {
        tigers[i].draw();
        if (tigers[i].isDragging()) {
          tigers[i].setPositionX(processing.mouseX);
          tigers[i].setPositionY(processing.mouseY);
        }
      }
    }    
  }
  
  /**
   * Determine whether user's mouse is on a tiger or not 
   * @param tiger a tiger object that includes information about position, 
   *        image, isDragging, etc.
   * @return true if user's mouse is on a tiger
   */
  public static boolean isMouseOver(Tiger tiger) {
    float x = tiger.getPositionX();
    float y = tiger.getPositionY();
    float height = tiger.getImage().height;
    float width = tiger.getImage().width;
    if (x+height/2 > processing.mouseX && processing.mouseX > x-height/2) {
      if (y+width/2 > processing.mouseY && processing.mouseY > y-width/2) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }
  
  /**
   * Set true to tiger object's setDragging method when user push down mouse's 
   * left click on a tiger image. If mouse is on multiple tigers, a tiger 
   * with the lowest index is selected.
   */
  public static void mouseDown() {
    for (int i = 0; i < tigers.length; i++) {
      if (tigers[i] != null) {
        if (isMouseOver(tigers[i])) {
          tigers[i].setDragging(true);
          return;
        }
      }
    }
  }
  
  /**
   * Set false to tiger object's setDragging method when user stop pushing 
   * down mouse's left click on a tiger image. 
   */
  public static void mouseUp() {
    for (int i = 0; i < tigers.length; i++) {
      if (tigers[i] != null) {
        if (isMouseOver(tigers[i])) {
          tigers[i].setDragging(false);
        }
      }
    }
  }
  
  /**
   * Call PApplet object, which is processing, and check whether processing.key
   * return t/r or not. If it is t/T, adds a tiger on GUI until there are eight 
   * tigers. If it is r/R, removes a tiger when user's mouse is on a tiger image,
   * and always remove a tiger with the lowest index when mouse is multiple tigers.     
   */
  public static void keyPressed() {
    if (Character.toUpperCase(processing.key) == 'T') {
      for (int i = 0; i < tigers.length; i++) {
        if(tigers[i] == null) {
          tigers[i] = new Tiger(processing, 
              (float) randGen.nextInt(processing.width), 
              (float) randGen.nextInt(processing.height));
          return;
        }
      }
    }
    else if (Character.toUpperCase(processing.key) == 'R') {
      for (int i = 0; i < tigers.length; i++) {
        if(tigers[i] != null && isMouseOver(tigers[i])) {
          tigers[i] = null;
          return;
        }
      }
    }
  }

}

