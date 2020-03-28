
#include <iostream>
#include <fstream>
#include <sstream>
#include "txnManager.h"

namespace littleBadger {
  // a set of transactions needed to be executed
  std::vector<Txn> txnSet;

  /**
   * this method reads txns.txt to build txnSet 
   */
  const void buildTxnSet() {
    std::string line;
    std::ifstream myfile("txns.txt");

    if (myfile.is_open()) {
      // read each line of txns.txt, a line is like: READ key value
      while (getline(myfile, line)) {
        std::string parameters[3];
        int i = 0;
        // stringstream object separates line by space
        std::stringstream ssin(line);
        while (ssin.good() && i < 3) {
          ssin >> parameters[i];
          i++;
        }

        // check whether a transaction is valid
        TxnAction action;
        if (parameters[0].compare("READ") == 0) {
          action = READ;
        } else if (parameters[0].compare("WRITE") == 0) {
          action = WRITE;
        } else {
          std::cout << "transaction has no action" << std::endl;
          continue;
        }

        // stores a transaction in txnSet
        Txn txn;
        txn.set(action, std::stoi(parameters[1], nullptr), parameters[2]);
        txnSet.push_back(txn);
      }

      myfile.close();
    }
  };
}
