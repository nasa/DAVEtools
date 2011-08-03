package gov.nasa.daveml.dave;


import junit.framework.*;


public class BlockArrayListTest extends TestCase {

	private Signal _fs;
	private BlockInput _bi;
	private BlockMathConstant _b1;
	private BlockMathConstant _b2;
	private BlockMathConstant _b3;
	private Model _m;
	private BlockArrayList _bal;

	protected void setUp() throws Exception {
		_m  = new Model(2,2);
		_fs = new Signal();
		_bi = new BlockInput( _fs, _m );
		_b1 = new BlockMathConstant( "1", _m );
		_b2 = new BlockMathConstant( "2", _m );
		_b3 = new BlockMathConstant( "3", _m );
		_bal = new BlockArrayList( 3 );
	}
	
	public void testConstructor() throws Exception {
		assertEquals( 0, _bal.size() );
		assertTrue(      _bal.isEmpty() );
		try {
			_bal.get(0);
			fail("Did not get exception for IndexOutOfBounds in testConstructor() of TestBlockArrayList");
		} catch(Exception e ) {
			// we wanted to get an exception
		}
	}
	
	public void testAddToList() {
		_bal.add( _b1 );
		assertEquals( 1,    _bal.size() );
		assertTrue(         _bal.contains(_b1) );
		assertFalse(        _bal.contains(_b2) );
		assertFalse(        _bal.contains(_b3) );
		assertEquals( _b1,  _bal.get(0) );
		try { _bal.get(1); fail("Didn't get IndexOutOfBounds exception in testAddToList of TestBlockArrayList");
		} catch (Exception e) { }
		try { _bal.get(2); fail("Didn't get IndexOutOfBounds exception in testAddToList of TestBlockArrayList");
		} catch (Exception e) { }
		
		_bal.add( _b2 );
		assertEquals( 2,    _bal.size() );
		assertTrue(         _bal.contains(_b1) );
		assertTrue(         _bal.contains(_b2) );
		assertFalse(        _bal.contains(_b3) );
		assertEquals( _b1,  _bal.get(0) );
		assertEquals( _b2,  _bal.get(1) );
		try { _bal.get(2); fail("Didn't get IndexOutOfBounds exception in testAddToList of TestBlockArrayList");
		} catch (Exception e) { }

		_bal.add( _b3 );
		assertEquals( 3,    _bal.size() );
		assertTrue(         _bal.contains(_b1) );
		assertTrue(         _bal.contains(_b2) );
		assertTrue(         _bal.contains(_b3) );
		assertFalse(        _bal.contains(_bi) );
		assertEquals( 0,    _bal.indexOf(_b1) );
		assertEquals( 1,    _bal.indexOf(_b2) );
		assertEquals( 2,    _bal.indexOf(_b3) );
		assertEquals( _b1,  _bal.get(0) );
		assertEquals( _b2,  _bal.get(1) );
		assertEquals( _b3,  _bal.get(2) );
	}
	
	public void testAdd4BlkTo3BlkList() {
		_bal.add( _b1 );
		_bal.add( _b2 );
		_bal.add( _b3 );
		_bal.add( _bi );
		assertEquals( 4,    _bal.size() );
		assertTrue(         _bal.contains(_b1) );
		assertTrue(         _bal.contains(_b2) );
		assertTrue(         _bal.contains(_b3) );
		assertTrue(         _bal.contains(_bi) );
		assertEquals( 0,    _bal.indexOf(_b1) );
		assertEquals( 1,    _bal.indexOf(_b2) );
		assertEquals( 2,    _bal.indexOf(_b3) );
		assertEquals( 3,    _bal.indexOf(_bi) );
		assertEquals( _b1,  _bal.get(0) );
		assertEquals( _b2,  _bal.get(1) );
		assertEquals( _b3,  _bal.get(2) );
		assertEquals( _bi,  _bal.get(3) );
	}
	
	public void testAddDuptoBlkList() {
		_bal.add( _b1 );
		_bal.add( _b2 );
		_bal.add( _b1 );
		_bal.add( _b2 );
		assertEquals( 4, _bal.size() );
		assertTrue( _bal.contains( _b1 ) );
		assertTrue( _bal.contains( _b2 ) );
		assertFalse( _bal.contains( _b3 ) );
		assertFalse( _bal.contains( _bi ) );
		assertEquals( 0,    _bal.indexOf(_b1) );
		assertEquals( 1,    _bal.indexOf(_b2) );
		assertEquals( 2,    _bal.lastIndexOf(_b1) );
		assertEquals( 3,    _bal.lastIndexOf(_b2) );
		assertEquals( _b1,  _bal.get(0) );
		assertEquals( _b2,  _bal.get(1) );
		assertEquals( _b1,  _bal.get(2) );
		assertEquals( _b2,  _bal.get(3) );
	}
	
	public static Test suite() {
		return new TestSuite( BlockArrayListTest.class );
	}

	public static void main (String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}