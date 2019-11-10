import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This class represents the backend for managing all the operations associated with FoodItems
 * 
 * @author YiShiun, Chang
 */
public class FoodData implements FoodDataADT<FoodItem> {
  private List<FoodItem> foodItemList; // list of all the food items
  
  // map of nutrients and their corresponding BPTrees: 
  // key = String could be calories, carbs, fat, protein, and fiber
  // value = BPTree, and Double is used to presents the value of a foodItem's nutrient
  // ex. HashMap<Fat, BPTree<100, butter>> can be considered as butter's fat is 100
  private HashMap<String, BPTree<Double, FoodItem>> indexes; 
  
  /**
   * Public constructor
   */
  public FoodData() {
    foodItemList = new ArrayList<FoodItem>();
    indexes = new HashMap<String, BPTree<Double, FoodItem>>();
  }
  
  /**
   * Loads the data in the .csv file with file format:
   * <id1>,<name>,<nutrient1>,<value1>,<nutrient2>,<value2>,...
   * <id2>,<name>,<nutrient1>,<value1>,<nutrient2>,<value2>,...
   * Example:
   * 556540ff5d613c9d5f5935a9,Stewarts_PremiumDarkChocolatewithMintCookieCrunch,calories,280,fat,18,carbohydrate,34,fiber,3,protein,3
   * 
   * @param filePath path of the food item data file (e.g. folder1/subfolder1/.../foodItems.csv) 
   */
  @Override
  public void loadFoodItems(String filePath) {
    File file = new File(filePath);
    try {
      Scanner inputStream = new Scanner(file);
      
      while (inputStream.hasNext()) { 
        String data = inputStream.next(); 
        String[] foodData = data.split(",");
        // create foodItem and update foodItemList
        FoodItem newFood = new FoodItem(foodData[0], foodData[1]);
        for (int i = 2; i <= 10; i += 2) {
          newFood.addNutrient(foodData[i], Double.parseDouble(foodData[i+1]));
        }
        foodItemList.add(newFood);
      }
      // after loop, close scanner
      inputStream.close();
    }catch (FileNotFoundException e){
      e.printStackTrace();
    }
    
    // setup HashMap<String, BPTree<Double, FoodItem>> indexes
    indexes.put("calories", new BPTree<Double, FoodItem>(4));
    indexes.put("fat", new BPTree<Double, FoodItem>(4));
    indexes.put("carbohydrate", new BPTree<Double, FoodItem>(4));
    indexes.put("fiber", new BPTree<Double, FoodItem>(4));
    indexes.put("protein", new BPTree<Double, FoodItem>(4));
    
    // update HashMap<String, BPTree<Double, FoodItem>> indexes
    for (FoodItem item: foodItemList) {
      indexes.get("calories").insert(item.getNutrients().get("calories"), item);
      indexes.get("fat").insert(item.getNutrients().get("fat"), item);
      indexes.get("carbohydrate").insert(item.getNutrients().get("carbohydrate"), item);
      indexes.get("fiber").insert(item.getNutrients().get("fiber"), item);
      indexes.get("protein").insert(item.getNutrients().get("protein"), item);
    } 
  }

  /**
   * Gets all the food items that have name containing the substring.
   * 
   * Example:
   *     All FoodItem
   *         51c38f5d97c3e6d3d972f08a,Similac_FormulaSoyforDiarrheaReadytoFeed,calories,100,fat,0,carbohydrate,0,fiber,0,protein,3
   *         556540ff5d613c9d5f5935a9,Stewarts_PremiumDarkChocolatewithMintCookieCrunch,calories,280,fat,18,carbohydrate,34,fiber,3,protein,3
   *     Substring: soy
   *     Filtered FoodItem
   *         51c38f5d97c3e6d3d972f08a,Similac_FormulaSoyforDiarrheaReadytoFeed,calories,100,fat,0,carbohydrate,0,fiber,0,protein,3
   * 
   * @param substring substring to be searched
   * @return list of filtered food items; if no food item matched, return empty list
   */
  @Override
  public List<FoodItem> filterByName(String substring) {
    List<FoodItem> res = new ArrayList<FoodItem>();
    
    // Pattern.compile(Pattern.quote(substring), Pattern.CASE_INSENSITIVE) lets substring become CASE_INSENSITIVE
    // then matches it to every foods' names. If match, add to res
    for (FoodItem food: foodItemList) {
      if (Pattern.compile(Pattern.quote(substring), Pattern.CASE_INSENSITIVE).matcher(food.getName()).find())
        res.add(food);
    }
    return res;
  }

