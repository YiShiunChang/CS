import java.util.Arrays;
import java.util.Random;

public class Ice {
  // icedDays for each year, from 1855 to 2017 (x = year, y = iced days) 
  static int[] icedDays = {118, 151, 121, 96, 110, 117, 132, 104, 125, 118, 125, 123, 110, 127, 131, 99,
      126, 144, 136, 126, 91, 130, 62, 112, 99, 161, 78, 124, 119, 124, 128, 131, 113, 88, 75, 
      111, 97, 112, 101, 101, 91, 110, 100, 130, 111, 107, 105, 89, 126, 108, 97, 94, 83, 106, 
      98, 101, 108, 99, 88, 115, 102, 116, 115, 82, 110, 81, 96, 125, 104, 105, 124, 103, 106, 
      96, 107, 98, 65, 115, 91, 94, 101, 121, 105, 97, 105, 96, 82, 116, 114, 92, 98, 101, 104, 
      96, 109, 122, 114, 81, 85, 92, 114, 111, 95, 126, 105, 108, 117, 112, 113, 120, 65, 98, 91,
      108, 113, 110, 105, 97, 105, 107, 88, 115, 123, 118, 99, 93, 96, 54, 111, 85, 107, 89, 87,
      97, 93, 88, 99, 108, 94, 74, 119, 102, 47, 82, 53, 115, 21, 89, 80, 101, 95, 66, 106, 97,
      87, 109, 57, 87, 117, 91, 62, 65, 94};
  
  //originally, x = year, but will be normalized later in order to avoid scale issue
  static int[] xYear = new int[icedDays.length];
  static double[] xNormal = new double[icedDays.length];
  
  static double sampleMean; // samples' mean (icedDays)
  static double sampleDeviation; // samples' standard deviation (icedDays)
  static double eta; // η = eta used in flag = 500 and 800
  static int t; // total iteration used in flag = 500 and 800
  
  /**
   *  Get the statistics: sampleMean (icedDays)
   */
  private static double getSampleMean() {
    sampleMean = Arrays.stream(icedDays).sum() / (double) icedDays.length;
    return sampleMean;
  }
  
  /**
   * Get the statistics: sampleDeviation (icedDays)
   */
  private static double getSampleDeviation() {
    double squareSum = 0;
    // sum of square
    for (int i = 0; i < icedDays.length; i++) 
      squareSum += (icedDays[i] - sampleMean) * (icedDays[i] - sampleMean);
    // get square root
    sampleDeviation = Math.sqrt(squareSum / (icedDays.length - 1) );
    return sampleDeviation;
  }
  
  /**
   * Set up original x = year
   */
  private static void setOriginalX() {
    for (int i = 1855; i < 2018; i++)
      xYear[i - 1855] = i; 
  }
  
  /**
   * Get the MSE for original x
   */
  private static double getOriginalMSE(int[] x, double beta0, double beta1) {
    double mse = 0; // mean squared error
    for (int i = 0; i < x.length; i++) 
    	mse += Math.pow((beta0 + beta1 * x[i] - icedDays[i]), 2) / icedDays.length;
      
    return mse;
  }
  
  /**
   * Get the MSE for normalized x
   */
  private static double getNormalizeMSE(double[] x, double beta0, double beta1) {
    double mse = 0; // mean squared error
    for (int i = 0; i < x.length; i++) 
      mse += Math.pow((beta0 + beta1 * x[i] - icedDays[i]), 2) / icedDays.length;
    return mse;
  }
  
  /**
   * Get corresponding gradient of beta0 for original x
   */
  private static double gradientDB0(double beta0, double beta1) {
    double gradientDB0 = 0;
    for (int i = 0; i < 163; i++)
      gradientDB0 += (beta0 + beta1 * (i + 1855) - icedDays[i]) / icedDays.length * 2;
    return gradientDB0;
  }
  
