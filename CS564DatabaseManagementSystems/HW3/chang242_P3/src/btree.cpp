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

// -----------------------------------------------------------------------------
// BTreeIndex::BTreeIndex -- Constructor
// -----------------------------------------------------------------------------

/**
 * BTreeIndex Constructor. 
 * Check to see if the corresponding index file exists. If so, open the file.
 * If not, create it and insert entries for every tuple in the base relation using FileScan class.
 * 
 * The name of the relation on which to build the index. 
 * The constructor should scan this relation (using ​FileScan​)
 * and insert entries for all the tuples in this relation into the index. 
 * You can insert an entry one-by-one.
 * 
 * The name of the index file; determine this name in the constructor as shown above, 
 * and return the name.
 * 
 * The instance of the global buffer manager.
 * 
 * The byte offset of the attribute in the tuple on which to build the index.
 * 
 * The data type of the attribute we are indexing.
 */
BTreeIndex::BTreeIndex(const std::string &relationName,
		std::string &outIndexName,
		BufMgr *bufMgrIn,
		const int attrByteOffset,
		const Datatype attrType)
{	

	// default setting for building btree
	this->bufMgr = bufMgrIn; 		 	 	
	this->attributeType = attrType; 		 	      // Datatype of attribute over which index is built
	this->attrByteOffset = attrByteOffset;   	  // Offset of attribute, over which index is built, inside records 
	this->leafOccupancy = INTARRAYLEAFSIZE;  	  // Number of keys in leaf node, depending upon the type of key
	this->nodeOccupancy = INTARRAYNONLEAFSIZE;	// Number of keys in non-leaf node, depending upon the type of key
  this->numNonLeafNodes = 0;                  // Number of NonLeafNodes
  this->numLeafNodes = 0;                     // Number of LeafNodes

	// default setting for scanning btree
	scanExecuting = false;

	// an index file name is constructed as “​relName.attrOffset​”. 
	// indexName is the name of the index file
	std::ostringstream idxStr;
	idxStr<<relationName<<'.'<<attrByteOffset;
	outIndexName = idxStr.str(); 

	// Check whether the corresponding index file exists. If so, open the file. If not, create it.
	// we can determine if an index file already exists by seeing if that exception gets thrown 
	// when you try to create the file with the create_new flag set to false, 
	// if it does get thrown, you know that you need to create a new one. 
	try {
		this->file = new BlobFile(outIndexName, false);
		std::cout << "BlobFile " << outIndexName << " exists.\n";

		// get header page of the index file, and check whether meta-data is correct
		this->headerPageNum = this->file->getFirstPageNo(); // Page number of meta page
		Page *headerPage;
		bufMgr->readPage(this->file, this->headerPageNum, headerPage);
		IndexMetaInfo *meta = (IndexMetaInfo *)headerPage;
		this->rootPageNum = meta->rootPageNo; // page number of root page of B+ tree inside index file
		// check whether meta-data match
		// std::cout << "relationName " << relationName << " " <<  meta->relationName[0] << "\n";
		// std::cout << "attrType " << attrType << " " <<  meta->attrType << "\n";
		// std::cout << "attrByteOffset " << attrByteOffset << " " <<  meta->attrByteOffset << "\n";
		if (relationName.compare(meta->relationName) != 0 || attrType != meta->attrType || attrByteOffset != meta->attrByteOffset)
		{
			throw BadIndexInfoException(outIndexName);
		}

		// unpin headerpage from memory since it is no longer required to remain in memory
		// true if the page to be unpinned is updated	
		bufMgr->unPinPage(file, headerPageNum, false);  
	} catch (FileNotFoundException &e) {
		std::cout << "create BlobFile " << outIndexName << ".\n";
		this->file = new BlobFile(outIndexName, true);

		// dedicate a header page for storing meta-data of the index file
		// allocate header page
		Page *headerPage;
		bufMgr->allocPage(file, headerPageNum, headerPage);

		// build meta info
		IndexMetaInfo *meta = (IndexMetaInfo *)headerPage;
		meta->attrByteOffset = attrByteOffset;
		meta->attrType = attrType;
		strncpy((char *)(&(meta->relationName)), relationName.c_str(), 20);
		// meta->relationName[19] = 0;
		
		// allocate root page
		Page *rootPage;
		bufMgr->allocPage(file, rootPageNum, rootPage);
		meta->rootPageNo = rootPageNum;
		initialPageNum = rootPageNum; // initialPageNum won't change, but rootPageNum will change as tree grows
		LeafNodeInt *root = (LeafNodeInt *)rootPage;
		root->rightSibPageNo = 0;

    // one page is used for root page
    numLeafNodes++;

		// unpin headerpage from memory since it is no longer required to remain in memory
		// true if the page to be unpinned is updated	
		bufMgr->unPinPage(file, headerPageNum, true);
		bufMgr->unPinPage(file, rootPageNum, true);

		// the constructor should scan this relation (using ​FileScan​) 
		// and insert entries for all the tuples in this relation into the index.
		FileScan fileScan(relationName, bufMgr);
		try
		{	
			RecordId scanRid;
			while(1)
			{
				fileScan.scanNext(scanRid);
				std::string recordStr = fileScan.getRecord();
				insertEntry(recordStr.c_str() + attrByteOffset, scanRid);
			}
		}
		catch(EndOfFileException e)
		{
			// save Btee index file to disk
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
  std::cout << "initialPageNum: " << initialPageNum << "\n";
  std::cout << "leafOccupancy: " << leafOccupancy << "\n";
  std::cout << "nodeOccupancy: " << nodeOccupancy << "\n";
  std::cout << "currentPageNum: " << currentPageNum << "\n";
  std::cout << "numNonLeafNodes: " << numNonLeafNodes << "\n";
  std::cout << "numLeafNodes: " << numLeafNodes << "\n";
}

/**
 * Retrun how many NonLeafNodes in the BPTree
 */
const int BTreeIndex::getNumNonLeafNodes() {
  return numNonLeafNodes;
}

/**
 * Retrun how many LeafNodes in the BPTree
 */
const int BTreeIndex::getNumLeafNodes() {
  return numLeafNodes;
}

// -----------------------------------------------------------------------------
// BTreeIndex::~BTreeIndex -- destructor
// -----------------------------------------------------------------------------

/**
 * The destructor. Perform any cleanup that may be necessary, including clearing up any state variables, unpinning any B+ tree pages that are pinned, and flushing the index file (by calling the function bufMgr->flushFile()​). Note that this method does not delete the index file! But, deletion of the ​file object is required, which will call the destructor of ​File​ class causing the index file to be closed.
 */
BTreeIndex::~BTreeIndex()
{
	scanExecuting = false;
	this->bufMgr->flushFile(BTreeIndex::file);
	delete this->file;
	this->file = nullptr;
}

// -----------------------------------------------------------------------------
// BTreeIndex::insertEntry
// -----------------------------------------------------------------------------

/**
 * This method inserts a new entry into the index using the pair <key, rid>.
 * const void *key: pointer to the value (integer) we want to insert.
 * const RecordId rid: the corresponding record id of the tuple in the base relation.
 */
const void BTreeIndex::insertEntry(const void *key, const RecordId rid) 
{
	// std::cout << "page_number: " << rid.page_number << ", slot_number: " << rid.slot_number << "key: " << key << std::endl;
	// get root
	Page* rootPage;
	bufMgr->readPage(file, rootPageNum, rootPage);

  // RIDKeyPair is added to the leaf node/pages of the tree
  // key: Integer, value: RecordId
  RIDKeyPair<int> dataEntry; 
	dataEntry.set(rid, *((int *)key));

  // if childEntry is not a nullptr, the we need to grow BPtree
	PageKeyPair<int> *childEntry = nullptr;
	insertNode(rootPage, rootPageNum, initialPageNum == rootPageNum? true:false, dataEntry, childEntry);
}

/**
 * This method implements how we traverse nonleafnodes and leafnodes, 
 * if leafnode is not full, we just insert dadaEntry into it, otherwise, we split leafnode
 * 
 * if leafnode is full, it means that we need to add a new key to the nonleadnode,
 * if nonleadnode is full, we split nonleafnode
 * 
 * note. a node is a page
 */
const void BTreeIndex::insertNode(Page *curPage, PageId curPageNum, bool nodeIsLeaf, const RIDKeyPair<int> dataEntry, PageKeyPair<int> *&newchildEntry)
{
  // insert for nonleaf node
  if (!nodeIsLeaf)
  { 
    // find the right key to traverse
    NonLeafNodeInt *curNode = (NonLeafNodeInt *)curPage;
    Page *nextPage;
    PageId nextPageNum;
    findNextNonLeafNode(curNode, nextPageNum, dataEntry.key);
    bufMgr->readPage(file, nextPageNum, nextPage);
    insertNode(nextPage, nextPageNum, curNode->level == 1, dataEntry, newchildEntry);
    
    // no split in child, just return
    if (newchildEntry == nullptr)
    {
	    // unpin current page from call stack
	    bufMgr->unPinPage(file, curPageNum, false);
    }
    // if a childnode splits, 
    // then current node needs to add a new key, which is the key of the newchildEntry,
    // and the corresponding value should be the pageNo of newchildEntry
    else
	  { 
      // if the curpage is not full
      if (curNode->pageNoArray[nodeOccupancy] == 0)
      {
        // insert the newchildEntry to curpage
        // curNode->keyArray[] stores the key of the newchildEntry
        // curNode->pageNoArray[] stores the pageNo of newchildEntry
        insertNonLeaf(curNode, newchildEntry);
        newchildEntry = nullptr;
        // finish the insert process, unpin current page
        bufMgr->unPinPage(file, curPageNum, true);
      }
      else
      { 
        // if the curpage isfull, we split curpage and update newchildEntry,
        // so the parentpage of curpage can use the updated newchildEntry to update parentpage itself
        splitNonLeaf(curNode, curPageNum, newchildEntry);

        // a new page is used when spliting 
        numNonLeafNodes++;
      }
    }
  }
  // insert for leaf node
  else
  {
    LeafNodeInt *leaf = (LeafNodeInt *)curPage;
    // page is not full
    if (leaf->ridArray[leafOccupancy - 1].page_number == 0)
    {
      insertLeaf(leaf, dataEntry);
      bufMgr->unPinPage(file, curPageNum, true);
      newchildEntry = nullptr;
    }
    else
    {
      splitLeaf(leaf, curPageNum, newchildEntry, dataEntry);
      numLeafNodes++;
    }
  }
}

/**
 * Find the childNode of the curNode based on key
 */
const void BTreeIndex::findNextNonLeafNode(NonLeafNodeInt *curNode, PageId &nextPageNum, int key)
{ 
  // nodeOccupancy means the max number of key-value pairs in a NonleafNode
  // ex. NonleafNode = 4, key = 30
  // pageNoArray [x<=10, 10<x<=20, 20<x<=30, 30<x<=40, 40<x]
  // keyArray     [10,20,30,40]
  int i = nodeOccupancy; 
  while (i >= 0 && (curNode->pageNoArray[i] == 0))
  {
    i--;
  }
  while (i > 0 && (curNode->keyArray[i - 1] >= key))
  {
    i--;
  }
  
  nextPageNum = curNode->pageNoArray[i];
}

/**
 * This method adds a PageKeyPair into a nonLeadNode
 * 
 * entry->pageNo updates the pageNoArray[] of the node
 * entry->key updates the keyArray[] of the node
 */
const void BTreeIndex::insertNonLeaf(NonLeafNodeInt *nonleaf, PageKeyPair<int> *entry)
{
  int i = nodeOccupancy;
  while (i >= 0 && (nonleaf->pageNoArray[i] == 0))
  {
    i--;
  }
  // shift
  while (i > 0 && (nonleaf->keyArray[i-1] > entry->key))
  {
    nonleaf->keyArray[i] = nonleaf->keyArray[i-1];
    nonleaf->pageNoArray[i+1] = nonleaf->pageNoArray[i];
    i--;
  }
  // insert
  nonleaf->keyArray[i] = entry->key;
  nonleaf->pageNoArray[i+1] = entry->pageNo;
}

/**
 * This method splits a NonLeafNode when it is full
 */
const void BTreeIndex::splitNonLeaf(NonLeafNodeInt *curNode, PageId curPageNum, PageKeyPair<int> *&newchildEntry)
{
  // allocate a new nonleaf node
  PageId newPageNum;
  Page *newPage;
  bufMgr->allocPage(file, newPageNum, newPage);
  NonLeafNodeInt *newNode = (NonLeafNodeInt *)newPage;

  // get the entry that needs to be push up to the parentNode 
  int mid = nodeOccupancy / 2; 
  int pushupIndex = mid;
  PageKeyPair<int> pushupEntry;
  // even number of keys
  if (nodeOccupancy % 2 == 0)
  {
    pushupIndex = newchildEntry->key < curNode->keyArray[mid]? mid - 1:mid;
  }
  // parentNode uses PageKeyPair<int> to update its pageNoArray[] and keyArray[]
  // set newPageNum in PageKeyPair<int> to update parentNode's pageNoArray[] in the future
  // set curNode->keyArray[pushupIndex] in PageKeyPair<int> to update the keyArray[] of the parentNode in the future
  pushupEntry.set(newPageNum, curNode->keyArray[pushupIndex]);

  // move half the entries to the new node
  mid = pushupIndex + 1;
  newNode->pageNoArray[0] = curNode->pageNoArray[mid];
  int index = mid;
  while (index < nodeOccupancy) {
    newNode->keyArray[index - mid] = curNode->keyArray[index];
    newNode->pageNoArray[index - mid + 1] = curNode->pageNoArray[index + 1];
    curNode->pageNoArray[index] = (PageId) 0;
    curNode->keyArray[index] = 0;
    index++;
  }
  curNode->pageNoArray[index] = (PageId) 0;

  // sibling node has the same level as current node
  newNode->level = curNode->level;
  // remove the entry that is pushed up from current node
  curNode->keyArray[pushupIndex] = 0;
  // insert the new child entry
  insertNonLeaf(newchildEntry->key < newNode->keyArray[0]? curNode:newNode, newchildEntry);
  // newchildEntry = new PageKeyPair<int>();
  newchildEntry = &pushupEntry;
  bufMgr->unPinPage(file, curPageNum, true);
  bufMgr->unPinPage(file, newPageNum, true);

  // if the curNode is the root
  if (curPageNum == rootPageNum)
  {
    updateRoot(curPageNum, newchildEntry, false);
  }
}

/**
 * This method adds a RIDKeyPair into a LeafNode
 * 
 * entry->RecordId updates the ridArray[] of the node
 * entry->key updates the keyArray[] of the node
 */
const void BTreeIndex::insertLeaf(LeafNodeInt *leaf, RIDKeyPair<int> entry)
{
  // empty leaf page
  if (leaf->ridArray[0].page_number == 0)
  {
    leaf->keyArray[0] = entry.key;
    leaf->ridArray[0] = entry.rid;    
  }
  else
  {
    int i = leafOccupancy - 1;
    // find the end
    while(i >= 0 && (leaf->ridArray[i].page_number == 0))
    {
      i--;
    }
    // shift entry
    while(i >= 0 && (leaf->keyArray[i] > entry.key))
    {
      leaf->keyArray[i+1] = leaf->keyArray[i];
      leaf->ridArray[i+1] = leaf->ridArray[i];
      i--;
    }
    // insert entry
    leaf->keyArray[i+1] = entry.key;
    leaf->ridArray[i+1] = entry.rid;
  }
}

/**
 * This method splits a LeafNode when it is full
 */
const void BTreeIndex::splitLeaf(LeafNodeInt *curNode, PageId curPageNum, PageKeyPair<int> *&newchildEntry, const RIDKeyPair<int> dataEntry)
{
  // allocate a new leaf page
  PageId newPageNum;
  Page *newPage;
  bufMgr->allocPage(file, newPageNum, newPage);
  LeafNodeInt *newLeafNode = (LeafNodeInt *)newPage;

  // odd number of keys
  int mid = leafOccupancy / 2;
  if (leafOccupancy % 2 == 1 && dataEntry.key > curNode->keyArray[mid])
  {
    mid = mid + 1;
  }

  // move half the pages to newLeafNode
  for (int i = mid; i < leafOccupancy; i++)
  {
    newLeafNode->keyArray[i-mid] = curNode->keyArray[i];
    newLeafNode->ridArray[i-mid] = curNode->ridArray[i];
    curNode->keyArray[i] = 0;
    curNode->ridArray[i].page_number = 0;
  }
  
  // insert dataEntry into newLeadNode of curNode
  if (dataEntry.key > curNode->keyArray[mid - 1])
  {
    insertLeaf(newLeafNode, dataEntry);
  }
  else
  {
    insertLeaf(curNode, dataEntry);
  }

  // update sibling pointer of curNode and newLeafNode
  newLeafNode->rightSibPageNo = curNode->rightSibPageNo;
  curNode->rightSibPageNo = newPageNum;

  // the smallest key from second page as the new child entry
  newchildEntry = new PageKeyPair<int>();
  PageKeyPair<int> newKeyPair;
  newKeyPair.set(newPageNum, newLeafNode->keyArray[0]);
  newchildEntry = &newKeyPair;
  bufMgr->unPinPage(file, curPageNum, true);
  bufMgr->unPinPage(file, newPageNum, true);

  // if curNode is root
  if (curPageNum == rootPageNum)
  {
    updateRoot(curPageNum, newchildEntry, true);
  }
}

/**
 * This method creates a new root node when the curRoot needs to be splited
 */
const void BTreeIndex::updateRoot(PageId curRootPageNum, PageKeyPair<int> *newchildEntry, bool fromLeaf)
{
  // create a new root 
  PageId newRootPageNum;
  Page *newRoot;
  bufMgr->allocPage(file, newRootPageNum, newRoot);
  NonLeafNodeInt *newRootPage = (NonLeafNodeInt *)newRoot;

  // update metadata
  newRootPage->level = fromLeaf? 1:0;
  newRootPage->pageNoArray[0] = curRootPageNum;
  newRootPage->pageNoArray[1] = newchildEntry->pageNo;
  newRootPage->keyArray[0] = newchildEntry->key;

  Page *meta;
  bufMgr->readPage(file, headerPageNum, meta);
  IndexMetaInfo *metaPage = (IndexMetaInfo *)meta;
  metaPage->rootPageNo = newRootPageNum;
  rootPageNum = newRootPageNum;
  // unpin unused page
  bufMgr->unPinPage(file, headerPageNum, true);
  bufMgr->unPinPage(file, newRootPageNum, true);

  // a new page is used when updating root page 
  numNonLeafNodes++;
}

// -----------------------------------------------------------------------------
// BTreeIndex::startScan
// -----------------------------------------------------------------------------

/**
 * This method seeks all entries with a value 
 * greater than "a" and less than or equal to "d" ("a",GT,"d",LTE) 
 */
const void BTreeIndex::startScan(const void* lowValParm,
				   const Operator lowOpParm,
				   const void* highValParm,
				   const Operator highOpParm)
{ 
	lowValInt = *((int *)lowValParm);   // low INTEGER value for scan.
  highValInt = *((int *)highValParm); // high INTEGER value for scan.

  // if (x < low || x <= low) || (x > high || x >= high), then throw exception 
  if(!((lowOpParm == GT || lowOpParm == GTE) && (highOpParm == LT || highOpParm == LTE)))
  {
    throw BadOpcodesException();
  }
  if(lowValInt > highValInt)
  {
    throw BadScanrangeException();
  }

  lowOp = lowOpParm; // low Operator: can only be GT(>) or GTE(>=).
  highOp = highOpParm; // high Operator. can only be LT(<) or LTE(<=).
  // scan is already started
  if (scanExecuting)
  {
    endScan();
  }
  // start scanning by reading rootpage into the buffer pool
  currentPageNum = rootPageNum;
  bufMgr->readPage(file, currentPageNum, currentPageData);
  // std::cout << "cehck 1 \n";
  // root is not a leaf
  if (initialPageNum != rootPageNum)
  { 
    // build a curNode for taversing from root to a leafNode
    NonLeafNodeInt* curNode = nullptr;
    bool foundLeaf = false;
    curNode = (NonLeafNodeInt *) currentPageData;
    while (!foundLeaf)
    { 
      // cast page to node
      curNode = (NonLeafNodeInt *) currentPageData;
      // std::cout << "step1.4.1 rootPageNum:" << rootPageNum << "\n";

      // check if this is the level above the leaf, if yes, the next level is the leaf
      if (curNode->level == 1)
      { 
        foundLeaf = true;
      }

      // find the leaf
      PageId nextPageNum;
      findNextNonLeafNode(curNode, nextPageNum, lowValInt);
      // unpin
      bufMgr->unPinPage(file, currentPageNum, false);
      currentPageNum = nextPageNum;
      // std::cout << "step1.4.3 nextPageNum: " << nextPageNum << "\n";
      // read the nextPage
      bufMgr->readPage(file, currentPageNum, currentPageData);
    }
  }

  // now the curNode is leaf node try to find the smallest one that satisefy the OP
  bool found = false;
  while (!found){ 
    // cast page to node
    LeafNodeInt* curNode = (LeafNodeInt *) currentPageData;
    // check if the whole page is null, if it is null, then there no such key
    if (curNode->ridArray[0].page_number == 0)
    {
      bufMgr->unPinPage(file, currentPageNum, false);
      throw NoSuchKeyFoundException();
    }
    // search from the left leaf page to the right to find the fit
    bool nullVal = false;

    for (int i = 0; i < leafOccupancy && !nullVal; i++)
    {
      int key = curNode->keyArray[i];
      // check if the next one in the key is not inserted
      if (i < leafOccupancy - 1 && curNode->ridArray[i + 1].page_number == 0)
      {
        nullVal = true;
      }
      if (validKey(lowValInt, lowOp, highValInt, highOp, key))
      {
        // select
        nextEntry = i;
        found = true;
        scanExecuting = true;
        break;
      }
      else if ((highOp == LT && key >= highValInt) || (highOp == LTE && key > highValInt))
      {
        bufMgr->unPinPage(file, currentPageNum, false);
        throw NoSuchKeyFoundException();
      }

      // did not find any matching key in this leaf, go to next leaf
      if (i == leafOccupancy - 1 || nullVal) {
        // unpin page
        bufMgr->unPinPage(file, currentPageNum, false);
        // did not find the matching one in the more right leaf
        if (curNode->rightSibPageNo == 0)
        {
          throw NoSuchKeyFoundException();
        }

        currentPageNum = curNode->rightSibPageNo;
        bufMgr->readPage(file, currentPageNum, currentPageData);
      }
    }
  }
}

// -----------------------------------------------------------------------------
// BTreeIndex::scanNext
// -----------------------------------------------------------------------------

/**
 * This method returns the next record from current page being scanned,
 * if current page has been scanned to its entirety, move on to the right sibling of current page, 
 * if any exists, to start scanning that page. Make sure to unpin any pages that are no longer required.
 */
const void BTreeIndex::scanNext(RecordId& outRid) 
{ 
  // if we are not executing scan, then we can't scan next
	if (!scanExecuting)
  {
    throw ScanNotInitializedException();
  }

	LeafNodeInt* curNode = (LeafNodeInt *) currentPageData;
  // go to the next page if we need
  if (curNode->ridArray[nextEntry].page_number == 0 || nextEntry == leafOccupancy)
  { 
    // no more next leaf
    if (curNode->rightSibPageNo == 0)
    {
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
  if (validKey(lowValInt, lowOp, highValInt, highOp, key))
  { 
    outRid = curNode->ridArray[nextEntry];
    // incrment nextEntry
    nextEntry++;
  }
  else
  { 
    // if current file has been scanned to its entirety
    throw IndexScanCompletedException();
  }
}

/**
 * This method checks whether a key is satisfied with the given operators and range.
 */
const bool BTreeIndex::validKey(int lowVal, const Operator lowOp, int highVal, const Operator highOp, int key)
{
  if (lowOp == GTE && highOp == LTE)
  {
    return key <= highVal && key >= lowVal;
  }
  else if (lowOp == GTE && highOp == LT)
  {
    return key < highVal && key >= lowVal;
  }
  else if (lowOp == GT && highOp == LTE)
  {
    return key <= highVal && key > lowVal;
  }
  else
  {
    return key < highVal && key > lowVal;
  }
}

// -----------------------------------------------------------------------------
// BTreeIndex::endScan
// -----------------------------------------------------------------------------

/**
 * This method terminates the current scan. Unpin any pinned pages. Reset scan specific variables.
 */
const void BTreeIndex::endScan() 
{
	if(!scanExecuting)
  {
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
}
