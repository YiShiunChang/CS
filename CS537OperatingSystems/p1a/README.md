The project consists of three files: my-cat.c, my-sed.c, and my-uniq.c.
All files function indepently.

my.cat reads a file as specified by the user and prints its contents.  
ex. ./my-cat filename

my.sed will only be used to find and replace it with the exact given string. It will find the first instance of a string in a line and substitute it with another. It will print the output to standard output. Instances following the first instance remain as is.
ex. ./my-sed foo bar baz.txt qux.txt

my-uniq finds out unique lines by only comparing them only with their adjacent lines, and prints them.
ex. ./my-sed foo "" bar.txt