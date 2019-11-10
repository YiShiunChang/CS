#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <assert.h>
#include <dirent.h>
#include <stdbool.h>
#include <sys/mman.h>
// just include what have been included in mkfs.c

#define stat xv6_stat      
#define dirent xv6_dirent
#include "types.h"
#include "fs.h"
#include "stat.h"
#undef stat
#undef dirent
// avoid clash with host struct stat

void raise_error(const char* err_code) ;
void knowxv6(char *argv[], int fd, struct stat sbuf);
void swap(char *x, char *y);
char *reverse(char *buffer, int i, int j);
char* itoa(uint num, char* str, int base);

const char* NO_FILE_PROVIDED = "Usage: xcheck <file_system_image>";
const char* IMAGE_NOT_FOUND = "image not found.";
const char* ERR_INODE = "ERROR: bad inode.";
const char* ERR_ROOT = "ERROR: root directory does not exist.";
const char* ERR_DIRECT_ADDR = "ERROR: bad direct address in inode.";
const char* ERR_INDIRECT_ADDR = "ERROR: bad indirect address in inode.";
const char* ERR_DIRECT_FORMAT = "ERROR: directory not properly formatted.";
const char* ERR_USEDINODE_FREEBITMAP = "ERROR: address used by inode but marked free in bitmap.";
const char* ERR_FREEINODE_USEDBITMAP = "ERROR: bitmap marks block in use but it is not in use.";
const char* ERR_DIRECT_ADDR_DUP = "ERROR: direct address used more than once.";
const char* ERR_INDIRECT_ADDR_DUP = "ERROR: indirect address used more than once.";
const char* ERR_USEDINODE_NODIRECTORY = "ERROR: inode marked use but not found in a directory.";
const char* ERR_NOINODE_USEDDIRECTORY = "ERROR: inode referred to in directory but marked free.";
const char* ERR_BAD_REF_COUNT = "ERROR: bad reference count for file.";
const char* ERR_DIRECTORY_DUP = "ERROR: directory appears more than once in file system.";

/**
 * xcheck is used as prompt> xcheck file_system_image.
 * 
 * The image file is a file that contains the file system image. 
 * If no image file is provided, you should print the usage error shown below:
 * Usage: xcheck <file_system_image> 
 * This output must be printed to standard error and exit with the error code of 1.
 * 
 * If the file system image does not exist, 
 * you should print the error image not found. to standard error and exit with the error code of 1.
 * 
 * If the checker detects any one of the 12 errors above, 
 * it should print the specific error to standard error and exit with error code 1.
 * 
 * If the checker detects none of the problems listed above, 
 * it should exit with return code of 0 and not print anything.
 */
