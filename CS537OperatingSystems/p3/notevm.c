#include "param.h"
#include "types.h"
#include "defs.h"
#include "x86.h"
#include "mmu.h"
#include "proc.h"
#include "elf.h"

extern char data[];  // defined in data.S

static pde_t *kpgdir;  // for use in scheduler()

// Allocate one page table for the machine for the kernel address
// space for scheduler processes.
void
kvmalloc(void)
{
  kpgdir = setupkvm();
}

// Set up CPU's kernel segment descriptors.
// Run once at boot time on each CPU.
void
seginit(void)
{
  struct cpu *c;

  // Map virtual addresses to linear addresses using identity map.
  // Cannot share a CODE descriptor for both kernel and user
  // because it would have to have DPL_USR, but the CPU forbids
  // an interrupt from CPL=0 to DPL=3.
  c = &cpus[cpunum()];
  c->gdt[SEG_KCODE] = SEG(STA_X|STA_R, 0, 0xffffffff, 0);
  c->gdt[SEG_KDATA] = SEG(STA_W, 0, 0xffffffff, 0);
  c->gdt[SEG_UCODE] = SEG(STA_X|STA_R, 0, 0xffffffff, DPL_USER);
  c->gdt[SEG_UDATA] = SEG(STA_W, 0, 0xffffffff, DPL_USER);

  // Map cpu, and curproc
  c->gdt[SEG_KCPU] = SEG(STA_W, &c->cpu, 8, 0);

  lgdt(c->gdt, sizeof(c->gdt));
  loadgs(SEG_KCPU << 3);
  
  // Initialize cpu-local storage.
  cpu = c;
  proc = 0;
}

/**
 * Return the address of the PTE in page table pgdir that corresponds to linear address va.
 * If create != 0, create any required page table pages.
 */
static pte_t * walkpgdir(pde_t *pgdir, const void *va, int create) {
  pde_t *pde; // page entry, typedef uint pde_t is defined in include/types.h 
  pte_t *pgtab; // page table, typedef uint pte_t is defined in kernel/mmu.h

  // page directory index
  // kernel/mmu.h: #define PDXSHIFT   22  
  // #define PDX(la) (((uint)(la) >> PDXSHIFT) & 0x3FF)
  pde = &pgdir[PDX(va)];
  if(*pde & PTE_P){
    pgtab = (pte_t*)PTE_ADDR(*pde);
  } else {
    if(!create || (pgtab = (pte_t*)kalloc()) == 0)
      return 0;
    // Make sure all those PTE_P bits are zero.
    memset(pgtab, 0, PGSIZE);
    // The permissions here are overly generous, but they can
    // be further restricted by the permissions in the page table 
    // entries, if necessary.
    *pde = PADDR(pgtab) | PTE_P | PTE_W | PTE_U;
  }
  return &pgtab[PTX(va)];
}

/**
 * create PTEs for linear addresses starting at la that refer to
 * physical addresses starting at pa. la and size might not be page-aligned.
 * ex. mappages(pgdir = 16711680, 0, PGSIZE = 4096, PADDR(mem) = 16658432, PTE_W|PTE_U);
 */
static int mappages(pde_t *pgdir, void *la, uint size, uint pa, int perm) {
  char *a, *last;
  pte_t *pte;
  
  a = PGROUNDDOWN(la); // compute which page are we going to start
  last = PGROUNDDOWN(la + size - 1); // which page are we going to end
  for(;;){
    // pte is a virtual address of a PTE, or say a page, pte = 16707584
    pte = walkpgdir(pgdir, a, 1);
    if(pte == 0)
      return -1;
    if(*pte & PTE_P)
      panic("remap");
    // *pte is a physical address, *pte = 16658439
    *pte = pa | perm | PTE_P; 
    if(a == last)
      break;
    a += PGSIZE;
    pa += PGSIZE;
  }
  return 0;
}

