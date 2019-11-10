/**
 * Filename:   Entity.java
 * Project:    p4
 * Authors:    Debra Deppeler
 * 
 * Represents the model of the input json file
 * An Entity object stores information about a single course 
 * including its name and its prerequisites
 */

public class Entity<T> {
	private T name; // courseName 
	private T[] prerequisites; // coursePrerequisites
	
	/** 
	 * Returns the name of the course 
	 */
	public T getName() {
		return name;
	}
	
	/** 
	 * Sets the name of the course 
	 */
	public void setName(T name) {
		this.name = name;
	}
	
	/** 
	 * Sets the PreRequisites for a course 
	 */
	public void setPrerequisites(T[] prerequisites) {
		this.prerequisites = prerequisites;
	}
	
	/** 
   * Returns the PreRequisites for a course 
   */
  public T[] getPrerequisites() {
    return prerequisites;
  }
}