int 
main(int argc, char *argv[]) {

	int repair_mode = 0;
	// struct dirent lost_found;
	// check an image file is provided or not
	if (argc < 2) raise_error(NO_FILE_PROVIDED);
	// check repair mode or not
	if (strcmp(argv[1], "-r") == 0 ) repair_mode = 1;
	char* image_name = repair_mode? argv[2] : argv[1];
	int fopen_mode = repair_mode? O_RDWR : O_RDONLY;
	// open image file and get its file descriptor
	int fd = open(image_name, fopen_mode);
	// check does the file system image exist or not
	if (fd < 0) raise_error(IMAGE_NOT_FOUND);
	
	struct stat sbuf; // stat buffer
	fstat(fd, &sbuf); // read stat of fd
	// knowxv6(argv, fd, sbuf); // un-comment this line to see the layout of xv6

	// imgptr points to the first address of this image file
	void *imgptr = mmap(NULL, sbuf.st_size, PROT_READ, MAP_PRIVATE, fd, 0);
	// get the superblock of this image file
	struct superblock *sb = (struct superblock *) (imgptr + BSIZE); 

	// each inode is either unallocated or one of the valid types (T_FILE, T_DIR, T_DEV)
	// if not, print ERROR: bad inode.
	struct dinode *dip = (struct dinode *) (imgptr + 2 * BSIZE); 
	for (int i=1; i<sb->ninodes; i++) {
		if (dip[i].type != 0 && dip[i].type !=  T_FILE&& dip[i].type != T_DIR && dip[i].type != T_DEV)
			raise_error(ERR_INODE);
	}

	// for in-use inodes, each address that is used by inode is valid (points to a valid datablock address within the image)
	// if the direct block is used and is invalid, print ERROR: bad direct address in inode.
	// if the indirect block is in use and is invalid, print ERROR: bad indirect address in inode.
	// iterate through all inodes
	for (int i=1; i<sb->ninodes; i++) {
		// check direct blocks of an inode
		for (int j=0; j<NDIRECT; j++) {
			if (dip[i].addrs[j] == 0) continue;
			if (dip[i].addrs[j] < 29 || dip[i].addrs[j] > sb->nblocks)
				raise_error(ERR_DIRECT_ADDR);
		}

		// check indirect blocks of an inode 
		uint indirect_data_block_addr = dip[i].addrs[NDIRECT];
		uint *indirect = (uint *) (imgptr + indirect_data_block_addr * BSIZE);
		for (int j=0; j<BSIZE/sizeof(uint); j++) {
			if (indirect[j] == 0) continue;
			if (indirect[j] < 29 || indirect[j] > sb->nblocks)
				raise_error(ERR_INDIRECT_ADDR);
		}
	}

	// when root directory exists, its inode number is 1, and the parent of the root directory is itself
	// if not, print ERROR: root directory does not exist.
	if (dip[1].type != T_DIR) raise_error(ERR_ROOT); // complementary to line 137
	
	for (int i=1; i<sb->ninodes; i++) { 
		// only process directory inode
		if (dip[i].type != T_DIR) continue; 

		// each directory contains . and .. entries, and the . entry points to the directory itself
		// if not, print ERROR: directory not properly formatted.
		struct xv6_dirent *dir_entry = (struct xv6_dirent *) (imgptr + dip[i].addrs[0] * BSIZE); 
		if (strcmp(dir_entry[0].name, ".") != 0) raise_error(ERR_DIRECT_FORMAT);
		if (dir_entry[0].inum != i) raise_error(ERR_DIRECT_FORMAT);
		if (strcmp(dir_entry[1].name, "..") != 0) raise_error(ERR_DIRECT_FORMAT);
		// note. the first data block pointed by an inode would have the first-two default directories,
		// we only check the first data block because an inode corresponds to a file/directory,
		// and if it corresponds to a directory, it will only use one data block
		
		// when root directory exists, its inode number is 1, and the parent of the root directory is itself
		// if not, print ERROR: root directory does not exist.
		if (i == 1) if (dir_entry[0].inum != 1 || dir_entry[1].inum != 1) raise_error(ERR_ROOT);
	}

	// for in-use inodes, each address in use is also marked in use in the bitmap, which means that
	// checking if the addresses pointed to by the inode are also marked as used in the bitmap
	// if not, print ERROR: address used by inode but marked free in bitmap.
	char *bitarray = malloc(sizeof(char) * 1024); // build an array to represent bitmap 
	uint *bitptr = (uint *) (imgptr + 28 * BSIZE); // bitmap starts at block 28
	// there are 1024 blocks in fs.img, and each byte has 8 bits, 
	// so we need 1024/8 = 128 bytes, and 128 bytes = 32 uints
	for (int i=0; i<32; i++) {
		char buffer[4]; // char is 1 byte while uint is 4 byte
		strcat(bitarray, itoa(bitptr[i], buffer, 2)); // here 2 means binary
	}
	// printf("bitmap \n%s\n", bitarray); // un-comment to check bitmap

	// blockarray stores which datablocks are used, and it is build for future use
	char blockarray[1024]; for (int i=0; i<1024; i++) blockarray[i] = 48;

	// if a data block is used, then it is pointed by an inode
	// so we raise error when the corresponding bitmap is free of an used data block 
	for (int i=1; i<sb->ninodes; i++) { 
		for (int j=0; j<NDIRECT; j++) {
			if (dip[i].addrs[j] == 0) continue;
			blockarray[dip[i].addrs[j]] = 49;
			if (bitarray[dip[i].addrs[j]] != 49) raise_error(ERR_USEDINODE_FREEBITMAP);
		}

		uint indirect_data_block_addr = dip[i].addrs[NDIRECT];
		if (indirect_data_block_addr != 0) {
			blockarray[indirect_data_block_addr] = 49;
			uint *indirect = (uint *) (imgptr + indirect_data_block_addr * BSIZE);
			for (int j=0; j<BSIZE/sizeof(uint); j++) {
				if (indirect[j] == 0) continue;
				blockarray[indirect[j]] = 49;
				if (bitarray[indirect[j]] != 49) raise_error(ERR_USEDINODE_FREEBITMAP);
			}
		} 
	}

	// for blocks marked in-use in bitmap, the block should actually be in-use in an inode or indirect block somewhere
	// if not, print ERROR: bitmap marks block in use but it is not in use.
	for (int i=29; i<1024; i++) {
		// printf("bitarray[%d]: %d, blockarray[%d]: %d\n", i, bitarray[i], i, blockarray[i]);
		if (bitarray[i] != blockarray[i]) raise_error(ERR_FREEINODE_USEDBITMAP);
	}
	free(bitarray);

	// reset blockarray for next two tests
	for (int i=0; i<1024; i++) blockarray[i] = 48;

	// for in-use inodes, each direct address in use is only used once
	// if not, print ERROR: direct address used more than once.
	// for in-use inodes, each indirect address in use is only used once
	// if not, print ERROR: indirect address used more than once.
	for (int i=1; i<sb->ninodes; i++) { 
		for (int j=0; j<NDIRECT; j++) {
			if (dip[i].addrs[j] == 0) continue;
			// if blockarray[dip[i].addrs[j]] is 49 already, it means this block has already been used
			if (blockarray[dip[i].addrs[j]] == 49) raise_error(ERR_DIRECT_ADDR_DUP);
			blockarray[dip[i].addrs[j]] = 49;
		}

		uint indirect_data_block_addr = dip[i].addrs[NDIRECT];
		if (indirect_data_block_addr != 0) {
			if (blockarray[indirect_data_block_addr] == 49) raise_error(ERR_INDIRECT_ADDR_DUP);
			blockarray[indirect_data_block_addr] = 49;
			uint *indirect = (uint *) (imgptr + indirect_data_block_addr * BSIZE);
			for (int j=0; j<BSIZE/sizeof(uint); j++) {
				if (indirect[j] == 0) continue;
				if (blockarray[indirect[j]] == 49) raise_error(ERR_INDIRECT_ADDR_DUP);
				blockarray[indirect[j]] = 49;
			}
		}
	}

	// for all inodes marked in use, each must be referred to in at least one directory
	// if not, print ERROR: inode marked use but not found in a directory.
	// reference inode stores inodes that have been referred
	int ref_inode[sb->ninodes];
	for (int i=0; i<sb->ninodes; i++) ref_inode[i] = 0;

	// iterate through all inodes to check inodes with T_DIR type, root "/" starts at inode 1
	for (int i=1; i<sb->ninodes; i++) {
		if (dip[i].type != T_DIR) continue;
		// iterate through direct pointed blocks of a T_DIR inode
		for (int j=0; j<NDIRECT; j++) {
			uint dir_data_block_addr = dip[i].addrs[j]; // get addr of pointed block
			if (dir_data_block_addr == 0) continue;     // addr = 0 means no block is pointed
			// get the memory address of a pointed block
			struct xv6_dirent *dir_entry = (struct xv6_dirent *) (imgptr + dir_data_block_addr * BSIZE); 
			// direct entries are stored in this block, lets check each entyry one by one  
			for (int k=0; k<BSIZE/sizeof(struct xv6_dirent); k++) {
				// printf("directory is %s and inum is %d\n", dir_entry[k].name, dir_entry[k].inum);
				if (strcmp(dir_entry[k].name, ".") == 0 || strcmp(dir_entry[k].name, "..") == 0) continue;
				if (dir_entry[k].inum != 0) ref_inode[dir_entry[k].inum] ++;
			}		
		}

		// iterate through indirect pointed block
		uint indirect_data_block_addr = dip[i].addrs[NDIRECT];
		if (indirect_data_block_addr == 0) continue;
		uint *indirect = (uint *) (imgptr + indirect_data_block_addr * BSIZE);
		for (int j=0; j<BSIZE/sizeof(uint); j++) {
			if (indirect[j] == 0) continue;
			struct xv6_dirent *indirect_entry = (struct xv6_dirent *) (imgptr + indirect[j] * BSIZE); 
			for (int k=0; k<BSIZE/sizeof(struct xv6_dirent); k++) {
				// printf("indirectory is %s and inum is %d\n", indirect_entry[k].name, indirect_entry[k].inum);
				if (strcmp(indirect_entry[k].name, ".") == 0 || strcmp(indirect_entry[k].name, "..") == 0) continue;
				if (indirect_entry[k].inum != 0) ref_inode[indirect_entry[i].inum] ++;
			}		
		}
	}
	ref_inode[1] = 1;
	
	// check ref_inode
	// for (int i=0; i<sb->ninodes; i++) {
	// 	if (dip[i].type == T_DIR)	printf("directory ref_inode[%d]: %d\n", i, ref_inode[i]);
	// 	if (dip[i].type != T_DIR)	printf("file ref_inode[%d]: %d\n", i, ref_inode[i]);
	// }

	// for all inodes marked in use, each must be referred to in at least one directory
	// if not, print ERROR: inode marked use but not found in a directory.
	for (int i=1; i<sb->ninodes; i++) {
		if (dip[i].type != T_DEV && dip[i].type != T_DIR && dip[i].type != T_FILE) continue;
		// printf("dip[%d].type: %d, dip[%d].size: %d, addr: %d\n", i, dip[i].type, i, dip[i].size, dip[i].addrs[0]);
		if (ref_inode[i] == 0) raise_error(ERR_USEDINODE_NODIRECTORY);
	}
	
	// for each inode number that is referred to in a valid directory, it is actually marked in use
	// if not, print ERROR: inode referred to in directory but marked free.
	for (int i=1; i<sb->ninodes; i++) {
		if (ref_inode[i] == 0) continue;
		if (dip[i].type == 0) raise_error(ERR_NOINODE_USEDDIRECTORY);
	}

	// reference counts (number of links) for regular files 
	// match the number of times file is referred to in directories (i.e., hard links work correctly)
	// if not, print ERROR: bad reference count for file.
	for (int i=1; i<sb->ninodes; i++) {
		if (dip[i].type != T_FILE) continue;
		if (dip[i].nlink != ref_inode[i]) raise_error(ERR_BAD_REF_COUNT); 
	}

	// no extra links allowed for directories (each directory only appears in one other directory)
	// if not, print ERROR: directory appears more than once in file system.
	for (int i=1; i<sb->ninodes; i++) {
		// only process T_DIR inode
		if (dip[i].type != T_DIR) continue;
		// printf("ref_inode[%d] %d\n", i, ref_inode[i]);
		// check the direcotry file
		if (ref_inode[i] > 1) raise_error(ERR_DIRECTORY_DUP); 
	}

	exit(0);
}