// The mappings from logical to linear are one to one (i.e.,
// segmentation doesn't do anything).
// There is one page table per process, plus one that's used
// when a CPU is not running any process (kpgdir).
// A user process uses the same page table as the kernel; the
// page protection bits prevent it from using anything other
// than its memory.
// 
// setupkvm() and exec() set up every page table like this:
//   0..640K          : user memory (text, data, stack, heap)
//   640K..1M         : mapped direct (for IO space)
//   1M..end          : mapped direct (for the kernel's text and data)
//   end..PHYSTOP     : mapped direct (kernel heap and user pages)
//   0xfe000000..0    : mapped direct (devices such as ioapic)
//
// The kernel allocates memory for its heap and for user memory
// between the end of kernel and the end of physical memory (PHYSTOP).
//
// The virtual address space of each user program includes the kernel
// (which is inaccessible in user mode).  
// 
// The user program addresses range from 0 till 640KB (USERTOP), which 
// where the I/O hole starts (both in physical memory and in the 
// kernel's virtual address space). 

// kernel/mmu.h: #define PTE_W 0x002 Writeable, this is page table/directory entry flags
// below is like static struct kmap kmap[] = {}, "struct kmap" is data type
static struct kmap {
  void *p;
  void *e;
  int perm; //permission
} kmap[] = {
  {(void*)USERTOP   , (void*)0x100000, PTE_W},  // I/O space: p=(void*)USERTOP, e=(void*)0x100000
  {(void*)0x100000  ,            data,     0},  // kernel text, rodata
  {data             ,  (void*)PHYSTOP, PTE_W},  // kernel data, memory
  {(void*)0xFE000000,               0, PTE_W},  // device mappings
};
// kmap example in decimal:
// k->p: 655360, (uint)k->p: 655360
// k->p: 1048576, (uint)k->p: 1048576
// k->p: 1089536, (uint)k->p: 1089536
// k->p: -33554432, (uint)k->p: -33554432

/**
 * set up kernel part of a page table and returns virtual address space.
 * 
 * note. the virtual address of every process is divided into kernel space and user space. 
 * however, both spaces have its own table
 * 
 * note. a page directory points to the beginning of a page table, while a page table is composed
 * of many page table entries (PTEs), and each PTE can be consider as a page in some sense.
 */
pde_t* setupkvm(void) {
  pde_t *pgdir;
  struct kmap *k;

  // pgdir = a virtual adress if success, otherwise 0
  // ex. pgdir could be 16527360 if we print out
  // waht kalloc() returns is a physical address, and we cast it to type pde_t*,
  // then we get a virtual address
  if((pgdir = (pde_t*) kalloc()) == 0)
    return 0;
  
  // set 0 on pgdir for PGSIZE, it is like starting from pgdir = 16527360 and going upward to
  // 16523264 while setting each of address 0. 16527360 - 16523264 = 4096 = PGSIZE
  memset(pgdir, 0, PGSIZE);
  // k = kmap, while kmap is from static struct kmap kmap[]
  // k->p = 655360 = 1024*640
  // k->e = 1048576 decimal = 1024*1024 = 1*(10^4)^5 binary = 0x100000 hex
  k = kmap;
  // kernel/defs.h: #define NELEM(x) (sizeof(x)/sizeof((x)[0]))
  for(k = kmap; k < &kmap[NELEM(kmap)]; k++)
    // mappages(pde_t *pgdir, void *la, uint size, uint pa, int perm)
    // create PTEs for addresses starting at la that refer to physical addresses starting at pa.
    if(mappages(pgdir, k->p, k->e - k->p, (uint)k->p, k->perm) < 0)
      return 0;

  return pgdir;
}

// Turn on paging.
void
vmenable(void)
{
  uint cr0;

  switchkvm(); // load kpgdir into cr3
  cr0 = rcr0();
  cr0 |= CR0_PG;
  lcr0(cr0);
}

// Switch h/w page table register to the kernel-only page table,
// for when no process is running.
void
switchkvm(void)
{
  lcr3(PADDR(kpgdir));   // switch to the kernel page table
}

