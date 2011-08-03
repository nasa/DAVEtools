package gov.nasa.daveml.dave;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;

public class ParseTextTest extends TestCase {
	
	protected final double EPS = 0.000000001;
	
	protected ParseText pt;

	protected void setUp() throws Exception {
		super.setUp();
		
		pt  = new ParseText( "-4.0, 5.555, 6.6e-6, 3.45E2" );
	}

	public void testNext() {
		try {
			assertEquals( -4., pt.next() );
		} catch (IOException e) {
			fail("Unexpected exception in testNext(): " + e.getMessage() );
		}
		assertFalse( pt.eof() );
		assertTrue( pt.validNumber() );
		try {
			assertEquals( 5.555, pt.next() );
		} catch (IOException e) {
			fail("Unexpected exception in testNext(): " + e.getMessage() );
		}
		assertFalse( pt.eof() );
		assertTrue( pt.validNumber() );
		try {
			assertEquals( 6.6E-6, pt.next() );
		} catch (IOException e) {
			fail("Unexpected exception in testNext(): " + e.getMessage() );
		}
		assertFalse( pt.eof() );
		assertTrue( pt.validNumber() );
		try {
			assertEquals( 345., pt.next() );
		} catch (IOException e) {
			fail("Unexpected exception in testNext(): " + e.getMessage() );
		}
		assertFalse( pt.eof() );
		assertTrue( pt.validNumber() );
		try {
			assertEquals( Double.NaN, pt.next() );
		} catch (IOException e) {
			fail("Unexpected exception in testNext(): " + e.getMessage() );
		}
		assertTrue( pt.eof() );
		assertFalse( pt.validNumber() );
	}

	// test run-on numbers
	public void testBadList1() {
		double value = 0;
		ParseText badList = new ParseText("-4-6.5");
		assertNotNull( badList );
		try {
			value = badList.next();
		} catch (IOException e) {
			fail("Unexpected exception in testBadList1(): " + e.getMessage() );
		}
		assertEquals( Double.NaN, value ); // should return NaN
		assertFalse( badList.goodNumber );
		assertFalse( badList.validNumber() );
		assertTrue( badList.eof() );
	}
	
	// test double decimal numbers
	public void testBadList2() {
		double value = 0;
		ParseText badList = new ParseText("4.6.5");
		assertNotNull( badList );
		try {
			value = badList.next();
		} catch (IOException e) {
			fail("Unexpected exception in testBadList2(): " + e.getMessage() );
		}
		assertEquals( Double.NaN, value ); // should return NaN
		assertFalse( badList.goodNumber );
		assertFalse( badList.validNumber() );
		assertTrue( badList.eof() );
	}

	// test double exponent numbers
	public void testBadList3() {
		double value = 0;
		ParseText badList = new ParseText("4E6e5");
		assertNotNull( badList );
		try {
			value = badList.next();
		} catch (IOException e) {
			fail("Unexpected exception in testBadList3(): " + e.getMessage() );
		}
		assertEquals( Double.NaN, value ); // should return NaN
		assertFalse( badList.goodNumber );
		assertFalse( badList.validNumber() );
		assertTrue( badList.eof() );
	}

	// test to make sure we get preceding numbers OK
	public void testBadList4() {
		double value = 0;
		ParseText badList = new ParseText("3.32,4E6e5");
		assertNotNull( badList );
		
		// read first number
		try {
			value = badList.next();
		} catch (IOException e) {
			fail("Unexpected exception in testBadList4(): " + e.getMessage() );
		}
		assertEquals( 3.32, value );
		assertTrue( badList.validNumber() );
		assertFalse( badList.eof() );
		
		// try to read second number
		try {
			value = badList.next();
		} catch (IOException e) {
			fail("Unexpected exception in testBadList4(): " + e.getMessage() );
		}
	
		assertEquals( Double.NaN, value ); // should return NaN
		assertFalse( badList.goodNumber );
		assertFalse( badList.validNumber() );
		assertTrue( badList.eof() );
	}
	
