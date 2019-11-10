/**
 * Filename:   CourseSchedulerUtil.java
 * Project:    p4
 * Course:     cs400 
 * Authors:    Debra Deppeler, Yi-Shiun Chang (004 / Class Number: 46373)
 * Due Date: 
 *
 * Additional credits: None
 * Bugs or other notes: None
 */
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Use this class for implementing Course Planner
 * 
 * @param <T> represents type
 */
public class CourseSchedulerUtil<T> {
  private GraphImpl<T> graphImpl; // a Map that its key = course, value = prerequisite courses
  private List<T> topOrder; // topological order of all the courses
    
  /**
   * Constructor to initialize a graph object
   */
  public CourseSchedulerUtil() {
    // the courses type T should usually be String, not only because of courses are better save
    // in String, but also because of String is derived from JSON file. Check the procedures in
    // createEntity(). However, we should use generic to ensure the use of other types
    this.graphImpl = new GraphImpl<T>();
  }
  
  /**
  * Read the user specified file and parse it to create a list of entities.
  * This createEntity method is for parsing the input json file, which includes information about
  * courses and their prerequisite courses. This method transforms JSONArray/JSONMap into an 
  * array of Entity objects. Each Entity object stores information about a single course including
  * its name and its prerequisites in T type.
  * 
  * @return array of Entity objects
  * @throws Exception like FileNotFound, JsonParseException
  */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Entity[] createEntity(String fileName) throws Exception {
    // parsing fileName into Object, and fileName is the address of a json file
    Object obj = new JSONParser().parse(new FileReader(fileName)); 
  
    // casting Object to JSONObject 
    JSONObject jo = (JSONObject) obj; 
    // getting courses, which is a key in this json like map (dict), and the corresponding value of
    // courses is an array, so we cast it into JSONArray. 
    // If the corresponding value is a map, we cast it like below: 
    // Map coursesInfo = ((Map)jo.get("courses")); 
    // System.out.println(coursesInfo); can print the details of corresponding value
    JSONArray coursesInfo = (JSONArray) jo.get("courses"); 
    
    // create an Entity array to store info of each course, which is an Entity, and return it
    Entity[] coursesEntity = new Entity[coursesInfo.size()];
    // steps for iterating coursesInfo and creating entity for each course:
    // 1. many maps are in this coursesInfo JSONArray, so we are going to iterate coursesInfo
    //    coursesInfo ex. [{"name":"cs300", "prerequisite":"["CS200"]"}, {}, {}, ...]
    Iterator coursesInfoIter = coursesInfo.iterator(); 
    // 2. iterate coursesInfo after we create its iterator
    int index = 0; // this index is used for Entity[] coursesEntity
    while (coursesInfoIter.hasNext() && index < coursesInfo.size()) { 
      // 3. for each course within the coursesInfo iterator, we get it and cast into map,
      //    it is because we know that info of each course is stored in map format.
      //    coursesInfoIter ex. {"name":"CS300", "prerequisite":"["CS200"]"}
      Map courseInfoSet = (Map) coursesInfoIter.next();
      // 4. for each course, create its corresponding Entity
      //    ex. private String Name = CS300, and private String[] prerequisites = ["CS200"]
      Entity<T> course = new Entity<T>();
      course.setName((T) courseInfoSet.get("name"));
      //    get the prerequisites of each course by casting JSONArrayList to ArrayList
      ArrayList prerequisitesArrayList = (ArrayList) courseInfoSet.get("prerequisites");
      // 5. update private T[] prerequisites of Entity<T> course 
      course.setPrerequisites((T[]) prerequisitesArrayList.toArray());
      // 6. update the info of Entity[] coursesEntity
      coursesEntity[index] = course;
      index ++;
    } 
    return coursesEntity;
  }
  
