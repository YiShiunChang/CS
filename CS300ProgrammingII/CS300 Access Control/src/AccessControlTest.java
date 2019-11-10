
public class AccessControlTest {

  /**
   * test AccessControl with 5 test cases
   * @param args
   */
  public static void main(String[] args) {
    int fails = 0;
    
    if (!testAddUser1()) {
      System.out.println("testAddUser1 [add user without being an adminisrator] failed");
      fails++;
    }
    
    if (!testLogin1()) {
      System.out.println("testLogin1 [bad username] failed");
      fails++;
    }
    
    if (!testLogin2()) {
      System.out.println("testLogin2 [good login] failed");
      fails++;
    }
    
    if (!testLogin3()) {
      System.out.println("testLogin3 [bad username with default password] failed");
      fails++;
    }
    
    if (!testLogin4()) {
      System.out.println("testLogin4 [change password and check isValidLogin without using driver] failed");
      fails++;
    }
    
    if (fails == 0)
      System.out.println("All tests passed!");
  }
  
  /**
   * Create a new AccessControl and do not log in an admin.
   * Verify that addUser(String username) returns false and that the new user is not added.
   * @return boolean test passed
   */
  public static boolean testAddUser1() {
    AccessControl ac = new AccessControl();
    String user = "alexi";
    boolean addUserReport = ac.addUser(user);
    if (addUserReport)
      return false; // addUserReport should be false Make sure user wasn't added anyway
    return !AccessControl.isValidLogin(user, "changeme");
  }
  
  /**
   * This test tries to log in a user that doesn't exist
   * @return boolean test passed
   */
  public static boolean testLogin1() {
    AccessControl ac = new AccessControl();
    String user = "probablyNotInTheSystem1234";
    String pw = "password";
    return !AccessControl.isValidLogin(user, pw); 
  }
  
  /**
   * test whether good username with correct password is valid login or not
   * @return true if good username with correct password is valid 
   */
  public static boolean testLogin2() {
    AccessControl ac = new AccessControl();
    String user = "admin";
    String pw = "root";
    return AccessControl.isValidLogin(user, pw); 
  }
  
  /**
   * test whether bad username with default password is valid login or not
   * @return true if bad username with default password is not valid login
   */
  public static boolean testLogin3() {
    AccessControl ac = new AccessControl();
    String user = "badusername";
    String pw = "changeme";
    return !AccessControl.isValidLogin(user, pw); 
  }
  
  /**
   * test isValidLogin after changed password without using driver
   * @return true if isValidLogin is passed
   */
  public static boolean testLogin4() {
    AccessControl ac = new AccessControl();
    ac.setCurrentUser("admin");
    ac.changePassword("trytry");
    ac.logout();
    return AccessControl.isValidLogin("admin", "trytry"); 
  }
}
