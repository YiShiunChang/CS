/**
 * Original thread pool version: Copyright (c) 2012 Jakob Progsch, Václav Zeman
 */
#pragma once

#include <vector>
#include <queue>
#include <memory>
#include <thread>
#include <mutex>
#include <condition_variable>
#include <future>
#include <functional>
#include <stdexcept>
#include <iostream>
#include "badgerThread.h"

namespace littleBadger {
    
  /**
   * ThreadPool is composed of lots of threads, and it manages those threads.
   */
  class ThreadManager {
  public:
    ThreadManager(size_t);
    
    void enqueueObj(BadgerThread);
    
    // template<class F, class... Args> 
    // auto enqueueFun(F&& f, Args&&... args) -> std::future<typename std::result_of<F(Args...)>::type>;
    
    ~ThreadManager();
  private:
      // need to keep track of threads so we can join them
      std::vector<std::thread> workers;

      // the task queue: one for object, another for function
      std::queue<BadgerThread> tasksO; // use in this project, an object = a tack = a transaction
      // std::queue<std::function<void()>> tasks; // notuse in this project

      // BadgerThread pointer is used to get a task from taskO
      BadgerThread *taskO;

      // synchronization
      std::condition_variable condition;
      std::mutex queue_mutex;
      bool stop;
  };

  /**
   * the constructor launches maxThds of threads, each thread is constructed by a lambda closure.
   * ex. [capturing] {body} https://en.cppreference.com/w/cpp/language/lambda
   */
  inline ThreadManager::ThreadManager(size_t maxThds) : stop(false) {
    for (size_t i = 0; i < maxThds; ++i) {
      // emplace_back = construct an element in place using args as the arguments for its 
      // constructor and insert element at the end
      workers.emplace_back( 
        [this]{
          for (;;) {
            std::unique_lock<std::mutex> lock(this->queue_mutex); // this is a lock that every threads hold
            // [this]{ return this->stop || !this->tasks.empty();} is a function that return ture or false
            // when it return false, then condition.wait(lock, bool) will keep waiting.
            // when it return true, then we can move to next line if the thread receives a notify
            this->condition.wait(lock, [this]{ return this->stop || !this->tasksO.empty();});
            if (this->stop && this->tasksO.empty()) {
              std::cout << "thread end" << std::endl; 
              return;
            } 

            // get an olders task from queue and executes it
            taskO = &(tasksO.front());
            this->tasksO.pop();
            taskO->run();
          }
        }
      );
    }
  }

  /** 
   * add new work item to the pool
   *
  template<class F, class... Args>
  auto ThreadPool::enqueueFun(F&& f, Args&&... args) -> std::future<typename std::result_of<F(Args...)>::type> {
      using return_type = typename std::result_of<F(Args...)>::type;

      auto task = std::make_shared<std::packaged_task<return_type()>>(
          std::bind(std::forward<F>(f), std::forward<Args>(args)...));
          
      std::future<return_type> res = task->get_future();
      std::unique_lock<std::mutex> lock(queue_mutex);

      // don't allow enqueueing after stopping the pool
      if (stop) {
          throw std::runtime_error("enqueue on stopped ThreadPool");
      }
          
      tasks.emplace([task](){ (*task)(); });
      
      condition.notify_one();
      return res;
  }
  */

  /**
   * when a new task is added to the queue, this queue will notify one thread to execute the task
   */
  void ThreadManager::enqueueObj(BadgerThread bThread) {
      std::unique_lock<std::mutex> lock(queue_mutex);

      // don't allow enqueueing after stopping the pool
      if (stop) {
          throw std::runtime_error("enqueue on stopped ThreadPool");
      }

      tasksO.emplace(bThread);
      condition.notify_one();
  }

  // the destructor joins all threads
  inline ThreadManager::~ThreadManager() {
      // std::unique_lock<std::mutex> lock(queue_mutex);
      stop = true;
      condition.notify_all();

      int sizeThd = workers.size();
      for (int i = sizeThd - 1; i >= 0; i--) {
          workers[i].join();
      }
  }

  /**
   * std::condition_variable
   * 
   * The condition_variable class is a synchronization primitive that can be used to block a thread, 
   * or multiple threads at the same time, until another thread both modifies a shared variable (the 
   * condition), and notifies the condition_variable. 
   * The thread that intends to modify the variable has to 
   * 1. acquire a std::mutex (typically via std::lock_guard)
   * 2. perform the modification while the lock is held
   * 3. execute notify_one or notify_all on the std::condition_variable (the lock does not need to be 
   *    held for notification.
   */

  /** 
   * std::function<void()>
   * 
   * Class template std::function is a general-purpose polymorphic function wrapper. 
   * Instances of std::function can store, copy, and invoke any Callable target functions, lambda 
   * expressions, bind expressions, or other function objects, as well as pointers to member 
   * functions and pointers to data members.
   */

  /**
   * In C++11, there are two syntaxes for function declaration:
   * 1. return-type identifier ( argument-declarations... )
   * 2. auto identifier ( argument-declarations... ) -> return_type
   * 
   * https://stackoverflow.com/questions/22514855/arrow-operator-in-function-heading
   */

  /**
   * [capture] {body} is a lambda expression that builds an unnamed function
   * a lambda expression constructs a closure, an unnamed function object capable of capturing variables in scope
   * [this] = capture variable in this, {body} is the function body
   */
}

