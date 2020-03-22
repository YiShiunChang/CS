/**
 * @author See Contributors.txt for code contributors and overview of BadgerDB.
 *
 * @section LICENSE
 * Copyright (c) 2012 Database Group, Computer Sciences Department, University of Wisconsin-Madison.
 */

#include "btree.h"
#include "filescan.h"
#include "exceptions/bad_index_info_exception.h"
#include "exceptions/bad_opcodes_exception.h"
#include "exceptions/bad_scanrange_exception.h"
#include "exceptions/no_such_key_found_exception.h"
#include "exceptions/scan_not_initialized_exception.h"
#include "exceptions/index_scan_completed_exception.h"
#include "exceptions/file_not_found_exception.h"
#include "exceptions/end_of_file_exception.h"


//#define DEBUG

namespace badgerdb
{

/**
 * BTreeIndex Constructor. 
 * 
 * @param relationName   The constructor should scan this relation (using ​FileScan​), and insert 
 *                       entries for all the tuples in this relation into the index file. 
 * @param outIndexName   The constructor check whether outIndexName, index file, exists. If so, 
 *                       open the file. If not, create it based on relationName.
 * @param bufMgrIn			 The instance of the global buffer manager
 * @param attrByteOffset The byte offset of the attribute in the tuple on which to build the index
 * @param attrType			 The data type of the attribute we are indexing
 */
BTreeIndex::BTreeIndex(const std::string &relationName, std::string &outIndexName,
		                  BufMgr *bufMgrIn, const int attrByteOffset, const Datatype attrType) {	

	// default setting for building btree
	this->bufMgr = bufMgrIn; 		 	 	
	this->attributeType = attrType; 		 	      // Datatype of attribute over which index is built
	this->attrByteOffset = attrByteOffset;   	  // Offset of attribute, over which index is built, inside records 
	this->leafOccupancy = INTARRAYLEAFSIZE;  	  // Number of keys in leaf node, depending upon the type of key
	this->nodeOccupancy = INTARRAYNONLEAFSIZE;	// Number of keys in nonleaf node, depending upon the type of key
  this->numNonLeafNodes = 0;                  // Number of NonLeafNodes
  this->numLeafNodes = 0;                     // Number of LeafNodes
	scanExecuting = false;                      // default setting for scanning btree

	// an index file name, outIndexName, is constructed as “​relName.attrOffset​”
	std::ostringstream idxStr;
	idxStr<<relationName<<'.'<<attrByteOffset;
	outIndexName = idxStr.str(); 

	// check whether the corresponding index file exists. If so, open the file. If not, create it.
	// we can determine if an index file is existed by seeing if that exception gets thrown.
  // a PageFile stores relations, which are the values of an attribute in a table
  // a BlobFile is a index file of B+Tree
  // a BlobFile can contain lots of pages, and each page is a node in B+Tree
	try {
    // use BlobFile() with create_new flag as false, create a new file if an exception is thrown
		this->file = new BlobFile(outIndexName, false);
		std::cout << "BlobFile " << outIndexName << " exists.\n";

		// get header page of the index file, and check whether meta-data is correct
    // header page is a meta page, which holds IndexMetaInfo as a struct
		this->headerPageNum = this->file->getFirstPageNo(); 
		Page *headerPage;
		bufMgr->readPage(this->file, this->headerPageNum, headerPage);
    // cast headerpage poniter to a metapage pointer
		IndexMetaInfo *meta = (IndexMetaInfo *) headerPage;
		// check whether meta-data matches the requirements of inputs
		if (relationName.compare(meta->relationName) != 0 || attrType != meta->attrType || attrByteOffset != meta->attrByteOffset) {
			throw BadIndexInfoException(outIndexName);
		}
    // get the page number of the root page, if we load the correct index file
    this->rootPageNum = meta->rootPageNo; 

		// unpin headerpage from memory since it is no longer required to remain in memory
		// true if the page to be unpinned is updated/dirty
		bufMgr->unPinPage(file, headerPageNum, false);  
	} catch (FileNotFoundException &e) {
		std::cout << "create BlobFile " << outIndexName << ".\n";
		this->file = new BlobFile(outIndexName, true);

		// dedicate a header page for storing meta-data of the index file
		// allocate header page, and get the corresponding header page number
		Page *headerPage;
		bufMgr->allocPage(file, headerPageNum, headerPage);

		// build meta info
		IndexMetaInfo *meta = (IndexMetaInfo *) headerPage;
    strncpy((char *)(&(meta->relationName)), relationName.c_str(), 20);
    meta->attrType = attrType;
		meta->attrByteOffset = attrByteOffset;
		
		// allocate root page if the file, and get the corresponding root page number
		Page *rootPage;
		bufMgr->allocPage(file, rootPageNum, rootPage);
		meta->rootPageNo = rootPageNum;
    // initialPageNum won't change, but rootPageNum will change as tree grows
		initialPageNum = rootPageNum; 
		LeafNodeInt *root = (LeafNodeInt *) rootPage; // root page is a leaf node at the beginning
		root->rightSibPageNo = 0; // 0 is an arbitrary number
    numLeafNodes++;

		// unpin headerpage/rootpage from memory since it is no longer required to remain in memory
		// true if the page to be unpinned is updated/dirty
		bufMgr->unPinPage(file, headerPageNum, true);
		bufMgr->unPinPage(file, rootPageNum, true);

		// the constructor should scan this relation (using ​FileScan​) 
		// and insert entries for all the tuples in this relation into the index.
		FileScan fileScan(relationName, bufMgr);
		try {	
      // RecordId includes page_number and slot_number, a page has many slots, a slot has a record
			RecordId scanRid;
			while(1) {
				fileScan.scanNext(scanRid);
				std::string recordStr = fileScan.getRecord();
        // std::cout << "key: " << *((int*)(recordStr.c_str() + attrByteOffset)) << std::endl;
				insertEntry(recordStr.c_str() + attrByteOffset, scanRid);
			}
		} catch (EndOfFileException e) {
			// save B+Tee index file to disk
			bufMgr->flushFile(file);
			std::cout << "Read all records" << std::endl;
		}
	}
}

/**
 * This method prints values of all private variables
 */
const void BTreeIndex::getPrivateInfo() {
  std::cout << "headerPageNum: " << headerPageNum << "\n";
  std::cout << "initialPageNum: " << initialPageNum << "\n";
  std::cout << "rootPageNum: " << rootPageNum << "\n";
  std::cout << "leafOccupancy: " << leafOccupancy << "\n";
  std::cout << "nodeOccupancy: " << nodeOccupancy << "\n";
  std::cout << "numNonLeafNodes: " << numNonLeafNodes << "\n";
  std::cout << "numLeafNodes: " << numLeafNodes << "\n";
}

/**
 * Retrun how many nonleafnodes in the B+Tree
 */
const int BTreeIndex::getNumNonLeafNodes() {
  return numNonLeafNodes;
}

/**
 * Retrun how many leafnodes in the B+Tree
 */
const int BTreeIndex::getNumLeafNodes() {
  return numLeafNodes;
}

/**
 * Print the B+Tree
 */
const void BTreeIndex::printTree() {
  Page *curPage;

  // root is leafnode
  if (initialPageNum == rootPageNum) {
    std::cout << "root is leafnode" << std::endl;
    bufMgr->readPage(file, rootPageNum, curPage);
    LeafNodeInt *curNode = (LeafNodeInt *) curPage;
    for (int i = 0; i < leafOccupancy; i++) {
      std::cout << curNode->keyArray[i] << " ";
    }
    return;
  } 

  // root is nonleafnode
  std::cout << "root is nonleafnode" << std::endl;
  PrintInfo info(rootPageNum, false); // false means nonleafnode
  std::queue<PrintInfo> myqueue;
  myqueue.push(info);

  while (!myqueue.empty()) {
    PrintInfo head = myqueue.front();
    bufMgr->readPage(file, head.pageid, curPage);

    // curNode is a nonleafnode
    if (!head.isLeaf) {
      NonLeafNodeInt *curNode = (NonLeafNodeInt *) curPage;
      bool aboveLeaf = curNode->level == 1? true:false;

      // print keys (nodeOccupancy = INTARRAYNONLEAFSIZE)
      for (int i = 0; i < nodeOccupancy && curNode->keyArray[i] != 0; i++) {
        std::cout << curNode->keyArray[i] << " ";
      }
      std::cout << std::endl << "===== between layer =====" << std::endl;

      // push next infos of nodes to queue
      for (int i = 0; i <= nodeOccupancy && curNode->pageNoArray[i] != 0; i++) {
        if (!aboveLeaf) {
          PrintInfo nextInfo(curNode->pageNoArray[i], false);
          myqueue.push(nextInfo);
        } else {
          PrintInfo nextInfo(curNode->pageNoArray[i], true);
          myqueue.push(nextInfo);
        }
      }
      
    } 
    // curNode is a leafnode
    else {
      LeafNodeInt *curNode = (LeafNodeInt *) curPage;
      // print keys
      for (int i = 0; i < leafOccupancy && curNode->ridArray != 0; i++) {
        std::cout << curNode->keyArray[i] << " ";
      }
      std::cout << std::endl;
    }
    
    bufMgr->unPinPage(file, head.pageid, false);
    myqueue.pop();
  }
}

/**
 * The destructor. 
 * Perform any cleanup that may be necessary, including clearing up any state variables, unpinning 
 * B+Tree pages that are pinned, and flushing the index file. 
 * Note that this method does not delete the index file! 
 * But, deletion of the ​file object is required, which will call the destructor of ​File​ class 
 * causing the index file to be closed.
 */
BTreeIndex::~BTreeIndex() {
	scanExecuting = false;
	this->bufMgr->flushFile(BTreeIndex::file);
	delete this->file;
	this->file = nullptr;
}

/**
 * This method inserts a new entry into the index using the pair <key, rid>.
 * 
 * @param key pointer to the value (integer) we want to insert
 * @param rid includes page # and slot # so we can find the record in a page file
 */
const void BTreeIndex::insertEntry(const void *key, const RecordId rid) {
	// std::cout << "page_number: " << rid.page_number << ", slot_number: " << rid.slot_number << "key: " << key << std::endl;
	// get root
	Page* rootPage;
	bufMgr->readPage(file, rootPageNum, rootPage);

  // RIDKeyPair<int> has key: int, value: RecordId
  RIDKeyPair<int> dataEntry;
	dataEntry.set(rid, *((int *) key));
  // RIDKeyPair<int> is added to a leaf node, and we grow B+Tree if childEntry is not a nullptr 
	PageKeyPair<int> *childEntry = nullptr;
	insertNode(rootPage, rootPageNum, initialPageNum == rootPageNum? true:false, dataEntry, childEntry);
}

/**
 * This method implements how we traverse nonleafnodes and leafnodes, if leafnode is not full, we 
 * just insert dadaEntry into it, otherwise, we split leafnode. If leafnode is full, it means that 
 * we need to add a new key to the nonleafnode, if nonleafnode is full, we split nonleafnode.
 * 
 * @param curPage pointer to the current node/page
 * @param curPageNum page number of the current node/page
 * @param nodeIsLeaf true if current node is a leaf node
 * @param dataEntry RIDKeyPair<int> has key: int, value: RecordId
 *                  RecordId has page # and slot # so we can find the record in a page file
 * @param newChildEntry PageKeyPair<int> has key: int, value: pageNo
 */
const void BTreeIndex::insertNode(Page *curPage, PageId curPageNum, bool nodeIsLeaf, 
                                  const RIDKeyPair<int> dataEntry, PageKeyPair<int> *&newchildEntry) {
  // insert nonleaf node
  if (!nodeIsLeaf) { 
    // find the right key to traverse
    // note that nonleafnode has no level, and the level above the leafnode is 1
    NonLeafNodeInt *curNode = (NonLeafNodeInt *) curPage;
    Page *nextPage;
    PageId nextPageNum;
    findNextNonLeafNode(curNode, nextPageNum, dataEntry.key);
    bufMgr->readPage(file, nextPageNum, nextPage);
    insertNode(nextPage, nextPageNum, curNode->level == 1, dataEntry, newchildEntry);
    
    // newchildEntry is nullptr means no split in child, just return. Otherwise, add the key of 
    // newchildEntry to curNode, and its corresponding value is the pageNo of newchildEntry
    if (newchildEntry == nullptr) {
	    // unpin current page from call stack, false means curPage is not dirty
	    bufMgr->unPinPage(file, curPageNum, false);
    } else { 
      // pageNoArray[nodeOccupancy] = 0 means curNode is not full, insert newchildEntry to curNode
      // otherwise, split curNode and update newchildEntry, so the parentPage of curPage can use 
      // the updated newchildEntry to update parentpage itself
      if (curNode->pageNoArray[nodeOccupancy] == 0) {
        insertNonLeaf(curNode, newchildEntry);
        newchildEntry = nullptr;
        // unpin current page from call stack, true means curPage is dirty
        bufMgr->unPinPage(file, curPageNum, true);
      } else { 
        splitNonLeaf(curNode, curPageNum, newchildEntry);
        numNonLeafNodes++;
      }
    }
  } else { // insert leaf node
    LeafNodeInt *leaf = (LeafNodeInt *) curPage;
    if (leaf->ridArray[leafOccupancy - 1].page_number == 0) {
      insertLeaf(leaf, dataEntry);
      newchildEntry = nullptr;
      bufMgr->unPinPage(file, curPageNum, true);
    } else {
      splitLeaf(leaf, curPageNum, newchildEntry, dataEntry);
      numLeafNodes++;
    }
  }
}

/**
 * Find the childNode of the curNode based on key
 * 
 * @param curNode is parent
 * @param nextPageNum an address used to store pageNumber of child
 * @param key helps us find the pageNumber of child
 */
const void BTreeIndex::findNextNonLeafNode(NonLeafNodeInt *curNode, PageId &nextPageNum, int key) { 
  // nodeOccupancy means the max number of key-value pairs in a NonleafNode
  // ex. NonleafNode = 6, key = 25
  // keyArray    [10, 20, 30, 40, 0, 0]
  // pageNoArray [x<=10, 10<x<=20, 20<x<=30, 30<x<=40, 40<x, 0, 0]
  int i = nodeOccupancy; 
  while (i >= 0 && (curNode->pageNoArray[i] == 0)) {
    i--;
  } // i = 6 -> 5 -> 4
  while (i > 0 && (curNode->keyArray[i - 1] >= key)) {
    i--;
  } // i = 4 -> 3 -> 2
  
  nextPageNum = curNode->pageNoArray[i]; // curNode->pageNoArray[2]
}

/**
 * This method adds a PageKeyPair into a nonLeafNode. PageKeyPair<int> has key: int, value: pageNo.
 * 
 * @param nonleaf is the node that we want to add a new entry
 * @param entry is the entry that we want to add
 */
const void BTreeIndex::insertNonLeaf(NonLeafNodeInt *nonleaf, PageKeyPair<int> *entry) {
  // ex. for a nonleafnode with keyArray[INTARRAYNONLEAFSIZE = 6]
  // keyArray        [10,       20,       30,       40,     0, 0] 
  // pageNoArray [y<10, 10<=y<20, 20<=x<30, 30<=y<40, 40<=y, 0, 0] 
  // and assume that x is a leafnode/leafpage, and its previous status is
  // keyArray[21, 22, 23, 26, 27, 28]
  // ridArray[21, 22, 23, 26, 27 ,28]
  // and if a new entry with key 25 and RecordId 25 is added to x, so x is splited as
  // oldX[21, 22, 23] newX[25, 26, 27, 28] for both keyArray and redArray
  // now, the status of the current nonleafnode becomes
  // keyArray        [10,       20,          30,       40,     0, 0] 
  // pageNoArray [y<10, 10<=y<20, 20<=oldx<25, 30<=y<40, 40<=y, 0, 0] 
  int i = nodeOccupancy;
  while (i >= 0 && (nonleaf->pageNoArray[i] == 0)) {
    i--;
  } // i = 6 -> 5 -> 4
  while (i > 0 && (nonleaf->keyArray[i - 1] > entry->key)) {
    nonleaf->keyArray[i] = nonleaf->keyArray[i - 1];
    nonleaf->pageNoArray[i + 1] = nonleaf->pageNoArray[i];
    i--;
  } 
  // keyArray        [10,       20,           0, 30,       40,     0]
  // pageNoArray [y<10, 10<=y<20, 20<=oldx<25, ?,  30<=y<40, 40<=y, 0] 
  nonleaf->keyArray[i] = entry->key;
  nonleaf->pageNoArray[i + 1] = entry->pageNo;
  // keyArray        [10,       20,          25,           30,       40,     0]
  // pageNoArray [y<10, 10<=y<20, 20<=oldx<25, 25<=newx<30,  30<=y<40, 40<=y, 0] 
}

/**
 * This method splits a nonleafnode when it is full
 * 
 * @param curNode is given as input
 * @param curPageNum is given as input 
 * @param newchildEntry has the lowest key of the new page/node
 */
const void BTreeIndex::splitNonLeaf(NonLeafNodeInt *curNode, PageId curPageNum, 
                                    PageKeyPair<int> *&newchildEntry) {
  // allocate a new nonleaf node and its pageNumber
  PageId newPageNum;
  Page *newPage;
  bufMgr->allocPage(file, newPageNum, newPage);
  NonLeafNodeInt *newNode = (NonLeafNodeInt *) newPage;

  // ex. for a nonleafnode with keyArray[INTARRAYNONLEAFSIZE = 5]
  // keyArray        [10,       20,       30,       40,       50,       60] 
  // pageNoArray [y<10, 10<=y<20, 20<=x<30, 30<=y<40, 40<=y<50, 50<=y<60, 60<=y] 
  // and assume that x is a leafnode/leafpage, and its previous status is
  // keyArray[21, 22, 23, 26, 27, 28]
  // ridArray[21, 22, 23, 26, 27 ,28]
  // and if a newchildEntry with key 25 and RecordId 25 is added to x, so x is splited as
  // oldX[21, 22, 23] newX[25, 26, 27, 28] for both keyArray and redArray
  // now, the status of the current nonleafnode becomes
  // keyArray        [10,       20,          30,       40,       50,       60] 
  // pageNoArray [y<10, 10<=y<20, 20<=oldx<25, 30<=y<40, 40<=y<50, 50<=y<60, 60<=y]
  // get the entry that needs to be push up to the parentNode 
  int mid = nodeOccupancy / 2; 
  int pushupIndex = mid;
  PageKeyPair<int> pushupEntry;
  // even number of keys
  if (nodeOccupancy % 2 == 0) {
    pushupIndex = newchildEntry->key < curNode->keyArray[mid]? mid - 1:mid;
    // pushupIndex = 3 - 1 = 2 -> curNode->keyArray[2] = 30
  }
  // parentNode uses PageKeyPair<int> to update its pageNoArray[] and keyArray[]
  pushupEntry.set(newPageNum, curNode->keyArray[pushupIndex]);

  // move half the entries to the new node
  mid = pushupIndex + 1; // mid = 2 + 1
  newNode->pageNoArray[0] = curNode->pageNoArray[mid];
  //     [10,  20,       30,   40,   50,    60]           [0, 0, 0, 0, 0, 0] 
  // [y<10, y<20, oldx<25, y<40, y<50, y<60, 60<=y]  [y<40, ?, ?, ?, ?, ?, ?]
  int index = mid; // index = mid = 3 
  while (index < nodeOccupancy) {
    // index - mid = 0, 1, 2 while index = 3, 4, 5
    newNode->keyArray[index - mid] = curNode->keyArray[index];
    curNode->keyArray[index] = 0;
    // index - mid + 1 = 1, 2, 3 while index + 1 = 4, 5, 6
    newNode->pageNoArray[index - mid + 1] = curNode->pageNoArray[index + 1];
    curNode->pageNoArray[index] = (PageId) 0;
    index++;
  }
  curNode->pageNoArray[index] = (PageId) 0;
  //     [10,  20,       30, 0, 0, 0]       [40,   50,   60,     0, 0, 0] 
  // [y<10, y<20, oldx<25, ?, ?, ?, ?]  [y<40, y<50, y<60, y<=60, ?, ?, ?]
  newNode->level = curNode->level; // sibling node has the same level as current node
  curNode->keyArray[pushupIndex] = 0; // remove the entry that is pushed up from current node
  //     [10,  20,        0, 0, 0, 0]       [40,   50,   60,     0, 0, 0] 
  // [y<10, y<20, oldx<25, ?, ?, ?, ?]  [y<40, y<50, y<60, y<=60, ?, ?, ?]
  insertNonLeaf(newchildEntry->key < newNode->keyArray[0]? curNode:newNode, newchildEntry);
  //     [10,  20,       25,       0, 0, 0]       [40,   50,   60,     0, 0, 0] 
  // [y<10, y<20, oldx<25, newX<30, ?, ?, ?]  [y<40, y<50, y<60, y<=60, ?, ?, ?]

  newchildEntry = new PageKeyPair<int>();
  newchildEntry = &pushupEntry;
  bufMgr->unPinPage(file, curPageNum, true);
  bufMgr->unPinPage(file, newPageNum, true);

  // if the curNode is the root
  if (curPageNum == rootPageNum){
    updateRoot(curPageNum, newchildEntry, false);
  }
}

/**
 * This method adds a RIDKeyPair to a leafnode
 * 
 * @param lead the current node to be updated
 * @param entry entry->RecordId updates the ridArray[] and entry->key updates the keyArray[]
 */
const void BTreeIndex::insertLeaf(LeafNodeInt *leaf, RIDKeyPair<int> entry) {
  // empty leaf page
  if (leaf->ridArray[0].page_number == 0) {
    leaf->keyArray[0] = entry.key;
    leaf->ridArray[0] = entry.rid;    
  } else {
    int i = leafOccupancy - 1;
    // find the end
    while(i >= 0 && (leaf->ridArray[i].page_number == 0)) {
      i--;
    }
    // shift entry
    while(i >= 0 && (leaf->keyArray[i] > entry.key)) {
      leaf->keyArray[i + 1] = leaf->keyArray[i];
      leaf->ridArray[i + 1] = leaf->ridArray[i];
      i--;
    }
    // insert entry
    leaf->keyArray[i + 1] = entry.key;
    leaf->ridArray[i + 1] = entry.rid;
  }
}

/**
 * This method splits a LeafNode when it is full
 * 
 * @param curNode is given as input
 * @param curPageNum is given as input 
 * @param newchildEntry carries a new entry to parent node of current node
 * @param dataEntry entry->RecordId updates the ridArray[] and entry->key updates the keyArray[]
 */
const void BTreeIndex::splitLeaf(LeafNodeInt *curNode, PageId curPageNum, 
                                PageKeyPair<int> *&newchildEntry, const RIDKeyPair<int> dataEntry) {
  // allocate a new leaf page
  PageId newPageNum;
  Page *newPage;
  bufMgr->allocPage(file, newPageNum, newPage);
  LeafNodeInt *newLeafNode = (LeafNodeInt *) newPage;

  // even number of keys
  // oldKeyArray [10, 20, 30, 40, 50, 60] (keyArray[INTARRAYLEAFSIZE])
  // oldridArray [10, 20, 30, 40, 50, 60] (ridArray[INTARRAYLEAFSIZE])
  int mid = leafOccupancy / 2;
  // odd number of keys
  // oldKeyArray [10, 20, 30, 40, 50] (keyArray[INTARRAYLEAFSIZE])
  // oldridArray [10, 20, 30, 40, 50] (ridArray[INTARRAYLEAFSIZE])
  if (leafOccupancy % 2 == 1 && dataEntry.key > curNode->keyArray[mid]) {
    mid = mid + 1;
  }

  // move half the pages to newLeafNode
  for (int i = mid; i < leafOccupancy; i++) {
    newLeafNode->keyArray[i - mid] = curNode->keyArray[i];
    newLeafNode->ridArray[i - mid] = curNode->ridArray[i];
    curNode->keyArray[i] = 0;
    curNode->ridArray[i].page_number = 0;
  }
  // even number of keys
  // [10, 20, 30, 40, 50, 60] -> [10, 20, 30, 0, 0] [40, 50, 60, 0, 0]
  // odd number of keys
  // [10, 20, 30, 40, 50] -> [10, 20, 30, 0, 0] [40, 50, 0, 0, 0] when dataEntry.key = 35
  // [10, 20, 30, 40, 50] -> [10, 20, 0, 0, 0] [30, 40, 50, 0, 0] when dataEntry.key = 25
  
  // insert dataEntry into newLeadNode of curNode
  if (dataEntry.key > curNode->keyArray[mid - 1]) {
    insertLeaf(newLeafNode, dataEntry);
  } else {
    insertLeaf(curNode, dataEntry);
  }
  // even number of keys
  // [10, 20, 30, 40, 50, 60] -> [10, 20, 30, 0, 0, 0] [35, 40, 50, 60, 0, 0] if dataEntry.key = 35
  // [10, 20, 30, 40, 50, 60] -> [10, 20, 25, 30, 0, 0] [40, 50, 60, 0, 0, 0] if dataEntry.key = 25
  // odd number of keys
  // [10, 20, 30, 40, 50] -> [10, 20, 30, 0, 0] [35, 40, 50, 0, 0] when dataEntry.key = 35
  // [10, 20, 30, 40, 50] -> [10, 20, 25, 0, 0] [30, 40, 50, 0, 0] when dataEntry.key = 25

  // update sibling pointer of curNode and newLeafNode
  newLeafNode->rightSibPageNo = curNode->rightSibPageNo;
  curNode->rightSibPageNo = newPageNum;

  // std::cout << "check leafnodes" << std::endl;
  { 
    // for (int i = 0; i < leafOccupancy; i++) {
    //   std::cout << curNode->keyArray[i] << " ";
    // }
    // std::cout << std::endl;
    // for (int i = 0; i < leafOccupancy; i++) {
    //   std::cout << newLeafNode->keyArray[i] << " ";
    // }
  }

  // the smallest key from second page as the new child entry
  newchildEntry = new PageKeyPair<int>();
  PageKeyPair<int> newKeyPair;
  newKeyPair.set(newPageNum, newLeafNode->keyArray[0]); // newKeyPair.set(pageNo, key)
  newchildEntry = &newKeyPair;
  bufMgr->unPinPage(file, curPageNum, true);
  bufMgr->unPinPage(file, newPageNum, true);

  // if curNode is root
  if (curPageNum == rootPageNum) {
    updateRoot(curPageNum, newchildEntry, true);
  }
}

/**
 * This method creates a new root node when the curRoot needs to be splited
 * 
 * @param curRootPageNum is given as input
 * @param newchildEntry is given as input
 */
const void BTreeIndex::updateRoot(PageId curRootPageNum, PageKeyPair<int> *newchildEntry, bool fromLeaf) {
  // create a new root 
  PageId newRootPageNum;
  Page *newRoot;
  bufMgr->allocPage(file, newRootPageNum, newRoot);
  NonLeafNodeInt *newRootPage = (NonLeafNodeInt *) newRoot;

  // update metadata
  newRootPage->level = fromLeaf? 1:0;
  newRootPage->pageNoArray[0] = curRootPageNum;
  newRootPage->pageNoArray[1] = newchildEntry->pageNo;
  newRootPage->keyArray[0] = newchildEntry->key;

  Page *meta;
  bufMgr->readPage(file, headerPageNum, meta);
  IndexMetaInfo *metaPage = (IndexMetaInfo *) meta;
  metaPage->rootPageNo = newRootPageNum;
  rootPageNum = newRootPageNum;

  // std::cout << "check newRoot" << std::endl;
  {
    // for (int i = 0; i < nodeOccupancy; i++) {
    //   std::cout << newRootPage->keyArray[i] << " ";
    // }
  }

  // unpin unused page
  bufMgr->unPinPage(file, headerPageNum, true);
  bufMgr->unPinPage(file, newRootPageNum, true);

  // a new page is used when updating root page 
  numNonLeafNodes++;
}

/**
 * This method seeks all entries to find the lowest value within a range that is 
 * greater than "a" and less than or equal to "d" ("a",GT,"d",LTE) 
 */
const void BTreeIndex::startScan(const void* lowValParm, const Operator lowOpParm,
				   const void* highValParm, const Operator highOpParm) { 
	lowValInt = *((int *)lowValParm);   // low INTEGER value for scan.
  highValInt = *((int *)highValParm); // high INTEGER value for scan.

  // if (x < low || x <= low) || (x > high || x >= high), then throw exception 
  if (!((lowOpParm == GT || lowOpParm == GTE) && (highOpParm == LT || highOpParm == LTE))) {
    throw BadOpcodesException();
  }
  if (lowValInt > highValInt) {
    throw BadScanrangeException();
  }
  lowOp = lowOpParm; // low Operator can only be GT(>) or GTE(>=).
  highOp = highOpParm; // high Operator can only be LT(<) or LTE(<=).
  
  // scan is already started
  if (scanExecuting) {
    endScan();
  }

  // start scanning by reading rootpage into the buffer pool
  currentPageNum = rootPageNum;
  bufMgr->readPage(file, currentPageNum, currentPageData);
  // root is not a leaf
  if (initialPageNum != rootPageNum) { 
    // build a curNode for taversing from root to a leafNode
    NonLeafNodeInt* curNode = nullptr;
    bool foundLeaf = false;
    curNode = (NonLeafNodeInt *) currentPageData;
    while (!foundLeaf) { 
      // cast page to node
      curNode = (NonLeafNodeInt *) currentPageData;
      // check if this is the level above the leaf, if yes, the next level is the leaf
      if (curNode->level == 1) { 
        foundLeaf = true;
      }

      // get the next page/node
      PageId nextPageNum;
      findNextNonLeafNode(curNode, nextPageNum, lowValInt);
      bufMgr->unPinPage(file, currentPageNum, false);
      currentPageNum = nextPageNum;
      bufMgr->readPage(file, currentPageNum, currentPageData);
    }
  }

  // the curNode is leaf node, try to find the smallest one that satisefy the OP
  bool found = false;
  while (!found) { 
    // cast page to node
    LeafNodeInt* curNode = (LeafNodeInt *) currentPageData;
    // check if the whole page is null, if it is null, then there no such key
    if (curNode->ridArray[0].page_number == 0) {
      bufMgr->unPinPage(file, currentPageNum, false);
      throw NoSuchKeyFoundException();
    }
    // search from the left of leaf node to the right of leaf node to find the fit
    bool nullVal = false;
    for (int i = 0; i < leafOccupancy && !nullVal; i++) {
      int key = curNode->keyArray[i];
      // check if the next record is not inserted
      if (i < leafOccupancy - 1 && curNode->ridArray[i + 1].page_number == 0) {
        nullVal = true;
      }
      // check whether the key is within the range
      if (validKey(lowValInt, lowOp, highValInt, highOp, key)) {
        // select
        nextEntry = i; // index of next entry to be scanned in the current leaf 
        found = true;
        scanExecuting = true;
        break;
      } else if ((highOp == LT && key >= highValInt) || (highOp == LTE && key > highValInt)) {
        bufMgr->unPinPage(file, currentPageNum, false);
        throw NoSuchKeyFoundException();
      }

      // did not find any matching key in this leaf, go to next leaf
      if (i == leafOccupancy - 1 || nullVal) {
        // unpin page
        bufMgr->unPinPage(file, currentPageNum, false);
        // did not find the matching one in the more right leaf
        if (curNode->rightSibPageNo == 0) {
          throw NoSuchKeyFoundException();
        }

        currentPageNum = curNode->rightSibPageNo;
        bufMgr->readPage(file, currentPageNum, currentPageData);
      }
    }
  }
} 

/**
 * This method returns the next record from current page being scanned, if current page has been 
 * scanned to its entirety, move on to the right sibling of current page, if any exists, to start 
 * scanning that page. 
 */
const void BTreeIndex::scanNext(RecordId& outRid) { 
  // if we are not executing scan, then we can't scan next
	if (!scanExecuting) {
    throw ScanNotInitializedException();
  }

	LeafNodeInt* curNode = (LeafNodeInt *) currentPageData;
  // go to the next page if we need
  if (curNode->ridArray[nextEntry].page_number == 0 || nextEntry == leafOccupancy) { 
    // no more next leaf
    if (curNode->rightSibPageNo == 0) {
      throw IndexScanCompletedException();
    }

    // unpin page and read papge
    bufMgr->unPinPage(file, currentPageNum, false);
    currentPageNum = curNode->rightSibPageNo;
    bufMgr->readPage(file, currentPageNum, currentPageData);
    curNode = (LeafNodeInt *) currentPageData;

    // reset nextEntry
    nextEntry = 0;
  }
 
  // check if rid satisfy
  int key = curNode->keyArray[nextEntry];
  if (validKey(lowValInt, lowOp, highValInt, highOp, key)) { 
    outRid = curNode->ridArray[nextEntry];
    nextEntry++; // incrment nextEntry
  } else { 
    // if current file has been scanned to its entirety
    throw IndexScanCompletedException();
  }
}

/**
 * This method terminates the current scan. Unpin any pinned pages. Reset scan specific variables.
 */
const void BTreeIndex::endScan() {
	if (!scanExecuting) {
    throw ScanNotInitializedException();
  }
  scanExecuting = false;
  // unpin page
  bufMgr->unPinPage(file, currentPageNum, false);
  // reset variable
  currentPageData = nullptr;
  currentPageNum = static_cast<PageId>(-1);
  nextEntry = -1;
}

/**
 * This method checks whether a key is satisfied with the given operators and range.
 */
const bool BTreeIndex::validKey(int lowVal, const Operator lowOp, int highVal, 
                                const Operator highOp, int key) {
  if (lowOp == GTE && highOp == LTE) {
    return key <= highVal && key >= lowVal;
  } else if (lowOp == GTE && highOp == LT) {
    return key < highVal && key >= lowVal;
  } else if (lowOp == GT && highOp == LTE) {
    return key <= highVal && key > lowVal;
  } else {
    return key < highVal && key > lowVal;
  }
}

}
