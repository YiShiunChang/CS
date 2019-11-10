/**
 * Filename:   GraphImpl.java
 * Project:    p4
 * Course:     cs400 
 * Authors:    Yi-Shiun, Chang (004 / Class Number: 46373)
 * Due Date: 
 *
 * Additional credits: None
 * Bugs or other notes: None
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * T is the type of a vertex, which is a key
 * List<T> is a list of adjacent vertices for that vertex
 *
 * @param <T> type of a vertex
 */
public class GraphImpl<T> implements GraphADT<T> {
  private Map<T, List<T>> verticesMap; // store the vertices and the vertices' adjacent vertices
    
  /**
   * Construct and initialize and empty Graph
   */ 
  public GraphImpl() {
    verticesMap = new HashMap<T, List<T>>();
  }
  
  /**
   * Given a vertex as key, set its values to a new ArrayList<T>
   */
  public void addVertex(T vertex) {
  	if (vertex == null || hasVertex(vertex)) 
  		return;
  	
    verticesMap.put(vertex, new ArrayList<T>());
  }

  /**
   * Remove a key-value pair, which has key = vertex, from verticesMap
   */
  public void removeVertex(T vertex) {
  	if (vertex == null || !hasVertex(vertex))
      return;

	  for (List<T> list : verticesMap.values()) {
	      list.remove(vertex);
	  }
	  
    verticesMap.remove(vertex);
  }

  /**
   * This method actually updates the value of a key-value pair,
   * it calls the value, which is an ArrayList, for a given key and add an new element to it
   */
  public void addEdge(T vertex1, T vertex2) {
  	// check nulls
    if (vertex1 == null || vertex2 == null)
      return;

    // check node exists
    if (!hasVertex(vertex1) || !hasVertex(vertex2))
      return;

    // check edge not already exist
    List<T> v1List = verticesMap.get(vertex1);
    if (v1List.contains(vertex2))
      return;
    
    v1List.add(vertex2);
  }
  
  /**
   * This method actually updates the value of a key-value pair,
   * it calls the value, which is an ArrayList, for a given key and remove an element from it
   */
  public void removeEdge(T vertex1, T vertex2) {
  	// check nulls
    if (vertex1 == null || vertex2 == null)
      return;

    // check node exists
    if (!hasVertex(vertex1) || !hasVertex(vertex2))
      return;

    // check edge not already exist
    List<T> v1List = verticesMap.get(vertex1);
    if (!v1List.contains(vertex2))
      return;

    v1List.remove(vertex2);
  }    
  
  /**
   * Get a set of Keys from this verticesMap
   * 
   * @return Set consists of keys of a Map
   */
  public Set<T> getAllVertices() {
    return verticesMap.keySet();
  }

  /**
   * This method actually get the value of a key-value pair,
   * it gets the value, which is an ArrayList, for a given key
   * 
   * @return List consists of vertices that adjacent to the given vertex
   */
  public List<T> getAdjacentVerticesOf(T vertex) {
    return verticesMap.get(vertex);
  }
  
  /**
   * Check whether a given vertex exists or not
   * 
   * @return true if verticesMap contains the given vertex
   */
  public boolean hasVertex(T vertex) {
    return verticesMap.containsKey(vertex);
  }
  
  /**
   * Returns the number of vertices in this graph
   * 
   * @return number of vertices in graph
   */
  public int order() {
    return verticesMap.keySet().size();
  }
  
  /**
   * Returns the number of edges in this graph
   * 
   * @return number of edges in the graph
   */
  public int size() {
    int size = 0;
    for (T key: verticesMap.keySet()) 
      size += verticesMap.get(key).size();
    return size;
  }
    
    
  /**
   * Prints the graph for the reference
   * DO NOT EDIT THIS FUNCTION
   * DO ENSURE THAT YOUR verticesMap is being used 
   * to represent the vertices and edges of this graph
   */
  public void printGraph() {
    for ( T vertex : verticesMap.keySet() ) {
      if ( verticesMap.get(vertex).size() != 0) {
        for (T edges : verticesMap.get(vertex)) {
            System.out.println(vertex + " -> " + edges + " ");
        }
      } else {
        System.out.println(vertex + " -> " + " " );
      }
    }
  }
}
