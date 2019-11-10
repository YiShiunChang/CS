#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>

int main(int argc, char *argv[]) {
    printf("hello world (pid:%d)\n", (int) getpid());
    int rc = fork();
    if (rc < 0) {         // fork failed; exit
        fprintf(stderr, "fork failed\n");
        exit(1);
    } else if (rc == 0) { // child (new process)
        printf("hello, I am child (pid:%d)\n", (int) getpid());
    } else {              // parent goes down this path (main)
        int rc_wait = wait(NULL);
        printf("hello, I am parent of %d (rc_wait:%d) (pid:%d)\n",rc, rc_wait, (int) getpid());
    }
    return 0;
}

/**
 * Usually (as callFork), the output is not deterministic. 
 * When the child process is created, there are now two active processes 
 * in the system that we care about: the parent and the child. 
 * Assuming we are running on a system with a single CPU (for simplicity), 
 * then either the child or the parent might run at that point.
 * 
 * In this case (callForkWait), parent waits child to finish frist.
 * The reason is wait(NULL), this function waits until child is finished,
 * then allows parent to continue its work.
 * It's like wait is in the belly of parent, and is out of the body of child,
 * so we are certainly wait for child.
 * 
 * Note. there are a few cases where wait() returns before the child exits; 
 * read the man page for more details.
 **/