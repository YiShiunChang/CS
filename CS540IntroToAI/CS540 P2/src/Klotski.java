import java.util.*;

public class Klotski {  
  
  /**
   * This method prints out all the children-states of the initial state
   * @param s initial state of the game
   * @param flag is an integer that specifies the behavior and output of the program
   */
  public static void printNextStates(GameState s, int flag) {
      List<GameState> states = s.getNextStates(flag);
      for (GameState state: states) {
          state.printBoard();
          System.out.println();
      }
  }
  
  /**
   * Main function that transforms String initial state into GameState s, and do aStarSearch
   * @param args String input of flag and initial state 
   */
  public static void main(String[] args) {
      if (args == null || args.length < 21) {
          return;
      }
      
      int flag = Integer.parseInt(args[0]);
      int[][] board = new int[5][4];
      for (int i = 0; i < 5; i++) {
          for (int j = 0; j < 4; j++) {
              board[i][j] = Integer.parseInt(args[i * 4 + j + 1]);
          }                
      }
      
      GameState s = new GameState(board);

      if (flag == 100) {
          printNextStates(s, flag);
          return;
      }
      else {
        AStarSearch search = new AStarSearch();        
        search.aStarSearch(flag, s);  
        return;
      } 
  }
}

class AStarSearch{
  Queue<GameState> openSet;
  Set<GameState> closedSet;

  /**
   * Do different aStarSearch for different flags
   * @param flag is an integer that specifies the behavior and output of the program
   * @param state is GameState
   * @return GameState after doing aStarSearch for flag 300, 400, and 500
   */
  public GameState aStarSearch(int flag, GameState state) {   
    openSet = new PriorityQueue<>(stateComparator);
    closedSet = new HashSet<>();
    
    if (flag == 200 || flag == 400)
      state = flag200400(state, flag);
    if (flag == 300 || flag == 500)
      state = flag300500(state, flag);
    
    return state;
  }
  
  /**
   * Heuristic cost = 0 for flag 200
   * Heuristic cost = manhattan distance for flag 400
   * @param state
   * @param flag
   * @return GameState the state that reaches goal 
   */
  private GameState flag200400(GameState state, int flag) {
    int iteration = 1;
    
    //compute hcost and cost for the initial state of flag 400
    if (flag == 400) {
      state.hcost = GameState.manhattanD(state);
      state.cost = GameState.manhattanD(state);
    }
    
    //start loop of aStarSearch
    System.out.println("iteration "+iteration);
    System.out.println(state.getStateID());
    state.printBoard();
    System.out.println(state.cost+" "+state.gcost+" "+state.hcost);
    System.out.println(state.parent);
    
    openSet.add(state); //step1 add initial state to openSet 
    setEmpty(openSet); //step2 check whether openSet is empty
    GameState checkState = openSet.poll(); //step3 get the minimum state from openSet
    
    boolean stopLoop = checkGoal(checkState); //step4 check goal 
    while (!stopLoop) {
      closedSet.add(checkState); //start step5 expand openSet and closedSet
      List<GameState> childStates = checkState.getNextStates(flag);
      updateOpenClosed(childStates); //end step5 expand openSet and closedSet
      
      printOpenList();
      printClosedList();

      iteration ++;
      System.out.println("iteration "+iteration);
      setEmpty(openSet); //step2
      checkState = openSet.poll(); //step3
      stopLoop = checkGoal(checkState); //step4
      System.out.println(checkState.getStateID());
      checkState.printBoard();
      System.out.println(checkState.cost+" "+checkState.gcost+" "+checkState.hcost);
      System.out.println(checkState.parent.getStateID());
    }
    //end loop of aStarSearch
    
    return checkState;
  }
  
