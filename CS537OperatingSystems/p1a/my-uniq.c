#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(int argc, char *argv[]) {
	
	// read stdin
	if (argc < 2) {
		char *stringarray[1000]; 
		char buffer[1000];
		char previous[1000];
		
		int stored = 0;
		while ( stored < 1000 && fgets(buffer, 1000, stdin)) {
			if (strcmp(previous, buffer) != 0) {
				stringarray[stored] = strndup(buffer, 1000);
				strcpy(previous, buffer);
				stored++;
			}
		}
		
		for (int i = 0; i < stored; i++) {
   			printf("%s", stringarray[i]); 
		}
	}
	
	// read input file
	if (argc >= 2) {
		for(int i = 1; i < argc; i++) {
			// read a file
			FILE *stream = fopen(argv[i], "r");

			// if there is no such file
			if (stream == NULL) {
				printf("my-uniq: cannot open file\n");
      			exit(1); // error exit
    		}

			// read each line in a file
			char *line = NULL;
   			size_t len = 0;
			ssize_t nread;
			char *previous = NULL;
			int count = 0;

			// getline return -1 on failure to read a line
    		while ((nread = getline(&line, &len, stream)) != -1) {
				if (count == 0) {
					previous = (char *) malloc(strlen(line)); 
					strcpy(previous, line);
					printf("%s", line);
					count ++;
				}
				else {
					char buffer[strlen(line)];
					strcpy(buffer, line);

					// if (buffer[strlen(buffer) - 1] == '\n') buffer[strlen(buffer) - 1] = '\0';
					if (strcmp(previous, buffer) != 0) {
						free(previous);
						previous = (char *) malloc(strlen(buffer));
						strcpy(previous, buffer);
						printf("%s", buffer);
					}
				}
				
				*&line = NULL;
			}

			// memset(&previous[0], 0, sizeof(previous));
			// free(previous);
			// free(line);
    		fclose(stream);
		}
		
		exit(0);
	}

}