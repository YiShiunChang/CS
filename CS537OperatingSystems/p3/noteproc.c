#include "types.h"
#include "defs.h"
#include "param.h"
#include "mmu.h"
#include "x86.h"
#include "proc.h"
#include "spinlock.h"
#include "pstat.h"

/**
 * This is the struct for process table, not page table
 */
struct {
  // spinlock is defined in spinlock.h, uint locked; unsigned int locked;
  struct spinlock lock;
  // include/param.h: #define NPROC 64, which is maximum number of processes
  // proc is defined in proc.h, proc is data structure of process
  struct proc proc[NPROC]; 
} ptable;

static struct proc *initproc; // initialprocess is used in userinit() and exit()
int nextpid = 1; // first process ID is 1
extern void forkret(void); // forkreturn
extern void trapret(void); // trapreturn, which is defined in trapasm.S, assembly code
static void wakeup1(void *chan); // wake up a specific process 

/**
 * Initialize a lock, which is called ptable
 * check spinlock.h to glimpse at the struct of spinlock
 * check spinlock.c to have an idea of what initlock() is doing
 */
void pinit(void) {
  // we have a lock in the ptable, and we are going to initialize it
  // we give that lock a name "ptable" and the cpu that holds the lock "ptable" is 0
  initlock(&ptable.lock, "ptable");
}

/**
 * A fork child's very first scheduling by scheduler() will swtch here. 
 * "Return" to user space
 */
void forkret(void) {
  // still holding ptable.lock from scheduler
  release(&ptable.lock);
  // return to "caller", actually trapret (see allocproc in proc.c)
}

/**
 * Set up first user process so it can be runnable
 * the process transform from ?? to RUNNABLE 
 * 
 * 1. we first allocate 4096byte, a physical memory to this process by allocproc()
 *    context, kstack, and pid are initialized in function allocproc()
 * 2. then we set this process as our global proc initproc in proc.c
 * 3. we set up the pagetable of this process by setupkvm
 * 4. we set up the kernel address space and user address space of this process 
 * 5. we set up name, cwd, tf, and state of this process
 * 6. now this process is runnable
 */
