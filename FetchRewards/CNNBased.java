import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This is a simple neural network to perform binary classification. Each input item has two real 
 * valued features x = (x1, x2), and the class label y is either 0 or 1. Our neural network has a 
 * very simple structure:
 * 
 * There is one hidden layer with two hidden units A, B, and one output layer with a single output 
 * unit C. The input layer is fully connected to the hidden layer, and the hidden layer is fully 
 * connected to the output layer. Each unit also has a constant bias 1 input with the corresponding
 * weight. Units A and B are ReLU: max(u1,0), and C is Sigmoid: 1 / (1 + Math.pow(Math.E, -uC)).
 * Note. please check figure 
 */
public class CNNBased {
	static double[] res;
  static double[] arguments;
  static ArrayList<Double[]> trainingSet;
  static ArrayList<Double[]> evaluateSet;
  static ArrayList<Double[]> testSet;
  
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
      for(int i = 0; i < data.length; i++) {
      	doubleData[i] = Double.parseDouble(data[i]);
      }
      
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
        for (int j = 0; j < 9; j++) {
        	arguments[j] = arguments[j] - arguments[9] * dW[j];
        }
      }
      
      // check whether to stop do more training
      double oldEvaSetError = evaError;
      evaError = 0;
      // after per training based on trainingSet, use evaluateSet to compute new error
      for (Double[] evaExample: evaluateSet) {
        evaError += Math.pow(fowardPropagate(evaExample[0], evaExample[1])[5] - evaExample[2], 2) / 2;
      }
      // if new error is larger than old one, stop doing more training
      if (oldEvaSetError < evaError && epochRun != 0) {
      	quit = true;
      }
      
      // after per epoch(training), epochRun++ and T--
      epochRun++; 
      T--;
    }
    
    // print results: epochRun, w1, w2, ..., w9, error
    System.out.println("epochRun: " + epochRun);
    for (int j = 0; j < 9; j++) {
    	System.out.printf("w%d: %.5f ", j + 1, arguments[j]);
    }
    System.out.printf("\nEvaluation Error: %.5f\n", evaError);
    
    // classification accuracy
    int correctCount = 0;
    int falseCount = 0;
    // use testSet to compute accuracy of this network
    for (Double[] testExample: testSet) {
      if (fowardPropagate(testExample[0], testExample[1])[5] >= 0.5) {
        if (testExample[2] == 1) {
        	correctCount++; 
        } else {
        	falseCount++;
        }
      } else {
        if (testExample[2] == 0) {
        	correctCount++; 
        } else {
        	falseCount++;
        }
      }
    }
    
    // print accuracy
    System.out.printf("Accuracy: %.5f\n", correctCount / (double) (correctCount + falseCount) );
  }
  
  /**
   * This method handles starts a simple CNN training with a default parameters setting
   * 
   * @param args
   * @throws FileNotFoundException
   */
  public static void main(String[] args) throws FileNotFoundException {
  	// set w1 = 0.1, ..., w9 = 0.9, η = 0.1, T = 10000
  	arguments = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.1, 10000};
  	
    // read training, evaluate, and test sets
    trainingSet = readFile("CNNBased_train.txt");
    evaluateSet = readFile("CNNBased_eval.txt");
    testSet = readFile("CNNBased_test.txt");
     
    // train a simple neural network, T is the maximum epochs for training this neural network
    simpleNeuralNetwork(arguments[10]);
  }
}
