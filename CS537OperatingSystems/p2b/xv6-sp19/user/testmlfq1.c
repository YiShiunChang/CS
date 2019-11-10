#include "types.h"
#include "stat.h"
#include "user.h"
#include "pstat.h"

int main(int argc, char *argv[]) {
    struct pstat st; // intitialize variable pstat as st

    // struct pstat {
    //     int inuse[NPROC]; // whether this slot of the process table is in use (1 or 0)
    //     int pid[NPROC];   // PID of each process
    //     int priority[NPROC];  // current priority level of each process (0-3)
    //     enum procstate state[NPROC];  // current state (e.g., SLEEPING or RUNNABLE) of each process
    //     int ticks[NPROC][4];  // number of ticks each process has accumulated at each of 4 priorities
    //     int wait_ticks[NPROC][4]; // number of ticks each process has waited before being scheduled
    // };

    if (argc != 2) {
        printf(1, "usage: mytest counter");
        exit();
    }

    int i, x, l, j;
    int mypid = getpid();

    for (i = 1; i < atoi(argv[1]); i++) {
        x = x + i;
    }

    getpinfo(&st);
    for (j = 0; j < NPROC; j++) {
        if (st.inuse[j] && st.pid[j] >= 3 && st.pid[j] == mypid) {
            for (l = 3; l >= 0; l--) {
                printf(1, "level:%d \t ticks-used:%d\n", l, st.ticks[j][l]);
            }
        }
    }
    
    exit();
    return 0;
}