void userinit(void) {
  struct proc *p;
  extern char _binary_initcode_start[], _binary_initcode_size[];
  
  // allocproc returns p as a process which can be run in kernel, 
  // or 0 when there is no unused process in ptable
  // allocproc is defined in proc.c
  p = allocproc();

  // lock this process table by acquiring &ptable.lock
  acquire(&ptable.lock);
  // set initial process to p, since initproc is a static variable in proc.c
  // we can find out that initproc is the root process, or the first process, 
  // or the main process, or the mother process of all children process
  initproc = p; 

  // ==============================================================================================
  // now, we have initialized our first process, and we can finally run something by using it
  // but before we run it, we have to build it, that is, give it address space
  // ==============================================================================================

  // if kernel memory = 0, means out of memory
  // setupkvm(), setup kernel virtual memory, is defined in vm.c
  // setupkvm() sets up the kernel part of a page table, and returns a page directory or 0
  // it is like initialize a page table for a whole new process initproc
  // note. the virtual address of every process is divided into kernel space and user space. 
  // however, both spaces have its own table. 
  // when process switch, kernel table pointer is constant, but user table pointer is changed
  // I think kernel table is constant and shared within all processes
  if ((p->pgdir = setupkvm()) == 0) panic("userinit: out of memory?");

  // ==============================================================================================
  // now, we give out first process a virtual address space after mapping virtual address to 
  // physical address, so remember that pgdir here is a virtual address.
  // also notes that we only build kernel address space now, so we have to build user address space
  // ==============================================================================================

  // load the initcode into address 0 of pgdir, and sz(size) must be less than a page.
  // void inituvm(pde_t *pgdir, char *init, uint sz)
  // it is like adding the code of this process into its virtual memory
  inituvm(p->pgdir, _binary_initcode_start, (int)_binary_initcode_size);

  // ==============================================================================================
  // now, we initialize user address space, which is also virtual but mapping to physical, and we
  // add the initial code into the user address space
  // ==============================================================================================

  // size of this process = PGSIZE
  p->sz = PGSIZE;
  // memset(void *dst, int c, uint n) is defined in string.c
  // it calls stosb(dst, c, n), which is included in x86
  // stosb(dst, c, n) write data c into destination dst with n bytes
  // p->tf means trapframe of a process p, and trapframe is defined in include/x86.h
  // trapframe is composed of a bunch of registers
  memset(p->tf, 0, sizeof(*p->tf));

  // what I know about register is like:
  // 1. registers are phyical memory unit that are closest to the cpu, so they are fast
  // 2. registers are used to store data, like int
  // 3. if we are going to simulate registers, it is like what inside trapframe, they 
  //    are a bunch of int variables, each variable is like a register

  // what I know about task, context, and trapframe
  // 1. first we have to understand "the cpu state" - it is about context switch, 
  //    while trapframe holds userspace state saved in tcb(task control block) 
  //    after exception or irq(interrupt request) have arised
  // 2. a task struct stores information like pid, context and trap frame
  // 3. the context is a set of callee-saved registers, which represents state of the task 
  //    before it was preempted by other task (or say before context switch)
  // 4. when the context switch in scheduler is occured, current task saves its registers to 
  //    its context, and new set of registers are loaded from next task
  // 5. let's recall, context is a struct that is saved in task struct
  // 6. then what is trapframe? it turns out that trapframe is also a struct saved in task
  // 7. however, trapframe stores register set which was saved during exception have arised, 
  //    so using trapframe we can return back and proceed execution (when exception or irq will be handled)
  // 8. it can be said that trapframe is used when we don't want to do context switch
  

  // set a bunch of registers to help initialize a process
  p->tf->cs = (SEG_UCODE << 3) | DPL_USER;
  p->tf->ds = (SEG_UDATA << 3) | DPL_USER;
  p->tf->es = p->tf->ds;
  p->tf->ss = p->tf->ds;
  p->tf->eflags = FL_IF;
  p->tf->esp = PGSIZE;
  p->tf->eip = 0;  // beginning of initcode.S

  // set the process name to "initcode", safestrcpy is defined in string.c
  // safestrcpy is like strncpy but guaranteed to NUL-terminate
  safestrcpy(p->name, "initcode", sizeof(p->name));
  // cwd = current working directory
  // namei() is defined in fs.c, it looks up and returns the inode for a path name
  // inode is an index node that stores the attributes and disk block location(s) of an object, ex. file
  // as a result, this line is like finding the physical location of a path name
  // and set it to the process
  p->cwd = namei("/");
  // after so many settings for a process, this process is finally can be run
  p->state = RUNNABLE;

  // to do: before we run, we should set up its priority

  release(&ptable.lock);
}

/**
 * This function is called when we initialize our first user process, or when we fork
 * It looks in the process table for an UNUSED proc.
 * If found, change state to EMBRYO and initialize state required to run in the kernel.
 * Otherwise return 0.
 * 
 * this function is like preparing a process in memory, so the empty process can be 
 * transformed from UNUSED to EMBRYO
 * 
 * we prepare a 4096-byte page of physical memory for the process 
 * we set up its context, so it becomes EMBRYO
 */
static struct proc* allocproc(void) {
  struct proc *p;
  char *sp;

  // lock this process table by acquiring &ptable.lock
  acquire(&ptable.lock);
  // iterates through the process table to find any UNUSED process
  // there are NPROC=64 UNUSED processes can be used in a process table at the beginning
  for (p = ptable.proc; p < &ptable.proc[NPROC]; p++)
    if (p->state == UNUSED) goto found; // like continue, but we only skip from line 144 to 146
  release(&ptable.lock);
  return 0;

found: 
  // we are going to use this process, so set its state to EMBRYO, and give it an ID
  p->state = EMBRYO;
  p->pid = nextpid++;
  release(&ptable.lock);

  // allocate kernel stack if possible.
  // kalloc() is defined in kalloc.c and it allocates one 4096-byte page of physical memory.
  // returns a pointer that the kernel can use, or 0 if the memory cannot be allocated.
  if ((p->kstack = kalloc()) == 0) {
    p->state = UNUSED;
    return 0;
  }

  // Note. kalloc() is defined in and it used struct run, which is also defined in kalloc.c,
  // to give a pointer to p->kstack. That pointer points to the beginning of an address space,
  // and that address space is 4096 bytes (don't know how it is set to be 4096 )

