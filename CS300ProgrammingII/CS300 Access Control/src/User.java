
public class User {
  private final String USERNAME; // The user's name
  private String password; // The user's password
  private boolean isAdmin; // Whether or not the user has Admin powers
  
//Creates a new user with the given password and admin status
  public User(String username, String password, boolean isAdmin) {
    USERNAME = username;
    this.password = password;
    this.isAdmin = isAdmin;
  }
  
//Report whether the password is correct
  public boolean isValidLogin(String password) {
    if (this.password.equals(password))
      return true;
    return false;
  } 
  
//Return the user's name
  public String getUsername() {
    return USERNAME;
  } 
  
//Report whether the user is an admin
  public boolean getIsAdmin() {
    return isAdmin;
  } 
  
//Set the new password
  public void setPassword(String password) {
    this.password = password;
  } 
  
//Set the new admin status
  public void setIsAdmin(boolean isAdmin) {
    this.isAdmin = isAdmin;
  } 


}
