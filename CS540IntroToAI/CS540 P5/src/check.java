import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class check {
  static Comparator<String[]> stringArrayCompare = new Comparator<String[]>() {
    // example of String[] strings: [neighbor's name, neighbor's category]
    public int compare(String[] strings, String[] otherStrings) {
      return strings[0].compareTo(otherStrings[0]);
    }
  };
  
  public static void main(String[] args) {
    ArrayList<String[]> knnInf = new ArrayList<String[]>();
    knnInf.add(new String[] {"watermellon", "fruit"});
    knnInf.add(new String[] {"dragonfruit", "fruit"});
    knnInf.add(new String[] {"banana", "zzz"});
    knnInf.add(new String[] {"apple", "fruit"});
    
    
    // sort the knnInf: ex. [banana, fruit], [apple, fruit] -> [apple, fruit], [banana, fruit]
    Collections.sort(knnInf, stringArrayCompare);
    
    for (int i = 0; i < knnInf.size(); i++) {
      System.out.println(knnInf.get(i)[0]);
    }
    
  }

}