  // Note. different OS has different structure about kernel stack and user stack
  // however, below is a way to understand kernel stack
  // just like there has to be a separate place for each process to hold its set of saved registers 
  // (in its process table entry), each process also needs its own kernel stack, 
  // to work as its execution stack when it is executing in the kernel.
  // For example, if a process is doing a read syscall, it is executing the kernel code for read, 
  // and needs a stack to do this. It could block on user input, and give up the CPU, 
  // but that whole execution environment held on the stack has to be saved for its later use.  
  // Another process could run meanwhile and do its own syscall, 
  // and then it needs its own kernel stack, separate from that blocked reader’s stack, to support its own kernel execution.

  // since our p->kstack is a pointer that points to the beginning of an 4096byte address space
  // and since KSTACKSIZE = 4096, which is defined in include/param.h
  // by doing sp = p->kstack + KSTACKSIZE, we set the sp as a pointer that points to the end of the
  // 4096bytes address space
  sp = p->kstack + KSTACKSIZE;

  // Leave room for trap frame, it is like 4096 - size of "tf",
  // so the pointer sp is back to some point in the address space
  // ex. if sizeof *p->tf is 1000, then sp is back to 3096
  sp -= sizeof *p->tf;
  // since sp points to some point at address space, let's assume 3096
  // then we are going to transform the address space from 3096 to 4096 into trapframe
  // and assign it to p->tf, thus, the trapframe of p is initialized
  p->tf = (struct trapframe*) sp;
  // Set up new context to start executing at forkret, which returns to trapret.
  sp -= 4;
  *(uint*)sp = (uint)trapret;
  sp -= sizeof *p->context;
  // p->context is a bunch of registers, and these registers are used in kernel when running
  // so, I guess these registers are main information that being used when running the process
  // so, they are called context
  p->context = (struct context*)sp;
  // memset(void *dst, int c, uint n) is defined in string.c
  // it calls stosb(dst, c, n), which is included in x86
  // stosb(dst, c, n) write data c into destination dst with n bytes
  // so it is like we write some data to p->context
  memset(p->context, 0, sizeof *p->context);
  p->context->eip = (uint)forkret;

  return p;
}

/**
 * Grow current user process's memory by n bytes.
 * Return 0 on success, -1 on failure.
 */
int growproc(int n) {
  uint sz;
  
  // int allocuvm(pde_t *pgdir, uint oldsz, uint newsz) is defined in vm.c
  // allocate page tables and physical memory to grow user process from oldsz to
  // newsz, which need not be page aligned.  Returns new size or 0 on error.
  sz = proc->sz;
  if(n > 0){
    if((sz = allocuvm(proc->pgdir, sz, sz + n)) == 0)
      return -1;
  } else if(n < 0){
    if((sz = deallocuvm(proc->pgdir, sz, sz + n)) == 0)
      return -1;
  }
  proc->sz = sz;
  // switch to user virtual memory, switchuvm() is definied in vm.c
  switchuvm(proc);
  return 0;
}

/**
 * Create a new process by copying p as the parent.
 * Sets up stack to return as if from system call.
 * Caller must set state of returned proc to RUNNABLE.
 */
int fork(void) {
  int i, pid;
  struct proc *np;

  // allocate process
  // allocproc() loops in the process table for an UNUSED proc 
  // if found, change state to EMBRYO and initialize state required to run in the kernel
  // otherwise return 0 (assign a 4096byte page for the proc when initialize it)
  if((np = allocproc()) == 0) return -1;

  // now we have a new process np, which is initialized, since it is a child process
  // we are going to copy process state from parent 
  // pde_t *copyuvm(pde_t *pgdir, uint sz) is given a parent process's page table, 
  // create a copy of it for a child. pgdir is a pagetable
  // return pagetable if success or 0 if fail 
  if((np->pgdir = copyuvm(proc->pgdir, proc->sz)) == 0){
    kfree(np->kstack);
    np->kstack = 0;
    np->state = UNUSED;
    return -1;
  }
  
  // set child process np parameters based on parent process proc
  np->sz = proc->sz;
  np->parent = proc;
  *np->tf = *proc->tf;
  // clear %eax so that fork returns 0 in the child
  // recall assignment p2a, when fork(), child is 0, parent is not 0
  np->tf->eax = 0; 

  // below is like copy the files that parent are used to child 
  // struct file is defined in file.h
  for (i = 0; i < NOFILE; i++)
    if (proc->ofile[i])
      np->ofile[i] = filedup(proc->ofile[i]);
  // below is like copy the current working directory that parent is used to child 
  np->cwd = idup(proc->cwd);
  // set child process np parameters
  pid = np->pid;
  np->state = RUNNABLE;
  safestrcpy(np->name, proc->name, sizeof(proc->name));
  return pid;
}

