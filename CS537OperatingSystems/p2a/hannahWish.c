#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/wait.h>


int main(int argc, char* argv[])
{
    int mode = (argc-1);  //mode 0: interactive, 1: batch

    //TODO handle argc > 2 ERROR 
    if(argc > 2)
    {
        char error_message[30] = "An error has occurred\n";
        write(STDERR_FILENO, error_message, strlen(error_message)); 
        exit(1);
    }


    FILE *fp;
    if(mode == 1)
    {
        
        fp = fopen(argv[1], "r");
        if(fp == NULL)
        {
            char error_message[30] = "An error has occurred\n";
            write(STDERR_FILENO, error_message, strlen(error_message)); 
            exit(1);
        }
    }

    //TODO create paths, default path "/bin"
        char *path[50];
        char defaultPath[5];
        strcpy(defaultPath, "/bin");
        path[0] = &defaultPath[0];

    while(1) {


        char* wholeCommand;
        size_t bufsize = 4;
        
        if (!mode)
        {
            printf("wish> "); //print the prompt
            fflush(stdout);
            wholeCommand = malloc(bufsize*sizeof(char));
            if(getline(&wholeCommand, &bufsize, stdin) < 0)
            {
                exit(0);
            }
            // printf("%s", wholeCommand);
        }
        else
        {
            
            wholeCommand = malloc(bufsize*sizeof(char));
            if(getline(&wholeCommand, &bufsize, fp) < 0)
            {
            //  printf("EOF\n");
                exit(0);
            }
            
            // printf("so far so good\n");
            //printf("%s\n", wholeCommand);  
        }

        //TODO parse inputs to tokens
        char *str1, *str2, *str3, *token, *subtoken, *ssubtoken;
        char *saveptr1, *saveptr2, *saveptr3;
        char *delim = "&";
        char *subdelim = ">";
        char *ssubdelim = " \t\n";
        char* commands[50][50];
        char* output[50];
        int i, j, p;

        i = 0, j = 0;
        while(commands[i][0] != NULL)
        {
            while(commands[i][j] != NULL)
            {
                commands[i][j] = NULL;
                j++;
            }
            output[i] = NULL;
            i++;
            j = 0;
        }

        for(i = 0, str1 = wholeCommand; ; str1 = NULL)
        {
            token = strtok_r(str1, delim, &saveptr1);
            if(token == NULL || strlen(token) == 0)
            {
                break;
            }
            
            for(str2 = token, p=0; ; str2 = NULL)
            {
                
                subtoken = strtok_r(str2, subdelim, &saveptr2);
                if(subtoken == NULL || strlen(subtoken) == 0)
                    break;
                if(p > 1)  //if more than 1 redirection symbols
                {
                    char error_message[30] = "An error has occurred\n";
                    write(STDERR_FILENO, error_message, strlen(error_message));
                    break;
                }
                //printf("--%d %d-- %s\n", i, p, subtoken);

                if(p == 0)  //command&arguments
                {
                    for(str3 = subtoken, j = 0; ; str3 = NULL)
                    {
                        ssubtoken = strtok_r(str3, ssubdelim, &saveptr3);
                        if(ssubtoken == NULL || strlen(ssubtoken) == 0)
                            break;
                        //printf("%d    %d   %s\n", i, j, ssubtoken);
                        commands[i][j] = ssubtoken;
                        j++;
                    }
                }
                if(p == 1) //output
                {
                    if(strcmp(subtoken,"\n") == 0) // if no output file specified
                    {
                        char error_message[30] = "An error has occurred\n";
                        write(STDERR_FILENO, error_message, strlen(error_message));
                        output[i] = subtoken;
                        break;
                    }

                    int q;
                    for(str3 = subtoken, q = 0; ; str3 = NULL)
                    {
                        ssubtoken = strtok_r(str3, ssubdelim, &saveptr3);
                        if(ssubtoken == NULL || strlen(ssubtoken) == 0)
                            break;
                        if(q > 0) //print err msg if more than 1 output file
                        {
                            char error_message[30] = "An error has occurred\n";
                            write(STDERR_FILENO, error_message, strlen(error_message));
                            break;
                        }
                        // printf("output to   %s\n", ssubtoken);
                        output[i] = ssubtoken;
                        q++;
                    }
                }


                
                //commands[i][0] is the i-th command to execute
                //commands[i][1] onwards are the arguments
                //when redirection received, store the output file name at commands[i][0]
                //print error message if invalid/missing output file name

                
                
                
                
                p++;
            }
            i++;
        }

        // i = 0, j = 0;
        // while(commands[i][0] != NULL)
        // {
        //     while(commands[i][j] != NULL)
        //     {
        //         printf("%s ", commands[i][j]);
        //         j++;
        //     }
        //     printf("\n");
        //     printf("output to : %s\n", output[i]);
        //     i++;
        // }

        int ret = 1;
        i = 0;
        int numFork = 0;

        //execute commands
        while(commands[i][0] != NULL)
        {
            // printf("iteration: %d\nProcess: %d\n", i, getpid());


            //check built-in commands
            //"exit"
            if(strcmp(commands[i][0],"exit") == 0) 
            {
                //error msg if has arguments
                if(commands[i][1] != NULL)
                {
                    char error_message[30] = "An error has occurred\n";
                    write(STDERR_FILENO, error_message, strlen(error_message)); 
                }   

                exit(0);
            }
            //"cd"
            else if(strcmp(commands[i][0],"cd") == 0)
            {
                //error is 0 or >1 arguments
                if(commands[i][1] == NULL || commands[i][2] != NULL)
                {
                    char error_message[30] = "An error has occurred\n";
                    write(STDERR_FILENO, error_message, strlen(error_message)); 
                    i++;
                    continue;
                }
                //make chdir system call. error msg if failed.
                
                // printf("%s\n",commands[i][1]);
                // const char *DIR = commands[i][2];
                int chdirRet = chdir(commands[i][1]);
                
                // char cwd[1024];
                // if (getcwd(cwd, sizeof(cwd)) != NULL)
                //      fprintf(stdout, "Current working dir: %s\n", cwd);
                // else
                //     perror("getcwd() error");
                if(chdirRet != 0)
                {
                    char error_message[30] = "An error has occurred\n";
                    write(STDERR_FILENO, error_message, strlen(error_message)); 
                }
            }
            //"path"
            else if(strcmp(commands[i][0],"path") == 0)
            {
                int k = 0;
                while(path[k] != NULL)  //clear the array
                {
                    // printf("1: %s\n",path[k]);
                    path[k] = NULL;
                    k++;
                }
                
                j = 1;
                while(commands[i][j] != NULL)
                {
                    path[j-1] = commands[i][j];
                    // printf("  %s\n", path[j-1]);
                    j++;
                }
            }
            else // ie, other commands
            {
                ret = fork();
                if(ret < 0) //fork failed
                {
                    char error_message[30] = "An error has occurred\n";
                    write(STDERR_FILENO, error_message, strlen(error_message)); 
                }
                else if(ret > 0) //parent
                {
                    numFork++;
                    i++;
                    continue;
                }
                else //child
                {
                    //output to file if redirected
                    if(output[i] != NULL)
                    {
                        // int ofile = open(output[i],O_CREAT|O_WRONLY);
                        // dup2(ofile, 1);
                        freopen(output[i], "w+", stdout); 
                    }

                    //printf("child created: %d\n", getpid());
                    int k = 0;
                    int complete = -1;
                    //printf("path %s\n", path[k]);
                    while(path[k] != NULL)
                    {
                        //TODO fix strcat while default path
                        //printf("path %s\n", path[k]);
                        size_t lengthOfCommand = strlen(commands[i][0]);
                        char *newPath;
                        newPath = malloc(sizeof(char) * (strlen(path[k]) + 1));
                        strcpy(newPath, path[k]);
                        //printf("new path: %s\n", newPath);
                        strncat(newPath, "/", 2);
                        printf("new path: %s\n", newPath);
                        strncat(newPath, commands[i][0], lengthOfCommand);
                        printf("new path: %s\n", newPath);
                        // printf("next path is %s\n", path[k+1]);
                        
                        //search for file in path
                        if(access(newPath, X_OK) == 0)
                        {
                            //printf("accessable path\n");
                            execv(newPath, commands[i]);
                            complete = 0;
                            break;
                        }
                        // else{
                        //     printf("not found\n");
                        // }
                        k++;
                        // printf("next path is %s\n", path[k]);
                    }

                    if(complete != 0)
                    {
                        char error_message[30] = "An error has occurred\n";
                        write(STDERR_FILENO, error_message, strlen(error_message));
                    }
                    return 0;
                }

                
            }
            i++;
        }

        // printf("so far so good\n Process id is %d\n",getpid());
        //TODO wait for all child processes to complete
        
        if(ret > 0) //parent wait for all child processes
        {
            for(int q = numFork; q > 0; q--)
            {
                wait(NULL);
            }
            //printf("Parent process %d complete. \n", i);
        }
        // printf("at the end of loop PID: %d\n", getpid());
        
    }

}
