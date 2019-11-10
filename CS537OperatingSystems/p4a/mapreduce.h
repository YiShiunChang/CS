#ifndef __mapreduce_h__
#define __mapreduce_h__

// Different function pointer types used by MR_Run
typedef char *(*Getter)(char *key, int partition_number);
typedef void (*Mapper)(char *file_name);
typedef void (*Reducer)(char *key, Getter get_func, int partition_number);
typedef unsigned long (*Partitioner)(char *key, int num_partitions);

// External functions: these are what *you must implement*
void MR_Emit(char *key, char *value);

unsigned long MR_DefaultHashPartition(char *key, int num_partitions);

void MR_Run(int argc, char *argv[], 
	    Mapper map, int num_mappers, 
	    Reducer reduce, int num_reducers, 
	    Partitioner partition);

#endif // __mapreduce_h__


/**
 * The most important function is MR_Run, which takes the command line parameters of a given program, 
 * a pointer to a Map function (type Mapper, called map), 
 * the number of mapper threads your library should create (num_mappers), 
 * a pointer to a Reduce function (type Reducer, called reduce), 
 * the number of reducers (num_reducers), 
 * a pointer to a Partition function (partition, described below).
 * 
 * Thus, when a user is writing a MapReduce computation with your library, 
 * they will implement a Map function, 
 * implement a Reduce function, 
 * possibly implement a Partition function, 
 * and then call MR_Run(). 
 * The infrastructure will then create threads as appropriate and run the computation.
 * 
 * One basic assumption is that the library will create num_mappers threads (in a thread pool) 
 * that perform the map tasks. 
 * 
 * Another is that your library will create num_reducers threads to perform the reduction tasks. 
 * 
 * Finally, your library will create some kind of internal data structure to pass keys and values 
 * from mappers to reducers; more on this below.
 */