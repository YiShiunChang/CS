#include "types.h"
#include "stat.h"
#include "user.h"
#include "fs.h"
#include "fcntl.h"
#include "syscall.h"
#include "traps.h"

int main(int argc, char *argv[])
{
  int k = atoi(argv[1]);
  printf(1,"hello %d\n", k);

  int initCount = getopenedcount();
  
  for(int i = 0; i < k; i++) {
    open("emptyfile", 1);
  }  
  int afterCount = getopenedcount();

  printf(1, "%d\n", afterCount-initCount); 

  exit();
}
