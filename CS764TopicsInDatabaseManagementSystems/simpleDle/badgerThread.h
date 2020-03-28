#pragma once

#include <map> 
#include <string>
#include "txnManager.h"

namespace littleBadger {
  // concurrency control algroithm: deferredLockEnforecment, traditional
  enum CCAlg {DLE, TRAD};
  // solution for dead lock: waitForever, killImediately
  enum DLSol {WAIT, KILL};

  /**
   * BadgerThread is an object that can be called by a thread of the threadManager. 
   * a BadgerThread is corresponing to a transaction by having infomation of a transaction.
   * a BadgerThread has functions to handle the transaction.
   */
  class BadgerThread {
  public:
    // parameters for a BadgerThread to cooperate with the other BadgerThreads
    CCAlg cc_alg; 
    DLSol dl_sol;

    // parameters for a transaction
    TxnAction action;
    int key;
    std::string value;

    BadgerThread(CCAlg alg, DLSol sol, TxnAction action, int key, std::string value) {
      this->cc_alg = alg;
      this->dl_sol = sol;
      this->action = action;
      this->key = key;
      this->value = value;
    };

    // the basic function for a BadgerThread to exectue a transaction
    const void run();

    // BadgerThread executes a transaction in a traditional lock way
    const void tradRun();

    // BadgerThread executes a transaction in a dle lock way
    const void dleRun();

    // BadgerThread executes a read transaction 
    const void readRecord();

    // BadgerThread executes a write transaction 
    const void writeRecord();

    // BadgerThread executes a delete transaction 
    const void deleteRecord();
  };
}