  /**
   * Get corresponding gradient of beta1 for original x
   */
  private static double gradientDB1(double beta0, double beta1) {
    double gradientDB1 = 0;
    for (int i = 0; i < 163; i++)
      gradientDB1 += ((beta0 + beta1 * (i + 1855) - icedDays[i]) * (i + 1855)) / icedDays.length * 2;
    return gradientDB1;
  }
  
  /**
   * Get corresponding gradient of beta0 for normalized x
   */
  private static double gradientDNB0(double[] x, double beta0, double beta1) {
    double gradientDB0 = 0;
    for (int i = 0; i < x.length; i++)
      gradientDB0 += (beta0 + beta1 * x[i] - icedDays[i]) / icedDays.length * 2;
    return gradientDB0;
  }
  
  /**
   * Get corresponding gradient of beta1 for normalized x
   */
  private static double gradientDNB1(double[] x, double beta0, double beta1) {
    double gradientDB1 = 0;
    for (int i = 0; i < x.length; i++)
      gradientDB1 += ((beta0 + beta1 * x[i] - icedDays[i]) * x[i]) / icedDays.length * 2;
    return gradientDB1;
  }
  
  /**
   * Get corresponding stochastic gradient of beta0 for normalized x
   */
  private static double sGDNB0(double[] x, double beta0, double beta1, Random rand) {
    int i = rand.nextInt(163);
    return (beta0 + beta1 * x[i] - icedDays[i]) * 2;
  }
  
  /**
   * Get corresponding stochastic gradient of beta1 for normalized x
   */
  private static double sGDNB1(double[] x, double beta0, double beta1, Random rand) {
    int i = rand.nextInt(163);
    return ((beta0 + beta1 * x[i] - icedDays[i]) * x[i]) * 2;
  }

  /**
   * Get beta0 and beta1 by closed-form based on original x
   */
  private static double[] closedFormBetas() {
    getSampleMean();
    double xMean = (2017 + 1855) / 2;
    double numerator = 0;
    double denominator = 0;
    for (int i = 0; i < icedDays.length; i++) {
      numerator += (i + 1855 - xMean) * (icedDays[i] - sampleMean); 
      denominator += (i + 1855 - xMean) * (i + 1855 - xMean);
    }
    return new double[] {sampleMean - (numerator / denominator) * xMean, (numerator / denominator)};
  }
  
  /**
   * Normalize the original x to handle scale issue
   */
  private static double[] normalizeX() {
    double[] newX = new double[163];
    double xMean = (2017 + 1855) / 2;
    double xStd = 0;
    for (int i = 1855; i < 2018; i++)
      xStd += (i - xMean) * (i - xMean);
    xStd = Math.sqrt(xStd / 162);
    
    for (int i = 1855; i < 2018; i++)
      newX[i - 1855] = (i - xMean) / xStd;
    
    return newX;
  }
  
