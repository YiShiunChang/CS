import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;


/**
 * A kNN classification algorithm implementation.
 */
public class KNN {
  
  /**
   * This method calculate the Euclidean distance of a test item and a training item 
   * 
   * @param test features of a test item
   * @param training features of a training item
   * @return double Euclidean distance
   */
  private double euclideanD (double[] test, double[] training) {
    double d1 = Math.abs(test[0] - training[0]);
    double d2 = Math.abs(test[1] - training[1]);
    double d3 = Math.abs(test[2] - training[2]);
    double distance = Math.sqrt(d1*d1 + d2*d2 + d3*d3);
    return distance;
  }
  
  /**
   * This is a KNN algorithm
   * 
   * @param trainingData an Item array of training data
   * @param testData an Item array of test data
   * @param k the number of neighbors to use for classification
   * @return KNNResult contains classification accuracy, category assignment, and KnearestNeighbors
   */
  public KNNResult classify(Item[] trainingData, Item[] testData, int k) {
    
    // the accuracy of this kNN algorithm in accordance with k
    double accuracy; 
    int correctPredict = 0; // the number of correct prediction
    int wrongPredict = 0; // the number of wrong prediction 
    
    // the category assignment of each test instance
    String[] categoryAssignment = new String[testData.length]; 
    
    // the assignment of k nearest neighbors for each test item
    String[][] nearestNeighbors = new String[testData.length][k];
    TreeMap<Double, ArrayList<String[]>> knn; // stores distances from training neighbors for each test item
    
    // when multiple neighbors have the same distance from a test item, use this customized Comparator
    // to decide which neighbor comes first to the k nearest neighbors for the test item
    Comparator<String[]> stringArrayCompare = new Comparator<String[]>() {
      // example of String[] strings: [neighbor's name, neighbor's category]
      public int compare(String[] strings, String[] otherStrings) {
        return strings[0].compareTo(otherStrings[0]);
      }
    };
    
    // for each test item in testData
    for (int i = 0; i < testData.length; i++) {
      knn = new TreeMap<Double, ArrayList<String[]>>(); 
      for (int j = 0; j < trainingData.length; j++) {
        // get the Euclidean distances from its training neighbors and save as keys
        double dist = euclideanD(testData[i].features, trainingData[j].features);
        // store names/categories of training neighbors as values
        // if a distance as key is already existed, add a new String[] to its value
        if (!knn.containsKey(dist)) {
          ArrayList<String[]> value = new ArrayList<String[]>();
          value.add(new String[] {trainingData[j].name, trainingData[j].category});
          knn.put(dist, value);
        } else {
          knn.get(dist).add(new String[] {trainingData[j].name, trainingData[j].category});
        }
      }
      
      // find the k nearest neighbors in trainingData for this test item, and
      // store their value: name, category in knnVote
      ArrayList<String[]> knnVote = new ArrayList<String[]>();
      // k candidates should be in the knnVote 
      while (knnVote.size() < k) {
        // get value from a key-value pair that has the lowest key, which is the lowest distance
        ArrayList<String[]> knnInf = knn.pollFirstEntry().getValue();
        // sort the knnInf: ex. [banana, fruit], [apple, fruit] -> [apple, fruit], [banana, fruit]
        Collections.sort(knnInf, stringArrayCompare);
        while  (knnVote.size() < k && !knnInf.isEmpty()) {
          nearestNeighbors[i][knnVote.size()] = knnInf.get(0)[0];
          knnVote.add(knnInf.get(0));
          knnInf.remove(0);
        }
      }
      
      // get predicted category for this test item
      int[] cateVote = {0, 0, 0};
      for (int j = 0; j < k; j++) {
        if (knnVote.get(j)[1].equals("nation"))
          cateVote[0] ++;
        else if (knnVote.get(j)[1].equals("machine"))
          cateVote[1] ++;
        else if (knnVote.get(j)[1].equals("fruit"))
          cateVote[2] ++;
      }
      
      // when ties occur, break them by using the priority order: nation, machine, fruit
      if (cateVote[0] >= cateVote[1] && cateVote[0] >= cateVote[2])
        categoryAssignment[i] = "nation";
      else if (cateVote[1] > cateVote[0] && cateVote[1] >= cateVote[2])
        categoryAssignment[i] = "machine";
      else if (cateVote[2] > cateVote[0] && cateVote[2] > cateVote[1])
        categoryAssignment[i] = "fruit";
      
      if (categoryAssignment[i].equals(testData[i].category)) correctPredict ++;
      else wrongPredict ++;
    }
    
    // calculate accuracy
    accuracy = correctPredict / (double) (correctPredict + wrongPredict);
    
    KNNResult res = new KNNResult();
    res.nearestNeighbors = nearestNeighbors;
    res.categoryAssignment = categoryAssignment;
    res.accuracy = accuracy;
    return res;
  }
}