  /**
   * Construct a directed graph from the created Entity[] object, this graph is a GraphImpl object,
   * which is composed of HashMap. Its keys are T type and values are List<T> type.
   * 
   * @param entities which has information about a single course and its prerequisites
   */
  public void constructGraph(Entity<T>[] entities) {
    // traverse Entity[] entities, which stores all courses and their prerequisites
    for (int i = 0; i < entities.length; i++) {
      // save a course in GraphImpl object, which is composed of HashMap, as a key
      T currentCourse = (T) entities[i].getName();
      graphImpl.addVertex(currentCourse);
      
      // get the prerequisites of a course and update GraphImpl object, which is a HashMap
      // each course in T[] preCourses should be added to the List<T> of that given course
      T[] preCourses = entities[i].getPrerequisites();
      for (int j = 0; j < preCourses.length; j++) {
        graphImpl.addVertex((T) preCourses[j]);
        graphImpl.addEdge(currentCourse, (T) preCourses[j]);
      }
    }
    // graphImpl.printGraph();
  }
  
  
  /**
   * Returns all the unique available courses which were obtained from the json file
   * 
   * @return the sorted list of all available courses
   */
  public Set<T> getAllCourses() {
    return graphImpl.getAllVertices();
  }
  
  
  /**
   * To check whether all given courses can be completed or not.
   * The graph of all courses is a topological graph, so if there is a cycle in the graph,
   * all courses can not be completed
   * 
   * @return true if all given courses can be completed, otherwise false
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public boolean canCoursesBeCompleted() throws Exception {
    // create a array to store indegrees of all vertices
    // indegree is an int used to represent how many arrows are directed to a vertex
    Map<T, Integer> indegree = new HashMap<T, Integer>();
    // update Map<T, Integer> indegree, ex. if a course are prerequisites of three other courses
    // then its value = 3, and key = course name
    T[] graphVertices = (T[]) graphImpl.getAllVertices().toArray();
    for (int i = 0; i < graphVertices.length; i++) {
    	// Integer::sum is an abbreviation of (Integer n1, Integer n2) -> n1 + n2
    	// this is valid when inputs n1, n2 is exactly what Integer.sum(n1, n2) takes
      indegree.merge(graphVertices[i], 0, Integer::sum); 
      // check the prerequisites for a given course = graphVertices[i]
      List<T> adjVertices = graphImpl.getAdjacentVerticesOf(graphVertices[i]);
      for (T vertex: adjVertices) {
        // if key doesn't exist set value default 1, otherwise, value +1
        indegree.merge(vertex, 1, Integer::sum); 
      }
    }
    
    // System.out.println(indegree);
    // create a queue and enqueue all vertices with indegree 0, 
    // vertices with indegree 0 are not other vertices' prerequisites (have no predecessor)
    // this q is used for the building the topologicalOrder
    Queue<T> q = new LinkedList<T>(); 
    for (int i = 0; i < graphVertices.length; i++) { 
      if(indegree.get(graphVertices[i]) == 0) 
        q.add(graphVertices[i]); 
    } 
      
    // Initialize count of visited vertices, if count exceeds the number of vertices
    // this topological graph has at least a cycle
    int cnt = 0; 
    // Create a vector to store result (topological ordering of the vertices) 
    topOrder = new Vector<T>(); 
    while (!q.isEmpty()) { 
      // dequeue a vertex and add it to the topological order 
      // the dequeued vertices are not other vertices' prerequisites, or their predecessors 
      // are already dequeued
      T u = q.poll(); 
      topOrder.add(u); 
      
      // iterate through vertex u's adjVertices and decrease their indegree by 1
      // if any vertex's indegree becomes zero, add it to queue 
      List<T> adjVertices = graphImpl.getAdjacentVerticesOf(u);
      for (int i = 0; i < adjVertices.size(); i++) {        
        indegree.put(adjVertices.get(i), indegree.get(adjVertices.get(i))-1);
        if (indegree.get(adjVertices.get(i)) == 0) {
          q.add(adjVertices.get(i));
        }
      }
      cnt++; 
    } 
    // System.out.println(cnt);
    // Check if there was a cycle         
    if(cnt != graphVertices.length) { 
      return false; // there exists a cycle in the graph
    } 
    return true;
  }
  
  
  /**
   * The order of courses in which the courses has to be taken
   * 
   * @return the list of courses in the order it has to be taken
   * @throws Exception when courses can't be completed in any order
   */
  public List<T> getSubjectOrder() throws Exception {
    if (canCoursesBeCompleted()) {
      Collections.reverse(topOrder);
      return topOrder;
    }
    throw new Exception("There is a cycle in graph");
  }

      
  /**
   * The minimum courses required to be taken for a given course
   * 
   * @param courseName 
   * @return the number of minimum courses needed for a given course
   */
  public int getMinimalCourseCompletion(T courseName) throws Exception {
  	
  	// check cycle...
  	Map<T, Integer> indegree = new HashMap<T, Integer>();
  	indegree.merge(courseName, 0, Integer::sum); 
    // update Map<T, Integer> indegree, ex. if a course are prerequisites of three other courses
    // then its value = 3, and key = course name
    T[] graphVertices = (T[]) graphImpl.getAdjacentVerticesOf(courseName).toArray();
    for (int i = 0; i < graphVertices.length; i++) {
    	// Integer::sum means that there is a lambda function called "sum", 
    	// and sum is called by Integer::sum
      indegree.merge(graphVertices[i], 0, Integer::sum); 
      // check the prerequisites for a given course = graphVertices[i]
      List<T> adjVertices = graphImpl.getAdjacentVerticesOf(graphVertices[i]);
      for (T vertex: adjVertices) {
        // if key doesn't exist set value default 1, otherwise, value +1
        indegree.merge(vertex, 1, Integer::sum); 
      }
    }
    
    // System.out.println(indegree);
    // create a queue and enqueue all vertices with indegree 0, 
    // vertices with indegree 0 are not other vertices' prerequisites (have no predecessor)
    // this q is used for the building the topologicalOrder
    Queue<T> q = new LinkedList<T>(); 
    for (int i = 0; i < graphVertices.length; i++) { 
      if(indegree.get(graphVertices[i]) == 0) 
        q.add(graphVertices[i]); 
    } 
      
    // Initialize count of visited vertices, if count exceeds the number of vertices
    // this topological graph has at least a cycle
    int cnt = 0; 
    // Create a vector to store result (topological ordering of the vertices) 
    topOrder = new Vector<T>(); 
    while (!q.isEmpty()) { 
      // dequeue a vertex and add it to the topological order 
      // the dequeued vertices are not other vertices' prerequisites, or their predecessors 
      // are already dequeued
      T u = q.poll(); 
      topOrder.add(u); 
      
      // iterate through vertex u's adjVertices and decrease their indegree by 1
      // if any vertex's indegree becomes zero, add it to queue 
      List<T> adjVertices = graphImpl.getAdjacentVerticesOf(u);
      for (int i = 0; i < adjVertices.size(); i++) {        
        indegree.put(adjVertices.get(i), indegree.get(adjVertices.get(i))-1);
        if (indegree.get(adjVertices.get(i)) == 0) {
          q.add(adjVertices.get(i));
        }
      }
      cnt++; 
    } 
    // System.out.println(cnt);
    // Check if there was a cycle         
    if(cnt != graphVertices.length+1) { 
      return -1; // there exists a cycle in the graph
    } 
    // check cycle...
  	
    q = new LinkedList<T>(); 
    q.add(courseName);
    
    // Create a vector to store result (topological ordering of the vertices) 
    Set<T> minCourses = new HashSet<T>(); 
    while (!q.isEmpty()) { 
      // dequeue a vertex and add it to the topological order 
      // the dequeued vertices are not other vertices' prerequisites, or their predecessors 
      // are already dequeued
      T u = q.poll(); 
      minCourses.add(u); 
      
      // iterate through vertex u's adjVertices and decrease their indegree by 1
      // if any vertex's indegree becomes zero, add it to queue 
      List<T> adjVertices = graphImpl.getAdjacentVerticesOf(u);
      for (int i = 0; i < adjVertices.size(); i++) {
        if (!minCourses.contains(adjVertices.get(i)))
          q.add(adjVertices.get(i));
      }
    } 
    return minCourses.size()-1; 
  }
} 
    
