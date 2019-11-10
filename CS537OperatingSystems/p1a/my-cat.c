#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>

int main(int argc, char *argv[]) {
    // no file is specified
    if (argc < 2) exit(0); // safe exit

    // one or more files
    for(int i = 1; i < argc; i++) { 
        // open returns a FILE pointer. Otherwise, NULL is returned
        // and the global variable errno is set to indicate the error
        FILE *fp = fopen(argv[i], "r");
      
        if (fp == NULL) {
            // char *strerror(int errnum) returns a pointer to the error string describing error
            if (errno == 2) printf("my-cat: cannot open file\n");
            else printf("Error: %s\n", strerror(errno));
            exit(1); // error exit
        }
      
        // build a string buffer with 1000 characters
        char buffer[1000];

        // char *fgets(char *str, int n, FILE *stream) reads a line from the specified stream 
        // and stores it into the string pointed to by str. 
        // It stops when either (n-1) characters are read, the newline character is read, 
        // or the end-of-file is reached, whichever comes first.
        // If the End-of-File is encountered and no characters have been read, 
        // the contents of str remain unchanged and a null pointer is returned.
        while(fgets(buffer, 1000, fp) != NULL) {
            printf("%s", buffer);
        }

        // int fclose(FILE *stream) closes the stream. All buffers are flushed.
        fclose(fp);
    }

    exit(0); // safe exit
}