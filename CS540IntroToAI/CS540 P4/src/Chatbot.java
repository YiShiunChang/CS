
import java.util.*;
import java.io.*;

public class Chatbot{
  private static String filename = "./WARC201709_wid.txt";
  
  /**
   * Each number in the WARC201709_wid.txt is a word type
   * WARC201709_wid consists of 4700 types, which form 228548 token
   * a word token is the occurrences of a corresponding word
   * 
   * @return ArrayList<Integer> the corpus
   */
  private static ArrayList<Integer> readCorpus(){
    // ArrayList corpus consists of WARC201709_wid, and presents in a digital way
    ArrayList<Integer> corpus = new ArrayList<Integer>(); 
    try{
      File f = new File(filename);
      Scanner sc = new Scanner(f);
      // when there is a next and it is int, add to corpus
      while (sc.hasNext()) {
        if (sc.hasNextInt()) {
          int i = sc.nextInt();
          corpus.add(i);
        }
        else {
          sc.next();
        }
      }
    }
    catch (FileNotFoundException ex) {
      System.out.println("File Not Found.");
    }
    return corpus;
  }
  
  /**
   * 
   * 
   * @param args
   */
  static public void main(String[] args) {
    ArrayList<Integer> corpus = readCorpus(); // get corpus
    int flag = Integer.valueOf(args[0]); // get question type
    
    // when question type = 100, print count(w = i) and probability(w = i)
    if (flag == 100) {
      // w is the w-th word type
      int w = Integer.valueOf(args[1]);
      int count = 0;
      for (int i = 0; i < corpus.size(); i++) {
        if (corpus.get(i) == w)
          count ++;
      }
      
      System.out.println(count);
      System.out.println(String.format("%.7f",count/(double)corpus.size()));
    }
    // when question type = 200, print out three numbers on three lines:
    // the word type index i that this r = n1/n2 selects, 
    // and the left end of Wordi’s interval, 
    // and the right end of Worfi’s interval.
    else if(flag == 200){
      int n1 = Integer.valueOf(args[1]); // numerator used for selecting word type i  
      int n2 = Integer.valueOf(args[2]); // denominator used for selecting word type i 
      
      // use method type200 to solve flag 200
      ArrayList<Object[]> word_info = type200(corpus);
      
      // traverse all words' probabilities and find the corresponding word to n1 / n2
      for (int i = 0; i < word_info.size(); i++) {
        if (n1 / (double) n2 <= (double) word_info.get(i)[2]) {
          System.out.println((int) word_info.get(i)[0]);
          System.out.println(String.format("%.7f", word_info.get(i)[1]));
          System.out.println(String.format("%.7f", word_info.get(i)[2]));
          break; // when encounter with the first key that has probability >= r, break loop
        }
      }
    }
    // when question type = 300, print out three numbers on three lines:
    // c(h, w), 􏰀c(h, u) for u = all word types, and p(w | h)
    else if(flag == 300){
      int h = Integer.valueOf(args[1]); // h is the index of history word type
      int w = Integer.valueOf(args[2]); // w is the index of the current word type
      int count = 0; // count for w that appears after h
      
      // words_after_h saves all words that appear after h
      ArrayList<Integer> words_after_h = new ArrayList<Integer>();
      for (int i = 0; i < corpus.size()-1; i++) {
        if (corpus.get(i) == h) {
          int nextWord = corpus.get(i+1);
          words_after_h.add(nextWord);
          
          if (nextWord == w) // for the word u after h, if it is w, count +1
            count ++;
        }
      }
      
      //output 
      System.out.println(count);
      System.out.println(words_after_h.size());
      System.out.println(String.format("%.7f",count/(double)words_after_h.size()));
    }
    // when question type = 400, print out three numbers on three lines: 
    // the word type index i that this n1/n2 selects, 
    // and the left end of Wordi’s interval conditioned on h, 
    // and the right end of Wordi’s interval conditioned on h
    else if(flag == 400){
      int n1 = Integer.valueOf(args[1]); // number used to get next word
      int n2 = Integer.valueOf(args[2]); // number used to get next word
      int h = Integer.valueOf(args[3]); // history word
      
      // use method type400 to solve flag 400
      ArrayList<Object[]> word_info = type400(corpus, h);
      
      // traverse all words' probabilities and find the corresponding word to n1 / n2
      for (int i = 0; i < word_info.size(); i++) {
        if (n1/(double) n2 <= (double) word_info.get(i)[2]) {
          System.out.println(word_info.get(i)[0]);
          System.out.println(String.format("%.7f", word_info.get(i)[1]));
          System.out.println(String.format("%.7f", word_info.get(i)[2]));
          break;
        }
      }
    }
    // when question type = 500, print out three numbers on three lines: 
    // c(h1, h2, w), c(h1, h2, u) for u = all word types, and p(w | h1, h2).
    // in the case that p(w | h1, h2) is undefined, the third line should be the text "undefined"
    else if(flag == 500){
      int h1 = Integer.valueOf(args[1]); // first history word 
      int h2 = Integer.valueOf(args[2]); // second history word 
      int w = Integer.valueOf(args[3]); // word that appears just after h1, h2
      int count = 0; // occurrences of w that appears just after h1, h2
      
      // words_after_h1h2 saves all words that appear just after h1, h2
      ArrayList<Integer> words_after_h1h2 = new ArrayList<Integer>();
      for (int i = 0; i < corpus.size()-2; i++) {
        if (corpus.get(i) == h1 && corpus.get(i+1) == h2) {
          int nextWord = corpus.get(i+2);
          words_after_h1h2.add(nextWord);
          
          if (nextWord == w)
            count ++;
        }
      }
   
      // output 
      System.out.println(count);
      System.out.println(words_after_h1h2.size());
      if(words_after_h1h2.size() == 0)
        System.out.println("undefined");
      else
        System.out.println(String.format("%.7f",count/(double)words_after_h1h2.size()));
    }
    // when question type = 600, print out three numbers on three lines: 
    // the word type index w that this n1/n2 selects,
    // and the left end of Wordi’s interval conditioned on h1 and h2, 
    // and the right end of Wordi’s interval conditioned on h1 and h2. 
    // Otherwise, your code should output a single line with text "undefined"
    else if(flag == 600){
      int n1 = Integer.valueOf(args[1]); // numerator used to get w
      int n2 = Integer.valueOf(args[2]); // denominator used to get w
      int h1 = Integer.valueOf(args[3]); // first history word
      int h2 = Integer.valueOf(args[4]); // second history word
      
      // words_after_h1h2 saves all words that appear just after h1, h2
      ArrayList<Integer> words_after_h1h2 = new ArrayList<Integer>();
      for (int i = 0; i < corpus.size()-2; i++) {
        if (corpus.get(i) == h1 && corpus.get(i+1) == h2) {
          words_after_h1h2.add(corpus.get(i+2));
        }
      }
      
      // use method type600 to solve flag 600
      ArrayList<Object[]> word_info =  type600(words_after_h1h2, h1, h2);
      
      // if words_after_h1h2.size() == 0, then there is no occurrence of h1+h2
      if (words_after_h1h2.size() == 0) 
        System.out.println("undefined");
      
      // traverse all words' probabilities and find the corresponding word to n1 / n2
      for (int i = 0; i < word_info.size(); i++) {
        if (n1/(double) n2 <= (double) word_info.get(i)[2]) {
          System.out.println(word_info.get(i)[0]);
          System.out.println(String.format("%.7f", word_info.get(i)[1]));
          System.out.println(String.format("%.7f", word_info.get(i)[2]));
          break;
        }
      }
    }
    // when question type = 700, generate random sentences using n-gram language models
    else if(flag == 700){
      // if seed = −1, do not set the seed , so generate different random sentences
      // otherwise, set the seed to seed, so generate the same sentences when seed is the same
      int seed = Integer.valueOf(args[1]); 
      // t only needs to be 0, 1, or 2, which represents the number of prefix
      int t = Integer.valueOf(args[2]); 
      int h1 = 0, h2 = 0;
      Random rng = new Random();
      if (seed != -1) rng.setSeed(seed); // if seed != -1, set seed = Integer.valueOf(args[1]) 
      
      // flag == 700, number of prefix = 0
      if(t == 0){
        double r = rng.nextDouble(); // generate first word using r
        // use method type200 to solve flag 200 case
        ArrayList<Object[]> word_info = type200(corpus);
        
        // traverse probabilityOfTokens to find a interval that includes r
        for (int i = 0; i < word_info.size(); i++) {
          if (r <= (double) word_info.get(i)[2]) {
            h1 = (int) word_info.get(i)[0];
            break; // when encounter with the first key that has probability >= r, break loop
          }
        }
        
        System.out.println(h1);
        if(h1 == 9 || h1 == 10 || h1 == 12){
            return;
        }

        // generate second word using r
        r = rng.nextDouble();
        // use method type400 to solve flag 400 case
        word_info = type400(corpus, h1);
        // traverse probabilityOfTokens to find a interval that includes r
        for (int i = 0; i < word_info.size(); i++) {
          if (r <= (double) word_info.get(i)[2]) {
            h2 = (int) word_info.get(i)[0];
            break; // when encounter with the first key that has probability >= r, break loop
          }
        }
        System.out.println(h2);   
      }
      else if (t == 1) {
        h1 = Integer.valueOf(args[3]);
        // generate second word using r
        double r = rng.nextDouble();
        // use method type400 to solve flag 400 case
        ArrayList<Object[]> word_info = type400(corpus, h1);
        for (int i = 0; i < word_info.size(); i++) {
          if (r <= (double) word_info.get(i)[2]) {
            h2 = (int) word_info.get(i)[0];
            break;
          }
        } 
        System.out.println(h2);
      }
      else if(t == 2){
        h1 = Integer.valueOf(args[3]);
        h2 = Integer.valueOf(args[4]);
      }
      
      while(h2 != 9 && h2 != 10 && h2 != 12){
        ArrayList<Object[]> word_info;
        double r = rng.nextDouble();
        int w  = 0;
        // generate new word using h1,h2
        // words_after_h1h2 saves all words that appear just after h1, h2
        ArrayList<Integer> words_after_h1h2 = new ArrayList<Integer>();
        for (int i = 0; i < corpus.size()-2; i++) {
          if (corpus.get(i) == h1 && corpus.get(i+1) == h2) {
            words_after_h1h2.add(corpus.get(i+2));
          }
        }
        // use method type600 to solve flag 600 case
        word_info =  type600(words_after_h1h2, h1, h2);
        
        // output
        if (words_after_h1h2.size() == 0) {
          word_info = type400(corpus, h1);
          for (int i = 0; i < word_info.size(); i++) {
            if (r <= (double) word_info.get(i)[2]) {
              w = (int) word_info.get(i)[0];
              break;
            }
          }
        } else {
          for (int i = 0; i < word_info.size(); i++) {
            if (r <= (double) word_info.get(i)[2]) {
              w = (int) word_info.get(i)[0];
              break;
            }
          }
        }
        
        System.out.println(w);
        h1 = h2;
        h2 = w;
      }
    }
  }

