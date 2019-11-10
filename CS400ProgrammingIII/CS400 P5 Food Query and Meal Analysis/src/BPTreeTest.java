import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class is built to test BPTree
 */
public class BPTreeTest {
	static BPTree<Integer, String> bpt;
	
	/**
	 * Code here will run once before the start of each test
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		bpt = new BPTree<Integer, String>(4);
	}
	
	/**
	 * Test insert
	 */
	@Test
  public void testInsert() {
		// insert several key, value pairs, include duplicated keys
		try {
			bpt.insert(0, "a");
	    bpt.insert(1, "b");
	    bpt.insert(1, "duplicate1");
	    bpt.insert(1, "duplicate2");
	    bpt.insert(1, "duplicate3");
	    bpt.insert(2, "c");
	    bpt.insert(2, "duplicate4");
	    bpt.insert(3, "d");
	    bpt.insert(4, "e");
	    bpt.insert(5, "f");
	    bpt.insert(6, "g");
	    bpt.insert(7, "h");
	    bpt.insert(8, "i");
	    bpt.insert(9, "j");
	    bpt.insert(9, "duplicate5");
	    bpt.insert(9, "duplicate6");
		} catch (Exception e) {
      fail("Catch unexpectec exception: "+e);
    }
  }
	
	/**
	 * Test RangeSearch
	 */
	@Test
	public void testRangeSearch() {
		try {
			bpt.insert(0, "a");
	    bpt.insert(1, "b");
	    bpt.insert(1, "duplicate1");
	    bpt.insert(1, "duplicate2");
	    bpt.insert(1, "duplicate3");
	    bpt.insert(1, "duplicate4");
	    bpt.insert(2, "c");
	    bpt.insert(8, "i");
	    bpt.insert(9, "j");
	    bpt.insert(9, "duplicate5");
	    bpt.insert(9, "duplicate6");
		} catch (Exception e) {
      fail("Catch unexpectec exception: "+e);
    }
		
		// there should be 4 key, value pairs for key >= 8 
		List<String> range = bpt.rangeSearch(8, ">=");
		assertEquals(range.size(), 4, 0.001); 
		
		// there should be 6 key, value pairs for key <= 1 
		range = bpt.rangeSearch(1, "<=");
		assertEquals(range.size(), 6, 0.001); 
		
		// there should be 1 key, value pair for key == 2 
		range = bpt.rangeSearch(2, "==");
		assertEquals(range.size(), 1, 0.001); 
	}
	
	@Test
	public void testToString() {
		try {
			bpt.insert(0, "a");
	    bpt.insert(1, "b");
	    bpt.insert(1, "duplicate1");
	    bpt.insert(1, "duplicate2");
	    bpt.insert(1, "duplicate3");
	    bpt.insert(1, "duplicate4");
	    bpt.insert(2, "c");
	    bpt.insert(8, "i");
	    bpt.insert(9, "j");
	    bpt.insert(9, "duplicate5");
	    bpt.insert(9, "duplicate6");
		} catch (Exception e) {
      fail("Catch unexpectec exception: "+e);
    }
		
		// its toString should print hierarchical order
		String result = bpt.toString();
		String answer = "{[2]}\n{[0, 1], [2, 8, 9]}\n";
		assertTrue(result.equals(answer));
	}
}