  /**
   * Heuristic cost = 0 for flag 300
   * Heuristic cost = manhattan distance for flag 500
   * @param state
   * @param flag
   * @return GameState the state that reaches goal 
   */
  private GameState flag300500(GameState state, int flag) {
    List<GameState> reversedPath = new ArrayList<GameState>();
    int goalCheck = 1;
    int maxOPEN = -1;
    int maxCLOSED = -1;
    int steps = 0;
    
    //compute hcost and cost for the initial state of flag 500
    if (flag == 500) {
      state.hcost = GameState.manhattanD(state);
      state.cost = GameState.manhattanD(state);
    }
    
    //start loop of aStarSearch
    openSet.add(state); //step1 add initial state to openSet 
    setEmpty(openSet); //step2 check whether openSet is empty
    GameState checkState = openSet.poll(); //step3 get the minimum state from openSet
    
    boolean stopLoop = checkGoal(checkState); //step4 check goal 
    while (!stopLoop) {
      closedSet.add(checkState); //start step5 expand openSet and closedSet
      List<GameState> childStates = checkState.getNextStates(flag);
      updateOpenClosed(childStates); //end step5 expand openSet and closedSet
      maxOPEN = Math.max(maxOPEN, openSet.size());
      maxCLOSED = Math.max(maxCLOSED, closedSet.size());
      setEmpty(openSet); //step2
      checkState = openSet.poll(); //step3
      stopLoop = checkGoal(checkState); //step4
      goalCheck ++;
    }
    //end loop of aStarSearch
    
    //find the solution path 
    reversedPath.add(checkState);
    while (checkState.parent != null) {
      checkState = checkState.parent;
      reversedPath.add(checkState);
    }
    
    //print solution path
    while (!reversedPath.isEmpty()) {
      reversedPath.get(reversedPath.size()-1).printBoard();
      System.out.println("");
      reversedPath.remove(reversedPath.size()-1);
      steps ++;
    }
    steps --;
    
    System.out.println("goalCheckTimes " + goalCheck);
    System.out.println("maxOPENSize " + maxOPEN);
    System.out.println("maxCLOSEDSize " + maxCLOSED);
    System.out.println("steps " + steps);
    return checkState;
  }
  
  /**
   * Check whether openSet is empty, if empty, throws IllegalArgumentException()
   * @param openSet
   */
  private void setEmpty(Queue<GameState> openSet) {
    if (openSet.isEmpty()) {
      System.out.println("The openSet has no state");
      throw new IllegalArgumentException();
    }
  }
  
  /**
   * Comparator for the GameState
   * First, compares different states' costs
   * If tied, compares alphabetic orders
   */
  public Comparator<GameState> stateComparator = new Comparator<GameState>() {
    @Override
    public int compare(GameState o1, GameState o2) {
      if (o1.cost - o2.cost != 0)
        return o1.cost - o2.cost;
      else
        return o1.getStateID().compareTo(o2.getStateID());
    }
  };   
  
  /**
   * Comparator for the GameState
   * First, compares different states' gcosts
   * If tied, compares alphabetic orders
   */
  public Comparator<GameState> stateComparatorg = new Comparator<GameState>() {
    @Override
    public int compare(GameState o1, GameState o2) {
      if (o1.gcost - o2.gcost != 0)
        return o1.gcost - o2.gcost;
      else
        return o1.getStateID().compareTo(o2.getStateID());
    }
  }; 

  /**
   * Get the position of blockType1 and check whether it reaches goal
   * @param state
   * @return boolean true if state reaches goal position
   */
  private boolean checkGoal(GameState state) {
    ArrayList<Block> rawBlockArray = state.getPosition(0);
    Map<String, ArrayList<Block>> map = state.buildBlockArray(rawBlockArray);
    ArrayList<Block> blockArray = map.get("blockArray");
    
    for (Block block: blockArray) {
      if (block.blockType == 1 && block.location.get(0) == 13) 
        return true;
    }
    return false;
  }
  
