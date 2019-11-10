import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Neural {
  static double[] res;
  static double[] arguments;
  static ArrayList<Double[]> trainingSet;
  static ArrayList<Double[]> evaluateSet;
  static ArrayList<Double[]> testSet;
  
  /**
   * This method transforms String[] args to double[].
   * Contents of double[] are slightly different for different cases, 
   * usually, double[] contains weights, x1, x2, etc.
   * 
   * @param args
   * @return double[]
   */
  private static double[] argsStringToDouble(String[] args) {
    arguments = new double[args.length-1];
    // w1 = arguments[0], ..., w9 = arguments[8], x1 = arguments[9], x2 = arguments[10]
    for (int i = 1; i < args.length; i++) 
      arguments[i-1] = Double.parseDouble(args[i]);
    return arguments;
  }
  
  /**
   * This method is used to read file and return the data set that we want 
   * 
   * @param filePath
   * @return ArrayList<Double[]>
   * @throws FileNotFoundException
   */
  private static ArrayList<Double[]> readFile(String filePath) throws FileNotFoundException {
    ArrayList<Double[]> res = new ArrayList<Double[]>();
    // get file path and open by scanner
    File file = new File(filePath);
    Scanner inputStream = new Scanner(file);
    // read line by line
    while (inputStream.hasNextLine()) { 
      String[] data = inputStream.nextLine().split(" ");
      Double[] doubleData = new Double[3];
      // parse String into Double and add it into ArrayList
      for(int i = 0; i < data.length; i++) doubleData[i] = Double.parseDouble(data[i]);
      res.add(doubleData);
    }
    
    // after loop, close scanner
    inputStream.close();
    return res;
  }
  
  /**
   * Compute ui = linearFunction(input) and vi = activationFunction(input) for each perceptron.
   * ex. perceptron A:
   *     1. takes inputs 1, x1, and x2.
   *     2. get uA = linearFunction(1, x1, x2).
   *     3. get vA = activationFunction(uA).
   * At the end, x1 and x2 are inputs of this network, while vC is the output 
   * 
   * @param x1 input1
   * @param x2 input2
   * @return double[] uA, vA, uB, vB, uC, vC
   */
  private static double[] fowardPropagate(double x1, double x2) {
    // uA = w1*1 + w2*x1 + w3*x2, vA = max(u1,0)
    double uA = arguments[0] * 1 + arguments[1] * x1 + arguments[2] * x2;
    double vA = Math.max(uA, 0);
    // uB = w4*1 + w5*x1 + w6*x2, vB = max(u1,0)
    double uB = arguments[3] * 1 + arguments[4] * x1 + arguments[5] * x2;
    double vB = Math.max(uB, 0);
    // uC = w7*1 + w8*vA + w9*vB, vC = 1/(1 + e**(-uC))
    double uC = arguments[6] * 1 + arguments[7] * vA + arguments[8] * vB;
    double vC = 1 / (1 + Math.pow(Math.E, -uC));
    return new double[] {uA, vA, uB, vB, uC, vC};
  }
  
  /**
   * This method is doing back propagation by using chained rules,
   * so all weights can be updated by gradient descent.
   * 
   * @param x1 input1
   * @param x2 input2
   * @return error, parDvC, parDuC, parDvB, parDuB, parDvA, parDuA
   */
  private static double[] backwardPropagate(double y, double vC, double uB, double uA) {
    double error = Math.pow((vC - y), 2) / 2; // E = ((vC - y)**2) / 2
    double parDvC = vC - y; // ∂E/∂vC 
    double parDuC = parDvC * vC * (1 - vC); // ∂E/∂uC 
    
    double parDvB = arguments[8] * parDuC; // ∂E/∂vB = wBC * ∂E/∂uC
    double reluDuB = (uB >= 0)? 1:0; // ∂max(uB,0)/∂uB = 1, if uB ≥ 0. ∂max(uB,0)/∂uB = 0, if uB < 0. 
    double parDuB = parDvB * reluDuB; // ∂E/∂uB = ∂E/∂vB * ∂vB/∂uB = ∂E/∂vB * ( ∂max(uB,0)/∂uB )
    
    double parDvA = arguments[7] * parDuC; // ∂E/∂vA = wAC * ∂E/∂uC
    double reluDuA = (uA >= 0)? 1:0; // ∂max(uA,0)/∂uA = 1, if uA ≥ 0. ∂max(uA,0)/∂uA = 0, if uA < 0. 
    double parDuA = parDvA * reluDuA; // ∂E/∂uA = ∂E/∂vA * ∂vA/∂uA = ∂E/∂vA * ( ∂max(uA,0)/∂uA )
    
    return new double[] {error, parDvC, parDuC, parDvB, parDuB, parDvA, parDuA};
  }
  
  /**
   * This method returns partial derivative of Error(or cost function) on each weight
   * 
   * @return dw1, dw2, dw3, dw4, dw5, dw6, dw7, dw8, dw9
   */
  private static double[] derivativeOnEachWeight(double vB, double vA, double parDuC, 
      double x1, double x2, double parDuB, double parDuA) {
    double dw9 = vB * parDuC; // ∂E/∂wBC = vB * ∂E/∂uC
    double dw8 = vA * parDuC; // ∂E/∂wAC = vA * ∂E/∂uC
    double dw7 = 1 * parDuC; // ∂E/∂w7 = 1 * ∂E/∂uC
    double dw6 = x2 * parDuB; // ∂E/∂w6 = x2 * ∂E/∂uB
    double dw5 = x1 * parDuB; // ∂E/∂w5 = x1 * ∂E/∂uB
    double dw4 = 1 * parDuB; // ∂E/∂w4 = 1 * ∂E/∂uB
    double dw3 = x2 * parDuA; // ∂E/∂w3 = x2 * ∂E/∂uA
    double dw2 = x1 * parDuA; // ∂E/∂w2 = x1 * ∂E/∂uA
    double dw1 = 1 * parDuA; // ∂E/∂w1 = 1 * ∂E/∂uA
    return new double[] {dw1, dw2, dw3, dw4, dw5, dw6, dw7, dw8, dw9};
  }

  /**
   * This method will execute one epoch, which means training this network by trainingSet once 
   */
  private static void oneEpoch() {
    for (int i = 0; i < trainingSet.size(); i++) {
      double x1 = trainingSet.get(i)[0];
      double x2 = trainingSet.get(i)[1];
      double y = trainingSet.get(i)[2];
      // (a) x1, x2, y
      System.out.printf("%.5f %.5f %.5f\n", x1, x2, y);
      
      // forward propagating
      double[] forwardProRes = fowardPropagate(x1, x2);
      double uA = forwardProRes[0];
      double vA = forwardProRes[1];
      double uB = forwardProRes[2];
      double vB = forwardProRes[3];
      double uC = forwardProRes[4];
      double vC = forwardProRes[5];
      
      // backward propagating 
      // returns error, parDvC, parDuC, parDvB, parDuB, parDvA, parDuA
      double[] backwardProRes = backwardPropagate(y, vC, uB, uA);
      double parDuC = backwardProRes[2]; // ∂E/∂uC 
      double parDuB = backwardProRes[4]; // ∂E/∂uB = ∂E/∂vB * ∂vB/∂uB = ∂E/∂vB * ( ∂max(uB,0)/∂uB )
      double parDuA = backwardProRes[6]; // ∂E/∂uA = ∂E/∂vA * ∂vA/∂uA = ∂E/∂vA * ( ∂max(uA,0)/∂uA )
      
      // derivative of Error on each weight
      // returns dw1, dw2, dw3, dw4, dw5, dw6, dw7, dw8, dw9
      double[] dW = derivativeOnEachWeight(vB, vA, parDuC, x1, x2, parDuB, parDuA);
      
      // update w1...w9: wi+1 = wi − η ∂E/∂wi (gradient descent)
      for (int j = 0; j < 9; j++) arguments[j] = arguments[j] - arguments[9] * dW[j];
      // (b) theupdatedw1...w9
      for (int j = 0; j < 9; j++) System.out.printf("%.5f ", arguments[j]);
     
      // evaluation set error
      double evaError = 0;
      for (Double[] evaExample: evaluateSet) 
        evaError += Math.pow(fowardPropagate(evaExample[0], evaExample[1])[5] - evaExample[2], 2) / 2;
      // (c) the evaluation set error under the updated w
      System.out.printf("\n%.5f\n", evaError);
    }
  }
  
  /**
   * This method will execute T epoch, which means training this network by trainingSet T times
   * @param T
   */
  private static void tEpoch(double T) {
    for (int t = 0; t < T; t++) {
      for (int i = 0; i < trainingSet.size(); i++) {
        double x1 = trainingSet.get(i)[0];
        double x2 = trainingSet.get(i)[1];
        double y = trainingSet.get(i)[2];
        
        // forward propagating
        double[] forwardProRes = fowardPropagate(x1, x2);
        double uA = forwardProRes[0];
        double vA = forwardProRes[1];
        double uB = forwardProRes[2];
        double vB = forwardProRes[3];
        double uC = forwardProRes[4];
        double vC = forwardProRes[5];
        
        // backward propagating 
        // returns error, parDvC, parDuC, parDvB, parDuB, parDvA, parDuA
        double[] backwardProRes = backwardPropagate(y, vC, uB, uA);
        double parDuC = backwardProRes[2]; // ∂E/∂uC 
        double parDuB = backwardProRes[4]; // ∂E/∂uB = ∂E/∂vB * ∂vB/∂uB = ∂E/∂vB * ( ∂max(uB,0)/∂uB )
        double parDuA = backwardProRes[6]; // ∂E/∂uA = ∂E/∂vA * ∂vA/∂uA = ∂E/∂vA * ( ∂max(uA,0)/∂uA )
        
        // derivative of Error on each weight
        // returns dw1, dw2, dw3, dw4, dw5, dw6, dw7, dw8, dw9
        double[] dW = derivativeOnEachWeight(vB, vA, parDuC, x1, x2, parDuB, parDuA);
        
        // update w1...w9: wi+1 = wi − η ∂E/∂wi (gradient descent)
        for (int j = 0; j < 9; j++) arguments[j] = arguments[j] - arguments[9] * dW[j];
      }
      // at the end of each epoch, print w
      for (int i = 0; i < 9; i++) System.out.printf("%.5f ", arguments[i]);
      
      // evaluation set error
      double evaSetError = 0;
      for (Double[] evaExample: evaluateSet) 
        evaSetError += Math.pow(fowardPropagate(evaExample[0], evaExample[1])[5] - evaExample[2], 2) / 2;
      // print the evaluation set error.
      System.out.printf("\n%.5f\n", evaSetError);
    }
  }
  
  /**
   * This method may execute T epoch, but it will stop as soon as evaluation set error 
   * starts to increase after completing a whole epoch. 
   * If evaluation set error decreases or stays the same, it will stop after T epochs.
   * 
   * @param T is the maximum epochs for training this neural network
   */
  private static void simpleNeuralNetwork(double T) {
    int epochRun = 0; // how many epochs that have been ran for this network 
    double evaError = 0; // evaError is computed to check how this network is trained per epoch
    boolean quit = false; // set quit to true when evaError increases 
    
    // train this network until T = 0 or evaError increases 
    while(T > 0 && !quit) {
      // for each epoch, all training samples are used to train weights
      for (int i = 0; i < trainingSet.size(); i++) {
        double x1 = trainingSet.get(i)[0];
        double x2 = trainingSet.get(i)[1];
        double y = trainingSet.get(i)[2];
        
        // forward propagating
        double[] forwardProRes = fowardPropagate(x1, x2);
        double uA = forwardProRes[0];
        double vA = forwardProRes[1];
        double uB = forwardProRes[2];
        double vB = forwardProRes[3];
        double uC = forwardProRes[4];
        double vC = forwardProRes[5];
        
        // backward propagating
        double[] backwardProRes = backwardPropagate(y, vC, uB, uA);
        double parDuC = backwardProRes[2]; // ∂E/∂uC 
        double parDuB = backwardProRes[4]; // ∂E/∂uB = ∂E/∂vB * ∂vB/∂uB = ∂E/∂vB * ( ∂max(uB,0)/∂uB )
        double parDuA = backwardProRes[6]; // ∂E/∂uA = ∂E/∂vA * ∂vA/∂uA = ∂E/∂vA * ( ∂max(uA,0)/∂uA )
        
        // derivative of Error on each weight
        double[] dW = derivativeOnEachWeight(vB, vA, parDuC, x1, x2, parDuB, parDuA);
        
        // update w1...w9: wi+1 = wi − η ∂E/∂wi (gradient descent)
        for (int j = 0; j < 9; j++)
          arguments[j] = arguments[j] - arguments[9] * dW[j];
      }
      
      // check whether to stop do more training
      double oldEvaSetError = evaError;
      evaError = 0;
      // after per training based on trainingSet, use evaluateSet to compute new error
      for (Double[] evaExample: evaluateSet) 
        evaError += Math.pow(fowardPropagate(evaExample[0], evaExample[1])[5] - evaExample[2], 2) / 2;
      // if new error is larger than old one, stop doing more training
      if (oldEvaSetError < evaError && epochRun != 0) quit = true;
      
      // after per epoch(training), epochRun++ and T--
      epochRun ++; T --;
    }
    
    // print results: epochRun, w1, w2, ..., w9, error
    System.out.println(epochRun);
    for (int j = 0; j < 9; j++) 
      System.out.printf("%.5f ", arguments[j]);
    System.out.printf("\n%.5f\n", evaError);
    
    // classification accuracy
    int correctCount = 0;
    int falseCount = 0;
    // use testSet to compute accuracy of this network
    for (Double[] testExample: testSet) {
      if (fowardPropagate(testExample[0], testExample[1])[5] >= 0.5) {
        if (testExample[2] == 1) correctCount++; else falseCount++;
      } else {
        if (testExample[2] == 0) correctCount++; else falseCount++;
      }
    }
    // print accuracy
    System.out.printf("%.5f\n", correctCount / (double) (correctCount + falseCount) );
  }
  
  /**
   * This method handles args in a proper way
   * 
   * @param args
   * @throws FileNotFoundException
   */
  public static void main(String[] args) throws FileNotFoundException {
    int flag = Integer.parseInt(args[0]);
    
    switch (flag) {
     case 100:
       res = new double[6];
       // get w1 = arguments[0], ..., w9 = arguments[8], x1 = arguments[9], x2 = arguments[10]
       arguments = argsStringToDouble(args);
       // get uA, vA, uB, vB, uC, vC
       res = fowardPropagate(arguments[9], arguments[10]);
       // print uA, vA, uB, vB, uC, vC
       System.out.printf("%.5f %.5f %.5f %.5f %.5f %.5f", res[0], res[1], res[2], res[3], res[4], res[5]);
       break;
     case 200:
       res = new double[9];
       // get w1 = arguments[0], ..., x1 = arguments[9], x2 = arguments[10], y = arguments[11]
       arguments = argsStringToDouble(args);
       
       // get uA, vA, uB, vB, uC, vC
       System.arraycopy(fowardPropagate(arguments[9], arguments[10]), 0, res, 0, 6);
       // get E, ∂E/∂vC, and ∂E/∂uC
       System.arraycopy(backwardPropagate(arguments[11], res[5], 0, 0), 0, res, 6, 3);
       // copy array1 (from position1 and total length items) to array2 (starting paste on position2)
       // System.arraycopy(array1, position1, array2, position2, length);
       
       // print E, ∂E/∂vC, and ∂E/∂uC
       System.out.printf("%.5f %.5f %.5f", res[6], res[7], res[8]);
       break;
     case 300:
       res = new double[13];
       // get w1 = arguments[0], ..., x1 = arguments[9], x2 = arguments[10], y = arguments[11]
       arguments = argsStringToDouble(args);
       // get uA, vA, uB, vB, uC, vC
       System.arraycopy(fowardPropagate(arguments[9], arguments[10]), 0, res, 0, 6);
       // get error, parDvC, parDuC, parDvB, parDuB, ∂E/∂vA (parDvA), ∂E/∂uA (parDuA)
       // backwardPropagate(double y, double vC, double uB, double uA)
       System.arraycopy(backwardPropagate(arguments[11], res[5], res[2], res[0]), 0, res, 6, 7);
       
       // print ∂E/∂vA, ∂E/∂uA, ∂E/∂vB, ∂E/∂uB
       System.out.printf("%.5f %.5f %.5f %.5f", res[11], res[12], res[9], res[10]);
       break;
     case 400:
       res = new double[22];
       // get w1 = arguments[0], ..., x1 = arguments[9], x2 = arguments[10], y = arguments[11]
       arguments = argsStringToDouble(args);
       // get uA, vA, uB, vB, uC, vC
       System.arraycopy(fowardPropagate(arguments[9], arguments[10]), 0, res, 0, 6);
       // get error, parDvC, parDuC, parDvB, parDuB, ∂E/∂vA (parDvA), ∂E/∂uA (parDuA)
       System.arraycopy(backwardPropagate(arguments[11], res[5], res[2], res[0]), 0, res, 6, 7);
       // get new weights
       System.arraycopy(derivativeOnEachWeight(res[3], res[1], res[8], arguments[9], arguments[10], res[10], res[12]), 0, res, 13, 9);
       // print new weights
       System.out.printf("%.5f %.5f %.5f %.5f %.5f %.5f %.5f %.5f %.5f", 
           res[13], res[14], res[15], res[16], res[17], res[18], res[19], res[20], res[21]);
       break;
     case 500:
       res = new double[22];
       //(a) the old w1 ...w9
       // get w1 = arguments[0], ..., x1 = arguments[9], x2 = arguments[10], y = arguments[11], η = arguments[12]
       arguments = argsStringToDouble(args);
       for (int i = 0; i < 9; i++)
         System.out.printf("%.5f ", arguments[i]);
       
       //(b) the error E under the old w
       // get uA, vA, uB, vB, uC, vC
       System.arraycopy(fowardPropagate(arguments[9], arguments[10]), 0, res, 0, 6);
       // get error, parDvC, parDuC, parDvB, parDuB, ∂E/∂vA (parDvA), ∂E/∂uA (parDuA)
       System.arraycopy(backwardPropagate(arguments[11], res[5], res[2], res[0]), 0, res, 6, 7);
       System.out.printf("\n%.5f\n", res[6]);
       
       //(c) theupdatedw1...w9
       // get new weights by derivative of Error on each weight, returns dw1, dw2, dw3, dw4, dw5, dw6, dw7, dw8, dw9
       System.arraycopy(derivativeOnEachWeight(res[3], res[1], res[8], arguments[9], arguments[10], res[10], res[12]), 0, res, 13, 9);
       // update w1, w2, w3, ..., w9
       for (int i = 0; i < 9; i++) arguments[i] = arguments[i] - arguments[12] * res[i+13];
       for (int i = 0; i < 9; i++) System.out.printf("%.5f ", arguments[i]);
       
       //(d) the error E after the update
       // get new uA, vA, uB, vB, uC, vC
       double[] uvABC = fowardPropagate(arguments[9], arguments[10]);
       // get new error, parDvC, parDuC, parDvB, parDuB, ∂E/∂vA (parDvA), ∂E/∂uA (parDuA)
       System.out.printf("\n%.5f ", backwardPropagate(arguments[11], uvABC[5], uvABC[2], uvABC[0])[0]);
       break;
     case 600:
       trainingSet = readFile("hw2_midterm_A_train.txt");
       evaluateSet = readFile("hw2_midterm_A_eval.txt");
       // w1 = arguments[0], w2 = arguments[1], ..., w9 = arguments[8], η = arguments[9]
       arguments = argsStringToDouble(args);
       // do one training process for trainingSet 
       oneEpoch();
       break;
     case 700:
       trainingSet = readFile("hw2_midterm_A_train.txt");
       evaluateSet = readFile("hw2_midterm_A_eval.txt");
       // w1 = arguments[0], w2 = arguments[1], ..., w9 = arguments[8], η = arguments[9], T = arguments[10]
       arguments = argsStringToDouble(args);
       // do T training processes for trainingSet 
       tEpoch(arguments[10]);
       break;
     case 800:
       // read training, evaluate, and test sets
       trainingSet = readFile("hw2_midterm_A_train.txt");
       evaluateSet = readFile("hw2_midterm_A_eval.txt");
       testSet = readFile("hw2_midterm_A_test.txt");
       // get w1 = arguments[0], ..., w9 = arguments[8], η = arguments[9], T = arguments[10]
       arguments = argsStringToDouble(args);
       // train a simple neural network, T is the maximum epochs for training this neural network
       simpleNeuralNetwork(arguments[10]);
       break;
     default:
       break;
    }
  }
}