// Switch TSS and h/w page table to correspond to process p.
void
switchuvm(struct proc *p)
{
  pushcli();
  cpu->gdt[SEG_TSS] = SEG16(STS_T32A, &cpu->ts, sizeof(cpu->ts)-1, 0);
  cpu->gdt[SEG_TSS].s = 0;
  cpu->ts.ss0 = SEG_KDATA << 3;
  cpu->ts.esp0 = (uint)proc->kstack + KSTACKSIZE;
  ltr(SEG_TSS << 3);
  if(p->pgdir == 0)
    panic("switchuvm: no pgdir");
  lcr3(PADDR(p->pgdir));  // switch to new address space
  popcli();
}

/**
 * load the initcode into address 0 of pgdir. sz must be less than a page.
 * ex. pgdir = 16711680 (hex), init = user code of a program, sz = size of a user code
 */
void inituvm(pde_t *pgdir, char *init, uint sz) {
  char *mem;
  
  // size of user code must be smaller than 4096 bytes 
  if(sz >= PGSIZE) panic("inituvm: more than a page");

  // allocate 4096 bytes physical memory to mem, mem is in type char*
  // mem = 16658432
  mem = kalloc();
  // set all byte within mem to 0
  memset(mem, 0, PGSIZE);

  // turn a kernel linear address into a physical address.
  // #define PADDR(a) ((uint)(a)): we cast our type char to type uint 
  // PADDR(mem) = 16658432
  mappages(pgdir, 0, PGSIZE, PADDR(mem), PTE_W|PTE_U);

  // move each code into mem like saving codes to memory
  memmove(mem, init, sz);
}

// Load a program segment into pgdir.  addr must be page-aligned
// and the pages from addr to addr+sz must already be mapped.
int
loaduvm(pde_t *pgdir, char *addr, struct inode *ip, uint offset, uint sz)
{
  uint i, pa, n;
  pte_t *pte;

  if((uint)addr % PGSIZE != 0)
    panic("loaduvm: addr must be page aligned");
  for(i = 0; i < sz; i += PGSIZE){
    if((pte = walkpgdir(pgdir, addr+i, 0)) == 0)
      panic("loaduvm: address should exist");
    pa = PTE_ADDR(*pte);
    if(sz - i < PGSIZE)
      n = sz - i;
    else
      n = PGSIZE;
    if(readi(ip, (char*)pa, offset+i, n) != n)
      return -1;
  }
  return 0;
}

/**
 * Allocate page tables and physical memory to grow process from oldsz to
 * newsz, which need not be page aligned.  Returns new size or 0 on error.
 * ex. 
 * pagedirectory -------     -------
 *                 ...         ...
 * oldsz = 400   -------  => -------
 *                 ...         ...
 * pagesize 4096 -------     -------
 *                             ...
 * newsz = 4496              -------
 *                             ...
 * pagesize 8192             -------
 */
int allocuvm(pde_t *pgdir, uint oldsz, uint newsz) {
  char *mem;
  uint a;

  // #define USERTOP 0xA0000 end of user address space is defined in include/param.h
  if(newsz > USERTOP)
    return 0;
  if(newsz < oldsz)
    return oldsz;

  // the position of oldsz is in a page, and PGROUNDUP(oldsz) finds the end of that page
  a = PGROUNDUP(oldsz);
  // #define PGSIZE 4096 bytes mapped by a page is defined in kernel/mmu.h
  for(; a < newsz; a += PGSIZE){
    // kalloc.c is a physical memory allocator, intended to allocate memory for user processes, 
    // kernel stacks, page table pages, and pipe buffers. Allocates 4096-byte pages.
    // kalloc() allocate one 4096-byte page of physical memory. 
    // returns a pointer that the kernel can use, or 0 if the memory cannot be allocated.
    mem = kalloc();
    if(mem == 0){
      cprintf("allocuvm out of memory\n");
      deallocuvm(pgdir, newsz, oldsz);
      return 0;
    }
    // memset(void *dst, int c, uint n) is defined in string.c
    // it calls stosb(dst, c, n), which is included in x86
    // stosb(dst, c, n) write data c into destination dst with n bytes
    memset(mem, 0, PGSIZE);
    // mappages(pde_t *pgdir, void *la, uint size, uint pa, int perm) is defined in vm.c
    // it create PTEs for linear addresses starting at la that refer to physical addresses 
    // starting at pa. 
    mappages(pgdir, (char*)a, PGSIZE, PADDR(mem), PTE_W|PTE_U);
  }
  return newsz;
}

