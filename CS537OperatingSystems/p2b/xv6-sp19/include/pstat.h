#ifndef _PSTAT_H_
#define _PSTAT_H_
#include "param.h"

// Note. #ifndef #define #endif
// Once the header is included, it checks if a unique value (in this case _PSTAT_H_) is defined. 
// Then if it's not defined, it defines it and continues to the rest of the page.
// When the code is included again, the first ifndef fails, resulting in a blank file.
// That prevents double declaration of any identifiers such as types, enums and static variables.

/**
 * This struct pstat is build to test result of MLFQ (multi-level feedback queue) implementation
 */
struct pstat {
  int inuse[NPROC]; // whether this slot of the process table is in use (1 or 0)
  int pid[NPROC];   // PID of each process
  int priority[NPROC];  // current priority level of each process (0-3)
  enum procstate state[NPROC];  // current state (e.g., SLEEPING or RUNNABLE) of each process
  int ticks[NPROC][4];  // number of ticks each process has accumulated at each of 4 priorities
  int wait_ticks[NPROC][4]; // number of ticks each process has waited before being scheduled
};

#endif // _PSTAT_H_