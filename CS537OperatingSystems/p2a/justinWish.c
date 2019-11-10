#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <signal.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>

char *inputString();
void print_prompt();
void print_error();
int parse_cmd();
void run_cd();
void run_pwd();
void run_pipe();

/**
 * This function wirtes prompt "wish (hisn)> " to the shell screen
 * hisn is a number from 0 to the times of calling prompt 
 */
void print_prompt(int his_n) {
    char his_w[12], prompt[30];
    // int sprintf(char *str, const char *string,...); 
    // sprintf stands for “String print”. Instead of printing on console, 
    // it store output on char buffer "char *str", which are specified in sprintf
    sprintf(his_w, "%d", his_n);
    strcpy(prompt, "wish (");
    strcat(prompt, his_w);
    strcat(prompt, ")> ");

    // get the actual size of prompt
    // prompt ends when it reaches '\0' before end of the array prompt[30]
    int true_prompt_size = 0;
    for (true_prompt_size=0; true_prompt_size<30; true_prompt_size++) {
        if (prompt[true_prompt_size] == '\0') break;
    }

    // ssize_t write(int fd, const void *buf, size_t count);
    // which writes up to "count" bytes from the buffer "buf" to the file
    // referred to by the file descriptor "fd"
    write(STDOUT_FILENO, prompt, true_prompt_size);
}

/**
 * This function gets input from stdin (FILE *fp = stdin), and its initial size is 10.
 * While getting a char from fp at a time, this function adjust the size of returning 
 * string dynamically.  
 * 
 * @param FILE *fp stdin input, could be a command
 * @return char* a string with fit memory size
 */
char* inputString(FILE *fp, size_t size) {
    char *str;
    int ch;
    // size_t is an unsigned integer type of at least 16 bit
    // the size is extended by the input with the value of the provisional
    size_t len = 0;

    // void *realloc(void *ptr, size_t size) attempts to resize the memory block pointed to by ptr 
    // that was previously allocated with a call to malloc or calloc
    // void *realloc() returns a pointer to the newly allocated memory, or NULL if the request fails
    str = realloc(NULL, sizeof(char) * size); 
    if (!str) return str; // check whether request fails

    // fgetc() takes a single character at a time from input, which is a file
    while (EOF != (ch=fgetc(fp)) && ch != '\n') {
        str[len++] = ch;
        if (len == size) {
            str = realloc(str, sizeof(char) * (size += 16));
            if (!str) return str;
        }
    }
    // \0 is the implicit nul terminator that is always added - even if the string literal just happens to end with \0
    // char str[6] = "Hello\0"; // strlen(str) = 5, sizeof(str) = 6 (with one NUL)
    // char str[7] = "Hello\0"; // strlen(str) = 5, sizeof(str) = 7 (with two NULs)
    // char str[8] = "Hello\0"; // strlen(str) = 5, sizeof(str) = 8 (with three NULs per C99 6.7.8.21)
    str[len++]='\0';

    return realloc(str, sizeof(char) * len);
}

/**
 * This function is used to count int argc, which is the number of program and arguments
 * ex. cat file.c 
 * return 2
 * 
 * @param char* a command line, which is composed of program and arguments
 * @return int 1 + the number of arguments
 */
int count_token(char* str) {
    int count = 0;
    char *ptr = str;

    // char *strchr(const char *str, int c) searches for the first occurrence of 
    // the character c (an unsigned char) in the string pointed to by the argument str
    // it returns a pointer to the first occurrence of the character c in the string str, 
    // or NULL if the character is not found

    // set ptr = the substring after the first occurrence of ' ' (include ' ')
    while((ptr = strchr(ptr, ' ')) != NULL) {
        count++;
        ptr++; // since ' ' is included, so we need ptr++
    }
    
    return count+1;
}

/**
 * This function check whether a input is NULL or not.
 * It takes a line of commands from "input" and separate it to several commands,
 * and save them in nargv
 * 
 * @param input a string that is composed of program and arguments
 * @param nargv an empty array of string with a specific size
 */