	// test to make sure we ignore preceding garbage
	public void testBadList5() {
		double value = 0;
		ParseText badList = new ParseText("4E6e5,3.32");
		assertNotNull( badList );
		
		// read first number
		try {
			value = badList.next();
		} catch (IOException e) {
			fail("Unexpected exception in testBadList5(): " + e.getMessage() );
		}
		assertEquals( 3.32, value );
		assertTrue( badList.validNumber() );
		assertFalse( badList.eof() );
		
		// try to read second number
		try {
			value = badList.next();
		} catch (IOException e) {
			fail("Unexpected exception in testBadList5(): " + e.getMessage() );
		}
	
		assertEquals( Double.NaN, value ); // should return NaN
		assertFalse( badList.goodNumber );
		assertFalse( badList.validNumber() );
		assertTrue( badList.eof() );
	}
	
	public void testToList() {
		ArrayList<Double> theList = null;
		try {
			theList = pt.toList();
		} catch (IOException e) {
			fail("Unexpected exception in testToList(): " + e.getMessage() );
		}
		assertNotNull( theList );
		Iterator<Double> it = theList.iterator();
		assertNotNull( it );
		assertTrue( it.hasNext() );
		assertEquals( -4.0, it.next() );
		assertTrue( it.hasNext() );
		assertEquals( 5.555, it.next() );
		assertTrue( it.hasNext() );
		assertEquals( 0.0000066, it.next() );
		assertTrue( it.hasNext() );
		assertEquals( 345.0, it.next() );
		assertFalse( it.hasNext() );
	}

	public void testToListString() {
		ArrayList<Double> answers = new ArrayList<Double>(2);
		answers.add(-4.0);
		answers.add( 6.666);
		
		// with comma
		compareStringToList( "-4.,6.666"     , answers ); // comma, no space
		compareStringToList( "-4.0, 6.666"   , answers ); // comma, single space
		compareStringToList( "-4.,    6.666" , answers ); // comma, multi spaces
		compareStringToList( "-4.    ,6.666" , answers ); // multi spaces, comma
		compareStringToList( "-4.,	6.666" , answers ); // comma, tab
		compareStringToList( "-4.	,6.666"    , answers ); // tab, comma
		compareStringToList( "-4.    	,6.666", answers ); // multi-spaces, tab, comma
		compareStringToList( "-4,   	    	,  6.666", answers ); // mixed spaces, tab, comma

		compareStringToList( "-4.0  6.666"   , answers ); // single space
		compareStringToList( "-4.    6.666"  , answers ); // multi spaces
		compareStringToList( "-4.	6.666"     , answers ); // tab
		compareStringToList( "-4.    	6.666" , answers ); // multi-spaces, tab
		compareStringToList( "-4.   	    	  6.666", answers ); // mixed spaces, tab
		
		compareStringToList( "#-4 6.666"       , answers ); // ignore misc char at start
		compareStringToList( "-4 # 6.666"      , answers ); // ignore misc char in middle
		compareStringToList( "-4# 6.666"       , answers ); // ignore misc char in middle
		compareStringToList( "-4 #6.666"       , answers ); // ignore misc char in middle
		compareStringToList( "-4#6.666"        , answers ); // ignore misc char in middle
		compareStringToList( "-4 6.666#"       , answers ); // ignore misc char at end

		answers.add( -5567. );
		compareStringToList( "-4. 6.666\n-5567", answers ); // third number with newline, no decimal
		compareStringToList( "-4.\n6.666\n-5567,", answers ); // 2 newlines, extra comma
	}
	
	protected void compareStringToList( String string, ArrayList<Double> expectedValues) {
		ArrayList<Double> theList = null;
		try {
			theList = ParseText.toList(string);
		} catch (IOException e) {
			fail("Unexpected exception in testToListString() with string '"
					+ string + "': " + e.getMessage() );
		}
		assertEquals(expectedValues.size(), theList.size() );
		Iterator<Double> it = theList.iterator();
		assertNotNull( it );
		
		Iterator<Double> ev_it = expectedValues.iterator();
		
		while (ev_it.hasNext()) {
			assertTrue( it.hasNext() );
			assertEquals( ev_it.next(), it.next(), EPS );
		}
		assertFalse( it.hasNext() );
	}

}
