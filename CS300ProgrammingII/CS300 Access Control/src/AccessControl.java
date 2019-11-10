import java.util.ArrayList;
import java.util.Scanner;

public class AccessControl {
  private static ArrayList<User> users; // An ArrayList of valid users.
  private User currentUser;             // Who is currently logged in, if anyone?
  private static final String DEFAULT_PASSWORD = "changeme"; // Default password given to new users or when we reset a user's password.
  
  public AccessControl() {
    users = new ArrayList<User>();
    users.add(new User("admin", "root", true));
    currentUser = null;
  }
  
  /**
   * isValidLogin method should return true 
   * if the username/password pair matches any user in your users ArrayList and false otherwise. 
   * @param username
   * @param password
   * @return true if username is in the users, and password is matched 
   */
  public static boolean isValidLogin(String username, String password) {
    for (User person: users) {
      if (person.getUsername().equals(username) && person.isValidLogin(password)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Change the current user's password when currentUser is exist
   * @param newPassword
   */
  public void changePassword(String newPassword) {
    if (this.currentUser == null) 
      System.out.println("Fuckoff, don't hack this system.");
    else {
      this.currentUser.setPassword(newPassword);
      System.out.println("Change password successfully.");
    }
  } 
  
  /**
   * Set the currentUser null
   */
  public void logout() {
    currentUser = null;
  } 
  
  /**
   * A mutator you can use to write tests by setting currentUser,
   * and without simulating user input/using driver
   * @param username
   */
  public void setCurrentUser(String username) {
    for (User person: users) {
      if (person.getUsername().equals(username)) {
        this.currentUser = person;
      }
    }
  }
  
  /**
   * Create a new user With the default password and isAdmin==false
   * @param username
   * @return true if an user is successfully added
   */
  public boolean addUser(String username) {
    if (this.currentUser == null) 
      return false;
    else {
      if (this.currentUser.getIsAdmin()) {
        for (User person: users) {
          if (person.getUsername().equals(username)) {
            System.out.println("This user is already in list.");
            return false;
          }
        }
        User newUser = new User(username, DEFAULT_PASSWORD, false);
        users.add(newUser);
        System.out.println("User is successfully added.");
        return true;
      }
      System.out.println("You don't have administrative access.");
      return false;
    }
  } 
  
  /**
   * Create a new user with his/her specific admin status
   * @param username
   * @param isAdmin
   * @return true if an user is successfully added
   */
  public boolean addUser(String username, boolean isAdmin) {
    if (this.currentUser == null) 
      return false;
    else {
      if (this.currentUser.getIsAdmin()) {
        for (User person: users) {
          if (person.getUsername().equals(username)) {
            System.out.println("This user is already in list.");
            return false;
          }
        }
        User newUser = new User(username, DEFAULT_PASSWORD, isAdmin);
        users.add(newUser);
        System.out.println("User is successfully added.");
        return true;
      }
      System.out.println("You don't have administrative access.");
      return false;
    }
  } 
  
  /**
   * Remove a user (names should be unique) 
   * @param username
   * @return true if an user is successfully removed
   */
  public boolean removeUser(String username) {
    if (this.currentUser == null) 
      return false;
    else {
      if (this.currentUser.getIsAdmin()) {
        for (int i=0; i<users.size(); i++) {
          if (users.get(i).getUsername().equals(username)) {
            users.remove(i);
            System.out.println("User is successfully removed.");
            return true;
          }
        }
        System.out.println("User is not in list.");
        return false;
      }
      System.out.println("You don't have administrative access.");
      return false;
    }
  } 
  
  /**
   * Give an user admin power 
   * @param username
   * @return false when the user is not in users, or the currentUser has no admin power 
   */
  public boolean giveAdmin(String username) {
    if (this.currentUser == null) 
      return false;
    else {
      if (currentUser.getIsAdmin()) {
        for (int i=0; i<users.size(); i++) {
          if (users.get(i).getUsername().equals(username)) {
            users.get(i).setIsAdmin(true);
            System.out.println("User is giveAdmin successfully.");
            return true;
          }
        }
        System.out.println("User is not in list.");
        return false;
      }
      System.out.println("You don't have administrative access.");
      return false;
    }
  } 
  
  /**
   * Remove a user's admin power 
   * @param username
   * @return false when the user is not in users, or currentUser has no admin power
   */
  public boolean takeAdmin(String username) {
    if (this.currentUser == null) 
      return false;
    else {
      if (currentUser.getIsAdmin()) {
        for (int i=1; i<users.size(); i++) {
          if (users.get(i).getUsername().equals(username)) {
            users.get(i).setIsAdmin(false);
            System.out.println("User's Admin is took successfully.");
            return true;
          }
        }
        System.out.println("User is not in list.");
        return false;
      }
      System.out.println("You don't have administrative access.");
      return false;
    }
  } 
  
  /**
   * Reset a user's password
   * @param username
   * @return true if an user's password reset successfully
   */
  public boolean resetPassword(String username) {
    if (this.currentUser == null) 
      return false;
    else {
      if (currentUser.getIsAdmin()) {
        for (int i=1; i<users.size(); i++) {
          if (users.get(i).getUsername().equals(username)) {
            users.get(i).setPassword(DEFAULT_PASSWORD);
            System.out.println("User's password reset successfully.");
            return true;
          }
        }
        System.out.println("User is not in list.");
        return false;
      }
      System.out.println("You don't have administrative access.");
      return false;
    }
  }
  
  /**
   * Main driver loop.
   * Prompt the user for login information, and calls the isValidLogin method
   * If the login is valid, call the sessionScreen method
   * @param userInputScanner
   */
  public void loginScreen(Scanner userInputScanner) {
    String username;
    String password;
    
    while (true) {
      System.out.print("\nPLease enter your username: ");
      username = userInputScanner.nextLine();
      System.out.print("PLease enter your password: ");
      password = userInputScanner.nextLine();
      
      if (isValidLogin(username, password)) {
        sessionScreen(username, userInputScanner);
      }
      else 
        System.out.println("\nYour username and password do not match.");
    }
  } 
  
  /**
   * Set the currentUser 
   * Allows them to changePassword or logout, if they are an admin, gives access to admin methods
   * @param username
   * @param userInputScanner
   */
  public void sessionScreen(String username, Scanner userInputScanner) {
    for (User person: users) {
      if (person.getUsername().equals(username)) {
        currentUser = person;
      }        
    }

    boolean loop = true;
    while (loop) {
      String input;
      
      System.out.println("\nHere are 8 items: \n" +
          "logout\n" + 
          "newpw [newpassword]\n" +
          "adduser [username]\n" +
          "adduser [username] [true or false]\n" +
          "rmuser [username]\n" +
          "giveadmin [username]\n" +
          "rmadmin [username]\n" +
          "resetpw [username]\n\n" +
          "Please enter 1 item to do: (Note. [username] indicates that the user inputs a username.)");
      input = userInputScanner.nextLine();
      String[] selectItem = input.split(" ");
      
      switch (selectItem[0]) {
      case "logout":
        logout();
        loop = false;
        break;
      case "newpw": 
        changePassword(selectItem[1]);
        break;
      case "adduser": 
        if (selectItem.length == 3)
          addUser(selectItem[1], Boolean.parseBoolean(selectItem[2]));
        else {
          addUser(selectItem[1]);
        }
        break;
      case "rmuser":
        removeUser(selectItem[1]);
        break;
      case "giveadmin":
        giveAdmin(selectItem[1]);
        break;
      case "rmadmin":
        takeAdmin(selectItem[1]);
        break;
      case "resetpw":
        resetPassword(selectItem[1]);
        break;
      }
    }    
  } 

  /**
   * Launch an AccessControl instance
   */
  public static void main(String[] args) {
    AccessControl ac = new AccessControl();
    // If we have any persistent information to lead, this is where we load it.
    Scanner userIn = new Scanner(System.in);
    ac.loginScreen(userIn);
  }

}
