import java.util.*;

public class ProfileSample {
  
  /**
   *  
   * @param args
   */
  public static void main(String args[]) {
    // if there is no argument, print "Expected one argument" and exit(1)
    if (args.length < 1) {
      System.out.println("Expected one argument: <num_elements>");
      System.exit(1);
    }
    // get the first item from argument and parse it into long
    long numElements = Long.parseLong(args[0]);
    ArrayList<Long> elements = new ArrayList<Long>();
    // insert i elements into ArrayList elements
    for (long i = 0; i < numElements; i++) {
      elements.add(i);    
    }
    // print how many elements are inserted in the hash set
    System.out.println(String.format("inserted %d elements into the hash set", numElements));
  }
}
