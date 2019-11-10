#include <stdio.h>
#include <stdlib.h> 
#include <string.h>
#include <errno.h>
#include <stdbool.h> 

int main(int argc, char *argv[]) {

	// oldWord and replaceWprd are not fully specified
  	if (argc < 3) {
		printf("my-sed: find_term replace_term [file ...]\n");
    	exit(1); // unsucessful termination exit
  	}	
	
	// get the old word and the replace word and their lengths
	char *oldW = argv[1];
	char *replaceW = argv[2];
	// lengths are used for future work of building answer string
	int oWLen = strlen(oldW);
	int rWLen = strlen(replaceW);

	// ask for stdin when there is no input file
	if (argc == 3) {
		char line[1000];
		while (fgets(line, 1000, stdin)) {
			// pointer to the first character of a matching oldW 
			// that is found in the string line
			char *oFound = strstr(line, oldW); 
			
			if (oFound != NULL) {
				// the position of the first matching old word 
				// line is already pointing to the first character of itself
				int oPosition = (int) (oFound - line); 
				
				// making new string of enough length     			
				char *result = (char *) malloc(strlen(line) - oWLen + rWLen); 
  
    			int oIndex = 0; 
				int nIndex = 0;
				bool visitOldWord = false;
				// line[oIndex] return char in "oIndex"th index of line
   				while (line[oIndex]) { 
  					if (oIndex == oPosition && !visitOldWord) { 
						if (rWLen == 0) visitOldWord = true;
            			else {
							strcpy(&result[nIndex], replaceW);
							nIndex += rWLen;
						} 
            
						oIndex += oWLen; 
       				} 
       	 			else {
						result[nIndex] = line[oIndex];
						oIndex++;
						nIndex++;
					}
    			} 
				
				printf("%s", result);
			}
			else printf("%s", line);
		}
	}

	// read files
	for (int i = 3; i < argc; i++) {
		// read a file
		FILE *stream = fopen(argv[i], "r");

		// if there is no such file
		if (stream == NULL) {
			if (errno == 2) printf("my-sed: cannot open file\n");
      		else printf("Error: %s\n", strerror(errno));
			
      		exit(1); // error exit
    	}
		
		// read each line in a file
		char *line = NULL;
   		size_t len = 0;
		ssize_t nread;

		// getline return -1 on failure to read a line
   		while ((nread = getline(&line, &len, stream)) != -1) {
			// pointer to the first character found in the string sentence
			char *oFound = strstr(line, oldW); 

			// if oldW is in a line
			if (oFound != NULL) {
				// line is already pointing to the first string character of itself
				int oPosition = (int) (oFound - line); 
				
				// making new string of enough length 
    			char *result = (char *) malloc(strlen(line) - oWLen + rWLen); 
  
    			int i = 0; 
				bool visitOldWord = false;
    			while (*line) { 
  					if (i == oPosition && !visitOldWord) { 
						if (rWLen == 0) visitOldWord = true;
      	   				else {
							strcpy(&result[i], replaceW);
							i += rWLen;
						} 

           				line += oWLen; 
      				} 
       	 			else {
						result[i] = *line;
						i++;
						line++;
					}
   				} 
				
				result[i] = '\0'; 
				printf("%s", result);
			}
			else printf("%s", line);
			
			*&line = NULL;
   		}

   		free(line);
    	fclose(stream);
	}
	
	exit(0);	
}