/**
 * Wait for a child process to exit and return its pid.
 * Return -1 if this process has no children.
 * 
 * When a parent process "proc" is calling wait(), it keeps running in the for loop
 * until all its child processes are transformed from ZOMBIE to UNUSED
 * or the "proc" itsefl is killed
 * 
 * If there is still a child process is not in ZOMBIE, 
 * the parent process keep sleeping until one of its child wakes it up
 */
int wait(void) {
  struct proc *p;
  int havekids, pid;

  acquire(&ptable.lock);
  for(;;){ // infinite loop 
    // Scan through table looking for zombie children.
    havekids = 0;
    for (p = ptable.proc; p < &ptable.proc[NPROC]; p++) {
      if (p->parent != proc) continue;
      havekids = 1;
      if (p->state == ZOMBIE) {
        // Found one.
        pid = p->pid;
        kfree(p->kstack);
        p->kstack = 0;
        freevm(p->pgdir);
        p->state = UNUSED;
        p->pid = 0;
        p->parent = 0;
        p->name[0] = 0;
        p->killed = 0;
        release(&ptable.lock);
        return pid;
      }
    }

    // No point waiting if we don't have any children.
    if (!havekids || proc->killed) {
      release(&ptable.lock);
      return -1;
    }

    // Wait for children to exit.  (See wakeup1 call in proc_exit.)
    sleep(proc, &ptable.lock);  //DOC: wait-sleep
  }
}

/**
 * Exit the current process. Does not return.
 * An exited process remains in the zombie state
 * until its parent calls wait() to find out it exited and transform it to UNUSED
 */
void exit(void) {
  struct proc *p;
  int fd;

  // panic is defined in console.c
  // what panic does is like 
  // 1. call cli(), which could be command line interface
  // 2. print out many warnings
  // 3. free other cpus
  if (proc == initproc) panic("init exiting");

  // Close all open files.
  for (fd = 0; fd < NOFILE; fd++) {
    if (proc->ofile[fd]) {
      fileclose(proc->ofile[fd]);
      proc->ofile[fd] = 0;
    }
  }

  iput(proc->cwd);
  proc->cwd = 0;
  acquire(&ptable.lock);
  // Parent might be sleeping in wait().
  wakeup1(proc->parent);
  // Pass abandoned children to init.
  for (p = ptable.proc; p < &ptable.proc[NPROC]; p++) {
    if (p->parent == proc) {
      p->parent = initproc;
      if (p->state == ZOMBIE)
        wakeup1(initproc);
    }
  }

  // Jump into the scheduler, never to return.
  proc->state = ZOMBIE;
  // what sched does is like calling a panic if conditions are satisfied
  // and then give back the control from process to sheduler
  sched();
  panic("zombie exit");
}

/**
 * Wake up all processes sleeping on chan.
 * The ptable lock must be held, which means there must to be a process table
 */
static void wakeup1(void *chan) {
  struct proc *p;
  // iterates the page table to check each process, 
  // if that process is SLEEPING and is "chan" process, wake it to RUNNABLE
  for (p = ptable.proc; p < &ptable.proc[NPROC]; p++)
    if (p->state == SLEEPING && p->chan == chan)
      p->state = RUNNABLE;
}

/**
 * Enter scheduler
 * Must hold only ptable.lock and have changed proc->state
 */
void sched(void) {
  int intena;

  if (!holding(&ptable.lock))
    panic("sched ptable.lock");
  if (cpu->ncli != 1)
    panic("sched locks");
  if (proc->state == RUNNING)
    panic("sched running");
  if (readeflags()&FL_IF)
    panic("sched interruptible");
  intena = cpu->intena;
  swtch(&proc->context, cpu->scheduler);
  cpu->intena = intena;
}

