#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>   
     
int main(int argc, char *argv[]) {
    // getpid() get the process ID of current running process
    printf("hello world (pid:%d)\n", (int) getpid());

    // fork returns two numbers, otherwise negative number to represent failed
    // rc = 0 means in child process, rc > 0 means parent process
    // actually, parent's rc is equal to child's process ID
    int rc = fork();
    if (rc < 0) {         // fork failed; exit
        fprintf(stderr, "fork failed\n");
        exit(1);
    } else if (rc == 0) { // child (new process)
        printf("hello, I am child (pid:%d)\n", (int) getpid());
    } else {              // parent goes down this path (main)
        printf("hello, I am parent of %d (pid:%d)\n", rc, (int) getpid());
    }
    return 0;
}

/**
 * Now the interesting part begins. 
 * The process calls the fork() system call, which the OS provides as a way to create a new process. 
 * The odd part: the process that is created is an (almost) exact copy of the calling pro- cess. 
 * That means that to the OS, it now looks like there are two copies of the program p1 running, 
 * and both are about to return from the fork() system call. 
 * The newly-created process (called the child, in contrast to the creating parent) 
 * doesn’t start running at main(), 
 * like you might expect (note, the “hello, world” message only got printed out once); 
 * rather, it just comes into life as if it had called fork() itself.
 * 
 * You might have noticed: the child isn’t an exact copy. 
 * Specifically, al- though it now has its own copy of the address space 
 * (i.e., its own private memory), its own registers, its own PC, and so forth, 
 * the value it returns to the caller of fork() is different. 
 * Specifically, while the parent receives the PID of the newly-created child, 
 * the child receives a return code of zero. 
 * This differentiation is useful, 
 * because it is simple then to write the code that handles the two different cases (as above).
 **/