int parse_cmd(char *input, char **nargv) {
    // char *strtok(char *str, const char *delim);
    // strtok splits a string "str" by a delimiter "delim", and return the first token after split
    // On the first call to strtok() the string to be parsed should be specified in str. 
    // In each subsequent call that should parse the same string, str should be NULL, so
    // we can get the remaining tokens of the "str" that we parse in at the beginning
    char *token = strtok(input, " ");
    int idx = 0;
    while(token != NULL) {
        // the strdup() and strndup() functions are used to duplicate a string
        nargv[idx++] = strdup(token);
        // idx++;
        token = strtok(NULL, " ");
    }

    // if idx is outofthebound, then this line has no effect
    // otherwise, we set nargv[idx] = NULL
    // this line exists for the case of NULL input
    nargv[idx] = NULL;
 
    return idx;
}

/**
 * This function writes error message to to the file 
 * referred to by the file descriptor "STDERR_FILENO"
 * 
 * STDERR_FILENO usually refers to 2
 */
void print_error() {
    char error_message[30] = "An error has occurred\n";
    write(STDERR_FILENO, error_message, strlen(error_message));
}










/**
 * 
 */
void run_pipe(int *fd, char **cmd1, char **cmd2) {

    int status;
    int rc1, rc2;
                
    switch(rc1 = fork()) {
                
        case 0:
            // dup() system call creates a copy of a file descriptor
            // dup2(input, 0); replace standard input with input file
            // dup2(output, 1); replace standard output with output file

            // int dup2(int oldfd, int newfd);
            // After a successful return, the old and new file descriptors may be
            // used interchangeably.  They refer to the same open file description
            // (see open(2)) and thus share file offset and file status flags; for
            // example, if the file offset is modified by using lseek(2) on one of
            // the file descriptors, the offset is also changed for the other.
            dup2(fd[0], 0);

            // int close(int fd);
            // closes a file descriptor, so that it no longer refers to any file and may be reused
            close(fd[1]);

            // exec family of functions replaces the current running process with a new process
            // It can be used to run a C program by using another C program
            // in this case. it is like using wc command to replace everything in child process
            execvp(cmd2[0], cmd2);
            print_error();
        default:
            switch(rc2=fork()) {
                case 0:
                    dup2(fd[1], 1);
                    close(fd[0]);
                    execvp(cmd1[0], cmd1);
                    print_error();
                default:
                    close(fd[0]); close(fd[1]);
                    do {
                        // WUNTRACED
                        // the status of any child processes specified by pid that are stopped, 
                        // and whose status has not yet been reported since they stopped, 
                        // shall also be reported to the requesting process.

                        // if wait() or waitpid() return because the status of a child process is available, 
                        // these functions shall return a value equal to the process ID of the child process. 
                        // in this case, if the value of the argument stat_loc is not a null pointer, 
                        // information shall be stored in the location pointed to by stat_loc.
                        waitpid(rc1, &status, WUNTRACED);
                        waitpid(rc2, &status, WUNTRACED);
                        
                    // WIFEXITED(stat_val)
                    // Evaluates to a non-zero value if status was returned for a child process that terminated normally
                    // WIFSIGNALED(stat_val)
                    // Evaluates to a non-zero value if status was returned for a child process that terminated 
                    // due to the receipt of a signal that was not caught (see <signal.h>).
                    } while (!WIFEXITED(status) && !WIFSIGNALED(status));
            }
    }
}

/**
 * 
 */
void run_cd(char *nargv[]) {
    
    if(!nargv[1]) 
        // the chdir command is a system function (system call) which is used to change the current working directory. 
        // On some systems, this command is used as an alias for the shell command cd. 
        // chdir changes the current working directory of the calling process to the directory specified in path.
        // This command returns zero (0) on success. -1 is returned on an error and errno is set appropriately.

        // The getenv() function searches the environment list to find the
        // environment variable name, and returns a pointer to the corresponding value string
        // ex. PATH is a environment variable and it is the key of a "key-value" pair
        // printf("PATH : %s\n", getenv("PATH"));
        // PATH : /sbin:/usr/sbin:/bin:/usr/bin:/usr/local/bin
        // /sbin:/usr/sbin:/bin:/usr/bin:/usr/local/bin is the value

        // Environment variables are not specific to the C language, 
        // any program that runs on your system has a set of predefined environment variables. 
        // Those are global variables defined and passed to the process that runs your program 
        // from its parent process (every single process has a parent, 
        // except for init, which is the father of all user processes). 
        // They define different things that may affect your program’s behavior, 
        // like the name of the default shell or text editor, 
        // the PATH that is searched for binary executables, and so forth.
        chdir(getenv("HOME"));
    else {
        int chdirn = chdir(nargv[1]);
        if(chdirn) print_error(); // 0 is false, and non-0 is ture
    }
}