  /**
   * Update openSet or closedSet
   * For every childStates, check whether there is a duplicated one in openSet or Closed
   * If not, update it in openSet
   * Otherwise, check the cost or alphabetic order to decide whether updates state(node) or not 
   * @param childStates
   */
  private void updateOpenClosed(List<GameState> childStates) {
    for (GameState childState: childStates) {
      GameState duplicatedState = duplicatedOpenClosed(openSet, closedSet, childState);
      if (duplicatedState == null) 
        openSet.add(childState);
      else {
        int compResult = stateComparatorg.compare(childState, duplicatedState);
        if (compResult < 0) {
          duplicatedState.cost = childState.cost;
          duplicatedState.gcost = childState.gcost;
          duplicatedState.hcost = childState.hcost;
          duplicatedState.parent = childState.parent;
          if (closedSet.contains(duplicatedState)) {
            closedSet.remove(duplicatedState);
            openSet.add(duplicatedState);
          }
        }
      }
    }
  }
  
  /**
   * Check both openSet and closedSet for a possible duplicated state
   * @param openSet
   * @param closedSet
   * @param currentState
   * @return GameState if there is a duplicated one in openSet or closedSet
   */
  private GameState duplicatedOpenClosed(Queue<GameState> openSet, Set<GameState> closedSet, GameState currentState) {
    boolean duplicatedOpen = false;
    boolean duplicatedClosed = false;
    GameState dupOpenState = null;
    GameState dupClosedState = null;
    
    for (GameState openState: openSet) {
      if (duplicatedState(currentState, openState)) {
        duplicatedOpen = true;
        dupOpenState = openState;
      }
    }
    
    for (GameState closedState: closedSet) {
      if (duplicatedState(currentState, closedState)) {
        duplicatedClosed = true;
        dupClosedState = closedState;
      }
    }
    
    if (duplicatedOpen)
      return dupOpenState;
    if (duplicatedClosed)
      return dupClosedState;
    return null;
  }
  
  /**
   * Check whether two states are the same
   * @param childState
   * @param existedState
   * @return boolean true if two states are the same
   */
  private boolean duplicatedState(GameState childState, GameState existedState) {
    for(int i = 0; i < 5; i++) {
      for(int j = 0; j < 4; j++) {
        if(childState.board[i][j] != existedState.board[i][j]) {
          return false;
        }
      }
    }
    return true;
  } 
  
  /**
   * Print 20 digits ID, board, cost, gcost, hcost, and parent 20 digits ID of
   * all GameStates in openSet
   */
  public void printOpenList() {
    System.out.println("OPEN");
    for (GameState openState: openSet) {
      System.out.println(openState.getStateID());
      openState.printBoard();
      System.out.println(openState.cost+" "+openState.gcost+" "+openState.hcost);
      if (openState.parent == null)
        System.out.println(openState.parent);
      else
        System.out.println(openState.parent.getStateID());
    }
  }
  
  /**
   * Print 20 digits ID, board, cost, gcost, hcost, and parent 20 digits ID of
   * all GameStates in closedSet
   */
  public void printClosedList() {
    System.out.println("CLOSED");
    for (GameState closedState: closedSet) {
      System.out.println(closedState.getStateID());
      closedState.printBoard();
      System.out.println(closedState.cost+" "+closedState.gcost+" "+closedState.hcost);
      if (closedState.parent == null)
        System.out.println(closedState.parent);
      else
        System.out.println(closedState.parent.getStateID());
    }
  } 
}

class GameState {      
  public int[][] board = new int[5][4];
  public GameState parent = null;
  public int gcost = 0;
  public int hcost = 0;
  public int cost = 0;

  public GameState(int [][] inputBoard) {
      for(int i = 0; i < 5; i++)
          for(int j = 0; j < 4; j++)
              this.board[i][j] = inputBoard[i][j];
  }
  
  /**
   * Build child states, sort them, and return
   * @param flag
   * @return List<GameState> consists of childStates' GameStates
   */
  public List<GameState> getNextStates(int flag) {
      ArrayList<Block> rawBlockArray;
      Map<String, ArrayList<Block>> map;
      ArrayList<Block> blockArray;
      ArrayList<Block> emptyBlockArray;
      ArrayList<String> stringSuccessors = new ArrayList<>();
      List<GameState> successors = new ArrayList<GameState>();
      
      rawBlockArray = getPosition(0); //get each 1*1 square's blockType and position
      map = buildBlockArray(rawBlockArray); //merge squares if necessary and separate them
      blockArray = map.get("blockArray");
      emptyBlockArray = map.get("emptyBlockArray");

      //collect all blocks and make them in a form of String type 20 digits ID
      stringSuccessors = blockArrayToStringSuccessors(blockArray, emptyBlockArray, stringSuccessors);
      Collections.sort(stringSuccessors);
      successors = stringToGameState(successors, stringSuccessors, flag);

      return successors;
  }
  
