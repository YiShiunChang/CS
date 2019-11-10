#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <fcntl.h>
#include <signal.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>

// used for syntax errors defined by this project
char error_message[30] = "An error has occurred\n"; 
// parameters for builtin history command
char *historycommands;
int historycommands_index;
// parameters for where does wish search a specified program 
char initpath[100][204];
int path_index;
// function for reading input from a file or stdin
int inputs_fromfilesorterminal();
// function for transforming a line of input to program + arguments
int inputs_stringtoprogram();
int numElements; // store the sum of number of program and number of arguments 
// builtin commands
void run_builtin_cd();
void run_builtin_history();
void run_builtin_path();
// functions for split an input into two two commands
// ex. ls -l | wc -l => ls -l and wc -l
// ex. ls > t.txt => ls and t.txt
void split_input();
// functions for pipeline ex. ls -l | wc -l
void run_pipeline();
int ispipe = 0; // 0 means no | exist, >=1 means there are | in the input line
int pipe_index = 0; // the index of | in arguments
// functions for redirection
void run_redirect();
int isredirect = 0;
int redirect_index = 0;
// function for building path and check whether that path is existed
int path_exist();

int main(int argc, char *argv[]) {
    // initialize parameters of builtin history command
    int r = 100, c = 1000; // r = row, c = column
    historycommands = (char *)malloc(r * c * sizeof(char)); 
    historycommands_index = 0;

    // array for search path definition, and built-in path command
    strcpy(initpath[0], "/bin/"); // now wish can search program in /bin/
    path_index = 1;

    // two modes of calling wish: 1. interactive mode, 2. batch mode
    // ex. prompt> ./wish
    //     wish> 
    // ex. prompt> ./wish batch.txt
    // therefore, when calling wish in terminal, argc can only be 2 at most
    if (argc > 2) {
        write(STDERR_FILENO, error_message, strlen(error_message)); 
        exit(1);
    }
    
    FILE *fp;
    // batch mode (argc == 2)
    // get all commands(each command is composed of program and argument) from the input file
    if (argc == 2) {
        fp = fopen(argv[1], "r");
        if (fp == NULL) {
            write(STDERR_FILENO, error_message, strlen(error_message)); 
            exit(1);
        }
    }

    while (1) {
        // each line of input can not exceed 1000 chars
        char input[1000]; 
        // interactive mode
        if (argc == 1) {
            printf("wish> ");
            fp = stdin;
            int getline_success = inputs_fromfilesorterminal(fp, input);
            if (getline_success + 1 == 0) continue;
        }
        // batch mode
        else {
            int getline_success = inputs_fromfilesorterminal(fp, input);
            if (getline_success + 1 == 0) break;
        }

        // if historycommands_index == r, it means historycommands array is full
        // so we need to increase its size and 
        if (historycommands_index == r) {
            char *temp = historycommands;
            free(historycommands);
            historycommands = (char *) malloc((r*2) * c * sizeof(char));
            for (int i=0; i<historycommands_index; i++) {
                for (int j=0; j<c; j++) 
                    *(historycommands + i*c + j) = *(temp + i*c + j);
            }
        }
        // update historycommands for built-in history
        if (strcmp(input, "") != 0) {
            for (int i = 0; i<c; i++) {
                *(historycommands + historycommands_index*c + i) = input[i]; 
            }
            historycommands_index ++;
        }
        
        fflush(stdin);
        fflush(stdout);

        // built-in exit command
        // when the user types exit, shell should simply call the exit system call with 0 as a parameter
        // bin/exit is not a valid path, exit is not under bin
        if (strcmp(input, "exit") == 0) exit(0);

        // decompose the input string to string array
        // each elements in the array tokens could be a program, argument, |, &, >, or <
        // if input = ls -l, thus, tokens is a command line ex. [ls] [-l], and numElements = 2
        char *tokens[50];
        numElements = inputs_stringtoprogram(input, tokens);

        // for (int i=0; i<=numElements; i++)
        //     printf("token %d: %s\n", i, tokens[i]);
        // printf("%d, %d, %d\n", ispipe, numElements, pipe_index);
        
        if (numElements == 0) {
            continue;
        }
        if (strcmp(tokens[0], "cd") == 0) {
            run_builtin_cd(tokens);
            continue;
        }
        if (strcmp(tokens[0], "history") == 0) {
            run_builtin_history(tokens, c);
            continue;
        }
        if (strcmp(tokens[0], "path") == 0) {
            run_builtin_path(tokens);
            continue;
        }

        // printf("start process tokens[0]: %s\n", tokens[0]);
        // create child process
        int rc = fork();
        if (rc < 0) { // fork failed; exit
            write(STDERR_FILENO, error_message, strlen(error_message));
            exit(1);
        } 
        else if (rc == 0) { // child (new process)
            // an input can only has one | or one >
            if ((ispipe != 0 && isredirect != 0) || ispipe > 1 || isredirect > 1) {
                write(STDERR_FILENO, error_message, strlen(error_message));
            }
            // if there is one | and no >, try to execute pipline command
            else if (ispipe == 1 && isredirect == 0) {
                if (pipe_index != 0 && pipe_index != numElements -1) {
                    char *cmd1[pipe_index + 1]; // char *tokens[] for cmd1
                    char *cmd2[numElements - pipe_index]; // char *tokens[] for cmd2

                    split_input(tokens, cmd1, cmd2, 0);
                    run_pipeline(tokens, cmd1, cmd2);
                }
                else write(STDERR_FILENO, error_message, strlen(error_message));
            }
            // if there is no | and one >, try to execute redirection command
            else if (ispipe == 0 && isredirect == 1) {
                char *cmd1[redirect_index + 1]; // char *tokens[] for cmd1
                char *cmd2[numElements - redirect_index]; // char *tokens[] for cmd2
                split_input(tokens, cmd1, cmd2, 1);
                // FILE *freopen(const char *filename, const char *mode, FILE *stream) associates 
                // a new filename with the given open stream and at the same time closes the old file in the stream.
                freopen(cmd2[0], "w", stdout); 
                char path[104];
                // concatnate initpath and cmd1[0], ex. /bin/ + ls = /bin/ls
                path_exist(initpath, path_index, cmd1[0], path);
                execv(path, cmd1); 
                write(STDOUT_FILENO, error_message, strlen(error_message));
            }
            else if (isredirect == 0 && ispipe == 0) {
                char path[104];
                path_exist(initpath, path_index, tokens[0], path);

                // int execv(const char *path, char *const argv[]);
                // execv(), execvp(), and execvpe() functions provide an array of pointers, which is char *const argv[],
                // to null-terminated strings that represent the argument list available to the new program. 
                // the first argument, by convention, should point to the filename associated with the file being executed. 
                // the array of pointers must be terminated by a NULL pointer
                execv(path, tokens); 
                write(STDERR_FILENO, error_message, strlen(error_message));
            }
            exit(0); // leave child process
        } 
        else { // parent goes down this path (main)
            wait(NULL); // int rc_wait = wait(NULL);
            ispipe = 0;
            isredirect = 0;
            // printf("hello, I am parent of %d (rc_wait:%d) (pid:%d)\n", rc, rc_wait, (int) getpid());
        }
    }
    free(historycommands);
}

