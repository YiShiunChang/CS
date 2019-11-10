#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/wait.h>

int main(int argc, char *argv[]) {
    printf("hello world (pid:%d)\n", (int) getpid());
    int rc = fork();
    if (rc < 0) {         // fork failed; exit
        fprintf(stderr, "fork failed\n");
        exit(1);
    } else if (rc == 0) { // child (new process)
        printf("hello, I am child (pid:%d)\n", (int) getpid());
        char *myargs[3];

        // wc command - print newline, word, and byte counts for each file
        // strdup command - strdup() and strndup() functions are used to duplicate a string
        myargs[0] = strdup("wc");   // program: "wc" (word count)
        myargs[1] = strdup("callForkWaitExec.c"); // argument: file to count
        myargs[2] = NULL;           // marks end of array

        // exec family of functions replaces the current running process with a new process. 
        // It can be used to run a C program by using another C program.
        // in this case. it is like using wc command to replace everything in child process

        // The execv(), execvp(), and execvpe() functions provide an array of pointers to 
        // null-terminated strings that represent the argument list available to the new program. 
        // The first argument, by convention, should point to the filename associated with 
        // the file being executed. The array of pointers must be terminated by a NULL pointer.
        // myargs is arguments for myargs[0]
        execvp(myargs[0], myargs);  // runs word count
        printf("this shouldn’t print out");
    } else {              // parent goes down this path (main)
        int rc_wait = wait(NULL);
        printf("hello, I am parent of %d (rc_wait:%d) (pid:%d)\n",
                rc, rc_wait, (int) getpid());
    }
    return 0;
}

/**
 * The fork() system call is strange; its partner in crime, exec(), is not so normal either. 
 * What it does: given the name of an executable (e.g., wc), and some arguments (e.g., callForkWaitExec.c), 
 * it loads code (and static data) from that executable 
 * and overwrites its current code segment (and current static data) with it; 
 * the heap and stack and other parts of the memory space of the program are re-initialized. 
 * Then the OS simply runs that program, passing in any arguments as the argv of that process. 
 * 
 * Thus, it does not create a new process; rather, 
 * it transforms the currently running program (formerly callForkWaitExec) 
 * into a different running program (wc). 
 * 
 * After the exec() in the child, it is almost as if callForkWaitExec.c never ran; 
 * a successful call to exec() never returns.
 **/ 