// Deallocate user pages to bring the process size from oldsz to
// newsz.  oldsz and newsz need not be page-aligned, nor does newsz
// need to be less than oldsz.  oldsz can be larger than the actual
// process size.  Returns the new process size.
int
deallocuvm(pde_t *pgdir, uint oldsz, uint newsz)
{
  pte_t *pte;
  uint a, pa;

  if(newsz >= oldsz)
    return oldsz;

  a = PGROUNDUP(newsz);
  for(; a  < oldsz; a += PGSIZE){
    pte = walkpgdir(pgdir, (char*)a, 0);
    if(pte && (*pte & PTE_P) != 0){
      pa = PTE_ADDR(*pte);
      if(pa == 0)
        panic("kfree");
      kfree((char*)pa);
      *pte = 0;
    }
  }
  return newsz;
}

// Free a page table and all the physical memory pages
// in the user part.
void
freevm(pde_t *pgdir)
{
  uint i;

  if(pgdir == 0)
    panic("freevm: no pgdir");
  deallocuvm(pgdir, USERTOP, 0);
  for(i = 0; i < NPDENTRIES; i++){
    if(pgdir[i] & PTE_P)
      kfree((char*)PTE_ADDR(pgdir[i]));
  }
  kfree((char*)pgdir);
}

// Given a parent process's page table, create a copy
// of it for a child.
pde_t*
copyuvm(pde_t *pgdir, uint sz)
{
  pde_t *d;
  pte_t *pte;
  uint pa, i;
  char *mem;

  if((d = setupkvm()) == 0)
    return 0;
  for(i = 0; i < sz; i += PGSIZE){
    if((pte = walkpgdir(pgdir, (void*)i, 0)) == 0)
      panic("copyuvm: pte should exist");
    if(!(*pte & PTE_P))
      panic("copyuvm: page not present");
    pa = PTE_ADDR(*pte);
    if((mem = kalloc()) == 0)
      goto bad;
    memmove(mem, (char*)pa, PGSIZE);
    if(mappages(d, (void*)i, PGSIZE, PADDR(mem), PTE_W|PTE_U) < 0)
      goto bad;
  }
  return d;

bad:
  freevm(d);
  return 0;
}

// Map user virtual address to kernel physical address.
char*
uva2ka(pde_t *pgdir, char *uva)
{
  pte_t *pte;

  pte = walkpgdir(pgdir, uva, 0);
  if((*pte & PTE_P) == 0)
    return 0;
  if((*pte & PTE_U) == 0)
    return 0;
  return (char*)PTE_ADDR(*pte);
}

// Copy len bytes from p to user address va in page table pgdir.
// Most useful when pgdir is not the current page table.
// uva2ka ensures this only works for PTE_U pages.
int
copyout(pde_t *pgdir, uint va, void *p, uint len)
{
  char *buf, *pa0;
  uint n, va0;
  
  buf = (char*)p;
  while(len > 0){
    va0 = (uint)PGROUNDDOWN(va);
    pa0 = uva2ka(pgdir, (char*)va0);
    if(pa0 == 0)
      return -1;
    n = PGSIZE - (va - va0);
    if(n > len)
      n = len;
    memmove(pa0 + (va - va0), buf, n);
    len -= n;
    buf += n;
    va = va0 + PGSIZE;
  }
  return 0;
}
