#include <stdio.h>
#include <vector>
#include <thread>
#include "badgerThread.h"
#include "lockManager.h"
#include "threadPool.h"

using namespace littleBadger;

int main(int argc, char **argv) {
	printf("begin setup.\n");
	// global parameters setting
	CCAlg cc_alg = DLE;  // concurrency control algorithm: DLE, TRADITIONAL
	DLSol dl_sol = WAIT; // solution to dead lock: DL_WAIT, DL_KILL
	int maxThd = 10;     // # threads 

	// init stats, which collects statistics of the threads, each thread is a transaction
	readTxnCount = 0;
	writeTxnCount = 0;

	// txn manager assigns a queuing txn to an idle thread  
	littleBadger::buildTxnSet();

	// lock manager is used to handle conflicts

	// init threads
	
	

	printf("setup ready and start experiment.\n");

	// threads ask txns from txn_manager to start working, until all txns are done
	// for (uint32_t i = 0; i < thd_cnt; i++) {
	// 	m_thds[i]->init(i, m_wl);
	// }
	
	// get summary of workload
	// printf("Simulation Time = %ld\n", endtime - starttime);
	// stats.print();
	
	return 1;
}