  /**
   * For each word in corpus, compute its probability of occurrences
   * Note. this type = 200 part doesn't deal with word types that have 0 word token
   * 
   * @param corpus a bad of word(BOW) from WARC201709_wid.txt
   * @return ArrayList<Object[]>
   */
  private static ArrayList<Object[]> type200(ArrayList<Integer> corpus) {   
    // wordCount is built for saving occurrences of all words 
    int[] wordCount = new int[4700];
    for (int i = 0; i < wordCount.length; i++) {
      wordCount[i] = 0;
    }
    
    // update wordCount by occurrences of all words 
    for (int i = 0; i < corpus.size(); i++) {
      wordCount[corpus.get(i)] += 1;
    }
    
    ArrayList<Object[]> word_info = new ArrayList<Object[]>();
    for (int i = 0; i < wordCount.length; i++) {
      Object[] wordProb = new Object[3];
      wordProb[0] = i; // i is word type
      // create left end of Wordi’s interval conditioned on h, update it later
      wordProb[1] = 0d; 
      // create right end of Wordi’s interval conditioned on h, update it later
      wordProb[2] = wordCount[i] / (double) corpus.size();
      word_info.add(wordProb);
    }
    
    // for words in word_info, update left/right end of Wordi’s interval conditioned on h
    for (int i = 1; i < word_info.size(); i++) {
      word_info.get(i)[1] = word_info.get(i-1)[2];
      word_info.get(i)[2] = (double) word_info.get(i)[1] + (double) word_info.get(i)[2];
    }

    return word_info;
  }
  
