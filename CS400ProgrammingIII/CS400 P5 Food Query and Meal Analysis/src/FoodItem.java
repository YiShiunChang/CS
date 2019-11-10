import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class represents a food item with all its properties.
 * 
 * @author aka
 */
public class FoodItem {
  private String id; // the id of the food item
  private String name; // the name of the food item
  private HashMap<String, Double> nutrients; // Map of nutrients and the corresponding values
    
  /**
   * Constructor
   * 
   * @param name name of the food item
   * @param id unique id of the food item 
   */
  public FoodItem(String id, String name) {
    this.id = id;
    this.name = name;
    this.nutrients = new HashMap<String, Double>();
  }
    
  /**
   * Gets the name of the food item
   * 
   * @return name of the food item
   */
  public String getName() {
      return name;
  }

  /**
   * Gets the unique id of the food item
   * 
   * @return id of the food item
   */
  public String getID() {
    return id;
  }
    
  /**
   * Gets the nutrients of the food item
   * 
   * @return nutrients of the food item
   */
  public HashMap<String, Double> getNutrients() {
    return nutrients;
  }

  /**
   * Adds a nutrient and its value to this food. If nutrient already exists, updates its value.
   */
  public void addNutrient(String name, double value) {
    nutrients.put(name, value);
  }

  /**
   * Returns the value of the given nutrient for this food item. 
   * 
   * @return 0 if the given nutrient not exists
   */
  public double getNutrientValue(String name) {
    if (nutrients.containsKey(name))
      return nutrients.get(name);
    return 0;
  }   
}
