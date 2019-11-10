/**
 * @file wl.cpp
 * @author  yishiun chang 
 *
 * Description: Implementation of class WL that stores words and the indexes of them in a map
 * Student Name: yi-shiun, chang
 * UW Campus ID: 9080813901
 * email: chang242@wisc.edu
 */

#include "wl.h" 

using namespace std;

WL::WL()
{
}

/**
 * This function load a file and store each words of the file in a map
 * the key value of the map is "word"
 * the key value of the map is "index" where the word occurs 
 * each word is only composed of English alphabet, numbers, and the apostrophe
 * other characters are considered as break of a word, like white space
 * 
 * @param  filename, which could be a .txt 
 * @return void
 */
void WL::load(string filename) {  
   ifstream myfile (filename); // open a file called filename as myfile
   string line;

   if (myfile.is_open()) {  
      // reset the map every time a file is loaded
      locator.clear(); 
      // word is used to build each word of the file
      string word = "";
      // the count-th word of the file
      int count = 1;
      
      // read line by line
      while (getline (myfile, line)) {  
         for (unsigned int i = 0; i < line.length(); i++) {
            // each word is only composed of English alphabet, numbers, and the apostrophe
            if (isalpha(line[i]) || isdigit(line[i]) || line[i] == '\'') {
               word = word + (char) tolower(line[i]);
            } else if (word.length() > 0) {
               // update the map of class WL
               if (locator.count(word) > 0) {
                  locator.find(word)->second.push_back(count);
               } else {
                  locator.insert(pair<string, vector<int> >(word, vector<int>(1, count)));
               }
               word = "";
               count++;
            } 
         }

         // check whether there is a last word in current line
         if (word.length() != 0) {
            if (locator.count(word) > 0) {
                  locator.find(word)->second.push_back(count);
            } else {
               locator.insert(pair<string, vector<int> >(word, vector<int>(1, count)));
            }
            word = "";
            count++;
         }
      }

      myfile.close();
   } else {
      cout << "ERROR: Invalid command\n"; 
   }
}

/**
 * This function finds "the th word" in the map
 * if the th word does't exist, prints "No matching entry"
 * ex. word = "the", thword = "3", then find the third "the" in the map
 * 
 * @param  word that we want to find
 * @param  th, we want to find the th word of the loaded file
 * @return void
 */
void WL::locate(string word, string thword) {
   // when input is a locate, transform each character to lower case for word
   for (int i = 0; i < (int) word.length(); i++) {
      // word has to be valid
      if (!(isalpha(word[i]) || isdigit(word[i]) || word[i] == '\'')) {
         cout << "ERROR: Invalid command\n";
         return;
      }
      word[i] = (char) tolower(word[i]);
   }

   // check whether the third command can be transformed to digit
   int th = 0;
   if (allDigit(thword)) {
      th = stoi(thword);
   } else {
      cout << "ERROR: Invalid command\n";
   } 

   th--;
   if (locator.count(word) > 0) {
      vector<int> index = locator.find(word)->second;
      if ((int) index.size() <= th) {
         cout << "No matching entry\n"; 
      } else {
         cout << index[th] << '\n';
      }
   } else {
      cout << "No matching entry\n";
   }
}

/**
 * This function checks whether command is "load"
 * this function is case insensitive
 * 
 * @param  command
 * @return true if command is "load" no matter what case it is
 */
bool WL::isLoad(string command) {
   for (int i = 0; i < 4; i++) {
      command[i] = tolower(command[i]);
   }

   return command.compare("load") == 0;
}

/**
 * This function checks whether command is "locate" 
 * this function is case insensitive
 * 
 * @param  command
 * @return true if command is "locate" no matter what case it is
 */
bool WL::isLocate(string command) {
   for (int i = 0; i < 6; i++) {
      command[i] = tolower(command[i]);
   }

   return command.compare("locate") == 0;
}

/**
 * This function checks whether command is "new" 
 * this function is case insensitive
 * 
 * @param  command
 * @return true if command is "new" no matter what case it is
 */
bool WL::isNew(string command) {
   for (int i = 0; i < 3; i++) {
      command[i] = tolower(command[i]);
   }

   return command.compare("new") == 0;
}

/**
 * This function checks whether command is "end" 
 * this function is case insensitive
 * 
 * @param  command
 * @return true if command is "end" no matter what case it is
 */
bool WL::isEnd(string command) {
   for (int i = 0; i < 3; i++) {
      command[i] = tolower(command[i]);
   }

   return command.compare("end") == 0;
}

bool WL::allDigit(string command) {
   for (int i = 0; i < (int) command.length(); i++) {
      if (!isdigit(command[i])) {
         return false;
      }
   }

   return true;
}

/**
 * This function is an infinite loop, until user inputs "end"
 * it takes four commands and it is case insensitive
 * 1. load filename
 * 2. locate word th
 * 3. new
 * 4. end
 * Other inputs are invalid
 * 
 * @param
 * @return 0 if main is executed successfully
 */
int main() {  
   WL wl;
   bool endProgram = false;
   // "input" takes whatever user inputs
   string input;
   // "command" stores a segment of an input
   string command;
   // "commands" stores all segments of an input
   vector<string> commands;

   while (!endProgram) {
      // get what user inputs
      commands.clear();
      cout << ">";
      getline(cin, input);
      stringstream tempInput(input);
      while (getline(tempInput, command, ' ')) {
         // when there are many ' ', we skip the command between ' ' and ' ' 
         if ((int) command.length() == 0) {
            continue;
         }
         commands.push_back(command);
      }

      // check whether input is valid as "load", "locate", "new", and "end"
      if (commands.size() == 2 && wl.isLoad(commands[0])) {
         wl.load(commands[1]);
      } else if (commands.size() == 3 && wl.isLocate(commands[0])) {
         wl.locate(commands[1], commands[2]);
      } else if (commands.size() == 1 && wl.isNew(commands[0])) {
         wl.locator.clear();
      } else if (commands.size() == 1 && wl.isEnd(commands[0])) {
         endProgram = true;
         break;
      } else if (commands.size() == 0) {
         continue;
      } else {
         cout << "ERROR: Invalid command\n";
      }
   }

   return 0;
}

