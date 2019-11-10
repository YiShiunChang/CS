/**
 * Class for item (or instance) storage.
 * Called Item instead of Instance to avoid collision with Java nomenclature.
 * !! DO NOT MODIFY !!
 *
 */

public class Item {
	public String category; // the category of the instance
	public String name; // the name of the instance
	public double[] features; // the feature vector of the instance
	
	public Item() {
		features = new double[3]; // the length of a feature vector is 3
	}
	
	public Item(String category, String name, double[] features) {
		this.category = category;
		this.name = name;
		this.features = features;
	}
}