/**
 * 
 *  below are build in functions
 * 
 */

/**
 * Read program and arguments from terminal or file
 */
int inputs_fromfilesorterminal(FILE *fp, char *input) {
    // ssize_t getline(char **lineptr, size_t *n, FILE *stream);
    // getline() reads an entire line from stream, storing the address of the buffer containing the text into *lineptr.  
    // the buffer is null-terminated and includes the newline character, if one was found.
    // getline() returns the number of characters read, including the delimiter character, 
    // but not including the terminating null byte ('\0'). Otherwiese, return -1
    // char buffer[100];
    // char *input = buffer;
    size_t bufsize = 1000;
    ssize_t inputLen = getline(&input, &bufsize, fp);
    // change newline character to null terminator
    if (input[inputLen - 1] == '\n') {
        input[inputLen - 1] = '\0';
    }

    return inputLen;
}

/**
 * Decompose string input into sub-string by using delimiter " ",
 * so a string is decomposed into program and arguments 
 * ex. char *input = ab cd ed,   [ab] [cd] [ed]
 */
int inputs_stringtoprogram(char *input, char *tokens[50]) {
    // char *strtok(char *str, const char *delim);
    // strtok() breaks string str into a series of tokens using the delimiter delim.
    char *token; 
    int numTokens = 0;
    
    token = strtok(input, " ");
    while (token != NULL) { // get other tokens one by one
        
        char *pPosition = strchr(token, '|');
        char *rPosition = strchr(token, '>');
        if (pPosition) {
            if (strlen(token) == 1) {
                pipe_index = numTokens;
                ispipe ++;
                tokens[numTokens++] = token;
                token = strtok(NULL, " ");
            }
            else {
                char *beforepipe;
                beforepipe = (char *) malloc((strlen(token) - strlen(pPosition))*sizeof(char));
                strncpy(beforepipe, token, strlen(token) - strlen(pPosition));
                if (strlen(beforepipe) != 0) tokens[numTokens++] = beforepipe;
                
                pipe_index = numTokens;
                ispipe++;
                tokens[numTokens++] = "|";

                pPosition++;

                if (strlen(pPosition) != 0) tokens[numTokens++] = pPosition;
                if (strchr(pPosition, '|')) {
                    ispipe ++;
                    break;
                }
                token = strtok(NULL, " ");
            }
        }
        else if (rPosition) {
            if (strlen(token) == 1) {
                redirect_index = numTokens;
                isredirect ++;
                tokens[numTokens++] = token;
                token = strtok(NULL, " ");
            }
            else {
                char *beforeredirect;
                beforeredirect = (char *) malloc((strlen(token) - strlen(rPosition))*sizeof(char));
                strncpy(beforeredirect, token, strlen(token) - strlen(rPosition));
                if (strlen(beforeredirect) != 0) tokens[numTokens++] = beforeredirect;
                
                redirect_index = numTokens;
                isredirect++;
                tokens[numTokens++] = ">";

                rPosition++;

                if (strlen(rPosition) != 0) tokens[numTokens++] = rPosition;
                if (strchr(rPosition, '>')) {
                    ispipe ++;
                    break;
                }
                token = strtok(NULL, " ");
            }
        }
        else {
            tokens[numTokens++] = token;
            token = strtok(NULL, " ");
        }        
    }
    tokens[numTokens] = NULL;

    return numTokens;
}

