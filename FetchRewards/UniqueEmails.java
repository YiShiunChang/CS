import java.util.HashSet;
import java.util.Set;

/**
 * Please write a web service that takes in a list of email addresses and returns an integer 
 * indicating the number of unique email addresses. Where "unique" email addresses means they 
 * will be delivered to the same account using Gmail account matching. 
 * 
 * Specifically: 
 * Gmail will ignore the placement of "." in the username, and it will ignore any portion of 
 * the username after a "+".
 * 
 * Assumption:
 * Each emails[i] contains Only one '@' character in each email.
 * '+' won't be the first character in the local name of each email.
 * Local and domain names are not empty.
 */
public class UniqueEmails {
	
	public int getNumUniqueEmails(String[] emails) {
		Set<String> set = new HashSet<>();
    
		for (String email: emails) {
			if (email.length() == 0) {
				continue;
			}
			
			set.add(getUnique(email));
		}
    
		return set.size();
	}
	
	public String getUnique(String email) {
		String[] names = email.split("@");
		StringBuilder localName = new StringBuilder();
		
		for (int i = 0; i < names[0].length(); i++) {
			char c = names[0].charAt(i);
			if (c == '+') {
				break;
			} else if (c == '.') {
				continue;
			}
  
			localName.append(c);
		}

		return localName.toString() + "@" + names[1];
	}
}