// Per-CPU process scheduler.
// Each CPU calls scheduler() after setting itself up.
// Scheduler never returns.  It loops, doing:
//  - choose a process to run
//  - swtch to start running that process
//  - eventually that process transfers control via swtch back to the scheduler.
void scheduler(void) {
  struct proc *p;

  for(;;){ // for(;;) means infinite loop
    sti(); // Enable interrupts on this processor.

    acquire(&ptable.lock); // this line locks the below lines
    // this for loop iterates a page table, and runs procs that are runnable
    for (p = ptable.proc; p < &ptable.proc[NPROC]; p++) {
      // skip whole loop if proc p is not runnable
      if(p->state != RUNNABLE) continue;

      // Switch to chosen process. 
      // It is the process's job to release ptable.lock and then reacquire it before jumping back to us.
      proc = p;
      switchuvm(p);
      p->state = RUNNING;
      swtch(&cpu->scheduler, proc->context);
      switchkvm();

      // Process is done running for now.
      // It should have changed its p->state before coming back.
      proc = 0;
    }


    release(&ptable.lock); // this line unlocks the lock

  }
}

/**
 * Give up the CPU for one scheduling round.
 */
void yield(void) {
  acquire(&ptable.lock);  //DOC: yieldlock
  proc->state = RUNNABLE;
  sched();
  release(&ptable.lock);
}

/**
 * Atomically release lock and sleep on chan.
 * Reacquires lock when awakened.
 */
void sleep(void *chan, struct spinlock *lk) {
  if (proc == 0)
    panic("sleep");

  if (lk == 0)
    panic("sleep without lk");

  // Must acquire ptable.lock in order to
  // change p->state and then call sched.
  // Once we hold ptable.lock, we can be
  // guaranteed that we won't miss any wakeup
  // (wakeup runs with ptable.lock locked),
  // so it's okay to release lk.
  if (lk != &ptable.lock) {  //DOC: sleeplock0
    acquire(&ptable.lock);  //DOC: sleeplock1
    release(lk);
  }

  // Go to sleep.
  proc->chan = chan;
  proc->state = SLEEPING;
  sched();

  // Tidy up.
  proc->chan = 0;

  // Reacquire original lock.
  if (lk != &ptable.lock) {  //DOC: sleeplock2
    release(&ptable.lock);
    acquire(lk);
  }
}

/**
 * Wake up all processes sleeping on chan.
 */
void wakeup(void *chan) {
  acquire(&ptable.lock);
  wakeup1(chan);
  release(&ptable.lock);
}

/**
 * Kill the process with the given pid.
 * Process won't exit until it returns to user space (see trap in trap.c).
 */
int kill(int pid) {
  struct proc *p;

  acquire(&ptable.lock);
  for (p = ptable.proc; p < &ptable.proc[NPROC]; p++){
    if (p->pid == pid) {
      p->killed = 1;
      // Wake process from sleep if necessary.
      if (p->state == SLEEPING)
        p->state = RUNNABLE;
      release(&ptable.lock);
      return 0;
    }
  }
  release(&ptable.lock);
  return -1;
}

/**
 * Print a process listing to console.  For debugging.
 * Runs when user types ^P on console.
 * No lock to avoid wedging a stuck machine further.
 */
void procdump(void) {
  static char *states[] = {
  [UNUSED]    "unused",
  [EMBRYO]    "embryo",
  [SLEEPING]  "sleep ",
  [RUNNABLE]  "runble",
  [RUNNING]   "run   ",
  [ZOMBIE]    "zombie"
  };
  int i;
  struct proc *p;
  char *state;
  uint pc[10];
  
  for (p = ptable.proc; p < &ptable.proc[NPROC]; p++){
    if(p->state == UNUSED)
      continue;
    if(p->state >= 0 && p->state < NELEM(states) && states[p->state])
      state = states[p->state];
    else
      state = "???";
    cprintf("%d %s %s", p->pid, state, p->name);
    if(p->state == SLEEPING){
      getcallerpcs((uint*)p->context->ebp+2, pc);
      for(i=0; i<10 && pc[i] != 0; i++)
        cprintf(" %p", pc[i]);
    }
    cprintf("\n");
  }
}