/**
 * This function runs built-in cd command
 */
void run_builtin_cd(char *tokens[]) {
    if (numElements != 2) {
        write(STDERR_FILENO, error_message, strlen(error_message));
        return;
    } 
    // int chdir(const char *path);
    // chdir() is a system function (system call) which is used to change the current working directory
    // return 0 if success, otherwise, -1
    if (chdir(tokens[1]) != 0) write(STDERR_FILENO, error_message, strlen(error_message)); 
}

/**
 * This function runs built-in history command
 */
void run_builtin_history(char *tokens[], int c) {
    char *history = historycommands;
    // history can only have one argument
    if (numElements > 2) {
        write(STDERR_FILENO, error_message, strlen(error_message)); 
    }

    // no argument case ex. history
    if (numElements == 1) {
        for (int i=0; i<historycommands_index; i++) {
            write(STDOUT_FILENO, history, strlen(history)); 
            write(STDOUT_FILENO, "\n", strlen("\n")); 
            history += c;
        }
    }

    // one argument ex. history 3, history x, history 3.5
    if (numElements == 2) {
        char temp[100];
        char *endptr = temp;
        // casts string, which is tokens[1] in this case, to float if it can
        // endptr = "", if success, otherwise, endptr = string
        float nlines_f = strtof(tokens[1], &endptr);
        
        // arguments can not be cast to float
        if (strcmp(endptr, "") != 0) write(STDERR_FILENO, error_message, strlen(error_message));
        int nline = (int) ceil(nlines_f);
        
        // argument >= number of historycommands, or <
        if (nline >= historycommands_index) {
            for (int i=0; i<historycommands_index; i++) {
                write(STDOUT_FILENO, history, strlen(history)); 
                write(STDOUT_FILENO, "\n", strlen("\n")); 
                history += c;
            }
        }
        else {
            for (int i=0; i<historycommands_index; i++) {
                if (i < historycommands_index - nline) history += c;
                else {
                    write(STDOUT_FILENO, history, strlen(history)); 
                    write(STDOUT_FILENO, "\n", strlen("\n")); 
                    history += c;
                }
            }
        }
    }
}

/**
 * This function runs built-in path command
 */
