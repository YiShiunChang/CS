#include "types.h"
#include "defs.h"
#include "param.h"
#include "mmu.h"
#include "x86.h"
#include "proc.h"
#include "spinlock.h"
#include "pstat.h" // include pstat.h to get definition of pstat

struct {
  struct spinlock lock;
  struct proc proc[NPROC];
} ptable;

static struct proc *initproc;
int nextpid = 1;
extern void forkret(void);
extern void trapret(void);
static void wakeup1(void *chan);

void
pinit(void)
{
  initlock(&ptable.lock, "ptable");
}

// Look in the process table for an UNUSED proc.
// If found, change state to EMBRYO and initialize
// state required to run in the kernel.
// Otherwise return 0.
static struct proc*
allocproc(void)
{
  struct proc *p;
  char *sp;

  acquire(&ptable.lock);
  for(p = ptable.proc; p < &ptable.proc[NPROC]; p++)
    if(p->state == UNUSED)
      goto found;
  release(&ptable.lock);
  return 0;

found:
  p->state = EMBRYO;
  p->pid = nextpid++;
  release(&ptable.lock);

  // Allocate kernel stack if possible.
  if((p->kstack = kalloc()) == 0){
    p->state = UNUSED;
    return 0;
  }
  sp = p->kstack + KSTACKSIZE;
  
  // Leave room for trap frame.
  sp -= sizeof *p->tf;
  p->tf = (struct trapframe*)sp;
  
  // Set up new context to start executing at forkret,
  // which returns to trapret.
  sp -= 4;
  *(uint*)sp = (uint)trapret;
  sp -= sizeof *p->context;
  p->context = (struct context*)sp;
  memset(p->context, 0, sizeof *p->context);
  p->context->eip = (uint)forkret;

  // Set up MLFQ default variables
  p->priority = 3;       
  p->cur_ticks = 0;   
  for (int i=0; i<4; i++) {
    p->ticks[i] = 0;                  
    p->wait_ticks[i] = 0;   
  }      
             
  return p;
}

// Set up first user process.
void
userinit(void)
{
  struct proc *p;
  extern char _binary_initcode_start[], _binary_initcode_size[];
  
  // for(int i = 0; i < NPROC; i++) {
  //   ptable.proc[i].ticks = 0;
  //   ptable.proc[i].tickets = 1;
  // }

  p = allocproc();
  acquire(&ptable.lock);
  initproc = p;
  if((p->pgdir = setupkvm()) == 0)
    panic("userinit: out of memory?");
  inituvm(p->pgdir, _binary_initcode_start, (int)_binary_initcode_size);
  p->sz = PGSIZE;
  memset(p->tf, 0, sizeof(*p->tf));
  p->tf->cs = (SEG_UCODE << 3) | DPL_USER;
  p->tf->ds = (SEG_UDATA << 3) | DPL_USER;
  p->tf->es = p->tf->ds;
  p->tf->ss = p->tf->ds;
  p->tf->eflags = FL_IF;
  p->tf->esp = PGSIZE;
  p->tf->eip = 0;  // beginning of initcode.S

  safestrcpy(p->name, "initcode", sizeof(p->name));
  p->cwd = namei("/");
  
  p->state = RUNNABLE;
  release(&ptable.lock);
}

// Grow current process's memory by n bytes.
// Return 0 on success, -1 on failure.
int
growproc(int n)
{
  uint sz;
  
  sz = proc->sz;
  if(n > 0){
    if((sz = allocuvm(proc->pgdir, sz, sz + n)) == 0)
      return -1;
  } else if(n < 0){
    if((sz = deallocuvm(proc->pgdir, sz, sz + n)) == 0)
      return -1;
  }
  proc->sz = sz;
  switchuvm(proc);
  return 0;
}

// Create a new process copying p as the parent.
// Sets up stack to return as if from system call.
// Caller must set state of returned proc to RUNNABLE.
int
fork(void)
{
  int i, pid;
  struct proc *np;

  // Allocate process.
  if((np = allocproc()) == 0)
    return -1;

  // Copy process state from p.
  if((np->pgdir = copyuvm(proc->pgdir, proc->sz)) == 0){
    kfree(np->kstack);
    np->kstack = 0;
    np->state = UNUSED;
    return -1;
  }
  // np->tickets = proc->tickets;
  // np->ticks = proc->ticks;
  np->sz = proc->sz;
  np->parent = proc;
  *np->tf = *proc->tf;

  // Clear %eax so that fork returns 0 in the child.
  np->tf->eax = 0;

  for(i = 0; i < NOFILE; i++)
    if(proc->ofile[i])
      np->ofile[i] = filedup(proc->ofile[i]);
  np->cwd = idup(proc->cwd);
 
  pid = np->pid;
  np->state = RUNNABLE;
  safestrcpy(np->name, proc->name, sizeof(proc->name));
  return pid;
}