  /**
   * Execute the assignment
   */
  public static void main(String[] args) {
    int flag = Integer.parseInt(args[0]); // read training data
    double beta0;
    double beta1;
    
    switch (flag) {
      // flag = 100, print out the data set: year, icedDays
      case 100:
        setOriginalX();
        for (int i = 0; i < icedDays.length; i++) 
          System.out.println(xYear[i] + " " + icedDays[i]);
        break;
        
      // flag = 200, print the number of data points, the sample mean and the standard deviation
      case 200:
        System.out.printf("%d\n%.2f\n%.2f", icedDays.length, getSampleMean(), getSampleDeviation());
        break;
        
      // flag = 300, arg1 = β0, and arg2 = β1: print the corresponding MSE
      case 300:
        beta0 = Double.parseDouble(args[1]); // read training data
        beta1 = Double.parseDouble(args[2]); // read training data
        setOriginalX();
        System.out.printf("%.2f", getOriginalMSE(xYear, beta0, beta1));
        break;
        
      // flag = 400, arg1 = β0, and arg2 = β1: print the corresponding gradients
      case 400:
        beta0 = Double.parseDouble(args[1]); // read training data
        beta1 = Double.parseDouble(args[2]); // read training data
        System.out.printf("%.2f\n%.2f", gradientDB0(beta0, beta1), gradientDB1(beta0, beta1));
        break;
        
      // flag = 500, arg1 = η, and arg2 = T: start from initial parameter (β0, β1) = (0,0) 
      // print i, β0(i), β1(i), MSE(β0(i),β1(i)) for each iteration (total t iterations)
      case 500:
        eta = Double.parseDouble(args[1]); // η = eta
        t = Integer.parseInt(args[2]); // total iteration
        beta0 = 0; // initial parameter (β0, β1) = (0,0) 
        beta1 = 0; // initial parameter (β0, β1) = (0,0) 
        setOriginalX();
        for (int i = 1; i <= t; i++) {
          double gD0 = gradientDB0(beta0, beta1);
          double gD1 = gradientDB1(beta0, beta1);
          beta0 = beta0 - eta * gD0;
          beta1 = beta1 - eta * gD1;
          System.out.printf("%d %.2f %.2f %.2f\n", i, beta0, beta1, getOriginalMSE(xYear, beta0, beta1));
        }
        break;
        
      // flag = 600, print β-head0 and β-head1 (note the order), and the corresponding MSE
      // β-head0 and β-head1 are derived from closed-form 
      case 600:
        double[] betas = closedFormBetas();
        setOriginalX();
        System.out.printf("%.2f %.2f %.2f", betas[0], betas[1], getOriginalMSE(xYear, betas[0], betas[1]));
        break;
        
      // flag = 700, arg1 = year: print the predicted icedDays for that year
      case 700:
        int year = Integer.parseInt(args[1]);
        System.out.printf("%.2f", closedFormBetas()[0] + closedFormBetas()[1] * year);
        break;
        
      // flag = 800: first normalize the original x, then proceed exactly as when flag = 500
      case 800:
        xNormal = normalizeX();
        eta = Double.parseDouble(args[1]); // η = eta
        t = Integer.parseInt(args[2]); // total iteration
        beta0 = 0; // initial parameter (β0, β1) = (0,0) 
        beta1 = 0; // initial parameter (β0, β1) = (0,0) 
        for (int i = 1; i <= t; i++) {
          double gD0 = gradientDNB0(xNormal, beta0, beta1);
          double gD1 = gradientDNB1(xNormal, beta0, beta1);
          beta0 = beta0 - eta * gD0;
          beta1 = beta1 - eta * gD1;
          System.out.printf("%d %.2f %.2f %.2f\n", i, beta0, beta1, getNormalizeMSE(xNormal, beta0, beta1));
        }
        break;
      
      // implement Stochastic Gradient Descent (SGD) with everything the same as part 8,
      // except the definition of gradient in equations, which changed as follows: 
      // in iteration i we randomly pick one of the n items, which is j. 
      // Say we picked (xji , yji ), then we approximate the gradient using that item only.
      case 900:
        Random rand = new Random();
        xNormal = normalizeX();
        eta = Double.parseDouble(args[1]); // η = eta
        t = Integer.parseInt(args[2]); // total iteration
        beta0 = 0; // initial parameter (β0, β1) = (0,0) 
        beta1 = 0; // initial parameter (β0, β1) = (0,0) 
        for (int i = 1; i <= t; i++) {
          double gD0 = sGDNB0(xNormal, beta0, beta1, rand);
          double gD1 = sGDNB1(xNormal, beta0, beta1, rand);
          beta0 = beta0 - eta * gD0;
          beta1 = beta1 - eta * gD1;
          System.out.printf("%d %.2f %.2f %.2f\n", i, beta0, beta1, getNormalizeMSE(xNormal, beta0, beta1));
        }
        break;
      default:
        System.out.println("Please enter 3 valid arguments: <FLAG> <arg1> <arg2>");
        return;
    }
  }
}
