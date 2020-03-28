#include <iostream>
#include <chrono>
#include "threadManager.h"
#include "sysStructure.h"

using namespace littleBadger;

int main(int argc, char **argv) {
  // init mapStructure
  initMapStructure();

  // create thread pool with 4 worker threads
  ThreadManager pool(4);

  std::this_thread::sleep_for (std::chrono::seconds (3));
  std::cout << "write action" << std::endl; 

  for (int i = 0; i < 500; i++) {
    CCAlg alg = DLE;
    DLSol sol = KILL;
    TxnAction act = WRITE;
    BadgerThread expThd(alg, sol, act, i, "check");
    pool.enqueueObj(expThd);
  }
  

  // enqueue and store future
  // auto result = pool.enqueue([](int answer) { return answer; }, 42);
  

  std::this_thread::sleep_for (std::chrono::seconds (3));
  std::cout << "read action" << std::endl; 

  for (int i = 0; i < 500; i++) {
    CCAlg alg = DLE;
    DLSol sol = KILL;
    TxnAction act = READ;
    BadgerThread expThd(alg, sol, act, rand() % 1000, "check");
    pool.enqueueObj(expThd);
  }

  // get result from future
  // std::cout << result.get() << std::endl;

  // end threads
  // pool.~ThreadPool();
  std::this_thread::sleep_for (std::chrono::seconds (3));
  std::cout << "finish" << std::endl; 
}
