import java.util.ArrayList;
import java.util.List;

public class FoodDataTest {
  public static void main(String[] args) {
    FoodData myDinner = new FoodData();
    myDinner.loadFoodItems("myDinner.csv");
    
    System.out.println("\nfilter by name:");
    List<FoodItem> yishiun =  myDinner.filterByName("shiun");
    for (FoodItem item: yishiun) {
      System.out.println(item.getName());
    }
    
    System.out.println("\nfilter by nutrients 1:");
    List<String> rules = new ArrayList<String>();
    rules.add("calories >= 300");
    rules.add("carbohydrate >= 35");
    yishiun = myDinner.filterByNutrients(rules);
    for (FoodItem item: yishiun) {
      System.out.println(item.getName());
    }
    
    System.out.println("\nfilter by nutrients 2:");
    rules = new ArrayList<String>();
    rules.add("calories <= 300");
    rules.add("calories >= 280");
    rules.add("fat >= 18");
    yishiun = myDinner.filterByNutrients(rules);
    for (FoodItem item: yishiun) {
      System.out.println(item.getName());
    }
    
    System.out.println("\nadd food item and get all food items");
    FoodItem newItem = new FoodItem("556540ff5d613c9d5f5935f9", "CS400Pizza");
    newItem.addNutrient("calories", 1000);
    newItem.addNutrient("fat", 999);
    newItem.addNutrient("carbohydrate", 5000);
    newItem.addNutrient("fiber", 0);
    newItem.addNutrient("protein", 1314);
    myDinner.addFoodItem(newItem);
    for (FoodItem item: myDinner.getAllFoodItems()) {
      System.out.println(item.getName());
    }
    
    myDinner.saveFoodItems("mylunch");
  }
}