  /**
   * For each word that follows h in corpus, compute its probability of occurrences
   * 
   * @param corpus a bad of word(BOW) from WARC201709_wid.txt
   * @param h a history word served as a condition
   * @return ArrayList<Object[]>
   */
  private static ArrayList<Object[]> type400(ArrayList<Integer> corpus, int h) {
    // words_after_h saves all words that appear after h
    ArrayList<Integer> words_after_h = new ArrayList<Integer>();
    for (int i = 0; i < corpus.size()-1; i++) {
      if (corpus.get(i) == h) {
        words_after_h.add(corpus.get(i+1));
      }
    }
    
    // wordcount_after_h is built for saving occurrences of words that appear after h
    ArrayList<Integer> wordcount_after_h = new ArrayList<Integer>();
    for (int i = 0; i < 4700; i++) {
      wordcount_after_h.add(0);
    }
    
    // wordcount_after_h saves occurrences words that appear after h
    for (int i = 0; i < words_after_h.size(); i++) {
      int word = words_after_h.get(i);
      wordcount_after_h.set(word, wordcount_after_h.get(word)+1);
    }
    
    // word_info saves the probability and word type for words that appear after h
    ArrayList<Object[]> word_info = new ArrayList<Object[]>();
    for (int i = 0; i < wordcount_after_h.size(); i++) {
      // only save words that appear after h
      if (wordcount_after_h.get(i) != 0) {
        Object[] wordprob_after_h = new Object[3];
        wordprob_after_h[0] = i; // i is word type
        // create left end of Wordi’s interval conditioned on h, update it later
        wordprob_after_h[1] = 0d; 
        // create right end of Wordi’s interval conditioned on h, update it later
        wordprob_after_h[2] = wordcount_after_h.get(i) / (double) words_after_h.size();
        word_info.add(wordprob_after_h);
      }
    }
    
    // for words in word_info, update left/right end of Wordi’s interval conditioned on h
    for (int i = 1; i < word_info.size(); i++) {
      word_info.get(i)[1] = word_info.get(i-1)[2];
      word_info.get(i)[2] = (double) word_info.get(i)[1] + (double) word_info.get(i)[2];
    }
    
    return word_info;
  }
  