  /**
   * Gets all the food items that fulfill ALL the provided rules
   * Format of a rule:
   * "<nutrient> <comparator> <value>"
   * 
   * Definition of a rule:
   * A rule is a string which has three parts separated by a space:
   * 1. <nutrient>: Name of one of the 5 nutrients [CASE-INSENSITIVE]
   * 2. <comparator>: One of the following comparison operators: <=, >=, ==
   * 3. <value>: a double value
   * 
   * @param rules list of rules
   * @return list of filtered food items; if no food item matched, return empty list
   */
  @Override
  public List<FoodItem> filterByNutrients(List<String> rules) {
    List<FoodItem> foodList1;
    List<FoodItem> foodList2;
    Set<FoodItem> foodSet1 = new HashSet<FoodItem>();
    Set<FoodItem> foodSet2 = new HashSet<FoodItem>();
    
    // get the first rule and get its corresponding List of foodItems that satisfy the rule 
    // then transform the List into HashSet for doing intersection
    String[] firstRule = rules.get(0).split(" ");
    rules.remove(0);
    foodList1 = indexes.get(firstRule[0]).rangeSearch(Double.parseDouble(firstRule[2]), firstRule[1]);
    for (FoodItem item: foodList1) {
      foodSet1.add(item);
    }
    
    // when there are multiple rules, transform all corresponding Lists into HashSets
    if (!rules.isEmpty()) {
      for (String rule: rules) { 
        String[] ruleArray = rule.split(" ");
        foodList2 = indexes.get(ruleArray[0]).rangeSearch(Double.parseDouble(ruleArray[2]), ruleArray[1]);
        for (FoodItem item: foodList2) 
          foodSet2.add(item);
          
        // doing intersection
        foodSet1.retainAll(foodSet2);
        foodSet2 = new HashSet<FoodItem>();
      }
    }
    
    return new ArrayList<FoodItem>(foodSet1);
  }

  /**
   * Adds a food item to the loaded data.
   * 
   * @param foodItem the food item instance to be added
   */
  @Override
  public void addFoodItem(FoodItem foodItem) {
    foodItemList.add(foodItem);
    indexes.get("calories").insert(foodItem.getNutrients().get("calories"), foodItem);
    indexes.get("fat").insert(foodItem.getNutrients().get("fat"), foodItem);
    indexes.get("carbohydrate").insert(foodItem.getNutrients().get("carbohydrate"), foodItem);
    indexes.get("fiber").insert(foodItem.getNutrients().get("fiber"), foodItem);
    indexes.get("protein").insert(foodItem.getNutrients().get("protein"), foodItem);
  }

  /**
   * Gets the list of all food items.
   * 
   * @return list of FoodItem
   */
  @Override
  public List<FoodItem> getAllFoodItems() {
    return foodItemList;
  }

  /**
   * Save the list of food items in ascending order by name
   * 
   * @param filename name of the file where the data needs to be saved 
   */
  @Override
  public void saveFoodItems(String filename) {
    String delimiter = ",";
    String newLineSeparator = "\n";

    FileWriter fileWriter = null;
   
    try {
        fileWriter = new FileWriter(filename);
        // write a new foodItem object to the CSV file
        for (FoodItem item: foodItemList) {
          fileWriter.append(item.getID());
          fileWriter.append(delimiter);
          fileWriter.append(item.getName());
          fileWriter.append(delimiter);
          fileWriter.append("calories");
          fileWriter.append(String.valueOf(item.getNutrientValue("calories")));
          fileWriter.append(delimiter);
          fileWriter.append("fat");
          fileWriter.append(String.valueOf(item.getNutrientValue("fat")));
          fileWriter.append(delimiter);
          fileWriter.append("carbohydrate");
          fileWriter.append(String.valueOf(item.getNutrientValue("carbohydrate")));
          fileWriter.append(delimiter);
          fileWriter.append("fiber");
          fileWriter.append(String.valueOf(item.getNutrientValue("fiber")));
          fileWriter.append(delimiter);
          fileWriter.append("protein");
          fileWriter.append(String.valueOf(item.getNutrientValue("protein")));
          fileWriter.append(newLineSeparator);
        }
        System.out.println("CSV file was created successfully !!!");
    } catch (Exception e) {
        System.out.println("Error in CsvFileWriter !!!");
        e.printStackTrace();
    } finally {
      try {
        fileWriter.flush();
        fileWriter.close();
      } catch (IOException e) {
        System.out.println("Error while flushing/closing fileWriter !!!");
        e.printStackTrace();
      }
    }
  }
}
