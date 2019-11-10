#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "mapreduce.h"

/**
 * This is a user build function.
 */
void Map(char *file_name) {
    FILE *fp = fopen(file_name, "r");
    assert(fp != NULL);

    char *line = NULL;
    size_t size = 0;
    while (getline(&line, &size, fp) != -1) {
        char *token, *dummy = line;
        while ((token = strsep(&dummy, " \t\n\r")) != NULL) {
            MR_Emit(token, "1");
        }
    }
    free(line);
    fclose(fp);
}

/**
 * This is a user build function.
 */
void Reduce(char *key, Getter get_next, int partition_number) {
    int count = 0;
    char *value;
    while ((value = get_next(key, partition_number)) != NULL)
        count++;
    printf("%s %d\n", key, count);
}

int main(int argc, char *argv[]) {
    MR_Run(argc, argv, Map, 10, Reduce, 10, MR_DefaultHashPartition);
}

/**
 * Let’s walk through this code:
 * First, notice that Map() is called with a file name. In general, we assume that this type of 
 * computation is being run over many files; each invocation of Map() is thus handed one file 
 * name and is expected to process that file in its entirety.
 * 
 * In this example, the code above just reads through the file, one line at a time, and uses 
 * strsep() to chop the line into tokens. 
 * Each token is then emitted using the MR_Emit() function, which takes two strings as input: 
 * a key and a value. 
 * The key here is the word itself, and the token is just a count, in this case, 1 (as a string). 
 * It then closes the file.
 * 
 * The MR_Emit() function is thus another key part of your library; 
 * it needs to take key/value pairs from the many different mappers and store them in a way that 
 * later reducers can access them, given constraints described below. 
 * Designing and implementing this data structure is thus a central challenge of the project.
 * 
 * After the mappers are finished, your library should have stored the key/value pairs in such a 
 * way that the Reduce() function can be called. 
 * 
 * Reduce() is invoked once per key, and is passed the key along with a function that enables 
 * iteration over all of the values that produced that same key. 
 * To iterate, the code just calls get_next() repeatedly until a NULL value is returned; 
 * get_next returns a pointer to the value passed in by the MR_Emit() function above, 
 * or NULL when the key’s values have been processed. 
 * 
 * The output, in the example, is just a count of how many times a given word has appeared, 
 * and is just printed to standard output.
 * 
 * All of this computation is started off by a call to MR_Run() in the main() routine of the user 
 * program. This function is passed the argv array, and assumes that argv[1] … argv[n-1] 
 * (with argc equal to n) all contain file names that will be passed to the mappers.
 */