/**
 * 
 */
void run_pwd(char *nargv[]) {
    if(nargv[1]) {
        print_error();
        return;
    }
    char cwd[1000];
    // The getcwd() function copies an absolute pathname of the current
    // working directory to the array pointed to by buf, which is of length size
    getcwd(cwd, 1000);
    printf("%s\n", cwd);
    //exit(0);
    return;
}

/**
 * 
 */
int main(int argc, char *argv[]) {

    // extra argument
    if (argc>1) {
        print_error();
        exit(1);
    }

    // times of calling prompt 
    int history_number = 1;
    // keep going in while loop until break inside
    while(1) {
        // show prompt on wish screen ex. wish (1)> 
        print_prompt(history_number++);
        
        // get input string with dynamic fit size
        char *input;
        input = inputString(stdin, 10);
        
        // a command can not be too long
        if (strlen(input) > 127) {
            // strlen(input) counts until \0, sizeof(input) counts memory size
            // a very long command line (over 128 bytes)
            print_error();
            continue;
        }
        
        // count_token() get the sum from number of program and arguments
        // ex. wish (1)> ls => 1
        // ex. wish (2)> ls -l => 2
        int count = count_token(input);
        // make an empty array with count size
        char *nargv[count];
        // parse the command line (which is input)
        // if input is NULL, we get nargv_len: 0, nargv[0]: (null)
        // otherwise, we get nargv_len: count, nargv[0]: program(name)
        int nargv_len = parse_cmd(input, nargv); 

        // do nothing if input is null
        if(!nargv[0]) continue;

        // exit wish if input is "exit"
        // there are many kinds of signal, ex.
        // #define SIGHUP  1   /* Hangup the process */ 
        // #define SIGINT  2   /* Interrupt the process */ 
        // https://www.geeksforgeeks.org/signals-c-language/

        // kill() system call can be used to send any signal to any process group or process
        // int kill(pid_t pid, int sig); 
        // if pid equals 0, then sig is sent to every process in the process
        // group of the calling process
        if(strcmp(nargv[0], "exit") == 0) kill(0, SIGINT);
 
        // parse pipeline & background
        int is_pipeline = 0;
        int is_background = 0;
        int pipe_opr = 0;
        int back_opr = 0;

        // nargv[] may be like below:
        // ls (only program)
        // ls -l (program, arguments)
        // grep -o foo file | wc -l （program arguments | program arguments)
        // | is called pipe, which set the result of first program arguments as input of the second one
        while (nargv[pipe_opr]) {
            // if argv[pipe_opr] == |, means there is a new program behinds this pipe
            if(strcmp(nargv[pipe_opr], "|") == 0) {
                is_pipeline = 1;
                break;
            }
            // a single ampersand & can often be found at the end of a command.
            // ex ./myscript.py &
            // this trailing ampersand directs the shell to run the command in the background, 
            // that is, it is forked and run in a separate sub-shell, as a job, asynchronously. 
            // the shell will immediately return the return status of 0 for true and continue as normal, 
            // either processing further commands in a script or 
            // returning the cursor focus back to the user in a Linux terminal.
            else if (strcmp(nargv[back_opr], "&") == 0) {
                // check whether & is at the end of the nargv[]
                // if not, it is a wrong input
                if (back_opr < (nargv_len - 1)) {
                    print_error();
                    continue;
                }
                is_background = 1;
                nargv[back_opr] = NULL; /* delete & in nargv */
                break;
            }
            pipe_opr ++;
            back_opr ++;
        }
    
        // currently, we know the detail of input in nargv[]
        // nargv[] is like program arguments combinations 
        // | separates out input into several sub-inputs, 
        // and we handle sub-inputs one-by-one
        int rc = fork();
        // child process
        if (rc == 0) {
            // redirection
            // > : directs the output of a command into a file
            // < : gives input to a command
            if (strcmp(nargv[0], ">") == 0 || strcmp(nargv[0], "<") == 0) {
                continue;
            }
            char *out_name="", *in_name="";
            int first_operator = -1;
            int is_redirection_error = 0;
            int tmp = 1;
            while (nargv[tmp]) {
                if (strcmp(nargv[tmp], ">") == 0) {
                    // redirecting output
                    // after directing the output of a command into a file called nargv[tmp+1],
                    // if nargv[tmp+2], it can't be < and | at the same time
                    if (nargv[tmp+2] && strcmp(nargv[tmp+2], "<") != 0 && strcmp(nargv[tmp+2], "|") != 0) {
                        is_redirection_error = 1;
                        break;
                    }
                    else {
                        out_name = strdup(nargv[tmp+1]);
                        if (first_operator == -1) first_operator = tmp;
                    }
                }
                else if (strcmp(nargv[tmp], "<") == 0) {
                    // redirecting input
                    if (nargv[tmp+2] && strcmp(nargv[tmp+2], ">") != 0 && strcmp(nargv[tmp+2], "|") != 0) {
                        is_redirection_error = 1;
                        break;
                    }
                    else {
                        // int access(const char *pathname, int mode);
                        // access() checks whether the calling process can access the file pathname. 
                        // if pathname is a symbolic link, it is dereferenced
                        // F_OK tests for the existence of the file
                        // On success (all requested permissions granted), zero is returned. 
                        // On error -1 is returned, and errno is set appropriately.
                        if (access(nargv[tmp+1], F_OK) == -1) {
                            is_redirection_error = 1;
                            break;
                        }
                        in_name = strdup(nargv[tmp+1]);
                        if (first_operator == -1) first_operator = tmp;
                    }
                }
                tmp++;
            }

            // show error message if is_redirection_error == 1
            if (is_redirection_error) {
                print_error();
                continue;
            }

            // replace stdout with output descriptor, or replace stdin with input descriptor 
            int in, out;
            if (first_operator != -1) {
                // execute redirecting
                if(*out_name) {
                    close(STDOUT_FILENO);
                    out = open(out_name, O_CREAT | O_WRONLY | O_TRUNC, S_IRWXU);
                    // replace 1 with out, which means replace stdout with output file
                    // output file is an output descriptor 
                    dup2(out, 1);
                }
                if (*in_name) {
                    in = open(in_name, O_RDONLY);
                    dup2(in, 0);
                }
                nargv[first_operator] = NULL;
            }
        
            // pipeline
            if (is_pipeline) {
                // pipe operator can not be at nargv[0] or nargv[len-1]
                if( pipe_opr == 0 || pipe_opr == nargv_len - 1) {
                    print_error();
                    continue;
                }
                else {
                    // split nargv to two commands
                    char *cmd1[pipe_opr + 1];
                    char *cmd2[nargv_len - pipe_opr];
                    int is_cmd1 = 1;
                    int tmp = 0;
                    for (int i=0; i<nargv_len; i++) {
                        if (is_cmd1) {
                            if (i == pipe_opr) {
                                is_cmd1 = 0;
                                tmp = 0;
                                continue;
                            }
                            cmd1[tmp] = strdup(nargv[i]);
                        }
                        else
                            cmd2[tmp] = strdup(nargv[i]);
                        tmp++;
                    }
                    cmd1[pipe_opr] = NULL;
                    cmd2[nargv_len - pipe_opr - 1] = NULL;
                
                    // execute pipeline
                    int fd[2];
                    pipe(fd);
                    run_pipe(fd, cmd1, cmd2);
                }
            }
            else {
                // execution
                if (strcmp(nargv[0], "cd") == 0) {
                    run_cd(nargv);
                }
                else if(strcmp(nargv[0], "pwd") == 0) {
                    run_pwd(nargv);
                }
                else {
                    execvp(nargv[0], nargv);
                    print_error();
                }
                //exit(0);
            }
        } else if (rc>0) {
            if(!is_background)
                (void) wait(NULL);

        } else {
            print_error();
        }   


    }

    return 0;
}
