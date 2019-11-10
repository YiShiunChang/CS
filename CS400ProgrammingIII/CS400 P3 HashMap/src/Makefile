#
# SAMPLE FROM:
# https://www.cs.swarthmore.edu/~newhall/unixhelp/javamakefiles.html
#

JFLAGS = -g
JRE = java
JC = javac
CP = .:/usr/share/java/junit4.jar

.SUFFIXES: .java .class

#
# compiles .java source into .class files
#
.java.class:
	$(JC) $(JFLAGS) -cp $(CP) $*.java


CLASSES = \
	HashTableADT.java \
	HashTable.java \
	TestHashTable.java \
	Profile.java \

default: classes

classes: $(CLASSES:.java=.class)

#
# make clean - removes all class files and permit new build of all
#
clean:
	$(RM) *.class

#
# make profile - runs Profile driver program
#
profile:
	java Profile 100

#
# make junit -  run the JUnit test class
#
junit4:
	javac -cp .:/usr/share/java/junit4.jar *.java
	java -cp .:/usr/share/java/junit4.jar org.junit.runner.JUnitCore TestHashTable

junit5:
	javac -cp junit-platform-console-standalone-1.3.1.jar *.java
	java -jar junit-platform-console-standalone-1.3.1.jar -f TestHashTable

#
# make get_files - use scp to copy files FROM CS400                TO your local directory
# make put_files - use scp to copy files FROM your local directory TO your CS account directory
#
# CAUTION: this will overwrite existing files with same name in same directory 
#          without warning or chance to cancel copy
# 
# TO USE:
#          remove echo from start of scp command
#          replace deppeler with your CS login username
#          replace /d/e/ with first and second characters of your CS login username
#
get_files:
	echo scp -r deppeler@best-linux.cs.wisc.edu:/p/course/cs400-deppeler/public/html-s/assignments/p3/files/* .

put_files:
	echo scp -r *.java deppeler@best-linux.cs.wisc.edu:/u/d/e/deppeler/private/cs400/p3/
