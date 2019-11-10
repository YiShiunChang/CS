#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <fcntl.h>
#include <sys/wait.h>

int main(int argc, char *argv[]) {
    int rc = fork();
    if (rc < 0) {          // fork failed; exit
        fprintf(stderr, "fork failed\n");
        exit(1);
    } else if (rc == 0) { // child: redirect standard output to a file
        // google: Input-output system calls in C 
        // https://www.geeksforgeeks.org/input-output-system-calls-c-create-open-close-read-write/
        // in unistd.h, there is a symbolic constant STDOUT_FILENO, which is a file descriptor of "1"
        close(STDOUT_FILENO);
        // O_CREAT: if pathname does not exist, create it as a regular file
        // O_TRUNC: if the file already exists and is a regular file and the
        // access mode allows writing (i.e., is O_RDWR or O_WRONLY) it
        // will be truncated to length 0
        // S_IRWXU: 00700 user (file owner) has read, write, and execute permission

        // The argument flags must include one of the following access modes:
        // O_RDONLY, O_WRONLY, or O_RDWR.  These request opening the file read-
        // only, write-only, or read/write, respectively.

        // A call to creat() is equivalent to calling open() with flags equal to
        // O_CREAT|O_WRONLY|O_TRUNC.
        open("./redirection.output", O_CREAT|O_WRONLY|O_TRUNC, S_IRWXU);
    
        // now exec "wc"...
        char *myargs[3];
        myargs[0] = strdup("wc");
        myargs[1] = strdup("redirection.c");
        myargs[2] = NULL;
        execvp(myargs[0], myargs);
    } else {              // parent
        int rc_wait = wait(NULL);
        printf("parent ends with rc_wait = %d\n", rc_wait);
    }
    return 0;
}

/*
 * Specifically, UNIX systems start looking for free file descriptors at zero. 
 * In this case, STDOUT FILENO will be the first available one 
 * and thus get assigned when open() is called. 
 * 
 * Subsequent writes by the child process to the standard output file descriptor, 
 * for example by routines such as printf(), 
 * will then be routed transparently to the newly-opened file instead of the screen.
 */