  /**
   * For each word that follows h1 and h2 in corpus, compute its probability of occurrences
   * 
   * @param words_after_h1h2 words that are just after h1, h2
   * @param h1 a history word served as condition1
   * @param h2 a history word served as condition2
   * @return ArrayList<Object[]>
   */
  private static ArrayList<Object[]> type600(ArrayList<Integer> words_after_h1h2, int h1, int h2) {
    // wordcount_after_h1h2 is built for saving occurrences of words that appear after h1, h2
    ArrayList<Integer> wordcount_after_h1h2 = new ArrayList<Integer>();
    for (int i = 0; i < 4700; i++) {
      wordcount_after_h1h2.add(0);
    }
    
    // traverse words_after_h1h2 to count every word type, and update wordcount_after_h1h2
    for (int i = 0; i < words_after_h1h2.size(); i++) {
      int word = words_after_h1h2.get(i);
      wordcount_after_h1h2.set(word, wordcount_after_h1h2.get(word)+1);
    }
    
    // word_info saves the probability and word type for words that appear just after h1, h2
    ArrayList<Object[]> word_info = new ArrayList<Object[]>();
    for (int i = 0; i < wordcount_after_h1h2.size(); i++) {
      if (wordcount_after_h1h2.get(i) != 0) { // only save words that appear after h
        Object[] wordprob_after_h1h2 = new Object[3];
        wordprob_after_h1h2[0] = i; // i is word type
        // create left end of Wordi’s interval conditioned on h, update it later
        wordprob_after_h1h2[1] = 0d;
        // create right end of Wordi’s interval conditioned on h, update it later
        wordprob_after_h1h2[2] = wordcount_after_h1h2.get(i) / (double) words_after_h1h2.size();
        word_info.add(wordprob_after_h1h2); 
      }
    }
    
    // for words in word_info, update left/right end of Wordi’s interval conditioned on h1, h2
    for (int i = 1; i < word_info.size(); i++) {
      word_info.get(i)[1] = word_info.get(i-1)[2];
      word_info.get(i)[2] = (double) word_info.get(i)[1] + (double) word_info.get(i)[2];
    }
    return word_info;
  }
}
