
#include <iostream>
#include <chrono>
#include "badgerThread.h"
#include "sysStructure.h"

namespace littleBadger {
  /**
   * this function decides the way of how a BadgerThread cooperates with the others.
   */
  const void BadgerThread::run() {
    if (cc_alg == TRAD) {
      dleRun();
    } else if (cc_alg == DLE) {
      tradRun();
    }
  };

  /**
   * TODO:
   * how to run traditiondal lock based on lockManager
   */
  const void BadgerThread::tradRun() {
    if (action == READ) {
      readRecord();
    } else if (action == WRITE) {
      writeRecord();
    }
  };

  /**
   * TODO:
   * how to run dle based on lockManager
   */
  const void BadgerThread::dleRun() {
    if (action == READ) {
      readRecord();
    } else if (action == WRITE) {
      writeRecord();
    }
  };

  const void BadgerThread::readRecord() {
    readMap(key);
  };

  const void BadgerThread::writeRecord() {
    writeMap(key, value);
  };

  const void BadgerThread::deleteRecord() {
    deleteMap(key);
  };
}