/**
 * A little help function
 */
void raise_error(const char* err_code) {
	// printf("\n===== error: %s =====\n", err_code);
	fprintf(stderr, "%s\n", err_code);
	exit(1);
}

/**
 * This function helps us know the layout of xv6
 * ==================== layout of xv6 we now know ====================
 * block 0 / block 1    / block 2 ... block 26 / block 27 / block 28 / block 29 ... block 1023
 * unused  / superblock / inodes (inode table) / unused   / bitmap   / data block
 * unused and superblock is defined in fs.h, and inode-table is set in mkfs.c
 * note. inode 0 in inode-table has no info is because uint freeinode = 1 (root_inode number is 1, so 0 is unused)
 * ==================== layout of xv6 we now know ====================
 */
void 
knowxv6(char *argv[], int fd, struct stat sbuf) {
	printf("%s has size %ld\n", argv[1], sbuf.st_size); // check fd size

	// mmap() creates a new mapping in the virtual address space of the calling process
	// void *mmap(void *addr, size_t length, int prot, int flags, int fd, off_t offset);
	void *imgptr = mmap(NULL, sbuf.st_size, PROT_READ, MAP_PRIVATE, fd, 0);

	// imgptr points to byte 0 of fd, superblock and BSIZE is defined in fs.h and mkfs.c
	struct superblock *sb = (struct superblock *) (imgptr + BSIZE); 
	printf("%s has num_blocks: %d, num_datablocks: %d, num_inodes: %d\n", argv[1], sb->size, sb->nblocks, sb->ninodes);

	// dip points to an array of inodes, so it points to inode table
	// data block address: 29 means that file is in 29th data block
	struct dinode *dip = (struct dinode *) (imgptr + 2*BSIZE); 
	printf("inode 0 has file type: %d, nlink: %d, size: %d, data block address: %d\n", dip[0].type, dip[0].nlink, dip[0].size, dip[0].addrs[0]);
	printf("inode 1 has file type: %d, nlink: %d, size: %d, data block address: %d\n", dip[1].type, dip[1].nlink, dip[1].size, dip[1].addrs[0]);
	
	// dip[1] is an inode, this inode has 12 direct pointers that points to a data block, and 1 indirect pointer points to other inodes
	// directory is a file containing a sequence of dirent structures, so the first pointer of dip[1] is pointing to a directory/file "/"
	// directory/file "/" saves info about its child directories and files, the info links each children to its corresponding inode
	uint data_block_addr = dip[1].addrs[0];
	printf("\ndata block address of / is %d\n", data_block_addr);
	struct xv6_dirent *entry = (struct xv6_dirent *) (imgptr + data_block_addr * BSIZE); 
	for (int i=0; i<5; i++) 
		printf("file/directory is %s and inum is %d\n", entry[i].name, entry[i].inum);
	
	// dip[2] is corresponding inode of README, and its first pointer points to the first data block that stores README file
	// we traverse all pointers of dip[2], and find that README is stored in 30, 31, 32, and 33 data blocks
	data_block_addr = dip[2].addrs[0];
	printf("\ndata block address of /README is %d\n", data_block_addr);
	for (int i=0; i<NDIRECT+1; i++)
		printf("%d ", dip[2].addrs[i]);
	printf("\n");

	// let's try to check what is stored in README's data block
	char *readme = (char *) (imgptr + data_block_addr * BSIZE);
	for (int i=0; i<50; i++)
		printf("%c", readme[i]);
	printf("\n");

	// let's try an example of indirect pointer, which is from cat
	printf("inode 3 has file type: %d, nlink: %d, size: %d, data block address: %d\n", dip[3].type, dip[3].nlink, dip[3].size, dip[3].addrs[0]);
	data_block_addr = dip[3].addrs[NDIRECT]; // the last pointer points to a data block that stores more address
	uint *cat_indirect = (uint *) (imgptr + data_block_addr * BSIZE);
	for (int i=0; i<BSIZE/sizeof(uint); i++) {
		printf("%d ", cat_indirect[i]);
	}
	printf("\n");

	// let's check how many inodes are in inode table
	printf("there are %ld inodes in inode table\n", sb->ninodes / IPB);

	// let's check the bitmap value of datablock 0, which is block 29
	// note. 4bytes for an uint, which means 32bits for an uint
	// char *bitmapptr = malloc(sizeof(char) * 128);

	char *tempc = malloc(sizeof(char) * 1024);
	uint *temp = (uint *) (imgptr + 28 * BSIZE);
	for (int i=0; i<32; i++) {
		char buffer[4];
		// printf("check: %s \n", itoa(temp[i], buffer, 2));   // here 2 means binary
		strcat(tempc, itoa(temp[i], buffer, 2));
	}
	printf("tempc: \n%s\n", tempc);
	free(tempc);
}

/**
 * Swap two numbers
 */
void 
swap(char *x, char *y) {
	char t = *x;
	*x = *y;
	*y = t; 
}


/**
 * Reverse buffer[i..j]
 */
char 
*reverse(char *buffer, int i, int j) {
	while (i<j) {
		swap(&buffer[i++], &buffer[j--]);
	}
	return buffer;
}


/**
 * Iterative function to implement itoa() in c
 */
char* 
itoa(uint value, char* buffer, int base) { 
	// invalid input
	if (base<2 || base>32) return buffer;
	// consider absolute value of number
	uint n = value;

	int i = 0;
	while (n) {
		int r = n % base;
		if (r>=10) buffer[i++] = 65 + (r-10);
		else buffer[i++] = 48 + r;
		n = n / base;
	}

	// if number is 0
	if (i == 0) buffer[i++] = '0';

	// if base is 10 and value is negative, 
	// the resulting string is preceded with a minus sign "-"
	// with any other base, value is considered unsigned
	if (value < 0 && base == 10) buffer[i++] = '-';

	while (i<32)
		buffer[i++] = '0';

	buffer[i] = '\0'; // null terminate string

	// reverse the string and return it
	return buffer;
} 