  /**
   * For a 1*1 square, saves it as a block that consists of blockType and location
   * @param location
   * @return ArrayList<Block> of blocks that consist of blockType and location 
   */
  public ArrayList<Block> getPosition(int location) {
    ArrayList<Block> rawBlockArray = new ArrayList<>();
    Block newBlock;
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 4; j++) {
        newBlock = new Block(this.board[i][j], location);
        location ++;
        rawBlockArray.add(newBlock);
      }
    }
    return rawBlockArray;
  }
  
  /**
   * Build blockArray from rawBlockArray, there are blockType1*1, blockType2*4, blockType3*1...
   * Build emptyBlockArray, there are blockType0*2
   * @param rawBlockArray
   * @return Map<String, ArrayList<Block>> that consists of blockArray and emptyBlockArray
   */
  public Map<String, ArrayList<Block>> buildBlockArray(ArrayList<Block> rawBlockArray){
    Map<String, ArrayList<Block>> map = new HashMap<String, ArrayList<Block>>();
    ArrayList<Block> blockArray = new ArrayList<>();
    ArrayList<Block> emptyBlockArray = new ArrayList<>();
    Block newBlock;
    
    for (int i = 0; i < rawBlockArray.size(); i++) {
      switch (rawBlockArray.get(i).blockType) {
      case 0: // blockType0 consists of 1*1 square
        newBlock = new Block(0, rawBlockArray.get(i).location.get(0));
        emptyBlockArray.add(newBlock);
        rawBlockArray.remove(i);
        i--;
        break;
      case 1: // blockType1 consists of 2*2 square
        newBlock = new Block(1, rawBlockArray.get(i).location.get(0));
        newBlock.location.add(rawBlockArray.get(i).location.get(0)+1);
        newBlock.location.add(rawBlockArray.get(i).location.get(0)+4);
        newBlock.location.add(rawBlockArray.get(i).location.get(0)+5);
        blockArray.add(newBlock);
        for (int j = i+1; j < rawBlockArray.size(); j++) {
          if (rawBlockArray.get(j).blockType == 1) {
            rawBlockArray.remove(j);
            j--;
          }
        }
        rawBlockArray.remove(i);
        i--;
        break;
      case 2: // blockType2 consists of 2*1 square
        for (int j = i+1; j < rawBlockArray.size(); j++) {
          if (rawBlockArray.get(j).blockType == 2 && 
              rawBlockArray.get(j).location.get(0).equals(rawBlockArray.get(i).location.get(0)+4)) {
            newBlock = new Block(2, rawBlockArray.get(i).location.get(0));
            newBlock.location.add(rawBlockArray.get(j).location.get(0));
            blockArray.add(newBlock);
            rawBlockArray.remove(j);
            rawBlockArray.remove(i);
            i--;
            break;
          }
        }
        break;
      case 3: // blockType3 consists of 1*2 square
        for (int j = i+1; j < rawBlockArray.size(); j++) {
          if (rawBlockArray.get(j).blockType == 3 && 
              rawBlockArray.get(j).location.get(0).equals(rawBlockArray.get(i).location.get(0)+1)) {
            newBlock = new Block(3, rawBlockArray.get(i).location.get(0));
            newBlock.location.add(rawBlockArray.get(j).location.get(0));
            blockArray.add(newBlock);
            rawBlockArray.remove(j);
            rawBlockArray.remove(i);
            i--;
            break;
          }
        }
        break;
      case 4: // blockType0 consists of 1*1 square
        newBlock = new Block(4, rawBlockArray.get(i).location.get(0));
        blockArray.add(newBlock);
        rawBlockArray.remove(i);
        i--;
        break;
      }
    }
    map.put("blockArray", blockArray);
    map.put("emptyBlockArray", emptyBlockArray);
    return map;
  }
  
  /**
   * Find all childStates of currentState when
   * Transform all blocks' blockTypes and positions into a String that represents 20 digits ID 
   * @param blockArray
   * @param emptyBlockArray
   * @param stringSuccessors
   * @return ArrayList<String> consists of all childStates 20 digits ID
   */
  private ArrayList<String> blockArrayToStringSuccessors(ArrayList<Block> blockArray, 
      ArrayList<Block> emptyBlockArray, ArrayList<String> stringSuccessors) {
    for (Block block: blockArray) {
      String successor; 
      int empty1;
      int empty2;
      
      switch (block.blockType) {
      case 4:
        for (Block empty: emptyBlockArray) {
          successor = getStateID();
          successor = successor.substring(0,block.location.get(0))+'0'+
              successor.substring(block.location.get(0)+1);
          successor = successor.substring(0,empty.location.get(0))+'4'+
              successor.substring(empty.location.get(0)+1);
          stringSuccessors.add(successor);
        }
        break;
      case 3:
        int left = block.location.get(0);
        int right = block.location.get(0)+1;
        empty1 = emptyBlockArray.get(0).location.get(0);
        empty2 = emptyBlockArray.get(1).location.get(0);
        
        if (right+1 == empty1 && right+1 != empty2 && ((right+1)%4) != 0) { //right shift
          successor = getStateID();
          successor = successor.substring(0,left)+'0'+successor.substring(left+1);
          successor = successor.substring(0,empty1)+'3'+successor.substring(empty1+1);
          stringSuccessors.add(successor);
        }
        if (right+1 != empty1 && right+1 == empty2 && ((right+1)%4) != 0) { //right shift
          successor = getStateID();
          successor = successor.substring(0,left)+'0'+successor.substring(left+1);
          successor = successor.substring(0,empty2)+'3'+successor.substring(empty2+1);
          stringSuccessors.add(successor);
        }
        if (left-1 == empty1 && left-1 != empty2 && ((left-1)%4) != 3) { //left shift
          successor = getStateID();
          successor = successor.substring(0,right)+'0'+successor.substring(right+1);
          successor = successor.substring(0,empty1)+'3'+successor.substring(empty1+1);
          stringSuccessors.add(successor);
        }
        if (left-1 != empty1 && left-1 == empty2 && ((left-1)%4) != 3) { //left shift
          successor = getStateID();
          successor = successor.substring(0,right)+'0'+successor.substring(right+1);
          successor = successor.substring(0,empty2)+'3'+successor.substring(empty2+1);
          stringSuccessors.add(successor);
        }
        if ((left-4 == empty1 && right-4 == empty2) || 
            (left-4 == empty2 && right-4 == empty1)) { //up shift
          successor = getStateID();
          successor = successor.substring(0,right)+'0'+successor.substring(right+1);
          successor = successor.substring(0,left)+'0'+successor.substring(left+1);
          successor = successor.substring(0,empty1)+'3'+successor.substring(empty1+1);
          successor = successor.substring(0,empty2)+'3'+successor.substring(empty2+1);
          stringSuccessors.add(successor);
        }
        if ((left+4 == empty1 && right+4 == empty2) || 
            (left+4 == empty2 && right+4 == empty1)) { //down shift
          successor = getStateID();
          successor = successor.substring(0,right)+'0'+successor.substring(right+1);
          successor = successor.substring(0,left)+'0'+successor.substring(left+1);
          successor = successor.substring(0,empty1)+'3'+successor.substring(empty1+1);
          successor = successor.substring(0,empty2)+'3'+successor.substring(empty2+1);
          stringSuccessors.add(successor);
        }
        break;
      case 2:
        int up = block.location.get(0);
        int down = block.location.get(1);
        empty1 = emptyBlockArray.get(0).location.get(0);
        empty2 = emptyBlockArray.get(1).location.get(0);

        if (up-4 == empty1 && up-4 != empty2) { //up shift
          successor = getStateID();
          successor = successor.substring(0,down)+'0'+successor.substring(down+1);
          successor = successor.substring(0,empty1)+'2'+successor.substring(empty1+1);
          stringSuccessors.add(successor);
        }
        if (up-4 != empty1 && up-4 == empty2) { //up shift
          successor = getStateID();
          successor = successor.substring(0,down)+'0'+successor.substring(down+1);
          successor = successor.substring(0,empty2)+'2'+successor.substring(empty2+1);
          stringSuccessors.add(successor);
        }
        if (down+4 == empty1 && down+4 != empty2) { //down shift
          successor = getStateID();
          successor = successor.substring(0,up)+'0'+successor.substring(up+1);
          successor = successor.substring(0,empty1)+'2'+successor.substring(empty1+1);
          stringSuccessors.add(successor);
        }
        if (down+4 != empty1 && down+4 == empty2) { //down shift
          successor = getStateID();
          successor = successor.substring(0,up)+'0'+successor.substring(up+1);
          successor = successor.substring(0,empty2)+'2'+successor.substring(empty2+1);
          stringSuccessors.add(successor);
        }
        if ((up-1 == empty1 && down-1 == empty2 && ((up-1)%4) != 3) || 
            (up-1 == empty2 && down-1 == empty1 && ((up-1)%4) != 3)) { //left shift
          successor = getStateID();
          successor = successor.substring(0,up)+'0'+successor.substring(up+1);
          successor = successor.substring(0,down)+'0'+successor.substring(down+1);
          successor = successor.substring(0,empty1)+'2'+successor.substring(empty1+1);
          successor = successor.substring(0,empty2)+'2'+successor.substring(empty2+1);
          stringSuccessors.add(successor);
        }
        if ((up+1 == empty1 && down+1 == empty2 && ((up+1)%4) != 0) || 
            (up+1 == empty2 && down+1 == empty1) && ((up+1)%4) != 0) { //right shift
          successor = getStateID();
          successor = successor.substring(0,up)+'0'+successor.substring(up+1);
          successor = successor.substring(0,down)+'0'+successor.substring(down+1);
          successor = successor.substring(0,empty1)+'2'+successor.substring(empty1+1);
          successor = successor.substring(0,empty2)+'2'+successor.substring(empty2+1);
          stringSuccessors.add(successor);
        }
        break;
      case 1:
        successor = getStateID();
        int first = block.location.get(0);
        int second = block.location.get(1);
        int third = block.location.get(2);
        int last = block.location.get(3);
        empty1 = emptyBlockArray.get(0).location.get(0);
        empty2 = emptyBlockArray.get(1).location.get(0);
        
        if ((first-4 == empty1 && second-4 == empty2) || (first-4 == empty2 && second-4 == empty1)) { //up shift
          successor = successor.substring(0,third)+'0'+successor.substring(third+1);
          successor = successor.substring(0,last)+'0'+successor.substring(last+1);
          successor = successor.substring(0,empty1)+'1'+successor.substring(empty1+1);
          successor = successor.substring(0,empty2)+'1'+successor.substring(empty2+1);
          stringSuccessors.add(successor);
        }
        else if ((third+4 == empty1 && last+4 == empty2) || (third+4 == empty2 && last+4 == empty1)) { //down shift
          successor = successor.substring(0,first)+'0'+successor.substring(first+1);
          successor = successor.substring(0,second)+'0'+successor.substring(second+1);
          successor = successor.substring(0,empty1)+'1'+successor.substring(empty1+1);
          successor = successor.substring(0,empty2)+'1'+successor.substring(empty2+1);
          stringSuccessors.add(successor);
        }
        else if ((first-1 == empty1 && third-1 == empty2 && (first-1)%4 != 3) || 
            (first-1 == empty2 && third-1 == empty1 && (first-1)%4 != 3)) { //left shift
          successor = successor.substring(0,second)+'0'+successor.substring(second+1);
          successor = successor.substring(0,last)+'0'+successor.substring(last+1);
          successor = successor.substring(0,empty1)+'1'+successor.substring(empty1+1);
          successor = successor.substring(0,empty2)+'1'+successor.substring(empty2+1);
          stringSuccessors.add(successor);
        }
        else if ((second+1 == empty1 && last+1 == empty2 && (second+1)%4 != 1) || 
            (second+1 == empty2 && last+1 == empty1 && (second+1)%4 != 1)) { //right shift
          successor = successor.substring(0,first)+'0'+successor.substring(first+1);
          successor = successor.substring(0,third)+'0'+successor.substring(third+1);
          successor = successor.substring(0,empty1)+'1'+successor.substring(empty1+1);
          successor = successor.substring(0,empty2)+'1'+successor.substring(empty2+1);
          stringSuccessors.add(successor);
        }
        break;
      }
    }
    return stringSuccessors;
  }
  
  /**
   * String stringSuccessor is childStates in String type, changed them to GameState type
   * At the meantime, set each childState's gcost, hcost, cost, parentState based on flag
   * @param successors
   * @param stringSuccessors
   * @param flag
   * @return List<GameState> consists of GameStates that are transformed from stringSuccessor
   */
  private List<GameState> stringToGameState(List<GameState> successors, ArrayList<String> stringSuccessors, int flag) {
    if (flag == 400 || flag == 500) {
      for (String stringSuccessor: stringSuccessors) {
        int[][] successor = new int[5][4];
        for (int i = 0; i < 20; i++) {
          char temp = stringSuccessor.charAt(i);
          successor[i/4][i%4] = Character.getNumericValue(temp);
        }
        GameState successorGameState = new GameState(successor);
        successorGameState.gcost = this.gcost+1;
        successorGameState.hcost = manhattanD(successorGameState);
        successorGameState.cost = this.gcost+1+manhattanD(successorGameState);
        successorGameState.parent = this;
        successors.add(successorGameState);
      }
    }
    else {
      for (String stringSuccessor: stringSuccessors) {
        int[][] successor = new int[5][4];
        for (int i = 0; i < 20; i++) {
          char temp = stringSuccessor.charAt(i);
          successor[i/4][i%4] = Character.getNumericValue(temp);
        }
        GameState successorGameState = new GameState(successor);
        successorGameState.gcost = this.gcost+1;
        successorGameState.hcost = this.hcost;
        successorGameState.cost = this.cost+1;
        successorGameState.parent = this;
        successors.add(successorGameState);
      }
    }
    return successors;
  }
  
  /**
   * Compute manhattanD between blockType1 and goal
   * @param state
   * @return int manhattan distance
   */
  public static int manhattanD(GameState state) {
    ArrayList<Block> rawBlockArray = state.getPosition(0);
    Map<String, ArrayList<Block>> map = state.buildBlockArray(rawBlockArray);
    ArrayList<Block> blockArray = map.get("blockArray");
    
    int cost = 0;
    for (Block block: blockArray) {
      if (block.blockType == 1) {
        int up = block.location.get(0)/4;
        int left = block.location.get(0)%4;
        cost = Math.abs(3-up) + Math.abs(1-left);
      }
    }
   return cost;
  }

  /**
   * Get 20 digits ID from board
   * @return String the 20-digit number as ID
   */
  public String getStateID() {
      String s = "";
      for (int i = 0; i < 5; i++) {
          for (int j = 0; j < 4; j++)
              s += this.board[i][j];
      }
      return s;
  }

  /**
   * Print board
   */
  public void printBoard() {
      for (int i = 0; i < 5; i++) {
          for (int j = 0; j < 4; j++)
              System.out.print(this.board[i][j]);
          System.out.println();
      }
  }   
}

class Block {
  public int blockType = -1;
  public ArrayList<Integer> location = new ArrayList<>();
    
  public Block(int type, Integer location) {
    this.blockType = type; //0, 1, 2, 3, 4
    this.location.add(location); //0, 1, 2, ..., 19 left to right, top to down
  }
}