void run_builtin_path(char *tokens[]) {
    // renew array of search path definition
    int newpath_index = 0;
    // get all arguments as paths
    for (int i=1; i<numElements; i++) {
        // strtok doesn't create a new string, instead, it makes tokens[i] to point to
        // the address where it should be
        // so, we need to copy tokens[i] to new char* before we manipulate a specific string
        char *temptoken = (char *) malloc(204 * sizeof(char));
        strcpy(temptoken, tokens[i]);

        // makesure each argument is ended with '/'
        int end = strlen(temptoken) - 1;
        if (temptoken[end] != '/') strncat(temptoken, "/", 1);

        // check whether paths exist or not, renew path-array if they exist
        if (access(tokens[i], X_OK) == 0) {
            strcpy(initpath[i-1], temptoken);
            newpath_index ++;
        }
        free(temptoken);
    }  

    for (int i=newpath_index; i<path_index; i++) strcpy(initpath[i], "");
    path_index = newpath_index;
}

/**
 * This function splits an input into two commands
 * 
 * @param int pipeorredi 0 for pipe, 1 for redirect
 */
void split_input(char *tokens[], char *cmd1[], char *cmd2[], int pipeorredi) {
    int split_index;
    if (pipeorredi) split_index = redirect_index;
    else split_index = pipe_index;

    // split tokens[], which is arguments to two commands
    int is_cmd1_now = 1;
    int tmp_index = 0;
    for (int i=0; i<numElements; i++) {
        if (is_cmd1_now) {
            if (i == split_index) {
                is_cmd1_now = 0;
                tmp_index = 0;
                continue;
            }
            cmd1[tmp_index] = strdup(tokens[i]);
        }
        else
            cmd2[tmp_index] = strdup(tokens[i]);
        tmp_index++;
    }
    cmd1[split_index] = NULL;
    cmd2[numElements - split_index - 1] = NULL;
}

// pipe | case
void run_pipeline(char *tokens[], char *cmd1[], char *cmd2[]) {
    // printf("cmd1 %s\n", cmd1[0]);
    // printf("cmd2 %s\n", cmd2[0]);
    int READ_END = 0;
    int WRITE_END = 1;

    int fd[2];
    pipe(fd);

    int rc1 = 0;
    int rc2 = 0;
    if ((rc1 = fork()) == 0) {
        // dup2(fd[WRITE_END], STDOUT_FILENO);
        // close(fd[READ_END]);
        dup2(fd[1], 1);
        close(fd[0]);

        char path[104];
        path_exist(initpath, path_index, cmd1[0], path);
        execv(path, cmd1);
        write(fd[1], error_message, strlen(error_message));
        // exit(1);
    }
    else { 
        if ((rc2 = fork()) == 0) {
            // dup2(fd[READ_END], STDIN_FILENO);
            // // close(fd[READ_END]);
            // close(fd[WRITE_END]);
            int status;
            waitpid(rc1, &status, WUNTRACED);
            // printf("child process status: %d\n", status);
            // printf("WIFEXITED(status) return: %d\n", WIFEXITED(status));
            // printf("WEXITSTATUS(status): %d\n", WEXITSTATUS(status));
            dup2(fd[0], 0);
            close(fd[1]);

            char path[104];
            path_exist(initpath, path_index, cmd2[0], path);
            execv(path, cmd2);
            write(STDERR_FILENO, error_message, strlen(error_message));
        }
        else {
            int status;
            close(fd[READ_END]);
            close(fd[WRITE_END]);
            waitpid(rc2, &status, WUNTRACED);
        }
    }
}

/**
 * Concatnate the initpaths with tokens[0], and
 * check whether a program is existed in the given initpath array
 * ex. /bin/ concatnate ls, and check /bin/ls 
 */
int path_exist(char initpath[100][204], int path_index, char *program, char *path) {

    for (int i=0; i<path_index; i++) {
        strcpy(path, initpath[i]);
        // char *strncat(char *dest, const char *src, size_t n)
        // n: represents maximum number of character to be appended. size_t is an unsigned integral type.
        strncat(path, program, strlen(program)); 

        // int access(const char *pathname, int mode);
        // mode F_OK tests for the existence of the file.
        // R_OK, W_OK, and X_OK test whether the file exists and grants read, write, and execute permissions, respectively.
        // on success (all requested permissions granted), 0 is returned, otherwise, -1.
        if (access(path, X_OK) == 0) return 0;
    }

    return -1;
}
