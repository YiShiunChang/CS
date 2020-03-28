#pragma once

#include <string>
#include <vector>

namespace littleBadger {
  // a transaction has action for READ or WRITE
  enum TxnAction {READ, WRITE};

  /**
   * Txn stores basic info of a transaction
   */
  class Txn {
  public:
    TxnAction action;
    int key;
    std::string value;

    void set(TxnAction action, int k, std::string val) {
      this->action = action;
      this->key = k;
      this->value = val;
    }
  };

  // a txnSet has lots of transactions needed to be executed
  const void buildTxnSet();

}