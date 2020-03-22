# .o files are objects. They are the output of the compiler and input to the linker/librarian.
# .a files are archives. They are groups of objects or static libraries and are also input into the linker.
# -I. is included so that gcc will look in the current directory (.) for including .h file. 
# -c says not to run the linker. Then the output consists of object files output by the assembler.
CC = g++
CFLAGS = -std=c++11 -Wall -Werror
LIB = lib

# get badgerdb_main
all: main.o btree.o $(LIB)/bufmgr.a filescan.o 
	$(CC) $(CFLAGS) main.o btree.o filescan.o lib/bufmgr.a lib/exceptions.a -o badgerdb_main

# specify that ./lib/bufmgr.a is created based on ./lib/exceptions.a buffer.* file.* page.* bufHashTbl.*
$(LIB)/bufmgr.a: $(LIB)/exceptions.a buffer.* file.* page.* bufHashTbl.*
	$(CC) $(CFLAGS) -c -I. buffer.cpp file.cpp page.cpp bufHashTbl.cpp
	ar cq ./lib/bufmgr.a buffer.o file.o page.o bufHashTbl.o

# specify that ./lib/exceptions.a is created based on exceptions/*
# ar cq extracts the archived exceptions.a
# g++ -std=c++11 -Wall -Werror -c -I. ./exceptions/*.cpp
$(LIB)/exceptions.a: exceptions/*
	mkdir $(LIB)
	$(CC) $(CFLAGS) -c -I. exceptions/*.cpp
	ar cq ./lib/exceptions.a *.o

# specify that filescan.o is created based on filescan.*
# g++ -std=c++11 -Wall -Werror -c -I. filescan.cpp
filescan.o: filescan.*
	$(CC) $(CFLAGS) -c -I. filescan.cpp

# specify that main.o is created based on main.cpp
# g++ -std=c++11 -Wall -Werror -c -I. main.cpp
main.o: main.cpp
	$(CC) $(CFLAGS) -c -I. main.cpp

# specify that btree.o is created based on btree.*
# g++ -std=c++11 -Wall -Werror -c -I. btree.cpp
btree.o: btree.*  
	$(CC) $(CFLAGS) -c -I. btree.cpp


