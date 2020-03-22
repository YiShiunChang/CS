/**
 * @author See Contributors.txt for code contributors and overview of BadgerDB.
 *
 * @section LICENSE
 * Copyright (c) 2012 Database Group, Computer Sciences Department, University of Wisconsin-Madison.
 */

#include <vector>
#include "btree.h"
#include "page.h"
#include "filescan.h"
#include "page_iterator.h"
#include "file_iterator.h"
#include "exceptions/insufficient_space_exception.h"
#include "exceptions/index_scan_completed_exception.h"
#include "exceptions/file_not_found_exception.h"
#include "exceptions/no_such_key_found_exception.h"
#include "exceptions/bad_scanrange_exception.h"
#include "exceptions/bad_opcodes_exception.h"
#include "exceptions/scan_not_initialized_exception.h"
#include "exceptions/end_of_file_exception.h"

#define checkPassFail(a, b) \
{\
	if(a == b) { \
		std::cout << "\nTest passed at line no:" << __LINE__ << "\n"; \
	}	else { \
		std::cout << "\nTest FAILS at line no:" << __LINE__; \
		std::cout << "\nExpected no of records:" << b; \
		std::cout << "\nActual no of records found:" << a; \
		std::cout << std::endl;\
		exit(1);\
	}\
}\

using namespace badgerdb;

// setting of global paramteters
typedef struct tuple {
	int i;
	double d;
	char s[64];
} RECORD;

const std::string relationName = "relA"; // PageFile name
const int	relationSize = 500; // 1024 * 3; // number of records
RECORD tempRecord;
BufMgr * bufMgr = new BufMgr(100);
std::string intIndexName; // string for storing name of the BlobFile, index file
// RecordId rid;
// std::string dbRecord1;
// This is the structure for tuples in the base relation

// void createRelationForward();
// void createRelationBackward();
// void createRelationRandom();
// void intTests();
// int intScan(BTreeIndex *index, int lowVal, Operator lowOp, int highVal, Operator highOp);
void treeTests();
// void test1();
// void test2();
// void test3();
// void errorTests();
// void deleteRelation();

int main(int argc, char **argv) {
  // clean relationName from any previous crashed runs
	{
		try {
			File::remove(relationName);
		} catch (FileNotFoundException) {
		}
	}

	// create a new PageFile, please considered it as a file with many records in a database
	// PageFile is composed of pages, each page has header and slots, each slot is a record
	// ex. if sizeof(PageFile) = 1024 and sizeof(RECORD) = 80, then a PageFile can store 11 records,
	//     records range from 128 to 1008, header ranges from 1008 to 1024, slot indexes from 0 to 66
	//     free space ranges from 66 to 128
	// newPage.getPageHeaderInfo(); can be used to get the above infomation
	{
		PageFile newFile = PageFile::create(relationName);
		PageId newPageNumber;
		Page newPage = newFile.allocatePage(newPageNumber);
		int numPage = 1;
		for (int i = 0; i < relationSize; ++i) {
			tempRecord.i = i;
			tempRecord.d = (double) i;
			sprintf(tempRecord.s, "%05d string record", i); // stores "%05d string record" in tempRecord.s
			// sizeof(newData): 32 for tempRecord.s: 02199 string record = 16, tempRecord.i + tempRecord.d = 16
			// contstruct a string based on tempRecord, and this string is the data of a record
			std::string newData(reinterpret_cast<char*>(&tempRecord), sizeof(tempRecord));

			// insert newData into the newPage when there is room for insertion
			while (1) {
				try {
					newPage.insertRecord(newData);
					break;
				} catch (InsufficientSpaceException e) {
					newFile.writePage(newPageNumber, newPage);
					newPage = newFile.allocatePage(newPageNumber);
					numPage++;
				}
			}
		}
		newFile.writePage(newPageNumber, newPage);

		// print basic info of a record
		std::cout << "sizeof(RECORD): " << sizeof(RECORD) << std::endl;
		std::cout << "# records: " << relationSize << std::endl;
		std::cout << "sizeof(Page): " << Page::SIZE << std::endl;
		std::cout << "# allocated pages of relA: " << numPage << std::endl;
	}
	
	// scan the file, which is relationName, to ensure that we create what we want
	{	
		// opens relationName as a PageFile and waits for scan 
		FileScan fscan(relationName, bufMgr);

		int thRecord = 0;
		try {
			RecordId scanRid;
			while (1) {
				fscan.scanNext(scanRid); // scan file to let fsacn point to the next record
				thRecord++; // used for print progress

				// get the info of the next record
				std::string recordStr = fscan.getRecord();
				const char *record = recordStr.c_str(); // c_str() returns a pointer to a null-terminated sequence of characters
				
				if (thRecord % 100 == 0) {
					// checking int of a record
					int key = *((int *)(record + offsetof(RECORD, i)));
					std::cout << "Extracted int: " << key << std::endl;
					// checking string of a record
					// std::string value = (char *)(record + offsetof(RECORD, s));
					// std::cout << "Extracted string: " << value << std::endl;
				}
			}
		} catch (EndOfFileException e) {
			std::cout << "Read all records" << std::endl;
		}
	}

	treeTests();

	// remove file if needed
	File::remove(relationName);

  return 1;
}

void treeTests()
{
  std::cout << "create a B+Tree index on the relA" << std::endl;
	
  BTreeIndex index(relationName, intIndexName, bufMgr, offsetof(tuple, i), INTEGER);
	index.getPrivateInfo();
	index.printTree();
}