// Exit the current process.  Does not return.
// An exited process remains in the zombie state
// until its parent calls wait() to find out it exited.
void
exit(void)
{
  struct proc *p;
  int fd;

  if(proc == initproc)
    panic("init exiting");

  // Close all open files.
  for(fd = 0; fd < NOFILE; fd++){
    if(proc->ofile[fd]){
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
  for(p = ptable.proc; p < &ptable.proc[NPROC]; p++){
    if(p->parent == proc){
      p->parent = initproc;
      if(p->state == ZOMBIE)
        wakeup1(initproc);
    }
  }

  // Jump into the scheduler, never to return.
  proc->state = ZOMBIE;
  sched();
  panic("zombie exit");
}

// Wait for a child process to exit and return its pid.
// Return -1 if this process has no children.
int
wait(void)
{
  struct proc *p;
  int havekids, pid;

  acquire(&ptable.lock);
  for(;;){
    // Scan through table looking for zombie children.
    havekids = 0;
    for(p = ptable.proc; p < &ptable.proc[NPROC]; p++){
      if(p->parent != proc)
        continue;
      havekids = 1;
      if(p->state == ZOMBIE){
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
    if(!havekids || proc->killed){
      release(&ptable.lock);
      return -1;
    }

    // Wait for children to exit.  (See wakeup1 call in proc_exit.)
    sleep(proc, &ptable.lock);  //DOC: wait-sleep
  }
}

// Per-CPU process scheduler.
// Each CPU calls scheduler() after setting itself up.
// Scheduler never returns.  It loops, doing:
//  - choose a process to run
//  - swtch to start running that process
//  - eventually that process transfers control
//      via swtch back to the scheduler.
void
scheduler(void)
{
  struct proc *p;

  for(;;){
    // Enable interrupts on this processor.
    sti();

    // Loop over process table looking for process to run.
    acquire(&ptable.lock);

    // Iterates through process table to get a RUNNABLE process.
    // Since we are goint to implement Round Robin for priority 3, 2 and 1,
    // we are using wait_ticks of each process to realize Round Robin.
    // Round Robin: each process is processed a tick at one time, and processes are processed in order.
    // priority 3 > 2 > 1 > 0
    // every time we schedule, if there is a process in priority 3, than it will be run first, so on so forth
    // when they are multiple process in a priority, we run them in Round Robin
    // priority 0 is run in FIFO
    struct proc *lwp3, *lwp2, *lwp1, *lwp0;
    int waittp3 = -1, waittp2 = -1, waittp1 = -1, waittp0 = -1;
    // find the process with longest wait_ticks for each priority
    for (p = ptable.proc; p < &ptable.proc[NPROC]; p++) {
      if (p->priority == 3 && p->state == RUNNABLE && p->wait_ticks[3] > waittp3) {
        waittp3 = p->wait_ticks[3];
        lwp3 = p;
        p->wait_ticks[3]++;
      }
      else if (p->priority == 2 && p->state == RUNNABLE && p->wait_ticks[2] > waittp2) {
        waittp2 = p->wait_ticks[2];
        lwp2 = p;
        p->wait_ticks[2]++;
      }
      else if (p->priority == 1 && p->state == RUNNABLE && p->wait_ticks[1] > waittp1) {
        waittp1 = p->wait_ticks[1];
        lwp1 = p;
        p->wait_ticks[1]++;
      }
      else if (p->priority == 0 && p->state == RUNNABLE && p->wait_ticks[0] > waittp0) {
        waittp0 = p->wait_ticks[0];
        lwp0 = p;
        p->wait_ticks[0]++;
      }
    }

    // run the chosen process, it is choosed based on priority 3>2>1>0 and wait_ticks
    if (waittp3 != -1) {
      proc = lwp3;
      switchuvm(lwp3);
      lwp3->cur_ticks++; // accumulated used ticks in current priority
      lwp3->ticks[3]++; // accumulate used ticks from the birth of this process
      lwp3->wait_ticks[3] = 0; // a process is run, so wait_tick reset to 0
      lwp3->state = RUNNING;
      swtch(&cpu->scheduler, proc->context);
      switchkvm();
      // demote a process when its cur_ticks reaches spec
      if (lwp3->cur_ticks == 8) {
        lwp3->priority = 2;
        lwp3->cur_ticks = 0;
        lwp3->wait_ticks[2] = 0;
      }
      proc = 0;
    }
    else if (waittp2 != -1) {
      proc = lwp2;
      switchuvm(lwp2);
      lwp2->cur_ticks++;
      lwp2->ticks[2]++;
      lwp2->wait_ticks[2] = 0;
      lwp2->state = RUNNING;
      swtch(&cpu->scheduler, proc->context);
      switchkvm();
      if (lwp2->cur_ticks == 16) {
        lwp2->priority = 1;
        lwp2->cur_ticks = 0;
        lwp2->wait_ticks[1] = 0;
      }
      proc = 0;
    }
    else if (waittp1 != -1) {
      proc = lwp1;
      switchuvm(lwp1);
      lwp1->cur_ticks++;
      lwp1->ticks[1]++;
      lwp1->wait_ticks[1] = 0;
      lwp1->state = RUNNING;
      swtch(&cpu->scheduler, proc->context);
      switchkvm();
      if (lwp1->cur_ticks == 32) {
        lwp1->priority = 0;
        lwp1->cur_ticks = 0;
        lwp1->wait_ticks[0] = 0;
      }
      proc = 0;
      release(&ptable.lock);
      continue;
    }
    else if (waittp0 != -1) {
      proc = lwp0;
      switchuvm(lwp0);
      lwp0->cur_ticks++;
      lwp0->ticks[0]++;
      lwp0->state = RUNNING;
      swtch(&cpu->scheduler, proc->context);
      switchkvm();
      proc = 0;
    }

    // iterates process table to promote processes that reach spec
    for (p = ptable.proc; p < &ptable.proc[NPROC]; p++) {
      if (p->priority == 2 && p->wait_ticks[2] == 160) {
        p->priority = 3;
        p->cur_ticks = 0;
        p->wait_ticks[2] = 0;
        p->wait_ticks[3] = 0;
      }
      else if (p->priority == 1 && p->wait_ticks[1] == 320) {
        p->priority = 2;
        p->cur_ticks = 0;
        p->wait_ticks[1] = 0;
        p->wait_ticks[2] = 0;
      }
      else if (p->priority == 0 && p->wait_ticks[0] == 500) {
        p->priority = 1;
        p->cur_ticks = 0;
        p->wait_ticks[0] = 0;
        p->wait_ticks[1] = 0;
      }
    }

    release(&ptable.lock);
  }
}

// Enter scheduler.  Must hold only ptable.lock
// and have changed proc->state.
void
sched(void)
{
  int intena;

  if(!holding(&ptable.lock))
    panic("sched ptable.lock");
  if(cpu->ncli != 1)
    panic("sched locks");
  if(proc->state == RUNNING)
    panic("sched running");
  if(readeflags()&FL_IF)
    panic("sched interruptible");
  intena = cpu->intena;
  swtch(&proc->context, cpu->scheduler);
  cpu->intena = intena;
}

// Give up the CPU for one scheduling round.
void
yield(void)
{
  acquire(&ptable.lock);  //DOC: yieldlock
  proc->state = RUNNABLE;
  sched();
  release(&ptable.lock);
}

// A fork child's very first scheduling by scheduler()
// will swtch here.  "Return" to user space.
void
forkret(void)
{
  // Still holding ptable.lock from scheduler.
  release(&ptable.lock);
  
  // Return to "caller", actually trapret (see allocproc).
}

// Atomically release lock and sleep on chan.
// Reacquires lock when awakened.
void
sleep(void *chan, struct spinlock *lk)
{
  if(proc == 0)
    panic("sleep");

  if(lk == 0)
    panic("sleep without lk");

  // Must acquire ptable.lock in order to
  // change p->state and then call sched.
  // Once we hold ptable.lock, we can be
  // guaranteed that we won't miss any wakeup
  // (wakeup runs with ptable.lock locked),
  // so it's okay to release lk.
  if(lk != &ptable.lock){  //DOC: sleeplock0
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
  if(lk != &ptable.lock){  //DOC: sleeplock2
    release(&ptable.lock);
    acquire(lk);
  }
}

// Wake up all processes sleeping on chan.
// The ptable lock must be held.
static void
wakeup1(void *chan)
{
  struct proc *p;

  for(p = ptable.proc; p < &ptable.proc[NPROC]; p++)
    if(p->state == SLEEPING && p->chan == chan)
      p->state = RUNNABLE;
}

// Wake up all processes sleeping on chan.
void
wakeup(void *chan)
{
  acquire(&ptable.lock);
  wakeup1(chan);
  release(&ptable.lock);
}

// Kill the process with the given pid.
// Process won't exit until it returns
// to user space (see trap in trap.c).
int
kill(int pid)
{
  struct proc *p;

  acquire(&ptable.lock);
  for(p = ptable.proc; p < &ptable.proc[NPROC]; p++){
    if(p->pid == pid){
      p->killed = 1;
      // Wake process from sleep if necessary.
      if(p->state == SLEEPING)
        p->state = RUNNABLE;
      release(&ptable.lock);
      return 0;
    }
  }
  release(&ptable.lock);
  return -1;
}

// Print a process listing to console.  For debugging.
// Runs when user types ^P on console.
// No lock to avoid wedging a stuck machine further.
void
procdump(void)
{
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
  
  for(p = ptable.proc; p < &ptable.proc[NPROC]; p++){
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

// Collect info from process table to put into the pstat struct
int 
getpinfo (struct pstat *ps) 
{

  acquire(&ptable.lock);
  // iterates through process table to get information of each process
  for (int i = 0; i < NPROC; i++) {
      struct proc *proc_ptr = &(ptable.proc[i]); // get address of a process
      ps->inuse[i] = proc_ptr->state == UNUSED? 0 : 1;
      ps->pid[i] = proc_ptr->pid;
      ps->priority[i] = proc_ptr->priority;
      ps->state[i] = proc_ptr->state;

      for (int j=0; j<4; j++) {
        ps->ticks[i][j] = proc_ptr->ticks[j];
        ps->wait_ticks[i][j] = proc_ptr->wait_ticks[j];
      }
  }
  release(&ptable.lock);
  return 0;
}


