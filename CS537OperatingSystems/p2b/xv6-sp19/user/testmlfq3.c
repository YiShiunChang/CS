#include "types.h"
#include "stat.h"
#include "user.h"

// type "testmlfq3 &; testmlfq3" in command line when using "make qemu-nox"

int
main(int argc, char *argv[])
{
  int x = 0;
  for (int i = 0; i < 1300000000; i++) {
    if (i % 10000000 == 0) printf(1, "\n i = %d", i);
    x += i;
  }

  exit();
}
