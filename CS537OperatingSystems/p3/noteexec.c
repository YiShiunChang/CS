#include "types.h"
#include "param.h"
#include "mmu.h"
#include "proc.h"
#include "defs.h"
#include "x86.h"
#include "elf.h"

/**
 * return -1 when the physical location of a path name doesn't exist
 * 
 * 
 * Note. 
 * ELF is a common standard file format for executable files, object code, shared libraries, and core dumps.
 * ELF file is made up of one ELF file header, which are followed by file data. The data can include:
 * 1. program header table, describing zero or more memory segments, 
 *    and telling the system how to create a process image. 
 *    program header table is found at file offset e_phoff (program header offset), 
 *    and consists of e_phnum (program header number) entries, each with size e_phentsize.
 * 2. section header table, describing zero or more sections
 * 3. data referred to by entries in the program header table or section header table
 * 
 * the segments contain information that is needed for run time execution of the file, 
 * while sections contain important data for linking and relocation.
 * 
 * the file header contains three fields that are affected by this setting, 
 * and file header offset other fields that follow them (the three fields).
 */
int exec(char *path, char **argv) {
  char *s, *last;
  int i, off;
  uint argc, sz, sp, ustack[3+MAXARG+1];
  // inode is an index node that stores the attributes and disk block location(s) of an object, ex. file
  // struct inode is defined in file.h
  struct inode *ip;
  // struct elfhdr is defined in file.h, and it is the "file headr" of ELF (Extensible Linking Format)
  struct elfhdr elf;
  // struct proghdr "ph" is defined in elf.h, and it is "program header" and "section header"
  struct proghdr ph;
  // typedef uint pde_t is defined in types.h, pde_t is used to represent page directory
  pde_t *pgdir, *oldpgdir;

  // namei() is defined in fs.c, it looks up and returns the inode for a path name
  // namei() returns the physical location of a path name if success, otherwise, 0
  if((ip = namei(path)) == 0) return -1;

  ilock(ip); // lock the given inode 
  pgdir = 0; // set page directory to 0

  // int readi(struct inode *ip, char *dst, uint off, uint n) is defined in fs.c, and readi()
  // reads data from inode and save to dst and returns sizeof(elf) while success, otherwise, -1

  // read ip and save to ELF's file header, this action is like checking ELF header
  if(readi(ip, (char*)&elf, 0, sizeof(elf)) < sizeof(elf))
    goto bad;

  // ELF_MAGIC are defined in file.h, and #define ELF_MAGIC 0x464C457FU 
  if(elf.magic != ELF_MAGIC)
    goto bad;

  // setupkvm(), setup kernel virtual memory, is defined in vm.c
  // setupkvm() sets up the kernel part of a page table, and returns a page directory or 0
  // it is like initialize a page table for a whole new process initproc
  // note. the virtual address of every process is divided into kernel space and user space. 
  // however, both spaces have its own table. 
  // when process switch, kernel table pointer is constant, but user table pointer is changed
  // I think kernel table is constant and shared within all processes

  // if kernel memory = 0, means out of memory
  if((pgdir = setupkvm()) == 0)
    goto bad;

  // cprintf("elf file header pgdir: %d\n", pgdir);

  // we are finished with ELF's file header, therefore, 
  // we are going to finish ELF's "program header" and "section header"
  // file header is stored in "elf", and program/section header are stored in "ph"
  sz = 0;
  // let us recall that:
  // program header table is found at file offset e_phoff (program header offset), 
  // and consists of e_phnum (program header number) entries, each with size e_phentsize.
  // in this for loop, it is like that our program header table is starting from off=elf.phoff
  // and there are elf.phnum of entries, where each entry size is sizeof(ph)
  for(i=0, off=elf.phoff; i<elf.phnum; i++, off+=sizeof(ph)){
    if(readi(ip, (char*)&ph, off, sizeof(ph)) != sizeof(ph))
      goto bad;
    // #define ELF_PROG_LOAD 1 is defined in elf.h,
    // and it is "Values for Proghdr(prgram section header) type" 
    if(ph.type != ELF_PROG_LOAD)
      continue;
    // memsz should be size in bytes of the segment in memory. May be 0.
    // filesz should be size in bytes of the segment in the file image. May be 0.
    if(ph.memsz < ph.filesz)
      goto bad;
    // allocuvm(pde_t *pgdir, uint oldsz, uint newsz) allocates page tables and physical memory to 
    // grow process from oldsz to newsz. returns new size or 0 on error. It is defined in vm.c.
    // va should be virtual address of the segment in memory
    if((sz = allocuvm(pgdir, sz, ph.va + ph.memsz)) == 0)
      goto bad;
    // load a program segment into pgdir. addr must be page-aligned
    // and the pages from addr to addr+sz must already be mapped.
    if(loaduvm(pgdir, (char*)ph.va, ip, ph.offset, ph.filesz) < 0)
      goto bad;
  }
  iunlockput(ip);
  ip = 0;
  // cprintf("program header pgdir: %d\n", pgdir);

  // ==============================================================================================
  // now, we have set up "headers" for our user address
  // ==============================================================================================

  // allocate a one-page stack at the next page boundary
  // PGROUNDUP and PGROUNDDOWN are macros to round the address sent to a multiple of the PGSIZE. 
  // ex. if we already use size 6672 to store our headers, since it needs two pages = 8192 to 
  //     contain 6672, our one-page stack will ends at 12288 (4096*3)
  sz = PGROUNDUP(sz);
  if((sz = allocuvm(pgdir, sz, sz + PGSIZE)) == 0)
    goto bad;

  // Push argument strings, prepare rest of stack in ustack.
  sp = sz;
  for(argc = 0; argv[argc]; argc++) {
    // #define MAXARG 32 max exec arguments is defined in include/param.h
    if(argc >= MAXARG)
      goto bad;
    // let's assume that sp = sz = 12288, and strlen(argv[argc] = ls) = 2
    // we get our sp = 12288 - 2 + 1 = 12287
    sp -= strlen(argv[argc]) + 1;
    // this is a bit wise operation:
    // 12287 =  (10111111111111) => (10111111111111) &=
    //    ~3 = ~(11)             => (            00)
    //                           => (10111111111100) = 12284
    sp &= ~3;
    // copyout(pde_t *pgdir, uint va, void *p, uint len) copies len bytes from p to user address va 
    // in page table pgdir. Most useful when pgdir is not the current page table.
    // it's copying argv[argc] to the user address which starts from pgdir 
    if(copyout(pgdir, sp, argv[argc], strlen(argv[argc]) + 1) < 0)
      goto bad;
    // uint ustack[3+MAXARG+1] is set at the beginning, and it is ustack[36]
    // ustack[36] should means that there are 36 userstacks
    // continuing our example, ustack[3+argc] = ustack[3] = 12284
    ustack[3+argc] = sp;
  }
  ustack[3+argc] = 0;           // if argc is 1, then ustack[3+1] = ustack[4] = 0
  ustack[0] = 0xffffffff;       // fake return PC
  ustack[1] = argc;             // this stack stores "argc"
  ustack[2] = sp - (argc+1)*4;  // this stack stores "argv pointer"
                                // sp - (argc+1)*4 = 12284 - (1+1)*4 = 12276
  // now, we find out that our stack starts at the end of a new page, and it grows up
  // we always use first 3 user stacks to store "fake return pc", "argc", and "argv pointer"
  // and if there is only 1 argv like our example, then ustack[3] = sp while ustack[4] = 0
  // therefore, we can imagine that when there are 2 argv like "wc abc", we get
  // usatck[3] = sp = 12284, 
  // ustack[4] = 12283 &= ~3 = 12280
  // ustack[5] = 0

  // continuing example with argc = 1: 12284 - (3+1+1)*4 = 12284 - 20 = 12264
  // 3+argc+1 is the number of stack that we have used currently
  // if argc = 1, then we use 4 user stacks, while argc = 2 means using 5 user stacks
  sp -= (3+argc+1) * 4; 
  // uint is 4byte, so we copy "number of used stacks" * "4bytes" from ustack to user address 
  if(copyout(pgdir, sp, ustack, (3+argc+1)*4) < 0)
    goto bad;

  // ==============================================================================================
  // now, our user addrees which starts from pgdir has been set up with "headers" and "stack"
  // ==============================================================================================

  // save program name for debugging
  // by iterates though each char in path, we get the program name and set it to process->name
  // ex. "path = /bin/ls" => "last = ls"
  for(last=s=path; *s; s++)
    if(*s == '/')
      last = s+1;
  safestrcpy(proc->name, last, sizeof(proc->name));

  // commit to the user image
  // elf.entry is the memory address of the entry point from where the process starts executing
  // an entry point is where the first instructions in a program are executed,
  // and where the program has access to command line arguments.
  oldpgdir = proc->pgdir;
  proc->pgdir = pgdir;
  proc->sz = sz;
  proc->tf->eip = elf.entry;  // main
  proc->tf->esp = sp;
  // since we construct our user process, so we can switch to user virtual memory to run?
  switchuvm(proc);
  freevm(oldpgdir);

  return 0;

  // when something bad happened, like setupkvm() fail, share headers fail, read inode fail
  // we release page table "pgdir" if it exists, we unlock inode "ip" if it is locked, then retrun -1
 bad:
  if(pgdir)
    freevm(pgdir);
  if(ip)
    iunlockput(ip);
  return -1;
}
